package com.anto426.uniapp.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.monet.LiquidMonetSeed
import com.anto426.liquidmonet.theme.monet.blend
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.settings.presentation.ColorLabUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.motion.UniMotion
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private enum class ColorLabTab(val labelRes: StringResource) {
    Spectrum(Res.string.ui_colors_tab_spectrum),
    Adjust(Res.string.ui_colors_tab_adjust),
    Presets(Res.string.ui_colors_tab_presets),
}

private enum class SliderChannelMode(val label: String) {
    Hsb("HSB"),
    Rgb("RGB"),
}

private enum class PresetCategory(val labelRes: StringResource) {
    UniApp(Res.string.ui_colors_cat_uniapp),
    Vibrant(Res.string.ui_colors_cat_vibrant),
    Pastel(Res.string.ui_colors_cat_pastel),
    Elegant(Res.string.ui_colors_cat_elegant),
}

private data class ColorLabPreset(
    val name: String,
    val color: Color,
)

private val SpectrumHueStops =
    listOf(
        Color(0xFFFF0000),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF00FFFF),
        Color(0xFF0000FF),
        Color(0xFFFF00FF),
        Color(0xFFFF0000),
    )

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
    val animatedActiveColorState = animateColorAsState(targetValue = activeColor, label = "colorLabSeed")

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

        LiquidAnimatedSwitcher(
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

        LiquidCard(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Info,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 32.dp,
                        iconSize = 17.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        shape = RoundedRectangle(9.dp),
                    ),
                )
                Text(
                    text = stringResource(Res.string.ui_colors_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ColorLabHeroCard(
    animatedColorState: State<Color>,
    rawColor: Color,
    hue: Float,
    saturation: Float,
    brightness: Float,
    isDark: Boolean,
    canReset: Boolean,
    onRandomize: () -> Unit,
    onReset: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val seed = remember(rawColor) { LiquidMonetSeed.fromColor(rawColor) }
    val monetPrimary = if (isDark) seed.darkPrimary else seed.lightPrimary
    val monetSecondary = if (isDark) seed.darkSecondary else seed.lightSecondary
    val monetTertiary = if (isDark) seed.darkTertiary else seed.lightTertiary
    val monetContainer =
        blend(
            monetPrimary,
            if (isDark) Color.Black else Color(0xFFF3F3F6),
            if (isDark) 0.42f else 0.73f,
        )

    val r = (rawColor.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (rawColor.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (rawColor.blue * 255f).roundToInt().coerceIn(0, 255)
    val hexText = remember(rawColor) { colorToHex(rawColor) }
    val swatchShape = remember { RoundedRectangle(20.dp) }

    LiquidCard(
        contentPadding = PaddingValues(18.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(26.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(swatchShape)
                        .drawBehind {
                            val current = animatedColorState.value
                            drawRect(
                                brush = Brush.linearGradient(
                                    listOf(
                                        blend(current, Color.White, 0.16f),
                                        current,
                                        blend(current, Color.Black, 0.18f),
                                    )
                                )
                            )
                        }
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = swatchShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = LiquidIcons.Palette,
                        contentDescription = null,
                        tint = if (rawColor.luminance() > 0.46f) Color.Black.copy(alpha = 0.78f) else Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = hexText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "RGB $r · $g · $b",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "HSB ${hue.roundToInt()}° · ${(saturation * 100f).roundToInt()}% · ${(brightness * 100f).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.ui_colors_monet_preview),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MonetSwatchChip(
                        label = stringResource(Res.string.ui_colors_token_primary),
                        color = monetPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    MonetSwatchChip(
                        label = stringResource(Res.string.ui_colors_token_surface),
                        color = monetContainer,
                        modifier = Modifier.weight(1f),
                    )
                    MonetSwatchChip(
                        label = stringResource(Res.string.ui_colors_token_secondary),
                        color = monetSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    MonetSwatchChip(
                        label = stringResource(Res.string.ui_colors_token_tertiary),
                        color = monetTertiary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LiquidButton(
                    text = stringResource(Res.string.ui_colors_action_random),
                    onClick = onRandomize,
                    variant = LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Small,
                    leadingIcon = {
                        Icon(
                            imageVector = LiquidIcons.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
                LiquidButton(
                    text = stringResource(Res.string.ui_colors_action_reset),
                    onClick = onReset,
                    enabled = canReset,
                    variant = LiquidButtonVariant.Glass,
                    size = LiquidButtonSize.Small,
                    leadingIcon = {
                        Icon(
                            imageVector = LiquidIcons.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MonetSwatchChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val labelColor =
        if (color.luminance() > 0.46f) {
            Color(0xFF14161C)
        } else {
            Color(0xFFF7F8FA)
        }
    val shape = remember { RoundedRectangle(12.dp) }
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(color)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun ColorLabSpectrumCard(
    hue: Float,
    saturation: Float,
    brightness: Float,
    activeColor: Color,
    onPressFeedback: () -> Unit,
    onSaturationBrightnessChanged: (Float, Float) -> Unit,
    onHueChanged: (Float) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val pureHueColor = remember(hue) { hsvToColor(hue, 1f, 1f) }
    val padShape = remember { RoundedRectangle(18.dp) }
    val satBrightLabel = stringResource(Res.string.ui_colors_sat_bright)
    val hueLabel = stringResource(Res.string.ui_colors_hue)

    val currentOnPress by rememberUpdatedState(onPressFeedback)
    val currentOnSatBright by rememberUpdatedState(onSaturationBrightnessChanged)
    val currentOnHue by rememberUpdatedState(onHueChanged)

    LiquidCard(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = satBrightLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = "${(saturation * 100f).roundToInt()}% · ${(brightness * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = colorScheme.onSurfaceVariant,
                )
            }

            // Single-node 2D Saturation × Brightness pad with cached gradient brushes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp)
                    .clip(padShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.24f),
                        shape = padShape,
                    )
                    .semantics { contentDescription = satBrightLabel }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { currentOnPress() },
                            onTap = { offset ->
                                val s = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                val v = 1f - (offset.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                                currentOnSatBright(s, v)
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { currentOnPress() },
                            onDrag = { change, _ ->
                                change.consume()
                                val s = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                val v = 1f - (change.position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                                currentOnSatBright(s, v)
                            },
                        )
                    }
                    .drawWithCache {
                        val satBrush = Brush.horizontalGradient(listOf(Color.White, pureHueColor))
                        val valBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
                        val outerRadius = 12.dp.toPx()
                        val shadowRadius = outerRadius + 2.dp.toPx()
                        val innerRadius = outerRadius - 2.dp.toPx()
                        val stroke = Stroke(width = 2.5.dp.toPx())
                        onDrawBehind {
                            drawRect(brush = satBrush)
                            drawRect(brush = valBrush)
                            val cx = (saturation * size.width).coerceIn(0f, size.width)
                            val cy = ((1f - brightness) * size.height).coerceIn(0f, size.height)
                            val center = Offset(cx, cy)
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.35f),
                                radius = shadowRadius,
                                center = center,
                            )
                            drawCircle(
                                color = activeColor,
                                radius = innerRadius,
                                center = center,
                            )
                            drawCircle(
                                color = Color.White,
                                radius = outerRadius,
                                center = center,
                                style = stroke,
                            )
                        }
                    },
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = hueLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = "${hue.roundToInt()}°",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                // Single-node continuous rainbow Hue bar with cached spectrum brush
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(Capsule())
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.28f),
                            shape = Capsule(),
                        )
                        .semantics { contentDescription = hueLabel }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { currentOnPress() },
                                onTap = { offset ->
                                    val nextHue = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f) * 360f
                                    currentOnHue(nextHue)
                                },
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { currentOnPress() },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val nextHue = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f) * 360f
                                    currentOnHue(nextHue)
                                },
                            )
                        }
                        .drawWithCache {
                            val hueBrush = Brush.horizontalGradient(SpectrumHueStops)
                            val thumbRadius = 11.dp.toPx()
                            val shadowRadius = thumbRadius + 2.dp.toPx()
                            val innerRadius = thumbRadius - 2.dp.toPx()
                            val stroke = Stroke(width = 2.5.dp.toPx())
                            val minX = thumbRadius + 3.dp.toPx()
                            val maxX = (size.width - thumbRadius - 3.dp.toPx()).coerceAtLeast(minX)
                            onDrawBehind {
                                drawRect(brush = hueBrush)
                                val x = minX + ((hue / 360f).coerceIn(0f, 1f)) * (maxX - minX)
                                val center = Offset(x, size.height / 2f)
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.30f),
                                    radius = shadowRadius,
                                    center = center,
                                )
                                drawCircle(
                                    color = pureHueColor,
                                    radius = innerRadius,
                                    center = center,
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = thumbRadius,
                                    center = center,
                                    style = stroke,
                                )
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun ColorLabSlidersCard(
    sliderMode: SliderChannelMode,
    onSliderModeChange: (SliderChannelMode) -> Unit,
    hue: Float,
    saturation: Float,
    brightness: Float,
    activeColor: Color,
    onHsvChanged: (Float, Float, Float) -> Unit,
    onRgbChanged: (Color) -> Unit,
) {
    val pureHueColor = remember(hue) { hsvToColor(hue, 1f, 1f) }
    val rInt = (activeColor.red * 255f).roundToInt().coerceIn(0, 255)
    val gInt = (activeColor.green * 255f).roundToInt().coerceIn(0, 255)
    val bInt = (activeColor.blue * 255f).roundToInt().coerceIn(0, 255)

    LiquidCard(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SliderChannelMode.entries.forEach { mode ->
                    LiquidChip(
                        label = mode.label,
                        selected = sliderMode == mode,
                        onClick = { onSliderModeChange(mode) },
                    )
                }
            }

            UniContentHost(
                targetState = sliderMode,
                modifier = Modifier.fillMaxWidth(),
                transition = UniMotion.inlineContentTransition,
                isForward = { from, to -> to.ordinal >= from.ordinal },
                label = "ColorLabSliderMode",
            ) { mode ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (mode == SliderChannelMode.Hsb) {
                        ChannelSliderRow(
                            label = stringResource(Res.string.ui_colors_hue),
                            valueText = "${hue.roundToInt()}°",
                            value = hue,
                            valueRange = 0f..360f,
                            dotColor = pureHueColor,
                            onValueChange = { onHsvChanged(it, saturation, brightness) },
                        )
                        ChannelSliderRow(
                            label = stringResource(Res.string.ui_colors_saturation),
                            valueText = "${(saturation * 100f).roundToInt()}%",
                            value = saturation,
                            valueRange = 0f..1f,
                            dotColor = activeColor,
                            onValueChange = { onHsvChanged(hue, it, brightness) },
                        )
                        ChannelSliderRow(
                            label = stringResource(Res.string.ui_colors_brightness),
                            valueText = "${(brightness * 100f).roundToInt()}%",
                            value = brightness,
                            valueRange = 0f..1f,
                            dotColor = blend(Color.Black, Color.White, brightness),
                            onValueChange = { onHsvChanged(hue, saturation, it) },
                        )
                    } else {
                        ChannelSliderRow(
                            label = stringResource(Res.string.ui_colors_red),
                            valueText = rInt.toString(),
                            value = rInt.toFloat(),
                            valueRange = 0f..255f,
                            dotColor = Color(0xFFE53935),
                            onValueChange = {
                                onRgbChanged(Color(it / 255f, activeColor.green, activeColor.blue, 1f))
                            },
                        )
                        ChannelSliderRow(
                            label = stringResource(Res.string.ui_colors_green),
                            valueText = gInt.toString(),
                            value = gInt.toFloat(),
                            valueRange = 0f..255f,
                            dotColor = Color(0xFF2ECC71),
                            onValueChange = {
                                onRgbChanged(Color(activeColor.red, it / 255f, activeColor.blue, 1f))
                            },
                        )
                        ChannelSliderRow(
                            label = stringResource(Res.string.ui_colors_blue),
                            valueText = bInt.toString(),
                            value = bInt.toFloat(),
                            valueRange = 0f..255f,
                            dotColor = Color(0xFF2979FF),
                            onValueChange = {
                                onRgbChanged(Color(activeColor.red, activeColor.green, it / 255f, 1f))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    dotColor: Color,
    onValueChange: (Float) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(0.75.dp, Color.White.copy(alpha = 0.40f), CircleShape)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colorScheme.onSurfaceVariant,
            )
        }
        LiquidSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            tint = dotColor,
        )
    }
}

@Composable
private fun ColorLabPresetsCard(
    category: PresetCategory,
    onCategorySelected: (PresetCategory) -> Unit,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val uniAppPresets = remember {
        UniAppInitialData.palettes.map { ColorLabPreset(it.name, it.color) } +
            listOf(
                ColorLabPreset("Royal", Color(0xFF0B57D0)),
                ColorLabPreset("Ruby", Color(0xFFE53935)),
                ColorLabPreset("Ocean", Color(0xFF00838F)),
                ColorLabPreset("Forest", Color(0xFF2E7D32)),
            )
    }
    val vibrantPresets = remember {
        listOf(
            ColorLabPreset("Electric", Color(0xFF2979FF)),
            ColorLabPreset("Mint", Color(0xFF00C853)),
            ColorLabPreset("Amber", Color(0xFFFF9100)),
            ColorLabPreset("Coral", Color(0xFFFF1744)),
            ColorLabPreset("Magenta", Color(0xFFD500F9)),
            ColorLabPreset("Cyan", Color(0xFF00B8D4)),
            ColorLabPreset("Gold", Color(0xFFFFB300)),
            ColorLabPreset("Flame", Color(0xFFFF5722)),
        )
    }
    val pastelPresets = remember {
        listOf(
            ColorLabPreset("Sky", Color(0xFF64B5F6)),
            ColorLabPreset("Sage", Color(0xFF81C784)),
            ColorLabPreset("Peach", Color(0xFFFFAB91)),
            ColorLabPreset("Lavender", Color(0xFFB39DDB)),
            ColorLabPreset("Rose", Color(0xFFF48FB1)),
            ColorLabPreset("Aqua", Color(0xFF80DEEA)),
            ColorLabPreset("Sand", Color(0xFFFFD54F)),
            ColorLabPreset("Periwinkle", Color(0xFF9FA8DA)),
        )
    }
    val elegantPresets = remember {
        listOf(
            ColorLabPreset("Slate", Color(0xFF455A64)),
            ColorLabPreset("Indigo", Color(0xFF3949AB)),
            ColorLabPreset("Bordeaux", Color(0xFF880E4F)),
            ColorLabPreset("Pine", Color(0xFF1B5E20)),
            ColorLabPreset("Bronze", Color(0xFF6D4C41)),
            ColorLabPreset("Petrol", Color(0xFF006064)),
            ColorLabPreset("Plum", Color(0xFF4A148C)),
            ColorLabPreset("Graphite", Color(0xFF546E7A)),
        )
    }

    val presets =
        when (category) {
            PresetCategory.UniApp -> uniAppPresets
            PresetCategory.Vibrant -> vibrantPresets
            PresetCategory.Pastel -> pastelPresets
            PresetCategory.Elegant -> elegantPresets
        }
    val tileShape = remember { RoundedRectangle(14.dp) }

    LiquidCard(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PresetCategory.entries.forEach { item ->
                    LiquidChip(
                        label = stringResource(item.labelRes),
                        selected = category == item,
                        onClick = { onCategorySelected(item) },
                    )
                }
            }

            presets.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowItems.forEach { preset ->
                        val isSelected = selectedColor.sameRgbAs(preset.color)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .height(48.dp)
                                    .clip(tileShape)
                                    .background(preset.color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) colorScheme.onSurface else Color.White.copy(alpha = 0.24f),
                                        shape = tileShape,
                                    )
                                    .clickable(role = Role.RadioButton) { onColorSelected(preset.color) }
                                    .semantics {
                                        selected = isSelected
                                        contentDescription = preset.name
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = LiquidIcons.Check,
                                        contentDescription = null,
                                        tint = if (preset.color.luminance() > 0.48f) Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorLabHexAndRecentsGroup(
    hexDraft: String,
    activeColor: Color,
    recentColors: List<Color>,
    onHexDraftChanged: (String) -> Unit,
    onApplyHex: (Color) -> Unit,
    onSelectRecent: (Color) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val parsedColor = remember(hexDraft) { parseHexColor(hexDraft) }
    val previewShape = remember { RoundedRectangle(14.dp) }

    UniInsetGroup(title = stringResource(Res.string.ui_colors_hex_and_recents)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(previewShape)
                        .background(parsedColor ?: activeColor.copy(alpha = 0.35f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.28f),
                            shape = previewShape,
                        )
                )

                LiquidTextField(
                    value = hexDraft,
                    onValueChange = onHexDraftChanged,
                    placeholder = "#2979FF",
                    singleLine = true,
                    maxLength = 7,
                    modifier = Modifier.weight(1f),
                )

                if (parsedColor != null && !parsedColor.sameRgbAs(activeColor)) {
                    LiquidButton(
                        text = stringResource(Res.string.ui_colors_hex_apply),
                        onClick = { onApplyHex(parsedColor) },
                        variant = LiquidButtonVariant.Primary,
                        size = LiquidButtonSize.Small,
                    )
                }
            }

            if (hexDraft.isNotBlank() && parsedColor == null) {
                Text(
                    text = stringResource(Res.string.ui_colors_hex_invalid),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.error,
                )
            }
        }

        if (recentColors.isNotEmpty()) {
            LiquidHorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorScheme.outlineVariant.copy(alpha = 0.25f),
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_colors_recents),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    recentColors.forEach { swatch ->
                        val isSelected = activeColor.sameRgbAs(swatch)
                        val swatchHex = remember(swatch) { colorToHex(swatch) }
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable(role = Role.RadioButton) { onSelectRecent(swatch) }
                                .semantics {
                                    selected = isSelected
                                    contentDescription = swatchHex
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) colorScheme.onSurface else Color.White.copy(alpha = 0.28f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = LiquidIcons.Check,
                                        contentDescription = null,
                                        tint = if (swatch.luminance() > 0.48f) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
