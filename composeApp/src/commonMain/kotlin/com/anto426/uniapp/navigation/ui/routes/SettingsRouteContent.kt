package com.anto426.uniapp.navigation.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.auth.presentation.LoginViewModel
import com.anto426.uniapp.data.UniProjectManager
import com.anto426.uniapp.data.UniSessionController
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.ui.AppRouteContext
import com.anto426.uniapp.navigation.ui.ConnectedDevicesActionHost
import com.anto426.uniapp.notifications.runtime.AppNotificationController
import com.anto426.uniapp.project.presentation.ProjectInfoViewModel
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.settings.presentation.ColorLabViewModel
import com.anto426.uniapp.settings.presentation.ConnectedDevicesViewModel
import com.anto426.uniapp.settings.presentation.DeviceSessionsActionViewModel
import com.anto426.uniapp.settings.presentation.SettingsViewModel
import com.anto426.uniapp.ui.account.AccountRemovalDialog
import com.anto426.uniapp.ui.auth.AppPasswordSetupScreen
import com.anto426.uniapp.ui.auth.LoginScreen
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.settings.AppInfoScreen
import com.anto426.uniapp.ui.settings.ColorLabScreen
import com.anto426.uniapp.ui.settings.ConnectedDevicesScreen
import com.anto426.uniapp.ui.settings.CreatorCreditsScreen
import com.anto426.uniapp.ui.settings.SettingsScreen
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun LoginRouteContent(
    context: AppRouteContext,
    sessionController: UniSessionController,
    accountSwitcherViewModel: AccountSwitcherViewModel,
    accountSwitcherUiState: AccountSwitcherUiState,
    biometricAuthenticator: BiometricAuthenticator,
) {
    val loginViewModel =
        viewModel(key = context.viewModelKey) { LoginViewModel(sessionController, context.toastSink) }
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
        onOpenPrivacy = { context.navigator.navigate(AppRoute.Privacy) },
        onOpenTerms = { context.navigator.navigate(AppRoute.Terms) },
    )
}

@Composable
internal fun SettingsRouteContent(
    context: AppRouteContext,
    settingsViewModel: SettingsViewModel?,
    localDataStore: UniLocalDataStore,
    biometricAuthenticator: BiometricAuthenticator,
    notificationController: AppNotificationController,
    accountSwitcherViewModel: AccountSwitcherViewModel,
    accountSwitcherUiState: AccountSwitcherUiState,
    updateUiState: AppUpdateUiState,
    projectData: UniProjectManager,
    onSignOut: () -> Unit,
) {
    val resolvedSettingsViewModel =
        settingsViewModel ?: viewModel(key = context.viewModelKey) {
            SettingsViewModel(
                localDataStore = localDataStore,
                accountId = context.accountId,
                toastSink = context.toastSink,
                biometricAuthenticator = biometricAuthenticator,
                notificationController = notificationController,
            )
        }
    val settingsUiState by resolvedSettingsViewModel.uiState.collectAsStateWithLifecycle()
    AccountRemovalDialog(
        state = accountSwitcherUiState,
        onConfirm = { accountSwitcherViewModel.confirmAccountRemoval(biometricAuthenticator) },
        onDismiss = accountSwitcherViewModel::dismissAccountRemoval,
    )
    SettingsScreen(
        uiState = settingsUiState,
        accountUiState = accountSwitcherUiState,
        installedVersion = updateUiState.installedVersion,
        hasUpdateAvailable = updateUiState.isUpdateAvailable,
        onSelectAccount = accountSwitcherViewModel::selectAccount,
        onRemoveAccount = accountSwitcherViewModel::requestAccountRemoval,
        onAddAccount = accountSwitcherViewModel::addAccount,
        onOpenInfo = { context.navigator.navigate(AppRoute.Info) },
        onOpenTheme = { context.navigator.navigate(AppRoute.Theme) },
        onOpenUpdates = { context.navigator.navigate(AppRoute.Updates) },
        onOpenDevices = { context.navigator.navigate(AppRoute.Devices) },
        onOpenLanguage = { context.navigator.navigate(AppRoute.Language) },
        onOpenContribute = { context.uriHandler.openUri(context.systemDomain?.repositoryUrl ?: projectData.uiState.value.repositoryUrl) },
        onOpenReportBug = { context.uriHandler.openUri(context.systemDomain?.issuesUrl ?: projectData.uiState.value.issuesUrl) },
        onOpenLogin = accountSwitcherViewModel::addAccount,
        onSignOut = onSignOut,
        onNotificationsEnabledChange = resolvedSettingsViewModel::setNotificationsEnabled,
        onBiometricEnabledChange = { enabled ->
            if (enabled && !settingsUiState.biometricEnabled && !settingsUiState.isBiometricAuthenticating) {
                resolvedSettingsViewModel.setBiometricEnabled(true)
                context.navigator.navigate(AppRoute.BiometricPinSetup)
            } else {
                resolvedSettingsViewModel.setBiometricEnabled(enabled)
            }
        },
        onRequestSignOut = resolvedSettingsViewModel::requestSignOut,
        onDismissSignOut = resolvedSettingsViewModel::dismissSignOut,
    )
}

@Composable
internal fun BiometricPinSetupRouteContent(
    context: AppRouteContext,
    settingsViewModel: SettingsViewModel?,
    localDataStore: UniLocalDataStore,
    biometricAuthenticator: BiometricAuthenticator,
    notificationController: AppNotificationController,
) {
    val resolvedSettingsViewModel =
        settingsViewModel ?: viewModel(key = context.viewModelKey) {
            SettingsViewModel(
                localDataStore = localDataStore,
                accountId = context.accountId,
                toastSink = context.toastSink,
                biometricAuthenticator = biometricAuthenticator,
                notificationController = notificationController,
            )
        }
    val settingsUiState by resolvedSettingsViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(settingsUiState.isPasswordSetupVisible, settingsUiState.biometricEnabled) {
        if ((settingsUiState.biometricEnabled || !settingsUiState.isPasswordSetupVisible) &&
            context.navigator.currentRoute == AppRoute.BiometricPinSetup
        ) {
            context.navigator.goBack()
        }
    }
    DisposableEffect(resolvedSettingsViewModel) {
        onDispose {
            resolvedSettingsViewModel.dismissBiometricPasswordSetup()
        }
    }
    AppPasswordSetupScreen(
        isAuthenticating = settingsUiState.isBiometricAuthenticating,
        error = settingsUiState.passwordSetupError,
        onSubmit = resolvedSettingsViewModel::submitBiometricPassword,
        onDismiss = {
            resolvedSettingsViewModel.dismissBiometricPasswordSetup()
            context.navigator.goBack()
        },
        handleBack = false,
    )
}

@Composable
internal fun ColorLabRouteContent(
    localDataStore: UniLocalDataStore,
    onCustomColorSelected: (Color) -> Unit,
) {
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

@Composable
internal fun DevicesRouteContent(
    context: AppRouteContext,
    connectedDevicesActionHost: ConnectedDevicesActionHost?,
) {
    val devicesViewModel =
        viewModel(key = context.viewModelKey) { ConnectedDevicesViewModel(context.dataSource, context.toastSink) }
    val devicesUiState by devicesViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(devicesViewModel) {
        devicesViewModel.refresh(force = true)
    }
    val disconnectAllViewModel =
        viewModel(key = "disconnect-all|${context.viewModelKey}") {
            DeviceSessionsActionViewModel(context.dataSource, context.toastSink)
        }
    val disconnectAllUiState by disconnectAllViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(devicesUiState.canDisconnectAll, disconnectAllUiState.isDisconnecting) {
        if (!devicesUiState.canDisconnectAll && !disconnectAllUiState.isDisconnecting) {
            disconnectAllViewModel.dismissConfirmation()
        }
    }
    LaunchedEffect(disconnectAllUiState.refreshRevision) {
        if (disconnectAllUiState.refreshRevision > 0) {
            devicesViewModel.refresh(force = true)
        }
    }
    val requestDisconnectAll = remember(devicesViewModel, disconnectAllViewModel) {
        {
            if (devicesViewModel.uiState.value.canDisconnectAll) {
                disconnectAllViewModel.requestDisconnectAll()
            }
        }
    }
    val canShowDisconnectAllAction =
        devicesUiState.canDisconnectAll && !disconnectAllUiState.isDisconnecting
    DisposableEffect(connectedDevicesActionHost, canShowDisconnectAllAction, requestDisconnectAll) {
        if (canShowDisconnectAllAction) {
            connectedDevicesActionHost?.register(requestDisconnectAll)
        }
        onDispose {
            connectedDevicesActionHost?.unregister(requestDisconnectAll)
        }
    }
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
            disconnectAllUiState = disconnectAllUiState,
            onDismissDisconnectAll = disconnectAllViewModel::dismissConfirmation,
            onConfirmDisconnectAll = {
                if (devicesViewModel.uiState.value.canDisconnectAll) {
                    disconnectAllViewModel.confirmDisconnectAll()
                } else {
                    disconnectAllViewModel.dismissConfirmation()
                }
            },
        )
    }
}

@Composable
internal fun InfoRouteContent(
    context: AppRouteContext,
    updateUiState: AppUpdateUiState,
    projectData: UniProjectManager,
) {
    AppInfoScreen(
        onReportBug = { context.uriHandler.openUri(context.systemDomain?.bugReportUrl() ?: projectData.uiState.value.issuesUrl) },
        updateUiState = updateUiState,
        installedBuild = context.systemDomain?.currentInstalledBuild() ?: InstalledAppBuild(),
        onOpenSource = { context.uriHandler.openUri(context.systemDomain?.repositoryUrl ?: projectData.uiState.value.repositoryUrl) },
        onOpenAboutUniApp = { context.navigator.navigate(AppRoute.AboutUniApp) },
        onOpenPrivacy = { context.navigator.navigate(AppRoute.Privacy) },
        onOpenTerms = { context.navigator.navigate(AppRoute.Terms) },
        onOpenCookies = { context.navigator.navigate(AppRoute.Cookies) },
        onOpenCreatorCredits = { context.navigator.navigate(AppRoute.Author) },
        onOpenUpdates = { context.navigator.navigate(AppRoute.Updates) },
    )
}

@Composable
internal fun AuthorRouteContent(
    context: AppRouteContext,
    projectData: UniProjectManager,
) {
    val projectViewModel = viewModel(key = "public-project-info") {
        ProjectInfoViewModel(projectData)
    }
    val projectState by projectViewModel.uiState.collectAsStateWithLifecycle()
    CreatorCreditsScreen(
        state = projectState,
        onOpenLink = { url ->
            val parsed = runCatching { Url(url) }.getOrNull()
            if (parsed?.protocol?.name == "https") context.uriHandler.openUri(url)
        },
    )
}
