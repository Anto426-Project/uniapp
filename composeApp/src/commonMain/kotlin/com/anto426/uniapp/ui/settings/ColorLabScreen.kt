package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.uniapp.settings.presentation.ColorLabUiState
import com.anto426.uniapp.ui.components.display.UniNotice
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.settings.components.ColorLabHeroCard
import com.anto426.uniapp.ui.settings.components.ColorLabHexAndRecentsGroup
import com.anto426.uniapp.ui.settings.components.ColorLabPresetsCard
import com.anto426.uniapp.ui.settings.components.ColorLabSlidersCard
import com.anto426.uniapp.ui.settings.components.ColorLabSpectrumCard
import com.anto426.uniapp.ui.settings.components.ColorLabTab
import com.anto426.uniapp.ui.settings.components.PresetCategory
import com.anto426.uniapp.ui.settings.components.SliderChannelMode
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.Res
import uniapp.composeapp.generated.resources.ui_colors_info

@Composable
fun ColorLabScreen(
    uiState: ColorLabUiState,
    onColorSelected: (Color) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val isDark = colorScheme.surface.luminance() < 0.5f

    val initialColor = remember { uiState.selectedColor }
    val initialHsv = remember { colorToHsv(uiState.selectedColor) }
    var hue by remember { mutableFloatStateOf(initialHsv.hue) }
    var saturation by remember { mutableFloatStateOf(initialHsv.saturation) }
    var brightness by remember { mutableFloatStateOf(initialHsv.value) }

    var activeTabName by rememberSaveable { mutableStateOf(ColorLabTab.Spectrum.name) }
    var sliderModeName by rememberSaveable { mutableStateOf(SliderChannelMode.Hsb.name) }
    var presetCategoryName by rememberSaveable { mutableStateOf(PresetCategory.UniApp.name) }
    val activeTab = remember(activeTabName) { ColorLabTab.valueOf(activeTabName) }
    val sliderMode = remember(sliderModeName) { SliderChannelMode.valueOf(sliderModeName) }
    val presetCategory = remember(presetCategoryName) { PresetCategory.valueOf(presetCategoryName) }

    var hexDraft by remember { mutableStateOf(colorToHex(uiState.selectedColor)) }
    var recentColors by remember { mutableStateOf(listOf(uiState.selectedColor)) }

    fun pushRecent(color: Color) {
        if (recentColors.firstOrNull()?.sameRgbAs(color) == true) return
        recentColors = (listOf(color) + recentColors.filterNot { it.sameRgbAs(color) }).take(8)
    }

    // Keep HSV and HEX synchronized when the color updates from storage or external presets,
    // without snapping hue to 0 when saturation/brightness touch the edges.
    LaunchedEffect(uiState.selectedColor) {
        val currentComputed = hsvToColor(hue, saturation, brightness)
        if (!currentComputed.sameRgbAs(uiState.selectedColor)) {
            val synced = colorToHsv(uiState.selectedColor, fallbackHue = hue)
            hue = synced.hue
            saturation = synced.saturation
            brightness = synced.value
        }
        val formatted = colorToHex(uiState.selectedColor)
        if (!hexDraft.equals(formatted, ignoreCase = true)) {
            hexDraft = formatted
        }
        pushRecent(uiState.selectedColor)
    }

    val activeColor = remember(hue, saturation, brightness) { hsvToColor(hue, saturation, brightness) }
    // Keep as State<Color> so animation ticks stay in Draw phase (kmp-compose-motion / performance).
    val animatedActiveColorState = animateLiquidColorAsState(
        targetValue = activeColor,
        animationSpec = LiquidMotion.snappySpring(),
        label = "colorLabSeed",
    )

    fun commitHsv(nextHue: Float, nextSat: Float, nextVal: Float) {
        hue = nextHue.coerceIn(0f, 360f)
        saturation = nextSat.coerceIn(0f, 1f)
        brightness = nextVal.coerceIn(0f, 1f)
        val color = hsvToColor(hue, saturation, brightness)
        hexDraft = colorToHex(color)
        onColorSelected(color)
    }

    fun commitColor(color: Color) {
        val hsv = colorToHsv(color, fallbackHue = hue)
        hue = hsv.hue
        saturation = hsv.saturation
        brightness = hsv.value
        hexDraft = colorToHex(color)
        pushRecent(color)
        onColorSelected(color)
    }

    UniScreenColumn {
        UniAnimatedEntrance(order = 0) {
            ColorLabHeroCard(
                animatedColorState = animatedActiveColorState,
                rawColor = activeColor,
                hue = hue,
                saturation = saturation,
                brightness = brightness,
                isDark = isDark,
                canReset = !activeColor.sameRgbAs(initialColor),
                onRandomize = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    commitColor(randomHarmoniousColor())
                },
                onReset = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    commitColor(initialColor)
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ColorLabTab.entries.forEach { tab ->
                LiquidChip(
                    label = stringResource(tab.labelRes),
                    selected = activeTab == tab,
                    onClick = {
                        if (activeTab != tab) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            activeTabName = tab.name
                        }
                    },
                )
            }
        }

        UniContentHost(
            targetState = activeTab,
            mode = LiquidSwitcherMode.Tab,
            isForward = { from, to -> to.ordinal >= from.ordinal },
            modifier = Modifier.fillMaxWidth(),
            label = "ColorLabTabs",
        ) { tab ->
            when (tab) {
                ColorLabTab.Spectrum -> {
                    ColorLabSpectrumCard(
                        hue = hue,
                        saturation = saturation,
                        brightness = brightness,
                        activeColor = activeColor,
                        onPressFeedback = {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        },
                        onSaturationBrightnessChanged = { s, v -> commitHsv(hue, s, v) },
                        onHueChanged = { h -> commitHsv(h, saturation, brightness) },
                    )
                }
                ColorLabTab.Adjust -> {
                    ColorLabSlidersCard(
                        sliderMode = sliderMode,
                        onSliderModeChange = { next ->
                            if (sliderMode != next) {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                sliderModeName = next.name
                            }
                        },
                        hue = hue,
                        saturation = saturation,
                        brightness = brightness,
                        activeColor = activeColor,
                        onHsvChanged = ::commitHsv,
                        onRgbChanged = ::commitColor,
                    )
                }
                ColorLabTab.Presets -> {
                    ColorLabPresetsCard(
                        category = presetCategory,
                        onCategorySelected = { next ->
                            if (presetCategory != next) {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                presetCategoryName = next.name
                            }
                        },
                        selectedColor = activeColor,
                        onColorSelected = { color ->
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            commitColor(color)
                        },
                    )
                }
            }
        }

        ColorLabHexAndRecentsGroup(
            hexDraft = hexDraft,
            activeColor = activeColor,
            recentColors = recentColors,
            onHexDraftChanged = { next ->
                val filtered =
                    next.uppercase()
                        .filterIndexed { idx, ch ->
                            (idx == 0 && ch == '#') ||
                                ch in '0'..'9' ||
                                ch in 'A'..'F'
                        }
                        .take(7)
                hexDraft = filtered
                parseHexColor(filtered)?.let { parsed ->
                    if (filtered.removePrefix("#").length == 6) {
                        commitColor(parsed)
                    }
                }
            },
            onApplyHex = { parsed ->
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                commitColor(parsed)
            },
            onSelectRecent = { color ->
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                commitColor(color)
            },
        )

        UniNotice(text = stringResource(Res.string.ui_colors_info))
    }
}

