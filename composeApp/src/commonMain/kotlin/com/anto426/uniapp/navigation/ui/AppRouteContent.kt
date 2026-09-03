package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.data.UniAppDataSource
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
import com.anto426.uniapp.settings.presentation.LanguageViewModel
import com.anto426.uniapp.settings.presentation.SettingsViewModel
import com.anto426.uniapp.settings.presentation.ThemeViewModel
import com.anto426.uniapp.transport.presentation.TransportBookingViewModel
import com.anto426.uniapp.transport.presentation.TransportCatalogViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.uniapp.transport.presentation.ReservationDetailViewModel
import com.anto426.uniapp.transport.presentation.TicketDetailViewModel
import com.anto426.uniapp.updates.presentation.ChangelogViewModel
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.anto426.uniapp.ui.account.AccountSwitcherScreen
import com.anto426.uniapp.ui.auth.LoginScreen
import com.anto426.uniapp.ui.bootstrap.AppBootstrapScreen
import com.anto426.uniapp.ui.data.UiInitialData
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
import com.anto426.uniapp.ui.settings.AuthorScreen
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
import com.kyant.backdrop.Backdrop

@Composable
internal fun AppRouteContent(
    route: AppRoute,
    backdropState: Backdrop,
    navigator: AppNavigator,
    sessionController: AppSessionController,
    dataSource: UniAppDataSource,
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
    onCancelUnlock: () -> Unit,
    devicesRefreshRevision: Int,
    onRetryUpdate: () -> Unit,
    onOpenUpdate: () -> Unit,
    onSignOut: () -> Unit,
) {
    val viewModelKey = "$accountId|$route"
    val uriHandler = LocalUriHandler.current
    when (route) {
        AppRoute.Bootstrap ->
            AppBootstrapScreen(
                backdropState = backdropState,
                accountName = (sessionState as? AppSessionState.UnlockRequired)?.account?.displayName,
                unlockUiState = unlockUiState,
                onRequestUnlock = onRequestUnlock,
                onCancelUnlock = onCancelUnlock,
            )

        AppRoute.Login -> {
            val loginViewModel =
                viewModel(key = viewModelKey) { LoginViewModel(sessionController, toastSink) }
            val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()
            val accountViewModel =
                viewModel(key = "${viewModelKey}_account") { AccountSwitcherViewModel(sessionController, toastSink) }
            val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()
            LoginScreen(
                backdropState = backdropState,
                uiState = loginUiState,
                accountUiState = accountUiState,
                onSelectAccount = accountViewModel::selectAccount,
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
                            if (account?.isProfessor == true) UiInitialData.professorQuickActions
                            else UiInitialData.allQuickActions,
                        account = account,
                    )
                }
            val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                backdropState = backdropState,
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
                        studentServices = if (isProfessor) UiInitialData.professorServices else UiInitialData.studentServices,
                        universityPortals = if (isProfessor) UiInitialData.professorPortals else UiInitialData.universityPortals,
                    )
                }
            val servicesUiState by servicesViewModel.uiState.collectAsStateWithLifecycle()
            ServicesScreen(
                backdropState = backdropState,
                uiState = servicesUiState,
            ) { service ->
                when (service) {
                    "news" -> navigator.navigate(AppRoute.News)
                    "transport" -> navigator.navigate(AppRoute.Transport)
                    "taxes" -> navigator.navigate(AppRoute.Taxes)
                    "statistics" -> navigator.navigate(AppRoute.Statistics)
                    "contacts", "student-office", "professors" -> navigator.navigate(AppRoute.Contacts)
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
                backdropState = backdropState,
                onRetry = { didacticsViewModel.refresh(force = true) },
            ) {
                DidacticsScreen(
                    backdropState = backdropState,
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
                backdropState = backdropState,
                navigator = navigator,
            )

        AppRoute.Theses ->
            AcademicSectionRouteContent(
                section = AcademicSection.Theses,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                searchQuery = searchQuery,
                backdropState = backdropState,
                navigator = navigator,
            )

        AppRoute.Reports ->
            AcademicSectionRouteContent(
                section = AcademicSection.Reports,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                searchQuery = searchQuery,
                backdropState = backdropState,
                navigator = navigator,
            )

        is AppRoute.TeachingDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.Teachings,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                backdropState = backdropState,
            )

        is AppRoute.ProfessorExamDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.ExamRounds,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                backdropState = backdropState,
            )

        is AppRoute.ThesisDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.Theses,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                backdropState = backdropState,
            )

        is AppRoute.ReportDetail ->
            AcademicItemDetailRouteContent(
                section = AcademicSection.Reports,
                itemKey = route.itemKey,
                viewModelKey = viewModelKey,
                dataSource = dataSource,
                backdropState = backdropState,
            )

        AppRoute.Settings -> {
            val settingsViewModel =
                viewModel(key = viewModelKey) {
                    SettingsViewModel(
                        dataSource = dataSource,
                        toastSink = toastSink,
                        biometricAuthenticator = biometricAuthenticator,
                        notificationController = notificationController,
                    )
                }
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val accountViewModel =
                viewModel(key = "${viewModelKey}_account") { AccountSwitcherViewModel(sessionController, toastSink) }
            val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                backdropState = backdropState,
                uiState = settingsUiState,
                accountUiState = accountUiState,
                onSelectAccount = accountViewModel::selectAccount,
                onAddAccount = accountViewModel::addAccount,
                onOpenInfo = { navigator.navigate(AppRoute.Info) },
                onOpenTheme = { navigator.navigate(AppRoute.Theme) },
                onOpenUpdates = { navigator.navigate(AppRoute.Updates) },
                onOpenDevices = { navigator.navigate(AppRoute.Devices) },
                onOpenLanguage = { navigator.navigate(AppRoute.Language) },
                onOpenLogin = accountViewModel::addAccount,
                onSignOut = onSignOut,
                onNotificationsEnabledChange = settingsViewModel::setNotificationsEnabled,
                onBiometricEnabledChange = settingsViewModel::setBiometricEnabled,
                onRequestSignOut = settingsViewModel::requestSignOut,
                onDismissSignOut = settingsViewModel::dismissSignOut,
            )
        }

        AppRoute.Accounts -> {
            val accountViewModel =
                viewModel(key = viewModelKey) { AccountSwitcherViewModel(sessionController, toastSink) }
            val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()
            AccountSwitcherScreen(
                backdropState = backdropState,
                uiState = accountUiState,
                onSelectAccount = accountViewModel::selectAccount,
                onSelectProfile = accountViewModel::selectProfile,
                onAddAccount = accountViewModel::addAccount,
            )
        }

        AppRoute.Transcripts,
        -> {
            val transcriptsViewModel = viewModel(key = viewModelKey) { TranscriptsViewModel(dataSource) }
            val transcriptsUiState by transcriptsViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                transcriptsUiState.loadState,
                transcriptsUiState.errorMessage,
                backdropState,
                onRetry = { transcriptsViewModel.refresh(force = true) },
                emptyMessage = "Il libretto non contiene esami verbalizzati.",
            ) {
                TranscriptsScreen(
                    backdropState = backdropState,
                    uiState = transcriptsUiState,
                    onYearSelected = transcriptsViewModel::selectYear,
                )
            }
        }

        AppRoute.Info ->
            AppInfoScreen(
                backdropState = backdropState,
                installedVersion = updateUiState.installedVersion,
                onOpenSource = { uriHandler.openUri("https://github.com/Anto426-Project/uniapp") },
                onOpenAboutUniApp = { navigator.navigate(AppRoute.AboutUniApp) },
                onOpenPrivacy = { navigator.navigate(AppRoute.Privacy) },
                onOpenTerms = { navigator.navigate(AppRoute.Terms) },
                onOpenCookies = { navigator.navigate(AppRoute.Cookies) },
                onOpenAuthor = { navigator.navigate(AppRoute.Author) },
            )

        AppRoute.Theme -> {
            val themeViewModel = viewModel(key = viewModelKey) { ThemeViewModel(dataSource, toastSink) }
            val themeUiState by themeViewModel.uiState.collectAsStateWithLifecycle()
            ThemeScreen(
                backdropState = backdropState,
                uiState = themeUiState,
                onThemeSelected = { index ->
                    if (themeViewModel.selectTheme(index)) navigator.navigate(AppRoute.Colors)
                },
                onBackgroundStyleSelected = themeViewModel::selectBackgroundStyle,
                onGlassIntensityChanged = themeViewModel::setGlassIntensity,
                onEffectSpeedChanged = themeViewModel::setEffectSpeed,
            )
        }

        AppRoute.Colors -> {
            val colorLabViewModel = viewModel(key = viewModelKey) { ColorLabViewModel(dataSource) }
            val colorLabUiState by colorLabViewModel.uiState.collectAsStateWithLifecycle()
            ColorLabScreen(
                backdropState = backdropState,
                uiState = colorLabUiState,
                onColorSelected = colorLabViewModel::selectColor,
            )
        }
        AppRoute.Taxes -> {
            val taxesViewModel = viewModel(key = viewModelKey) { TaxesViewModel(dataSource) }
            val taxesUiState by taxesViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                taxesUiState.loadState,
                taxesUiState.errorMessage,
                backdropState,
                onRetry = { taxesViewModel.refresh(force = true) },
                emptyMessage = "Non risultano tasse per questo account.",
            ) { TaxesScreen(backdropState, taxesUiState) }
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
                backdropState,
                onRetry = { gradesViewModel.refresh(force = true) },
            ) {
                GradesScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { statisticsViewModel.refresh(force = true) },
            ) {
                StatisticsScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { contactsViewModel.refresh(force = true) },
            ) {
                ContactsScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { detailViewModel.refresh(force = true) },
            ) { detailUiState.contact?.let { contact -> ContactDetailScreen(contact, backdropState) } }
        }

        AppRoute.Transport -> {
            val transportViewModel = viewModel(key = viewModelKey) { TransportViewModel(dataSource) }
            val transportUiState by transportViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                transportUiState.loadState,
                transportUiState.errorMessage,
                backdropState,
                onRetry = { transportViewModel.refresh(force = true) },
                emptyMessage = "Non ci sono prenotazioni trasporto salvate.",
            ) {
                TransportScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { catalogViewModel.refresh(force = true) },
            ) {
                TransportCatalogScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { bookingViewModel.refresh(force = true) },
                emptyMessage = "Il portale non ha restituito linee prenotabili.",
            ) {
                TransportBookingScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { detailViewModel.refresh(force = true) },
            ) {
                detailUiState.ticket?.let { ticket ->
                    TicketDetailScreen(ticket, backdropState) { navigator.navigate(AppRoute.TransportBooking) }
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
                backdropState,
                onRetry = { detailViewModel.refresh(force = true) },
            ) {
                detailUiState.reservation?.let { reservation ->
                    ReservationDetailScreen(
                        reservation = reservation,
                        backdropState = backdropState,
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
                backdropState,
                onRetry = { examsViewModel.refresh(force = true) },
                emptyMessage = "Non sono disponibili appelli.",
            ) {
                ExamsScreen(
                    backdropState = backdropState,
                    uiState = examsUiState,
                    onTabSelected = examsViewModel::selectTab,
                    onToggleBooking = examsViewModel::toggleBooking,
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
                backdropState,
                onRetry = { historyViewModel.refresh(force = true) },
            ) { ExamsHistoryScreen(backdropState, historyUiState) }
        }
        AppRoute.StudyPlan -> {
            val studyPlanViewModel = viewModel(key = viewModelKey) { StudyPlanViewModel(dataSource) }
            val studyPlanUiState by studyPlanViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                studyPlanUiState.loadState,
                studyPlanUiState.errorMessage,
                backdropState,
                onRetry = { studyPlanViewModel.refresh(force = true) },
                emptyMessage = "Il piano di studi non contiene corsi.",
            ) {
                StudyPlanScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { detailViewModel.refresh(force = true) },
            ) { detailUiState.course?.let { course -> CourseDetailScreen(course, backdropState) } }
        }

        AppRoute.Questionnaires -> {
            val questionnairesViewModel = viewModel(key = viewModelKey) { QuestionnairesViewModel(dataSource) }
            val questionnairesUiState by questionnairesViewModel.uiState.collectAsStateWithLifecycle()
            FeatureStateContent(
                questionnairesUiState.loadState,
                questionnairesUiState.errorMessage,
                backdropState,
                onRetry = { questionnairesViewModel.refresh(force = true) },
                emptyMessage = "Non risultano questionari associati ai corsi.",
            ) {
                QuestionnairesScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = questionnaireViewModel::refresh,
                emptyMessage = "Il questionario non contiene domande.",
            ) {
                QuestionnaireDetailScreen(
                    backdropState = backdropState,
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
                backdropState,
                onRetry = { identityViewModel.refresh(force = true) },
            ) { AcademicIdentityScreen(backdropState, identityUiState) }
        }
        AppRoute.Attendance -> {
            val attendanceViewModel = viewModel(key = viewModelKey) { AttendanceViewModel(dataSource) }
            val attendanceUiState by attendanceViewModel.uiState.collectAsStateWithLifecycle()
            AttendanceScreen(
                backdropState = backdropState,
                uiState = attendanceUiState,
                onRegisterAttendance = { code ->
                    attendanceViewModel.registerAttendance(code)
                },
                onClearRegistrationStatus = {
                    attendanceViewModel.clearRegistrationStatus()
                },
            )
        }
        AppRoute.AboutUniApp -> AboutUniAppScreen(backdropState, UiInitialData.appInfoSections)
        AppRoute.Privacy -> PrivacyScreen(backdropState, UiInitialData.privacySections)
        AppRoute.Terms -> TermsScreen(backdropState, UiInitialData.termsSections)
        AppRoute.Cookies -> CookiesScreen(backdropState, UiInitialData.cookieSections)
        AppRoute.Updates ->
            UpdatesScreen(
                backdropState = backdropState,
                uiState = updateUiState,
                onRetry = onRetryUpdate,
                onOpenUpdate = onOpenUpdate,
                onOpenChangelog = { navigator.navigate(AppRoute.Changelog) },
            )

        AppRoute.Changelog -> {
            val changelogViewModel = viewModel(key = viewModelKey) { ChangelogViewModel(updateUiState) }
            val changelogUiState by changelogViewModel.uiState.collectAsStateWithLifecycle()
            ChangelogScreen(
                backdropState = backdropState,
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
                backdropState,
                onRetry = { newsViewModel.refresh(force = true) },
                emptyMessage = "Non ci sono nuove comunicazioni.",
            ) {
                NewsScreen(
                    backdropState = backdropState,
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
                backdropState = backdropState,
            )
        }

        AppRoute.Devices -> {
            val devicesViewModel =
                viewModel(key = viewModelKey) { ConnectedDevicesViewModel(dataSource, toastSink) }
            val devicesUiState by devicesViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(devicesRefreshRevision) {
                if (devicesRefreshRevision > 0) devicesViewModel.refresh(force = true)
            }
            FeatureStateContent(
                devicesUiState.loadState,
                devicesUiState.errorMessage,
                backdropState,
                onRetry = { devicesViewModel.refresh(force = true) },
            ) {
                ConnectedDevicesScreen(
                    backdropState = backdropState,
                    uiState = devicesUiState,
                    onRequestRevocation = devicesViewModel::requestRevocation,
                    onDismissRevocation = devicesViewModel::dismissRevocation,
                    onConfirmRevocation = devicesViewModel::confirmRevocation,
                )
            }
        }

        AppRoute.Language -> {
            val languageViewModel =
                viewModel(key = viewModelKey) {
                    LanguageViewModel(UiInitialData.languages, dataSource, toastSink)
                }
            val languageUiState by languageViewModel.uiState.collectAsStateWithLifecycle()
            LanguageScreen(
                backdropState = backdropState,
                uiState = languageUiState,
                onLanguageSelected = languageViewModel::selectLanguage,
            )
        }
        AppRoute.Author ->
            AuthorScreen(
                backdropState = backdropState,
                onOpenGitHub = { uriHandler.openUri("https://github.com/Anto426") },
                onOpenProject = { uriHandler.openUri("https://github.com/Anto426-Project/uniapp") },
            )
    }
}

@Composable
private fun AcademicSectionRouteContent(
    section: AcademicSection,
    viewModelKey: String,
    dataSource: UniAppDataSource,
    searchQuery: String,
    backdropState: Backdrop,
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
        backdropState = backdropState,
        onRetry = { sectionViewModel.refresh(force = true) },
        emptyMessage = "Non sono disponibili contenuti in questa sezione.",
    ) {
        AcademicSectionScreen(
            backdropState = backdropState,
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
    backdropState: Backdrop,
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
        backdropState = backdropState,
        onRetry = { detailViewModel.refresh(force = true) },
        emptyMessage = "Il dettaglio selezionato non è più disponibile.",
    ) {
        AcademicItemDetailScreen(
            backdropState = backdropState,
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
