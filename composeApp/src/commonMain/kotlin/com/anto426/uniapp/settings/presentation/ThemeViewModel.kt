package com.anto426.uniapp.settings.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.settings.ThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThemeUiState(
    val themes: List<ThemeOption> = defaultThemes,
    val selectedThemeIndex: Int = 0,
    val backgroundStyles: List<String> = defaultBackgroundStyles,
    val selectedBackgroundStyle: String = defaultBackgroundStyles.first(),
    val glassIntensity: Float = 0.82f,
    val effectSpeed: Float = 0.28f,
) {
    companion object {
        val defaultBackgroundStyles = listOf("Aurora", "Mesh Glow", "Orbital Pulse", "Radiant Beam")
        val defaultThemes =
            listOf(
                ThemeOption("Material You", "Dinamico", null),
                ThemeOption("Sapphire", "Cristallo", Color(0xFF4A90D9)),
                ThemeOption("Emerald", "Natura", Color(0xFF2ECC71)),
                ThemeOption("Personalizzato", "Tuo Stile", null, isCustom = true),
            )
    }
}

class ThemeViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = mutableUiState.asStateFlow()

    fun selectTheme(index: Int): Boolean {
        val selected = mutableUiState.value.themes.getOrNull(index) ?: return false
        mutableUiState.value = mutableUiState.value.copy(selectedThemeIndex = index)
        return selected.isCustom
    }

    fun selectBackgroundStyle(style: String) {
        if (style !in mutableUiState.value.backgroundStyles) return
        mutableUiState.value = mutableUiState.value.copy(selectedBackgroundStyle = style)
    }

    fun setGlassIntensity(value: Float) {
        mutableUiState.value = mutableUiState.value.copy(glassIntensity = value.coerceIn(0f, 1f))
    }

    fun setEffectSpeed(value: Float) {
        mutableUiState.value = mutableUiState.value.copy(effectSpeed = value.coerceIn(0f, 1f))
    }
}
