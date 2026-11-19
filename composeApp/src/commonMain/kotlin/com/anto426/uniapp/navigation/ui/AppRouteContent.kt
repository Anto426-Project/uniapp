package com.anto426.uniapp.navigation.ui



import androidx.compose.runtime.Composable
import com.anto426.uniapp.ui.settings.AboutUniAppScreen
import com.anto426.uniapp.ui.updates.ChangelogScreen
import com.anto426.uniapp.updates.presentation.ChangelogViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.uniapp.didactics.presentation.StudyPlanViewModel
import com.anto426.uniapp.didactics.presentation.ExamsHistoryViewModel
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.didactics.ExamBookableFilter
import com.anto426.uniapp.ui.didactics.ExamHistoryFilter
import com.anto426.uniapp.ui.didactics.StudyPlanContent
import com.anto426.uniapp.ui.didactics.TranscriptsContent
import com.anto426.uniapp.ui.didactics.examsHistoryContentItems
import com.anto426.uniapp.ui.didactics.studentExamsContentItems
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.auth.presentation.LoginViewModel
import com.anto426.uniapp.didactics.presentation.ExamsViewModel
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardViewModel
import com.anto426.uniapp.didactics.presentation.CourseDetailViewModel
import com.anto426.uniapp.didactics.presentation.GradesViewModel
import com.anto426.uniapp.didactics.presentation.AttendanceViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnairesViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnaireDetailViewModel
import com.anto426.uniapp.didactics.presentation.AcademicIdentityViewModel
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.didactics.presentation.AcademicSectionViewModel
import com.anto426.uniapp.didactics.presentation.AcademicItemDetailViewModel
import com.anto426.uniapp.didactics.presentation.academicItemKey
import com.anto426.uniapp.didactics.presentation.TranscriptsViewModel
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.library.presentation.LibraryBookDetailViewModel
import com.anto426.uniapp.library.presentation.LibrarySpaceBookingViewModel
import com.anto426.uniapp.library.presentation.LibraryViewModel
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.notifications.runtime.AppNotificationController
import com.anto426.uniapp.news.presentation.NewsViewModel
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.services.presentation.ContactsViewModel
import com.anto426.uniapp.services.presentation.ContactDetailViewModel
import com.anto426.uniapp.services.presentation.ServicesViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.session.SessionManager
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.settings.presentation.ConnectedDevicesViewModel
import com.anto426.uniapp.settings.presentation.ColorLabViewModel
import com.anto426.uniapp.settings.presentation.LanguageUiState
import com.anto426.uniapp.settings.presentation.SettingsViewModel
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.uniapp.transport.presentation.TransportBookingViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.uniapp.transport.presentation.ReservationDetailViewModel
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.anto426.uniapp.ui.account.AccountRemovalDialog
import com.anto426.uniapp.ui.auth.LoginScreen
import com.anto426.uniapp.ui.bootstrap.AppBootstrapScreen
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.didactics.AttendanceScreen
import com.anto426.uniapp.ui.didactics.CourseDetailScreen
import com.anto426.uniapp.ui.didactics.DidacticsScreen
import com.anto426.uniapp.ui.didactics.ExamsScreen
import com.anto426.uniapp.ui.didactics.GradesScreen
import com.anto426.uniapp.ui.didactics.QuestionnairesScreen
import com.anto426.uniapp.ui.didactics.QuestionnaireDetailScreen
import com.anto426.uniapp.ui.didactics.AcademicIdentityScreen
import com.anto426.uniapp.ui.didactics.AcademicSectionScreen
import com.anto426.uniapp.ui.didactics.AcademicItemDetailScreen
import com.anto426.uniapp.ui.home.dashboard.HomeScreen
import com.anto426.uniapp.ui.library.LibraryBookDetailScreen
import com.anto426.uniapp.ui.library.LibraryScreen
import com.anto426.uniapp.ui.library.LibrarySpaceBookingScreen
import com.anto426.uniapp.ui.legal.CookiesScreen
import com.anto426.uniapp.ui.legal.PrivacyScreen
import com.anto426.uniapp.ui.legal.TermsScreen
import com.anto426.uniapp.ui.news.NewsDetailScreen
import com.anto426.uniapp.ui.news.NewsScreen
import com.anto426.uniapp.ui.services.ContactDetailScreen
import com.anto426.uniapp.ui.services.ContactsScreen
import com.anto426.uniapp.ui.services.ServicesScreen
import com.anto426.uniapp.ui.services.TaxesScreen
import com.anto426.uniapp.ui.settings.AppInfoScreen
import com.anto426.uniapp.ui.settings.CreatorCreditsScreen
import com.anto426.uniapp.ui.settings.ColorLabScreen
import com.anto426.uniapp.ui.settings.ConnectedDevicesScreen
import com.anto426.uniapp.ui.settings.LanguageScreen
import com.anto426.uniapp.ui.settings.SettingsScreen
import com.anto426.uniapp.ui.settings.ThemeScreen
import com.anto426.uniapp.ui.transport.ReservationDetailScreen
import com.anto426.uniapp.ui.transport.TransportBookingScreen
import com.anto426.uniapp.ui.transport.TransportScreen
import com.anto426.uniapp.ui.updates.UpdatesScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AppRouteContent(
    route: AppRoute,
    reservationDeleteActionHost: ReservationDeleteActionHost,
    navigator: AppNavigator,
    sessionController: SessionManager,
    accountSwitcherViewModel: AccountSwitcherViewModel,
    accountSwitcherUiState: AccountSwitcherUiState,
    dataSource: UniAppDataSource,
    localDataStore: UniLocalDataStore,
    projectData: com.anto426.uniapp.project.data.ProjectDataStore,
    accountId: String,
    searchQuery: String,
    isSearchActive: Boolean,
    updateUiState: AppUpdateUiState,
    notificationController: AppNotificationController,
    toastSink: AppToastSink,
    biometricAuthenticator: BiometricAuthenticator,
    sessionState: AppSessionState,
    unlockUiState: AppUnlockUiState,
    onRequestUnlock: () -> Unit,
    onPasswordUnlock: (String) -> Unit,
    onCancelUnlock: () -> Unit,
    onRetryUpdate: () -> Unit,
    onOpenUpdate: () -> Unit,
    themeUiState: ThemeUiState,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onThemeSelected: (Int) -> Unit,
    onBackgroundStyleSelected: (String) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onPageTransitionSelected: (LiquidSwitcherTransition) -> Unit,
    onLiquidIntensityChanged: (Float) -> Unit = {},
    onGlassQualitySelected: (LiquidGlassQualityTier?) -> Unit = {},
    onBackgroundIntensityChanged: (Float) -> Unit = {},
    onBackgroundSpeedChanged: (Float) -> Unit = {},
    onBackgroundVignetteChanged: (Boolean) -> Unit = {},
    onResetTheme: () -> Unit,
    onCustomColorSelected: (Color) -> Unit = {},
    languageUiState: LanguageUiState,
    onLanguageSelected: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    val activeProfileId = (sessionState as? AppSessionState.Authenticated)?.account?.activeProfileId
    val dataGeneration = (dataSource as? com.anto426.uniapp.data.runtime.ScopedDataRepository)?.generation ?: 0
    val viewModelKey = "$accountId|${activeProfileId.orEmpty()}|$dataGeneration|$route"
    val uriHandler = LocalUriHandler.current
    when (route) {
        AppRoute.Bootstrap ->
            AppBootstrapScreen(
                accountName = (sessionState as? AppSessionState.UnlockRequired)?.account?.displayName,
                unlockUiState = unlockUiState,
                onRequestUnlock = onRequestUnlock,
                onPasswordUnlock = onPasswordUnlock,
                accountId = (sessionState as? AppSessionState.UnlockRequired)?.account?.accountId,
                onCancelUnlock = onCancelUnlock,
            )

        AppRoute.Login -> {
            val loginViewModel =
                viewModel(key = viewModelKey) { LoginViewModel(sessionController, toastSink) }
            val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()
            AccountRemovalDialog(
                state = accountSwitcherUiState,
                onConfirm = { accountSwitcherViewModel.confirmAccountRemoval(biometricAuthenticator) },
                onDismiss = accountSwitcherViewModel::dismissAccountRemoval,
            )
            LoginScreen(
                uiState = loginUiState,
                accountUiState = accountSwitcherUiState,
                onSelectAccount = accountSwitcherViewModel::selectAccount,
                onRemoveAccount = accountSwitcherViewModel::requestAccountRemoval,
                onUsernameChange = loginViewModel::updateUsername,
                onPasswordChange = loginViewModel::updatePassword,
                onRememberCredentialsChange = loginViewModel::updateRememberCredentials,
                onSubmit = loginViewModel::submit,
                onCareerSelected = loginViewModel::selectCareer,
                onCancelCareerSelection = loginViewModel::cancelCareerSelection,
                onShowForgotPassword = loginViewModel::showForgotPassword,
                onDismissForgotPassword = loginViewModel::dismissForgotPassword,
                onOpenPrivacy = { navigator.navigate(AppRoute.Privacy) },
                onOpenTerms = { navigator.navigate(AppRoute.Terms) },
            )
        }

        AppRoute.Home -> {
            val account = (sessionState as? AppSessionState.Authenticated)?.account
            val homeViewModel =
                viewModel(key = viewModelKey) {
                    HomeDashboardViewModel(
                        dataSource = dataSource,
                        quickActions =
                            if (account?.isProfessor == true) UniAppInitialData.professorQuickActions
                            else UniAppInitialData.allQuickActions,
                        account = account,
                    )
                }
            val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                uiState = homeUiState,
                onOpenStatistics = { navigator.navigate(AppRoute.Grades) },
                onOpenTaxes = { navigator.navigate(AppRoute.Taxes) },
                onOpenExams = { navigator.navigate(AppRoute.Exams()) },
                onOpenNews = { navigator.navigate(AppRoute.News) },
                onOpenBadge = { navigator.navigate(AppRoute.Badge) },
                onShowNews = { news ->
                    navigator.navigate(
                        AppRoute.NewsDetail(
                            newsKey = news.key,
                            accountId = accountId,
                            profileId = activeProfileId,
                        ),
                    )
                },
                onNextNews = homeViewModel::showNextNews,
                onPreviousNews = homeViewModel::showPreviousNews,
                onSelectNews = homeViewModel::selectNews,
                onRefresh = { homeViewModel.refresh(force = true) },
                onToggleCustomization = homeViewModel::toggleCustomization,
                onFinishCustomization = homeViewModel::finishCustomization,
                onToggleQuickAction = homeViewModel::toggleQuickAction,
                onQuickActionClick = navigator::openHomeAction,
            )
        }

        AppRoute.Services -> {
            val isProfessor =
                (sessionState as? AppSessionState.Authenticated)?.account?.isProfessor == true
            val servicesViewModel =
                viewModel(key = viewModelKey) {
                    ServicesViewModel(
                        studentServices = if (isProfessor) UniAppInitialData.professorServices else UniAppInitialData.studentServices,
                        universityPortals = if (isProfessor) UniAppInitialData.professorPortals else UniAppInitialData.universityPortals,
                    )
                }
            val servicesUiState by servicesViewModel.uiState.collectAsStateWithLifecycle()
            ServicesScreen(
                uiState = servicesUiState,
            ) { service ->
                when (service) {
                    "news" -> navigator.navigate(AppRoute.News)
                    "transport" -> navigator.navigate(AppRoute.Transport)
                    "taxes" -> navigator.navigate(AppRoute.Taxes)
                    "statistics" -> navigator.navigate(AppRoute.Grades)
                    "contacts" -> navigator.navigate(AppRoute.Contacts)
                    "student-office" -> uriHandler.openUri("https://www3.unimol.it/servizi/segreteria_studenti")
                    "esse3" -> uriHandler.openUri("https://unimol.esse3.cineca.it")
                    "moodle" -> uriHandler.openUri("https://learn.unimol.it")
                    "university-web" -> uriHandler.openUri("https://www.unimol.it")
                    "library" -> navigator.navigate(AppRoute.Library())
                    "email" -> uriHandler.openUri("https://outlook.office.com/mail/")
                }
            }
        }

        AppRoute.Didactics -> {
            val account = (sessionState as? AppSessionState.Authenticated)?.account
            val didacticsViewModel =
                viewModel(key = viewModelKey) { DidacticsDashboardViewModel(dataSource, account) }
            val didacticsUiState by didacticsViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                state = didacticsUiState.loadState,
                errorMessage = didacticsUiState.errorMessage,
                onRetry = { didacticsViewModel.refresh(force = true) },
            ) {
                DidacticsScreen(
                    uiState = didacticsUiState,
                    onOpenTaxes = { navigator.navigate(AppRoute.Taxes) },
                    onOpenGrades = { navigator.navigate(AppRoute.Grades) },
                    onOpenTranscripts = { navigator.navigate(AppRoute.Transcripts()) },
                    onOpenExams = { navigator.navigate(AppRoute.Exams()) },
                    onOpenQuestionnaires = { navigator.navigate(AppRoute.Questionnaires) },
                    onOpenBadge = { navigator.navigate(AppRoute.Badge) },
                    onOpenAttendance = { navigator.navigate(AppRoute.Attendance) },
                    onOpenTeachings = { navigator.navigate(AppRoute.Teachings) },
                    onOpenTheses = { navigator.navigate(AppRoute.Theses) },
                    onOpenReports = { navigator.navigate(AppRoute.Reports) },
                    onOpenNews = { navigator.navigate(AppRoute.News) },
                    onOpenSettings = { navigator.selectTopLevel(AppRoute.Settings) },
                )
            }
        }

        AppRoute.Teachings ->
            AcademicSectionRouteContent(
                section = AcademicSection.Teachings,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                searchQuery = searchQuery,
                navigator = navigator,
            )

        AppRoute.Theses ->
            AcademicSectionRouteContent(
                section = AcademicSection.Theses,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                searchQuery = searchQuery,
                navigator = navigator,
            )

        AppRoute.Reports ->
            AcademicSectionRouteContent(
                section = AcademicSection.Reports,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                searchQuery = searchQuery,
                navigator = navigator,
            )

        is AppRoute.TeachingDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.Teachings,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
            )

        is AppRoute.ProfessorExamDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.ExamRounds,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
            )

        is AppRoute.ThesisDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.Theses,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
            )

        is AppRoute.ReportDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.Reports,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
            )

        AppRoute.Settings -> {
            val settingsViewModel =
                viewModel(key = viewModelKey) {
                    SettingsViewModel(
                        localDataStore = localDataStore,
                        accountId = accountId,
                        toastSink = toastSink,
                        biometricAuthenticator = biometricAuthenticator,
                        notificationController = notificationController,
                    )
                }
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            AccountRemovalDialog(
                state = accountSwitcherUiState,
                onConfirm = { accountSwitcherViewModel.confirmAccountRemoval(biometricAuthenticator) },
                onDismiss = accountSwitcherViewModel::dismissAccountRemoval,
            )
            SettingsScreen(
                uiState = settingsUiState,
                accountUiState = accountSwitcherUiState,
                installedVersion = updateUiState.installedVersion,
                updateSubtitle = if (updateUiState.bannerState == com.anto426.uniapp.model.updates.UpdateState.AVAILABLE) {
                    updateUiState.statusText?.let { stringResource(it) } ?: stringResource(Res.string.msg_aggiornamento_disponibile)
                } else {
                    "Versione ${updateUiState.installedVersion.ifBlank { "2.0" }}"
                },
                onSelectAccount = accountSwitcherViewModel::selectAccount,
                onRemoveAccount = accountSwitcherViewModel::requestAccountRemoval,
                onAddAccount = accountSwitcherViewModel::addAccount,
                onOpenInfo = { navigator.navigate(AppRoute.Info) },
                onOpenTheme = { navigator.navigate(AppRoute.Theme) },
                onOpenUpdates = { navigator.navigate(AppRoute.Updates) },
                onOpenDevices = { navigator.navigate(AppRoute.Devices) },
                onOpenLanguage = { navigator.navigate(AppRoute.Language) },
                onOpenContribute = { uriHandler.openUri(com.anto426.unisdk.platform.ProjectInfo.repositoryUrl) },
                onOpenReportBug = { uriHandler.openUri(com.anto426.unisdk.platform.ProjectInfo.issuesUrl) },
                onOpenLogin = accountSwitcherViewModel::addAccount,
                onSignOut = onSignOut,
                onNotificationsEnabledChange = settingsViewModel::setNotificationsEnabled,
                onBiometricEnabledChange = settingsViewModel::setBiometricEnabled,
                onSubmitBiometricPassword = settingsViewModel::submitBiometricPassword,
                onDismissBiometricPassword = settingsViewModel::dismissBiometricPasswordSetup,
                onRequestSignOut = settingsViewModel::requestSignOut,
                onDismissSignOut = settingsViewModel::dismissSignOut,
            )
        }

        is AppRoute.Transcripts -> {
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
                if (selectedPane == 0) {
                    val model = viewModel(key = "$viewModelKey-completed") { TranscriptsViewModel(dataSource) }
                    val state by model.uiState.collectAsStateWithLifecycle()
                    FeatureStateContent(
                        state = state.loadState,
                        errorMessage = state.errorMessage,
                        onRetry = { model.refresh(force = true) },
                        emptyMessage = stringResource(Res.string.ui_state_empty_transcripts),
                        inline = true,
                    ) {
                        TranscriptsContent(
                            uiState = state,
                            onYearSelected = model::selectYear,
                            onExamClick = { exam ->
                                if (exam.code.isNotBlank()) navigator.navigate(AppRoute.CourseDetail(exam.code))
                            },
                            onOpenGrades = { navigator.navigate(AppRoute.Grades) },
                        )
                    }
                } else {
                    val model = viewModel(key = "$viewModelKey-plan") { StudyPlanViewModel(dataSource) }
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
                                if (course.id.isNotBlank()) navigator.navigate(AppRoute.CourseDetail(course.id))
                            },
                            onStatusFilterChanged = model::selectStatusFilter,
                        )
                    }
                }
            }
        }

        AppRoute.Info ->
            AppInfoScreen(
                onReportBug = { uriHandler.openUri(com.anto426.unisdk.platform.ProjectInfo.bugReportUrl()) },
                updateUiState = updateUiState,
                onOpenSource = { uriHandler.openUri(com.anto426.unisdk.platform.ProjectInfo.repositoryUrl) },
                onOpenAboutUniApp = { navigator.navigate(AppRoute.AboutUniApp) },
                onOpenPrivacy = { navigator.navigate(AppRoute.Privacy) },
                onOpenTerms = { navigator.navigate(AppRoute.Terms) },
                onOpenCookies = { navigator.navigate(AppRoute.Cookies) },
                onOpenCreatorCredits = { navigator.navigate(AppRoute.Author) },
                onOpenUpdates = { navigator.navigate(AppRoute.Updates) },
                onOpenChangelog = { navigator.navigate(AppRoute.Changelog) },
            )

        AppRoute.Theme -> {
            ThemeScreen(
                uiState = themeUiState,
                onThemeModeSelected = onThemeModeSelected,
                onThemeSelected = onThemeSelected,
                onBackgroundStyleSelected = onBackgroundStyleSelected,
                onReducedMotionChanged = onReducedMotionChanged,
                onPageTransitionSelected = onPageTransitionSelected,
                onLiquidIntensityChanged = onLiquidIntensityChanged,
                onGlassQualitySelected = onGlassQualitySelected,
                onBackgroundIntensityChanged = onBackgroundIntensityChanged,
                onBackgroundSpeedChanged = onBackgroundSpeedChanged,
                onBackgroundVignetteChanged = onBackgroundVignetteChanged,
                onReset = onResetTheme,
                onNavigateToColorLab = { navigator.navigate(AppRoute.Colors) },
            )
        }

        AppRoute.Colors -> {
            val colorLabViewModel = viewModel(key = "app-color-lab") { ColorLabViewModel(localDataStore) }
            val colorLabUiState by colorLabViewModel.uiState.collectAsStateWithLifecycle()
            ColorLabScreen(
                uiState = colorLabUiState,
                onColorSelected = { color ->
                    colorLabViewModel.selectColor(color)
                    onCustomColorSelected(color)
                },
            )
        }
        AppRoute.Taxes -> {
            val taxesViewModel = viewModel(key = viewModelKey) { TaxesViewModel(dataSource) }
            val taxesUiState by taxesViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                taxesUiState.loadState,
                taxesUiState.errorMessage,
                onRetry = { taxesViewModel.refresh(force = true) },
                emptyMessage = stringResource(Res.string.ui_state_empty_taxes),
            ) {
                TaxesScreen(
                    uiState = taxesUiState,
                    onFilterSelected = taxesViewModel::selectFilter,
                )
            }
        }
        AppRoute.Grades -> {
            val gradesViewModel =
                viewModel(key = viewModelKey) {
                    GradesViewModel(dataSource)
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
        AppRoute.Contacts -> {
            val contactsViewModel = viewModel(key = viewModelKey) { ContactsViewModel(dataSource) }
            val contactsUiState by contactsViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(searchQuery, isSearchActive) {
                contactsViewModel.updateSearchQuery(
                    searchQuery.takeIf { isSearchActive }.orEmpty(),
                )
            }
            FeatureStateContent(
                contactsUiState.loadState,
                contactsUiState.errorMessage,
                onRetry = { contactsViewModel.refresh(force = true) },
            ) {
                ContactsScreen(
                    uiState = contactsUiState,
                    onCategorySelected = contactsViewModel::selectCategory,
                    onContactClick = { contact ->
                        navigator.navigate(AppRoute.ContactDetail(contact.detailKey))
                    },
                )
            }
        }

        is AppRoute.ContactDetail -> {
            val detailViewModel = viewModel(key = viewModelKey) { ContactDetailViewModel(route.contactId, dataSource) }
            val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                detailUiState.loadState,
                detailUiState.errorMessage,
                onRetry = { detailViewModel.refresh(force = true) },
            ) { detailUiState.contact?.let { contact -> ContactDetailScreen(contact) } }
        }

        AppRoute.Transport -> {
            val transportViewModel = viewModel(key = viewModelKey) { TransportViewModel(dataSource) }
            val transportUiState by transportViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                transportUiState.loadState,
                transportUiState.errorMessage,
                onRetry = { transportViewModel.refresh(force = true) },
                emptyMessage = stringResource(Res.string.ui_state_empty_transport_reservations),
            ) {
                TransportScreen(
                    uiState = transportUiState,
                    onReservationClick = { reservation ->
                        navigator.navigate(
                            AppRoute.ReservationDetail(
                                reservationId = reservation.id,
                                title = reservation.route,
                            ),
                        )
                    },
                    onBookClick = { navigator.navigate(AppRoute.TransportBooking) },
                )
            }
        }

        AppRoute.TransportBooking -> {
            val bookingViewModel =
                viewModel(key = viewModelKey) {
                    TransportBookingViewModel(dataSource, toastSink)
                }
            val bookingUiState by bookingViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                bookingUiState.loadState,
                bookingUiState.errorMessage,
                onRetry = { bookingViewModel.refresh(force = true) },
                emptyMessage = stringResource(Res.string.ui_state_empty_transport_lines),
            ) {
                TransportBookingScreen(
                    uiState = bookingUiState,
                    onRouteSelected = bookingViewModel::selectRoute,
                    onBook = { dates, direction -> bookingViewModel.book(dates, direction) },
                )
            }
        }
        is AppRoute.ReservationDetail -> {
            val detailViewModel =
                viewModel(key = viewModelKey) {
                    ReservationDetailViewModel(route.reservationId, dataSource, toastSink)
                }
            val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
            val hasVisibleReservation = detailUiState.loadState == FeatureLoadState.Content &&
                detailUiState.reservation != null && !detailUiState.deleted
            val deleteAction = remember(
                route.reservationId,
                detailViewModel,
                hasVisibleReservation,
                detailUiState.isDeleting,
            ) {
                if (hasVisibleReservation) {
                    ReservationDeleteAction(
                        reservationId = route.reservationId,
                        enabled = !detailUiState.isDeleting,
                        onClick = detailViewModel::delete,
                    )
                } else {
                    null
                }
            }
            DisposableEffect(deleteAction, reservationDeleteActionHost) {
                if (deleteAction != null) reservationDeleteActionHost.register(deleteAction)
                onDispose {
                    if (deleteAction != null) reservationDeleteActionHost.unregister(deleteAction)
                }
            }
            LaunchedEffect(detailUiState.deleted) {
                if (detailUiState.deleted) navigator.goBack()
            }
            FeatureStateContent(
                detailUiState.loadState,
                detailUiState.errorMessage,
                onRetry = { detailViewModel.refresh(force = true) },
            ) {
                detailUiState.reservation?.let { reservation ->
                    ReservationDetailScreen(
                        reservation = reservation,
                        imageState = detailUiState.image,
                        onReloadImage = { detailViewModel.loadTicketImage(forceRefresh = true) },
                    )
                }
            }
        }

        is AppRoute.Library -> {
            val libraryViewModel =
                viewModel(key = viewModelKey) {
                    LibraryViewModel(route.initialTab, dataSource, toastSink)
                }
            val libraryUiState by libraryViewModel.uiState.collectAsStateWithLifecycle()
            LibraryScreen(
                uiState = libraryUiState,
                onTabSelected = libraryViewModel::selectTab,
                onQueryChange = libraryViewModel::updateQuery,
                onSearch = libraryViewModel::search,
                onBookClick = { book ->
                    navigator.navigate(AppRoute.LibraryBookDetail(bookId = book.id, title = book.title))
                },
                onRefreshMovements = libraryViewModel::refreshMovements,
                onRequestCancelMovement = libraryViewModel::requestCancelMovement,
                onConfirmCancelMovement = libraryViewModel::confirmCancelMovement,
                onDismissCancelMovement = libraryViewModel::dismissCancelMovement,
                onRenewMovement = libraryViewModel::renewMovement,
                onRefreshSpaces = libraryViewModel::refreshSpaces,
                onOpenSpaceBooking = { navigator.navigate(AppRoute.LibraryBooking) },
                onRequestCancelBooking = libraryViewModel::requestCancelBooking,
                onConfirmCancelBooking = libraryViewModel::confirmCancelBooking,
                onDismissCancelBooking = libraryViewModel::dismissCancelBooking,
            )
        }

        is AppRoute.LibraryBookDetail -> {
            val detailViewModel =
                viewModel(key = viewModelKey) {
                    LibraryBookDetailViewModel(route.bookId, dataSource, toastSink)
                }
            val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                state = detailUiState.loadState,
                errorMessage = detailUiState.errorMessage,
                onRetry = detailViewModel::refresh,
            ) {
                LibraryBookDetailScreen(
                    uiState = detailUiState,
                    onPrepareAction = detailViewModel::prepareRequest,
                    onConfirmDraft = detailViewModel::confirmDraft,
                    onDismissDraft = detailViewModel::dismissDraft,
                )
            }
        }

        AppRoute.LibraryBooking -> {
            val bookingViewModel =
                viewModel(key = viewModelKey) {
                    LibrarySpaceBookingViewModel(dataSource, toastSink)
                }
            val bookingUiState by bookingViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                state = bookingUiState.loadState,
                errorMessage = bookingUiState.errorMessage,
                onRetry = bookingViewModel::refresh,
                emptyMessage = stringResource(Res.string.ui_library_booking_slots_empty),
            ) {
                LibrarySpaceBookingScreen(
                    uiState = bookingUiState,
                    onSelectType = bookingViewModel::selectType,
                    onSelectOwner = bookingViewModel::selectOwner,
                    onSelectResource = bookingViewModel::selectResource,
                    onLoadCalendarPage = bookingViewModel::loadCalendarPage,
                    onToggleSlot = bookingViewModel::toggleSlot,
                    onUpdateDeviceAddress = bookingViewModel::updateDeviceAddress,
                    onUpdateNote = bookingViewModel::updateNote,
                    onBook = bookingViewModel::book,
                    onBookingCompleted = { navigator.goBack() },
                )
            }
        }

        is AppRoute.Exams -> {
            val account = (sessionState as? AppSessionState.Authenticated)?.account
            val model = viewModel(key = viewModelKey) { ExamsViewModel(dataSource, toastSink, account = account, initialTab = route.initialTab) }
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
                            navigator.navigate(AppRoute.ProfessorExamDetail(item.academicItemKey(), item.title))
                        },
                        onAddToCalendar = { model.openCalendar(it, uriHandler::openUri) },
                    )
                }
            } else {
                val history = viewModel(key = "$viewModelKey-history") { ExamsHistoryViewModel(dataSource) }
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
                UniScreenLazyColumn {
                    item(key = "exams-tab-bar") {
                        LiquidTabBar(
                            items = examTabs,
                            selectedIndex = state.selectedTab,
                            onTabSelected = model::selectTab,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    if (state.selectedTab == 2) {
                        if (historyState.loadState != FeatureLoadState.Content) {
                            item(key = "exams-history-state-${historyState.loadState}") {
                                FeatureStateContent(
                                    state = historyState.loadState,
                                    errorMessage = historyState.errorMessage,
                                    onRetry = { history.refresh(force = true) },
                                    inline = true,
                                ) {}
                            }
                        } else {
                            examsHistoryContentItems(
                                uiState = historyState,
                                filter = historyFilter,
                                onFilterSelected = { historyFilter = it },
                            )
                        }
                    } else {
                        if (state.loadState != FeatureLoadState.Content) {
                            item(key = "exams-state-${state.selectedTab}-${state.loadState}") {
                                FeatureStateContent(
                                    state = state.loadState,
                                    errorMessage = state.errorMessage,
                                    onRetry = { model.refresh(force = true) },
                                    emptyMessage = stringResource(Res.string.ui_state_empty_exams),
                                    inline = true,
                                ) {}
                            }
                        } else {
                            studentExamsContentItems(
                                uiState = state,
                                bookableFilter = bookableFilter,
                                onBookableFilterChange = { bookableFilter = it },
                                onToggleBooking = model::toggleBooking,
                                onAddToCalendar = { examId ->
                                    model.openCalendar(examId, uriHandler::openUri)
                                },
                            )
                        }
                    }
                }
            }
        }

        is AppRoute.CourseDetail -> {
            val detailViewModel = viewModel(key = viewModelKey) { CourseDetailViewModel(route.courseId, dataSource) }
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
                            navigator.navigate(AppRoute.ContactDetail(contact.detailKey))
                        },
                    )
                }
            }
        }

        AppRoute.Questionnaires -> {
            val questionnairesViewModel = viewModel(key = viewModelKey) { QuestionnairesViewModel(dataSource) }
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
                        navigator.navigate(
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
        is AppRoute.Questionnaire -> {
            val questionnaireViewModel =
                viewModel(key = viewModelKey) {
                    QuestionnaireDetailViewModel(
                        courseId = route.courseId,
                        tagList = route.tagList,
                        title = route.title,
                        dataSource = dataSource,
                        toastSink = toastSink,
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
        AppRoute.Badge -> {
            val account = (sessionState as? AppSessionState.Authenticated)?.account
            val identityViewModel =
                viewModel(key = viewModelKey) { AcademicIdentityViewModel(dataSource, account) }
            val identityUiState by identityViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                identityUiState.loadState,
                identityUiState.errorMessage,
                onRetry = { identityViewModel.refresh(force = true) },
            ) { AcademicIdentityScreen(identityUiState) }
        }
        AppRoute.Attendance -> {
            val attendanceViewModel = viewModel(key = viewModelKey) { AttendanceViewModel(dataSource) }
            val attendanceUiState by attendanceViewModel.uiState.collectAsStateWithLifecycle()
            AttendanceScreen(
                uiState = attendanceUiState,
                onRegisterAttendance = { code ->
                    attendanceViewModel.registerAttendance(code)
                },
                onClearRegistrationStatus = {
                    attendanceViewModel.clearRegistrationStatus()
                },
            )
        }
        AppRoute.AboutUniApp ->
            AboutUniAppScreen(
                sections = UniAppInitialData.appInfoSections,
                onReportBug = { uriHandler.openUri(com.anto426.unisdk.platform.ProjectInfo.bugReportUrl()) },
                onBack = { navigator.goBack() },
            )
        AppRoute.Privacy -> PrivacyScreen(UniAppInitialData.privacySections, onBack = { navigator.goBack() })
        AppRoute.Terms -> TermsScreen(UniAppInitialData.termsSections, onBack = { navigator.goBack() })
        AppRoute.Cookies -> CookiesScreen(UniAppInitialData.cookieSections)
        AppRoute.Updates ->
            UpdatesScreen(
                uiState = updateUiState,
                onRetry = onRetryUpdate,
                onOpenUpdate = onOpenUpdate,
                onOpenChangelog = { navigator.navigate(AppRoute.Changelog) },
            )

        AppRoute.Changelog -> {
            val changelogViewModel = viewModel(key = viewModelKey) { ChangelogViewModel(updateUiState) }
            LaunchedEffect(
                changelogViewModel,
                updateUiState.releaseNotes,
                updateUiState.displayedVersion,
                updateUiState.installedVersion,
                updateUiState.publishedAt,
                updateUiState.channel,
            ) {
                changelogViewModel.update(updateUiState)
            }
            val changelogUiState by changelogViewModel.uiState.collectAsStateWithLifecycle()
            ChangelogScreen(
                uiState = changelogUiState,
                onExpansionChanged = changelogViewModel::setExpanded,
            )
        }
        AppRoute.News -> {
            val newsViewModel = viewModel(key = viewModelKey) { NewsViewModel(dataSource) }
            val newsUiState by newsViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                newsUiState.loadState,
                newsUiState.errorMessage,
                onRetry = { newsViewModel.refresh(force = true) },
                emptyMessage = stringResource(Res.string.ui_news_empty_desc),
            ) {
                NewsScreen(
                    uiState = newsUiState,
                    onTabSelected = newsViewModel::selectTab,
                    onNewsSelected = { news ->
                        navigator.navigate(
                            AppRoute.NewsDetail(
                                newsKey = news.key,
                                accountId = accountId,
                                profileId = activeProfileId,
                            ),
                        )
                    },
                )
            }
        }

        is AppRoute.NewsDetail -> {
            val newsViewModel = viewModel(key = viewModelKey) { NewsViewModel(dataSource) }
            val newsUiState by newsViewModel.uiState.collectAsStateWithLifecycle()
            val news = newsUiState.news.firstOrNull { it.key == route.newsKey }
            if (news != null) {
                NewsDetailScreen(
                    title = news.title,
                    description = news.description,
                    fullContent = news.fullContent,
                    category = news.category,
                    publishedAt = news.publishedAt,
                    sourceUrl = news.sourceUrl,
                    type = news.type,
                    onOpenSource = { url -> uriHandler.openUri(url) },
                    onBack = { navigator.goBack() },
                )
            } else {
                FeatureStateContent(
                    state = if (newsUiState.loadState == FeatureLoadState.Content) FeatureLoadState.Empty else newsUiState.loadState,
                    errorMessage = newsUiState.errorMessage,
                    onRetry = { newsViewModel.refresh(force = true) },
                    emptyMessage = stringResource(Res.string.ui_news_empty_desc),
                ) {}
            }
        }

        AppRoute.Devices -> {
            val devicesViewModel =
                viewModel(key = viewModelKey) { ConnectedDevicesViewModel(dataSource, toastSink) }
            val devicesUiState by devicesViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                devicesUiState.loadState,
                devicesUiState.errorMessage,
                onRetry = { devicesViewModel.refresh(force = true) },
            ) {
                ConnectedDevicesScreen(
                    uiState = devicesUiState,
                    onRequestRevocation = devicesViewModel::requestRevocation,
                    onDismissRevocation = devicesViewModel::dismissRevocation,
                    onConfirmRevocation = devicesViewModel::confirmRevocation,
                )
            }
        }

        AppRoute.Language -> {
            LanguageScreen(
                uiState = languageUiState,
                onLanguageSelected = onLanguageSelected,
            )
        }
        AppRoute.Author -> {
            val projectViewModel = viewModel(key = "public-project-info") {
                com.anto426.uniapp.project.presentation.ProjectInfoViewModel(projectData)
            }
            val projectState by projectViewModel.uiState.collectAsStateWithLifecycle()
            CreatorCreditsScreen(
                state = projectState,
                onOpenLink = { url ->
                    val parsed = runCatching { io.ktor.http.Url(url) }.getOrNull()
                    if (parsed?.protocol?.name == "https") uriHandler.openUri(url)
                },
            )
        }
    }
}

@Composable
private fun AcademicSectionRouteContent(
    section: AcademicSection,
    viewModelKey: String,
    dataSource: UniAppDataSource,
    searchQuery: String,
    navigator: AppNavigator,
) {
    val sectionViewModel =
        viewModel(key = "$viewModelKey|$section") {
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
                val route =
                    when (section) {
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
private fun AcademicItemDetailRouteContent(
    section: AcademicSection,
    itemKey: String,
    viewModelKey: String,
    dataSource: UniAppDataSource,
) {
    val detailViewModel =
        viewModel(key = "$viewModelKey|$section|$itemKey") {
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

@Composable
private fun InvalidDetailRoute(navigator: AppNavigator) {
    LaunchedEffect(navigator) { navigator.goBack() }
}

private fun AppNavigator.openHomeAction(actionId: String) {
    when (actionId) {
        "libretto" -> navigate(AppRoute.Transcripts())
        "statistiche" -> navigate(AppRoute.Grades)
        "media" -> navigate(AppRoute.Grades)
        "appelli" -> navigate(AppRoute.Exams())
        "insegnamenti" -> navigate(AppRoute.Teachings)
        "tesi" -> navigate(AppRoute.Theses)
        "verbali" -> navigate(AppRoute.Reports)
        "didattica", "condivisione" -> selectTopLevel(AppRoute.Didactics)
        "trasporti" -> navigate(AppRoute.Transport)
        "biblioteca", "library" -> navigate(AppRoute.Library())
        "tasse" -> navigate(AppRoute.Taxes)
        "rubrica" -> navigate(AppRoute.Contacts)
        "notifiche" -> navigate(AppRoute.News)
        "sicurezza" -> navigate(AppRoute.Devices)
        "impostazioni" -> selectTopLevel(AppRoute.Settings)
        else -> selectTopLevel(AppRoute.Services)
    }
}
