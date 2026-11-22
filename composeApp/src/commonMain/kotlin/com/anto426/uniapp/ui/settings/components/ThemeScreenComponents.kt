package com.anto426.uniapp.ui.settings.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteOption
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteSelector
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*


internal data class ThemeOptionChoice<T>(
    val value: T,
    val title: String,
    val description: String,
)

/**
 * Single-layer Theme Studio header: live Monet token swatches + active configuration at a glance.
 * Avoids card-in-card glass nesting while preserving clear contrast in both light and dark modes.
 */
@Composable
internal fun ThemeStudioHeaderCard(
    paletteLabel: String,
    modeLabel: String,
    backgroundTitle: String,
    activeQualityLabel: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryState = animateLiquidColorAsState(
        targetValue = colorScheme.primary,
        animationSpec = LiquidMotion.snappySpring(),
        label = "studioPrimary",
    )
    val secondaryState = animateLiquidColorAsState(
        targetValue = colorScheme.secondary,
        animationSpec = LiquidMotion.snappySpring(),
        label = "studioSecondary",
    )
    val tertiaryState = animateLiquidColorAsState(
        targetValue = colorScheme.tertiary,
        animationSpec = LiquidMotion.snappySpring(),
        label = "studioTertiary",
    )
    val cardShape = remember { RoundedRectangle(24.dp) }
    val iconShape = remember { RoundedRectangle(14.dp) }

    LiquidCard(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = LiquidIcons.Palette,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 44.dp,
                    iconSize = 21.dp,
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    shape = iconShape,
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = paletteLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$modeLabel · $backgroundTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LiquidBadge(
                    text = activeQualityLabel,
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    contentColor = colorScheme.primary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TokenSwatchDot(colorState = primaryState)
                    TokenSwatchDot(colorState = secondaryState)
                    TokenSwatchDot(colorState = tertiaryState)
                }
            }
        }
    }
}

@Composable
private fun TokenSwatchDot(colorState: State<Color>) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .drawBehind { drawRect(colorState.value) },
    )
}

@Composable
private fun ThemeSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

/**
 * Tactile 3-up glass theme mode selector (System / Light / Dark) with crisp icon badges.
 */
@Composable
internal fun ThemeModeVisualSection(
    selectedMode: AppThemeMode,
    onThemeModeSelected: (AppThemeMode) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val tileShape = remember { RoundedRectangle(20.dp) }
    val modes = listOf(
        Triple(AppThemeMode.System, stringResource(Res.string.ui_theme_mode_system), ThemeIcons.AutoMode),
        Triple(AppThemeMode.Light, stringResource(Res.string.ui_theme_mode_light), ThemeIcons.LightMode),
        Triple(AppThemeMode.Dark, stringResource(Res.string.ui_theme_mode_dark), ThemeIcons.DarkMode),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ThemeSectionLabel(stringResource(Res.string.ui_theme_mode_group))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            modes.forEach { (mode, title, icon) ->
                val isSelected = selectedMode == mode
                LiquidCard(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 88.dp)
                        .semantics(mergeDescendants = true) {
                            role = Role.RadioButton
                            selected = isSelected
                        },
                    shape = tileShape,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.75.dp,
                        color = if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.25f),
                    ),
                    colors = if (isSelected) {
                        LiquidCardDefaults.colors(containerColor = colorScheme.primary.copy(alpha = 0.12f))
                    } else {
                        LiquidCardDefaults.colors()
                    },
                    onClick = {
                        if (!isSelected) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onThemeModeSelected(mode)
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 36.dp,
                                iconSize = 18.dp,
                                containerColor = if (isSelected) {
                                    colorScheme.primary.copy(alpha = 0.16f)
                                } else {
                                    colorScheme.onSurface.copy(alpha = 0.06f)
                                },
                                shape = CircleShape,
                            ),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ThemeColorGroup(
    uiState: ThemeUiState,
    onThemeSelected: (Int) -> Unit,
    onNavigateToColorLab: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDynamicSelected = uiState.selectedThemeIndex == 0
    val isCustomActive = uiState.selectedThemeIndex == 5 || (uiState.themes.getOrNull(uiState.selectedThemeIndex)?.isCustom == true)
    val customColor = uiState.customColor ?: Color(0xFF0B57D0)
    val fixedThemeEntries = uiState.themes.mapIndexedNotNull { index, option ->
        if (index > 0 && !option.isCustom && option.color != null) {
            index to LiquidPaletteOption(stringResource(option.nameRes), option.color)
        } else {
            null
        }
    }
    val paletteOptions = fixedThemeEntries.map { it.second }
    val selectedPaletteIdx = fixedThemeEntries.indexOfFirst { it.first == uiState.selectedThemeIndex }
    val fallbackIndex = fixedThemeEntries.firstOrNull()?.first ?: 1

    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_selection_title),
    ) {
        UniInsetRow(
            title = stringResource(Res.string.ui_theme_material_you_title),
            subtitle = stringResource(Res.string.ui_theme_material_you_sub),
            icon = ThemeIcons.Sparkles,
            iconTint = colorScheme.primary,
            showDivider = true,
            trailingContent = {
                LiquidSwitch(
                    checked = isDynamicSelected,
                    onCheckedChange = { enabled ->
                        onThemeSelected(if (enabled) 0 else fallbackIndex)
                    },
                )
            },
        )

        LiquidPaletteSelector(
            options = paletteOptions,
            selectedIndex = selectedPaletteIdx,
            onSelectIndex = { paletteIdx ->
                fixedThemeEntries.getOrNull(paletteIdx)?.let { (themeIndex, _) ->
                    onThemeSelected(themeIndex)
                }
            },
            title = null,
            showBadge = false,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )

        LiquidHorizontalDivider(
            modifier = Modifier.padding(start = 64.dp, end = 16.dp),
            color = colorScheme.outlineVariant.copy(alpha = 0.25f),
        )

        UniInsetRow(
            title = stringResource(Res.string.ui_colors_title),
            subtitle = if (isCustomActive) {
                customColor.toHexString()
            } else {
                stringResource(Res.string.ui_colors_subtitle)
            },
            icon = LiquidIcons.Palette,
            iconTint = if (isCustomActive) customColor else colorScheme.primary,
            onClick = onNavigateToColorLab,
            showDivider = false,
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isCustomActive) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(customColor)
                                .border(1.5.dp, colorScheme.onSurface.copy(alpha = 0.25f), CircleShape),
                        )
                    }
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            },
        )
    }
}

@Composable
internal fun ThemeBackgroundGroup(
    backgroundChoices: List<ThemeOptionChoice<LiquidBackgroundEffect>>,
    selectedBackground: ThemeOptionChoice<LiquidBackgroundEffect>,
    onBackgroundStyleSelected: (LiquidBackgroundEffect) -> Unit,
    backgroundIntensity: Float,
    onBackgroundIntensityChanged: (Float) -> Unit,
    backgroundSpeedFactor: Float,
    onBackgroundSpeedChanged: (Float) -> Unit,
    showBackgroundVignette: Boolean,
    onBackgroundVignetteChanged: (Boolean) -> Unit,
    reducedMotion: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    val roundedSpeed = (backgroundSpeedFactor * 10f).roundToInt() / 10f

    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_background_group),
    ) {
        ThemeDropdownRow(
            title = stringResource(Res.string.ui_theme_engine_group),
            selected = selectedBackground,
            items = backgroundChoices,
            icon = ThemeIcons.Wallpaper,
            iconTint = colorScheme.primary,
            onItemSelected = onBackgroundStyleSelected,
            showDivider = true,
        )

        ThemeSliderRow(
            title = stringResource(Res.string.ui_theme_glow_intensity),
            valueLabel = "${(backgroundIntensity * 100f).roundToInt()}%",
            value = backgroundIntensity,
            onValueChange = onBackgroundIntensityChanged,
            valueRange = 0f..1f,
            icon = LiquidIcons.Palette,
            iconTint = colorScheme.primary,
            showDivider = true,
        )
        if (!reducedMotion) {
            ThemeSliderRow(
                title = stringResource(Res.string.ui_theme_anim_speed),
                valueLabel = "${roundedSpeed}x",
                value = backgroundSpeedFactor,
                onValueChange = onBackgroundSpeedChanged,
                valueRange = 0.25f..2.0f,
                icon = ThemeIcons.SlowMotion,
                iconTint = colorScheme.secondary,
                showDivider = true,
            )
        }
        UniInsetRow(
            title = stringResource(Res.string.ui_theme_vignette_title),
            subtitle = stringResource(Res.string.ui_theme_vignette_sub),
            icon = ThemeIcons.Wallpaper,
            iconTint = colorScheme.tertiary,
            showDivider = false,
            trailingContent = {
                LiquidSwitch(checked = showBackgroundVignette, onCheckedChange = onBackgroundVignetteChanged)
            },
        )
    }
}

@Composable
internal fun ThemeGlassCustomizationGroup(
    qualityChoices: List<ThemeOptionChoice<LiquidGlassQualityTier?>>,
    selectedQuality: ThemeOptionChoice<LiquidGlassQualityTier?>,
    effectiveQualityLabel: String,
    onQualitySelected: (LiquidGlassQualityTier?) -> Unit,
    liquidIntensity: Float,
    onLiquidIntensityChanged: (Float) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val selectedQualitySubtitle = if (selectedQuality.value == null) {
        "${selectedQuality.title} · $effectiveQualityLabel"
    } else {
        selectedQuality.title
    }

    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_glass_group),
    ) {
        ThemeDropdownRow(
            title = stringResource(Res.string.ui_theme_glass_quality),
            selected = selectedQuality,
            items = qualityChoices,
            icon = LiquidIcons.Star,
            iconTint = colorScheme.primary,
            onItemSelected = onQualitySelected,
            showDivider = true,
            selectedSubtitle = selectedQualitySubtitle,
        )

        ThemeSliderRow(
            title = stringResource(Res.string.ui_theme_liquid_intensity),
            valueLabel = "${(liquidIntensity * 100f).roundToInt()}%",
            value = liquidIntensity,
            onValueChange = onLiquidIntensityChanged,
            valueRange = 0f..1f,
            icon = ThemeIcons.Sparkles,
            iconTint = colorScheme.secondary,
            showDivider = false,
        )
    }
}

@Composable
internal fun ThemeNavigationBarGroup(
    showLabels: Boolean,
    onShowLabelsChanged: (Boolean) -> Unit,
    autoHide: Boolean,
    onAutoHideChanged: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val previewShape = remember { RoundedRectangle(16.dp) }
    val previewItems = listOf(
        LiquidIcons.Home to stringResource(Res.string.ui_app_name),
        LiquidIcons.MenuBook to stringResource(Res.string.nav_route_didactics_title),
        LiquidIcons.Info to stringResource(Res.string.nav_route_services_title),
        LiquidIcons.Settings to stringResource(Res.string.nav_route_settings_title),
    )

    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_navbar_group),
    ) {
        // Live compact navbar preview (flat surface inside group — zero nested LiquidCard)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .clip(previewShape)
                .background(colorScheme.onSurface.copy(alpha = 0.05f))
                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.22f), previewShape)
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                previewItems.forEachIndexed { idx, (icon, label) ->
                    val active = idx == 0
                    val itemShape = remember { Capsule() }
                    Column(
                        modifier = Modifier
                            .clip(itemShape)
                            .background(if (active) colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (active) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        if (showLabels) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) colorScheme.primary else colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        LiquidHorizontalDivider(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            color = colorScheme.outlineVariant.copy(alpha = 0.22f),
        )

        UniInsetRow(
            title = stringResource(Res.string.ui_theme_navbar_labels_title),
            subtitle = stringResource(Res.string.ui_theme_navbar_labels_sub),
            icon = ThemeIcons.NavigationBar,
            iconTint = colorScheme.primary,
            showDivider = true,
            trailingContent = {
                LiquidSwitch(checked = showLabels, onCheckedChange = onShowLabelsChanged)
            },
        )
        UniInsetRow(
            title = stringResource(Res.string.ui_theme_navbar_autohide_title),
            subtitle = stringResource(Res.string.ui_theme_navbar_autohide_sub),
            icon = ThemeIcons.SlowMotion,
            iconTint = colorScheme.secondary,
            showDivider = false,
            trailingContent = {
                LiquidSwitch(checked = autoHide, onCheckedChange = onAutoHideChanged)
            },
        )
    }
}

@Composable
internal fun ThemeAnimationsGroup(
    motionChoices: List<ThemeOptionChoice<LiquidSwitcherTransition>>,
    selectedMotion: ThemeOptionChoice<LiquidSwitcherTransition>,
    onTransitionSelected: (LiquidSwitcherTransition) -> Unit,
    entranceChoices: List<ThemeOptionChoice<LiquidScreenEntranceAnimation>>,
    selectedEntrance: ThemeOptionChoice<LiquidScreenEntranceAnimation>,
    onEntranceSelected: (LiquidScreenEntranceAnimation) -> Unit,
    reducedMotion: Boolean,
    onReducedMotionChanged: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_animations_group),
    ) {
        UniInsetRow(
            title = stringResource(Res.string.ui_theme_reduced_motion_title),
            subtitle = stringResource(Res.string.ui_theme_reduced_motion_sub),
            icon = ThemeIcons.SlowMotion,
            iconTint = colorScheme.tertiary,
            showDivider = true,
            trailingContent = {
                LiquidSwitch(checked = reducedMotion, onCheckedChange = onReducedMotionChanged)
            },
        )
        if (reducedMotion) {
            Text(
                text = stringResource(Res.string.ui_theme_page_motion_reduced_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        } else {
            ThemeDropdownRow(
                title = stringResource(Res.string.ui_theme_page_motion_style),
                selected = selectedMotion,
                items = motionChoices,
                icon = ThemeIcons.PageTransition,
                iconTint = colorScheme.primary,
                onItemSelected = onTransitionSelected,
                showDivider = false,
            )
            ThemeTransitionPreviewRow(
                transition = selectedMotion.value,
                transitionName = selectedMotion.title,
            )
            LiquidHorizontalDivider(
                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                color = colorScheme.outlineVariant.copy(alpha = 0.25f),
            )
            val showEntrancePreview = selectedEntrance.value != LiquidScreenEntranceAnimation.None
            ThemeDropdownRow(
                title = stringResource(Res.string.ui_theme_element_entrance_title),
                selected = selectedEntrance,
                items = entranceChoices,
                icon = ThemeIcons.ElementEntrance,
                iconTint = colorScheme.secondary,
                onItemSelected = onEntranceSelected,
                showDivider = false,
            )
            if (showEntrancePreview) {
                ThemeEntrancePreviewRow(
                    animation = selectedEntrance.value,
                )
            }
        }
    }
}

@Composable
private fun ThemeTransitionPreviewRow(
    transition: LiquidSwitcherTransition,
    transitionName: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    var previewPage by remember { mutableIntStateOf(0) }
    var forward by remember { mutableStateOf(true) }
    val surfaceShape = remember { RoundedRectangle(16.dp) }
    val iconShape = remember { RoundedRectangle(10.dp) }
    val pageTitles = listOf(
        stringResource(Res.string.ui_home_career_eyebrow),
        stringResource(Res.string.ui_career_open_stats),
        stringResource(Res.string.ui_settings),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.ui_theme_transition_preview_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidIconButton(
                    icon = LiquidIcons.ChevronLeft,
                    contentDescription = stringResource(Res.string.ui_theme_transition_preview_prev),
                    size = 44.dp,
                    iconSize = 18.dp,
                    variant = LiquidIconButtonVariant.Ghost,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        forward = false
                        previewPage = (previewPage + 2) % 3
                    },
                )
                LiquidIconButton(
                    icon = LiquidIcons.ChevronRight,
                    contentDescription = stringResource(Res.string.ui_theme_transition_preview_next),
                    size = 44.dp,
                    iconSize = 18.dp,
                    variant = LiquidIconButtonVariant.Ghost,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        forward = true
                        previewPage = (previewPage + 1) % 3
                    },
                )
            }
        }

        UniContentHost(
            targetState = previewPage,
            transition = transition,
            mode = LiquidSwitcherMode.Screen,
            isForward = { _, _ -> forward },
            suppressElementEntrance = false,
            modifier = Modifier.fillMaxWidth(),
            label = "themeTransitionPreview",
        ) { page ->
            val icon = when (page) {
                0 -> LiquidIcons.Home
                1 -> LiquidIcons.Analytics
                else -> LiquidIcons.Settings
            }
            val accent = when (page) {
                0 -> colorScheme.primary
                1 -> colorScheme.tertiary
                else -> colorScheme.secondary
            }
            // Flat tinted preview container (no nested LiquidCard glass layer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(surfaceShape)
                    .background(accent.copy(alpha = 0.08f))
                    .border(1.dp, accent.copy(alpha = 0.24f), surfaceShape)
                    .liquidClickable(
                        shape = surfaceShape,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            forward = true
                            previewPage = (previewPage + 1) % 3
                        },
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 36.dp,
                        iconSize = 18.dp,
                        containerColor = accent.copy(alpha = 0.16f),
                        shape = iconShape,
                    ),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = pageTitles[page],
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = transitionName,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                LiquidBadge(
                    text = "${page + 1}/3",
                    containerColor = accent.copy(alpha = 0.16f),
                    contentColor = accent,
                )
            }
        }
    }
}

@Composable
private fun ThemeEntrancePreviewRow(
    animation: LiquidScreenEntranceAnimation,
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    var replayCount by remember { mutableIntStateOf(0) }
    val tileShape = remember { RoundedRectangle(14.dp) }
    val previewAccents = listOf(colorScheme.primary, colorScheme.secondary, colorScheme.tertiary)
    val previewLabels = listOf("01", "02", "03")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.ui_theme_element_entrance_preview_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
            )
            LiquidButton(
                text = stringResource(Res.string.ui_theme_element_entrance_replay),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    replayCount++
                },
                variant = LiquidButtonVariant.Tonal,
                size = LiquidButtonSize.Small,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            previewLabels.forEachIndexed { index, label ->
                val accent = previewAccents[index]
                UniAnimatedEntrance(
                    order = index,
                    replayKey = replayCount to animation,
                    animationOverride = animation,
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(tileShape)
                            .background(accent.copy(alpha = 0.08f))
                            .border(1.dp, accent.copy(alpha = 0.22f), tileShape)
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .drawBehind { drawRect(accent) },
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(4.dp)
                                .clip(Capsule())
                                .drawBehind { drawRect(accent.copy(alpha = 0.50f)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: ImageVector,
    iconTint: Color,
    enabled: Boolean = true,
    showDivider: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 36.dp,
                        iconSize = 18.dp,
                        containerColor = iconTint.copy(alpha = 0.14f),
                        shape = RoundedRectangle(10.dp),
                    ),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.48f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                LiquidBadge(
                    text = valueLabel,
                    containerColor = colorScheme.primary.copy(alpha = if (enabled) 0.14f else 0.06f),
                    contentColor = if (enabled) colorScheme.primary else colorScheme.onSurfaceVariant,
                )
            }
            LiquidSlider(
                value = value.coerceIn(valueRange),
                onValueChange = onValueChange,
                valueRange = valueRange,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showDivider) {
            LiquidHorizontalDivider(
                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                color = colorScheme.outlineVariant.copy(alpha = 0.25f),
            )
        }
    }
}

@Composable
private fun <T> ThemeDropdownRow(
    title: String,
    selected: ThemeOptionChoice<T>,
    items: List<ThemeOptionChoice<T>>,
    icon: ImageVector,
    iconTint: Color,
    onItemSelected: (T) -> Unit,
    showDivider: Boolean,
    selectedSubtitle: String = selected.title,
) {
    val haptics = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    val anchorState = rememberLiquidGlassOverlayAnchorState()
    Box(modifier = Modifier.fillMaxWidth().liquidGlassOverlayAnchor(anchorState)) {
        UniInsetRow(
            title = title,
            subtitle = selectedSubtitle,
            icon = icon,
            iconTint = iconTint,
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                expanded = !expanded
            },
            showDivider = showDivider,
            trailingContent = {
                Icon(
                    imageVector = if (expanded) LiquidIcons.KeyboardArrowUp else LiquidIcons.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp),
                )
            },
        )
        LiquidDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            anchorState = anchorState,
            offset = DpOffset(0.dp, 4.dp),
        ) {
            items.forEach { choice ->
                LiquidMenuItem(
                    text = choice.title,
                    supportingText = choice.description,
                    selected = choice.value == selected.value,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onItemSelected(choice.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

internal fun String.toBackgroundEffect(): LiquidBackgroundEffect = when (this) {
    "Aurora" -> LiquidBackgroundEffect.Aurora
    "Mesh Glow" -> LiquidBackgroundEffect.MeshGlow
    "Radiant Beam" -> LiquidBackgroundEffect.RadiantBeam
    else -> LiquidBackgroundEffect.OrbitalPulse
}

internal fun LiquidBackgroundEffect.toStyleName(): String = when (this) {
    LiquidBackgroundEffect.MeshGlow -> "Mesh Glow"
    LiquidBackgroundEffect.OrbitalPulse -> "Orbital Pulse"
    LiquidBackgroundEffect.RadiantBeam -> "Radiant Beam"
    LiquidBackgroundEffect.Aurora -> "Aurora"
}

internal fun Color.toHexString(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return "#${r.toHexByte()}${g.toHexByte()}${b.toHexByte()}"
}

private fun Int.toHexByte(): String {
    val hex = "0123456789ABCDEF"
    val h = hex[(this ushr 4) and 0x0F]
    val l = hex[this and 0x0F]
    return "$h$l"
}
