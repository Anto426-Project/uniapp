package com.anto426.uniapp.session

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.demo.DemoAccount
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
    private val prepareNotificationOwner: suspend (String?) -> Unit = {},
) {
    val avatars = com.anto426.uniapp.account.data.AccountAvatarStore(accountStore)
    private val lock = Mutex()
    private var demoAuthor: com.anto426.uniapp.project.model.GitHubAuthor? = null
    private val mutableState = MutableStateFlow<AppSessionState>(AppSessionState.Initializing)
    private val mutableAccountsRevision = MutableStateFlow(0L)
    internal val accountsRevision: StateFlow<Long> = mutableAccountsRevision.asStateFlow()

    val state: StateFlow<AppSessionState> = mutableState.asStateFlow()
    internal val storageResetNotice = accountStore.storageResetNotice
    internal suspend fun acknowledgeStorageReset() = accountStore.acknowledgeStorageReset()

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
                        account == null -> {
                            prepareNotificationOwner(null)
                            AppSessionState.SignedOut()
                        }
                        requiresBiometricUnlock(account.accountId) -> {
                            prepareNotificationOwner(null)
                            AppSessionState.UnlockRequired(account)
                        }
                        DemoAccount.isDemo(account) -> {
                            prepareNotificationOwner(null)
                            AppSessionState.Authenticated(refreshDemoProfileLocked(account))
                        }
                        else -> {
                            prepareNotificationOwner(account.accountId)
                            coordinator.resumeActiveAccount().toAppState().also { restored ->
                                if (restored !is AppSessionState.Authenticated) prepareNotificationOwner(null)
                            }
                        }
                    }
                } catch (error: CancellationException) {
                    prepareNotificationOwner(null)
                    throw error
                } catch (error: Throwable) {
                    prepareNotificationOwner(null)
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
                    prepareNotificationOwner(requirement.account.accountId)
                    activateStoredAccount(requirement.account.accountId)
                } catch (error: CancellationException) {
                    prepareNotificationOwner(null)
                    mutableState.value = requirement
                    throw error
                } catch (error: Throwable) {
                    prepareNotificationOwner(null)
                    mutableState.value = requirement
                    throw error
                }
        }
    }

    suspend fun cancelUnlock() {
        lock.withLock {
            val requirement = mutableState.value as? AppSessionState.UnlockRequired ?: return
            prepareNotificationOwner(null)
            accountStore.setActiveAccount(null)
            mutableState.value = AppSessionState.SignedOut()
            requirement.fallbackAccount?.accountId?.let { accountId ->
                try {
                    coordinator.closeRuntimeSession(accountId)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    // The login screen remains available even if closing the old RAM session fails.
                }
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
            try {
                prepareNotificationOwner(requirement.account.accountId)
                mutableState.value = activateStoredAccount(requirement.account.accountId)
            } catch (error: Throwable) {
                prepareNotificationOwner(null)
                throw error
            }
            PasswordUnlockResult.Unlocked
        }

    suspend fun authenticate(
        credentials: UniAccountCredentials,
        selectedCareer: LoginCareerOption? = null,
        preferredAccountId: String? = null,
    ) {
        lock.withLock {
            if (DemoAccount.requested(credentials)) {
                prepareNotificationOwner(null)
                if (!DemoAccount.accepts(credentials)) {
                    mutableState.value = AppSessionState.SignedOut(getString(Res.string.ui_demo_invalid_credentials))
                    return@withLock
                }
                val account = accountStore.persistAuthenticatedAccount(credentials, demoProfileLocked(),
                    com.anto426.unisdk.session.UniSessionTicket.restore("local-demo-v1".encodeToByteArray()))
                mutableState.value = AppSessionState.Authenticated(account)
                mutableAccountsRevision.value += 1
                return@withLock
            }
            prepareNotificationOwner(preferredAccountId)
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
                    prepareNotificationOwner(null)
                    throw error
                } catch (error: Throwable) {
                    prepareNotificationOwner(null)
                    AppSessionState.SignedOut(error.message ?: getString(Res.string.msg_accesso_non_riuscito))
                }
            prepareNotificationOwner((mutableState.value as? AppSessionState.Authenticated)?.account?.accountId)
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
                prepareNotificationOwner(null)
                return@withLock AppSessionState.UnlockRequired(
                    account = target,
                    fallbackAccount = current,
                ).also { mutableState.value = it }
            }
            // Do not publish an intermediate state and do not hide resume failures: callers must
            // only report a successful switch after the selected account is actually active.
            try {
                prepareNotificationOwner(accountId)
                activateStoredAccount(accountId).also { nextState -> mutableState.value = nextState }
            } catch (error: Throwable) {
                prepareNotificationOwner(current?.accountId)
                throw error
            }
        }

    private suspend fun activateStoredAccount(accountId: String): AppSessionState {
        val account = accountStore.snapshot().accounts.first { it.accountId == accountId }
        val next = if (DemoAccount.isDemo(account)) {
            accountStore.setActiveAccount(accountId)
            AppSessionState.Authenticated(refreshDemoProfileLocked(account))
        } else coordinator.activate(accountId).toAppState()
        if (next !is AppSessionState.Authenticated || DemoAccount.isDemo(account)) {
            prepareNotificationOwner(null)
        }
        return next
    }

    suspend fun activateProfile(profileId: String): AppSessionState =
        lock.withLock {
            val current =
                (mutableState.value as? AppSessionState.Authenticated)?.account
                    ?: throw IllegalStateException(getString(Res.string.msg_nessun_account_attivo))
            if (current.activeProfileId == profileId) return@withLock mutableState.value
            coordinator.activateProfile(current.accountId, profileId).toAppState().also { nextState ->
                if (nextState !is AppSessionState.Authenticated) prepareNotificationOwner(null)
                mutableState.value = nextState
            }
        }

    suspend fun signOut() {
        lock.withLock {
            prepareNotificationOwner(null)
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
            ?.takeUnless(DemoAccount::isDemo)
            ?.accountId
            ?.let(coordinator::accountClient)

    internal fun accountClient(accountId: String): UniAccountClient? =
        (mutableState.value as? AppSessionState.Authenticated)
            ?.account
            ?.takeUnless(DemoAccount::isDemo)
            ?.accountId
            ?.takeIf { it == accountId }
            ?.let(coordinator::accountClient)

    suspend fun accounts(): List<UniAccountSummary> = accountStore.snapshot().accounts

    /** Public author metadata never authenticates or changes a university account. */
    internal suspend fun updateDemoAuthor(author: com.anto426.uniapp.project.model.GitHubAuthor) = lock.withLock {
        if (!author.login.equals(com.anto426.unisdk.platform.ProjectInfo.authorLogin, ignoreCase = true)) return@withLock
        demoAuthor = author
        val current = (mutableState.value as? AppSessionState.Authenticated)?.account ?: return@withLock
        if (!DemoAccount.isDemo(current)) return@withLock
        val updated = refreshDemoProfileLocked(current)
        if (updated != current) mutableState.value = AppSessionState.Authenticated(updated)
    }

    private suspend fun demoProfileLocked(): com.anto426.unisdk.session.UniUserProfile {
        if (demoAuthor == null) {
            demoAuthor = try { localDataStore.read(LocalDataScope.Application, UniAppDataKeys.GitHubProject).author }
                catch (error: CancellationException) { throw error } catch (_: Exception) { null }
        }
        return DemoAccount.profile(demoAuthor)
    }

    private suspend fun refreshDemoProfileLocked(account: UniAccountSummary): UniAccountSummary {
        val profile = demoProfileLocked()
        // Keep an already resolved identity when offline and the public cache is unavailable.
        if (demoAuthor == null || (account.displayName == profile.displayName && account.photoUrl == profile.photoUrl)) return account
        return try {
            accountStore.updateSession(account.accountId, profile,
                com.anto426.unisdk.session.UniSessionTicket.restore("local-demo-v1".encodeToByteArray())).also {
                mutableAccountsRevision.value += 1
            }
        } catch (error: CancellationException) { throw error } catch (_: Exception) { account }
    }

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
            prepareNotificationOwner((mutableState.value as? AppSessionState.Authenticated)?.account?.accountId)
            localDataStore.invalidateAccount(accountId)
            avatars.remove(accountId)
            mutableAccountsRevision.value += 1
        }
    }

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
