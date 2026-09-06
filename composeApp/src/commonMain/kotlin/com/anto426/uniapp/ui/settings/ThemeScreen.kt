package com.anto426.uniapp.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.pickers.LiquidColorPicker
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
 * Offre controlli interattivi in tempo reale per modalità tema, tavolozze Monet dinamiche,
 * gestione e decisione del colore personalizzato, sfondi ottici a sfumature pure e fisica del movimento.
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
    var showPickerSheet by remember { mutableStateOf(false) }
    val activeCustomColor = uiState.customColor ?: Color(0xFF2979FF)

    val curatedAccents = remember {
        listOf(
            Color(0xFF2979FF) to "Electric Blue",
            Color(0xFF00B4D8) to "Cyber Teal",
            Color(0xFF00E676) to "Neon Mint",
            Color(0xFFFFD600) to "Sunny Gold",
            Color(0xFFFF9100) to "Vivid Amber",
            Color(0xFFFF1744) to "Laser Ruby",
            Color(0xFFD500F9) to "Neon Magenta",
            Color(0xFF7C4DFF) to "Deep Violet"
        )
    }

    UniScreenColumn {
        val selectedTheme = uiState.themes.getOrElse(uiState.selectedThemeIndex) {
            uiState.themes.first()
        }

        // 1. Hero Live Preview Card
        LiquidCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Liquid Monet ${com.anto426.unisdk.platform.AppInfoProvider.current.module("liquid-monet")?.version.orEmpty()}".trim(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${selectedTheme.name} • ${uiState.selectedBackgroundStyle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = when (uiState.themeMode) {
                                AppThemeMode.System -> stringResource(Res.string.ui_theme_mode_system)
                                AppThemeMode.Light -> stringResource(Res.string.ui_theme_mode_light)
                                AppThemeMode.Dark -> stringResource(Res.string.ui_theme_mode_dark)
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Barrette cromatiche dinamiche della palette attiva
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                }

                // Anteprima rapida componenti UI reali
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Pulsante Primario",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Superficie Tono",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Accento",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // 2. Modalità Aspetto (Selettore a 3 Vie Diretto)
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

        Spacer(Modifier.height(4.dp))

        // 3. Tavolozza Colori & Monet Seed (Palette Visive)
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
                            .clickable { onThemeSelected(index) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                    }

                    if (index < uiState.themes.lastIndex) {
                        LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // 4. Gestione & Selezione del Colore Personalizzato
        LiquidPreferenceGroup(title = "Gestione Colore Personalizzato") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info bar con campione attivo, codice Hex e stato
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(activeCustomColor)
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Colore Seme Attivo",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = activeCustomColor.toHexString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val isCustomThemeActive = uiState.selectedThemeIndex == 5 ||
                        (uiState.themes.getOrNull(uiState.selectedThemeIndex)?.isCustom == true)

                    if (isCustomThemeActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "In Uso",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Tavolozza Rapida di Accenti Vivaci ad 1 Tocco
                Text(
                    text = "Tonalità rapide consigliate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    curatedAccents.forEach { (accentColor, label) ->
                        val isCurrentAccent = activeCustomColor.value == accentColor.value
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                                .border(
                                    width = if (isCurrentAccent) 2.5.dp else 1.dp,
                                    color = if (isCurrentAccent) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { onCustomColorSelected(accentColor) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentAccent) {
                                Icon(
                                    imageVector = LiquidIcons.Check,
                                    contentDescription = label,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                LiquidHorizontalDivider()

                // Pulsante per aprire il Selettore Avanzato (Spettro, Sliders, Categorie)
                LiquidButton(
                    text = "Apri Selettore Avanzato (Spettro & Hex)",
                    onClick = { showPickerSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Secondary
                )
            }
        }

        // Modal Sheet Selettore Colore Avanzato
        if (showPickerSheet) {
            LiquidSheet(
                onDismissRequest = { showPickerSheet = false },
                title = "Selettore Colore Avanzato",
                subtitle = "Spettro 2D, cursori HSV e codice esadecimale"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    LiquidColorPicker(
                        selectedColor = activeCustomColor,
                        onColorSelected = { newColor ->
                            onCustomColorSelected(newColor)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // 5. Sfondo Ottico & Sfumature Fluide
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_theme_engine_group)) {
            LiquidBackgroundSelector(
                selectedEffect = uiState.selectedBackgroundStyle.toBackgroundEffect(),
                onEffectSelected = { onBackgroundStyleSelected(it.toStyleName()) },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        // 6. Fisica e Feedback Tattile
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

        Spacer(Modifier.height(8.dp))

        // 7. Ripristino Valori Predefiniti
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
