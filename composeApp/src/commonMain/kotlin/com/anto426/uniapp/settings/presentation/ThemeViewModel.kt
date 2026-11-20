package com.anto426.uniapp.settings.presentation

import com.anto426.uniapp.localization.readAppString
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
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
import com.anto426.uniapp.settings.model.PageMotionPreferences
import com.anto426.uniapp.ui.motion.UniMotion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

enum class AppThemeMode {
    System,
    Light,
    Dark,
}

data class ThemeUiState(
    val themes: List<ThemeOption> = defaultThemes,
    val selectedThemeIndex: Int = 0,
    val themeMode: AppThemeMode = AppThemeMode.System,
    val backgroundStyles: List<String> = defaultBackgroundStyles,
    val selectedBackgroundStyle: String = defaultBackgroundStyles.first(),
    val reducedMotion: Boolean = false,
    val pageMotion: PageMotionPreferences = PageMotionPreferences(),
    val customColor: Color? = null,
    val liquidIntensity: Float = DEFAULT_LIQUID_INTENSITY,
    val glassQuality: LiquidGlassQualityTier? = null,
    val backgroundIntensity: Float = DEFAULT_BACKGROUND_INTENSITY,
    val backgroundSpeedFactor: Float = DEFAULT_BACKGROUND_SPEED,
    val showBackgroundVignette: Boolean = true,
) {
    companion object {
        const val DEFAULT_LIQUID_INTENSITY = 0.82f
        const val DEFAULT_BACKGROUND_INTENSITY = 0.90f
        const val DEFAULT_BACKGROUND_SPEED = 1.0f

        val defaultBackgroundStyles = listOf("Aurora", "Mesh Glow", "Orbital Pulse", "Radiant Beam")
        val defaultThemes = buildThemes(null)

        fun buildThemes(customColor: Color?): List<ThemeOption> = listOf(
            ThemeOption(Res.string.ui_theme_palette_dynamic, null),
            ThemeOption(Res.string.ui_theme_palette_sapphire, Color(0xFF4A90D9)),
            ThemeOption(Res.string.ui_theme_palette_emerald, Color(0xFF2ECC71)),
            ThemeOption(Res.string.ui_theme_palette_sunset, Color(0xFFE67E22)),
            ThemeOption(Res.string.ui_theme_palette_violet, Color(0xFF9B59B6)),
            ThemeOption(Res.string.ui_theme_palette_custom, customColor ?: Color(0xFF2979FF), isCustom = true),
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
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = mutableUiState.asStateFlow()
    private val pageMotionWrites = Mutex()
    private var pageMotionChanged = false
    private val sliderJobs = mutableMapOf<String, Job>()

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
                    pageMotion = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemePageMotion)
                        .let(UniMotion::normalized)
                        .let { if (pageMotionChanged) mutableUiState.value.pageMotion else it },
                    liquidIntensity = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeLiquidIntensity)
                        .coerceFiniteIn(0.2f..1f, ThemeUiState.DEFAULT_LIQUID_INTENSITY),
                    glassQuality = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeGlassQuality)
                        ?.let(::parseGlassQuality),
                    backgroundIntensity = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeBackgroundIntensity)
                        .coerceFiniteIn(0.2f..1f, ThemeUiState.DEFAULT_BACKGROUND_INTENSITY),
                    backgroundSpeedFactor = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeBackgroundSpeed)
                        .coerceFiniteIn(0.25f..2.0f, ThemeUiState.DEFAULT_BACKGROUND_SPEED),
                    showBackgroundVignette = localDataStore
                        .read(LocalDataScope.Application, UniAppDataKeys.ThemeBackgroundVignette),
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
            customColor = effectiveCustomColor,
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
            selectedThemeIndex = customIndex,
        )
        viewModelScope.launch {
            try {
                localDataStore.write(
                    LocalDataScope.Application,
                    UniAppDataKeys.ThemeCustomColor,
                    color.value.toString(),
                )
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeSelection, customIndex)
                toastSink.success(readAppString(Res.string.msg_custom_color_applied))
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

    fun setLiquidIntensity(intensity: Float) {
        val normalized = intensity.coerceFiniteIn(0.2f..1f, ThemeUiState.DEFAULT_LIQUID_INTENSITY)
        mutableUiState.value = mutableUiState.value.copy(liquidIntensity = normalized)
        persistDebouncedPreference(UniAppDataKeys.ThemeLiquidIntensity, normalized)
    }

    fun selectGlassQuality(quality: LiquidGlassQualityTier?) {
        mutableUiState.value = mutableUiState.value.copy(glassQuality = quality)
        persistPreference(UniAppDataKeys.ThemeGlassQuality, quality?.name)
    }

    fun setBackgroundIntensity(intensity: Float) {
        val normalized = intensity.coerceFiniteIn(0.2f..1f, ThemeUiState.DEFAULT_BACKGROUND_INTENSITY)
        mutableUiState.value = mutableUiState.value.copy(backgroundIntensity = normalized)
        persistDebouncedPreference(UniAppDataKeys.ThemeBackgroundIntensity, normalized)
    }

    fun setBackgroundSpeedFactor(speed: Float) {
        val normalized = speed.coerceFiniteIn(0.25f..2.0f, ThemeUiState.DEFAULT_BACKGROUND_SPEED)
        mutableUiState.value = mutableUiState.value.copy(backgroundSpeedFactor = normalized)
        persistDebouncedPreference(UniAppDataKeys.ThemeBackgroundSpeed, normalized)
    }

    fun setBackgroundVignette(show: Boolean) {
        mutableUiState.value = mutableUiState.value.copy(showBackgroundVignette = show)
        persistPreference(UniAppDataKeys.ThemeBackgroundVignette, show, showSuccess = false)
    }

    fun selectPageTransition(transition: LiquidSwitcherTransition) {
        if (transition == LiquidSwitcherTransition.None) return
        updatePageMotion(PageMotionPreferences(true, transition.name))
    }

    private fun updatePageMotion(preferences: PageMotionPreferences) {
        pageMotionChanged = true
        mutableUiState.value = mutableUiState.value.copy(pageMotion = preferences)
        viewModelScope.launch {
            try {
                pageMotionWrites.withLock {
                    localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemePageMotion, preferences)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error(getString(Res.string.msg_impossibile_salvare_la_personalizzazione))
            }
        }
    }

    fun reset() {
        sliderJobs.values.forEach { it.cancel() }
        sliderJobs.clear()
        val defaults = ThemeUiState()
        updatePageMotion(defaults.pageMotion)
        mutableUiState.value = defaults
        viewModelScope.launch {
            try {
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeSelection, defaults.selectedThemeIndex)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeMode, defaults.themeMode.name)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeBackground, defaults.selectedBackgroundStyle)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeReducedMotion, defaults.reducedMotion)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeLiquidIntensity, defaults.liquidIntensity)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeGlassQuality, defaults.glassQuality?.name)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeBackgroundIntensity, defaults.backgroundIntensity)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeBackgroundSpeed, defaults.backgroundSpeedFactor)
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.ThemeBackgroundVignette, defaults.showBackgroundVignette)
                localDataStore.remove(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor)
                toastSink.success(readAppString(Res.string.msg_theme_reset))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error(getString(Res.string.msg_impossibile_salvare_la_personalizzazione))
            }
        }
    }

    private fun <T> persistDebouncedPreference(key: LocalDataKey<T>, value: T) {
        sliderJobs.remove(key.name)?.cancel()
        sliderJobs[key.name] = viewModelScope.launch {
            delay(180L)
            try {
                localDataStore.write(LocalDataScope.Application, key, value)
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
                if (showSuccess) toastSink.success(readAppString(Res.string.msg_theme_updated))
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

    private fun parseGlassQuality(value: String): LiquidGlassQualityTier? =
        LiquidGlassQualityTier.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }

    private fun Float.coerceFiniteIn(range: ClosedFloatingPointRange<Float>, fallback: Float): Float =
        if (isFinite()) coerceIn(range) else fallback
}
