package com.anto426.uniapp.session

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.session.ManagedAuthenticationResult
import com.anto426.uniapp.account.session.ManagedSessionResult
import com.anto426.uniapp.account.session.UniAccountClient
import com.anto426.uniapp.account.session.UniSessionCoordinator
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.model.LoginCareerOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import com.anto426.uniapp.security.password.verifyAppPassword
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.account.authorizeAccountRemoval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppSessionController internal constructor(
    private val coordinator: UniSessionCoordinator,
    private val accountStore: UniAccountStore,
    private val localDataStore: UniLocalDataStore,
) {
    private val lock = Mutex()
    private val mutableState = MutableStateFlow<AppSessionState>(AppSessionState.Initializing)
    private val mutableAccountsRevision = MutableStateFlow(0L)
    internal val accountsRevision: StateFlow<Long> = mutableAccountsRevision.asStateFlow()

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
                        requiresBiometricUnlock(account.accountId) ->
                            AppSessionState.UnlockRequired(account)
                        else -> coordinator.resumeActiveAccount().toAppState()
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    AppSessionState.SignedOut(error.message ?: getString(Res.string.msg_impossibile_ripristinare_la_sessione_protetta))
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

    /** Checks and opens the same account under one lock; switching cannot reuse this result. */
    internal suspend fun unlockWithPassword(password: String): PasswordUnlockResult =
        lock.withLock {
            val requirement = mutableState.value as? AppSessionState.UnlockRequired
                ?: return@withLock PasswordUnlockResult.Invalid
            if (password.isEmpty() || password.length > 128) return@withLock PasswordUnlockResult.Invalid
            val scope = LocalDataScope.Account(requirement.account.accountId)
            val now = Clock.System.now().toEpochMilliseconds()
            if (now < localDataStore.read(scope, UniAppDataKeys.PasswordRetryAfter)) {
                return@withLock PasswordUnlockResult.RetryLater
            }
            val verifier = localDataStore.read(scope, UniAppDataKeys.PasswordVerifier)
                ?: return@withLock PasswordUnlockResult.Invalid
            // Persist the throttle before verification, including cancelled attempts or restarts.
            localDataStore.write(scope, UniAppDataKeys.PasswordRetryAfter, now + 5_000L)
            val valid = withContext(Dispatchers.Default) { verifyAppPassword(password, verifier) }
            if (!valid) return@withLock PasswordUnlockResult.Invalid
            localDataStore.remove(scope, UniAppDataKeys.PasswordRetryAfter)
            // Leave UnlockRequired in place until activation succeeds, allowing a safe retry.
            mutableState.value = coordinator.activate(requirement.account.accountId).toAppState()
            PasswordUnlockResult.Unlocked
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
                    AppSessionState.SignedOut(error.message ?: getString(Res.string.msg_accesso_non_riuscito))
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
            if (requiresBiometricUnlock(accountId)) {
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
                    ?: throw IllegalStateException(getString(Res.string.msg_nessun_account_attivo))
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

    /** Local deletion only. Authorization applies to the target account, even if it is inactive. */
    internal suspend fun removeAccount(accountId: String, authenticator: BiometricAuthenticator): Boolean =
        lock.withLock {
            require(accountStore.snapshot().accounts.any { it.accountId == accountId }) { "Unknown account" }
            val isProtected = requiresBiometricUnlock(accountId)
            if (!authorizeAccountRemoval(isProtected, authenticator)) return@withLock false
            removeAccountLocked(accountId)
            true
        }

    private suspend fun removeAccountLocked(accountId: String) {
        val previous = mutableState.value
        try {
            coordinator.forgetAccount(accountId)
        } finally {
            // Closing the runtime session may have succeeded even if vault cleanup failed.
            mutableState.value = when (previous) {
                is AppSessionState.Authenticated ->
                    if (previous.account.accountId == accountId) AppSessionState.SignedOut() else previous
                is AppSessionState.UnlockRequired -> when {
                    previous.account.accountId == accountId ->
                        previous.fallbackAccount?.let(AppSessionState::Authenticated) ?: AppSessionState.SignedOut()
                    previous.fallbackAccount?.accountId == accountId -> previous.copy(fallbackAccount = null)
                    else -> previous
                }
                is AppSessionState.ReauthenticationRequired ->
                    if (previous.account.accountId == accountId) AppSessionState.SignedOut() else previous
                else -> previous
            }
            mutableAccountsRevision.value += 1
        }
    }

    internal suspend fun cachedProfileImage(account: UniAccountSummary): ByteArray? =
        account.photoUrl
            ?.takeIf(String::isNotBlank)
            ?.let { source -> accountStore.readProfileImage(account.accountId, source)?.bytes }

    private suspend fun requiresBiometricUnlock(accountId: String): Boolean =
        localDataStore.read(LocalDataScope.Account(accountId), UniAppDataKeys.BiometricUnlock)

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

internal enum class PasswordUnlockResult { Unlocked, Invalid, RetryLater }
