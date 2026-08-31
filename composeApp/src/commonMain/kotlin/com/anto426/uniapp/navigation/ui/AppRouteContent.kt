package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.auth.presentation.LoginViewModel
import com.anto426.uniapp.didactics.presentation.ExamsViewModel
import com.anto426.uniapp.didactics.presentation.ExamsHistoryViewModel
import com.anto426.uniapp.didactics.presentation.GradesViewModel
import com.anto426.uniapp.didactics.presentation.AttendanceViewModel
import com.anto426.uniapp.didactics.presentation.QuestionnairesViewModel
import com.anto426.uniapp.didactics.presentation.StatisticsViewModel
import com.anto426.uniapp.didactics.presentation.StudyPlanViewModel
import com.anto426.uniapp.didactics.presentation.TranscriptsViewModel
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.news.presentation.NewsViewModel
import com.anto426.uniapp.services.presentation.ContactsViewModel
import com.anto426.uniapp.services.presentation.ServicesViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.settings.presentation.ConnectedDevicesViewModel
import com.anto426.uniapp.settings.presentation.ColorLabViewModel
import com.anto426.uniapp.settings.presentation.LanguageViewModel
import com.anto426.uniapp.settings.presentation.SettingsViewModel
import com.anto426.uniapp.settings.presentation.ThemeViewModel
import com.anto426.uniapp.transport.presentation.TransportBookingViewModel
import com.anto426.uniapp.transport.presentation.TransportCatalogViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.uniapp.updates.presentation.ChangelogViewModel
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.anto426.uniapp.ui.account.AccountSwitcherScreen
import com.anto426.uniapp.ui.auth.LoginScreen
import com.anto426.uniapp.ui.bootstrap.AppBootstrapScreen
import com.anto426.uniapp.ui.data.UiInitialData
import com.anto426.uniapp.ui.didactics.AttendanceScreen
import com.anto426.uniapp.ui.didactics.CourseDetailScreen
import com.anto426.uniapp.ui.didactics.DidacticsScreen
import com.anto426.uniapp.ui.didactics.ExamsHistoryScreen
import com.anto426.uniapp.ui.didactics.ExamsScreen
import com.anto426.uniapp.ui.didactics.GradesScreen
import com.anto426.uniapp.ui.didactics.QuestionnairesScreen
import com.anto426.uniapp.ui.didactics.StatisticsScreen
import com.anto426.uniapp.ui.didactics.StudentIdScreen
import com.anto426.uniapp.ui.didactics.StudyPlanScreen
import com.anto426.uniapp.ui.didactics.TranscriptsScreen
import com.anto426.uniapp.ui.home.dashboard.HomeScreen
import com.anto426.uniapp.ui.legal.CookiesScreen
import com.anto426.uniapp.ui.legal.PrivacyScreen
import com.anto426.uniapp.ui.legal.TermsScreen
import com.anto426.uniapp.ui.news.NewsScreen
import com.anto426.uniapp.ui.services.ContactDetailScreen
import com.anto426.uniapp.ui.services.ContactsScreen
import com.anto426.uniapp.ui.services.ServicesScreen
import com.anto426.uniapp.ui.services.TaxesScreen
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
    searchQuery: String,
    isSearchActive: Boolean,
    updateUiState: AppUpdateUiState,
    onRetryUpdate: () -> Unit,
    onOpenUpdate: () -> Unit,
    onSignOut: () -> Unit,
) {
    when (route) {
        AppRoute.Bootstrap -> AppBootstrapScreen(backdropState)

        AppRoute.Login -> {
            val loginViewModel = viewModel { LoginViewModel(sessionController) }
            val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()
            LoginScreen(
                backdropState = backdropState,
                uiState = loginUiState,
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
            val homeViewModel =
                viewModel {
                    HomeDashboardViewModel(
                        news = UiInitialData.homeNews,
                        quickActions = UiInitialData.allQuickActions,
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
                onShowNews = homeViewModel::showNews,
                onDismissNews = homeViewModel::dismissNews,
                onNextNews = homeViewModel::showNextNews,
                onPreviousNews = homeViewModel::showPreviousNews,
                onToggleCustomization = homeViewModel::toggleCustomization,
                onFinishCustomization = homeViewModel::finishCustomization,
                onToggleQuickAction = homeViewModel::toggleQuickAction,
                onQuickActionClick = { actionId ->
                    navigator.openHomeAction(actionId)
                },
            )
        }

        AppRoute.Services -> {
            val servicesViewModel =
                viewModel {
                    ServicesViewModel(
                        studentServices = UiInitialData.studentServices,
                        universityPortals = UiInitialData.universityPortals,
                    )
                }
            val servicesUiState by servicesViewModel.uiState.collectAsStateWithLifecycle()
            ServicesScreen(
                backdropState = backdropState,
                uiState = servicesUiState,
            ) { service ->
                when (service) {
                    "Trasporti" -> navigator.navigate(AppRoute.Transport)
                    "Tasse" -> navigator.navigate(AppRoute.Taxes)
                    "Statistiche" -> navigator.navigate(AppRoute.Statistics)
                    "Rubrica" -> navigator.navigate(AppRoute.Contacts)
                    "Esse3" -> navigator.navigate(AppRoute.Transcripts)
                }
            }
        }

        AppRoute.Didactics ->
            DidacticsScreen(
                backdropState,
                onOpenCareer = { navigator.navigate(AppRoute.Career) },
                onOpenTaxes = { navigator.navigate(AppRoute.Taxes) },
                onOpenGrades = { navigator.navigate(AppRoute.Grades) },
                onOpenStatistics = { navigator.navigate(AppRoute.Statistics) },
                onOpenTranscripts = { navigator.navigate(AppRoute.Transcripts) },
                onOpenExams = { navigator.navigate(AppRoute.Exams) },
                onOpenQuestionnaires = { navigator.navigate(AppRoute.Questionnaires) },
                onOpenBadge = { navigator.navigate(AppRoute.Badge) },
                onOpenAttendance = { navigator.navigate(AppRoute.Attendance) },
                onOpenStudyPlan = { navigator.navigate(AppRoute.StudyPlan) },
            )

        AppRoute.Settings -> {
            val settingsViewModel = viewModel { SettingsViewModel() }
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                backdropState = backdropState,
                uiState = settingsUiState,
                onOpenInfo = { navigator.navigate(AppRoute.Info) },
                onOpenTheme = { navigator.navigate(AppRoute.Theme) },
                onOpenUpdates = { navigator.navigate(AppRoute.Updates) },
                onOpenDevices = { navigator.navigate(AppRoute.Devices) },
                onOpenLanguage = { navigator.navigate(AppRoute.Language) },
                onOpenLogin = { navigator.navigate(AppRoute.Accounts) },
                onSignOut = onSignOut,
                onNotificationsEnabledChange = settingsViewModel::setNotificationsEnabled,
                onBiometricEnabledChange = settingsViewModel::setBiometricEnabled,
                onRequestSignOut = settingsViewModel::requestSignOut,
                onDismissSignOut = settingsViewModel::dismissSignOut,
            )
        }

        AppRoute.Accounts -> {
            val accountViewModel = viewModel { AccountSwitcherViewModel(sessionController) }
            val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()
            AccountSwitcherScreen(
                backdropState = backdropState,
                uiState = accountUiState,
                onSelectAccount = accountViewModel::selectAccount,
                onAddAccount = accountViewModel::addAccount,
            )
        }

        AppRoute.Career,
        AppRoute.Transcripts,
        -> {
            val transcriptsViewModel = viewModel { TranscriptsViewModel(UiInitialData.transcripts) }
            val transcriptsUiState by transcriptsViewModel.uiState.collectAsStateWithLifecycle()
            TranscriptsScreen(
                backdropState = backdropState,
                uiState = transcriptsUiState,
                onYearSelected = transcriptsViewModel::selectYear,
            )
        }

        AppRoute.Info ->
            AppInfoScreen(
                backdropState,
                onOpenPrivacy = { navigator.navigate(AppRoute.Privacy) },
                onOpenTerms = { navigator.navigate(AppRoute.Terms) },
                onOpenCookies = { navigator.navigate(AppRoute.Cookies) },
                onOpenAuthor = { navigator.navigate(AppRoute.Author) },
            )

        AppRoute.Theme -> {
            val themeViewModel = viewModel { ThemeViewModel() }
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
            val colorLabViewModel = viewModel { ColorLabViewModel() }
            val colorLabUiState by colorLabViewModel.uiState.collectAsStateWithLifecycle()
            ColorLabScreen(
                backdropState = backdropState,
                uiState = colorLabUiState,
                onColorSelected = colorLabViewModel::selectColor,
            )
        }
        AppRoute.Taxes -> {
            val taxesViewModel = viewModel { TaxesViewModel(UiInitialData.taxPayments) }
            val taxesUiState by taxesViewModel.uiState.collectAsStateWithLifecycle()
            TaxesScreen(backdropState, taxesUiState)
        }
        AppRoute.Grades -> {
            val gradesViewModel =
                viewModel {
                    GradesViewModel(
                        currentExams = UiInitialData.currentGradeExams,
                        simulationPresets = UiInitialData.gradeSimulation,
                    )
                }
            val gradesUiState by gradesViewModel.uiState.collectAsStateWithLifecycle()
            GradesScreen(
                backdropState = backdropState,
                uiState = gradesUiState,
                onTabSelected = gradesViewModel::selectTab,
                onSimulatedGradeChanged = gradesViewModel::updateSimulatedGrade,
            )
        }
        AppRoute.Statistics -> {
            val statisticsViewModel = viewModel { StatisticsViewModel() }
            val statisticsUiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()
            StatisticsScreen(
                backdropState = backdropState,
                uiState = statisticsUiState,
                onTabSelected = statisticsViewModel::selectTab,
            )
        }

        AppRoute.Contacts -> {
            val contactsViewModel = viewModel { ContactsViewModel(UiInitialData.contacts) }
            val contactsUiState by contactsViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(searchQuery, isSearchActive) {
                contactsViewModel.updateSearchQuery(
                    searchQuery.takeIf { isSearchActive }.orEmpty(),
                )
            }
            ContactsScreen(
                backdropState = backdropState,
                uiState = contactsUiState,
                onCategorySelected = contactsViewModel::selectCategory,
                onContactClick = { contact ->
                    navigator.navigate(AppRoute.ContactDetail(contact.email))
                },
            )
        }

        is AppRoute.ContactDetail -> {
            val contact = UiInitialData.contacts.firstOrNull { it.email == route.contactId }
            if (contact == null) InvalidDetailRoute(navigator) else ContactDetailScreen(contact, backdropState)
        }

        AppRoute.Transport -> {
            val transportViewModel = viewModel { TransportViewModel(UiInitialData.myTransportReservations) }
            val transportUiState by transportViewModel.uiState.collectAsStateWithLifecycle()
            TransportScreen(
                backdropState = backdropState,
                uiState = transportUiState,
                onReservationClick = { reservation ->
                    navigator.navigate(AppRoute.ReservationDetail(reservation.id))
                },
            )
        }

        AppRoute.TransportCatalog -> {
            val catalogViewModel = viewModel { TransportCatalogViewModel(UiInitialData.availableTickets) }
            val catalogUiState by catalogViewModel.uiState.collectAsStateWithLifecycle()
            TransportCatalogScreen(
                backdropState = backdropState,
                uiState = catalogUiState,
                onTicketClick = { ticket -> navigator.navigate(AppRoute.TicketDetail(ticket.id)) },
            )
        }

        AppRoute.TransportBooking -> {
            val bookingViewModel =
                viewModel {
                    TransportBookingViewModel(UiInitialData.transportRoutes.map { it.route })
                }
            val bookingUiState by bookingViewModel.uiState.collectAsStateWithLifecycle()
            TransportBookingScreen(
                backdropState = backdropState,
                uiState = bookingUiState,
                onRouteSelected = bookingViewModel::selectRoute,
            )
        }
        is AppRoute.TicketDetail -> {
            val ticket = UiInitialData.availableTickets.firstOrNull { it.id == route.ticketId }
            if (ticket == null) InvalidDetailRoute(navigator) else TicketDetailScreen(ticket, backdropState)
        }

        is AppRoute.ReservationDetail -> {
            val reservation =
                UiInitialData.myTransportReservations.firstOrNull { it.id == route.reservationId }
            if (reservation == null) {
                InvalidDetailRoute(navigator)
            } else {
                ReservationDetailScreen(reservation, backdropState)
            }
        }

        AppRoute.Exams -> {
            val examsViewModel = viewModel { ExamsViewModel(UiInitialData.examSessions) }
            val examsUiState by examsViewModel.uiState.collectAsStateWithLifecycle()
            ExamsScreen(
                backdropState = backdropState,
                uiState = examsUiState,
                onTabSelected = examsViewModel::selectTab,
            )
        }
        AppRoute.ExamsHistory -> {
            val historyViewModel = viewModel { ExamsHistoryViewModel(UiInitialData.pastExams) }
            val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
            ExamsHistoryScreen(backdropState, historyUiState)
        }
        AppRoute.StudyPlan -> {
            val studyPlanViewModel = viewModel { StudyPlanViewModel(UiInitialData.studyPlan) }
            val studyPlanUiState by studyPlanViewModel.uiState.collectAsStateWithLifecycle()
            StudyPlanScreen(
                backdropState = backdropState,
                uiState = studyPlanUiState,
                onYearSelected = studyPlanViewModel::selectYear,
                onCourseClick = { course -> navigator.navigate(AppRoute.CourseDetail(course.id)) },
            )
        }

        is AppRoute.CourseDetail -> {
            val course =
                UiInitialData.studyPlan
                    .asSequence()
                    .flatMap { it.courses.asSequence() }
                    .firstOrNull { it.id == route.courseId }
            if (course == null) InvalidDetailRoute(navigator) else CourseDetailScreen(course, backdropState)
        }

        AppRoute.Questionnaires -> {
            val questionnairesViewModel = viewModel { QuestionnairesViewModel(UiInitialData.questionnaires) }
            val questionnairesUiState by questionnairesViewModel.uiState.collectAsStateWithLifecycle()
            QuestionnairesScreen(backdropState, questionnairesUiState)
        }
        AppRoute.Badge -> StudentIdScreen(backdropState)
        AppRoute.Attendance -> {
            val attendanceViewModel = viewModel { AttendanceViewModel(UiInitialData.attendance) }
            val attendanceUiState by attendanceViewModel.uiState.collectAsStateWithLifecycle()
            AttendanceScreen(backdropState, attendanceUiState)
        }
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
            val changelogViewModel = viewModel { ChangelogViewModel(UiInitialData.changelog) }
            val changelogUiState by changelogViewModel.uiState.collectAsStateWithLifecycle()
            ChangelogScreen(
                backdropState = backdropState,
                uiState = changelogUiState,
                onExpansionChanged = changelogViewModel::setExpanded,
            )
        }
        AppRoute.News -> {
            val newsViewModel =
                viewModel {
                    NewsViewModel(
                        listOf(
                            UiInitialData.universityNews,
                            UiInitialData.departmentNews,
                            UiInitialData.eventNews,
                        ),
                    )
                }
            val newsUiState by newsViewModel.uiState.collectAsStateWithLifecycle()
            NewsScreen(
                backdropState = backdropState,
                uiState = newsUiState,
                onTabSelected = newsViewModel::selectTab,
                onNewsSelected = newsViewModel::showNews,
                onDismissNews = newsViewModel::dismissNews,
            )
        }

        AppRoute.Devices -> {
            val devicesViewModel = viewModel { ConnectedDevicesViewModel(UiInitialData.devices) }
            val devicesUiState by devicesViewModel.uiState.collectAsStateWithLifecycle()
            ConnectedDevicesScreen(
                backdropState = backdropState,
                uiState = devicesUiState,
                onRequestRevocation = devicesViewModel::requestRevocation,
                onDismissRevocation = devicesViewModel::dismissRevocation,
                onConfirmRevocation = devicesViewModel::confirmRevocation,
            )
        }

        AppRoute.Language -> {
            val languageViewModel = viewModel { LanguageViewModel(UiInitialData.languages) }
            val languageUiState by languageViewModel.uiState.collectAsStateWithLifecycle()
            LanguageScreen(
                backdropState = backdropState,
                uiState = languageUiState,
                onLanguageSelected = languageViewModel::selectLanguage,
            )
        }
        AppRoute.Author -> AuthorScreen(backdropState)
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
