package com.anto426.uniapp.settings.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.liquidmonet.theme.monet.LiquidMonetPresets
import com.anto426.liquidmonet.theme.monet.LiquidMonetSeed
import com.anto426.uniapp.data.local.LocalDataKey
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.model.settings.ThemeOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppThemeMode {
    System,
    Light,
    Dark
}

data class ThemeUiState(
    val themes: List<ThemeOption> = defaultThemes,
    val selectedThemeIndex: Int = 0,
    val themeMode: AppThemeMode = AppThemeMode.System,
    val backgroundStyles: List<String> = defaultBackgroundStyles,
    val selectedBackgroundStyle: String = defaultBackgroundStyles.first(),
    val reducedMotion: Boolean = false,
    val customColor: Color? = null
) {
    companion object {
        val defaultBackgroundStyles = listOf("Aurora", "Mesh Glow", "Orbital Pulse", "Radiant Beam")
        val defaultThemes = buildThemes(null)

        fun buildThemes(customColor: Color?): List<ThemeOption> = listOf(
            ThemeOption("Material You", "Dinamico", null),
            ThemeOption("Sapphire", "Cristallo", Color(0xFF4A90D9)),
            ThemeOption("Emerald", "Natura", Color(0xFF2ECC71)),
            ThemeOption("Sunset", "Caldo", Color(0xFFE67E22)),
            ThemeOption("Violet", "Ametista", Color(0xFF9B59B6)),
            ThemeOption("Personalizzato", "Colore su misura", customColor ?: Color(0xFF2979FF), isCustom = true)
        )
    }

    fun resolveMonetSeed(): LiquidMonetSeed {
        val isCustomTheme = selectedThemeIndex == 5 || (themes.getOrNull(selectedThemeIndex)?.isCustom == true)
        return when {
            isCustomTheme -> {
                val custom = customColor ?: themes.getOrNull(selectedThemeIndex)?.color ?: Color(0xFF2979FF)
                LiquidMonetSeed.fromColor(custom)
            }
            selectedThemeIndex == 2 -> LiquidMonetPresets.Emerald
            selectedThemeIndex == 3 -> LiquidMonetPresets.Sunset
            selectedThemeIndex == 4 -> LiquidMonetPresets.Violet
            else -> LiquidMonetPresets.Sapphire
        }
    }
}

class ThemeViewModel(
    private val localDataStore: UniLocalDataStore,
    private val toastSink: AppToastSink = AppToastSink.None
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val current = mutableUiState.value
                val rawColor = localDataStore.read(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor)
                val loadedCustomColor = rawColor?.let(::parseStoredColor)
                val themes = ThemeUiState.buildThemes(loadedCustomColor)
                val savedThemeIndex =
                    localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection)
                        .coerceIn(themes.indices)

                mutableUiState.value = current.copy(
                    themes = themes,
                    customColor = loadedCustomColor,
                    selectedThemeIndex = savedThemeIndex,
                    themeMode = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeMode)
                        .let(::parseThemeMode)
                        ?: current.themeMode,
                    selectedBackgroundStyle = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeBackground)
                        .takeIf { it in current.backgroundStyles }
                        ?: current.selectedBackgroundStyle,
                    reducedMotion = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeReducedMotion),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                // Keep the shell usable if encrypted local storage is temporarily unavailable.
            }
        }
    }

    fun selectTheme(index: Int) {
        val current = mutableUiState.value
        if (current.themes.getOrNull(index) == null) return
        val isCustom = current.themes.getOrNull(index)?.isCustom == true
        val effectiveCustomColor = if (isCustom && current.customColor == null) Color(0xFF2979FF) else current.customColor
        mutableUiState.value = current.copy(
            selectedThemeIndex = index,
            customColor = effectiveCustomColor
        )
        persistPreference(UniAppDataKeys.ThemeSelection, index)
    }

    fun selectCustomColor(color: Color) {
        val current = mutableUiState.value
        val updatedThemes = ThemeUiState.buildThemes(color)
        val customIndex = updatedThemes.indexOfFirst { it.isCustom }.takeIf { it >= 0 } ?: 5
        mutableUiState.value = current.copy(
            themes = updatedThemes,
            customColor = color,
            selectedThemeIndex = customIndex
        )
        viewModelScope.launch {
            try {
                localDataStore.write(
                    LocalDataScope.Application,
                    UniAppDataKeys.ThemeCustomColor,
                    color.value.toString(),
                )
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeSelection, customIndex)
                toastSink.success("Colore personalizzato applicato.")
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error(getString(Res.string.msg_impossibile_salvare_la_personalizzazione))
            }
        }
    }

    fun selectThemeMode(mode: AppThemeMode) {
        mutableUiState.value = mutableUiState.value.copy(themeMode = mode)
        persistPreference(UniAppDataKeys.ThemeMode, mode.name)
    }

    fun selectBackgroundStyle(style: String) {
        if (style !in mutableUiState.value.backgroundStyles) return
        mutableUiState.value = mutableUiState.value.copy(selectedBackgroundStyle = style)
        persistPreference(UniAppDataKeys.ThemeBackground, style)
    }

    fun setReducedMotion(reduced: Boolean) {
        mutableUiState.value = mutableUiState.value.copy(reducedMotion = reduced)
        persistPreference(UniAppDataKeys.ThemeReducedMotion, reduced, showSuccess = false)
    }

    fun reset() {
        val defaults = ThemeUiState()
        mutableUiState.value = defaults
        viewModelScope.launch {
            try {
                localDataStore.write(
                    LocalDataScope.Application,
                    UniAppDataKeys.ThemeSelection,
                    defaults.selectedThemeIndex,
                )
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeMode, defaults.themeMode.name)
                localDataStore.write(
                    LocalDataScope.Application,
                    UniAppDataKeys.ThemeBackground,
                    defaults.selectedBackgroundStyle,
                )
                localDataStore.write(
                    LocalDataScope.Application,
                    UniAppDataKeys.ThemeReducedMotion,
                    defaults.reducedMotion,
                )
                localDataStore.remove(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor)
                toastSink.success("Tema ripristinato.")
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error(getString(Res.string.msg_impossibile_salvare_la_personalizzazione))
            }
        }
    }

    private fun <T> persistPreference(key: LocalDataKey<T>, value: T, showSuccess: Boolean = true) {
        viewModelScope.launch {
            try {
                localDataStore.write(LocalDataScope.Application, key, value)
                if (showSuccess) toastSink.success("Tema aggiornato.")
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error(getString(Res.string.msg_impossibile_salvare_la_personalizzazione))
            }
        }
    }

    private fun parseStoredColor(raw: String): Color? {
        return raw.toULongOrNull()?.let { Color(it) }
            ?: raw.toLongOrNull()?.let { Color(it) }
            ?: raw.toIntOrNull()?.let { Color(it) }
    }

    private fun parseThemeMode(value: String): AppThemeMode? =
        AppThemeMode.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }

}
