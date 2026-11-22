package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.data.UniCacheEngine
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.UniProjectManager
import com.anto426.uniapp.data.UniRepository
import com.anto426.uniapp.data.UniSessionController
import com.anto426.uniapp.data.UniSystemDomain
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.navigation.ui.routes.AcademicItemDetailRouteContent
import com.anto426.uniapp.navigation.ui.routes.AcademicSectionRouteContent
import com.anto426.uniapp.navigation.ui.routes.AttendanceRouteContent
import com.anto426.uniapp.navigation.ui.routes.AuthorRouteContent
import com.anto426.uniapp.navigation.ui.routes.BadgeRouteContent
import com.anto426.uniapp.navigation.ui.routes.BiometricPinSetupRouteContent
import com.anto426.uniapp.navigation.ui.routes.ColorLabRouteContent
import com.anto426.uniapp.navigation.ui.routes.ContactDetailRouteContent
import com.anto426.uniapp.navigation.ui.routes.ContactsRouteContent
import com.anto426.uniapp.navigation.ui.routes.CourseDetailRouteContent
import com.anto426.uniapp.navigation.ui.routes.DevicesRouteContent
import com.anto426.uniapp.navigation.ui.routes.DidacticsRouteContent
import com.anto426.uniapp.navigation.ui.routes.ExamsRouteContent
import com.anto426.uniapp.navigation.ui.routes.GradesRouteContent
import com.anto426.uniapp.navigation.ui.routes.HomeRouteContent
import com.anto426.uniapp.navigation.ui.routes.InfoRouteContent
import com.anto426.uniapp.navigation.ui.routes.LibraryBookDetailRouteContent
import com.anto426.uniapp.navigation.ui.routes.LibraryBookingRouteContent
import com.anto426.uniapp.navigation.ui.routes.LibraryRouteContent
import com.anto426.uniapp.navigation.ui.routes.LoginRouteContent
import com.anto426.uniapp.navigation.ui.routes.NewsDetailRouteContent
import com.anto426.uniapp.navigation.ui.routes.NewsRouteContent
import com.anto426.uniapp.navigation.ui.routes.QuestionnaireDetailRouteContent
import com.anto426.uniapp.navigation.ui.routes.QuestionnairesRouteContent
import com.anto426.uniapp.navigation.ui.routes.ReservationDetailRouteContent
import com.anto426.uniapp.navigation.ui.routes.ServicesRouteContent
import com.anto426.uniapp.navigation.ui.routes.SettingsRouteContent
import com.anto426.uniapp.navigation.ui.routes.TaxesRouteContent
import com.anto426.uniapp.navigation.ui.routes.TranscriptsRouteContent
import com.anto426.uniapp.navigation.ui.routes.TransportBookingRouteContent
import com.anto426.uniapp.navigation.ui.routes.TransportRouteContent
import com.anto426.uniapp.notifications.runtime.AppNotificationController
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.LanguageUiState
import com.anto426.uniapp.settings.presentation.SettingsViewModel
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.bootstrap.AppBootstrapScreen
import com.anto426.uniapp.ui.legal.CookiesScreen
import com.anto426.uniapp.ui.legal.PrivacyScreen
import com.anto426.uniapp.ui.legal.TermsScreen
import com.anto426.uniapp.ui.settings.AboutUniAppScreen
import com.anto426.uniapp.ui.settings.LanguageScreen
import com.anto426.uniapp.ui.settings.ThemeScreen
import com.anto426.uniapp.ui.updates.ChangelogRoute
import com.anto426.uniapp.ui.updates.UpdatesScreen
import com.anto426.uniapp.updates.presentation.AppUpdateUiState

internal class AppRouteContext(
    val navigator: AppNavigator,
    val dataSource: UniGateway,
    val sessionState: AppSessionState,
    val accountId: String,
    val activeProfileId: String?,
    val viewModelKey: String,
    val searchQuery: String,
    val isSearchActive: Boolean,
    val toastSink: AppToastSink,
    val uriHandler: UriHandler,
    val systemDomain: UniSystemDomain?,
)

@Composable
internal fun AppRouteContent(
    route: AppRoute,
    reservationDeleteActionHost: ReservationDeleteActionHost,
    attendanceScannerActionHost: AttendanceScannerActionHost,
    newsDetailActionHost: NewsDetailActionHost? = null,
    connectedDevicesActionHost: ConnectedDevicesActionHost? = null,
    navigator: AppNavigator,
    sessionController: UniSessionController,
    accountSwitcherViewModel: AccountSwitcherViewModel,
    accountSwitcherUiState: AccountSwitcherUiState,
    dataSource: UniGateway,
    localDataStore: UniLocalDataStore,
    gradeSimulations: GradeSimulationRepository,
    projectData: UniProjectManager,
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
    onNavBarShowLabelsChanged: (Boolean) -> Unit = {},
    onNavBarAutoHideChanged: (Boolean) -> Unit = {},
    onElementEntranceSelected: (LiquidScreenEntranceAnimation) -> Unit = {},
    onResetTheme: () -> Unit,
    onCustomColorSelected: (Color) -> Unit = {},
    languageUiState: LanguageUiState,
    onLanguageSelected: (String) -> Unit,
    settingsViewModel: SettingsViewModel? = null,
    onSignOut: () -> Unit,
) {
    val activeProfileId = (sessionState as? AppSessionState.Authenticated)?.account?.activeProfileId
    val dataGeneration = (dataSource as? UniRepository)?.generation
        ?: (dataSource as? UniCacheEngine)?.generation ?: 0
    val systemDomain = (dataSource as? UniRepository)?.system
    val viewModelKey = "$accountId|${activeProfileId.orEmpty()}|$dataGeneration|$route"
    val uriHandler = LocalUriHandler.current
    val context = AppRouteContext(
        navigator = navigator,
        dataSource = dataSource,
        sessionState = sessionState,
        accountId = accountId,
        activeProfileId = activeProfileId,
        viewModelKey = viewModelKey,
        searchQuery = searchQuery,
        isSearchActive = isSearchActive,
        toastSink = toastSink,
        uriHandler = uriHandler,
        systemDomain = systemDomain,
    )

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

        AppRoute.Login ->
            LoginRouteContent(
                context = context,
                sessionController = sessionController,
                accountSwitcherViewModel = accountSwitcherViewModel,
                accountSwitcherUiState = accountSwitcherUiState,
                biometricAuthenticator = biometricAuthenticator,
            )

        AppRoute.Home -> HomeRouteContent(context)
        AppRoute.News -> NewsRouteContent(context)
        is AppRoute.NewsDetail -> NewsDetailRouteContent(route, context, newsDetailActionHost)

        AppRoute.Services -> ServicesRouteContent(context)
        AppRoute.Taxes -> TaxesRouteContent(context)
        AppRoute.Contacts -> ContactsRouteContent(context)
        is AppRoute.ContactDetail -> ContactDetailRouteContent(route, context)

        AppRoute.Didactics -> DidacticsRouteContent(context)
        AppRoute.Teachings -> AcademicSectionRouteContent(AcademicSection.Teachings, viewModelKey, dataSource, searchQuery, navigator)
        AppRoute.Theses -> AcademicSectionRouteContent(AcademicSection.Theses, viewModelKey, dataSource, searchQuery, navigator)
        AppRoute.Reports -> AcademicSectionRouteContent(AcademicSection.Reports, viewModelKey, dataSource, searchQuery, navigator)
        is AppRoute.TeachingDetail -> AcademicItemDetailRouteContent(AcademicSection.Teachings, route.itemKey, viewModelKey, dataSource)
        is AppRoute.ProfessorExamDetail -> AcademicItemDetailRouteContent(AcademicSection.ExamRounds, route.itemKey, viewModelKey, dataSource)
        is AppRoute.ThesisDetail -> AcademicItemDetailRouteContent(AcademicSection.Theses, route.itemKey, viewModelKey, dataSource)
        is AppRoute.ReportDetail -> AcademicItemDetailRouteContent(AcademicSection.Reports, route.itemKey, viewModelKey, dataSource)
        is AppRoute.Transcripts -> TranscriptsRouteContent(route, context)
        AppRoute.Grades -> GradesRouteContent(context, gradeSimulations)
        is AppRoute.Exams -> ExamsRouteContent(route, context)
        is AppRoute.CourseDetail -> CourseDetailRouteContent(route, context)
        AppRoute.Questionnaires -> QuestionnairesRouteContent(context)
        is AppRoute.Questionnaire -> QuestionnaireDetailRouteContent(route, context)
        AppRoute.Badge -> BadgeRouteContent(context)
        AppRoute.Attendance -> AttendanceRouteContent(context, attendanceScannerActionHost)

        AppRoute.Transport -> TransportRouteContent(context)
        AppRoute.TransportBooking -> TransportBookingRouteContent(context)
        is AppRoute.ReservationDetail -> ReservationDetailRouteContent(route, context, reservationDeleteActionHost)

        is AppRoute.Library -> LibraryRouteContent(route, context)
        is AppRoute.LibraryBookDetail -> LibraryBookDetailRouteContent(route, context)
        AppRoute.LibraryBooking -> LibraryBookingRouteContent(context)

        AppRoute.Settings ->
            SettingsRouteContent(
                context = context,
                settingsViewModel = settingsViewModel,
                localDataStore = localDataStore,
                biometricAuthenticator = biometricAuthenticator,
                notificationController = notificationController,
                accountSwitcherViewModel = accountSwitcherViewModel,
                accountSwitcherUiState = accountSwitcherUiState,
                updateUiState = updateUiState,
                projectData = projectData,
                onSignOut = onSignOut,
            )

        AppRoute.BiometricPinSetup ->
            BiometricPinSetupRouteContent(
                context = context,
                settingsViewModel = settingsViewModel,
                localDataStore = localDataStore,
                biometricAuthenticator = biometricAuthenticator,
                notificationController = notificationController,
            )

        AppRoute.Info -> InfoRouteContent(context, updateUiState, projectData)
        AppRoute.Theme ->
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
                onNavBarShowLabelsChanged = onNavBarShowLabelsChanged,
                onNavBarAutoHideChanged = onNavBarAutoHideChanged,
                onElementEntranceSelected = onElementEntranceSelected,
                onReset = onResetTheme,
                onNavigateToColorLab = { navigator.navigate(AppRoute.Colors) },
            )

        AppRoute.Colors -> ColorLabRouteContent(localDataStore, onCustomColorSelected)
        AppRoute.Devices -> DevicesRouteContent(context, connectedDevicesActionHost)
        AppRoute.Language -> LanguageScreen(uiState = languageUiState, onLanguageSelected = onLanguageSelected)
        AppRoute.Author -> AuthorRouteContent(context, projectData)
        AppRoute.AboutUniApp ->
            AboutUniAppScreen(
                sections = UniAppInitialData.appInfoSections,
                onReportBug = { uriHandler.openUri(systemDomain?.bugReportUrl() ?: projectData.uiState.value.issuesUrl) },
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
        AppRoute.Changelog -> ChangelogRoute(updateUiState, viewModelKey)
    }
}
