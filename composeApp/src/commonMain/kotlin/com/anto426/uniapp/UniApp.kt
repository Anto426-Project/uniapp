package com.anto426.uniapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.liquidmonet.theme.LiquidMonetTheme
import com.anto426.uniapp.app.runtime.rememberUniAppRuntime
import com.anto426.uniapp.navigation.ui.AppNavigationHost
import com.anto426.uniapp.session.presentation.AppSessionViewModel
import com.anto426.uniapp.security.biometric.rememberPlatformBiometricAuthenticator

@Composable
fun UniApp() {
    val runtime = rememberUniAppRuntime()
    val biometricAuthenticator = rememberPlatformBiometricAuthenticator()
    val sessionViewModel = viewModel { AppSessionViewModel(runtime.sessionController) }
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val navigationOwnerKey =
        (sessionState as? com.anto426.uniapp.session.model.AppSessionState.Authenticated)
            ?.account
            ?.let { account -> "${account.accountId}|${account.activeProfileId.orEmpty()}" }
            ?: "public"

    LiquidMonetTheme(useMonetEngine = true, liquidIntensity = .82f) {
        // Navigation 3 retains ViewModelStore and saveable state by entry. Re-key the complete
        // authenticated subtree so an account switch cancels old jobs and cannot mix UI snapshots.
        key(navigationOwnerKey) {
            AppNavigationHost(
                runtime = runtime,
                sessionViewModel = sessionViewModel,
                sessionState = sessionState,
                biometricAuthenticator = biometricAuthenticator,
            )
        }
    }
}
