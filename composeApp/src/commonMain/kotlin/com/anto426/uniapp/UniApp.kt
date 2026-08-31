package com.anto426.uniapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.liquidmonet.theme.LiquidMonetTheme
import com.anto426.uniapp.app.runtime.rememberUniAppRuntime
import com.anto426.uniapp.navigation.ui.AppNavigationHost
import com.anto426.uniapp.session.presentation.AppSessionViewModel

@Composable
fun UniApp() {
    val runtime = rememberUniAppRuntime()
    val sessionViewModel = viewModel { AppSessionViewModel(runtime.sessionController) }
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()

    LiquidMonetTheme(useMonetEngine = true, liquidIntensity = .82f) {
        AppNavigationHost(
            runtime = runtime,
            sessionViewModel = sessionViewModel,
            sessionState = sessionState,
        )
    }
}
