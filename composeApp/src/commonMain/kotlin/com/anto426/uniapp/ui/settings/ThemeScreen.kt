package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.motion.UniMotion
import com.anto426.uniapp.ui.settings.components.ThemeAnimationsGroup
import com.anto426.uniapp.ui.settings.components.ThemeBackgroundGroup
import com.anto426.uniapp.ui.settings.components.ThemeColorGroup
import com.anto426.uniapp.ui.settings.components.ThemeGlassCustomizationGroup
import com.anto426.uniapp.ui.settings.components.ThemeModeVisualSection
import com.anto426.uniapp.ui.settings.components.ThemeNavigationBarGroup
import com.anto426.uniapp.ui.settings.components.ThemeOptionChoice
import com.anto426.uniapp.ui.settings.components.ThemeStudioHeaderCard
import com.anto426.uniapp.ui.settings.components.toBackgroundEffect
import com.anto426.uniapp.ui.settings.components.toHexString
import com.anto426.uniapp.ui.settings.components.toStyleName
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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
            LiquidBackgroundEffect.OrbitalPulse,
            stringResource(Res.string.ui_theme_background_orbital),
            stringResource(Res.string.ui_theme_background_orbital_sub),
        ),
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
            icon = LiquidIcons.Sparkles,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_theme_tab_interface),
            icon = LiquidIcons.NavigationBar,
        ),
    )

    UniScreenColumn {
        // 1. Compact Studio Summary Banner (single-surface, zero nested glass cards)
        UniAnimatedEntrance(order = 0) {
            ThemeStudioHeaderCard(
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

        // 3. Categorized Controls
        UniTabContentHost(
            selectedTab = selectedCategory,
            tabCount = categoryTabs.size,
            label = "ThemeCategoryTabs",
        ) { category ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                when (category) {
                    0 -> {
                        ThemeModeVisualSection(
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
                        ThemeGlassCustomizationGroup(
                            qualityChoices = qualityChoices,
                            selectedQuality = selectedQuality,
                            effectiveQualityLabel = effectiveQualityLabel,
                            onQualitySelected = onGlassQualitySelected,
                            liquidIntensity = uiState.liquidIntensity,
                            onLiquidIntensityChanged = onLiquidIntensityChanged,
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

        // 4. Full-width Outlined Glass Reset Action
        LiquidButton(
            text = stringResource(Res.string.ui_theme_reset_short),
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onReset()
            },
            leadingIcon = {
                Icon(
                    imageVector = LiquidIcons.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            variant = LiquidButtonVariant.Outlined,
            size = LiquidButtonSize.Medium,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 50.dp),
        )
    }
}
