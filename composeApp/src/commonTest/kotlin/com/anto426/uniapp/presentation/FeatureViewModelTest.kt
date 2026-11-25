package com.anto426.uniapp.presentation

import com.anto426.uniapp.didactics.presentation.GradesViewModel

import com.anto426.uniapp.account.model.UniAccountProfileSummary
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.buildExamHistory
import com.anto426.uniapp.data.local.FakeUniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.contract.toAcademicContentItem
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.feedback.runtime.AppToastMessage
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.didactics.presentation.QuestionnairesViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnaireDetailViewModel
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardViewModel
import com.anto426.uniapp.didactics.presentation.ExamsViewModel
import com.anto426.uniapp.didactics.presentation.StudyPlanViewModel
import com.anto426.uniapp.didactics.presentation.TranscriptsViewModel
import com.anto426.uniapp.didactics.presentation.AcademicIdentityViewModel
import com.anto426.uniapp.didactics.presentation.AcademicItemDetailViewModel
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.didactics.presentation.academicItemKey
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.data.contract.PastExamStatus
import com.anto426.uniapp.settings.presentation.DeviceSessionsActionViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.CareerExam
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.StudyPlanCourse
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.StudentDetails
import com.anto426.unisdk.model.ProfessorContentItem
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.SurveyAnswerOption
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.SurveyPage
import com.anto426.unisdk.model.SurveyQuestion
import com.anto426.unisdk.model.SurveySaveRequest
import com.anto426.unisdk.model.TaxInstallment
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FeatureViewModelTest : com.anto426.uniapp.testing.ResourceTest() {
    @Test
    fun taxesArePartitionedFromRepositoryData() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean) = Taxes(
                dueAmount = "100",
                installments = listOf(
                    TaxInstallment("Da pagare", "100", "domani", false),
                    TaxInstallment("Pagata", "20", "ieri", true),
                ),
            )
        }
        val viewModel = TaxesViewModel(source.asUniRepository(backgroundScope))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.pendingPayments.size)
        assertEquals(1, viewModel.uiState.value.paidPayments.size)
        assertEquals("€ 100.00", viewModel.uiState.value.dueAmountLabel)
        assertEquals("€ 20.00", viewModel.uiState.value.paidTotalLabel)
        assertEquals("domani", viewModel.uiState.value.nextDueDate)
        viewModel.selectFilter(com.anto426.uniapp.services.presentation.TaxesFilter.PENDING)
        assertEquals(com.anto426.uniapp.services.presentation.TaxesFilter.PENDING, viewModel.uiState.value.filter)
    }

    @Test
    fun didacticsDashboardUsesRemoteValuesWithoutJoiningCfuNumbers() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetails(
                    fullName = "Student",
                    matricola = "165432",
                    degreeName = "Ingegneria Informatica",
                    departmentName = "Dipartimento di Ingegneria",
                )

            override suspend fun loadCareer(forceRefresh: Boolean) =
                Career(
                    average = "28.2",
                    cfu = "120 / 180",
                    cfuTarget = 180,
                    year = "3° anno",
                    status = "Attiva",
                    exams = List(14) { index ->
                        CareerExam("Esame $index", "28", "01/01/2026", 6)
                    },
                )

            override suspend fun loadStudyPlan(forceRefresh: Boolean) =
                StudyPlan(
                    List(20) { index ->
                        StudyPlanCourse(
                            title = "Attività $index",
                            cfu = 9,
                            year = (index / 7 + 1).coerceAtMost(3),
                            completed = index < 14,
                        )
                    },
                )

            override suspend fun loadExamRounds(forceRefresh: Boolean) =
                listOf(ExamRound("Analisi", "01/09/2026 09:00", "Aula 1", 20, open = true))

            override suspend fun loadSurveyCourses(forceRefresh: Boolean) =
                listOf(SurveyCourse("1", "Analisi", "tag", completed = false))
        }
        val viewModel = DidacticsDashboardViewModel(source.asUniRepository(backgroundScope))
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
    fun didacticsDashboardStaysContentWithoutErrorWhenSecondaryDatasetsFail() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetails(fullName = "Ada Lovelace", degreeName = "Informatica", matricola = "12345")

            override suspend fun loadCareer(forceRefresh: Boolean) =
                Career(
                    average = "28.5",
                    degreeBase = "104.5",
                    cfu = "90 / 180",
                    cfuTarget = 180,
                    year = "2° anno",
                    status = "Attiva",
                    exams = listOf(CareerExam("Algoritmi", "30", "01/02/2026", 9, "A")),
                )

            override suspend fun loadStudyPlan(forceRefresh: Boolean): StudyPlan = error("offline study plan")
            override suspend fun loadExamRounds(forceRefresh: Boolean): List<ExamRound> = error("offline exams")
            override suspend fun loadSurveyCourses(forceRefresh: Boolean): List<SurveyCourse> = error("offline surveys")
        }

        val viewModel = DidacticsDashboardViewModel(source.asUniRepository(backgroundScope))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(FeatureLoadState.Content, state.loadState)
        assertNull(state.errorMessage)
        assertEquals("Informatica", state.degreeName)
        assertEquals(90, state.acquiredCfu)
        assertEquals(1, state.completedExams)
    }

    @Test
    fun disconnectAllSessionsPublishesFeedbackAndRefreshSignal() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun disconnectAllOtherDevices() = "Altre sessioni disconnesse"
        }
        val messages = mutableListOf<AppToastMessage>()
        val viewModel = DeviceSessionsActionViewModel(source.asUniRepository(backgroundScope), AppToastSink(messages::add))

        viewModel.requestDisconnectAll()
        assertTrue(viewModel.uiState.value.isConfirmationVisible)
        viewModel.confirmDisconnectAll()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.refreshRevision)
        assertEquals(false, viewModel.uiState.value.isConfirmationVisible)
        assertEquals("Altre sessioni disconnesse", messages.single().resolveText())
    }

    @Test
    fun questionnaireProgressUsesRemoteCourseStatuses() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadSurveyCourses(forceRefresh: Boolean) = listOf(
                SurveyCourse("1", "Corso A", "tag-a", "A", completed = true),
                SurveyCourse("2", "Corso B", "tag-b", "B", completed = false),
            )
        }
        val viewModel = QuestionnairesViewModel(source.asUniRepository(backgroundScope))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalCount)
        assertEquals(.5f, viewModel.uiState.value.completedProgress)
    }

    @Test
    fun studyPlanKeepsYearsProvidedByTheAcademicBackend() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudyPlan(forceRefresh: Boolean) =
                StudyPlan(
                    listOf(
                        StudyPlanCourse("Analisi", cfu = 9, year = 1, completed = true),
                        StudyPlanCourse("Tirocinio", cfu = 6, year = 4),
                    ),
                )
        }
        val viewModel = StudyPlanViewModel(source.asUniRepository(backgroundScope))
        advanceUntilIdle()

        assertEquals(listOf(1, 4), viewModel.uiState.value.years.map { it.yearNumber })
        assertEquals(listOf(1, 4), viewModel.uiState.value.displayedYears.map { it.yearNumber })
        assertEquals(1, viewModel.uiState.value.completedCount)
        assertEquals(2, viewModel.uiState.value.totalCoursesCount)
        assertEquals(9, viewModel.uiState.value.acquiredCfu)
        assertEquals(180, viewModel.uiState.value.totalCfu)
        viewModel.selectStatusFilter(com.anto426.uniapp.data.contract.CourseStatus.COMPLETED)
        assertEquals(com.anto426.uniapp.data.contract.CourseStatus.COMPLETED, viewModel.uiState.value.statusFilter)
        viewModel.selectYear(0)
        assertEquals(1, viewModel.uiState.value.displayedYears.single().yearNumber)
        viewModel.selectYear(1)
        assertEquals(4, viewModel.uiState.value.displayedYears.single().yearNumber)
    }

    @Test
    fun transcriptsSelectsYearDirectlyWithoutAllOption() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadCareer(forceRefresh: Boolean) =
                Career(
                    average = "28.0",
                    degreeBase = "102.0",
                    cfu = "18",
                    cfuTarget = 180,
                    year = "2",
                    status = "Attivo",
                    exams = listOf(
                        CareerExam("Analisi", "30", "01/02/2025", 9, "A"),
                        CareerExam("Tirocinio", "IDONEO", "15/05/2025", 6, "T"),
                        CareerExam("Programmazione", "30 e lode", "20/06/2025", 9, "P"),
                        CareerExam("Seminario", "", "20/06/2025", 3, "S"),
                        CareerExam("Fisica", "28", "01/02/2026", 9, "B"),
                    ),
                )

            override suspend fun loadStudyPlan(forceRefresh: Boolean) =
                StudyPlan(
                    listOf(
                        StudyPlanCourse("Analisi", year = 1, adsceId = "A"),
                        StudyPlanCourse("Tirocinio", year = 1, adsceId = "T"),
                        StudyPlanCourse("Programmazione", year = 1, adsceId = "P"),
                        StudyPlanCourse("Fisica", year = 2, adsceId = "B"),
                    ),
                )
        }
        val viewModel = TranscriptsViewModel(source.asUniRepository(backgroundScope))
        advanceUntilIdle()

        val allExams = viewModel.uiState.value.examsByYear.values.flatten()
        assertEquals(listOf("Analisi", "Tirocinio", "Programmazione", "Seminario", "Fisica"), allExams.map { it.name })
        assertEquals(true, allExams.first { it.name == "Programmazione" }.lode)
        assertEquals("Idoneo", allExams.first { it.name == "Tirocinio" }.grade)
        assertEquals("Idoneo", allExams.first { it.name == "Seminario" }.grade)
        assertEquals(listOf(1, 2, 0), viewModel.uiState.value.availableYears)
        assertEquals(com.anto426.uniapp.didactics.presentation.ALL_ACADEMIC_YEARS, viewModel.uiState.value.selectedYear)
        assertEquals(listOf(1, 2, 0), viewModel.uiState.value.displayedYears)
        assertEquals("28.0", viewModel.uiState.value.averageLabel)
        assertEquals("102.0", viewModel.uiState.value.degreeBaseLabel)
        assertEquals(18, viewModel.uiState.value.acquiredCfu)
        assertEquals(180, viewModel.uiState.value.targetCfu)
        assertEquals(5, viewModel.uiState.value.totalExamsCount)
        assertEquals(1, viewModel.uiState.value.lodeCount)
        viewModel.selectYear(2)
        assertEquals(2, viewModel.uiState.value.selectedYear)
        assertEquals(listOf(2), viewModel.uiState.value.displayedYears)
    }

    @Test
    fun questionnaireSubmissionUsesSelectedRemoteAnswerIds() = runViewModelTest {
        var savedRequest: SurveySaveRequest? = null
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetails(fullName = "Student", stuId = "student-1")

            override suspend fun loadSurveyFirstPage(courseId: String, tagList: String) =
                SurveyForm(
                    userCompId = "user-comp",
                    questCompId = "quest-comp",
                    pages = listOf(
                        SurveyPage(
                            pageId = "page-1",
                            questions = listOf(
                                SurveyQuestion(
                                    questionId = "question-1",
                                    questionText = "Valutazione",
                                    answers = listOf(SurveyAnswerOption("answer-1", "Ottimo")),
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
        assertEquals("Salvato", messages.single().resolveText())
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
        val viewModel = TransportViewModel(source.asUniRepository(backgroundScope))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.days.size)
        assertEquals(2, viewModel.uiState.value.days.single().reservations.size)
    }

    @Test
    fun selectingStatisticsTabPreservesRemoteChartData() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadCareer(forceRefresh: Boolean) = Career(
                average = "28.5",
                degreeBase = "104.5",
                cfu = "18",
                cfuTarget = 180,
                year = "1",
                status = "Attivo",
                exams = listOf(
                    CareerExam("Analisi", "30", "01/02/2026", 9, "A"),
                    CareerExam("Fisica", "27", "01/03/2026", 9, "B"),
                ),
            )
        }
        val simRepo = GradeSimulationRepository(FakeUniLocalDataStore(), backgroundScope)
        val viewModel = GradesViewModel(
            dataSource = source.asUniRepository(backgroundScope, simulations = simRepo),
            simulationRepository = simRepo,
            simulationOwner = LocalDataScope.Profile("account", "career"),
            toastSink = AppToastSink.None,
        )
        advanceUntilIdle()
        val initial = viewModel.uiState.value.singleGradeEntries

        viewModel.selectTab(2)

        assertEquals(2, viewModel.uiState.value.selectedTab)
        assertEquals(initial, viewModel.uiState.value.singleGradeEntries)
        assertEquals(listOf(2f), viewModel.uiState.value.cfuEntries.map { it.value })
        assertEquals(listOf("9 CFU"), viewModel.uiState.value.cfuEntries.map { it.label })
        assertTrue(viewModel.uiState.value.cfuMax >= 2f)
        assertTrue(initial.isNotEmpty())
    }

    @Test
    fun gradesSimulationSelectionsSurviveRefresh() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadCareer(forceRefresh: Boolean) = Career(
                average = "25.0",
                degreeBase = "91.67",
                cfu = "12",
                cfuTarget = 168,
                year = "1",
                status = "Attivo",
                exams = listOf(CareerExam("Analisi", "25", "01/02/2026", 12, "A")),
            )

            override suspend fun loadStudyPlan(forceRefresh: Boolean) = StudyPlan(
                courses = listOf(
                    StudyPlanCourse("Reti", cfu = 9, year = 2, completed = false, adsceId = "course-reti"),
                ),
            )
        }
        val simRepo = GradeSimulationRepository(FakeUniLocalDataStore(), backgroundScope)
        val viewModel = GradesViewModel(
            dataSource = source.asUniRepository(backgroundScope, simulations = simRepo),
            simulationRepository = simRepo,
            simulationOwner = LocalDataScope.Profile("account", "career"),
            toastSink = AppToastSink.None,
        )
        advanceUntilIdle()

        viewModel.updateSimulatedGrade(viewModel.uiState.value.simulationItems.single().id, 30)
        viewModel.refresh(force = true)
        advanceUntilIdle()

        val item = viewModel.uiState.value.simulationItems.single()
        assertEquals(true, item.isEnabled)
        assertEquals(30, item.grade)
        assertEquals(168, viewModel.uiState.value.degreeCfuTarget)
    }

    @Test
    fun examRoundsAreChronologicalAndPastBookingsLeaveActiveSections() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadExamRounds(forceRefresh: Boolean) =
                listOf(
                    ExamRound("Reti", "12/09/2026 09:00", "Aula 3", 10, open = true),
                    ExamRound("Analisi", "04/09/2026 11:00", "Aula 1", 0, open = false, booked = true),
                    ExamRound("Fisica", "06/09/2026 08:30", "Aula 2", 5, open = false, booked = true),
                    ExamRound("Basi di dati", "07/09/2026 15:00", "Lab", 12, open = true),
                )
        }
        val viewModel = ExamsViewModel(source.asUniRepository(backgroundScope), today = { LocalDate(2026, 9, 5) })
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
                        ExamRound(
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
                        ExamRound(
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
                StudentDetails(
                    fullName = "Ada Lovelace",
                    matricola = "12345",
                    badgeQrValue = "001234|LOVELACE|ADA|Informatica",
                    badgeBarcodeValue = "001234",
                    photoUrl = expectedPhoto,
                )

            override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray {
                assertEquals(expectedPhoto, source)
                return expectedBytes
            }
        }
        val repo = source.asUniRepository(backgroundScope)
        val home = HomeDashboardViewModel(repo, quickActions = emptyList(), preparationDispatcher = Dispatchers.Main)
        val badge = AcademicIdentityViewModel(repo)
        advanceUntilIdle()

        home.uiState.first { it.profilePhotoData != null || it.loadState == FeatureLoadState.Error }
        assertContentEquals(expectedBytes, home.uiState.value.profilePhotoData)
        assertContentEquals(expectedBytes, badge.uiState.value.photoData)
        assertEquals("001234|LOVELACE|ADA|Informatica", badge.uiState.value.badgeQrValue)
        assertEquals("001234", badge.uiState.value.badgeBarcodeValue)
    }

    @Test
    fun professorBadgeUsesTheActiveAccountProfileWithoutLoadingStudentData() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetails =
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
                activeProfileType = UniProfileRole.PROFESSOR,
                profiles =
                    listOf(
                        UniAccountProfileSummary(
                            profileId = "professor-1",
                            displayName = "Mario Rossi",
                            degreeName = "",
                            dipId = "DIP-12",
                            departmentName = "Dipartimento di Ingegneria",
                            teacherId = "DOC-42",
                            type = UniProfileRole.PROFESSOR,
                        ),
                    ),
            )

        val badge = AcademicIdentityViewModel(source.asUniRepository(backgroundScope), account)
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
            override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetails =
                error("Professor Home must not request student details")

            override suspend fun loadProfessorDashboard(forceRefresh: Boolean) =
                ProfessorDashboard(
                    teachings = listOf(ProfessorContentItem("Analisi"), ProfessorContentItem("Fisica")),
                    examRounds =
                        listOf(
                            ProfessorContentItem("Analisi", detail = "Prenotati: 12"),
                            ProfessorContentItem("Fisica", detail = "Prenotati: 0"),
                        ),
                    theses = listOf(ProfessorContentItem("Ada Lovelace")),
                )
        }

        val viewModel = HomeDashboardViewModel(source.asUniRepository(backgroundScope), emptyList(), professorAccount(), preparationDispatcher = Dispatchers.Main)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isProfessor)
        assertEquals(2, state.teachingCount)
        assertEquals(2, state.openExamRounds)
        assertEquals(1, state.thesisCount)
    }

    @Test
    fun homeNewsCarouselSelectsAndClampsIndices() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadUniversityNews(forceRefresh: Boolean) = listOf(
                UniversityNews(id = "1", title = "Uno", summary = "S1", content = "C1", publishedAt = "2026-09-25", category = "Ateneo", sourceUrl = null),
                UniversityNews(id = "2", title = "Due", summary = "S2", content = "C2", publishedAt = "2026-09-24", category = "Dipartimento", sourceUrl = null),
                UniversityNews(id = "3", title = "Tre", summary = "S3", content = "C3", publishedAt = "2026-09-23", category = "Corso di studi", sourceUrl = null),
            )
        }

        val viewModel = HomeDashboardViewModel(source.asUniRepository(backgroundScope), emptyList(), preparationDispatcher = Dispatchers.Main)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.activeNewsIndex)
        viewModel.selectNews(2)
        assertEquals(2, viewModel.uiState.value.activeNewsIndex)
        viewModel.selectNews(99)
        assertEquals(2, viewModel.uiState.value.activeNewsIndex)
        viewModel.selectNews(-4)
        assertEquals(0, viewModel.uiState.value.activeNewsIndex)
        viewModel.showPreviousNews()
        assertEquals(2, viewModel.uiState.value.activeNewsIndex)
        viewModel.showNextNews()
        assertEquals(0, viewModel.uiState.value.activeNewsIndex)
    }

    @Test
    fun professorDidacticsAndExamsReuseRoleAwarePresentationModels() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadProfessorDashboard(forceRefresh: Boolean) =
                ProfessorDashboard(
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
        val repo = source.asUniRepository(backgroundScope)
        val didactics = DidacticsDashboardViewModel(repo, account)
        val exams = ExamsViewModel(repo, account = account)
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
                ProfessorDashboard(theses = listOf(thesis), examRounds = listOf(exam))
        }

        val repo = source.asUniRepository(backgroundScope)
        val thesisItem = thesis.toAcademicContentItem()
        val examItem = exam.toAcademicContentItem()
        val thesisDetail =
            AcademicItemDetailViewModel(AcademicSection.Theses, thesisItem.academicItemKey(), repo)
        val examDetail =
            AcademicItemDetailViewModel(AcademicSection.ExamRounds, examItem.academicItemKey(), repo)
        advanceUntilIdle()

        thesisDetail.uiState.first { it.loadState != FeatureLoadState.Loading }
        assertEquals(thesisItem, thesisDetail.uiState.value.item)
        examDetail.uiState.first { it.loadState != FeatureLoadState.Loading }
        assertEquals(examItem, examDetail.uiState.value.item)
    }

    @Test
    fun academicMetricsAreIdenticalAcrossAllScreensAndExcludeUnpassedLibrettoRows() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetails(
                    fullName = "Ada Lovelace",
                    matricola = "165432",
                    degreeName = "Ingegneria Informatica",
                    departmentName = "Dipartimento di Ingegneria",
                )

            override suspend fun loadCareer(forceRefresh: Boolean) =
                Career(
                    average = "29.0",
                    degreeBase = "106.33",
                    cfu = "24 / 180",
                    cfuTarget = 180,
                    year = "2° anno",
                    status = "Attiva",
                    exams = listOf(
                        CareerExam("Analisi", "30", "01/02/2025", 9, "A"),
                        CareerExam("Fisica", "28", "20/06/2025", 9, "B"),
                        CareerExam("Inglese", "IDONEO", "10/07/2025", 6, "E"),
                        // Unpassed /libretto rows returned by Esse3 with "-" grade and "-" date:
                        CareerExam("Reti di Calcolatori", "-", "-", 9, "R"),
                        CareerExam("Sistemi Operativi", "", "-", 9, "S"),
                    ),
                )

            override suspend fun loadStudyPlan(forceRefresh: Boolean) =
                StudyPlan(
                    listOf(
                        StudyPlanCourse("Analisi", cfu = 9, year = 1, completed = true, adsceId = "A"),
                        StudyPlanCourse("Fisica", cfu = 9, year = 1, completed = true, adsceId = "B"),
                        StudyPlanCourse("Inglese", cfu = 6, year = 1, completed = true, adsceId = "E"),
                        StudyPlanCourse("Reti di Calcolatori", cfu = 9, year = 2, completed = false, adsceId = "R"),
                        StudyPlanCourse("Sistemi Operativi", cfu = 9, year = 2, completed = false, adsceId = "S"),
                    ),
                )
        }

        val simRepo = GradeSimulationRepository(FakeUniLocalDataStore(), backgroundScope)
        val repo = source.asUniRepository(backgroundScope, simulations = simRepo)
        val home = HomeDashboardViewModel(repo, emptyList(), preparationDispatcher = Dispatchers.Main)
        val didactics = DidacticsDashboardViewModel(repo)
        val transcripts = TranscriptsViewModel(repo)
        val studyPlan = StudyPlanViewModel(repo)
        val grades = GradesViewModel(
            dataSource = repo,
            simulationRepository = simRepo,
            simulationOwner = LocalDataScope.Profile("account", "career"),
            toastSink = AppToastSink.None,
        )
        val history = buildExamHistory(
            careerExams = source.loadCareer(false).exams,
            rounds = emptyList(),
            today = LocalDate(2026, 9, 5),
        )
        advanceUntilIdle()

        // 1. All screens agree on 24 acquired CFU / 180 target CFU
        assertEquals("24", home.uiState.value.acquiredCfu)
        assertEquals(180, home.uiState.value.targetCfu)
        assertEquals(24, didactics.uiState.value.acquiredCfu)
        assertEquals(180, didactics.uiState.value.targetCfu)
        assertEquals(24, transcripts.uiState.value.acquiredCfu)
        assertEquals(180, transcripts.uiState.value.targetCfu)
        assertEquals(24, studyPlan.uiState.value.acquiredCfu)
        assertEquals(180, studyPlan.uiState.value.totalCfu)
        assertEquals(24, grades.uiState.value.effectiveAcquiredCfu)
        assertEquals(24, grades.uiState.value.totalProjectedCfu)
        assertEquals(180, grades.uiState.value.degreeCfuTarget)
        assertEquals(156, grades.uiState.value.remainingDegreeCfu)

        // 2. All screens agree on 3 passed exams (2 numeric + 1 idoneità), never counting unpassed "-" rows
        assertEquals(3, home.uiState.value.completedExams)
        assertEquals(3, didactics.uiState.value.completedExams)
        assertEquals(3, transcripts.uiState.value.totalExamsCount)
        assertEquals(3, studyPlan.uiState.value.completedCount)
        assertEquals(3, grades.uiState.value.totalPassedExamsCount)
        assertEquals(2, grades.uiState.value.currentExams.size)
        assertEquals(3, history.size)

        // 3. Unpassed courses are simulatable and not in transcript or exam history
        assertEquals(
            listOf("Analisi", "Fisica", "Inglese"),
            transcripts.uiState.value.examsByYear.values.flatten().map { it.name },
        )
        assertEquals(
            listOf("Reti di Calcolatori", "Sistemi Operativi"),
            grades.uiState.value.simulationItems.map { it.name },
        )
    }

    @Test
    fun studyPlanAndHomeAlwaysAgreeOnPassedExamCountEvenWithDuplicateOrStalePlanRows() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) =
                StudentDetails(fullName = "Ada Lovelace", matricola = "165432", degreeName = "Informatica")

            override suspend fun loadCareer(forceRefresh: Boolean) =
                Career(
                    average = "28.0",
                    degreeBase = "102.67",
                    cfu = "96 / 180",
                    cfuTarget = 180,
                    year = "3° anno",
                    status = "Attiva",
                    exams = List(16) { index ->
                        CareerExam("Esame ${index + 1}", "28", "01/02/2026", 6, "ID-${index + 1}")
                    } + listOf(
                        CareerExam("Tesi di Laurea", "-", "-", 6, "ID-17"),
                    ),
                )

            override suspend fun loadStudyPlan(forceRefresh: Boolean) =
                StudyPlan(
                    courses = List(16) { index ->
                        StudyPlanCourse(
                            title = "Esame ${index + 1}",
                            cfu = 6,
                            year = (index / 6) + 1,
                            completed = true,
                            adsceId = "ID-${index + 1}",
                        )
                    } + listOf(
                        // Duplicate entry from /piani with different year and no adsceId
                        StudyPlanCourse(title = "Esame 1", cfu = 6, year = 3, completed = false, adsceId = null),
                        // Unpassed activity erroneously flagged completed=true in plan payload (e.g. stato="P")
                        StudyPlanCourse(title = "Tesi di Laurea", cfu = 6, year = 3, completed = true, adsceId = "ID-17"),
                        StudyPlanCourse(title = "Sistemi Distribuiti", cfu = 6, year = 3, completed = false, adsceId = "ID-18"),
                    ),
                )
        }

        val repo = source.asUniRepository(backgroundScope)
        val home = HomeDashboardViewModel(repo, emptyList(), preparationDispatcher = Dispatchers.Main)
        val studyPlan = StudyPlanViewModel(repo)
        val didactics = DidacticsDashboardViewModel(repo)
        val transcripts = TranscriptsViewModel(repo)
        advanceUntilIdle()

        assertEquals(16, home.uiState.value.completedExams)
        assertEquals(16, didactics.uiState.value.completedExams)
        assertEquals(16, transcripts.uiState.value.totalExamsCount)
        assertEquals(16, studyPlan.uiState.value.completedCount)
        assertEquals(18, studyPlan.uiState.value.totalCoursesCount)
        assertEquals(
            16,
            studyPlan.uiState.value.years.sumOf { y ->
                y.courses.count { it.status == com.anto426.uniapp.data.contract.CourseStatus.COMPLETED }
            },
        )
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
            activeProfileType = UniProfileRole.PROFESSOR,
            profiles =
                listOf(
                    UniAccountProfileSummary(
                        profileId = "professor-1",
                        displayName = "Mario Rossi",
                        degreeName = "",
                        dipId = "DIP-12",
                        departmentName = "Dipartimento di Ingegneria",
                        teacherId = "DOC-42",
                        type = UniProfileRole.PROFESSOR,
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
