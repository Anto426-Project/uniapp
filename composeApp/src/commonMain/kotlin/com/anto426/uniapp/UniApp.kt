package com.anto426.uniapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.app.runtime.rememberUniAppRuntime
import com.anto426.uniapp.feedback.runtime.AppToastManager
import com.anto426.uniapp.navigation.ui.AppNavigationHost
import com.anto426.uniapp.session.presentation.AppSessionViewModel
import com.anto426.uniapp.security.biometric.rememberPlatformBiometricAuthenticator

@Composable
fun UniApp() {
    val runtime = rememberUniAppRuntime()
    val biometricAuthenticator = rememberPlatformBiometricAuthenticator()
    val toastManager = remember { AppToastManager() }
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

    // Navigation 3 retains ViewModelStore and saveable state by entry. Re-key the complete
    // authenticated subtree so an account switch cancels old jobs and cannot mix UI snapshots.
    key(navigationOwnerKey) {
        AppNavigationHost(
            runtime = runtime,
            sessionViewModel = sessionViewModel,
            sessionState = sessionState,
            biometricAuthenticator = biometricAuthenticator,
            accountSwitcherViewModel = accountSwitcherViewModel,
            toastManager = toastManager,
        )
    }
}
