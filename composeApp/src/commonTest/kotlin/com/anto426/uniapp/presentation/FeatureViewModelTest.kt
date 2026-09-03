package com.anto426.uniapp.presentation

import com.anto426.uniapp.account.model.UniAccountProfileSummary
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastMessage
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.didactics.presentation.QuestionnairesViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnaireDetailViewModel
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardViewModel
import com.anto426.uniapp.didactics.presentation.ExamsViewModel
import com.anto426.uniapp.didactics.presentation.buildExamHistory
import com.anto426.uniapp.didactics.presentation.StudyPlanViewModel
import com.anto426.uniapp.didactics.presentation.TranscriptsViewModel
import com.anto426.uniapp.didactics.presentation.StatisticsViewModel
import com.anto426.uniapp.didactics.presentation.AcademicIdentityViewModel
import com.anto426.uniapp.didactics.presentation.AcademicItemDetailViewModel
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.didactics.presentation.academicItemKey
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.model.didactics.PastExamStatus
import com.anto426.uniapp.settings.presentation.DeviceSessionsActionViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.unisdk.backend.model.CareerData
import com.anto426.unisdk.backend.model.BackendCareerType
import com.anto426.unisdk.backend.model.CareerExamData
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.SurveyCourseData
import com.anto426.unisdk.backend.model.StudyPlanCourseData
import com.anto426.unisdk.backend.model.StudyPlanData
import com.anto426.unisdk.backend.model.StudentDetailsData
import com.anto426.unisdk.backend.model.ProfessorContentItem
import com.anto426.unisdk.backend.model.ProfessorDashboardData
import com.anto426.unisdk.backend.model.SurveyAnswerOptionData
import com.anto426.unisdk.backend.model.SurveyFirstPageData
import com.anto426.unisdk.backend.model.SurveyPageData
import com.anto426.unisdk.backend.model.SurveyQuestionData
import com.anto426.unisdk.backend.model.SurveySaveRequest
import com.anto426.unisdk.backend.model.TaxInstallmentData
import com.anto426.unisdk.backend.model.TaxesData
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FeatureViewModelTest {
    @Test
    fun taxesArePartitionedFromRepositoryData() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean) = TaxesData(
                dueAmount = "100",
                installments = listOf(
                    TaxInstallmentData("Da pagare", "100", "domani", false),
                    TaxInstallmentData("Pagata", "20", "ieri", true),
                ),
            )
        }
        val viewModel = TaxesViewModel(source)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.pendingPayments.size)
        assertEquals(1, viewModel.uiState.value.paidPayments.size)
    }

    @Test
    fun didacticsDashboardUsesRemoteValuesWithoutJoiningCfuNumbers() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetailsData(
                    fullName = "Student",
                    matricola = "165432",
                    degreeName = "Ingegneria Informatica",
                    departmentName = "Dipartimento di Ingegneria",
                )

            override suspend fun loadCareer(forceRefresh: Boolean) =
                CareerData(
                    average = "28.2",
                    cfu = "120 / 180",
                    cfuTarget = 180,
                    year = "3° anno",
                    status = "Attiva",
                    exams = List(14) { index ->
                        CareerExamData("Esame $index", "28", "01/01/2026", 6)
                    },
                )

            override suspend fun loadStudyPlan(forceRefresh: Boolean) =
                StudyPlanData(
                    List(20) { index ->
                        StudyPlanCourseData(
                            title = "Attività $index",
                            cfu = 9,
                            year = (index / 7 + 1).coerceAtMost(3),
                            completed = index < 14,
                        )
                    },
                )

            override suspend fun loadExamRounds(forceRefresh: Boolean) =
                listOf(ExamRoundData("Analisi", "01/09/2026 09:00", "Aula 1", 20, open = true))

            override suspend fun loadSurveyCourses(forceRefresh: Boolean) =
                listOf(SurveyCourseData("1", "Analisi", "tag", completed = false))
        }
        val viewModel = DidacticsDashboardViewModel(source)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(120, state.acquiredCfu)
        assertEquals(180, state.targetCfu)
        assertEquals(14, state.completedExams)
        assertEquals(20, state.plannedActivities)
        assertEquals(3, state.currentYear)
        assertEquals(1, state.openExamRounds)
        assertEquals(1, state.pendingQuestionnaires)
    }

    @Test
    fun disconnectAllSessionsPublishesFeedbackAndRefreshSignal() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun disconnectAllOtherDevices() = "Altre sessioni disconnesse"
        }
        val messages = mutableListOf<AppToastMessage>()
        val viewModel = DeviceSessionsActionViewModel(source, AppToastSink(messages::add))

        viewModel.requestDisconnectAll()
        assertTrue(viewModel.uiState.value.isConfirmationVisible)
        viewModel.confirmDisconnectAll()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.refreshRevision)
        assertEquals(false, viewModel.uiState.value.isConfirmationVisible)
        assertEquals("Altre sessioni disconnesse", messages.single().text)
    }

    @Test
    fun questionnaireProgressUsesRemoteCourseStatuses() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadSurveyCourses(forceRefresh: Boolean) = listOf(
                SurveyCourseData("1", "Corso A", "tag-a", "A", completed = true),
                SurveyCourseData("2", "Corso B", "tag-b", "B", completed = false),
            )
        }
        val viewModel = QuestionnairesViewModel(source)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalCount)
        assertEquals(.5f, viewModel.uiState.value.completedProgress)
    }

    @Test
    fun studyPlanKeepsYearsProvidedByTheAcademicBackend() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudyPlan(forceRefresh: Boolean) =
                StudyPlanData(
                    listOf(
                        StudyPlanCourseData("Analisi", cfu = 9, year = 1, completed = true),
                        StudyPlanCourseData("Tirocinio", cfu = 6, year = 4),
                    ),
                )
        }
        val viewModel = StudyPlanViewModel(source)
        advanceUntilIdle()

        assertEquals(listOf(1, 4), viewModel.uiState.value.years.map { it.yearNumber })
        assertEquals(1, viewModel.uiState.value.displayedYears.single().yearNumber)
        viewModel.selectYear(1)
        assertEquals(4, viewModel.uiState.value.displayedYears.single().yearNumber)
    }

    @Test
    fun transcriptsSelectsYearDirectlyWithoutAllOption() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadCareer(forceRefresh: Boolean) =
                CareerData(
                    average = "28.0",
                    degreeBase = "102.0",
                    cfu = "18",
                    cfuTarget = 180,
                    year = "2",
                    status = "Attivo",
                    exams = listOf(
                        CareerExamData("Analisi", "30", "01/02/2025", 9, "A"),
                        CareerExamData("Tirocinio", "IDONEO", "15/05/2025", 6, "T"),
                        CareerExamData("Programmazione", "30 e lode", "20/06/2025", 9, "P"),
                        CareerExamData("Seminario", "", "20/06/2025", 3, "S"),
                        CareerExamData("Fisica", "28", "01/02/2026", 9, "B"),
                    ),
                )
        }
        val viewModel = TranscriptsViewModel(source)
        advanceUntilIdle()

        val allExams = viewModel.uiState.value.examsByYear.values.flatten()
        assertEquals(listOf("Analisi", "Tirocinio", "Programmazione", "Fisica"), allExams.map { it.name })
        assertEquals(true, allExams.first { it.name == "Programmazione" }.lode)
        assertEquals("IDONEO", allExams.first { it.name == "Tirocinio" }.grade)
        assertEquals(1, viewModel.uiState.value.selectedYear)
        assertEquals(listOf(1), viewModel.uiState.value.displayedYears)
        viewModel.selectYear(2)
        assertEquals(2, viewModel.uiState.value.selectedYear)
        assertEquals(listOf(2), viewModel.uiState.value.displayedYears)
    }

    @Test
    fun questionnaireSubmissionUsesSelectedRemoteAnswerIds() = runViewModelTest {
        var savedRequest: SurveySaveRequest? = null
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetailsData(fullName = "Student", stuId = "student-1")

            override suspend fun loadSurveyFirstPage(courseId: String, tagList: String) =
                SurveyFirstPageData(
                    userCompId = "user-comp",
                    questCompId = "quest-comp",
                    pages = listOf(
                        SurveyPageData(
                            pageId = "page-1",
                            questions = listOf(
                                SurveyQuestionData(
                                    questionId = "question-1",
                                    questionText = "Valutazione",
                                    answers = listOf(SurveyAnswerOptionData("answer-1", "Ottimo")),
                                ),
                            ),
                        ),
                    ),
                )

            override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String {
                savedRequest = request
                return "Salvato"
            }
        }
        val messages = mutableListOf<AppToastMessage>()
        val viewModel =
            QuestionnaireDetailViewModel(
                "course-1",
                "tag",
                "Corso",
                source,
                AppToastSink(messages::add),
            )
        advanceUntilIdle()
        viewModel.selectAnswer("question-1", "answer-1", multipleChoice = false)
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("answer-1", savedRequest?.surveyPageDtos?.single()?.ansBodyDto?.single()?.answerId)
        assertEquals("Salvato", messages.single().text)
        assertEquals(true, viewModel.uiState.value.submitted)
    }

    @Test
    fun transportTicketsAreGroupedByRemoteDate() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTransportData(forceRefresh: Boolean) = TransportData(
                routeLabel = "Campus",
                bookings = listOf(
                    TransportBooking("1", "A", "R", "2026-09-01", "A", false, "ticket-1"),
                    TransportBooking("2", "B", "R", "2026-09-01", "R", true, "ticket-2"),
                ),
                totalCount = 2,
            )
        }
        val viewModel = TransportViewModel(source)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.days.size)
        assertEquals(2, viewModel.uiState.value.days.single().reservations.size)
    }

    @Test
    fun selectingStatisticsTabPreservesRemoteChartData() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadCareer(forceRefresh: Boolean) = CareerData(
                average = "28.5",
                degreeBase = "104.5",
                cfu = "18",
                cfuTarget = 180,
                year = "1",
                status = "Attivo",
                exams = listOf(
                    CareerExamData("Analisi", "30", "01/02/2026", 9, "A"),
                    CareerExamData("Fisica", "27", "01/03/2026", 9, "B"),
                ),
            )
        }
        val viewModel = StatisticsViewModel(source)
        advanceUntilIdle()
        val initial = viewModel.uiState.value.gradeEntries

        viewModel.selectTab(2)

        assertEquals(2, viewModel.uiState.value.selectedTabIndex)
        assertEquals(initial, viewModel.uiState.value.gradeEntries)
        assertEquals(listOf(9f, 9f), viewModel.uiState.value.cfuEntries.map { it.value })
        assertTrue(viewModel.uiState.value.cfuMax > 9f)
        assertTrue(initial.isNotEmpty())
    }

    @Test
    fun examRoundsAreChronologicalAndPastBookingsLeaveActiveSections() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadExamRounds(forceRefresh: Boolean) =
                listOf(
                    ExamRoundData("Reti", "12/09/2026 09:00", "Aula 3", 10, open = true),
                    ExamRoundData("Analisi", "04/09/2026 11:00", "Aula 1", 0, open = false, booked = true),
                    ExamRoundData("Fisica", "06/09/2026 08:30", "Aula 2", 5, open = false, booked = true),
                    ExamRoundData("Basi di dati", "07/09/2026 15:00", "Lab", 12, open = true),
                )
        }
        val viewModel = ExamsViewModel(source, today = { LocalDate(2026, 9, 5) })
        advanceUntilIdle()

        assertEquals(
            listOf("06/09/2026", "07/09/2026", "12/09/2026"),
            viewModel.uiState.value.exams.map { it.date },
        )
        assertTrue(viewModel.uiState.value.exams.none { it.name == "Analisi" })
        assertEquals(1, viewModel.uiState.value.bookedCount)
    }

    @Test
    fun historyContainsPastBookingsWithEveryAvailableDetail() {
        val history =
            buildExamHistory(
                careerExams = emptyList(),
                rounds =
                    listOf(
                        ExamRoundData(
                            courseName = "Analisi",
                            dateTime = "04/09/2026 11:30",
                            room = "Aula Magna",
                            availableSlots = 7,
                            open = false,
                            booked = true,
                            adsceId = "ADSCE-42",
                            presidentName = "Ada",
                            presidentSurname = "Lovelace",
                            registrationStartingDate = "20/08/2026",
                            registrationEndingDate = "02/09/2026",
                            typeCode = "SO",
                            notes = "Portare un documento",
                            totalRegistrations = 93,
                        ),
                        ExamRoundData(
                            courseName = "Fisica",
                            dateTime = "06/09/2026 09:00",
                            room = "Aula 2",
                            availableSlots = 4,
                            open = false,
                            booked = true,
                        ),
                    ),
                today = LocalDate(2026, 9, 5),
            )

        val exam = history.single()
        assertEquals(PastExamStatus.BOOKED_PAST, exam.status)
        assertEquals("04/09/2026", exam.date)
        assertEquals("11:30", exam.time)
        assertEquals("Aula Magna", exam.room)
        assertEquals("Scritto e Orale", exam.type)
        assertEquals("Ada Lovelace", exam.professor)
        assertEquals("20/08/2026", exam.bookingOpenDate)
        assertEquals("02/09/2026", exam.bookingCloseDate)
        assertEquals(93, exam.bookedUsersCount)
        assertEquals(7, exam.availableSlots)
        assertEquals("Portare un documento", exam.notes)
        assertEquals("ADSCE-42", exam.code)
    }

    @Test
    fun studentPhotoReachesHomeAndBadgePresentationState() = runViewModelTest {
        val expectedPhoto = "https://studenti.example/avatar/profile.jpg"
        val expectedBytes = byteArrayOf(1, 2, 3, 4)
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetailsData(
                    fullName = "Ada Lovelace",
                    matricola = "12345",
                    photoUrl = expectedPhoto,
                )

            override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray {
                assertEquals(expectedPhoto, source)
                return expectedBytes
            }
        }
        val home = HomeDashboardViewModel(source, quickActions = emptyList())
        val badge = AcademicIdentityViewModel(source)
        advanceUntilIdle()

        assertContentEquals(expectedBytes, home.uiState.value.profilePhotoData)
        assertContentEquals(expectedBytes, badge.uiState.value.photoData)
    }

    @Test
    fun professorBadgeUsesTheActiveAccountProfileWithoutLoadingStudentData() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetailsData =
                error("The professor badge must not request student details")
        }
        val account =
            UniAccountSummary(
                accountId = "account-1",
                serverUserId = "m.rossi",
                displayName = "Mario Rossi",
                degreeName = "",
                matricola = null,
                email = null,
                photoUrl = null,
                isGuest = false,
                activeProfileId = "professor-1",
                activeProfileType = BackendCareerType.PROFESSOR,
                profiles =
                    listOf(
                        UniAccountProfileSummary(
                            profileId = "professor-1",
                            displayName = "Mario Rossi",
                            degreeName = "",
                            dipId = "DIP-12",
                            departmentName = "Dipartimento di Ingegneria",
                            teacherId = "DOC-42",
                            type = BackendCareerType.PROFESSOR,
                        ),
                    ),
            )

        val badge = AcademicIdentityViewModel(source, account)
        advanceUntilIdle()

        assertTrue(badge.uiState.value.isProfessor)
        assertEquals("Mario Rossi", badge.uiState.value.fullName)
        assertEquals("m.rossi", badge.uiState.value.username)
        assertEquals("DOC-42", badge.uiState.value.teacherId)
        assertEquals("Dipartimento di Ingegneria", badge.uiState.value.departmentName)
        assertEquals("DIP-12", badge.uiState.value.departmentId)
    }

    @Test
    fun professorHomeUsesTheSharedDashboardWithProfessorMetrics() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetailsData =
                error("Professor Home must not request student details")

            override suspend fun loadProfessorDashboard(forceRefresh: Boolean) =
                ProfessorDashboardData(
                    teachings = listOf(ProfessorContentItem("Analisi"), ProfessorContentItem("Fisica")),
                    examRounds =
                        listOf(
                            ProfessorContentItem("Analisi", detail = "Prenotati: 12"),
                            ProfessorContentItem("Fisica", detail = "Prenotati: 0"),
                        ),
                    theses = listOf(ProfessorContentItem("Ada Lovelace")),
                )
        }

        val viewModel = HomeDashboardViewModel(source, emptyList(), professorAccount())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isProfessor)
        assertEquals(2, state.teachingCount)
        assertEquals(2, state.openExamRounds)
        assertEquals(1, state.thesisCount)
    }

    @Test
    fun professorDidacticsAndExamsReuseRoleAwarePresentationModels() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadProfessorDashboard(forceRefresh: Boolean) =
                ProfessorDashboardData(
                    teachings = listOf(ProfessorContentItem("Analisi")),
                    examRounds =
                        listOf(
                            ProfessorContentItem("Analisi", detail = "Prenotati: 3"),
                            ProfessorContentItem("Fisica", detail = "Prenotati: 0"),
                        ),
                    theses = listOf(ProfessorContentItem("Ada Lovelace")),
                    reports = listOf(ProfessorContentItem("Verbale Analisi")),
                )
        }
        val account = professorAccount()
        val didactics = DidacticsDashboardViewModel(source, account)
        val exams = ExamsViewModel(source, account = account)
        advanceUntilIdle()

        assertTrue(didactics.uiState.value.isProfessor)
        assertEquals(1, didactics.uiState.value.teachingCount)
        assertEquals(1, didactics.uiState.value.thesisCount)
        assertEquals(1, didactics.uiState.value.reportCount)
        assertTrue(exams.uiState.value.isProfessor)
        assertEquals(2, exams.uiState.value.visibleProfessorExamRounds.size)
        exams.selectTab(1)
        assertEquals(2, exams.uiState.value.visibleProfessorExamRounds.size)
    }

    @Test
    fun professorThesisAndExamDetailsResolveTheSelectedSdkItem() = runViewModelTest {
        val thesis =
            ProfessorContentItem(
                title = "Ada Lovelace",
                subtitle = "Ingegneria Informatica",
                detail = "Matricola: 12345\nTitolo tesi: Motori analitici\nVoto: 110 e lode",
            )
        val exam =
            ProfessorContentItem(
                title = "Analisi",
                subtitle = "Ingegneria Informatica",
                detail = "Data: 10/09/2026 09:00\nPrenotati: 12\nCommissione: Mario Rossi",
            )
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadProfessorDashboard(forceRefresh: Boolean) =
                ProfessorDashboardData(theses = listOf(thesis), examRounds = listOf(exam))
        }

        val thesisDetail =
            AcademicItemDetailViewModel(AcademicSection.Theses, thesis.academicItemKey(), source)
        val examDetail =
            AcademicItemDetailViewModel(AcademicSection.ExamRounds, exam.academicItemKey(), source)
        advanceUntilIdle()

        assertEquals(thesis, thesisDetail.uiState.value.item)
        assertEquals(exam, examDetail.uiState.value.item)
    }

    private fun professorAccount() =
        UniAccountSummary(
            accountId = "professor-account",
            serverUserId = "m.rossi",
            displayName = "Mario Rossi",
            degreeName = "",
            matricola = null,
            email = null,
            photoUrl = null,
            isGuest = false,
            activeProfileId = "professor-1",
            activeProfileType = BackendCareerType.PROFESSOR,
            profiles =
                listOf(
                    UniAccountProfileSummary(
                        profileId = "professor-1",
                        displayName = "Mario Rossi",
                        degreeName = "",
                        dipId = "DIP-12",
                        departmentName = "Dipartimento di Ingegneria",
                        teacherId = "DOC-42",
                        type = BackendCareerType.PROFESSOR,
                    ),
                ),
        )

    private fun runViewModelTest(block: suspend kotlinx.coroutines.test.TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
