package com.anto426.uniapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.anto426.liquidmonet.theme.LiquidMonetTheme
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.motion.LocalUniContentTransition
import com.anto426.uniapp.ui.motion.UniMotion

/** The single application bridge from persisted appearance preferences to Liquid Monet. */
@Composable
fun UniTheme(
    state: ThemeUiState,
    content: @Composable () -> Unit
) {
    val darkTheme = when (state.themeMode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val monetSeed = state.resolveMonetSeed()

    LiquidMonetTheme(
        darkTheme = darkTheme,
        useMonetEngine = state.selectedThemeIndex == 0,
        customMonetSeed = monetSeed,
        liquidIntensity = 0.82f,
        reduceMotion = state.reducedMotion,
    ) {
        CompositionLocalProvider(LocalUniContentTransition provides UniMotion.effectiveTransition(state.pageMotion)) {
            content()
        }
    }
}
