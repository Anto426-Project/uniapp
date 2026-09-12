package com.anto426.uniapp.navigation.ui



import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.auth.presentation.LoginViewModel
import com.anto426.uniapp.didactics.presentation.ExamsViewModel
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardViewModel
import com.anto426.uniapp.didactics.presentation.CourseDetailViewModel
import com.anto426.uniapp.didactics.presentation.ExamsHistoryViewModel
import com.anto426.uniapp.didactics.presentation.GradesViewModel
import com.anto426.uniapp.didactics.presentation.AttendanceViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnairesViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnaireDetailViewModel
import com.anto426.uniapp.didactics.presentation.StatisticsViewModel
import com.anto426.uniapp.didactics.presentation.StudyPlanViewModel
import com.anto426.uniapp.didactics.presentation.AcademicIdentityViewModel
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.didactics.presentation.AcademicSectionViewModel
import com.anto426.uniapp.didactics.presentation.AcademicItemDetailViewModel
import com.anto426.uniapp.didactics.presentation.academicItemKey
import com.anto426.uniapp.didactics.presentation.TranscriptsViewModel
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.notifications.runtime.AppNotificationController
import com.anto426.uniapp.news.presentation.NewsViewModel
import com.anto426.uniapp.services.presentation.ContactsViewModel
import com.anto426.uniapp.services.presentation.ContactDetailViewModel
import com.anto426.uniapp.services.presentation.ServicesViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.settings.presentation.ConnectedDevicesViewModel
import com.anto426.uniapp.settings.presentation.ColorLabViewModel
import com.anto426.uniapp.settings.presentation.LanguageUiState
import com.anto426.uniapp.settings.presentation.SettingsViewModel
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.transport.presentation.TransportBookingViewModel
import com.anto426.uniapp.transport.presentation.TransportCatalogViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.uniapp.transport.presentation.ReservationDetailViewModel
import com.anto426.uniapp.transport.presentation.TicketDetailViewModel
import com.anto426.uniapp.updates.presentation.ChangelogViewModel
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.anto426.uniapp.ui.account.AccountRemovalDialog
import com.anto426.uniapp.ui.auth.LoginScreen
import com.anto426.uniapp.ui.bootstrap.AppBootstrapScreen
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.didactics.AttendanceScreen
import com.anto426.uniapp.ui.didactics.CourseDetailScreen
import com.anto426.uniapp.ui.didactics.DidacticsScreen
import com.anto426.uniapp.ui.didactics.ExamsHistoryScreen
import com.anto426.uniapp.ui.didactics.ExamsScreen
import com.anto426.uniapp.ui.didactics.GradesScreen
import com.anto426.uniapp.ui.didactics.QuestionnairesScreen
import com.anto426.uniapp.ui.didactics.QuestionnaireDetailScreen
import com.anto426.uniapp.ui.didactics.StatisticsScreen
import com.anto426.uniapp.ui.didactics.AcademicIdentityScreen
import com.anto426.uniapp.ui.didactics.AcademicSectionScreen
import com.anto426.uniapp.ui.didactics.AcademicItemDetailScreen
import com.anto426.uniapp.ui.didactics.StudyPlanScreen
import com.anto426.uniapp.ui.didactics.TranscriptsScreen
import com.anto426.uniapp.ui.home.dashboard.HomeScreen
import com.anto426.uniapp.ui.legal.CookiesScreen
import com.anto426.uniapp.ui.legal.PrivacyScreen
import com.anto426.uniapp.ui.legal.TermsScreen
import com.anto426.uniapp.ui.news.NewsDetailScreen
import com.anto426.uniapp.ui.news.NewsScreen
import com.anto426.uniapp.ui.services.ContactDetailScreen
import com.anto426.uniapp.ui.services.ContactsScreen
import com.anto426.uniapp.ui.services.ServicesScreen
import com.anto426.uniapp.ui.services.TaxesScreen
import com.anto426.uniapp.ui.settings.AboutUniAppScreen
import com.anto426.uniapp.ui.settings.AppInfoScreen
import com.anto426.uniapp.ui.settings.CreatorCreditsScreen
import com.anto426.uniapp.ui.settings.ColorLabScreen
import com.anto426.uniapp.ui.settings.ConnectedDevicesScreen
import com.anto426.uniapp.ui.settings.LanguageScreen
import com.anto426.uniapp.ui.settings.SettingsScreen
import com.anto426.uniapp.ui.settings.ThemeScreen
import com.anto426.uniapp.ui.transport.ReservationDetailScreen
import com.anto426.uniapp.ui.transport.TicketDetailScreen
import com.anto426.uniapp.ui.transport.TransportBookingScreen
import com.anto426.uniapp.ui.transport.TransportCatalogScreen
import com.anto426.uniapp.ui.transport.TransportScreen
import com.anto426.uniapp.ui.updates.ChangelogScreen
import com.anto426.uniapp.ui.updates.UpdatesScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AppRouteContent(
    route: AppRoute,
    navigator: AppNavigator,
    sessionController: AppSessionController,
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
    onResetTheme: () -> Unit,
    onCustomColorSelected: (Color) -> Unit = {},
    languageUiState: LanguageUiState,
    onLanguageSelected: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    val activeProfileId = (sessionState as? AppSessionState.Authenticated)?.account?.activeProfileId
    val dataGeneration = (dataSource as? com.anto426.uniapp.data.runtime.UniAppDataCoordinator)?.generation ?: 0
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
            val accountViewModel =
                viewModel(key = "${viewModelKey}_account") { AccountSwitcherViewModel(sessionController, toastSink) }
            val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()
            AccountRemovalDialog(
                state = accountUiState,
                onConfirm = { accountViewModel.confirmAccountRemoval(biometricAuthenticator) },
                onDismiss = accountViewModel::dismissAccountRemoval,
            )
            LoginScreen(
                uiState = loginUiState,
                accountUiState = accountUiState,
                onSelectAccount = accountViewModel::selectAccount,
                onRemoveAccount = accountViewModel::requestAccountRemoval,
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
                onOpenStatistics = { navigator.navigate(AppRoute.Statistics) },
                onOpenTaxes = { navigator.navigate(AppRoute.Taxes) },
                onOpenExams = { navigator.navigate(AppRoute.Exams) },
                onOpenNews = { navigator.navigate(AppRoute.News) },
                onOpenBadge = { navigator.navigate(AppRoute.Badge) },
                onShowNews = { news ->
                    navigator.navigate(
                        AppRoute.NewsDetail(
                            title = news.title,
                            description = news.description,
                            fullContent = news.fullContent,
                        ),
                    )
                },
                onNextNews = homeViewModel::showNextNews,
                onPreviousNews = homeViewModel::showPreviousNews,
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
                    "statistics" -> navigator.navigate(AppRoute.Statistics)
                    "contacts", "professors" -> navigator.navigate(AppRoute.Contacts)
                    "student-office" -> uriHandler.openUri("https://www3.unimol.it/servizi/segreteria_studenti")
                    "esse3" -> if (isProfessor) {
                        uriHandler.openUri("https://unimol.esse3.cineca.it")
                    } else {
                        navigator.navigate(AppRoute.Transcripts)
                    }
                    "moodle" -> uriHandler.openUri("https://learn.unimol.it")
                    "university-web" -> uriHandler.openUri("https://www.unimol.it")
                    "library" -> uriHandler.openUri("https://www.unimol.it/studente/servizi/biblioteche/")
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
                    onOpenStatistics = { navigator.navigate(AppRoute.Statistics) },
                    onOpenTranscripts = { navigator.navigate(AppRoute.Transcripts) },
                    onOpenExams = { navigator.navigate(AppRoute.Exams) },
                    onOpenQuestionnaires = { navigator.navigate(AppRoute.Questionnaires) },
                    onOpenBadge = { navigator.navigate(AppRoute.Badge) },
                    onOpenAttendance = { navigator.navigate(AppRoute.Attendance) },
                    onOpenStudyPlan = { navigator.navigate(AppRoute.StudyPlan) },
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
            val accountViewModel =
                viewModel(key = "${viewModelKey}_account") { AccountSwitcherViewModel(sessionController, toastSink) }
            val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()
            AccountRemovalDialog(
                state = accountUiState,
                onConfirm = { accountViewModel.confirmAccountRemoval(biometricAuthenticator) },
                onDismiss = accountViewModel::dismissAccountRemoval,
            )
            SettingsScreen(
                uiState = settingsUiState,
                accountUiState = accountUiState,
                installedVersion = updateUiState.installedVersion,
                updateSubtitle = if (updateUiState.bannerState == com.anto426.uniapp.model.updates.UpdateState.AVAILABLE) {
                    updateUiState.statusText?.let { stringResource(it) } ?: stringResource(Res.string.msg_aggiornamento_disponibile)
                } else {
                    "Versione ${updateUiState.installedVersion.ifBlank { "2.0" }}"
                },
                onSelectAccount = accountViewModel::selectAccount,
                onRemoveAccount = accountViewModel::requestAccountRemoval,
                onAddAccount = accountViewModel::addAccount,
                onOpenInfo = { navigator.navigate(AppRoute.Info) },
                onOpenTheme = { navigator.navigate(AppRoute.Theme) },
                onOpenUpdates = { navigator.navigate(AppRoute.Updates) },
                onOpenDevices = { navigator.navigate(AppRoute.Devices) },
                onOpenLanguage = { navigator.navigate(AppRoute.Language) },
                onOpenContribute = { uriHandler.openUri(com.anto426.unisdk.platform.ProjectInfo.repositoryUrl) },
                onOpenReportBug = { uriHandler.openUri(com.anto426.unisdk.platform.ProjectInfo.issuesUrl) },
                onOpenLogin = accountViewModel::addAccount,
                onSignOut = onSignOut,
                onNotificationsEnabledChange = settingsViewModel::setNotificationsEnabled,
                onBiometricEnabledChange = settingsViewModel::setBiometricEnabled,
                onSubmitBiometricPassword = settingsViewModel::submitBiometricPassword,
                onDismissBiometricPassword = settingsViewModel::dismissBiometricPasswordSetup,
                onRequestSignOut = settingsViewModel::requestSignOut,
                onDismissSignOut = settingsViewModel::dismissSignOut,
            )
        }

        AppRoute.Transcripts,
        -> {
            val transcriptsViewModel = viewModel(key = viewModelKey) { TranscriptsViewModel(dataSource) }
            val transcriptsUiState by transcriptsViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                transcriptsUiState.loadState,
                transcriptsUiState.errorMessage,
                onRetry = { transcriptsViewModel.refresh(force = true) },
                emptyMessage = stringResource(Res.string.ui_state_empty_transcripts),
            ) {
                TranscriptsScreen(
                    uiState = transcriptsUiState,
                    onYearSelected = transcriptsViewModel::selectYear,
                    onExamClick = { exam ->
                        if (exam.code.isNotBlank()) navigator.navigate(AppRoute.CourseDetail(exam.code))
                    },
                )
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
                onReset = onResetTheme,
                onCustomColorSelected = onCustomColorSelected,
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
            ) { TaxesScreen(taxesUiState) }
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
        AppRoute.Statistics -> {
            val statisticsViewModel = viewModel(key = viewModelKey) { StatisticsViewModel(dataSource) }
            val statisticsUiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                statisticsUiState.loadState,
                statisticsUiState.errorMessage,
                onRetry = { statisticsViewModel.refresh(force = true) },
            ) {
                StatisticsScreen(
                    uiState = statisticsUiState,
                    onTabSelected = statisticsViewModel::selectTab,
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
                        navigator.navigate(AppRoute.ContactDetail(contact.email.ifBlank { contact.name }))
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
                )
            }
        }

        AppRoute.TransportCatalog -> {
            val catalogViewModel = viewModel(key = viewModelKey) { TransportCatalogViewModel(dataSource) }
            val catalogUiState by catalogViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                catalogUiState.loadState,
                catalogUiState.errorMessage,
                onRetry = { catalogViewModel.refresh(force = true) },
            ) {
                TransportCatalogScreen(
                    uiState = catalogUiState,
                    onTicketClick = { ticket ->
                        navigator.navigate(
                            AppRoute.TicketDetail(
                                ticketId = ticket.id,
                                title = ticket.title,
                            ),
                        )
                    },
                )
            }
        }

        AppRoute.TransportBooking -> {
            val bookingViewModel =
                viewModel(key = viewModelKey) {
                    TransportBookingViewModel(dataSource, toastSink)
                }
            val bookingUiState by bookingViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(bookingUiState.bookedSuccessfully) {
                if (bookingUiState.bookedSuccessfully) {
                    navigator.goBack()
                }
            }
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
        is AppRoute.TicketDetail -> {
            val detailViewModel = viewModel(key = viewModelKey) { TicketDetailViewModel(route.ticketId, dataSource) }
            val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                detailUiState.loadState,
                detailUiState.errorMessage,
                onRetry = { detailViewModel.refresh(force = true) },
            ) {
                detailUiState.ticket?.let { ticket ->
                    TicketDetailScreen(ticket) { navigator.navigate(AppRoute.TransportBooking) }
                }
            }
        }

        is AppRoute.ReservationDetail -> {
            val detailViewModel =
                viewModel(key = viewModelKey) {
                    ReservationDetailViewModel(route.reservationId, dataSource, toastSink)
                }
            val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
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
                        isDeleting = detailUiState.isDeleting,
                        onDelete = detailViewModel::delete,
                    )
                }
            }
        }

        AppRoute.Exams -> {
            val account = (sessionState as? AppSessionState.Authenticated)?.account
            val examsViewModel = viewModel(key = viewModelKey) { ExamsViewModel(dataSource, toastSink, account = account) }
            val examsUiState by examsViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                examsUiState.loadState,
                examsUiState.errorMessage,
                onRetry = { examsViewModel.refresh(force = true) },
                emptyMessage = stringResource(Res.string.ui_state_empty_exams),
            ) {
                ExamsScreen(
                    uiState = examsUiState,
                    onTabSelected = examsViewModel::selectTab,
                    onToggleBooking = examsViewModel::toggleBooking,
                    onAddToCalendar = { examsViewModel.openCalendar(it, uriHandler::openUri) },
                    onProfessorExamClick = { exam ->
                        navigator.navigate(
                            AppRoute.ProfessorExamDetail(
                                itemKey = exam.academicItemKey(),
                                title = exam.title,
                            ),
                        )
                    },
                )
            }
        }
        AppRoute.ExamsHistory -> {
            val historyViewModel = viewModel(key = viewModelKey) { ExamsHistoryViewModel(dataSource) }
            val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                historyUiState.loadState,
                historyUiState.errorMessage,
                onRetry = { historyViewModel.refresh(force = true) },
            ) { ExamsHistoryScreen(historyUiState) }
        }
        AppRoute.StudyPlan -> {
            val studyPlanViewModel = viewModel(key = viewModelKey) { StudyPlanViewModel(dataSource) }
            val studyPlanUiState by studyPlanViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                studyPlanUiState.loadState,
                studyPlanUiState.errorMessage,
                onRetry = { studyPlanViewModel.refresh(force = true) },
                emptyMessage = stringResource(Res.string.ui_state_empty_study_plan),
            ) {
                StudyPlanScreen(
                    uiState = studyPlanUiState,
                    onYearSelected = studyPlanViewModel::selectYear,
                    onCourseClick = { course -> if (course.id.isNotBlank()) navigator.navigate(AppRoute.CourseDetail(course.id)) },
                )
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
                            navigator.navigate(AppRoute.ContactDetail(contact.email.ifBlank { contact.name }))
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
                                title = news.title,
                                description = news.description,
                                fullContent = news.fullContent,
                            ),
                        )
                    },
                )
            }
        }

        is AppRoute.NewsDetail -> {
            NewsDetailScreen(
                title = route.title,
                description = route.description,
                fullContent = route.fullContent,
                onBack = { navigator.goBack() },
            )
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
                onRefresh = { projectViewModel.refresh(force = true) },
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
        "libretto" -> navigate(AppRoute.Transcripts)
        "statistiche" -> navigate(AppRoute.Statistics)
        "media" -> navigate(AppRoute.Grades)
        "appelli" -> navigate(AppRoute.Exams)
        "insegnamenti" -> navigate(AppRoute.Teachings)
        "tesi" -> navigate(AppRoute.Theses)
        "verbali" -> navigate(AppRoute.Reports)
        "didattica", "condivisione" -> selectTopLevel(AppRoute.Didactics)
        "trasporti" -> navigate(AppRoute.Transport)
        "tasse" -> navigate(AppRoute.Taxes)
        "rubrica" -> navigate(AppRoute.Contacts)
        "notifiche" -> navigate(AppRoute.News)
        "sicurezza" -> navigate(AppRoute.Devices)
        "impostazioni" -> selectTopLevel(AppRoute.Settings)
        else -> selectTopLevel(AppRoute.Services)
    }
}
