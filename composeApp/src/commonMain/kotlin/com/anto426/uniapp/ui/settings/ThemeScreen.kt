package com.anto426.uniapp.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.layout.sectionentrance.LiquidSectionEntrance
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteOption
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteSelector
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.motion.UniMotion
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private object ThemeIcons {
    val Sparkles: ImageVector by lazy {
        ImageVector.Builder(
            name = "Sparkles",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9.5f, 3f)
                lineTo(11.4f, 8.6f)
                lineTo(17f, 10.5f)
                lineTo(11.4f, 12.4f)
                lineTo(9.5f, 18f)
                lineTo(7.6f, 12.4f)
                lineTo(2f, 10.5f)
                lineTo(7.6f, 8.6f)
                close()
                moveTo(18.5f, 2f)
                lineTo(19.6f, 4.9f)
                lineTo(22.5f, 6f)
                lineTo(19.6f, 7.1f)
                lineTo(18.5f, 10f)
                lineTo(17.4f, 7.1f)
                lineTo(14.5f, 6f)
                lineTo(17.4f, 4.9f)
                close()
                moveTo(18.5f, 14f)
                lineTo(19.4f, 16.6f)
                lineTo(22f, 17.5f)
                lineTo(19.4f, 18.4f)
                lineTo(18.5f, 21f)
                lineTo(17.6f, 18.4f)
                lineTo(15f, 17.5f)
                lineTo(17.6f, 16.6f)
                close()
            }
        }.build()
    }

    val AutoMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "AutoMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 4f)
                lineTo(12f, 20f)
                curveTo(7.58f, 20f, 4f, 16.42f, 4f, 12f)
                curveTo(4f, 7.58f, 7.58f, 4f, 12f, 4f)
                close()
            }
        }.build()
    }

    val LightMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "LightMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f)
                curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
                curveTo(7f, 14.76f, 9.24f, 17f, 12f, 17f)
                curveTo(14.76f, 17f, 17f, 14.76f, 17f, 12f)
                curveTo(17f, 9.24f, 14.76f, 7f, 12f, 7f)
                close()
                moveTo(11f, 2f); lineTo(13f, 2f); lineTo(13f, 5f); lineTo(11f, 5f); close()
                moveTo(11f, 19f); lineTo(13f, 19f); lineTo(13f, 22f); lineTo(11f, 22f); close()
                moveTo(2f, 11f); lineTo(5f, 11f); lineTo(5f, 13f); lineTo(2f, 13f); close()
                moveTo(19f, 11f); lineTo(22f, 11f); lineTo(22f, 13f); lineTo(19f, 13f); close()
                moveTo(4.93f, 6.34f); lineTo(6.34f, 4.93f); lineTo(8.46f, 7.05f); lineTo(7.05f, 8.46f); close()
                moveTo(15.54f, 16.95f); lineTo(16.95f, 15.54f); lineTo(19.07f, 17.66f); lineTo(17.66f, 19.07f); close()
                moveTo(17.66f, 4.93f); lineTo(19.07f, 6.34f); lineTo(16.95f, 8.46f); lineTo(15.54f, 7.05f); close()
                moveTo(7.05f, 15.54f); lineTo(8.46f, 16.95f); lineTo(6.34f, 19.07f); lineTo(4.93f, 17.66f); close()
            }
        }.build()
    }

    val DarkMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "DarkMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.3f, 3f)
                curveTo(10.64f, 4.58f, 9.75f, 6.91f, 10.12f, 9.39f)
                curveTo(10.67f, 13.06f, 13.68f, 15.86f, 17.39f, 16.15f)
                curveTo(18.78f, 16.26f, 20.1f, 15.96f, 21.22f, 15.36f)
                curveTo(19.86f, 18.69f, 16.59f, 21.02f, 12.78f, 21f)
                curveTo(7.69f, 20.98f, 3.63f, 16.75f, 3.85f, 11.67f)
                curveTo(4.06f, 6.89f, 7.77f, 3.16f, 12.3f, 3f)
                close()
            }
        }.build()
    }

    val Wallpaper: ImageVector by lazy {
        ImageVector.Builder(
            name = "Wallpaper",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(5f, 3f); lineTo(19f, 3f)
                curveTo(20.1f, 3f, 21f, 3.9f, 21f, 5f)
                lineTo(21f, 19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                lineTo(5f, 21f)
                curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
                lineTo(3f, 5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                close()
                moveTo(6f, 17f); lineTo(18f, 17f); lineTo(14.2f, 12f); lineTo(11.2f, 15.8f); lineTo(9.1f, 13.2f); close()
                moveTo(8.5f, 7f)
                curveTo(7.67f, 7f, 7f, 7.67f, 7f, 8.5f)
                curveTo(7f, 9.33f, 7.67f, 10f, 8.5f, 10f)
                curveTo(9.33f, 10f, 10f, 9.33f, 10f, 8.5f)
                curveTo(10f, 7.67f, 9.33f, 7f, 8.5f, 7f)
                close()
            }
        }.build()
    }

    val SlowMotion: ImageVector by lazy {
        ImageVector.Builder(
            name = "SlowMotion",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 7f); lineTo(14f, 7f); lineTo(14f, 9f); lineTo(4f, 9f); close()
                moveTo(16f, 6f); lineTo(18f, 6f); lineTo(18f, 10f); lineTo(16f, 10f); close()
                moveTo(19f, 7f); lineTo(21f, 7f); lineTo(21f, 9f); lineTo(19f, 9f); close()
                moveTo(4f, 15f); lineTo(8f, 15f); lineTo(8f, 17f); lineTo(4f, 17f); close()
                moveTo(9f, 14f); lineTo(11f, 14f); lineTo(11f, 18f); lineTo(9f, 18f); close()
                moveTo(12f, 15f); lineTo(21f, 15f); lineTo(21f, 17f); lineTo(12f, 17f); close()
            }
        }.build()
    }

    val PageTransition: ImageVector by lazy {
        ImageVector.Builder(
            name = "PageTransition",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 6f); lineTo(12f, 6f)
                curveTo(13.1f, 6f, 14f, 6.9f, 14f, 8f)
                lineTo(14f, 18f)
                curveTo(14f, 19.1f, 13.1f, 20f, 12f, 20f)
                lineTo(4f, 20f)
                curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
                lineTo(2f, 8f)
                curveTo(2f, 6.9f, 2.9f, 6f, 4f, 6f)
                close()
                moveTo(16f, 4f); lineTo(20f, 4f)
                curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
                lineTo(22f, 16f)
                curveTo(22f, 17.1f, 21.1f, 18f, 20f, 18f)
                lineTo(16f, 18f)
                close()
            }
        }.build()
    }

    val NavigationBar: ImageVector by lazy {
        ImageVector.Builder(
            name = "NavigationBar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(4f, 3f); lineTo(20f, 3f)
                curveTo(21.1f, 3f, 22f, 3.9f, 22f, 5f)
                lineTo(22f, 19f)
                curveTo(22f, 20.1f, 21.1f, 21f, 20f, 21f)
                lineTo(4f, 21f)
                curveTo(2.9f, 21f, 2f, 20.1f, 2f, 19f)
                lineTo(2f, 5f)
                curveTo(2f, 3.9f, 2.9f, 3f, 4f, 3f)
                close()
                moveTo(4f, 5f); lineTo(4f, 15f); lineTo(20f, 15f); lineTo(20f, 5f); close()
                moveTo(6f, 17.2f); lineTo(10f, 17.2f); lineTo(10f, 18.8f); lineTo(6f, 18.8f); close()
                moveTo(14f, 17.2f); lineTo(18f, 17.2f); lineTo(18f, 18.8f); lineTo(14f, 18.8f); close()
            }
        }.build()
    }

    val ElementEntrance: ImageVector by lazy {
        ImageVector.Builder(
            name = "ElementEntrance",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 8f); lineTo(4f, 8f); close()
                moveTo(4f, 10.5f); lineTo(17f, 10.5f); lineTo(17f, 13.5f); lineTo(4f, 13.5f); close()
                moveTo(4f, 16f); lineTo(14f, 16f); lineTo(14f, 19f); lineTo(4f, 19f); close()
            }
        }.build()
    }
}

private data class ThemeOptionChoice<T>(
    val value: T,
    val title: String,
    val description: String,
)

@Composable
fun ThemeScreen(
    uiState: ThemeUiState,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onThemeSelected: (Int) -> Unit,
    onBackgroundStyleSelected: (String) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onPageTransitionSelected: (LiquidSwitcherTransition) -> Unit,
    onLiquidIntensityChanged: (Float) -> Unit = {},
    onGlassQualitySelected: (LiquidGlassQualityTier?) -> Unit = {},
    onBackgroundIntensityChanged: (Float) -> Unit = {},
    onBackgroundSpeedChanged: (Float) -> Unit = {},
    onBackgroundVignetteChanged: (Boolean) -> Unit = {},
    onNavBarShowLabelsChanged: (Boolean) -> Unit = {},
    onNavBarAutoHideChanged: (Boolean) -> Unit = {},
    onElementEntranceSelected: (LiquidScreenEntranceAnimation) -> Unit = {},
    onReset: () -> Unit,
    onNavigateToColorLab: () -> Unit = {},
) {
    val backgroundChoices = listOf(
        ThemeOptionChoice(
            LiquidBackgroundEffect.Aurora,
            stringResource(Res.string.ui_theme_background_aurora),
            stringResource(Res.string.ui_theme_background_aurora_sub),
        ),
        ThemeOptionChoice(
            LiquidBackgroundEffect.MeshGlow,
            stringResource(Res.string.ui_theme_background_mesh),
            stringResource(Res.string.ui_theme_background_mesh_sub),
        ),
        ThemeOptionChoice(
            LiquidBackgroundEffect.OrbitalPulse,
            stringResource(Res.string.ui_theme_background_orbital),
            stringResource(Res.string.ui_theme_background_orbital_sub),
        ),
        ThemeOptionChoice(
            LiquidBackgroundEffect.RadiantBeam,
            stringResource(Res.string.ui_theme_background_radiant),
            stringResource(Res.string.ui_theme_background_radiant_sub),
        ),
    )
    val selectedBackground = backgroundChoices.first {
        it.value == uiState.selectedBackgroundStyle.toBackgroundEffect()
    }

    val motionChoices = UniMotion.options.map { option ->
        ThemeOptionChoice(
            option.transition,
            stringResource(option.title),
            stringResource(option.description),
        )
    }
    val selectedMotion = motionChoices.first {
        it.value == UniMotion.selectedTransition(uiState.pageMotion)
    }

    val entranceChoices = UniMotion.elementEntranceOptions.map { option ->
        ThemeOptionChoice(
            option.animation,
            stringResource(option.title),
            stringResource(option.description),
        )
    }
    val selectedEntrance = entranceChoices.firstOrNull {
        it.value == uiState.elementEntrance
    } ?: entranceChoices.first()

    val qualityChoices = listOf<ThemeOptionChoice<LiquidGlassQualityTier?>>(
        ThemeOptionChoice(
            null,
            stringResource(Res.string.ui_theme_glass_quality_auto),
            stringResource(Res.string.ui_theme_glass_quality_auto_sub),
        ),
        ThemeOptionChoice(
            LiquidGlassQualityTier.MINIMAL,
            stringResource(Res.string.ui_theme_glass_quality_minimal),
            stringResource(Res.string.ui_theme_glass_quality_minimal_sub),
        ),
        ThemeOptionChoice(
            LiquidGlassQualityTier.BALANCED,
            stringResource(Res.string.ui_theme_glass_quality_balanced),
            stringResource(Res.string.ui_theme_glass_quality_balanced_sub),
        ),
        ThemeOptionChoice(
            LiquidGlassQualityTier.HIGH,
            stringResource(Res.string.ui_theme_glass_quality_high),
            stringResource(Res.string.ui_theme_glass_quality_high_sub),
        ),
        ThemeOptionChoice(
            LiquidGlassQualityTier.ULTRA,
            stringResource(Res.string.ui_theme_glass_quality_ultra),
            stringResource(Res.string.ui_theme_glass_quality_ultra_sub),
        ),
    )
    val selectedQuality = qualityChoices.firstOrNull { it.value == uiState.glassQuality } ?: qualityChoices.first()

    val modeLabel = when (uiState.themeMode) {
        AppThemeMode.System -> stringResource(Res.string.ui_theme_mode_system)
        AppThemeMode.Light -> stringResource(Res.string.ui_theme_mode_light)
        AppThemeMode.Dark -> stringResource(Res.string.ui_theme_mode_dark)
    }
    val paletteLabel = when {
        uiState.selectedThemeIndex == 5 || (uiState.themes.getOrNull(uiState.selectedThemeIndex)?.isCustom == true) ->
            (uiState.customColor ?: Color(0xFF0B57D0)).toHexString()
        else -> stringResource(uiState.themes.getOrNull(uiState.selectedThemeIndex)?.nameRes ?: Res.string.ui_theme_palette_dynamic)
    }

    val haptics = LocalHapticFeedback.current
    val colorScheme = MaterialTheme.colorScheme
    val performance = LocalLiquidGlassPerformance.current
    val effectiveQualityTier = performance.opticalQualityTier ?: performance.qualityTier
    val effectiveQualityLabel = when (effectiveQualityTier) {
        LiquidGlassQualityTier.MINIMAL -> stringResource(Res.string.ui_theme_glass_quality_minimal)
        LiquidGlassQualityTier.BALANCED -> stringResource(Res.string.ui_theme_glass_quality_balanced)
        LiquidGlassQualityTier.HIGH -> stringResource(Res.string.ui_theme_glass_quality_high)
        LiquidGlassQualityTier.ULTRA -> stringResource(Res.string.ui_theme_glass_quality_ultra)
    }

    var selectedCategory by rememberSaveable { mutableIntStateOf(0) }
    val categoryTabs = listOf(
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_theme_tab_style),
            icon = LiquidIcons.Palette,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_theme_tab_effects),
            icon = ThemeIcons.Sparkles,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_theme_tab_interface),
            icon = ThemeIcons.NavigationBar,
        ),
    )

    UniScreenColumn {
        // 1. Live UniApp Monet Preview Card (always visible above category tabs)
        UniAnimatedEntrance(order = 0) {
            LiveThemePreviewCard(
                paletteLabel = paletteLabel,
                modeLabel = modeLabel,
                backgroundTitle = selectedBackground.title,
                activeQualityLabel = effectiveQualityLabel,
            )
        }

        // 2. Category Switcher (Stile · Effetti · Interfaccia)
        LiquidTabBar(
            items = categoryTabs,
            selectedIndex = selectedCategory,
            onTabSelected = { selectedCategory = it },
        )

        // 3. Active Category Content
        UniTabContentHost(
            selectedTab = selectedCategory,
            tabCount = categoryTabs.size,
            label = "ThemeCategoryTabs",
        ) { category ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (category) {
                    0 -> {
                        ThemeModeGroup(
                            selectedMode = uiState.themeMode,
                            onThemeModeSelected = onThemeModeSelected,
                        )
                        ThemeColorGroup(
                            uiState = uiState,
                            onThemeSelected = onThemeSelected,
                            onNavigateToColorLab = onNavigateToColorLab,
                        )
                    }

                    1 -> {
                        ThemeGlassCustomizationGroup(
                            qualityChoices = qualityChoices,
                            selectedQuality = selectedQuality,
                            effectiveQualityLabel = effectiveQualityLabel,
                            onQualitySelected = onGlassQualitySelected,
                            liquidIntensity = uiState.liquidIntensity,
                            onLiquidIntensityChanged = onLiquidIntensityChanged,
                        )
                        ThemeBackgroundGroup(
                            backgroundChoices = backgroundChoices,
                            selectedBackground = selectedBackground,
                            onBackgroundStyleSelected = { onBackgroundStyleSelected(it.toStyleName()) },
                            backgroundIntensity = uiState.backgroundIntensity,
                            onBackgroundIntensityChanged = onBackgroundIntensityChanged,
                            backgroundSpeedFactor = uiState.backgroundSpeedFactor,
                            onBackgroundSpeedChanged = onBackgroundSpeedChanged,
                            showBackgroundVignette = uiState.showBackgroundVignette,
                            onBackgroundVignetteChanged = onBackgroundVignetteChanged,
                            reducedMotion = uiState.reducedMotion,
                        )
                    }

                    else -> {
                        ThemeNavigationBarGroup(
                            showLabels = uiState.navBarShowLabels,
                            onShowLabelsChanged = onNavBarShowLabelsChanged,
                            autoHide = uiState.navBarAutoHide,
                            onAutoHideChanged = onNavBarAutoHideChanged,
                        )
                        ThemeAnimationsGroup(
                            motionChoices = motionChoices,
                            selectedMotion = selectedMotion,
                            onTransitionSelected = onPageTransitionSelected,
                            entranceChoices = entranceChoices,
                            selectedEntrance = selectedEntrance,
                            onEntranceSelected = onElementEntranceSelected,
                            reducedMotion = uiState.reducedMotion,
                            onReducedMotionChanged = onReducedMotionChanged,
                        )
                    }
                }
            }
        }

        // 4. Ripristina valori predefiniti
        UniInsetGroup(entranceOrder = 3) {
            UniInsetRow(
                title = stringResource(Res.string.ui_theme_reset_short),
                icon = LiquidIcons.Refresh,
                iconTint = colorScheme.primary,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onReset()
                },
                showDivider = false,
            )
        }
    }
}

@Composable
private fun ThemeModeGroup(
    selectedMode: AppThemeMode,
    onThemeModeSelected: (AppThemeMode) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val modeEntries = listOf(
        Triple(AppThemeMode.System, stringResource(Res.string.ui_theme_mode_system), ThemeIcons.AutoMode),
        Triple(AppThemeMode.Light, stringResource(Res.string.ui_theme_mode_light), ThemeIcons.LightMode),
        Triple(AppThemeMode.Dark, stringResource(Res.string.ui_theme_mode_dark), ThemeIcons.DarkMode),
    )

    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_mode_group),
        entranceOrder = 1,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            modeEntries.forEach { (mode, title, icon) ->
                val isSelected = selectedMode == mode
                LiquidCard(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 11.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 46.dp)
                        .semantics(mergeDescendants = true) {
                            role = Role.RadioButton
                            selected = isSelected
                        },
                    shape = Capsule(),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.75.dp,
                        color = if (isSelected) colorScheme.primary.copy(alpha = 0.78f) else colorScheme.outlineVariant.copy(alpha = 0.25f),
                    ),
                    colors = LiquidCardDefaults.colors(
                        containerColor = if (isSelected) colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                    ),
                    onClick = {
                        if (!isSelected) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onThemeModeSelected(mode)
                        }
                    },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
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
private fun LiveThemePreviewCard(
    paletteLabel: String,
    modeLabel: String,
    backgroundTitle: String,
    activeQualityLabel: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryState = animateColorAsState(targetValue = colorScheme.primary, label = "themePreviewPrimary")
    val secondaryState = animateColorAsState(targetValue = colorScheme.secondary, label = "themePreviewSecondary")
    val tertiaryState = animateColorAsState(targetValue = colorScheme.tertiary, label = "themePreviewTertiary")
    val cardShape = remember { RoundedRectangle(26.dp) }
    val tileShape = remember { RoundedRectangle(16.dp) }

    LiquidCard(
        contentPadding = PaddingValues(18.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        containerSize = 38.dp,
                        iconSize = 19.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.16f),
                        shape = RoundedRectangle(11.dp),
                    ),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
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
                        text = "$modeLabel • $backgroundTitle",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                LiquidBadge(
                    text = activeQualityLabel,
                    containerColor = colorScheme.primary.copy(alpha = 0.15f),
                    contentColor = colorScheme.primary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LiquidCard(
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f),
                    shape = tileShape,
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(Res.string.ui_average),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "28.5",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .height(6.dp)
                                .clip(Capsule())
                                .drawBehind { drawRect(primaryState.value) },
                        )
                    }
                }

                LiquidCard(
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f),
                    shape = tileShape,
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.ui_monet_palette),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TokenDot(colorState = primaryState)
                            TokenDot(colorState = secondaryState)
                            TokenDot(colorState = tertiaryState)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(6.dp)
                                .clip(Capsule())
                                .drawBehind { drawRect(secondaryState.value.copy(alpha = 0.70f)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenDot(colorState: State<Color>) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .drawBehind { drawRect(colorState.value) },
    )
}

@Composable
private fun ThemeColorGroup(
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
        entranceOrder = 2,
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
            iconTint = colorScheme.primary,
            onClick = onNavigateToColorLab,
            showDivider = false,
        )
    }
}

@Composable
private fun ThemeGlassCustomizationGroup(
    qualityChoices: List<ThemeOptionChoice<LiquidGlassQualityTier?>>,
    selectedQuality: ThemeOptionChoice<LiquidGlassQualityTier?>,
    effectiveQualityLabel: String,
    onQualitySelected: (LiquidGlassQualityTier?) -> Unit,
    liquidIntensity: Float,
    onLiquidIntensityChanged: (Float) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val qualitySubtitle = if (selectedQuality.value == null) {
        "${selectedQuality.title} · $effectiveQualityLabel"
    } else {
        selectedQuality.title
    }
    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_glass_group),
        entranceOrder = 1,
    ) {
        ThemeDropdownRow(
            title = stringResource(Res.string.ui_theme_glass_quality),
            selected = selectedQuality,
            selectedSubtitle = qualitySubtitle,
            items = qualityChoices,
            icon = LiquidIcons.Star,
            iconTint = colorScheme.primary,
            onItemSelected = onQualitySelected,
            showDivider = true,
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
private fun ThemeBackgroundGroup(
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
        entranceOrder = 2,
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
private fun ThemeNavigationBarGroup(
    showLabels: Boolean,
    onShowLabelsChanged: (Boolean) -> Unit,
    autoHide: Boolean,
    onAutoHideChanged: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    UniInsetGroup(
        title = stringResource(Res.string.ui_theme_navbar_group),
        entranceOrder = 1,
    ) {
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
private fun ThemeAnimationsGroup(
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
        entranceOrder = 2,
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        } else {
            ThemeDropdownRow(
                title = stringResource(Res.string.ui_theme_page_motion_style),
                selected = selectedMotion,
                items = motionChoices,
                icon = ThemeIcons.PageTransition,
                iconTint = colorScheme.primary,
                onItemSelected = onTransitionSelected,
                showDivider = true,
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
                showDivider = showEntrancePreview,
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
    val cardShape = remember { RoundedRectangle(18.dp) }
    val iconShape = remember { RoundedRectangle(11.dp) }
    val pageTitles = listOf(
        stringResource(Res.string.ui_home_career_eyebrow),
        stringResource(Res.string.ui_career_open_stats),
        stringResource(Res.string.ui_settings),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                    size = 34.dp,
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
                    size = 34.dp,
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

        LiquidAnimatedSwitcher(
            targetState = previewPage,
            transition = transition,
            mode = LiquidSwitcherMode.Screen,
            isForward = { _, _ -> forward },
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
            LiquidCard(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                shape = cardShape,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            containerColor = accent.copy(alpha = 0.14f),
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
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent,
                    )
                }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                LiquidSectionEntrance(
                    animation = animation,
                    delayMillis = LiquidMotion.sectionDelayMillis(index),
                    replayKey = replayCount to animation,
                    modifier = Modifier.weight(1f),
                ) {
                    LiquidCard(
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        shape = tileShape,
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .drawBehind { drawRect(accent) },
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(4.dp)
                                    .clip(Capsule())
                                    .drawBehind { drawRect(accent.copy(alpha = 0.45f)) },
                            )
                        }
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

private fun String.toBackgroundEffect(): LiquidBackgroundEffect = when (this) {
    "Mesh Glow" -> LiquidBackgroundEffect.MeshGlow
    "Orbital Pulse" -> LiquidBackgroundEffect.OrbitalPulse
    "Radiant Beam" -> LiquidBackgroundEffect.RadiantBeam
    else -> LiquidBackgroundEffect.Aurora
}

private fun LiquidBackgroundEffect.toStyleName(): String = when (this) {
    LiquidBackgroundEffect.MeshGlow -> "Mesh Glow"
    LiquidBackgroundEffect.OrbitalPulse -> "Orbital Pulse"
    LiquidBackgroundEffect.RadiantBeam -> "Radiant Beam"
    LiquidBackgroundEffect.Aurora -> "Aurora"
}

private fun Color.toHexString(): String {
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
