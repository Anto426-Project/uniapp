package com.anto426.uniapp.settings.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.model.settings.ThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

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

class ThemeViewModel(
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = mutableUiState.value
            mutableUiState.value = current.copy(
                selectedThemeIndex = dataSource.readPreference(THEME_KEY)?.toIntOrNull()?.coerceIn(current.themes.indices) ?: current.selectedThemeIndex,
                selectedBackgroundStyle = dataSource.readPreference(BACKGROUND_KEY)?.takeIf { it in current.backgroundStyles } ?: current.selectedBackgroundStyle,
                glassIntensity = dataSource.readPreference(INTENSITY_KEY)?.toFloatOrNull()?.coerceIn(0f, 1f) ?: current.glassIntensity,
                effectSpeed = dataSource.readPreference(SPEED_KEY)?.toFloatOrNull()?.coerceIn(0f, 1f) ?: current.effectSpeed,
            )
        }
    }

    fun selectTheme(index: Int): Boolean {
        val selected = mutableUiState.value.themes.getOrNull(index) ?: return false
        mutableUiState.value = mutableUiState.value.copy(selectedThemeIndex = index)
        persistPreference(THEME_KEY, index.toString(), "Tema aggiornato.")
        return selected.isCustom
    }

    fun selectBackgroundStyle(style: String) {
        if (style !in mutableUiState.value.backgroundStyles) return
        mutableUiState.value = mutableUiState.value.copy(selectedBackgroundStyle = style)
        persistPreference(BACKGROUND_KEY, style, "Sfondo aggiornato.")
    }

    fun setGlassIntensity(value: Float) {
        mutableUiState.value = mutableUiState.value.copy(glassIntensity = value.coerceIn(0f, 1f))
        viewModelScope.launch { dataSource.writePreference(INTENSITY_KEY, value.coerceIn(0f, 1f).toString()) }
    }

    fun setEffectSpeed(value: Float) {
        mutableUiState.value = mutableUiState.value.copy(effectSpeed = value.coerceIn(0f, 1f))
        viewModelScope.launch { dataSource.writePreference(SPEED_KEY, value.coerceIn(0f, 1f).toString()) }
    }

    private fun persistPreference(key: String, value: String, successMessage: String) {
        viewModelScope.launch {
            try {
                dataSource.writePreference(key, value)
                toastSink.success(successMessage)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error("Impossibile salvare la personalizzazione.")
            }
        }
    }

    private companion object {
        const val THEME_KEY = "theme.selection"
        const val BACKGROUND_KEY = "theme.background"
        const val INTENSITY_KEY = "theme.intensity"
        const val SPEED_KEY = "theme.speed"
    }
}
