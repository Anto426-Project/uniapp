package com.anto426.uniapp.data

import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.uniapp.account.data.AccountAvatar
import com.anto426.uniapp.account.data.AccountAvatarStore
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountProfileSummary
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniLoginProfileOption
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.library.model.LibraryBookDetail
import com.anto426.uniapp.library.model.LibraryBookLoanDraft
import com.anto426.uniapp.library.model.LibraryBookingOption
import com.anto426.uniapp.library.model.LibraryBookingOverview
import com.anto426.uniapp.library.model.LibraryCatalogPage
import com.anto426.uniapp.library.model.LibraryLoanSubmissionResult
import com.anto426.uniapp.library.model.LibraryMovementItem
import com.anto426.uniapp.library.model.LibrarySpaceBookingOutcome
import com.anto426.uniapp.library.model.LibrarySpaceCalendar
import com.anto426.uniapp.library.model.LibrarySpaceSelection
import com.anto426.uniapp.model.didactics.AcademicCommissionMember
import com.anto426.uniapp.model.didactics.AcademicContentItem
import com.anto426.uniapp.model.didactics.AcademicDashboardData
import com.anto426.uniapp.model.didactics.AcademicDetailField
import com.anto426.uniapp.model.didactics.AcademicExamBookingEntry
import com.anto426.uniapp.model.didactics.AttendanceData
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.ExamRecord
import com.anto426.uniapp.model.didactics.ExamSession
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.PastExam
import com.anto426.uniapp.model.didactics.PastExamStatus
import com.anto426.uniapp.model.didactics.QuestionnaireAnswerOption
import com.anto426.uniapp.model.didactics.QuestionnaireData
import com.anto426.uniapp.model.didactics.QuestionnaireForm
import com.anto426.uniapp.model.didactics.QuestionnairePage
import com.anto426.uniapp.model.didactics.QuestionnaireQuestion
import com.anto426.uniapp.model.didactics.QuestionnaireStatus
import com.anto426.uniapp.model.didactics.SimulationItem
import com.anto426.uniapp.model.didactics.SingleAttendanceEntry
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.model.didactics.StudyYear
import com.anto426.uniapp.model.didactics.ThesisData
import com.anto426.uniapp.model.news.NewsFeed
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.model.services.ContactCategory
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.model.services.TaxPaymentData
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.model.settings.DeviceType
import com.anto426.uniapp.model.transport.TransportOverviewData
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TransportRouteOption
import com.anto426.uniapp.model.transport.TransportRouteStops
import com.anto426.uniapp.model.transport.TripBookingDirection
import com.anto426.uniapp.model.transport.TripBookingOutcome
import com.anto426.uniapp.model.transport.TripBookingRequest
import com.anto426.uniapp.model.transport.TripDirection
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.UniAppModuleBuildInfo
import com.anto426.uniapp.updates.model.UniAppUpdateManifest
import com.anto426.unisdk.model.AppUpdateInfo
import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.CareerExam
import com.anto426.unisdk.model.CareerType
import com.anto426.unisdk.model.ConnectedDevice
import com.anto426.unisdk.model.ConnectedDeviceKind
import com.anto426.unisdk.model.CourseSyllabus
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ExamRoundBookingState
import com.anto426.unisdk.model.HistoricalExamStatus
import com.anto426.unisdk.model.LoginCareerOption
import com.anto426.unisdk.model.ProfessorContentItem
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.ProfessorDetailField
import com.anto426.unisdk.model.ResolvedStudyCourse
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.StudyPlanCourse
import com.anto426.unisdk.model.SurveyAnswerSubmission
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.SurveyPageSubmission
import com.anto426.unisdk.model.SurveySaveRequest
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityContactCategory
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.model.buildHistoricalExamRounds
import com.anto426.unisdk.model.filterUpcomingExamRounds
import com.anto426.unisdk.model.groupByCourse
import com.anto426.unisdk.model.isValidAcademicExamGrade
import com.anto426.unisdk.model.numericExamGradeOrNull
import com.anto426.unisdk.model.parseProfessorDetailFields
import com.anto426.unisdk.model.resolveStudyCourse
import com.anto426.unisdk.model.toNormalizedDisplayExamGrade
import com.anto426.unisdk.platform.AppInfoProvider
import com.anto426.unisdk.platform.ProjectInfo
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRoute
import com.anto426.unisdk.transport.displayFor
import com.anto426.unisdk.transport.parseTransportTravelDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import com.anto426.uniapp.localization.readAppString
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

    suspend fun prepareNews(news: List<com.anto426.unisdk.model.UniversityNews>, dispatcher: CoroutineDispatcher = Dispatchers.Default): NewsFeed =
        engine.prepareNews(news, dispatcher)

    suspend fun submitQuestionnaire(
        courseId: String,
        form: QuestionnaireForm,
        selectedAnswers: Map<String, Set<String>>,
        freeTextAnswers: Map<String, String>,
    ): String {
        val details = engine.loadStudentDetails()
        val studentId = details.primaryStudentId
            ?: error(readAppString(Res.string.msg_identificativo_studente_non_disponibile))
        val userCompId = form.userCompId?.takeIf(String::isNotBlank)
            ?: error(readAppString(Res.string.msg_identificativo_compilazione_non_disponibile))
        val questCompId = form.questCompId?.takeIf(String::isNotBlank)
            ?: error(readAppString(Res.string.msg_identificativo_questionario_non_disponibile))
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
            ?: error(readAppString(Res.string.msg_operazione_sull_appello_non_riuscita))
        if (!round.booked && !round.isBookable) {
            error(readAppString(Res.string.ui_exam_not_yet_bookable))
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

    suspend fun prepareNews(news: List<com.anto426.unisdk.model.UniversityNews>, dispatcher: CoroutineDispatcher = Dispatchers.Default): NewsFeed =
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
// 3. Domain Projections (Upstream-Normalized in uni-sdk)
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

internal fun Career.toExamRecords(studyPlan: StudyPlan? = null): List<ExamRecord> =
    resolveExamsWithYears(studyPlan).map { resolved ->
        ExamRecord(
            name = resolved.name,
            grade = resolved.grade,
            cfu = resolved.cfuLabel,
            date = resolved.date,
            year = resolved.year,
            code = resolved.code,
            lode = resolved.isLode,
        )
    }

internal fun Career.toGradeExams(): List<GradeExam> =
    gradedExams.mapNotNull { exam ->
        val grade = exam.numericGrade ?: return@mapNotNull null
        GradeExam(exam.name, grade, exam.cfu ?: 0)
    }

internal fun List<ExamRound>.toExamSessions(): List<ExamSession> =
    map { round ->
        ExamSession(
            name = round.courseName,
            date = round.examDateLabel,
            time = round.examTimeLabel,
            room = round.room,
            bookingOpenDate = round.registrationStartLabel,
            bookingCloseDate = round.registrationEndLabel,
            type = round.registrationTypeDescription,
            professor = round.presidentFullName.orEmpty(),
            bookedUsersCount = round.totalRegistrations ?: 0,
            availableSlots = round.availableSlots,
            notes = round.notes.orEmpty(),
            canBook = round.isBookable,
            isBooked = round.booked,
            id = round.stableId,
            isBookingFuture = round.bookingState == ExamRoundBookingState.FUTURE,
            calendarUrl = round.calendarUrlOrNull(),
        )
    }

internal fun buildExamHistory(
    careerExams: List<CareerExam>,
    rounds: List<ExamRound>,
    today: LocalDate,
): List<PastExam> =
    buildHistoricalExamRounds(careerExams, rounds, today).map { item ->
        PastExam(
            id = item.id,
            name = item.name,
            date = item.date,
            time = item.time,
            status = when (item.status) {
                HistoricalExamStatus.BOOKED_PAST -> PastExamStatus.BOOKED_PAST
                HistoricalExamStatus.VERBALIZED -> PastExamStatus.VERBALIZED
            },
            grade = item.grade.orEmpty(),
            cfu = item.cfu,
            room = item.room,
            type = item.type,
            professor = item.professor,
            bookingOpenDate = item.bookingOpenDate,
            bookingCloseDate = item.bookingCloseDate,
            bookedUsersCount = item.bookedUsersCount,
            availableSlots = item.availableSlots,
            notes = item.notes,
            code = item.code,
        )
    }

internal fun StudyPlan.toStudyYears(career: Career? = null): List<StudyYear> =
    resolveWithCareer(career).map { year ->
        StudyYear(
            yearNumber = year.yearNumber,
            yearName = year.yearTitle,
            courses = year.courses.map { it.toUiStudyCourse(plannedStatus = CourseStatus.PLANNED) },
        )
    }

internal fun CourseSyllabus.toStudyCourse(
    studyPlanCourse: StudyPlanCourse? = null,
    careerExam: CareerExam? = null,
): StudyCourse =
    resolveStudyCourse(studyPlanCourse, careerExam).toUiStudyCourse(plannedStatus = CourseStatus.ACTIVE)

private fun ResolvedStudyCourse.toUiStudyCourse(plannedStatus: CourseStatus): StudyCourse =
    StudyCourse(
        id = id,
        name = name,
        cfu = cfuLabel,
        status = if (completed) CourseStatus.COMPLETED else plannedStatus,
        professor = professor,
        description = description.ifBlank { category },
        semester = semester.ifBlank { completionDate },
        grade = grade,
        examDate = examDate,
        year = year,
        ssd = ssd,
        taf = taf.ifBlank { category },
        lingua = lingua,
        obiettivi = obiettivi,
        contenuti = contenuti,
        testiRiferimento = testiRiferimento,
        metodiDidattici = metodiDidattici,
        modalitaVerifica = modalitaVerifica,
        prerequisiti = prerequisiti,
    )

internal fun ProfessorContentItem.toAcademicContentItem(): AcademicContentItem =
    AcademicContentItem(
        id = id,
        title = title,
        subtitle = subtitle,
        detail = detail,
        code = code,
        date = date,
        fields = resolvedFields().map { AcademicDetailField(it.label, it.value) },
        bookings = bookings.map { AcademicExamBookingEntry(it.studentName, it.matricola, it.grade) },
        commission = commission.map { AcademicCommissionMember(it.displayName, it.role) },
    )

internal fun ProfessorDashboard.toAcademicDashboardData(): AcademicDashboardData =
    AcademicDashboardData(
        teachings = teachings.map(ProfessorContentItem::toAcademicContentItem),
        examRounds = examRounds.map(ProfessorContentItem::toAcademicContentItem),
        theses = theses.map(ProfessorContentItem::toAcademicContentItem),
        reports = reports.map(ProfessorContentItem::toAcademicContentItem),
        unavailableSections = unavailableSections.map { it.name },
    )

internal fun extractThesisData(item: AcademicContentItem): ThesisData {
    val sdkDetails = ProfessorContentItem(
        title = item.title,
        subtitle = item.subtitle,
        detail = item.detail,
        id = item.id,
        code = item.code,
        date = item.date,
        fields = item.fields.map { ProfessorDetailField(it.label, it.value) },
    ).extractThesisDetails()

    return ThesisData(
        candidateName = sdkDetails.candidateName,
        thesisTitle = sdkDetails.thesisTitle,
        relatore = sdkDetails.relatore,
        correlatore = sdkDetails.correlatore,
        matricola = sdkDetails.matricola,
        cds = sdkDetails.cds,
        date = sdkDetails.date,
        sede = sdkDetails.sede,
        voto = sdkDetails.voto,
        sessione = sdkDetails.sessione,
        extraFields = sdkDetails.extraFields.map { it.label to it.value },
    )
}

internal suspend fun AcademicContentItem.orderedAcademicDetailFields(
    section: AcademicSection,
): List<Pair<String, String>> {
    val sdkOrdered = ProfessorContentItem(
        title = title,
        subtitle = subtitle,
        detail = detail,
        id = id,
        code = code,
        date = date,
        fields = fields.map { ProfessorDetailField(it.label, it.value) },
    ).orderedDetailFields(isThesis = section == AcademicSection.Theses)
    if (section != AcademicSection.Theses) return sdkOrdered.map { it.label to it.value }
    return sdkOrdered.map { field ->
        val normalizedLabel = when (field.label.lowercase()) {
            "discussione", "data tesi" -> readAppString(Res.string.ui_thesis_discussion_date)
            "titolo tesi" -> readAppString(Res.string.ui_thesis_title)
            "cds" -> readAppString(Res.string.msg_corso_di_laurea_2)
            else -> field.label
        }
        normalizedLabel to field.value
    }
}

internal fun List<SurveyCourse>.toQuestionnaireDataList(): List<QuestionnaireData> =
    map { course ->
        QuestionnaireData(
            course = course.title,
            prof = course.professor.orEmpty(),
            code = course.adCod.orEmpty(),
            status = when {
                course.completed -> QuestionnaireStatus.COMPLETED
                course.enabled -> QuestionnaireStatus.PENDING
                else -> QuestionnaireStatus.UNAVAILABLE
            },
            courseId = course.courseId,
            tagList = course.tagList,
        )
    }

internal fun SurveyForm.toQuestionnaireForm(): QuestionnaireForm =
    QuestionnaireForm(
        userCompId = userCompId,
        questCompId = questCompId,
        pages = pages.map { page ->
            QuestionnairePage(
                pageId = page.pageId,
                questions = page.questions.map { q ->
                    QuestionnaireQuestion(
                        questionId = q.questionId,
                        questionText = q.questionText,
                        answers = q.answers.map { a ->
                            QuestionnaireAnswerOption(
                                answerId = a.answerId,
                                answerText = a.answerText,
                                freeText = a.freeText,
                            )
                        },
                        required = q.required,
                        multipleChoice = q.multipleChoice,
                        freeTextEnabled = q.freeTextEnabled,
                        pageId = q.pageId,
                    )
                },
            )
        },
    )

internal fun buildSurveySaveRequest(
    studentId: String,
    userCompId: String,
    questCompId: String,
    form: QuestionnaireForm,
    selectedAnswers: Map<String, Set<String>>,
    freeTextAnswers: Map<String, String>,
): SurveySaveRequest =
    SurveySaveRequest(
        studentId = studentId,
        userCompId = userCompId,
        questCompId = questCompId,
        surveyPageDtos = form.pages.map { page ->
            SurveyPageSubmission(
                pageId = page.pageId,
                ansBodyDto = page.questions.flatMap { question ->
                    val selected = selectedAnswers[question.questionId].orEmpty()
                    selected.map { answerId ->
                        val isFreeText = question.answers.any { it.answerId == answerId && it.freeText }
                        SurveyAnswerSubmission(
                            questionId = question.questionId,
                            answerId = answerId,
                            answerBody = if (isFreeText) {
                                freeTextAnswers[question.questionId].orEmpty().trim()
                            } else {
                                ""
                            },
                        )
                    }
                },
            )
        },
    )

internal fun Taxes.toTaxPayments(): List<TaxPaymentData> =
    installments.map { installment ->
        TaxPaymentData(
            title = installment.title,
            date = installment.deadline,
            amount = installment.amount,
            isPaid = installment.paid,
            iuv = "",
        )
    }

internal fun List<UniversityContact>.toContacts(): List<ContactData> =
    map { contact ->
        ContactData(
            name = contact.cleanName,
            role = contact.cleanRole,
            initials = contact.initials,
            email = contact.cleanEmail,
            phone = contact.primaryPhone,
            city = contact.cleanCity,
            category = when (contact.contactCategory) {
                UniversityContactCategory.SECRETARIAT -> ContactCategory.SECRETARIAT
                UniversityContactCategory.SERVICES -> ContactCategory.SERVICES
                UniversityContactCategory.TEACHERS -> ContactCategory.TEACHERS
            },
            department = contact.cleanDepartment,
            office = contact.officeLabel,
            officeHours = contact.cleanOfficeHours,
            id = contact.cleanId,
            firstName = contact.cleanFirstName,
            lastName = contact.cleanLastName,
            phones = contact.sortedPhones,
            address = contact.cleanAddress,
            building = contact.cleanBuilding,
        )
    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

internal fun List<AttendanceRecord>.toAttendanceData(): List<AttendanceData> =
    groupByCourse().map { summary ->
        AttendanceData(
            course = summary.courseName,
            percentage = summary.percentageLabel,
            count = summary.countLabel,
            attendedHours = summary.attendedHours,
            totalHours = summary.totalHours,
            records = summary.records.map { rec ->
                SingleAttendanceEntry(
                    id = rec.id.orEmpty(),
                    date = rec.occurredAt.orEmpty(),
                    time = rec.timeRangeLabel,
                    room = rec.room.orEmpty(),
                    teacher = rec.teacherName.orEmpty(),
                    hours = rec.durationHours,
                    status = rec.status.orEmpty(),
                )
            },
        )
    }

internal fun List<UniversityNews>.toNewsItems(): List<NewsItem> {
    val occurrences = mutableMapOf<String, Int>()
    return map { news ->
        val category = news.cleanCategory
        val publishedAt = news.cleanPublishedAt
        val rawId = news.id?.trim()
        val identityParts = if (rawId.isNullOrBlank()) listOf(category.lowercase(), publishedAt, news.title)
        else listOf(category.lowercase(), rawId)
        val identity = identityParts.joinToString("|") { "${it.length}:$it" }
        val occurrence = occurrences.getOrElse(identity) { 0 }
        occurrences[identity] = occurrence + 1

        val formatted = news.formatArticle()
        val rawSummary = news.summary.orEmpty()
        val rawBody = news.content.orEmpty()
        val item = NewsItem(
            title = news.cleanTitle,
            description = formatted.summaryText,
            fullContent = rawBody.ifBlank { rawSummary },
            type = news.category.toNewsType(),
            category = category,
            publishedAt = publishedAt,
            key = "${datasetCacheKey("news", identity)}#$occurrence",
            sourceUrl = news.resolvedSourceUrl,
        )
        item to news.sortTimestamp
    }.sortedWith(compareByDescending<Pair<NewsItem, Long>> { it.second }.thenBy { it.first.title })
        .map { it.first }
}

internal fun List<ConnectedDevice>.toDeviceInfo(): List<DeviceInfo> =
    map { device ->
        DeviceInfo(
            name = device.displayName,
            location = device.locationLabel,
            lastSeen = device.lastLogin,
            appVersion = device.appVersion,
            type = when (device.deviceKind) {
                ConnectedDeviceKind.PC -> DeviceType.PC
                ConnectedDeviceKind.TABLET -> DeviceType.TABLET
                ConnectedDeviceKind.PHONE -> DeviceType.PHONE
            },
            isCurrent = device.isCurrentDevice,
            id = device.stableId,
            revocationToken = device.token,
        )
    }

internal fun TripBookingDirection.toSdkDirection(): TransportDirection =
    when (this) {
        TripBookingDirection.OUTBOUND -> TransportDirection.OUTBOUND
        TripBookingDirection.RETURN -> TransportDirection.RETURN
        TripBookingDirection.ROUND_TRIP -> TransportDirection.ROUND_TRIP
    }

internal fun TransportDirection.toTripBookingDirection(): TripBookingDirection =
    when (this) {
        TransportDirection.OUTBOUND -> TripBookingDirection.OUTBOUND
        TransportDirection.RETURN -> TripBookingDirection.RETURN
        TransportDirection.ROUND_TRIP -> TripBookingDirection.ROUND_TRIP
    }

internal fun TransportActionResult.toTripBookingOutcome(): TripBookingOutcome =
    when (this) {
        TransportActionResult.Completed -> TripBookingOutcome.COMPLETED
        TransportActionResult.AlreadyExists -> TripBookingOutcome.ALREADY_EXISTS
    }

internal fun TripBookingRequest.toSdkRequest(): TransportBookingRequest =
    TransportBookingRequest(
        routeCode = routeCode,
        date = date,
        direction = direction.toSdkDirection(),
    )

internal fun TransportBookingRequest.toTripBookingRequest(): TripBookingRequest =
    TripBookingRequest(
        routeCode = routeCode,
        date = date,
        direction = direction.toTripBookingDirection(),
    )

internal fun TransportRoute.toTransportRouteOption(): TransportRouteOption {
    val out = displayFor(TransportDirection.OUTBOUND)
    val ret = displayFor(TransportDirection.RETURN)
    return TransportRouteOption(
        code = code,
        label = label,
        outboundStops = TransportRouteStops(out.label, out.departureStop, out.arrivalStop),
        returnStops = TransportRouteStops(ret.label, ret.departureStop, ret.arrivalStop),
    )
}

internal fun resolveTransportRouteStops(
    routeLabel: String,
    direction: TripBookingDirection,
): TransportRouteStops {
    val sdkDisplay = TransportRoute(code = "", label = routeLabel).displayFor(direction.toSdkDirection())
    return TransportRouteStops(
        label = sdkDisplay.label,
        departureStop = sdkDisplay.departureStop,
        arrivalStop = sdkDisplay.arrivalStop,
    )
}

internal fun TransportData.toReservations(): List<TransportReservation> =
    resolveBookings().map { booking ->
        val travel = parseTransportTravelDate(booking.date)
        TransportReservation(
            id = booking.id,
            route = booking.routeLabel,
            date = travel?.displayDate.orEmpty(),
            time = travel?.time.orEmpty(),
            direction = if (booking.isReturn) TripDirection.RITORNO else TripDirection.ANDATA,
            qrCodeData = "",
            departureStop = booking.departureStop,
            arrivalStop = booking.arrivalStop,
            busNumber = "",
            ticketUrl = booking.ticketUrl,
            ticketNumber = booking.number,
        )
    }

internal fun bookedTransportRequests(bookings: List<TransportBooking>): Set<TripBookingRequest> =
    com.anto426.unisdk.transport.bookedTransportRequests(bookings)
        .map(TransportBookingRequest::toTripBookingRequest)
        .toSet()

internal fun TransportData.toTransportOverview(): TransportOverviewData =
    TransportOverviewData(
        reservations = toReservations(),
        routes = availableRoutes.map(TransportRoute::toTransportRouteOption),
        bookedRequests = bookedRequests.map(TransportBookingRequest::toTripBookingRequest).toSet(),
        totalCount = totalCount,
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

internal fun ExamRound.stableUiId(): String = stableId

internal fun String.numericGradeOrNull(): Int? = numericExamGradeOrNull()

internal fun String.isValidExamGrade(): Boolean = isValidAcademicExamGrade()

internal fun String.toDisplayExamGrade(): String = toNormalizedDisplayExamGrade()

private fun String?.toNewsType(): LiquidStatusType =
    when {
        this?.contains("scad", ignoreCase = true) == true -> LiquidStatusType.Warning
        this?.contains("success", ignoreCase = true) == true -> LiquidStatusType.Success
        else -> LiquidStatusType.Info
    }
