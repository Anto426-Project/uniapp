package com.anto426.uniapp.navigation.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.didactics.presentation.AcademicIdentityViewModel
import com.anto426.uniapp.didactics.presentation.AcademicItemDetailViewModel
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.didactics.presentation.AcademicSectionViewModel
import com.anto426.uniapp.didactics.presentation.AttendanceViewModel
import com.anto426.uniapp.didactics.presentation.CourseDetailViewModel
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardViewModel
import com.anto426.uniapp.didactics.presentation.ExamsHistoryViewModel
import com.anto426.uniapp.didactics.presentation.ExamsViewModel
import com.anto426.uniapp.didactics.presentation.GradesViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnaireDetailViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnairesViewModel
import com.anto426.uniapp.didactics.presentation.StudyPlanViewModel
import com.anto426.uniapp.didactics.presentation.TranscriptsViewModel
import com.anto426.uniapp.didactics.presentation.academicItemKey
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.navigation.ui.AppRouteContext
import com.anto426.uniapp.navigation.ui.AttendanceScannerActionHost
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.didactics.AcademicIdentityScreen
import com.anto426.uniapp.ui.didactics.AcademicItemDetailScreen
import com.anto426.uniapp.ui.didactics.AcademicSectionScreen
import com.anto426.uniapp.ui.didactics.AttendanceScreen
import com.anto426.uniapp.ui.didactics.CourseDetailScreen
import com.anto426.uniapp.ui.didactics.DidacticsScreen
import com.anto426.uniapp.ui.didactics.ExamBookableFilter
import com.anto426.uniapp.ui.didactics.ExamHistoryFilter
import com.anto426.uniapp.ui.didactics.ExamsHistoryTabContent
import com.anto426.uniapp.ui.didactics.ExamsScreen
import com.anto426.uniapp.ui.didactics.GradesScreen
import com.anto426.uniapp.ui.didactics.QuestionnaireDetailScreen
import com.anto426.uniapp.ui.didactics.QuestionnairesScreen
import com.anto426.uniapp.ui.didactics.StudyPlanContent
import com.anto426.uniapp.ui.didactics.TranscriptsContent
import com.anto426.uniapp.ui.didactics.components.StudentExamsTabContent
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun DidacticsRouteContent(context: AppRouteContext) {
    val account = (context.sessionState as? AppSessionState.Authenticated)?.account
    val didacticsViewModel =
        viewModel(key = context.viewModelKey) { DidacticsDashboardViewModel(context.dataSource, account) }
    val didacticsUiState by didacticsViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        state = didacticsUiState.loadState,
        errorMessage = didacticsUiState.errorMessage,
        onRetry = { didacticsViewModel.refresh(force = true) },
    ) {
        DidacticsScreen(
            uiState = didacticsUiState,
            onOpenTaxes = { context.navigator.navigate(AppRoute.Taxes) },
            onOpenGrades = { context.navigator.navigate(AppRoute.Grades) },
            onOpenTranscripts = { context.navigator.navigate(AppRoute.Transcripts()) },
            onOpenExams = { context.navigator.navigate(AppRoute.Exams()) },
            onOpenQuestionnaires = { context.navigator.navigate(AppRoute.Questionnaires) },
            onOpenBadge = { context.navigator.navigate(AppRoute.Badge) },
            onOpenAttendance = { context.navigator.navigate(AppRoute.Attendance) },
            onOpenTeachings = { context.navigator.navigate(AppRoute.Teachings) },
            onOpenTheses = { context.navigator.navigate(AppRoute.Theses) },
            onOpenReports = { context.navigator.navigate(AppRoute.Reports) },
            onOpenNews = { context.navigator.navigate(AppRoute.News) },
            onOpenSettings = { context.navigator.selectTopLevel(AppRoute.Settings) },
        )
    }
}

@Composable
internal fun TranscriptsRouteContent(
    route: AppRoute.Transcripts,
    context: AppRouteContext,
) {
    var selectedPane by rememberSaveable(route.initialTab) { mutableIntStateOf(route.initialTab.coerceIn(0, 1)) }
    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_career_completed)),
        LiquidNavigationItem(stringResource(Res.string.ui_career_plan)),
    )
    UniScreenColumn {
        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedPane,
            onTabSelected = { selectedPane = it },
        )
        UniTabContentHost(
            selectedTab = selectedPane,
            tabCount = tabs.size,
            onTabSelected = { selectedPane = it },
            label = "TranscriptsTabs",
        ) { pane ->
            if (pane == 0) {
                val model = viewModel(key = "${context.viewModelKey}-completed") { TranscriptsViewModel(context.dataSource) }
                val state by model.uiState.collectAsStateWithLifecycle()
                FeatureStateContent(
                    state = state.loadState,
                    errorMessage = state.errorMessage,
                    onRetry = { model.refresh(force = true) },
                    emptyTitle = stringResource(Res.string.ui_grades_verbalized_empty),
                    emptyMessage = stringResource(Res.string.ui_state_empty_transcripts),
                    emptyActionLabel = stringResource(Res.string.ui_career_plan),
                    onEmptyActionClick = { selectedPane = 1 },
                    inline = true,
                ) {
                    TranscriptsContent(
                        uiState = state,
                        onYearSelected = model::selectYear,
                        onExamClick = { exam ->
                            if (exam.code.isNotBlank()) context.navigator.navigate(AppRoute.CourseDetail(exam.code))
                        },
                        onOpenGrades = { context.navigator.navigate(AppRoute.Grades) },
                    )
                }
            } else {
                val model = viewModel(key = "${context.viewModelKey}-plan") { StudyPlanViewModel(context.dataSource) }
                val state by model.uiState.collectAsStateWithLifecycle()
                FeatureStateContent(
                    state = state.loadState,
                    errorMessage = state.errorMessage,
                    onRetry = { model.refresh(force = true) },
                    emptyMessage = stringResource(Res.string.ui_state_empty_study_plan),
                    inline = true,
                ) {
                    StudyPlanContent(
                        uiState = state,
                        onYearSelected = model::selectYear,
                        onCourseClick = { course ->
                            if (course.id.isNotBlank()) context.navigator.navigate(AppRoute.CourseDetail(course.id))
                        },
                        onStatusFilterChanged = model::selectStatusFilter,
                    )
                }
            }
        }
    }
}

@Composable
internal fun GradesRouteContent(
    context: AppRouteContext,
    gradeSimulations: GradeSimulationRepository,
) {
    val account = (context.sessionState as? AppSessionState.Authenticated)?.account ?: return
    val simulationOwner = account.activeProfileId?.let { profileId ->
        LocalDataScope.Profile(account.accountId, profileId)
    } ?: LocalDataScope.Account(account.accountId)
    val gradesViewModel = viewModel(key = context.viewModelKey) {
        GradesViewModel(context.dataSource, gradeSimulations, simulationOwner, context.toastSink)
    }
    val gradesUiState by gradesViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        gradesUiState.loadState,
        gradesUiState.errorMessage,
        onRetry = { gradesViewModel.refresh(force = true) },
    ) {
        GradesScreen(
            uiState = gradesUiState,
            onTabSelected = gradesViewModel::selectTab,
            onToggleSimulationItem = gradesViewModel::toggleSimulationItem,
            onSimulatedGradeChanged = gradesViewModel::updateSimulatedGrade,
            onSimulatedCfuChanged = gradesViewModel::updateSimulatedCfu,
            onAddCustomExam = gradesViewModel::addCustomExam,
            onRemoveCustomExam = gradesViewModel::removeCustomExam,
            onSetAllGrades = gradesViewModel::setAllSimulatedGrades,
            onApplyCurrentAverage = gradesViewModel::applyCurrentAveragePreset,
            onResetSimulation = gradesViewModel::resetSimulation,
            onTargetDegreeChanged = gradesViewModel::updateTargetDegree,
            onThesisPointsChanged = gradesViewModel::updateThesisPoints,
            onBonusPointsChanged = gradesViewModel::updateBonusPoints,
        )
    }
}

@Composable
internal fun ExamsRouteContent(
    route: AppRoute.Exams,
    context: AppRouteContext,
) {
    val account = (context.sessionState as? AppSessionState.Authenticated)?.account
    val model = viewModel(key = context.viewModelKey) {
        ExamsViewModel(context.dataSource, context.toastSink, account = account, initialTab = route.initialTab)
    }
    val state by model.uiState.collectAsStateWithLifecycle()
    if (account?.isProfessor == true) {
        FeatureStateContent(
            state = state.loadState,
            errorMessage = state.errorMessage,
            onRetry = { model.refresh(force = true) },
            emptyMessage = stringResource(Res.string.ui_state_empty_exams),
        ) {
            ExamsScreen(
                uiState = state,
                onTabSelected = model::selectTab,
                showTabs = false,
                onToggleBooking = model::toggleBooking,
                onProfessorExamClick = { item ->
                    context.navigator.navigate(AppRoute.ProfessorExamDetail(item.academicItemKey(), item.title))
                },
                onAddToCalendar = { model.openCalendar(it, context.uriHandler::openUri) },
            )
        }
    } else {
        val history = viewModel(key = "${context.viewModelKey}-history") { ExamsHistoryViewModel(context.dataSource) }
        val historyState by history.uiState.collectAsStateWithLifecycle()
        var bookableFilter by rememberSaveable { mutableStateOf(ExamBookableFilter.ALL) }
        var historyFilter by rememberSaveable { mutableStateOf(ExamHistoryFilter.ALL) }
        val examTabs = listOf(
            LiquidNavigationItem(
                label = stringResource(Res.string.ui_bookable_exams),
                badge = state.bookableCount.takeIf { it > 0 }?.toString(),
            ),
            LiquidNavigationItem(
                label = stringResource(Res.string.ui_booked_exams),
                badge = state.bookedCount.takeIf { it > 0 }?.toString(),
            ),
            LiquidNavigationItem(stringResource(Res.string.msg_storico)),
        )
        UniScreenColumn {
            LiquidTabBar(
                items = examTabs,
                selectedIndex = state.selectedTab.coerceIn(0, examTabs.lastIndex),
                onTabSelected = model::selectTab,
            )
            UniTabContentHost(
                selectedTab = state.selectedTab,
                tabCount = examTabs.size,
                onTabSelected = model::selectTab,
                label = "ExamsRouteTabs",
            ) { tab ->
                if (tab == 2) {
                    FeatureStateContent(
                        state = historyState.loadState,
                        errorMessage = historyState.errorMessage,
                        onRetry = { history.refresh(force = true) },
                        emptyTitle = stringResource(Res.string.ui_history_empty_title),
                        emptyMessage = stringResource(Res.string.ui_history_empty_desc),
                        inline = true,
                    ) {
                        ExamsHistoryTabContent(
                            uiState = historyState,
                            filter = historyFilter,
                            onFilterSelected = { historyFilter = it },
                        )
                    }
                } else {
                    FeatureStateContent(
                        state = state.loadState,
                        errorMessage = state.errorMessage,
                        onRetry = { model.refresh(force = true) },
                        emptyMessage = stringResource(Res.string.ui_state_empty_exams),
                        inline = true,
                    ) {
                        StudentExamsTabContent(
                            uiState = state,
                            tabIndex = tab,
                            bookableFilter = bookableFilter,
                            onBookableFilterChange = { bookableFilter = it },
                            onToggleBooking = model::toggleBooking,
                            onAddToCalendar = { examId ->
                                model.openCalendar(examId, context.uriHandler::openUri)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CourseDetailRouteContent(
    route: AppRoute.CourseDetail,
    context: AppRouteContext,
) {
    val detailViewModel = viewModel(key = context.viewModelKey) { CourseDetailViewModel(route.courseId, context.dataSource) }
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        detailUiState.loadState,
        detailUiState.errorMessage,
        onRetry = { detailViewModel.refresh(force = true) },
    ) {
        detailUiState.course?.let { course ->
            CourseDetailScreen(
                course = course,
                professorContact = detailUiState.professorContact,
                onContactClick = { contact ->
                    context.navigator.navigate(AppRoute.ContactDetail(contact.detailKey))
                },
            )
        }
    }
}

@Composable
internal fun QuestionnairesRouteContent(context: AppRouteContext) {
    val questionnairesViewModel = viewModel(key = context.viewModelKey) { QuestionnairesViewModel(context.dataSource) }
    val questionnairesUiState by questionnairesViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        questionnairesUiState.loadState,
        questionnairesUiState.errorMessage,
        onRetry = { questionnairesViewModel.refresh(force = true) },
        emptyMessage = stringResource(Res.string.ui_state_empty_questionnaires),
    ) {
        QuestionnairesScreen(
            uiState = questionnairesUiState,
            onQuestionnaireClick = { questionnaire ->
                context.navigator.navigate(
                    AppRoute.Questionnaire(
                        courseId = questionnaire.courseId,
                        tagList = questionnaire.tagList,
                        title = questionnaire.course,
                    ),
                )
            },
        )
    }
}

@Composable
internal fun QuestionnaireDetailRouteContent(
    route: AppRoute.Questionnaire,
    context: AppRouteContext,
) {
    val questionnaireViewModel = viewModel(key = context.viewModelKey) {
        QuestionnaireDetailViewModel(
            courseId = route.courseId,
            tagList = route.tagList,
            title = route.title,
            dataSource = context.dataSource,
            toastSink = context.toastSink,
        )
    }
    val questionnaireUiState by questionnaireViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        questionnaireUiState.loadState,
        questionnaireUiState.errorMessage,
        onRetry = questionnaireViewModel::refresh,
        emptyMessage = stringResource(Res.string.ui_state_empty_questionnaire_detail),
    ) {
        QuestionnaireDetailScreen(
            uiState = questionnaireUiState,
            onAnswerSelected = questionnaireViewModel::selectAnswer,
            onFreeTextChanged = questionnaireViewModel::updateFreeText,
            onSubmit = questionnaireViewModel::submit,
        )
    }
}

@Composable
internal fun BadgeRouteContent(context: AppRouteContext) {
    val account = (context.sessionState as? AppSessionState.Authenticated)?.account
    val identityViewModel =
        viewModel(key = context.viewModelKey) { AcademicIdentityViewModel(context.dataSource, account) }
    val identityUiState by identityViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        identityUiState.loadState,
        identityUiState.errorMessage,
        onRetry = { identityViewModel.refresh(force = true) },
    ) { AcademicIdentityScreen(identityUiState) }
}

@Composable
internal fun AttendanceRouteContent(
    context: AppRouteContext,
    attendanceScannerActionHost: AttendanceScannerActionHost,
) {
    val attendanceViewModel = viewModel(key = context.viewModelKey) { AttendanceViewModel(context.dataSource) }
    val attendanceUiState by attendanceViewModel.uiState.collectAsStateWithLifecycle()
    var showCameraScanner by rememberSaveable { mutableStateOf(false) }
    val openScanner = remember(attendanceViewModel) {
        {
            attendanceViewModel.clearRegistrationStatus()
            showCameraScanner = true
        }
    }
    DisposableEffect(attendanceScannerActionHost, openScanner) {
        attendanceScannerActionHost.register(openScanner)
        onDispose { attendanceScannerActionHost.unregister(openScanner) }
    }
    AttendanceScreen(
        uiState = attendanceUiState,
        showCameraScanner = showCameraScanner,
        onOpenCameraScanner = openScanner,
        onDismissCameraScanner = { showCameraScanner = false },
        onRetry = { attendanceViewModel.refresh(force = true) },
        onRegisterAttendance = { code ->
            showCameraScanner = false
            attendanceViewModel.registerAttendance(code)
        },
        onClearRegistrationStatus = {
            attendanceViewModel.clearRegistrationStatus()
        },
    )
}

@Composable
internal fun AcademicSectionRouteContent(
    section: AcademicSection,
    viewModelKey: String,
    dataSource: UniGateway,
    searchQuery: String,
    navigator: AppNavigator,
) {
    val sectionViewModel = viewModel(key = "$viewModelKey|$section") {
        AcademicSectionViewModel(section = section, dataSource = dataSource)
    }
    val sectionUiState by sectionViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(sectionViewModel, searchQuery) {
        sectionViewModel.updateQuery(searchQuery)
    }
    FeatureStateContent(
        state = sectionUiState.loadState,
        errorMessage = sectionUiState.errorMessage,
        onRetry = { sectionViewModel.refresh(force = true) },
        emptyMessage = stringResource(Res.string.ui_state_empty_academic_section),
    ) {
        AcademicSectionScreen(
            uiState = sectionUiState,
            section = section,
            onItemClick = { item ->
                val route = when (section) {
                    AcademicSection.Teachings ->
                        AppRoute.TeachingDetail(item.academicItemKey(), item.title)
                    AcademicSection.Theses ->
                        AppRoute.ThesisDetail(item.academicItemKey(), item.title)
                    AcademicSection.Reports ->
                        AppRoute.ReportDetail(item.academicItemKey(), item.title)
                    AcademicSection.ExamRounds ->
                        AppRoute.ProfessorExamDetail(item.academicItemKey(), item.title)
                }
                navigator.navigate(route)
            },
        )
    }
}

@Composable
internal fun AcademicItemDetailRouteContent(
    section: AcademicSection,
    itemKey: String,
    viewModelKey: String,
    dataSource: UniGateway,
) {
    val detailViewModel = viewModel(key = "$viewModelKey|$section|$itemKey") {
        AcademicItemDetailViewModel(
            section = section,
            itemKey = itemKey,
            dataSource = dataSource,
        )
    }
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        state = detailUiState.loadState,
        errorMessage = detailUiState.errorMessage,
        onRetry = { detailViewModel.refresh(force = true) },
        emptyMessage = stringResource(Res.string.ui_state_empty_academic_detail),
    ) {
        AcademicItemDetailScreen(
            uiState = detailUiState,
            section = section,
            onTabSelected = detailViewModel::selectTab,
        )
    }
}
