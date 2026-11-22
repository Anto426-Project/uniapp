package com.anto426.uniapp.data

import com.anto426.securestorage.SecureStorageCorruptedException
import com.anto426.securestorage.SecureStorageDecodingException
import com.anto426.uniapp.account.data.AccountAvatarStore
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniLoginProfileOption
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.demo.DemoAccount
import com.anto426.uniapp.demo.DemoCatalog
import com.anto426.uniapp.data.contract.LibraryBookCopyAction
import com.anto426.uniapp.data.contract.LibraryBookDetail
import com.anto426.uniapp.data.contract.LibraryBookLoanDraft
import com.anto426.uniapp.data.contract.LibraryBookingOption
import com.anto426.uniapp.data.contract.LibraryBookingOverview
import com.anto426.uniapp.data.contract.LibraryCatalogBook
import com.anto426.uniapp.data.contract.LibraryCatalogPage
import com.anto426.uniapp.data.contract.LibraryLoanSubmissionResult
import com.anto426.uniapp.data.contract.LibraryMovementItem
import com.anto426.uniapp.data.contract.LibrarySpaceBooking
import com.anto426.uniapp.data.contract.LibrarySpaceBookingOutcome
import com.anto426.uniapp.data.contract.LibrarySpaceCalendar
import com.anto426.uniapp.data.contract.LibrarySpaceSelection
import com.anto426.uniapp.data.contract.LibrarySpaceSlot
import com.anto426.uniapp.data.contract.LibraryTextParser
import com.anto426.uniapp.localization.readAppString
import com.anto426.uniapp.presentation.userMessage
import com.anto426.uniapp.project.model.GitHubAuthor
import com.anto426.uniapp.project.model.GitHubContributor
import com.anto426.uniapp.project.model.GitHubProjectSnapshot
import com.anto426.uniapp.project.model.GitHubRepositoryInfo
import com.anto426.uniapp.security.account.authorizeAccountRemoval
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.password.verifyAppPassword
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.UniAppUpdateManifest
import com.anto426.uniapp.updates.platform.PlatformUpdateLaunchResult
import com.anto426.uniapp.updates.platform.PlatformUpdateLauncher
import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.errors.UniSdkErrorCode
import com.anto426.unisdk.errors.UniSdkException
import com.anto426.unisdk.library.LibraryBook
import com.anto426.unisdk.library.LibraryBookAction
import com.anto426.unisdk.library.LibraryBookMovement
import com.anto426.unisdk.library.LibraryBookRequestDraft
import com.anto426.unisdk.library.LibraryBookingContext
import com.anto426.unisdk.library.LibraryCalendar
import com.anto426.unisdk.library.LibraryResourceBooking
import com.anto426.unisdk.library.LibraryService
import com.anto426.unisdk.library.LibrarySession
import com.anto426.unisdk.library.LibrarySlot
import com.anto426.unisdk.library.RemoteLibraryService
import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.ConnectedDevice
import com.anto426.unisdk.model.CourseSyllabus
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.LoginCareerOption
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.StudentDetails
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.SurveyPage
import com.anto426.unisdk.model.SurveyQuestion
import com.anto426.unisdk.model.SurveyAnswerOption
import com.anto426.unisdk.model.SurveySaveRequest
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.platform.APP_CHANNEL_STABLE
import com.anto426.unisdk.platform.ProjectInfo
import com.anto426.unisdk.platform.currentEpochMillis
import com.anto426.unisdk.session.AuthenticationResult
import com.anto426.unisdk.session.SessionResumeResult
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSession
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportService
import com.anto426.unisdk.transport.TransportSession
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import uniapp.composeapp.generated.resources.*

// ============================================================================
// 1. Unified Gateway Interface
// ============================================================================

/** Account-aware, encrypted and policy-driven data boundary used by UniCacheEngine and UniRepository. */
interface UniGateway {
    suspend fun loadCareer(forceRefresh: Boolean = false): Career
    suspend fun loadProfessorDashboard(forceRefresh: Boolean = false): ProfessorDashboard
    suspend fun loadStudyPlan(forceRefresh: Boolean = false): StudyPlan
    suspend fun loadExamRounds(forceRefresh: Boolean = false): List<ExamRound>
    suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean = false): CourseSyllabus
    suspend fun bookExamRound(round: ExamRound): String
    suspend fun cancelExamRound(round: ExamRound): String
    suspend fun loadTaxes(forceRefresh: Boolean = false): Taxes
    suspend fun loadStudentDetails(forceRefresh: Boolean = false): StudentDetails
    suspend fun loadProfileImage(source: String, forceRefresh: Boolean = false): ByteArray
    suspend fun loadConnectedDevices(forceRefresh: Boolean = false): List<ConnectedDevice>
    suspend fun disconnectDevice(targetToken: String): String
    suspend fun disconnectAllOtherDevices(): String
    suspend fun loadAttendanceHistory(forceRefresh: Boolean = false): List<AttendanceRecord>
    suspend fun registerAttendance(
        qrCode: String,
        deviceLatitude: Double? = null,
        deviceLongitude: Double? = null,
        deviceAccuracyMeters: Double? = null,
    ): String
    suspend fun loadUniversityNews(forceRefresh: Boolean = false): List<UniversityNews>
    suspend fun loadUniversityContacts(forceRefresh: Boolean = false): List<UniversityContact>
    suspend fun loadSurveyCourses(forceRefresh: Boolean = false): List<SurveyCourse>
    suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyForm
    suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String
    suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean = false): Boolean
    suspend fun loadTransportData(forceRefresh: Boolean = false): TransportData
    suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean = false): ByteArray {
        throw UnsupportedOperationException("Ticket image download is unavailable")
    }
    suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult
    suspend fun deleteTransportBooking(bookingId: String): TransportActionResult

    suspend fun searchLibraryBooks(query: String, page: Int = 1): LibraryCatalogPage
    suspend fun loadLibraryBook(id: String): LibraryBookDetail
    suspend fun prepareLibraryBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft
    suspend fun confirmLibraryBookRequest(draftId: String): LibraryLoanSubmissionResult
    suspend fun loadLibraryMovements(forceRefresh: Boolean = false): List<LibraryMovementItem>
    suspend fun cancelLibraryMovement(movementToken: String)
    suspend fun renewLibraryMovement(movementToken: String): String
    suspend fun loadLibraryBookingOverview(): LibraryBookingOverview
    suspend fun loadLibraryResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection
    suspend fun loadLibraryResourcesForOwner(ownerId: String): List<LibraryBookingOption>
    suspend fun loadLibraryCalendar(
        contextId: String,
        resourceId: String,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null,
    ): LibrarySpaceCalendar
    suspend fun loadLibraryCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar
    suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String = "",
        deviceAddresses: Map<String, String> = emptyMap(),
    ): LibrarySpaceBookingOutcome
    suspend fun cancelLibrarySpaceBooking(bookingToken: String)
}

internal interface UniPortraitSharer {
    suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray
}

// ============================================================================
// 2. Session Controller (Authentication, Vaults, Biometrics & SDK Sessions)
// ============================================================================

enum class PasswordUnlockResult { Unlocked, Invalid, RetryLater }

class InactiveAccountSessionException(accountId: String) :
    IllegalStateException("Account '$accountId' does not have an active RAM session")

class UniSessionController internal constructor(
    private val backend: UniBackendService,
    private val accountStore: UniAccountStore,
    private val localDataStore: UniLocalDataStore,
    private val libraryService: LibraryService = RemoteLibraryService(),
    private val prepareNotificationOwner: suspend (String?) -> Unit = {},
) {
    val avatars = AccountAvatarStore(accountStore)
    private val lock = Mutex()
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
            val revoked = withTimeoutOrNull(8_000L) {
                runCatching { backend.revokeSession(current.session) }.isSuccess
            } == true
            if (!revoked) {
                runCatching { backend.closeSession(current.session) }
            }
            return
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
            if (previous.session !== next.session) {
                runCatching { backend.closeSession(previous.session) }
            }
        }
    }

    private suspend fun closeActiveSessionLocked(accountId: String? = null) {
        val current = activeSession ?: return
        if (accountId != null && current.account.accountId != accountId) return
        activeSession = null
        closeLibrarySessionForOwner(current.account.accountId)
        backend.closeSession(current.session)
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
        val active = activeContextFor(accountId, profileId)
        val value = accountStore.withCredentials(accountId) { credentials ->
            try {
                backend.block(active.session, credentials)
            } catch (error: UniSdkException) {
                if (error.rejectsPrimarySession() && isConfirmedInactive(active, credentials)) {
                    forgetInactiveSession(active)
                }
                throw error
            }
        }
        ensureActiveContext(active)
        return value
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

    private suspend fun isConfirmedInactive(active: ActiveSessionContext, credentials: UniCredentials): Boolean =
        try {
            !backend.isSessionValid(active.session, credentials)
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
}

private data class ActiveSessionContext(
    val account: UniAccountSummary,
    val profile: UniUserProfile,
    val session: UniSession,
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
        -> true
        else -> false
    }

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

// ============================================================================
// 3. PortalUniGateway (Encrypted Disk Cache + SDK + Sebina Library Token Vault)
// ============================================================================

class PortalUniGateway(
    private val sessions: UniSessionController,
    private val accounts: UniAccountStore,
    private val fixedAccountId: String? = null,
    private val fixedProfileId: String? = null,
    private val nowMillis: () -> Long = ::currentEpochMillis,
    private val fallbackToStaleCache: Boolean = true,
) : UniGateway, UniPortraitSharer {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val requestLocksGuard = Mutex()
    private val requestLocks = mutableMapOf<String, Mutex>()
    private val savedAtGuard = Mutex()
    private val savedAtByKey = mutableMapOf<String, Long>()

    // Opaque token vault for Sebina library session objects
    private data class OwnedLibraryHandle<T>(
        val session: LibrarySession,
        val value: T,
    )
    private val libraryHandleLock = Mutex()
    private var nextLibraryToken = 0L
    private val bookActions = mutableMapOf<String, OwnedLibraryHandle<LibraryBookAction>>()
    private val bookDrafts = mutableMapOf<String, OwnedLibraryHandle<LibraryBookRequestDraft>>()
    private val movements = mutableMapOf<String, OwnedLibraryHandle<LibraryBookMovement>>()
    private val bookingContexts = mutableMapOf<String, OwnedLibraryHandle<LibraryBookingContext>>()
    private val spaceBookings = mutableMapOf<String, OwnedLibraryHandle<LibraryResourceBooking>>()
    private val calendars = mutableMapOf<String, OwnedLibraryHandle<Pair<LibraryCalendar, Map<String, LibrarySlot>>>>()

    private fun issueLibraryToken(prefix: String): String {
        nextLibraryToken += 1
        return "$prefix#$nextLibraryToken"
    }

    private fun ActiveAccountContext.profileScopedKey(key: String): String =
        profileCacheKey(sessionState.account.activeProfileId, key)

    private fun activeContext(): ActiveAccountContext {
        val sessionState = sessions.state.value as? AppSessionState.Authenticated
            ?: throw CancellationException("The data request no longer has an authenticated owner")
        val account = sessionState.account
        requireDataRequestOwner(account.accountId, account.activeProfileId, fixedAccountId, fixedProfileId)
        return ActiveAccountContext(account.accountId, sessionState)
    }

    private suspend fun <T> ActiveAccountContext.backend(
        block: suspend UniBackendService.(UniSession, UniCredentials) -> T,
    ): T = sessions.callAuthenticated(accountId, sessionState.account.activeProfileId, block)

    private suspend fun <T> ActiveAccountContext.transport(
        block: suspend TransportService.(TransportSession) -> T,
    ): T = sessions.withTransportSession(accountId, sessionState.account.activeProfileId, block)

    private suspend fun <T> withAuthenticatedLibrary(
        retryOnExpiry: Boolean = true,
        block: suspend LibraryService.(LibrarySession) -> T,
    ): T {
        val context = activeContext()
        return sessions.withAuthenticatedLibrarySession(
            context.accountId,
            context.sessionState.account.activeProfileId,
            retryOnExpiry,
            block,
        )
    }

    private fun ensureCurrent(context: ActiveAccountContext) {
        if (sessions.state.value !== context.sessionState) {
            throw CancellationException("The account or profile changed while loading data")
        }
    }

    internal suspend fun readCachedSavedAtMillis(request: UniDatasetKey<*>): Long? {
        val context = activeContext()
        if (request.id.startsWith("portrait/")) {
            return accounts.readProfileImage(context.accountId, request.id.removePrefix("portrait/"))
                ?.savedAtMillis
                ?.also { ensureCurrent(context) }
        }
        val key = request.cacheKey ?: return null
        val cacheAddress = "${context.accountId}|${context.profileScopedKey(key)}"
        val savedAt = savedAtGuard.withLock { savedAtByKey[cacheAddress] }
        ensureCurrent(context)
        return savedAt
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <T : Any> readCached(request: UniDatasetKey<T>): CachedValue<T>? {
        val context = activeContext()
        if (request.id.startsWith("portrait/")) {
            val cached = accounts.readProfileImage(context.accountId, request.id.removePrefix("portrait/"))
            ensureCurrent(context)
            return cached?.let { CachedValue(it.savedAtMillis, it.bytes as T) }
        }
        val key = request.cacheKey ?: return null
        val serializer = request.serializer ?: return null
        val value = try {
            readEntry(context.accountId, context.profileScopedKey(key), serializer)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
        ensureCurrent(context)
        return value
    }

    override suspend fun loadCareer(forceRefresh: Boolean): Career =
        cached("career-v2", UniCachePolicies.Career, Career.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadCareer(session, credentials) }
        }

    override suspend fun loadProfessorDashboard(forceRefresh: Boolean): ProfessorDashboard =
        cached(
            "professor-dashboard",
            UniCachePolicies.ProfessorDashboard,
            ProfessorDashboard.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadProfessorDashboard(session, credentials) } }

    override suspend fun loadStudyPlan(forceRefresh: Boolean): StudyPlan =
        cached("study-plan", UniCachePolicies.StudyPlan, StudyPlan.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadStudyPlan(session, credentials) }
        }

    override suspend fun loadExamRounds(forceRefresh: Boolean): List<ExamRound> =
        cached("exam-rounds", UniCachePolicies.ExamRounds, ListSerializer(ExamRound.serializer()), forceRefresh) { context ->
            context.backend { session, credentials -> loadExamRounds(session, credentials) }
        }

    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean): CourseSyllabus =
        cached(
            datasetCacheKey("course-syllabus", adsceId),
            UniCachePolicies.CourseSyllabus,
            CourseSyllabus.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadCourseSyllabus(session, credentials, adsceId) } }

    override suspend fun bookExamRound(round: ExamRound): String {
        val context = activeContext()
        return context.backend { session, credentials -> bookExamRound(session, credentials, round) }.also {
            invalidate(context.accountId, context.profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun cancelExamRound(round: ExamRound): String {
        val context = activeContext()
        return context.backend { session, credentials -> cancelExamRound(session, credentials, round) }.also {
            invalidate(context.accountId, context.profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun loadTaxes(forceRefresh: Boolean): Taxes =
        cached("taxes", UniCachePolicies.Taxes, Taxes.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadTaxes(session, credentials) }
        }

    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetails =
        cached("student-details-v2", UniCachePolicies.StudentDetails, StudentDetails.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadStudentDetails(session, credentials) }
        }

    override suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray {
        val context = activeContext()
        return sessions.avatars.publish(context.accountId, source, bytes) { ensureCurrent(context) }
    }

    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray =
        activeContext().let { context ->
            requestLock(context.accountId, "profile-image").withLock {
                ensureCurrent(context)
                val cached = accounts.readProfileImage(context.accountId, source)
                ensureCurrent(context)
                if (!forceRefresh && cached != null && nowMillis() - cached.savedAtMillis in 0 until UniCachePolicies.ProfileImage.maxAgeMillis) {
                    return@withLock cached.bytes
                }
                try {
                    val downloaded = context.backend { session, credentials -> loadProfileImage(session, credentials, source) }.also { bytes ->
                        ensureCurrent(context)
                        try {
                            accounts.writeProfileImage(context.accountId, source, nowMillis(), bytes)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                        }
                        ensureCurrent(context)
                    }
                    downloaded
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    ensureCurrent(context)
                    cached?.bytes ?: throw error
                }
            }
        }

    override suspend fun loadConnectedDevices(forceRefresh: Boolean): List<ConnectedDevice> =
        cached(
            "connected-devices",
            UniCachePolicies.ConnectedDevices,
            ListSerializer(ConnectedDevice.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadConnectedDevices(session, credentials) } }

    override suspend fun disconnectDevice(targetToken: String): String {
        val context = activeContext()
        return context.backend { session, credentials -> disconnectDevice(session, credentials, targetToken) }.also {
            invalidate(context.accountId, context.profileScopedKey("connected-devices"))
        }
    }

    override suspend fun disconnectAllOtherDevices(): String {
        val context = activeContext()
        return context.backend { session, credentials -> disconnectAllOtherDevices(session, credentials) }.also {
            invalidate(context.accountId, context.profileScopedKey("connected-devices"))
        }
    }

    override suspend fun loadAttendanceHistory(forceRefresh: Boolean): List<AttendanceRecord> =
        cached(
            "attendance",
            UniCachePolicies.Attendance,
            ListSerializer(AttendanceRecord.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadAttendanceHistory(session, credentials) } }

    override suspend fun registerAttendance(
        qrCode: String,
        deviceLatitude: Double?,
        deviceLongitude: Double?,
        deviceAccuracyMeters: Double?,
    ): String {
        val context = activeContext()
        return context.backend { session, credentials ->
            registerAttendance(
                session = session,
                credentials = credentials,
                qrCode = qrCode,
                deviceLatitude = deviceLatitude,
                deviceLongitude = deviceLongitude,
                deviceAccuracyMeters = deviceAccuracyMeters,
            )
        }.also {
            invalidate(context.accountId, context.profileScopedKey("attendance"))
        }
    }

    override suspend fun loadUniversityNews(forceRefresh: Boolean): List<UniversityNews> =
        cached(
            "university-news-v3",
            UniCachePolicies.News,
            ListSerializer(UniversityNews.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadUniversityNews(session, credentials) } }

    override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> =
        cached(
            "university-contacts-v2",
            UniCachePolicies.Contacts,
            ListSerializer(UniversityContact.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadUniversityContacts(session, credentials) } }

    override suspend fun loadSurveyCourses(forceRefresh: Boolean): List<SurveyCourse> =
        cached(
            "survey-courses",
            UniCachePolicies.SurveyCourses,
            ListSerializer(SurveyCourse.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadSurveyCourses(session, credentials) } }

    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyForm =
        activeContext().backend { session, credentials -> loadSurveyFirstPage(session, credentials, courseId, tagList) }

    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String {
        val context = activeContext()
        return context.backend { session, credentials -> saveSurvey(session, credentials, courseId, request) }.also {
            invalidate(context.accountId, context.profileScopedKey("survey-courses"))
        }
    }

    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean): Boolean =
        cached(
            datasetCacheKey("survey-status", adCod),
            UniCachePolicies.SurveyStatus,
            Boolean.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadSurveyCompilationStatus(session, credentials, adCod) } }

    override suspend fun loadTransportData(forceRefresh: Boolean): TransportData =
        cached(
            "transport-data",
            UniCachePolicies.Transport,
            TransportData.serializer(),
            forceRefresh,
            requirePersistence = true,
            onSaved = { context, previous, current ->
                val activeIds = current.bookings.mapTo(mutableSetOf()) { it.id }
                previous?.bookings.orEmpty().asSequence()
                    .map { it.id }
                    .filterNot(activeIds::contains)
                    .forEach { removedId ->
                        val key = context.profileScopedKey(ticketImageCacheKey(removedId))
                        requestLock(context.accountId, key).withLock {
                            ensureCurrent(context)
                            accounts.removeCachedData(context.accountId, key)
                        }
                    }
            },
        ) { context -> context.transport { session -> loadTransportData(session) } }

    override suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean): ByteArray {
        val context = activeContext()
        val key = context.profileScopedKey(ticketImageCacheKey(bookingId))
        return requestLock(context.accountId, key).withLock {
            ensureCurrent(context)
            val original = try {
                accounts.readCachedData(context.accountId, key)
            } catch (_: SecureStorageCorruptedException) {
                runCatching { accounts.removeCachedData(context.accountId, key) }
                null
            } catch (_: SecureStorageDecodingException) {
                runCatching { accounts.removeCachedData(context.accountId, key) }
                null
            }
            ensureCurrent(context)
            if (!forceRefresh && original != null) return@withLock original
            try {
                val downloaded = context.transport { session -> loadTransportTicketImage(session, source) }
                ensureCurrent(context)
                accounts.writeCachedData(context.accountId, key, downloaded)
                ensureCurrent(context)
                downloaded
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                ensureCurrent(context)
                original ?: throw error
            }
        }
    }

    private fun ticketImageCacheKey(bookingId: String): String {
        require(bookingId.isNotEmpty() && bookingId.length <= 256)
        return "ticket-image-v1-" + bookingId.encodeToByteArray().joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    }

    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult {
        val context = activeContext()
        return context.transport { session -> bookTransport(session, request) }.also {
            safelyMarkTransportSnapshotStale(context)
        }
    }

    override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult {
        val context = activeContext()
        return context.transport { session -> deleteTransportBooking(session, bookingId) }.also {
            val imageKey = context.profileScopedKey(ticketImageCacheKey(bookingId))
            val imageRemoved = try {
                requestLock(context.accountId, imageKey).withLock { accounts.removeCachedData(context.accountId, imageKey) }
                true
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { false }
            safelyMarkTransportSnapshotStale(context, removedBookingId = bookingId.takeIf { imageRemoved })
        }
    }

    // ------------------------------------------------------------------------
    // Sebina Library Operations (Integrated Opaque Handle Vault)
    // ------------------------------------------------------------------------

    override suspend fun searchLibraryBooks(query: String, page: Int): LibraryCatalogPage {
        val context = activeContext()
        val authSession = sessions.currentAuthenticatedLibrarySessionOrNull(
            context.accountId,
            context.sessionState.account.activeProfileId,
        )
        val sdkPage = if (authSession != null) {
            withAuthenticatedLibrary(retryOnExpiry = true) { session -> searchBooks(session, query, page) }
        } else {
            sessions.withLibraryGuestSession { session -> searchBooks(session, query, page) }
        }
        return LibraryCatalogPage(
            query = query,
            books = sdkPage.books.map { book ->
                LibraryTextParser.parseCatalogBook(book.id, book.title, book.description)
            },
            page = sdkPage.page,
            total = sdkPage.total,
        )
    }

    override suspend fun loadLibraryBook(id: String): LibraryBookDetail {
        val context = activeContext()
        val existingAuth = sessions.currentAuthenticatedLibrarySessionOrNull(
            context.accountId,
            context.sessionState.account.activeProfileId,
        )
        var capturedSession: LibrarySession? = null
        val sdkBook: LibraryBook = if (existingAuth != null) {
            withAuthenticatedLibrary(retryOnExpiry = true) { session ->
                capturedSession = session
                loadBook(session, id)
            }
        } else {
            sessions.withLibraryGuestSession { session ->
                capturedSession = session
                loadBook(session, id)
            }
        }
        return mapAndRegisterBook(sdkBook, capturedSession!!)
    }

    override suspend fun prepareLibraryBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft {
        val existing = libraryHandleLock.withLock { bookActions[actionId]?.value }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        var createdDraft: LibraryBookRequestDraft? = null
        var draftSession: LibrarySession? = null
        withAuthenticatedLibrary(retryOnExpiry = true) { session ->
            val currentStored = libraryHandleLock.withLock { bookActions[actionId] }
            val actionForSession = if (currentStored != null && currentStored.session === session) {
                currentStored.value
            } else {
                val reloaded = loadBook(session, bookId)
                mapAndRegisterBook(reloaded, session)
                reloaded.actions.firstOrNull {
                    it.libraryCode == existing.libraryCode &&
                        it.movementType == existing.movementType &&
                        it.label == existing.label
                } ?: reloaded.actions.firstOrNull {
                    it.libraryCode == existing.libraryCode && it.movementType == existing.movementType
                } ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
            }
            createdDraft = prepareBookRequest(session, actionForSession)
            draftSession = session
        }
        val token = libraryHandleLock.withLock {
            val id = issueLibraryToken("draft")
            bookDrafts[id] = OwnedLibraryHandle(draftSession!!, createdDraft!!)
            id
        }
        return LibraryBookLoanDraft(draftId = token, description = createdDraft!!.description)
    }

    override suspend fun confirmLibraryBookRequest(draftId: String): LibraryLoanSubmissionResult {
        val stored = libraryHandleLock.withLock { bookDrafts.remove(draftId) }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        val submission = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            confirmBookRequest(session, stored.value, note = "")
        }
        return LibraryLoanSubmissionResult(
            description = submission.description,
            requiresVerification = submission.requiresVerification,
        )
    }

    override suspend fun loadLibraryMovements(forceRefresh: Boolean): List<LibraryMovementItem> {
        var owningSession: LibrarySession? = null
        val list = withAuthenticatedLibrary(retryOnExpiry = true) { session ->
            owningSession = session
            loadBookMovements(session)
        }
        return libraryHandleLock.withLock {
            movements.clear()
            list.map { item ->
                val token = if (item.canCancel || item.canRenew) {
                    issueLibraryToken("mov").also { movements[it] = OwnedLibraryHandle(owningSession!!, item) }
                } else {
                    null
                }
                LibraryTextParser.parseMovement(
                    id = item.id,
                    rawDescription = item.description,
                    canCancel = item.canCancel,
                    canRenew = item.canRenew,
                    actionToken = token,
                )
            }
        }
    }

    override suspend fun cancelLibraryMovement(movementToken: String) {
        val stored = libraryHandleLock.withLock { movements[movementToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            cancelBookRequest(session, stored.value)
        }
        libraryHandleLock.withLock { movements.remove(movementToken) }
    }

    override suspend fun renewLibraryMovement(movementToken: String): String {
        val stored = libraryHandleLock.withLock { movements[movementToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        return withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            renewBookLoan(session, stored.value)
        }
    }

    override suspend fun loadLibraryBookingOverview(): LibraryBookingOverview {
        var owningSession: LibrarySession? = null
        val ctx = withAuthenticatedLibrary(retryOnExpiry = true) { session ->
            owningSession = session
            loadBookingContext(session)
        }
        val contextId = libraryHandleLock.withLock {
            bookingContexts.clear()
            spaceBookings.clear()
            val id = issueLibraryToken("ctx")
            bookingContexts[id] = OwnedLibraryHandle(owningSession!!, ctx)
            id
        }
        val mappedBookings = libraryHandleLock.withLock {
            ctx.bookings.map { booking ->
                val token = if (booking.canCancel) {
                    issueLibraryToken("bkg").also { spaceBookings[it] = OwnedLibraryHandle(owningSession!!, booking) }
                } else {
                    null
                }
                LibraryTextParser.parseSpaceBooking(
                    id = booking.id,
                    rawDescription = booking.description,
                    canCancel = booking.canCancel,
                    bookingToken = token,
                )
            }
        }
        return LibraryBookingOverview(
            contextId = contextId,
            types = ctx.types.map { LibraryBookingOption(it.id, it.label) },
            bookings = mappedBookings,
        )
    }

    override suspend fun loadLibraryResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection {
        val stored = libraryHandleLock.withLock { bookingContexts[contextId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
        val selection = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            loadResourceSelection(session, stored.value, typeId)
        }
        return LibrarySpaceSelection(
            owners = selection.owners.map { LibraryBookingOption(it.id, it.label) },
            resources = selection.resources.map { LibraryBookingOption(it.id, it.label) },
        )
    }

    override suspend fun loadLibraryResourcesForOwner(ownerId: String): List<LibraryBookingOption> {
        val list = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            loadResourcesForOwner(session, ownerId)
        }
        return list.map { LibraryBookingOption(it.id, it.label) }
    }

    override suspend fun loadLibraryCalendar(
        contextId: String,
        resourceId: String,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): LibrarySpaceCalendar {
        val stored = libraryHandleLock.withLock { bookingContexts[contextId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
        var owningSession: LibrarySession? = null
        val calendar = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            owningSession = session
            loadCalendar(session, stored.value, resourceId, fromDate, toDate)
        }
        return registerCalendar(owningSession!!, calendar, page = 1)
    }

    override suspend fun loadLibraryCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar {
        val stored = libraryHandleLock.withLock { calendars[calendarId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        var owningSession: LibrarySession? = null
        val nextCalendar = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            owningSession = session
            loadCalendarPage(session, stored.value.first, page)
        }
        return registerCalendar(owningSession!!, nextCalendar, page = page)
    }

    override suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String,
        deviceAddresses: Map<String, String>,
    ): LibrarySpaceBookingOutcome {
        val stored = libraryHandleLock.withLock { calendars[calendarId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        val (sdkCalendar, slotMap) = stored.value
        val sdkSlots = slotIds.map { id ->
            slotMap[id] ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        }
        val sdkDeviceMap = buildMap<LibrarySlot, String> {
            deviceAddresses.forEach { (slotId, addr) ->
                slotMap[slotId]?.let { put(it, addr) }
            }
        }
        val result = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            bookResource(session, sdkCalendar, sdkSlots, note, sdkDeviceMap)
        }
        libraryHandleLock.withLock { calendars.remove(calendarId) }
        return LibrarySpaceBookingOutcome(confirmed = result.confirmed, serverCode = result.serverCode)
    }

    override suspend fun cancelLibrarySpaceBooking(bookingToken: String) {
        val stored = libraryHandleLock.withLock { spaceBookings[bookingToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            cancelResourceBooking(session, stored.value)
        }
        libraryHandleLock.withLock { spaceBookings.remove(bookingToken) }
    }

    private suspend fun mapAndRegisterBook(sdkBook: LibraryBook, session: LibrarySession): LibraryBookDetail {
        val mappedActions = libraryHandleLock.withLock {
            if (bookActions.size > 120) bookActions.clear()
            sdkBook.actions.map { action ->
                val token = issueLibraryToken("act")
                bookActions[token] = OwnedLibraryHandle(session, action)
                LibraryBookCopyAction(
                    actionId = token,
                    label = action.label.ifBlank { action.movementType },
                    libraryCode = action.libraryCode,
                    branchName = LibraryTextParser.branchDisplayName(action.libraryCode),
                    movementType = action.movementType,
                )
            }
        }
        return LibraryTextParser.parseBookDetail(
            id = sdkBook.id,
            title = sdkBook.title,
            rawDescription = sdkBook.description,
            actions = mappedActions,
        )
    }

    private suspend fun registerCalendar(
        session: LibrarySession,
        calendar: LibraryCalendar,
        page: Int,
    ): LibrarySpaceCalendar {
        val calId = issueLibraryToken("cal")
        val slotMap = linkedMapOf<String, LibrarySlot>()
        val mappedSlots = libraryHandleLock.withLock {
            calendars.clear()
            val mapped = calendar.slots.mapIndexed { index, slot ->
                val slotId = "${calId}_s$index"
                slotMap[slotId] = slot
                LibraryTextParser.parseSlot(
                    slotId = slotId,
                    label = slot.label,
                    rawDescription = slot.description,
                    requiresDeviceAddress = slot.requiresDeviceAddress,
                )
            }
            calendars[calId] = OwnedLibraryHandle(session, calendar to slotMap)
            mapped
        }
        return LibrarySpaceCalendar(
            calendarId = calId,
            resourceId = calendar.resourceId,
            page = page,
            description = calendar.description,
            slots = mappedSlots,
        )
    }

    // ------------------------------------------------------------------------
    // Encrypted Disk Cache Primitives (Zeroing ByteArray Buffers)
    // ------------------------------------------------------------------------

    private suspend fun safelyMarkTransportSnapshotStale(context: ActiveAccountContext, removedBookingId: String? = null) {
        try { markTransportSnapshotStale(context, removedBookingId) }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) {}
    }

    private suspend fun markTransportSnapshotStale(context: ActiveAccountContext, removedBookingId: String? = null) {
        val key = context.profileScopedKey("transport-data")
        requestLock(context.accountId, key).withLock {
            ensureCurrent(context)
            val cached = readEntry(context.accountId, key, TransportData.serializer()) ?: return@withLock
            val value = if (removedBookingId == null) cached.value else cached.value.copy(
                bookings = cached.value.bookings.filterNot { it.id == removedBookingId },
                totalCount = (cached.value.totalCount - cached.value.bookings.count { it.id == removedBookingId }).coerceAtLeast(0),
            )
            writeEntry(
                context.accountId,
                key,
                TransportData.serializer(),
                value,
                savedAtMillis = nowMillis() - UniCachePolicies.Transport.maxAgeMillis - 1L,
            )
        }
    }

    private suspend fun <T> cached(
        key: String,
        policy: UniCachePolicy,
        serializer: KSerializer<T>,
        forceRefresh: Boolean,
        requirePersistence: Boolean = false,
        onSaved: suspend (ActiveAccountContext, T?, T) -> Unit = { _, _, _ -> },
        fetch: suspend (ActiveAccountContext) -> T,
    ): T {
        val requestStartedAt = nowMillis()
        val context = activeContext()
        val accountId = context.accountId
        val scopedKey = context.profileScopedKey(key)
        return requestLock(accountId, scopedKey).withLock {
            ensureCurrent(context)
            val cached = try { readEntry(accountId, scopedKey, serializer) }
                catch (error: CancellationException) { throw error }
                catch (_: Exception) { null }
            ensureCurrent(context)
            if (cached != null && nowMillis() - cached.savedAtMillis in 0 until policy.maxAgeMillis &&
                (!forceRefresh || cached.savedAtMillis > requestStartedAt)
            ) {
                return@withLock cached.value
            }
            try {
                val value = fetch(context)
                ensureCurrent(context)
                var saved = false
                try {
                    writeEntry(accountId, scopedKey, serializer, value)
                    saved = true
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    if (requirePersistence) throw error
                }
                if (saved) {
                    try { onSaved(context, cached?.value, value) }
                    catch (error: CancellationException) { throw error }
                    catch (_: Exception) {}
                }
                ensureCurrent(context)
                value
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                ensureCurrent(context)
                if (fallbackToStaleCache) cached?.value ?: throw error else throw error
            }
        }
    }

    private suspend fun requestLock(accountId: String, key: String): Mutex =
        requestLocksGuard.withLock { requestLocks.getOrPut("$accountId|$key") { Mutex() } }

    private suspend fun <T> readEntry(accountId: String?, key: String, serializer: KSerializer<T>): CachedValue<T>? {
        val bytes = try {
            accounts.readCachedData(accountId, key)
        } catch (error: CancellationException) {
            throw error
        } catch (_: SecureStorageCorruptedException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            return null
        } catch (_: SecureStorageDecodingException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            return null
        } ?: return null
        return try {
            val envelope = json.decodeFromString<CacheEnvelope>(bytes.decodeToString(throwOnInvalidSequence = true))
            if (envelope.schemaVersion != CACHE_SCHEMA_VERSION) {
                runCatching { accounts.removeCachedData(accountId, key) }
                savedAtGuard.withLock { savedAtByKey.remove("$accountId|$key") }
                return null
            }
            savedAtGuard.withLock { savedAtByKey["$accountId|$key"] = envelope.savedAtMillis }
            CachedValue(envelope.savedAtMillis, json.decodeFromString(serializer, envelope.payload))
        } catch (error: CancellationException) {
            throw error
        } catch (_: SerializationException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            null
        } catch (_: CharacterCodingException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            null
        } catch (_: IllegalArgumentException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            null
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun <T> writeEntry(
        accountId: String?,
        key: String,
        serializer: KSerializer<T>,
        value: T,
        savedAtMillis: Long = nowMillis(),
    ) {
        val envelope = CacheEnvelope(CACHE_SCHEMA_VERSION, savedAtMillis, json.encodeToString(serializer, value))
        val bytes = json.encodeToString(CacheEnvelope.serializer(), envelope).encodeToByteArray()
        try {
            accounts.writeCachedData(accountId, key, bytes)
            savedAtGuard.withLock { savedAtByKey["$accountId|$key"] = savedAtMillis }
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun invalidate(accountId: String, key: String) {
        savedAtGuard.withLock { savedAtByKey.remove("$accountId|$key") }
        accounts.removeCachedData(accountId, key)
    }

    internal data class CachedValue<T>(val savedAtMillis: Long, val value: T)
    private data class ActiveAccountContext(
        val accountId: String,
        val sessionState: AppSessionState.Authenticated,
    )

    @Serializable
    private data class CacheEnvelope(val schemaVersion: Int, val savedAtMillis: Long, val payload: String)

    private companion object {
        const val CACHE_SCHEMA_VERSION = 3
    }
}

// ============================================================================
// 4. DemoUniGateway (Synthetic Account Implementation)
// ============================================================================

internal class DemoUniGateway(
    private val identity: () -> Pair<String, String?> = { DemoAccount.developerName(null) to DemoAccount.avatarFallback },
    private val imageLoader: suspend (String) -> ByteArray = { byteArrayOf() },
    private val portraitSharer: (suspend (String, ByteArray) -> ByteArray)? = null,
) : UniGateway, UniPortraitSharer {
    private val lock = Mutex()
    private var rounds: List<ExamRound>? = null
    private var transport: TransportData? = null
    private var surveys: List<SurveyCourse>? = null
    private var demoMovements: List<LibraryMovementItem> = listOf(
        LibraryMovementItem(
            id = "demo-mov-1",
            title = "Informatica : arte e mestiere",
            detail = "Biblioteca Campobasso · In prestito fino al 28/11/2026",
            branchName = "Campobasso",
            canCancel = false,
            canRenew = true,
            actionToken = "demo-mov-token-1",
        ),
    )
    private var demoSpaceBookings: List<LibrarySpaceBooking> = listOf(
        LibrarySpaceBooking(
            id = "demo-space-1",
            title = "Sala Studio Centrale · Campobasso",
            description = "Sala Studio Centrale · Campobasso · 20/11/2026 09:00 - 13:00",
            dateLabel = "20/11/2026",
            timeLabel = "09:00 – 13:00",
            canCancel = true,
            bookingToken = "demo-space-token-1",
        ),
    )
    private var pendingDraftBookTitle: String = ""
    private suspend fun feedback() = readAppString(Res.string.ui_demo_action)

    override suspend fun loadCareer(forceRefresh: Boolean) = DemoCatalog.load().career
    override suspend fun loadProfessorDashboard(forceRefresh: Boolean) = ProfessorDashboard()
    override suspend fun loadStudyPlan(forceRefresh: Boolean) = DemoCatalog.load().plan
    override suspend fun loadExamRounds(forceRefresh: Boolean) = lock.withLock { rounds ?: DemoCatalog.load().exams }
    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean) = DemoCatalog.load().syllabuses.first { it.adsceId == adsceId }
    override suspend fun bookExamRound(round: ExamRound): String {
        lock.withLock { rounds = (rounds ?: DemoCatalog.load().exams).map { if (it.appId == round.appId) it.copy(booked = true) else it } }
        return feedback()
    }
    override suspend fun cancelExamRound(round: ExamRound): String {
        lock.withLock { rounds = (rounds ?: DemoCatalog.load().exams).map { if (it.appId == round.appId) it.copy(booked = false) else it } }
        return feedback()
    }
    override suspend fun loadTaxes(forceRefresh: Boolean) = DemoCatalog.load().taxes
    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetails {
        val (name, avatar) = identity()
        return DemoCatalog.load().student.copy(fullName = name, photoUrl = avatar)
    }
    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray = imageLoader(source)
    override suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray = portraitSharer?.invoke(source, bytes) ?: bytes
    override suspend fun loadConnectedDevices(forceRefresh: Boolean) = DemoCatalog.load().devices
    override suspend fun disconnectDevice(targetToken: String) = feedback()
    override suspend fun disconnectAllOtherDevices() = feedback()
    override suspend fun loadAttendanceHistory(forceRefresh: Boolean) = DemoCatalog.load().attendance
    override suspend fun registerAttendance(qrCode: String, deviceLatitude: Double?, deviceLongitude: Double?, deviceAccuracyMeters: Double?) = feedback()
    override suspend fun loadUniversityNews(forceRefresh: Boolean) = DemoCatalog.load().news
    override suspend fun loadUniversityContacts(forceRefresh: Boolean) = DemoCatalog.load().contacts
    override suspend fun loadSurveyCourses(forceRefresh: Boolean) = lock.withLock { surveys ?: DemoCatalog.load().surveys }
    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyForm {
        val fixture = DemoCatalog.load()
        return SurveyForm(
            "demo-user",
            "demo-questionnaire",
            listOf(
                SurveyPage(
                    "demo-page",
                    listOf(
                        SurveyQuestion(
                            "demo-question",
                            fixture.surveyQuestion,
                            fixture.surveyAnswers.mapIndexed { i, text -> SurveyAnswerOption("demo-answer-$i", text) },
                            pageId = "demo-page",
                        ),
                    ),
                ),
            ),
        )
    }
    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String {
        lock.withLock { surveys = (surveys ?: DemoCatalog.load().surveys).map { if (it.courseId == courseId) it.copy(completed = true) else it } }
        return feedback()
    }
    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean) =
        loadSurveyCourses(false).firstOrNull { it.adCod == adCod }?.completed == true
    override suspend fun loadTransportData(forceRefresh: Boolean) = lock.withLock { transport ?: DemoCatalog.load().transport }
    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult = lock.withLock {
        val current = transport ?: DemoCatalog.load().transport
        val booking = TransportBooking(
            "demo-booking-${current.bookings.size + 1}",
            "DEMO-${current.bookings.size + 1}",
            request.routeCode,
            request.date.toString(),
            request.direction.name,
            request.direction == TransportDirection.RETURN,
            "UNIAPP-DEMO-NOT-A-VALID-TICKET",
        )
        transport = current.copy(bookings = current.bookings + booking, totalCount = current.totalCount + 1)
        TransportActionResult.Completed
    }
    override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult = lock.withLock {
        val current = transport ?: DemoCatalog.load().transport
        val bookings = current.bookings.filterNot { it.id == bookingId }
        transport = current.copy(bookings = bookings, totalCount = bookings.size)
        TransportActionResult.Completed
    }

    private val demoBooks = listOf(
        LibraryCatalogBook(
            id = "MOL00065117",
            title = "Informatica : arte e mestiere",
            subtitle = "Ceri, Stefano",
            metadata = "1999",
            branches = listOf("Campobasso", "Pesche", "Termoli"),
        ),
        LibraryCatalogBook(
            id = "MOL00371948",
            title = "Informatica giuridica",
            subtitle = "Ziccardi, Giovanni",
            metadata = "2020",
            branches = listOf("Campobasso", "Pesche"),
        ),
        LibraryCatalogBook(
            id = "MOL00453666",
            title = "Principi di sistemi operativi e reti",
            subtitle = "Tanenbaum, Andrew S.",
            metadata = "2022",
            branches = listOf("Pesche"),
        ),
        LibraryCatalogBook(
            id = "MOL00511204",
            title = "Economia aziendale e bilancio",
            subtitle = "Airoldi, Giuseppe",
            metadata = "2021",
            branches = listOf("Campobasso", "Termoli"),
        ),
    )

    override suspend fun searchLibraryBooks(query: String, page: Int): LibraryCatalogPage {
        val q = query.trim().lowercase()
        val matched = demoBooks.filter {
            q.isBlank() || it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q)
        }
        return LibraryCatalogPage(query = query, books = matched, page = page, total = matched.size)
    }

    override suspend fun loadLibraryBook(id: String): LibraryBookDetail {
        val book = demoBooks.firstOrNull { it.id == id } ?: demoBooks.first()
        return LibraryBookDetail(
            id = book.id,
            title = book.title,
            author = book.subtitle,
            year = book.metadata,
            hasAvailableDocument = true,
            branches = book.branches,
            inventory = "D1 1225",
            shelfmark = "PE SL004 CERIS",
            notes = "1 v.",
            description = "${book.title} · ${book.subtitle} (${book.metadata}). Copia disponibile presso le sedi d'Ateneo.",
            actions = listOf(
                LibraryBookCopyAction(
                    actionId = "demo-act-${book.id}-MOLBU",
                    label = "Richiesta di prestito",
                    libraryCode = "MOLBU",
                    branchName = "Biblioteca di Ateneo · Campobasso",
                    movementType = "RPE",
                ),
                LibraryBookCopyAction(
                    actionId = "demo-act-${book.id}-MOLBA",
                    label = "Richiesta di prestito",
                    libraryCode = "MOLBA",
                    branchName = "Biblioteca di Ateneo · Pesche",
                    movementType = "RPE",
                ),
            ),
        )
    }

    override suspend fun prepareLibraryBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft {
        val book = demoBooks.firstOrNull { it.id == bookId } ?: demoBooks.first()
        val branch = if (actionId.endsWith("MOLBA")) "Pesche" else "Campobasso"
        pendingDraftBookTitle = book.title
        return LibraryBookLoanDraft(
            draftId = "demo-draft-${demoMovements.size + 1}",
            description = "${book.title} — Richiesta di prestito ($branch)",
        )
    }

    override suspend fun confirmLibraryBookRequest(draftId: String): LibraryLoanSubmissionResult {
        lock.withLock {
            val idx = demoMovements.size + 1
            demoMovements = demoMovements + LibraryMovementItem(
                id = "demo-mov-$idx",
                title = pendingDraftBookTitle.ifBlank { "Documento biblioteca" },
                detail = "Biblioteca Campobasso · Richiesta registrata (modalità demo)",
                branchName = "Campobasso",
                canCancel = true,
                canRenew = false,
                actionToken = "demo-mov-token-$idx",
            )
        }
        return LibraryLoanSubmissionResult(description = feedback(), requiresVerification = true)
    }

    override suspend fun loadLibraryMovements(forceRefresh: Boolean): List<LibraryMovementItem> =
        lock.withLock { demoMovements }

    override suspend fun cancelLibraryMovement(movementToken: String) {
        lock.withLock {
            demoMovements = demoMovements.filterNot { it.actionToken == movementToken }
        }
    }

    override suspend fun renewLibraryMovement(movementToken: String): String = feedback()

    override suspend fun loadLibraryBookingOverview(): LibraryBookingOverview = lock.withLock {
        LibraryBookingOverview(
            contextId = "demo-ctx",
            types = listOf(
                LibraryBookingOption("study", "Posto sala studio"),
                LibraryBookingOption("pickup", "Appuntamento ritiro prestito"),
            ),
            bookings = demoSpaceBookings,
        )
    }

    override suspend fun loadLibraryResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection =
        if (typeId == "study") {
            LibrarySpaceSelection(
                owners = listOf(
                    LibraryBookingOption("MOLBU", "Campobasso · Sede Centrale"),
                    LibraryBookingOption("MOLBA", "Pesche · Scienze"),
                ),
                resources = listOf(
                    LibraryBookingOption("cb-sala-1", "Sala Lettura Piano 1"),
                    LibraryBookingOption("cb-sala-pc", "Postazione multimediale"),
                ),
            )
        } else {
            LibrarySpaceSelection(
                owners = emptyList(),
                resources = listOf(LibraryBookingOption("desk-cb", "Sportello prestiti Campobasso")),
            )
        }

    override suspend fun loadLibraryResourcesForOwner(ownerId: String): List<LibraryBookingOption> =
        if (ownerId == "MOLBA") {
            listOf(LibraryBookingOption("pe-sala-1", "Sala Studio Pesche"))
        } else {
            listOf(
                LibraryBookingOption("cb-sala-1", "Sala Lettura Piano 1"),
                LibraryBookingOption("cb-sala-pc", "Postazione multimediale"),
            )
        }

    override suspend fun loadLibraryCalendar(
        contextId: String,
        resourceId: String,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): LibrarySpaceCalendar = LibrarySpaceCalendar(
        calendarId = "demo-cal-$resourceId",
        resourceId = resourceId,
        page = 1,
        description = "Disponibilità simulata",
        slots = listOf(
            LibrarySpaceSlot(
                slotId = "slot-1",
                label = "1",
                timeLabel = "09:00 – 13:00",
                description = "21/11/2026 09:00 - 13:00 · 14 posti",
                dateLabel = "21/11/2026",
                seatsLabel = "14 / 20 posti",
                requiresDeviceAddress = false,
            ),
            LibrarySpaceSlot(
                slotId = "slot-2",
                label = "1",
                timeLabel = "14:00 – 18:00",
                description = "21/11/2026 14:00 - 18:00 · 9 posti",
                dateLabel = "21/11/2026",
                seatsLabel = "9 / 20 posti",
                requiresDeviceAddress = false,
            ),
        ),
    )

    override suspend fun loadLibraryCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar =
        loadLibraryCalendar("demo-ctx", calendarId.removePrefix("demo-cal-"), null, null).copy(page = page)

    override suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String,
        deviceAddresses: Map<String, String>,
    ): LibrarySpaceBookingOutcome {
        lock.withLock {
            val idx = demoSpaceBookings.size + 1
            demoSpaceBookings = demoSpaceBookings + LibrarySpaceBooking(
                id = "demo-space-$idx",
                title = "Prenotazione dimostrativa",
                description = "Prenotazione dimostrativa (${slotIds.size} fasce)",
                dateLabel = "21/11/2026",
                timeLabel = "09:00 – 13:00",
                canCancel = true,
                bookingToken = "demo-space-token-$idx",
            )
        }
        return LibrarySpaceBookingOutcome(confirmed = true, serverCode = 1)
    }

    override suspend fun cancelLibrarySpaceBooking(bookingToken: String) {
        lock.withLock {
            demoSpaceBookings = demoSpaceBookings.filterNot { it.bookingToken == bookingToken }
        }
    }
}

// ============================================================================
// 5. System & Application-Scoped Controllers (Updates + GitHub Project)
// ============================================================================

internal fun interface UniUpdateManifestLoader {
    suspend fun load(installedBuild: InstalledAppBuild): UniAppUpdateManifest?
}

internal class SdkUniUpdateManifestLoader(
    private val backend: UniBackendService,
) : UniUpdateManifestLoader {
    override suspend fun load(installedBuild: InstalledAppBuild): UniAppUpdateManifest? =
        backend.loadAppUpdateInfo(
            currentVersion = installedBuild.versionName,
            currentVersionCode = installedBuild.versionCode,
            preferredUpdateChannel = APP_CHANNEL_STABLE,
        )?.toUniAppUpdateManifest()
}

internal class UniUpdateManager(
    private val source: UniUpdateManifestLoader,
    installedBuild: InstalledAppBuild,
    private val launcher: PlatformUpdateLauncher,
    private val policy: UniCachePolicy = UniCachePolicies.AppUpdate,
    private val nowMillis: () -> Long = ::currentEpochMillis,
) {
    private val operationLock = Mutex()
    private var lastCheckedAtMillis: Long? = null
    private val mutableState = MutableStateFlow(AppUpdateState(installedBuild = installedBuild))
    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()

    suspend fun observeInstallation() {
        launcher.updates.collect { progress ->
            mutableState.update {
                it.copy(
                    phase = progress.phase,
                    message = progress.message,
                    installedBuild = progress.installedBuild ?: it.installedBuild,
                    updateInfo = if (progress.phase == AppUpdatePhase.UpToDate)
                        it.updateInfo?.copy(isUpdateAvailable = false, isMandatory = false) else it.updateInfo,
                    downloadedBytes = progress.downloadedBytes,
                    totalBytes = progress.totalBytes,
                )
            }
        }
    }

    suspend fun refresh(force: Boolean = true) {
        if (!operationLock.tryLock()) return
        val previous = mutableState.value
        try {
            val now = nowMillis()
            val lastChecked = lastCheckedAtMillis
            if (!force && lastChecked != null && now - lastChecked in 0 until policy.maxAgeMillis &&
                (previous.phase == AppUpdatePhase.UpToDate || previous.phase == AppUpdatePhase.Available)
            ) {
                return
            }
            val restoringInstall = previous.phase == AppUpdatePhase.Installing && previous.updateInfo == null
            if (previous.isBusy && !restoringInstall) return
            if (!restoringInstall) mutableState.value = previous.copy(phase = AppUpdatePhase.Checking, message = null)
            val info = source.load(previous.installedBuild)
                ?: error(readAppString(Res.string.msg_informazioni_di_aggiornamento_non_disponibili_per_questo_canale))
            val installedCode = previous.installedBuild.versionCode
            val latestCode = info.latestVersionCode
            val newer = if (installedCode != null && latestCode != null && installedCode > 0 && latestCode > 0) {
                latestCode > installedCode
            } else info.isUpdateAvailable
            val available = newer
            lastCheckedAtMillis = nowMillis()
            mutableState.value = previous.copy(
                phase = if (restoringInstall) mutableState.value.phase else if (available) AppUpdatePhase.Available else AppUpdatePhase.UpToDate,
                updateInfo = info.copy(
                    isUpdateAvailable = available,
                    isMandatory = available && info.isMandatory && !previous.installedBuild.isDebuggable,
                ),
                message = null,
                downloadedBytes = 0,
                totalBytes = null,
            )
        } catch (error: CancellationException) {
            mutableState.value = previous
            throw error
        } catch (error: Throwable) {
            mutableState.value = previous.copy(
                phase = if (previous.updateInfo?.isUpdateAvailable == true) AppUpdatePhase.Available else AppUpdatePhase.Failed,
                message = error.userMessage { readAppString(Res.string.msg_impossibile_controllare_gli_aggiornamenti) },
            )
        } finally {
            operationLock.unlock()
        }
    }

    suspend fun startUpdate(): Boolean {
        if (!operationLock.tryLock()) return false
        val current = mutableState.value
        try {
            if (!current.canStartUpdate) {
                if (!current.isBusy) {
                    mutableState.value = current.copy(message = readAppString(Res.string.msg_nessun_aggiornamento_installabile_disponibile))
                }
                return false
            }
            mutableState.value = current.copy(
                phase = AppUpdatePhase.Downloading,
                message = null,
                downloadedBytes = 0,
                totalBytes = null,
            )
            return when (val result = launcher.start(current.downloadUrl!!, current.updateInfo?.latestVersionCode)) {
                PlatformUpdateLaunchResult.Started -> {
                    mutableState.update {
                        if (it.phase == AppUpdatePhase.Downloading || it.phase == AppUpdatePhase.Verifying)
                            it.copy(phase = AppUpdatePhase.Installing) else it
                    }
                    true
                }
                PlatformUpdateLaunchResult.OpenedExternalStore -> {
                    val message = readAppString(Res.string.msg_update_finish_in_app_store)
                    mutableState.update { it.copy(phase = AppUpdatePhase.Available, message = message) }
                    true
                }
                is PlatformUpdateLaunchResult.Failed -> {
                    mutableState.update { it.copy(phase = AppUpdatePhase.Available, message = result.message) }
                    false
                }
            }
        } catch (error: CancellationException) {
            mutableState.update { if (it.phase == AppUpdatePhase.Installing) it else current }
            throw error
        } catch (error: Throwable) {
            mutableState.update {
                it.copy(
                    phase = AppUpdatePhase.Available,
                    message = error.userMessage { readAppString(Res.string.msg_impossibile_avviare_laggiornamento) },
                )
            }
            return false
        } finally {
            operationLock.unlock()
        }
    }
}

/** Public, unauthenticated GitHub requests, isolated from university cookies and credentials. */
internal class GitHubProjectClient(
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 20_000
        }
    },
    private val now: () -> Long = ::currentEpochMillis,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(previous: GitHubProjectSnapshot? = null): GitHubProjectSnapshot = coroutineScope {
        suspend fun <T> section(fallback: T, load: suspend () -> T): Pair<T, Boolean> = try {
            load() to false
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            fallback to true
        }
        val authorTask = async {
            section(previous?.author) {
                json.decodeFromString<GitHubAuthor>(request("$API/users/${ProjectInfo.authorLogin}").first)
            }
        }
        val projectTask = async {
            section(previous?.project) {
                json.decodeFromString<GitHubRepositoryInfo>(request("$API/repos/${ProjectInfo.repository}").first)
            }
        }
        val repositoriesTask = async {
            section(previous?.repositories.orEmpty()) {
                pages("$API/users/${ProjectInfo.authorLogin}/repos?sort=updated&per_page=100")
                    .map { json.decodeFromJsonElement(GitHubRepositoryInfo.serializer(), it) }.distinctBy { it.id }
            }
        }
        val contributorsTask = async {
            section(previous?.contributors.orEmpty()) {
                pages("$API/repos/${ProjectInfo.repository}/contributors?anon=true&per_page=100")
                    .map { json.decodeFromJsonElement(GitHubContributor.serializer(), it) }.sortedByDescending { it.contributions }
            }
        }
        val (author, authorFailed) = authorTask.await()
        val (project, projectFailed) = projectTask.await()
        val (repositories, repositoriesFailed) = repositoriesTask.await()
        val (contributors, contributorsFailed) = contributorsTask.await()
        if (author == null && project == null && repositories.isEmpty() && contributors.isEmpty()) {
            error(readAppString(Res.string.msg_github_non_e_disponibile_riprova_piu_tardi))
        }
        val incomplete = authorFailed || projectFailed || repositoriesFailed || contributorsFailed
        GitHubProjectSnapshot(
            author,
            project,
            repositories,
            contributors,
            if (incomplete) previous?.fetchedAt ?: 0 else now(),
            incomplete,
        )
    }

    private suspend fun pages(initial: String): List<JsonElement> {
        val items = mutableListOf<JsonElement>()
        val visited = mutableSetOf<String>()
        var next: String? = initial
        while (next != null) {
            check(visited.add(next)) { "GitHub returned a pagination cycle" }
            val (body, link) = request(next)
            items.addAll(json.parseToJsonElement(body) as JsonArray)
            next = link?.split(',')?.firstOrNull { it.contains("rel=\"next\"") }
                ?.substringAfter('<')?.substringBefore('>')
        }
        return items
    }

    private suspend fun request(url: String): Pair<String, String?> {
        val parsed = Url(url)
        require(parsed.protocol.name == "https" && parsed.host == "api.github.com")
        val response = client.get(url) {
            header("Accept", "application/vnd.github+json")
            header("X-GitHub-Api-Version", "2022-11-28")
            header("User-Agent", "UniApp")
        }
        check(response.status.value in 200..299) { "GitHub HTTP ${response.status.value}" }
        return (if (response.status.value == 204) "[]" else response.bodyAsText()) to response.headers["Link"]
    }

    fun close() { client.close() }
    private companion object { const val API = "https://api.github.com" }
}

data class ProjectDataState(
    val data: GitHubProjectSnapshot? = null,
    val loading: Boolean = true,
    val stale: Boolean = false,
    val error: Boolean = false,
    val authorLogin: String = ProjectInfo.authorLogin,
    val authorUrl: String = ProjectInfo.authorUrl,
    val repositoryUrl: String = ProjectInfo.repositoryUrl,
    val issuesUrl: String = ProjectInfo.issuesUrl,
)

internal class UniProjectManager(
    private val scope: CoroutineScope,
    private val store: UniLocalDataStore,
    private val client: GitHubProjectClient = GitHubProjectClient(),
    private val policy: UniCachePolicy = UniCachePolicies.GitHubProject,
) {
    private val lock = Mutex()
    private val state = MutableStateFlow(ProjectDataState())
    val uiState = state.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        scope.launch {
            if (!lock.tryLock()) return@launch
            try {
                val cached = state.value.data ?: try {
                    store.read(LocalDataScope.Application, UniAppDataKeys.GitHubProject)
                        .takeIf { it.fetchedAt > 0 || it.author != null }
                } catch (error: CancellationException) { throw error } catch (_: Exception) { null }
                state.value = ProjectDataState(data = cached)
                val age = cached?.let { currentEpochMillis() - it.fetchedAt }
                if (!force && cached != null && !cached.incomplete && age != null && age in 0 until policy.maxAgeMillis) {
                    state.value = ProjectDataState(data = cached, loading = false)
                    return@launch
                }
                val data = client.load(cached)
                state.value = ProjectDataState(data, loading = false, stale = data.incomplete, error = data.incomplete)
                try {
                    store.write(LocalDataScope.Application, UniAppDataKeys.GitHubProject, data)
                } catch (error: CancellationException) { throw error } catch (_: Exception) {
                }
            } catch (error: CancellationException) {
                state.value = state.value.copy(loading = false)
                throw error
            } catch (_: Exception) {
                state.value = state.value.copy(loading = false, stale = state.value.data != null, error = true)
            } finally { lock.unlock() }
        }
    }

    fun close() { client.close() }
}
