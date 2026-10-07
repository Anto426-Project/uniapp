package com.anto426.uniapp.ui.settings


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceDropdown
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.selection.backgroundselector.LiquidBackgroundSelector
import com.anto426.liquidmonet.components.selection.radiobutton.LiquidRadioButton
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniSection
import com.anto426.uniapp.ui.motion.UniMotion
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Schermata Temi e Personalizzazione Visiva Liquid Monet v2.
 *
 * Include:
 * 1. Selettore Modalità Aspetto a Mockup Visivi (Sistema, Chiaro, Scuro)
 * 2. Tavolozza Colori completa (Material You, Sapphire, Emerald, Sunset, Violet)
 *    con opzione finale per accedere direttamente al Laboratorio Colori avanzato
 * 3. Motore Grafico Sfondo Liquid (Aurora, Mesh Glow, Orbital Pulse, Radiant Beam)
 * 4. Ottimizzazioni di Fisica e Ripristino Impostazioni
 */
@Composable
fun ThemeScreen(
    uiState: ThemeUiState,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onThemeSelected: (Int) -> Unit,
    onBackgroundStyleSelected: (String) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onPageMotionEnabledChanged: (Boolean) -> Unit,
    onPageTransitionSelected: (LiquidSwitcherTransition) -> Unit,
    onReset: () -> Unit,
    onCustomColorSelected: (Color) -> Unit = {},
    onNavigateToColorLab: () -> Unit = {}
) {
    val activeAccentColor = when {
        uiState.selectedThemeIndex == 0 -> MaterialTheme.colorScheme.primary
        uiState.selectedThemeIndex == 5 || (uiState.themes.getOrNull(uiState.selectedThemeIndex)?.isCustom == true) ->
            uiState.customColor ?: Color(0xFF0B57D0)
        else -> uiState.themes.getOrNull(uiState.selectedThemeIndex)?.color ?: MaterialTheme.colorScheme.primary
    }

    UniScreenColumn {
        // 1. Modalità aspetto con anteprime visive
        UniSection(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_theme_mode_group),
                size = LiquidSectionHeaderSize.Small,
            )
            ThemeAppearanceSelector(
                selectedMode = uiState.themeMode,
                accentColor = activeAccentColor,
                onModeSelected = onThemeModeSelected,
            )
        }

        // 2. Tavolozza Colori & Monet Seed (Racchiusi in un unico contenitore)
        UniSection {
            LiquidPreferenceGroup(title = stringResource(Res.string.ui_theme_palette_group)) {
                // Palette predefinite (Material You, Sapphire, Emerald, Sunset, Violet)
                uiState.themes.forEachIndexed { index, theme ->
                    if (theme.isCustom) return@forEachIndexed

                    val isSelected = uiState.selectedThemeIndex == index

                    LiquidPreferenceItem(
                        title = if (index == 0) stringResource(Res.string.ui_theme_material_you_title) else theme.name,
                        subtitle = if (index == 0) stringResource(Res.string.ui_theme_material_you_sub) else theme.description,
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (theme.color != null) {
                                            Modifier.background(theme.color)
                                        } else {
                                            Modifier.background(
                                                Brush.sweepGradient(
                                                    listOf(
                                                        Color(0xFF4285F4),
                                                        Color(0xFF34A853),
                                                        Color(0xFFFBBC05),
                                                        Color(0xFFEA4335),
                                                        Color(0xFF4285F4),
                                                    )
                                                )
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = LiquidIcons.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            LiquidRadioButton(
                                selected = isSelected,
                                onClick = { onThemeSelected(index) },
                            )
                        },
                        onClick = { onThemeSelected(index) },
                    )

                    LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                }

                // Ultima opzione: Laboratorio Colori Personalizzati (che rimanda alla schermata Laboratorio)
                val isCustomActive = uiState.selectedThemeIndex == 5 || (uiState.themes.getOrNull(uiState.selectedThemeIndex)?.isCustom == true)
                val customColor = uiState.customColor ?: Color(0xFF0B57D0)

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_theme_palette_custom_lab),
                    subtitle = if (isCustomActive) {
                        "Personalizzato • ${customColor.toHexString()}"
                    } else {
                        stringResource(Res.string.ui_theme_palette_custom_lab_sub)
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(customColor)
                                .border(
                                    width = if (isCustomActive) 2.dp else 1.dp,
                                    color = if (isCustomActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isCustomActive) {
                                Icon(
                                    imageVector = LiquidIcons.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = LiquidIcons.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Icon(
                            imageVector = LiquidIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    onClick = onNavigateToColorLab,
                )
            }
        }

        // Sfondo Liquid, fisica e animazioni condividono un unico gruppo di preferenze.
        ThemeGraphicsPreferences(
            uiState = uiState,
            onBackgroundStyleSelected = onBackgroundStyleSelected,
            onReducedMotionChanged = onReducedMotionChanged,
            onEnabledChanged = onPageMotionEnabledChanged,
            onTransitionSelected = onPageTransitionSelected,
        )

        // 5. Ripristino Valori Predefiniti
        LiquidButton(
            text = stringResource(Res.string.ui_theme_reset_button),
            onClick = onReset,
            leadingIcon = {
                Icon(
                    imageVector = LiquidIcons.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
            variant = LiquidButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private data class ThemeMotionChoice(
    val transition: LiquidSwitcherTransition,
    val title: String,
    val description: String,
)

@Composable
private fun ThemeGraphicsPreferences(
    uiState: ThemeUiState,
    onBackgroundStyleSelected: (String) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onTransitionSelected: (LiquidSwitcherTransition) -> Unit,
) {
    val choices = UniMotion.options.map { option ->
        ThemeMotionChoice(option.transition, stringResource(option.title), stringResource(option.description))
    }
    val selected = choices.first { it.transition == UniMotion.selectedTransition(uiState.pageMotion) }
    UniSection {
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_theme_graphics_motion_group),
            subtitle = stringResource(Res.string.ui_theme_graphics_motion_sub),
        ) {
            LiquidBackgroundSelector(
                selectedEffect = uiState.selectedBackgroundStyle.toBackgroundEffect(),
                onEffectSelected = { onBackgroundStyleSelected(it.toStyleName()) },
                modifier = Modifier.fillMaxWidth(),
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_reduced_motion_title),
                subtitle = stringResource(Res.string.ui_theme_reduced_motion_sub),
                icon = LiquidIcons.Refresh,
                onClick = { onReducedMotionChanged(!uiState.reducedMotion) },
                trailingContent = {
                    LiquidSwitch(checked = uiState.reducedMotion, onCheckedChange = onReducedMotionChanged)
                },
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_page_motion_enabled),
                subtitle = stringResource(Res.string.ui_theme_page_motion_enabled_sub),
                icon = LiquidIcons.Refresh,
                onClick = { onEnabledChanged(!uiState.pageMotion.enabled) },
                trailingContent = {
                    LiquidSwitch(checked = uiState.pageMotion.enabled, onCheckedChange = onEnabledChanged)
                },
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
            LiquidPreferenceDropdown(
                title = stringResource(Res.string.ui_theme_page_motion_style),
                selectedItem = selected,
                items = choices,
                onItemSelected = { onTransitionSelected(it.transition) },
                itemLabel = { it.title },
                itemSubtitle = { it.description },
            )
            if (uiState.reducedMotion) {
                Text(
                    text = stringResource(Res.string.ui_theme_page_motion_reduced_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/**
 * Selettore Modalità Aspetto a Mockup Visivi (Sistema, Chiaro, Scuro).
 */
@Composable
private fun ThemeAppearanceSelector(
    selectedMode: AppThemeMode,
    accentColor: Color,
    onModeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = remember {
        listOf(
            Triple(AppThemeMode.System, "Sistema", "Segui disp."),
            Triple(AppThemeMode.Light, "Chiaro", "Luminoso"),
            Triple(AppThemeMode.Dark, "Scuro", "Contrasto"),
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        modes.forEach { (mode, title, subtitle) ->
            val isSelected = selectedMode == mode
            AppearanceMockupCard(
                mode = mode,
                title = title,
                subtitle = subtitle,
                isSelected = isSelected,
                accentColor = accentColor,
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Card singola con mockup visivo del layout dello smartphone, basata su [LiquidCard].
 */
@Composable
private fun AppearanceMockupCard(
    mode: AppThemeMode,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedContainerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent

    LiquidCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedRectangle(16.dp),
        contentPadding = 8.dp,
        colors = LiquidCardDefaults.colors(
            containerColor = animatedContainerColor,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Mockup Finestra Dispositivo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .clip(RoundedRectangle(10.dp))
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.12f),
                    shape = RoundedRectangle(10.dp),
                ),
        ) {
            when (mode) {
                AppThemeMode.Light -> {
                    // Finestra Chiara
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF2F4F7))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        // Barra superiore simulata
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(Capsule())
                                .background(Color.White),
                        )
                        // Card centrale simulata
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedRectangle(6.dp))
                                .background(Color.White)
                                .padding(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(6.dp)
                                    .clip(Capsule())
                                    .background(Color(0xFFD1D5DB)),
                            )
                        }
                        // Tasto d'azione simulato
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(12.dp)
                                .clip(Capsule())
                                .background(accentColor),
                        )
                    }
                }

                AppThemeMode.Dark -> {
                    // Finestra Scura
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF121418))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        // Barra superiore simulata
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(Capsule())
                                .background(Color(0xFF1E2128)),
                        )
                        // Card centrale simulata
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedRectangle(6.dp))
                                .background(Color(0xFF222630))
                                .padding(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(6.dp)
                                    .clip(Capsule())
                                    .background(Color(0xFF4B5563)),
                            )
                        }
                        // Tasto d'azione simulato
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(12.dp)
                                .clip(Capsule())
                                .background(accentColor),
                        )
                    }
                }

                AppThemeMode.System -> {
                    // Finestra Sistema Schermo Diviso (Day / Night)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Lato Giorno
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF2F4F7))
                                .padding(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(Capsule())
                                    .background(Color.White),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .clip(RoundedRectangle(6.dp))
                                    .background(Color.White),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(Capsule())
                                    .background(accentColor),
                            )
                        }

                        // Divisore centrale
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxWidth()
                                .background(Color.Gray.copy(alpha = 0.4f)),
                        )

                        // Lato Notte
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF121418))
                                .padding(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(Capsule())
                                    .background(Color(0xFF1E2128)),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .clip(RoundedRectangle(6.dp))
                                    .background(Color.White.copy(alpha = 0.08f)),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(Capsule())
                                    .background(accentColor.copy(alpha = 0.8f)),
                            )
                        }
                    }
                }
            }
        }

        // Titolo & Sottotitolo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }

        // Indicatore di selezione radio
        LiquidRadioButton(
            selected = isSelected,
            onClick = onClick,
        )
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
