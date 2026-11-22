package com.anto426.uniapp.data

import com.anto426.uniapp.account.data.AccountAvatar
import com.anto426.uniapp.account.data.AccountAvatarStore
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountProfileSummary
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniLoginProfileOption
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.uniapp.data.contract.LibraryBookDetail
import com.anto426.uniapp.data.contract.LibraryBookLoanDraft
import com.anto426.uniapp.data.contract.LibraryBookingOption
import com.anto426.uniapp.data.contract.LibraryBookingOverview
import com.anto426.uniapp.data.contract.LibraryCatalogPage
import com.anto426.uniapp.data.contract.LibraryLoanSubmissionResult
import com.anto426.uniapp.data.contract.LibraryMovementItem
import com.anto426.uniapp.data.contract.LibrarySpaceBookingOutcome
import com.anto426.uniapp.data.contract.LibrarySpaceCalendar
import com.anto426.uniapp.data.contract.LibrarySpaceSelection
import com.anto426.uniapp.data.contract.NewsFeed
import com.anto426.uniapp.data.contract.QuestionnaireForm
import com.anto426.uniapp.data.contract.ResolvedCourseDetail
import com.anto426.uniapp.data.contract.TripBookingOutcome
import com.anto426.uniapp.data.contract.TripBookingRequest
import com.anto426.uniapp.data.contract.UniAcademicContract
import com.anto426.uniapp.data.contract.UniAccountContract
import com.anto426.uniapp.data.contract.UniAttendanceContract
import com.anto426.uniapp.data.contract.UniCampusContract
import com.anto426.uniapp.data.contract.UniContractOwner
import com.anto426.uniapp.data.contract.UniDevicesContract
import com.anto426.uniapp.data.contract.UniExamsContract
import com.anto426.uniapp.data.contract.UniIdentityContract
import com.anto426.uniapp.data.contract.UniProfessorContract
import com.anto426.uniapp.data.contract.UniSurveysContract
import com.anto426.uniapp.data.contract.UniTaxesContract
import com.anto426.uniapp.data.contract.UniTransportContract
import com.anto426.uniapp.data.contract.buildAcademicContract
import com.anto426.uniapp.data.contract.buildAttendanceContract
import com.anto426.uniapp.data.contract.buildDevicesContract
import com.anto426.uniapp.data.contract.buildExamsContract
import com.anto426.uniapp.data.contract.buildIdentityContract
import com.anto426.uniapp.data.contract.buildProfessorContract
import com.anto426.uniapp.data.contract.buildSurveySaveRequest
import com.anto426.uniapp.data.contract.buildSurveysContract
import com.anto426.uniapp.data.contract.buildTaxesContract
import com.anto426.uniapp.data.contract.buildTransportContract
import com.anto426.uniapp.data.contract.filterUpcomingExamRounds
import com.anto426.uniapp.data.contract.resolveCourseDetailContract
import com.anto426.uniapp.data.contract.stableUiId
import com.anto426.uniapp.data.contract.toContacts
import com.anto426.uniapp.data.contract.toSdkRequest
import com.anto426.uniapp.data.contract.toTripBookingOutcome
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.UniAppModuleBuildInfo
import com.anto426.uniapp.updates.model.UniAppUpdateManifest
import com.anto426.unisdk.model.AppUpdateInfo
import com.anto426.unisdk.model.CareerType
import com.anto426.unisdk.model.LoginCareerOption
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.platform.AppInfoProvider
import com.anto426.unisdk.platform.ProjectInfo
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.session.UniUserProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

// ============================================================================
// 1. Domain Facades of UniRepository
// ============================================================================

class UniSessionDomain internal constructor(
    internal val controller: UniSessionController,
) {
    val state: StateFlow<AppSessionState> get() = controller.state
    val accountsRevision: StateFlow<Long> get() = controller.accountsRevision
    val avatars: AccountAvatarStore get() = controller.avatars
    val avatarImages: StateFlow<Map<String, AccountAvatar>> get() = controller.avatars.images
    val storageResetNotice: StateFlow<Boolean> get() = controller.storageResetNotice

    suspend fun initialize() = controller.initialize()
    suspend fun acknowledgeStorageReset() = controller.acknowledgeStorageReset()
    suspend fun unlockRequiredAccount() = controller.unlockRequiredAccount()
    suspend fun cancelUnlock() = controller.cancelUnlock()
    suspend fun unlockWithPassword(password: String): PasswordUnlockResult = controller.unlockWithPassword(password)
    suspend fun authenticate(
        credentials: UniAccountCredentials,
        selectedCareer: UniLoginProfileOption? = null,
        preferredAccountId: String? = null,
    ) = controller.authenticate(credentials, selectedCareer, preferredAccountId)
    suspend fun cancelAuthentication() = controller.cancelAuthentication()
    suspend fun activate(accountId: String): AppSessionState = controller.activate(accountId)
    suspend fun activateProfile(profileId: String): AppSessionState = controller.activateProfile(profileId)
    suspend fun signOut() = controller.signOut()
    suspend fun accounts(): List<UniAccountSummary> = controller.accounts()
    suspend fun removeAccount(accountId: String, authenticator: BiometricAuthenticator): Boolean =
        controller.removeAccount(accountId, authenticator)
}

class UniDidacticsDomain internal constructor(
    val engine: UniCacheEngine,
    private val simulations: GradeSimulationRepository? = null,
) {
    val portrait: StateFlow<ByteArray?> get() = engine.portrait

    fun startPortrait(account: UniAccountSummary?) = engine.startPortrait(account)
    fun refreshPortrait() = engine.refreshPortrait()

    fun refresh(requests: List<UniDatasetKey<*>>, force: Boolean = false) = engine.refresh(requests, force)

    suspend fun prepareNews(news: List<UniversityNews>, dispatcher: CoroutineDispatcher = Dispatchers.Default): NewsFeed =
        engine.prepareNews(news, dispatcher)

    suspend fun submitQuestionnaire(
        courseId: String,
        form: QuestionnaireForm,
        selectedAnswers: Map<String, Set<String>>,
        freeTextAnswers: Map<String, String>,
    ): String {
        val details = engine.loadStudentDetails()
        val studentId = listOfNotNull(details.stuId, details.matId, details.matricola)
            .firstOrNull { it.isNotBlank() }
            ?: error(getString(Res.string.msg_identificativo_studente_non_disponibile))
        val userCompId = form.userCompId?.takeIf(String::isNotBlank)
            ?: error(getString(Res.string.msg_identificativo_compilazione_non_disponibile))
        val questCompId = form.questCompId?.takeIf(String::isNotBlank)
            ?: error(getString(Res.string.msg_identificativo_questionario_non_disponibile))
        val request = buildSurveySaveRequest(
            studentId = studentId,
            userCompId = userCompId,
            questCompId = questCompId,
            form = form,
            selectedAnswers = selectedAnswers,
            freeTextAnswers = freeTextAnswers,
        )
        return engine.saveSurvey(courseId, request)
    }

    suspend fun toggleExamBooking(examId: String, today: LocalDate): String {
        val rounds = engine.loadExamRounds(forceRefresh = false)
            .filterUpcomingExamRounds(today)
        val round = rounds.firstOrNull { it.stableUiId() == examId }
            ?: error(getString(Res.string.msg_operazione_sull_appello_non_riuscita))
        if (!round.booked && !round.isBookable) {
            error(getString(Res.string.ui_exam_not_yet_bookable))
        }
        return if (round.booked) engine.cancelExamRound(round) else engine.bookExamRound(round)
    }

    suspend fun registerAttendance(
        qrCode: String,
        deviceLatitude: Double? = null,
        deviceLongitude: Double? = null,
        deviceAccuracyMeters: Double? = null,
    ): String = engine.registerAttendance(qrCode, deviceLatitude, deviceLongitude, deviceAccuracyMeters)
}

class UniTransportDomain internal constructor(
    val engine: UniCacheEngine,
) {
    fun refresh(force: Boolean = false) = engine.refresh(listOf(UniDatasets.Transport), force)

    suspend fun loadTicketImage(bookingId: String, source: String, forceRefresh: Boolean = false): ByteArray =
        engine.loadTransportTicketImage(bookingId, source, forceRefresh)

    suspend fun bookTransport(request: TripBookingRequest): TripBookingOutcome =
        engine.bookTransport(request.toSdkRequest()).toTripBookingOutcome()

    suspend fun deleteBooking(bookingId: String): TripBookingOutcome =
        engine.deleteTransportBooking(bookingId).toTripBookingOutcome()
}

class UniLibraryDomain internal constructor(
    val engine: UniCacheEngine,
) {
    suspend fun searchBooks(query: String, page: Int = 1): LibraryCatalogPage =
        engine.searchLibraryBooks(query, page)

    suspend fun loadBook(id: String): LibraryBookDetail =
        engine.loadLibraryBook(id)

    suspend fun prepareBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft =
        engine.prepareLibraryBookRequest(bookId, actionId)

    suspend fun confirmBookRequest(draftId: String): LibraryLoanSubmissionResult =
        engine.confirmLibraryBookRequest(draftId)

    suspend fun loadMovements(forceRefresh: Boolean = false): List<LibraryMovementItem> =
        engine.loadLibraryMovements(forceRefresh)

    suspend fun cancelMovement(movementToken: String) =
        engine.cancelLibraryMovement(movementToken)

    suspend fun renewMovement(movementToken: String): String =
        engine.renewLibraryMovement(movementToken)

    suspend fun loadBookingOverview(): LibraryBookingOverview =
        engine.loadLibraryBookingOverview()

    suspend fun loadResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection =
        engine.loadLibraryResourceSelection(contextId, typeId)

    suspend fun loadResourcesForOwner(ownerId: String): List<LibraryBookingOption> =
        engine.loadLibraryResourcesForOwner(ownerId)

    suspend fun loadCalendar(
        contextId: String,
        resourceId: String,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null,
    ): LibrarySpaceCalendar = engine.loadLibraryCalendar(contextId, resourceId, fromDate, toDate)

    suspend fun loadCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar =
        engine.loadLibraryCalendarPage(calendarId, page)

    suspend fun bookSpace(
        calendarId: String,
        slotIds: List<String>,
        note: String = "",
        deviceAddresses: Map<String, String> = emptyMap(),
    ): LibrarySpaceBookingOutcome = engine.bookLibrarySpace(calendarId, slotIds, note, deviceAddresses)

    suspend fun cancelSpaceBooking(bookingToken: String) =
        engine.cancelLibrarySpaceBooking(bookingToken)
}

class UniServicesDomain internal constructor(
    val engine: UniCacheEngine,
) {
    val cachedNewsFeed: NewsFeed? get() = engine.newsFeed

    suspend fun prepareNews(news: List<UniversityNews>, dispatcher: CoroutineDispatcher = Dispatchers.Default): NewsFeed =
        engine.prepareNews(news, dispatcher)

    suspend fun disconnectDevice(targetToken: String): String =
        engine.disconnectDevice(targetToken)

    suspend fun disconnectAllOtherDevices(): String =
        engine.disconnectAllOtherDevices()
}

class UniSystemDomain internal constructor(
    internal val updateManager: UniUpdateManager?,
    internal val projectManager: UniProjectManager?,
) {
    val updateState: StateFlow<AppUpdateState> =
        updateManager?.state ?: MutableStateFlow(AppUpdateState(installedBuild = currentInstalledBuild())).asStateFlow()

    val projectState: StateFlow<ProjectDataState> =
        projectManager?.uiState ?: MutableStateFlow(ProjectDataState()).asStateFlow()

    val authorLogin: String get() = ProjectInfo.authorLogin
    val authorUrl: String get() = ProjectInfo.authorUrl
    val repositoryUrl: String get() = ProjectInfo.repositoryUrl
    val issuesUrl: String get() = ProjectInfo.issuesUrl

    fun bugReportUrl(): String = ProjectInfo.bugReportUrl()

    fun currentInstalledBuild(): InstalledAppBuild {
        val info = AppInfoProvider.current
        return InstalledAppBuild(
            versionName = info.versionName,
            versionCode = info.versionCode,
            isDebuggable = info.isDebuggable,
            applicationId = info.applicationId,
            name = info.name,
            platform = info.platform,
            osVersion = info.osVersion,
            deviceModel = info.deviceModel,
            sourceRevision = info.sourceRevision,
            appStoreUrl = info.appStoreUrl,
            modules = info.modules.map {
                UniAppModuleBuildInfo(
                    name = it.name,
                    version = it.version,
                    revision = it.revision,
                    hasLocalChanges = it.hasLocalChanges,
                )
            },
        )
    }

    suspend fun startUpdate(): Boolean =
        updateManager?.startUpdate() ?: false
}

// ============================================================================
// 2. UniRepository: The Single Contact Point for All ViewModels
// ============================================================================

class UniRepository internal constructor(
    val engine: UniCacheEngine,
    sessionController: UniSessionController? = null,
    simulations: GradeSimulationRepository? = null,
    updateManager: UniUpdateManager? = null,
    projectManager: UniProjectManager? = null,
) : UniGateway by engine {
    val generation: Long get() = engine.generation
    val session: UniSessionDomain? = sessionController?.let(::UniSessionDomain)
    val didactics: UniDidacticsDomain = UniDidacticsDomain(engine, simulations)
    val transport: UniTransportDomain = UniTransportDomain(engine)
    val library: UniLibraryDomain = UniLibraryDomain(engine)
    val services: UniServicesDomain = UniServicesDomain(engine)
    val system: UniSystemDomain = UniSystemDomain(updateManager, projectManager)

    suspend fun maintainFreshData(): Nothing = engine.maintainFreshData()
    suspend fun close() = engine.close()
}

internal fun UniGateway.asUniRepository(
    scope: CoroutineScope,
    sessionController: UniSessionController? = null,
    simulations: GradeSimulationRepository? = null,
    updateManager: UniUpdateManager? = null,
    projectManager: UniProjectManager? = null,
): UniRepository =
    this as? UniRepository ?: UniRepository(
        engine = this.sharedEngine(scope),
        sessionController = sessionController,
        simulations = simulations,
        updateManager = updateManager,
        projectManager = projectManager,
    )

// ============================================================================
// 3. UniDataSnapshot -> UniAccountContract Projections
// ============================================================================

internal fun UniDataSnapshot.identityContract(
    account: UniAccountSummary? = null,
    photoData: ByteArray? = null,
    requireStudent: Boolean = false,
): UniIdentityContract =
    buildIdentityContract(
        account = account,
        student = if (requireStudent) require(UniDatasets.Student) else value(UniDatasets.Student),
        photoData = photoData,
    )

internal fun UniDataSnapshot.academicContract(
    requireCareer: Boolean = false,
    requireStudyPlan: Boolean = false,
    defaultTargetCfu: Int = 180,
): UniAcademicContract =
    buildAcademicContract(
        career = if (requireCareer) require(UniDatasets.Career) else value(UniDatasets.Career),
        studyPlan = if (requireStudyPlan) require(UniDatasets.StudyPlan) else value(UniDatasets.StudyPlan),
        examRounds = value(UniDatasets.Exams).orEmpty(),
        defaultTargetCfu = defaultTargetCfu,
    )

internal fun UniDataSnapshot.courseDetailContract(
    courseId: String,
): ResolvedCourseDetail =
    resolveCourseDetailContract(
        courseId = courseId,
        syllabus = require(UniDatasets.syllabus(courseId)),
        studyPlan = value(UniDatasets.StudyPlan),
        career = value(UniDatasets.Career),
        contacts = value(UniDatasets.Contacts)?.toContacts(),
    )

internal fun UniDataSnapshot.examsContract(
    today: LocalDate? = null,
    requireRounds: Boolean = false,
    requireCareer: Boolean = false,
): UniExamsContract =
    buildExamsContract(
        rounds = if (requireRounds) require(UniDatasets.Exams) else value(UniDatasets.Exams).orEmpty(),
        careerExams = (if (requireCareer) require(UniDatasets.Career) else value(UniDatasets.Career))?.exams.orEmpty(),
        today = today,
    )

internal fun UniDataSnapshot.professorContract(
    requireDashboard: Boolean = true,
): UniProfessorContract =
    (if (requireDashboard) require(UniDatasets.Professor) else value(UniDatasets.Professor))
        ?.let(::buildProfessorContract)
        ?: UniProfessorContract()

internal fun UniDataSnapshot.taxesContract(
    requireTaxes: Boolean = true,
): UniTaxesContract =
    (if (requireTaxes) require(UniDatasets.Taxes) else value(UniDatasets.Taxes))
        ?.let(::buildTaxesContract)
        ?: UniTaxesContract()

internal fun UniDataSnapshot.surveysContract(
    requireSurveys: Boolean = true,
): UniSurveysContract =
    (if (requireSurveys) require(UniDatasets.Surveys) else value(UniDatasets.Surveys))
        ?.let(::buildSurveysContract)
        ?: UniSurveysContract()

internal fun UniDataSnapshot.attendanceContract(
    requireAttendance: Boolean = true,
): UniAttendanceContract =
    (if (requireAttendance) require(UniDatasets.Attendance) else value(UniDatasets.Attendance))
        ?.let(::buildAttendanceContract)
        ?: UniAttendanceContract()

internal fun UniDataSnapshot.transportContract(
    requireTransport: Boolean = true,
): UniTransportContract =
    (if (requireTransport) require(UniDatasets.Transport) else value(UniDatasets.Transport))
        ?.let(::buildTransportContract)
        ?: UniTransportContract()

internal fun UniDataSnapshot.devicesContract(): UniDevicesContract {
    val state = state(UniDatasets.Devices)
    return buildDevicesContract(
        devices = state.value.orEmpty(),
        hasError = state.error != null,
    )
}

internal fun UniDataSnapshot.accountContract(
    account: UniAccountSummary? = null,
    generation: Long = 0L,
    today: LocalDate? = null,
    photoData: ByteArray? = null,
    newsFeed: NewsFeed = NewsFeed(),
    defaultTargetCfu: Int = 180,
): UniAccountContract =
    UniAccountContract(
        owner = UniContractOwner(
            accountId = account?.accountId.orEmpty(),
            profileId = account?.activeProfileId,
            role = account?.activeProfileType ?: UniProfileRole.STUDENT,
            generation = generation,
            isGuest = account?.isGuest == true,
        ),
        identity = identityContract(account = account, photoData = photoData),
        academic = academicContract(defaultTargetCfu = defaultTargetCfu),
        exams = examsContract(today = today),
        professor = professorContract(requireDashboard = false),
        taxes = taxesContract(requireTaxes = false),
        surveys = surveysContract(requireSurveys = false),
        attendance = attendanceContract(requireAttendance = false),
        transport = transportContract(requireTransport = false),
        devices = devicesContract(),
        campus = UniCampusContract(
            news = newsFeed,
            contacts = value(UniDatasets.Contacts)?.toContacts().orEmpty(),
        ),
    )

// ============================================================================
// 4. Session Login Profile Mapping
// ============================================================================

internal fun CareerType.toUniProfileRole(): UniProfileRole =
    when (this) {
        CareerType.STUDENT -> UniProfileRole.STUDENT
        CareerType.PROFESSOR -> UniProfileRole.PROFESSOR
    }

internal fun UniProfileRole.toSdkCareerType(): CareerType =
    when (this) {
        UniProfileRole.STUDENT -> CareerType.STUDENT
        UniProfileRole.PROFESSOR -> CareerType.PROFESSOR
    }

internal fun LoginCareerOption.toUniLoginProfileOption(): UniLoginProfileOption =
    UniLoginProfileOption(
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
        type = type.toUniProfileRole(),
    )

internal fun UniLoginProfileOption.toSdkLoginCareerOption(): LoginCareerOption =
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
        type = type.toSdkCareerType(),
    )

internal fun UniAccountProfileSummary.toSdkLoginCareerOption(): LoginCareerOption =
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
        type = type.toSdkCareerType(),
    )

internal fun AppUpdateInfo.toUniAppUpdateManifest(): UniAppUpdateManifest =
    UniAppUpdateManifest(
        latestVersion = latestVersion,
        latestVersionCode = latestVersionCode,
        isUpdateAvailable = isUpdateAvailable,
        isMandatory = isMandatory,
        downloadUrl = downloadUrl,
        notes = notes,
        publishedAt = publishedAt,
        channel = channel,
    )
