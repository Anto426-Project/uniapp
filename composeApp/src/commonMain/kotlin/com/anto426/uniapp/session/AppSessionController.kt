package com.anto426.uniapp.session

import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.session.ManagedAuthenticationResult
import com.anto426.uniapp.account.session.ManagedSessionResult
import com.anto426.uniapp.account.session.UniAccountClient
import com.anto426.uniapp.account.session.UniSessionCoordinator
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.security.account.AccountSecurityPreferences
import com.anto426.unisdk.backend.model.LoginCareerOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppSessionController internal constructor(
    private val coordinator: UniSessionCoordinator,
    private val accountStore: UniAccountStore,
) {
    private val lock = Mutex()
    private val mutableState = MutableStateFlow<AppSessionState>(AppSessionState.Initializing)

    val state: StateFlow<AppSessionState> = mutableState.asStateFlow()

    suspend fun initialize() {
        lock.withLock {
            if (mutableState.value !is AppSessionState.Initializing) return
            mutableState.value =
                try {
                    val snapshot = accountStore.snapshot()
                    val account = snapshot.activeAccountId?.let { activeId ->
                        snapshot.accounts.firstOrNull { it.accountId == activeId }
                    }
                    when {
                        account == null -> AppSessionState.SignedOut()
                        accountStore.requiresBiometricUnlock(account.accountId) ->
                            AppSessionState.UnlockRequired(account)
                        else -> coordinator.resumeActiveAccount().toAppState()
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    AppSessionState.SignedOut(error.message ?: "Impossibile ripristinare la sessione protetta")
                }
        }
    }

    suspend fun unlockRequiredAccount() {
        lock.withLock {
            val requirement = mutableState.value as? AppSessionState.UnlockRequired ?: return
            mutableState.value = AppSessionState.Initializing
            mutableState.value =
                try {
                    coordinator.activate(requirement.account.accountId).toAppState()
                } catch (error: CancellationException) {
                    mutableState.value = requirement
                    throw error
                } catch (error: Throwable) {
                    mutableState.value = requirement
                    throw error
                }
        }
    }

    suspend fun cancelUnlock() {
        lock.withLock {
            val requirement = mutableState.value as? AppSessionState.UnlockRequired ?: return
            mutableState.value =
                requirement.fallbackAccount?.let(AppSessionState::Authenticated)
                    ?: run {
                        accountStore.setActiveAccount(null)
                        AppSessionState.SignedOut()
                    }
        }
    }

    suspend fun authenticate(
        credentials: UniAccountCredentials,
        selectedCareer: LoginCareerOption? = null,
        preferredAccountId: String? = null,
    ) {
        lock.withLock {
            mutableState.value = AppSessionState.Authenticating
            mutableState.value =
                try {
                    when (
                        val result =
                            coordinator.authenticate(
                                credentials = credentials,
                                selectedCareer = selectedCareer,
                                preferredAccountId = preferredAccountId,
                            )
                    ) {
                        is ManagedAuthenticationResult.Authenticated ->
                            AppSessionState.Authenticated(result.active.account)

                        is ManagedAuthenticationResult.CareerSelectionRequired ->
                            AppSessionState.CareerSelectionRequired(result.careers)
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    AppSessionState.SignedOut(error.message ?: "Accesso non riuscito")
                }
        }
    }

    suspend fun cancelAuthentication() {
        lock.withLock {
            if (mutableState.value is AppSessionState.CareerSelectionRequired) {
                mutableState.value = AppSessionState.SignedOut()
            }
        }
    }

    suspend fun activate(accountId: String): AppSessionState =
        lock.withLock {
            val snapshot = accountStore.snapshot()
            val target = snapshot.accounts.firstOrNull { it.accountId == accountId }
                ?: throw IllegalArgumentException("Unknown account")
            val current = (mutableState.value as? AppSessionState.Authenticated)?.account
            if (accountStore.requiresBiometricUnlock(accountId)) {
                return@withLock AppSessionState.UnlockRequired(
                    account = target,
                    fallbackAccount = current,
                ).also { mutableState.value = it }
            }
            // Do not publish an intermediate state and do not hide resume failures: callers must
            // only report a successful switch after the selected account is actually active.
            coordinator.activate(accountId).toAppState().also { nextState ->
                mutableState.value = nextState
            }
        }

    suspend fun activateProfile(profileId: String): AppSessionState =
        lock.withLock {
            val current =
                (mutableState.value as? AppSessionState.Authenticated)?.account
                    ?: throw IllegalStateException("Nessun account attivo")
            if (current.activeProfileId == profileId) return@withLock mutableState.value
            coordinator.activateProfile(current.accountId, profileId).toAppState().also { nextState ->
                mutableState.value = nextState
            }
        }

    suspend fun signOut() {
        lock.withLock {
            (mutableState.value as? AppSessionState.Authenticated)?.account?.accountId?.let { accountId ->
                coordinator.closeRuntimeSession(accountId)
            }
            accountStore.setActiveAccount(null)
            mutableState.value = AppSessionState.SignedOut()
        }
    }

    fun currentAccountClient(): UniAccountClient? =
        (mutableState.value as? AppSessionState.Authenticated)
            ?.account
            ?.accountId
            ?.let(coordinator::accountClient)

    internal fun accountClient(accountId: String): UniAccountClient? =
        (mutableState.value as? AppSessionState.Authenticated)
            ?.account
            ?.accountId
            ?.takeIf { it == accountId }
            ?.let(coordinator::accountClient)

    suspend fun accounts(): List<UniAccountSummary> = accountStore.snapshot().accounts

    internal suspend fun cachedProfileImage(account: UniAccountSummary): ByteArray? =
        account.photoUrl
            ?.takeIf(String::isNotBlank)
            ?.let { source -> accountStore.readProfileImage(account.accountId, source)?.bytes }

    private suspend fun UniAccountStore.requiresBiometricUnlock(accountId: String): Boolean =
        readPreference(accountId, AccountSecurityPreferences.BIOMETRIC_UNLOCK)
            ?.toBooleanStrictOrNull() == true

    private fun ManagedSessionResult.toAppState(): AppSessionState =
        when (this) {
            is ManagedSessionResult.Active -> AppSessionState.Authenticated(value.account)
            ManagedSessionResult.NoActiveAccount -> AppSessionState.SignedOut()
            is ManagedSessionResult.ReauthenticationRequired ->
                AppSessionState.ReauthenticationRequired(
                    account = account,
                    message = "La sessione di ${account.displayName} è scaduta. Accedi di nuovo per continuare.",
                )
        }
}
