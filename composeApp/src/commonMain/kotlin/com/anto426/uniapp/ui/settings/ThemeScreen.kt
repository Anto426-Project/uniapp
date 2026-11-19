package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.motion.UniMotion
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
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
                // Clean cardinal + diagonal ticks
                moveTo(11f, 2f)
                lineTo(13f, 2f)
                lineTo(13f, 5f)
                lineTo(11f, 5f)
                close()
                moveTo(11f, 19f)
                lineTo(13f, 19f)
                lineTo(13f, 22f)
                lineTo(11f, 22f)
                close()
                moveTo(2f, 11f)
                lineTo(5f, 11f)
                lineTo(5f, 13f)
                lineTo(2f, 13f)
                close()
                moveTo(19f, 11f)
                lineTo(22f, 11f)
                lineTo(22f, 13f)
                lineTo(19f, 13f)
                close()
                moveTo(4.93f, 6.34f)
                lineTo(6.34f, 4.93f)
                lineTo(8.46f, 7.05f)
                lineTo(7.05f, 8.46f)
                close()
                moveTo(15.54f, 16.95f)
                lineTo(16.95f, 15.54f)
                lineTo(19.07f, 17.66f)
                lineTo(17.66f, 19.07f)
                close()
                moveTo(17.66f, 4.93f)
                lineTo(19.07f, 6.34f)
                lineTo(16.95f, 8.46f)
                lineTo(15.54f, 7.05f)
                close()
                moveTo(7.05f, 15.54f)
                lineTo(8.46f, 16.95f)
                lineTo(6.34f, 19.07f)
                lineTo(4.93f, 17.66f)
                close()
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
                moveTo(5f, 3f)
                lineTo(19f, 3f)
                curveTo(20.1f, 3f, 21f, 3.9f, 21f, 5f)
                lineTo(21f, 19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                lineTo(5f, 21f)
                curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
                lineTo(3f, 5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                close()
                moveTo(6f, 17f)
                lineTo(18f, 17f)
                lineTo(14.2f, 12f)
                lineTo(11.2f, 15.8f)
                lineTo(9.1f, 13.2f)
                close()
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
                moveTo(4f, 7f)
                lineTo(14f, 7f)
                lineTo(14f, 9f)
                lineTo(4f, 9f)
                close()
                moveTo(16f, 6f)
                lineTo(18f, 6f)
                lineTo(18f, 10f)
                lineTo(16f, 10f)
                close()
                moveTo(19f, 7f)
                lineTo(21f, 7f)
                lineTo(21f, 9f)
                lineTo(19f, 9f)
                close()
                moveTo(4f, 15f)
                lineTo(8f, 15f)
                lineTo(8f, 17f)
                lineTo(4f, 17f)
                close()
                moveTo(9f, 14f)
                lineTo(11f, 14f)
                lineTo(11f, 18f)
                lineTo(9f, 18f)
                close()
                moveTo(12f, 15f)
                lineTo(21f, 15f)
                lineTo(21f, 17f)
                lineTo(12f, 17f)
                close()
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
                moveTo(4f, 6f)
                lineTo(12f, 6f)
                curveTo(13.1f, 6f, 14f, 6.9f, 14f, 8f)
                lineTo(14f, 18f)
                curveTo(14f, 19.1f, 13.1f, 20f, 12f, 20f)
                lineTo(4f, 20f)
                curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
                lineTo(2f, 8f)
                curveTo(2f, 6.9f, 2.9f, 6f, 4f, 6f)
                close()
                moveTo(16f, 4f)
                lineTo(20f, 4f)
                curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
                lineTo(22f, 16f)
                curveTo(22f, 17.1f, 21.1f, 18f, 20f, 18f)
                lineTo(16f, 18f)
                close()
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

    val modeLabel = when (uiState.themeMode) {
        AppThemeMode.System -> stringResource(Res.string.ui_theme_mode_system)
        AppThemeMode.Light -> stringResource(Res.string.ui_theme_mode_light)
        AppThemeMode.Dark -> stringResource(Res.string.ui_theme_mode_dark)
    }
    val paletteLabel = when {
        uiState.selectedThemeIndex == 0 -> "Material You"
        uiState.selectedThemeIndex == 5 || (uiState.themes.getOrNull(uiState.selectedThemeIndex)?.isCustom == true) ->
            (uiState.customColor ?: Color(0xFF0B57D0)).toHexString()
        else -> uiState.themes.getOrNull(uiState.selectedThemeIndex)?.name ?: "Material You"
    }

    UniScreenColumn {
        // 1. Live UniApp Monet Preview Card
        LiveThemePreviewCard(
            paletteLabel = paletteLabel,
            modeLabel = modeLabel,
            backgroundTitle = selectedBackground.title,
        )

        // 2. Segmented Liquid Pill Bar for Sistema | Chiaro | Scuro
        val modeEntries = listOf(
            Triple(AppThemeMode.System, stringResource(Res.string.ui_theme_mode_system), ThemeIcons.AutoMode),
            Triple(AppThemeMode.Light, stringResource(Res.string.ui_theme_mode_light), ThemeIcons.LightMode),
            Triple(AppThemeMode.Dark, stringResource(Res.string.ui_theme_mode_dark), ThemeIcons.DarkMode),
        )
        LiquidTabBar(
            items = modeEntries.map { (_, title, icon) ->
                LiquidNavigationItem(label = title, icon = icon)
            },
            selectedIndex = modeEntries.indexOfFirst { it.first == uiState.themeMode }.coerceAtLeast(0),
            onTabSelected = { index -> onThemeModeSelected(modeEntries[index].first) },
            modifier = Modifier.fillMaxWidth(),
        )

        // 3. Tavolozza Colori & Monet Seed
        ThemeColorGroup(
            uiState = uiState,
            onThemeSelected = onThemeSelected,
            onNavigateToColorLab = onNavigateToColorLab,
        )

        // 4. Sfondo Liquid & Animazioni
        ThemeEffectsGroup(
            backgroundChoices = backgroundChoices,
            selectedBackground = selectedBackground,
            onBackgroundStyleSelected = { onBackgroundStyleSelected(it.toStyleName()) },
            motionChoices = motionChoices,
            selectedMotion = selectedMotion,
            onTransitionSelected = onPageTransitionSelected,
            reducedMotion = uiState.reducedMotion,
            onReducedMotionChanged = onReducedMotionChanged,
        )

        // 5. Ripristina valori predefiniti
        UniInsetGroup {
            UniInsetRow(
                title = stringResource(Res.string.ui_theme_reset_short),
                icon = LiquidIcons.Refresh,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onReset,
                showDivider = false,
            )
        }
    }
}

@Composable
private fun LiveThemePreviewCard(
    paletteLabel: String,
    modeLabel: String,
    backgroundTitle: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        contentPadding = PaddingValues(18.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(26.dp),
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
                    text = stringResource(Res.string.msg_attivo),
                    containerColor = colorScheme.primary.copy(alpha = 0.15f),
                    contentColor = colorScheme.primary,
                )
            }

            // Live Bento sample tiles + Monet token swatch pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LiquidCard(
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f),
                    shape = RoundedRectangle(16.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Media",
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
                                .background(colorScheme.primary),
                        )
                    }
                }

                LiquidCard(
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f),
                    shape = RoundedRectangle(16.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Palette Monet",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TokenDot(color = colorScheme.primary)
                            TokenDot(color = colorScheme.secondary)
                            TokenDot(color = colorScheme.tertiary)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(6.dp)
                                .clip(Capsule())
                                .background(colorScheme.secondary.copy(alpha = 0.70f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color),
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
    val fixedThemes = uiState.themes.mapIndexedNotNull { index, option ->
        if (index > 0 && !option.isCustom && option.color != null) {
            index to (option.name to option.color)
        } else {
            null
        }
    }
    val fallbackIndex = fixedThemes.firstOrNull()?.first ?: 1

    UniInsetGroup(title = stringResource(Res.string.ui_theme_selection_title)) {
        UniInsetRow(
            title = stringResource(Res.string.ui_theme_material_you_title),
            subtitle = stringResource(Res.string.ui_theme_material_you_sub),
            icon = ThemeIcons.Sparkles,
            iconTint = colorScheme.primary,
            onClick = { onThemeSelected(if (isDynamicSelected) fallbackIndex else 0) },
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            fixedThemes.forEach { (themeIndex, pair) ->
                val (themeName, swatchColor) = pair
                val isSelected = uiState.selectedThemeIndex == themeIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(Capsule())
                        .background(swatchColor.copy(alpha = if (isSelected) 1f else 0.78f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colorScheme.onSurface else Color.White.copy(alpha = 0.24f),
                            shape = Capsule(),
                        )
                        .clickable(role = Role.RadioButton) { onThemeSelected(themeIndex) }
                        .semantics { selected = isSelected },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = LiquidIcons.Check,
                            contentDescription = themeName,
                            tint = if (swatchColor.luminance() > 0.48f) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

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
private fun ThemeEffectsGroup(
    backgroundChoices: List<ThemeOptionChoice<LiquidBackgroundEffect>>,
    selectedBackground: ThemeOptionChoice<LiquidBackgroundEffect>,
    onBackgroundStyleSelected: (LiquidBackgroundEffect) -> Unit,
    motionChoices: List<ThemeOptionChoice<LiquidSwitcherTransition>>,
    selectedMotion: ThemeOptionChoice<LiquidSwitcherTransition>,
    onTransitionSelected: (LiquidSwitcherTransition) -> Unit,
    reducedMotion: Boolean,
    onReducedMotionChanged: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    UniInsetGroup(title = stringResource(Res.string.ui_theme_graphics_motion_group)) {
        ThemeDropdownRow(
            title = stringResource(Res.string.ui_theme_engine_group),
            selected = selectedBackground,
            items = backgroundChoices,
            icon = ThemeIcons.Wallpaper,
            iconTint = colorScheme.primary,
            onItemSelected = onBackgroundStyleSelected,
            showDivider = true,
        )
        ThemeDropdownRow(
            title = stringResource(Res.string.ui_theme_page_motion_style),
            selected = selectedMotion,
            items = motionChoices,
            icon = ThemeIcons.PageTransition,
            iconTint = colorScheme.secondary,
            onItemSelected = onTransitionSelected,
            showDivider = true,
        )
        UniInsetRow(
            title = stringResource(Res.string.ui_theme_reduced_motion_title),
            subtitle = stringResource(Res.string.ui_theme_reduced_motion_sub),
            icon = ThemeIcons.SlowMotion,
            iconTint = colorScheme.tertiary,
            showDivider = reducedMotion,
            trailingContent = {
                LiquidSwitch(checked = reducedMotion, onCheckedChange = onReducedMotionChanged)
            },
        )
        if (reducedMotion) {
            Text(
                text = stringResource(Res.string.ui_theme_page_motion_reduced_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
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
) {
    var expanded by remember { mutableStateOf(false) }
    val anchorState = rememberLiquidGlassOverlayAnchorState()
    Box(modifier = Modifier.fillMaxWidth().liquidGlassOverlayAnchor(anchorState)) {
        UniInsetRow(
            title = title,
            subtitle = selected.title,
            icon = icon,
            iconTint = iconTint,
            onClick = { expanded = !expanded },
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
