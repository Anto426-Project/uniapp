package com.anto426.uniapp.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.selection.LiquidBackgroundSelector
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Schermata Temi e personalizzazione visiva Liquid Monet.
 * Offre controlli per modalità tema, tavolozze Monet,
 * sfondi ottici e fisica del movimento.
 * La personalizzazione del colore apre la schermata dedicata Laboratorio Colori.
 */
@Composable
fun ThemeScreen(
    uiState: ThemeUiState,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onThemeSelected: (Int) -> Unit,
    onBackgroundStyleSelected: (String) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onReset: () -> Unit,
    onCustomColorSelected: (Color) -> Unit = {},
    onNavigateToColorLab: () -> Unit = {}
) {
    UniScreenColumn {
        // 1. Modalità Aspetto (Sistema / Chiaro / Scuro)
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_theme_mode_group)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    Triple(
                        AppThemeMode.System,
                        stringResource(Res.string.ui_theme_mode_system),
                        LiquidIcons.Settings
                    ),
                    Triple(
                        AppThemeMode.Light,
                        stringResource(Res.string.ui_theme_mode_light),
                        LiquidIcons.Star
                    ),
                    Triple(
                        AppThemeMode.Dark,
                        stringResource(Res.string.ui_theme_mode_dark),
                        LiquidIcons.Time
                    )
                )

                modes.forEach { (mode, label, icon) ->
                    val isSelected = uiState.themeMode == mode
                    val animatedBg by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                        label = "modeBg"
                    )
                    val animatedBorder by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        label = "modeBorder"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(animatedBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = animatedBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onThemeModeSelected(mode) }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Tavolozza Colori & Monet Seed (Palette Visive)
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_theme_palette_group)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                uiState.themes.forEachIndexed { index, theme ->
                    val isSelected = index == uiState.selectedThemeIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeSelected(index)
                                if (theme.isCustom) {
                                    onNavigateToColorLab()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            // Campione cromatico rotondo
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
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
                                                        Color(0xFF4285F4)
                                                    )
                                                )
                                            )
                                        }
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else {
                                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = LiquidIcons.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = theme.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                val descriptionText = if (theme.isCustom && uiState.customColor != null) {
                                    "Colore su misura (${uiState.customColor.toHexString()})"
                                } else {
                                    theme.description
                                }
                                Text(
                                    text = descriptionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Attivo",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (theme.isCustom) {
                                Icon(
                                    imageVector = LiquidIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (index < uiState.themes.lastIndex) {
                        LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
            }
        }

        // 3. Sfondo Ottico & Sfumature Fluide
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_theme_engine_group)) {
            LiquidBackgroundSelector(
                selectedEffect = uiState.selectedBackgroundStyle.toBackgroundEffect(),
                onEffectSelected = { onBackgroundStyleSelected(it.toStyleName()) },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // 4. Fisica e Feedback Tattile
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_theme_haptics_group)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_reduced_motion_title),
                subtitle = stringResource(Res.string.ui_theme_reduced_motion_sub),
                icon = LiquidIcons.Refresh,
                trailingContent = {
                    LiquidSwitch(
                        checked = uiState.reducedMotion,
                        onCheckedChange = onReducedMotionChanged
                    )
                }
            )
        }

        // 5. Ripristino Valori Predefiniti
        LiquidButton(
            text = stringResource(Res.string.ui_theme_reset_button),
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Secondary
        )
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
