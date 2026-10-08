package com.anto426.uniapp.session

import com.anto426.uniapp.presentation.userMessage

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.demo.DemoAccount
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.model.LoginCareerOption
import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.session.AuthenticationResult
import com.anto426.unisdk.session.SessionResumeResult
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSession
import com.anto426.unisdk.session.UniUserProfile
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.transport.TransportService
import com.anto426.unisdk.transport.TransportSession
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

class SessionManager internal constructor(
    private val backend: UniBackendService,
    private val accountStore: UniAccountStore,
    private val localDataStore: UniLocalDataStore,
    private val prepareNotificationOwner: suspend (String?) -> Unit = {},
) {
    val avatars = com.anto426.uniapp.account.data.AccountAvatarStore(accountStore)
    private val lock = Mutex()
    private var activeSession: ActiveSessionContext? = null
    private var pendingProfileCatalog: List<LoginCareerOption> = emptyList()
    private var pendingProfileUsername: String? = null
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
                            resumeAccountLocked(account).also { restored ->
                                if (restored !is AppSessionState.Authenticated) prepareNotificationOwner(null)
                            }
                        }
                    }
                } catch (error: CancellationException) {
                    prepareNotificationOwner(null)
                    throw error
                } catch (error: Throwable) {
                    prepareNotificationOwner(null)
                    AppSessionState.SignedOut(error.userMessage { getString(Res.string.msg_impossibile_ripristinare_la_sessione_protetta) })
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
                    closeActiveSessionLocked(accountId)
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
                closeActiveSessionLocked()
                val account = accountStore.persistAuthenticatedAccount(credentials, demoProfileLocked(),
                    com.anto426.unisdk.session.UniSessionTicket.restore("local-demo-v1".encodeToByteArray()))
                mutableState.value = AppSessionState.Authenticated(account)
                mutableAccountsRevision.value += 1
                return@withLock
            }
            // The login form can authenticate a different person than the account awaiting
            // reauthentication. Do not pass that account's push token into the new login.
            val notificationOwner = preferredAccountId?.takeIf { accountId ->
                accountStore.matchesStoredUsername(accountId, credentials.username)
            }
            prepareNotificationOwner(notificationOwner)
            mutableState.value = AppSessionState.Authenticating
            // A new login owns the process session; the previous SDK capability cannot be reused.
            try {
                closeActiveSessionLocked()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // The old session reference is already gone; login can continue.
            }
            mutableState.value =
                try {
                    authenticateLocked(credentials, selectedCareer, preferredAccountId)
                } catch (error: CancellationException) {
                    prepareNotificationOwner(null)
                    throw error
                } catch (error: Throwable) {
                    prepareNotificationOwner(null)
                    AppSessionState.SignedOut(error.userMessage { getString(Res.string.msg_accesso_non_riuscito) })
                }
            prepareNotificationOwner((mutableState.value as? AppSessionState.Authenticated)?.account?.accountId)
        }
    }

    suspend fun cancelAuthentication() {
        lock.withLock {
            if (mutableState.value is AppSessionState.CareerSelectionRequired) {
                pendingProfileCatalog = emptyList()
                pendingProfileUsername = null
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
            // Hide private routes while the new session is validated. On failure restore the
            // previous account without publishing any target data.
            mutableState.value = AppSessionState.Switching
            try {
                prepareNotificationOwner(accountId)
                activateStoredAccount(accountId).also { nextState ->
                    mutableState.value = nextState
                }
            } catch (error: Throwable) {
                mutableState.value = current?.let(AppSessionState::Authenticated) ?: AppSessionState.SignedOut()
                // Restore navigation before any suspend cleanup, including a cancelled caller.
                try { prepareNotificationOwner(current?.accountId) }
                catch (_: Throwable) { /* The runtime state observer also restores the owner. */ }
                throw error
            }
        }

    private suspend fun activateStoredAccount(accountId: String): AppSessionState {
        val account = accountStore.snapshot().accounts.first { it.accountId == accountId }
        val next = if (DemoAccount.isDemo(account)) {
            accountStore.setActiveAccount(accountId)
            AppSessionState.Authenticated(refreshDemoProfileLocked(account))
        } else resumeAccountLocked(account)
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
            mutableState.value = AppSessionState.Switching
            try {
                activateProfileLocked(current, profileId).also { nextAccount ->
                    mutableState.value = AppSessionState.Authenticated(nextAccount)
                }
                mutableState.value
            } catch (error: Throwable) {
                mutableState.value = AppSessionState.Authenticated(current)
                throw error
            }
        }

    suspend fun signOut() {
        lock.withLock {
            prepareNotificationOwner(null)
            closeActiveSessionLocked()
            accountStore.setActiveAccount(null)
            mutableState.value = AppSessionState.SignedOut()
        }
    }

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
            closeActiveSessionLocked(accountId)
            accountStore.forgetAccount(accountId)
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

    private suspend fun authenticateLocked(
        credentials: UniAccountCredentials,
        selectedCareer: LoginCareerOption?,
        preferredAccountId: String?,
    ): AppSessionState {
        val username = credentials.username.trim().lowercase()
        if (pendingProfileUsername != username) {
            pendingProfileCatalog = emptyList()
            pendingProfileUsername = null
        }
        return when (val result = backend.login(UniCredentials(credentials.username, credentials.password), selectedCareer)) {
            is AuthenticationResult.CareerSelectionRequired -> {
                pendingProfileCatalog = result.careers
                pendingProfileUsername = username
                AppSessionState.CareerSelectionRequired(result.careers)
            }
            is AuthenticationResult.Authenticated -> {
                if (!result.profile.matchesSelectedCareer(selectedCareer)) {
                    runCatching { backend.closeSession(result.session) }
                    error(getString(Res.string.msg_il_portale_non_ha_attivato_il_profilo_selezionato))
                }
                val profile = result.profile.withProfileCatalog(pendingProfileCatalog, selectedCareer)
                val account = try {
                    accountStore.persistAuthenticatedAccount(credentials, profile, result.ticket, preferredAccountId)
                } catch (error: Throwable) {
                    runCatching { backend.closeSession(result.session) }
                    throw error
                }
                pendingProfileCatalog = emptyList()
                pendingProfileUsername = null
                installSessionLocked(ActiveSessionContext(account, profile, result.session))
                mutableAccountsRevision.value += 1
                AppSessionState.Authenticated(account)
            }
        }
    }

    private suspend fun resumeAccountLocked(account: UniAccountSummary): AppSessionState {
        activeSession?.takeIf { it.account.accountId == account.accountId }?.let {
            accountStore.setActiveAccount(account.accountId)
            return AppSessionState.Authenticated(it.account)
        }
        val ticket = accountStore.loadSessionTicket(account.accountId)
            ?: return reauthenticationState(account)
        return accountStore.withCredentials(account.accountId) { credentials ->
            when (val result = backend.resumeSession(ticket, credentials)) {
                SessionResumeResult.ReauthenticationRequired -> {
                    accountStore.clearSessionTicket(account.accountId)
                    reauthenticationState(account)
                }
                is SessionResumeResult.Resumed -> {
                    val selected = account.profiles.firstOrNull { it.profileId == account.activeProfileId }
                        ?.toLoginCareerOption()
                    if (result.profile.id != account.serverUserId ||
                        (account.activeProfileId != null && selected == null) ||
                        !result.profile.matchesSelectedCareer(selected)
                    ) {
                        runCatching { backend.closeSession(result.session) }
                        accountStore.clearSessionTicket(account.accountId)
                        return@withCredentials reauthenticationState(account)
                    }
                    val profile = result.profile.withProfileCatalog(account.profiles.map { it.toLoginCareerOption() }, selected)
                    val updated = try {
                        accountStore.updateSession(account.accountId, profile, result.ticket).also {
                            accountStore.setActiveAccount(account.accountId)
                        }
                    } catch (error: Throwable) {
                        runCatching { backend.closeSession(result.session) }
                        throw error
                    }
                    installSessionLocked(ActiveSessionContext(updated, profile, result.session))
                    AppSessionState.Authenticated(updated)
                }
            }
        }
    }

    private suspend fun activateProfileLocked(account: UniAccountSummary, profileId: String): UniAccountSummary {
        val selected = account.profiles.firstOrNull { it.profileId == profileId }
            ?.toLoginCareerOption() ?: throw IllegalArgumentException("Unknown account profile")
        return accountStore.withCredentials(account.accountId) { credentials ->
            val result = backend.login(credentials, selected)
            val authenticated = result as? AuthenticationResult.Authenticated
                ?: error(getString(Res.string.msg_il_portale_richiede_ancora_la_scelta_della_carriera))
            if (authenticated.profile.id != account.serverUserId ||
                !authenticated.profile.matchesSelectedCareer(selected)
            ) {
                runCatching { backend.closeSession(authenticated.session) }
                error(getString(Res.string.msg_il_portale_ha_restituito_una_carriera_diversa))
            }
            val profile = authenticated.profile.withProfileCatalog(
                account.profiles.map { it.toLoginCareerOption() }, selected,
            )
            val updated = try {
                accountStore.updateSession(account.accountId, profile, authenticated.ticket)
            } catch (error: Throwable) {
                runCatching { backend.closeSession(authenticated.session) }
                throw error
            }
            installSessionLocked(ActiveSessionContext(updated, profile, authenticated.session))
            updated
        }
    }

    private suspend fun installSessionLocked(next: ActiveSessionContext) {
        val previous = activeSession
        activeSession = next
        if (previous != null && previous.session !== next.session) {
            runCatching { backend.closeSession(previous.session) }
        }
    }

    private suspend fun closeActiveSessionLocked(accountId: String? = null) {
        val current = activeSession ?: return
        if (accountId != null && current.account.accountId != accountId) return
        activeSession = null
        backend.closeSession(current.session)
    }

    internal suspend fun <T> callAuthenticated(
        accountId: String,
        profileId: String?,
        block: suspend UniBackendService.(UniSession, UniCredentials) -> T,
    ): T {
        val active = activeContextFor(accountId, profileId)
        val value = accountStore.withCredentials(accountId) { credentials ->
            backend.block(active.session, credentials)
        }
        ensureActiveContext(active)
        return value
    }

    internal suspend fun <T> withTransportSession(
        accountId: String,
        profileId: String?,
        block: suspend TransportService.(TransportSession) -> T,
    ): T {
        val active = activeContextFor(accountId, profileId)
        val value = accountStore.withCredentials(accountId) { credentials ->
            val transport = backend.openTransportSession(active.session, credentials)
            try { backend.block(transport) }
            finally { backend.closeTransportSession(transport) }
        }
        ensureActiveContext(active)
        return value
    }

    private suspend fun activeContextFor(accountId: String, profileId: String?): ActiveSessionContext =
        lock.withLock {
            val current = activeSession ?: throw InactiveAccountSessionException(accountId)
            val stateAccount = (mutableState.value as? AppSessionState.Authenticated)?.account
            if (current.account.accountId != accountId ||
                current.account.activeProfileId != profileId ||
                stateAccount?.accountId != accountId || stateAccount.activeProfileId != profileId
            ) throw InactiveAccountSessionException(accountId)
            current
        }

    private suspend fun ensureActiveContext(expected: ActiveSessionContext) {
        lock.withLock {
            if (activeSession !== expected || mutableState.value !is AppSessionState.Authenticated) {
                throw CancellationException("The account or career changed while loading data")
            }
        }
    }

    internal suspend fun shutdown() {
        lock.withLock {
            pendingProfileCatalog = emptyList()
            pendingProfileUsername = null
            runCatching { closeActiveSessionLocked() }
        }
    }

    private fun reauthenticationState(account: UniAccountSummary) =
        AppSessionState.ReauthenticationRequired(
            account,
            "La sessione di ${account.displayName} è scaduta. Accedi di nuovo per continuare.",
        )

    private suspend fun requiresBiometricUnlock(accountId: String): Boolean =
        localDataStore.read(LocalDataScope.Account(accountId), UniAppDataKeys.BiometricUnlock)
}

private data class ActiveSessionContext(
    val account: UniAccountSummary,
    val profile: UniUserProfile,
    val session: UniSession,
)

internal fun UniUserProfile.matchesSelectedCareer(selected: LoginCareerOption?): Boolean =
    selected == null || activeProfileId == selected.profileId

private fun UniUserProfile.withProfileCatalog(
    catalog: List<LoginCareerOption>,
    selected: LoginCareerOption?,
): UniUserProfile {
    if (catalog.isEmpty()) return this
    val profiles = catalog.map { option ->
        UniCareerProfile(
            profileId = option.profileId,
            displayName = option.displayName,
            degreeName = option.degreeName,
            matricola = option.matricola,
            matId = option.matId,
            stuId = option.stuId,
            anaId = option.anaId,
            cdsId = option.cdsId,
            dipId = option.dipId,
            departmentName = option.departmentName,
            teacherId = option.teacherId,
            type = option.type,
        )
    }
    val activeId = selected?.profileId ?: activeProfileId
    return copy(
        activeProfileId = activeId,
        profiles = profiles,
        activeProfileType = profiles.firstOrNull { it.profileId == activeId }?.type ?: activeProfileType,
    )
}

private fun com.anto426.uniapp.account.model.UniAccountProfileSummary.toLoginCareerOption() =
    LoginCareerOption(
        displayName = displayName,
        degreeName = degreeName,
        matricola = matricola,
        matId = matId,
        stuId = stuId,
        anaId = anaId,
        cdsId = cdsId,
        dipId = dipId,
        departmentName = departmentName,
        teacherId = teacherId,
        type = type,
    )

class InactiveAccountSessionException(accountId: String) :
    IllegalStateException("Account '$accountId' does not have an active RAM session")

internal enum class PasswordUnlockResult { Unlocked, Invalid, RetryLater }
