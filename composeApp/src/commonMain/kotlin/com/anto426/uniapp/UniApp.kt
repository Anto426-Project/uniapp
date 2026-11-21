package com.anto426.uniapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.app.runtime.rememberUniAppRuntime
import com.anto426.uniapp.feedback.runtime.AppToastHost
import com.anto426.uniapp.feedback.runtime.AppToastManager
import com.anto426.uniapp.navigation.ui.AppNavigationHost
import com.anto426.uniapp.session.presentation.AppSessionViewModel
import com.anto426.uniapp.settings.presentation.ThemeViewModel
import com.anto426.uniapp.ui.theme.UniTheme
import com.anto426.uniapp.security.biometric.rememberPlatformBiometricAuthenticator
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay

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
        val performance = LocalLiquidGlassPerformance.current
        val toastBackdrop = rememberLayerBackdrop()
        val hasToast = toastManager.liquidState.currentToast != null
        var keepToastBackdrop by remember { mutableStateOf(false) }
        LaunchedEffect(hasToast) {
            if (hasToast) {
                keepToastBackdrop = true
            } else {
                delay(350L)
                keepToastBackdrop = false
            }
        }
        val recordToastBackdrop =
            (hasToast || keepToastBackdrop) &&
                performance.device.supportsRenderEffect

        Box(Modifier.fillMaxSize()) {
            // Record the live app scene only while a toast is visible so toasts refract real
            // content across navigation transitions with zero idle overhead.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (recordToastBackdrop) {
                            Modifier.layerBackdrop(toastBackdrop)
                        } else {
                            Modifier
                        }
                    )
            ) {
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
            }
            AppToastHost(
                manager = toastManager,
                modifier = Modifier.matchParentSize(),
                backdropState = if (recordToastBackdrop) toastBackdrop else emptyBackdrop(),
            )
        }
    }
}
