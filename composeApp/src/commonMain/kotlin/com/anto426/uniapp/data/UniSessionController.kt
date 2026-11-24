package com.anto426.uniapp.data

import com.anto426.uniapp.account.data.AccountAvatarStore
import com.anto426.uniapp.account.model.MissingAccountCredentialsException
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniLoginProfileOption
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.demo.DemoAccount
import com.anto426.uniapp.localization.readAppString
import com.anto426.uniapp.presentation.userMessage
import com.anto426.uniapp.project.model.GitHubAuthor
import com.anto426.uniapp.security.account.authorizeAccountRemoval
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.password.verifyAppPassword
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.errors.UniSdkErrorCode
import com.anto426.unisdk.errors.UniSdkException
import com.anto426.unisdk.errors.uniSdkErrorCode
import com.anto426.unisdk.library.LibraryService
import com.anto426.unisdk.library.LibrarySession
import com.anto426.unisdk.library.RemoteLibraryService
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.LoginCareerOption
import com.anto426.unisdk.platform.ProjectInfo
import com.anto426.unisdk.platform.currentEpochMillis
import com.anto426.unisdk.session.AuthenticationResult
import com.anto426.unisdk.session.InvalidUniSessionTicketException
import com.anto426.unisdk.session.SessionResumeResult
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSession
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import com.anto426.unisdk.transport.TransportService
import com.anto426.unisdk.transport.TransportSession
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.IOException
import uniapp.composeapp.generated.resources.*

enum class PasswordUnlockResult { Unlocked, Invalid, RetryLater }

class InactiveAccountSessionException(accountId: String) :
    IllegalStateException("Account '$accountId' does not have an active RAM session")

class UniSessionController internal constructor(
    private val backend: UniBackendService,
    private val accountStore: UniAccountStore,
    private val localDataStore: UniLocalDataStore,
    private val libraryService: LibraryService = RemoteLibraryService(),
    private val nowMillis: () -> Long = ::currentEpochMillis,
    private val prepareNotificationOwner: suspend (String?) -> Unit = {},
) {
    val avatars = AccountAvatarStore(accountStore)
    private val lock = Mutex()
    private val onlineResumeLock = Mutex()
    private val libraryLock = Mutex()
    private var activeSession: ActiveSessionContext? = null
    private var activeLibrarySession: LibrarySessionContext? = null
    private var guestLibrarySession: LibrarySession? = null
    private var pendingProfileCatalog: List<LoginCareerOption> = emptyList()
    private var pendingProfileUsername: String? = null
    private var demoAuthor: GitHubAuthor? = null
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
                    AppSessionState.SignedOut(error.userMessage { readAppString(Res.string.msg_impossibile_ripristinare_la_sessione_protetta) })
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
            val fallback = requirement.fallbackAccount?.takeIf { previous ->
                DemoAccount.isDemo(previous) ||
                    activeSession?.account?.let {
                        it.accountId == previous.accountId && it.activeProfileId == previous.activeProfileId
                    } == true
            }
            if (fallback != null) {
                prepareNotificationOwner(fallback.accountId)
                mutableState.value = AppSessionState.Authenticated(fallback)
                return
            }
            prepareNotificationOwner(null)
            accountStore.setActiveAccount(null)
            mutableState.value = AppSessionState.SignedOut()
            requirement.fallbackAccount?.accountId?.let { accountId ->
                try {
                    closeActiveSessionLocked(accountId)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                }
            }
        }
    }

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
        selectedCareer: UniLoginProfileOption? = null,
        preferredAccountId: String? = null,
    ) {
        lock.withLock {
            if (DemoAccount.requested(credentials)) {
                prepareNotificationOwner(null)
                if (!DemoAccount.accepts(credentials)) {
                    mutableState.value = AppSessionState.SignedOut(readAppString(Res.string.ui_demo_invalid_credentials))
                    return@withLock
                }
                closeActiveSessionLocked()
                val account = accountStore.persistAuthenticatedAccount(
                    credentials,
                    demoProfileLocked(),
                    UniSessionTicket.restore("local-demo-v1".encodeToByteArray()),
                )
                mutableAccountsRevision.value += 1
                mutableState.value = if (requiresBiometricUnlock(account.accountId)) {
                    AppSessionState.UnlockRequired(account)
                } else {
                    AppSessionState.Authenticated(account)
                }
                return@withLock
            }
            val notificationOwner = preferredAccountId?.takeIf { accountId ->
                accountStore.matchesStoredUsername(accountId, credentials.username)
            }
            prepareNotificationOwner(notificationOwner)
            mutableState.value = AppSessionState.Authenticating
            try {
                try {
                    closeActiveSessionLocked()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                }
                mutableState.value =
                    try {
                        withContext(Dispatchers.IO) {
                            withTimeout(45_000L) {
                                authenticateLocked(credentials, selectedCareer?.toSdkLoginCareerOption(), preferredAccountId)
                            }
                        }
                    } catch (error: TimeoutCancellationException) {
                        prepareNotificationOwner(null)
                        val timeoutError = UniSdkException(UniSdkErrorCode.NETWORK_TIMEOUT, error)
                        AppSessionState.SignedOut(timeoutError.userMessage { readAppString(Res.string.msg_accesso_non_riuscito) })
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Throwable) {
                        prepareNotificationOwner(null)
                        AppSessionState.SignedOut(error.userMessage { readAppString(Res.string.msg_accesso_non_riuscito) })
                    }
                prepareNotificationOwner((mutableState.value as? AppSessionState.Authenticated)?.account?.accountId)
            } catch (error: CancellationException) {
                withContext(NonCancellable) {
                    if (mutableState.value == AppSessionState.Authenticating) {
                        mutableState.value = AppSessionState.SignedOut()
                    }
                    runCatching { prepareNotificationOwner(null) }
                }
                throw error
            }
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
            mutableState.value = AppSessionState.Switching
            try {
                prepareNotificationOwner(accountId)
                activateStoredAccount(accountId).also { nextState ->
                    mutableState.value = nextState
                }
            } catch (error: Throwable) {
                mutableState.value = current?.let(AppSessionState::Authenticated) ?: AppSessionState.SignedOut()
                withContext(NonCancellable) {
                    runCatching { prepareNotificationOwner(current?.accountId) }
                }
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
                    ?: throw IllegalStateException(readAppString(Res.string.msg_nessun_account_attivo))
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
            runCatching { prepareNotificationOwner(null) }
            runCatching { closeActiveSessionLocked() }
            accountStore.setActiveAccount(null)
            mutableState.value = AppSessionState.SignedOut()
        }
    }

    suspend fun accounts(): List<UniAccountSummary> = accountStore.snapshot().accounts

    internal suspend fun updateDemoAuthor(author: GitHubAuthor) = lock.withLock {
        if (!author.login.equals(ProjectInfo.authorLogin, ignoreCase = true)) return@withLock
        demoAuthor = author
        val current = (mutableState.value as? AppSessionState.Authenticated)?.account ?: return@withLock
        if (!DemoAccount.isDemo(current)) return@withLock
        val updated = refreshDemoProfileLocked(current)
        if (updated != current) mutableState.value = AppSessionState.Authenticated(updated)
    }

    private suspend fun demoProfileLocked(): UniUserProfile {
        if (demoAuthor == null) {
            demoAuthor = try { localDataStore.read(LocalDataScope.Application, UniAppDataKeys.GitHubProject).author }
                catch (error: CancellationException) { throw error } catch (_: Exception) { null }
        }
        return DemoAccount.profile(demoAuthor)
    }

    private suspend fun refreshDemoProfileLocked(account: UniAccountSummary): UniAccountSummary {
        val profile = demoProfileLocked()
        if (demoAuthor == null || (account.displayName == profile.displayName && account.photoUrl == profile.photoUrl)) return account
        return try {
            accountStore.updateSession(
                account.accountId,
                profile,
                UniSessionTicket.restore("local-demo-v1".encodeToByteArray()),
            ).also {
                mutableAccountsRevision.value += 1
            }
        } catch (error: CancellationException) { throw error } catch (_: Exception) { account }
    }

    internal suspend fun removeAccount(accountId: String, authenticator: BiometricAuthenticator): Boolean =
        lock.withLock {
            val targetAccount = accountStore.snapshot().accounts.firstOrNull { it.accountId == accountId }
                ?: throw IllegalArgumentException("Unknown account")
            val isProtected = requiresBiometricUnlock(accountId)
            if (!authorizeAccountRemoval(isProtected, authenticator)) return@withLock false
            removeAccountLocked(targetAccount)
            true
        }

    private suspend fun removeAccountLocked(account: UniAccountSummary) {
        val accountId = account.accountId
        val previous = mutableState.value
        try {
            disconnectAndCloseSessionLocked(account)
            accountStore.forgetAccount(accountId)
        } finally {
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

    private suspend fun disconnectAndCloseSessionLocked(account: UniAccountSummary) {
        val accountId = account.accountId
        if (DemoAccount.isDemo(account)) {
            closeActiveSessionLocked(accountId)
            return
        }
        val current = activeSession?.takeIf { it.account.accountId == accountId }
        if (current != null) {
            activeSession = null
            closeLibrarySessionForOwner(accountId)
            val liveSession = current.session
            if (liveSession != null) {
                val revoked = withTimeoutOrNull(8_000L) {
                    runCatching { backend.revokeSession(liveSession) }.isSuccess
                } == true
                if (!revoked) {
                    runCatching { backend.closeSession(liveSession) }
                }
                return
            }
        }
        val storedTicket = runCatching { accountStore.loadSessionTicket(accountId) }.getOrNull()
        if (storedTicket != null) {
            withTimeoutOrNull(8_000L) {
                runCatching { backend.revokeSessionTicket(storedTicket) }
            }
        }
        closeLibrarySessionForOwner(accountId)
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
                AppSessionState.CareerSelectionRequired(result.careers.map(LoginCareerOption::toUniLoginProfileOption))
            }
            is AuthenticationResult.Authenticated -> {
                if (!result.profile.matchesSelectedCareer(selectedCareer)) {
                    runCatching { backend.closeSession(result.session) }
                    error(readAppString(Res.string.msg_il_portale_non_ha_attivato_il_profilo_selezionato))
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
                mutableAccountsRevision.value += 1
                if (requiresBiometricUnlock(account.accountId)) {
                    runCatching { backend.closeSession(result.session) }
                    AppSessionState.UnlockRequired(account)
                } else {
                    installSessionLocked(ActiveSessionContext(account, profile, result.session))
                    AppSessionState.Authenticated(account)
                }
            }
        }
    }

    private suspend fun resumeAccountLocked(account: UniAccountSummary): AppSessionState {
        activeSession?.takeIf { it.account.accountId == account.accountId }?.let {
            accountStore.setActiveAccount(account.accountId)
            return AppSessionState.Authenticated(it.account)
        }
        val ticket = try {
            accountStore.loadSessionTicket(account.accountId)
        } catch (error: CancellationException) {
            throw error
        } catch (_: InvalidUniSessionTicketException) {
            accountStore.clearSessionTicket(account.accountId)
            return reauthenticationState(account)
        } ?: return reauthenticationState(account)

        return try {
            accountStore.withCredentials(account.accountId) { credentials ->
                val result = try {
                    withTimeout(ONLINE_RESUME_TIMEOUT_MILLIS) {
                        backend.resumeSession(ticket, credentials)
                    }
                } catch (timeout: TimeoutCancellationException) {
                    if (!currentCoroutineContext().isActive) throw timeout
                    throw UniSdkException(UniSdkErrorCode.NETWORK_TIMEOUT, timeout)
                }
                when (result) {
                    SessionResumeResult.ReauthenticationRequired -> {
                        accountStore.clearSessionTicket(account.accountId)
                        reauthenticationState(account)
                    }
                    is SessionResumeResult.Resumed -> {
                        val selected = account.profiles.firstOrNull { it.profileId == account.activeProfileId }
                            ?.toSdkLoginCareerOption()
                        if (result.profile.id != account.serverUserId ||
                            (account.activeProfileId != null && selected == null) ||
                            !result.profile.matchesSelectedCareer(selected)
                        ) {
                            runCatching { backend.closeSession(result.session) }
                            accountStore.clearSessionTicket(account.accountId)
                            return@withCredentials reauthenticationState(account)
                        }
                        val profile = result.profile.withProfileCatalog(account.profiles.map { it.toSdkLoginCareerOption() }, selected)
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
        } catch (error: CancellationException) {
            throw error
        } catch (_: MissingAccountCredentialsException) {
            accountStore.clearSessionTicket(account.accountId)
            reauthenticationState(account)
        } catch (_: InvalidUniSessionTicketException) {
            accountStore.clearSessionTicket(account.accountId)
            reauthenticationState(account)
        } catch (error: Throwable) {
            if ((error as? UniSdkException)?.rejectsPrimarySession() == true) {
                accountStore.clearSessionTicket(account.accountId)
                return reauthenticationState(account)
            }
            if (!error.isRecoverableOfflineResumeError()) throw error
            accountStore.setActiveAccount(account.accountId)
            val offlineContext = ActiveSessionContext(
                account = account,
                profile = account.toSdkUserProfile(),
                session = null,
                offlineFailureAtMillis = nowMillis(),
                lastOfflineError = error.asOfflineSdkException(),
            )
            installSessionLocked(offlineContext)
            AppSessionState.Authenticated(account)
        }
    }

    private suspend fun activateProfileLocked(account: UniAccountSummary, profileId: String): UniAccountSummary {
        val selected = account.profiles.firstOrNull { it.profileId == profileId }
            ?.toSdkLoginCareerOption() ?: throw IllegalArgumentException("Unknown account profile")
        return accountStore.withCredentials(account.accountId) { credentials ->
            val result = backend.login(credentials, selected)
            val authenticated = result as? AuthenticationResult.Authenticated
                ?: error(readAppString(Res.string.msg_il_portale_richiede_ancora_la_scelta_della_carriera))
            if (authenticated.profile.id != account.serverUserId ||
                !authenticated.profile.matchesSelectedCareer(selected)
            ) {
                runCatching { backend.closeSession(authenticated.session) }
                error(readAppString(Res.string.msg_il_portale_ha_restituito_una_carriera_diversa))
            }
            val profile = authenticated.profile.withProfileCatalog(
                account.profiles.map { it.toSdkLoginCareerOption() },
                selected,
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
        if (previous != null) withContext(NonCancellable) {
            if (previous.account.accountId != next.account.accountId ||
                previous.account.activeProfileId != next.account.activeProfileId
            ) {
                closeLibrarySessionForOwner(previous.account.accountId)
            }
            val previousLive = previous.session
            if (previousLive != null && previousLive !== next.session) {
                runCatching { backend.closeSession(previousLive) }
            }
        }
    }

    private suspend fun closeActiveSessionLocked(accountId: String? = null) {
        val current = activeSession ?: return
        if (accountId != null && current.account.accountId != accountId) return
        activeSession = null
        closeLibrarySessionForOwner(current.account.accountId)
        current.session?.let { backend.closeSession(it) }
    }

    private suspend fun closeLibrarySessionForOwner(accountId: String? = null) {
        libraryLock.withLock {
            val lib = activeLibrarySession
            if (lib != null && (accountId == null || lib.accountId == accountId)) {
                activeLibrarySession = null
                runCatching { libraryService.closeSession(lib.session) }
            }
            if (accountId == null) {
                val guest = guestLibrarySession
                guestLibrarySession = null
                if (guest != null) runCatching { libraryService.closeSession(guest) }
            }
        }
    }

    internal suspend fun <T> callAuthenticated(
        accountId: String,
        profileId: String?,
        block: suspend UniBackendService.(UniSession, UniCredentials) -> T,
    ): T {
        val active = ensureLiveSession(accountId, profileId)
        val liveSession = checkNotNull(active.session)
        val value = accountStore.withCredentials(accountId) { credentials ->
            try {
                backend.block(liveSession, credentials)
            } catch (error: UniSdkException) {
                if (error.rejectsPrimarySession() && isConfirmedInactive(active, liveSession, credentials)) {
                    forgetInactiveSession(active)
                }
                throw error
            }
        }
        ensureActiveContext(active)
        return value
    }

    private suspend fun ensureLiveSession(accountId: String, profileId: String?): ActiveSessionContext {
        val initial = activeContextFor(accountId, profileId)
        if (initial.session != null) return initial

        return onlineResumeLock.withLock {
            val current = activeContextFor(accountId, profileId)
            if (current.session != null) return@withLock current

            val failureAt = current.offlineFailureAtMillis
            if (failureAt != null && nowMillis() - failureAt in 0 until OFFLINE_RESUME_COOLDOWN_MILLIS) {
                throw (current.lastOfflineError ?: UniSdkException(UniSdkErrorCode.NETWORK_REQUEST_FAILED))
            }

            val ticket = try {
                accountStore.loadSessionTicket(accountId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: InvalidUniSessionTicketException) {
                null
            }
            if (ticket == null) {
                forgetInactiveSession(current)
                throw UniSdkException(UniSdkErrorCode.SESSION_EXPIRED_LOGIN_AGAIN)
            }

            try {
                accountStore.withCredentials(accountId) { credentials ->
                    val resumed = try {
                        withTimeout(ONLINE_RESUME_TIMEOUT_MILLIS) {
                            backend.resumeSession(ticket, credentials)
                        }
                    } catch (timeout: TimeoutCancellationException) {
                        if (!currentCoroutineContext().isActive) throw timeout
                        throw UniSdkException(UniSdkErrorCode.NETWORK_TIMEOUT, timeout)
                    }
                    when (resumed) {
                        SessionResumeResult.ReauthenticationRequired -> {
                            forgetInactiveSession(current)
                            throw UniSdkException(UniSdkErrorCode.SESSION_EXPIRED_LOGIN_AGAIN)
                        }
                        is SessionResumeResult.Resumed -> {
                            val selected = current.account.profiles
                                .firstOrNull { it.profileId == current.account.activeProfileId }
                                ?.toSdkLoginCareerOption()
                            if (resumed.profile.id != current.account.serverUserId ||
                                (current.account.activeProfileId != null && selected == null) ||
                                !resumed.profile.matchesSelectedCareer(selected)
                            ) {
                                runCatching { backend.closeSession(resumed.session) }
                                forgetInactiveSession(current)
                                throw UniSdkException(UniSdkErrorCode.SESSION_EXPIRED_LOGIN_AGAIN)
                            }
                            val catalog = current.account.profiles.map { it.toSdkLoginCareerOption() }
                            val profile = resumed.profile.withProfileCatalog(catalog, selected)
                            try {
                                accountStore.updateSession(accountId, profile, resumed.ticket)
                            } catch (error: Throwable) {
                                runCatching { backend.closeSession(resumed.session) }
                                throw error
                            }
                            lock.withLock {
                                if (activeSession !== current || mutableState.value !is AppSessionState.Authenticated) {
                                    withContext(NonCancellable) {
                                        runCatching { backend.closeSession(resumed.session) }
                                    }
                                    throw CancellationException("The account or career changed while resuming session")
                                }
                                // Keep the current Authenticated instance reference intact so in-flight
                                // PortalUniGateway requests pass ensureCurrent() without spurious cancellation.
                                val upgraded = current.copy(
                                    profile = profile,
                                    session = resumed.session,
                                    offlineFailureAtMillis = null,
                                    lastOfflineError = null,
                                )
                                activeSession = upgraded
                                upgraded
                            }
                        }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: MissingAccountCredentialsException) {
                forgetInactiveSession(current)
                throw UniSdkException(UniSdkErrorCode.SESSION_EXPIRED_LOGIN_AGAIN)
            } catch (_: InvalidUniSessionTicketException) {
                forgetInactiveSession(current)
                throw UniSdkException(UniSdkErrorCode.SESSION_EXPIRED_LOGIN_AGAIN)
            } catch (error: UniSdkException) {
                if (error.rejectsPrimarySession()) {
                    forgetInactiveSession(current)
                } else if (error.isRecoverableOfflineResumeError()) {
                    recordOfflineResumeFailure(current, error)
                }
                throw error
            } catch (error: Throwable) {
                if (error.isRecoverableOfflineResumeError()) {
                    val mapped = error.asOfflineSdkException()
                    recordOfflineResumeFailure(current, mapped)
                    throw mapped
                }
                throw error
            }
        }
    }

    private suspend fun recordOfflineResumeFailure(expected: ActiveSessionContext, error: UniSdkException) {
        lock.withLock {
            if (activeSession === expected) {
                activeSession = expected.copy(
                    offlineFailureAtMillis = nowMillis(),
                    lastOfflineError = error,
                )
            }
        }
    }

    internal suspend fun <T> withTransportSession(
        accountId: String,
        profileId: String?,
        block: suspend TransportService.(TransportSession) -> T,
    ): T = callAuthenticated(accountId, profileId) { session, credentials ->
        val transport = openTransportSession(session, credentials)
        try {
            block(transport)
        } finally {
            withContext(NonCancellable) {
                runCatching { closeTransportSession(transport) }
            }
        }
    }

    private suspend fun isConfirmedInactive(
        active: ActiveSessionContext,
        liveSession: UniSession,
        credentials: UniCredentials,
    ): Boolean =
        try {
            !backend.isSessionValid(liveSession, credentials)
        } catch (error: UniSdkException) {
            error.rejectsPrimarySession()
        }

    private suspend fun forgetInactiveSession(expected: ActiveSessionContext) = withContext(NonCancellable) {
        lock.withLock {
            if (activeSession !== expected) return@withLock
            val account = expected.account
            try {
                accountStore.clearSessionTicket(account.accountId)
            } finally {
                mutableState.value = when (val state = mutableState.value) {
                    is AppSessionState.Authenticated -> reauthenticationState(account)
                    is AppSessionState.UnlockRequired ->
                        state.copy(fallbackAccount = state.fallbackAccount?.takeUnless { it.accountId == account.accountId })
                    else -> state
                }
                try {
                    closeActiveSessionLocked(account.accountId)
                } finally {
                    prepareNotificationOwner(null)
                }
            }
        }
    }

    internal suspend fun <T> withLibraryGuestSession(
        retryOnExpiry: Boolean = true,
        block: suspend LibraryService.(LibrarySession) -> T,
    ): T = libraryLock.withLock {
        val session = guestLibrarySession ?: libraryService.openGuestSession().also {
            guestLibrarySession = it
        }
        try {
            libraryService.block(session)
        } catch (error: UniSdkException) {
            if (!retryOnExpiry ||
                (error.errorCode != UniSdkErrorCode.LIBRARY_SESSION_EXPIRED &&
                    error.errorCode != UniSdkErrorCode.LIBRARY_SESSION_INVALID)
            ) {
                throw error
            }
            guestLibrarySession = null
            runCatching { libraryService.closeSession(session) }
            val fresh = libraryService.openGuestSession().also { guestLibrarySession = it }
            libraryService.block(fresh)
        }
    }

    internal suspend fun <T> withAuthenticatedLibrarySession(
        accountId: String,
        profileId: String?,
        retryOnExpiry: Boolean = true,
        block: suspend LibraryService.(LibrarySession) -> T,
    ): T {
        val active = activeContextFor(accountId, profileId)
        val result = libraryLock.withLock {
            suspend fun openFresh(): LibrarySession {
                val previous = activeLibrarySession
                if (previous != null) {
                    activeLibrarySession = null
                    runCatching { libraryService.closeSession(previous.session) }
                }
                return accountStore.withCredentials(accountId) { credentials ->
                    libraryService.openSession(credentials)
                }.also { created ->
                    activeLibrarySession = LibrarySessionContext(accountId, profileId, created)
                }
            }

            val current = activeLibrarySession
                ?.takeIf { it.accountId == accountId && it.profileId == profileId }
                ?.session
                ?: openFresh()

            try {
                libraryService.block(current)
            } catch (error: UniSdkException) {
                if (error.errorCode != UniSdkErrorCode.LIBRARY_SESSION_EXPIRED &&
                    error.errorCode != UniSdkErrorCode.LIBRARY_SESSION_INVALID
                ) {
                    throw error
                }
                activeLibrarySession = null
                runCatching { libraryService.closeSession(current) }
                if (!retryOnExpiry) throw error
                val fresh = openFresh()
                libraryService.block(fresh)
            }
        }
        ensureActiveContext(active)
        return result
    }

    internal suspend fun currentAuthenticatedLibrarySessionOrNull(
        accountId: String,
        profileId: String?,
    ): LibrarySession? = libraryLock.withLock {
        activeLibrarySession?.takeIf { it.accountId == accountId && it.profileId == profileId }?.session
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
            runCatching { closeLibrarySessionForOwner(null) }
        }
    }

    private fun reauthenticationState(account: UniAccountSummary) =
        AppSessionState.ReauthenticationRequired(
            account,
            "La sessione di ${account.displayName} è scaduta. Accedi di nuovo per continuare.",
        )

    private suspend fun requiresBiometricUnlock(accountId: String): Boolean =
        localDataStore.read(LocalDataScope.Account(accountId), UniAppDataKeys.BiometricUnlock)

    private companion object {
        const val ONLINE_RESUME_TIMEOUT_MILLIS = 10_000L
        const val OFFLINE_RESUME_COOLDOWN_MILLIS = 2_000L
    }
}

private data class ActiveSessionContext(
    val account: UniAccountSummary,
    val profile: UniUserProfile,
    val session: UniSession?,
    val offlineFailureAtMillis: Long? = null,
    val lastOfflineError: UniSdkException? = null,
)

private data class LibrarySessionContext(
    val accountId: String,
    val profileId: String?,
    val session: LibrarySession,
)

private fun UniSdkException.rejectsPrimarySession(): Boolean =
    when (errorCode) {
        UniSdkErrorCode.SESSION_INVALID,
        UniSdkErrorCode.SESSION_EXPIRED_LOGIN_AGAIN,
        UniSdkErrorCode.PASSWORD_INVALID,
        UniSdkErrorCode.AUTH_INVALID_CREDENTIALS,
        UniSdkErrorCode.ACCOUNT_INVALID,
        -> true
        else -> false
    }

private fun Throwable.isRecoverableOfflineResumeError(): Boolean {
    if (this is IOException) return true
    val code = uniSdkErrorCode() ?: return false
    return when (code) {
        UniSdkErrorCode.NETWORK_REQUEST_FAILED,
        UniSdkErrorCode.NETWORK_TIMEOUT,
        UniSdkErrorCode.NETWORK_HTTP_ERROR,
        UniSdkErrorCode.REMOTE_REQUEST_FAILED,
        UniSdkErrorCode.APP_BACKEND_UNAVAILABLE,
        UniSdkErrorCode.CINECA_UNAVAILABLE,
        UniSdkErrorCode.CINECA_REQUEST_FAILED,
        UniSdkErrorCode.SERVICE_UNAVAILABLE,
        UniSdkErrorCode.SESSION_NOT_AVAILABLE,
        UniSdkErrorCode.SYNC_ERROR,
        UniSdkErrorCode.FEATURE_LOAD_TIMEOUT,
        -> true
        else -> false
    }
}

private fun Throwable.asOfflineSdkException(): UniSdkException =
    (this as? UniSdkException)
        ?: UniSdkException(uniSdkErrorCode() ?: UniSdkErrorCode.NETWORK_REQUEST_FAILED, this)

private fun UniAccountSummary.toSdkUserProfile(): UniUserProfile =
    UniUserProfile(
        id = serverUserId,
        displayName = displayName,
        degreeName = degreeName,
        matricola = matricola,
        email = email,
        photoUrl = photoUrl,
        isGuest = isGuest,
        activeProfileId = activeProfileId,
        profiles = profiles.map { profile ->
            UniCareerProfile(
                profileId = profile.profileId,
                displayName = profile.displayName,
                degreeName = profile.degreeName,
                matricola = profile.matricola,
                matId = profile.matId,
                stuId = profile.stuId,
                anaId = profile.anaId,
                cdsId = profile.cdsId,
                dipId = profile.dipId,
                departmentName = profile.departmentName,
                teacherId = profile.teacherId,
                type = profile.type.toSdkCareerType(),
            )
        },
        activeProfileType = activeProfileType.toSdkCareerType(),
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
