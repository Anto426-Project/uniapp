package com.anto426.uniapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.app.runtime.rememberUniAppRuntime
import com.anto426.uniapp.feedback.runtime.AppToastHost
import com.anto426.uniapp.feedback.runtime.AppToastManager
import com.anto426.uniapp.navigation.ui.AppNavigationHost
import com.anto426.uniapp.session.presentation.AppSessionViewModel
import com.anto426.uniapp.settings.presentation.ThemeViewModel
import com.anto426.uniapp.ui.theme.UniTheme
import com.anto426.uniapp.security.biometric.rememberPlatformBiometricAuthenticator

@Composable
fun UniApp() {
    val runtime = rememberUniAppRuntime()
    val biometricAuthenticator = rememberPlatformBiometricAuthenticator()
    val toastManager = viewModel { AppToastManager() }
    val themeViewModel = viewModel(key = "app-theme") {
        ThemeViewModel(runtime.localDataStore, toastManager)
    }
    val themeUiState by themeViewModel.uiState.collectAsStateWithLifecycle()
    val sessionViewModel = viewModel { AppSessionViewModel(runtime.sessionController) }
    // Account activation outlives Login and Settings entries, which disappear in Switching.
    val accountSwitcherViewModel = viewModel(key = "app-account-switcher") {
        AccountSwitcherViewModel(runtime.sessionController, toastManager)
    }
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val navigationOwnerKey =
        (sessionState as? com.anto426.uniapp.session.model.AppSessionState.Authenticated)
            ?.account
            ?.let { account -> "${account.accountId}|${account.activeProfileId.orEmpty()}" }
            ?: "public"

    UniTheme(state = themeUiState) {
        Box(Modifier.fillMaxSize()) {
            // Navigation 3 retains ViewModelStore and saveable state by entry.
            key(navigationOwnerKey) {
                AppNavigationHost(
                    runtime = runtime,
                    sessionViewModel = sessionViewModel,
                    sessionState = sessionState,
                    biometricAuthenticator = biometricAuthenticator,
                    accountSwitcherViewModel = accountSwitcherViewModel,
                    toastManager = toastManager,
                    themeViewModel = themeViewModel,
                    themeUiState = themeUiState,
                )
            }
            AppToastHost(manager = toastManager, modifier = Modifier.matchParentSize())
        }
    }
}
