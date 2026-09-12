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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.selection.LiquidBackgroundSelector
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.settings.ThemeOption
import com.anto426.uniapp.settings.presentation.AppThemeMode
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Schermata Temi e personalizzazione visiva rinnovata.
 * Modalità aspetto rifinita, tavolozza colori con campioni rapidi inline e controlli di fluidità.
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
                        ThemeSystemIcon,
                    ),
                    Triple(
                        AppThemeMode.Light,
                        stringResource(Res.string.ui_theme_mode_light),
                        ThemeLightIcon,
                    ),
                    Triple(
                        AppThemeMode.Dark,
                        stringResource(Res.string.ui_theme_mode_dark),
                        ThemeDarkIcon,
                    )
                )

                modes.forEach { (mode, label, icon) ->
                    val isSelected = uiState.themeMode == mode
                    val animatedBg by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.Transparent,
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
                            .clip(RoundedRectangle(16.dp))
                            .background(animatedBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = animatedBorder,
                                shape = RoundedRectangle(16.dp)
                            )
                            .clickable { onThemeModeSelected(mode) }
                            .padding(vertical = 16.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Tavolozza Colori & Monet Seed
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
                                if (theme.isCustom && uiState.customColor == null) {
                                    onCustomColorSelected(Color(0xFF0B57D0))
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            // Campione cromatico con anello animato
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
                                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
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

                            Text(
                                text = theme.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (theme.isCustom && uiState.customColor != null) {
                                Text(
                                    text = uiState.customColor.toHexString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedRectangle(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.msg_attivo),
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

                    // Sezione campioni rapidi inline per colore personalizzato
                    if (theme.isCustom && isSelected) {
                        CustomColorInlinePicker(
                            currentColor = uiState.customColor ?: Color(0xFF0B57D0),
                            onColorSelected = onCustomColorSelected,
                            onOpenAdvanced = onNavigateToColorLab
                        )
                    }

                    if (index < uiState.themes.lastIndex) {
                        LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
            }
        }

        // 4. Sfondo Ottico & Sfumature Fluide
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_theme_engine_group)) {
            LiquidBackgroundSelector(
                selectedEffect = uiState.selectedBackgroundStyle.toBackgroundEffect(),
                onEffectSelected = { onBackgroundStyleSelected(it.toStyleName()) },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // 5. Fisica e Feedback Tattile
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

        // 6. Ripristino Valori Predefiniti
        LiquidButton(
            text = stringResource(Res.string.ui_theme_reset_button),
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Secondary
        )

        // Spaziatura di sicurezza per non coprire il pulsante con la floating bar
        Spacer(modifier = Modifier.height(110.dp))
    }
}



/**
 * Selettore compatto di colori rapidi per la modalità Personalizzato.
 */
@Composable
private fun CustomColorInlinePicker(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onOpenAdvanced: () -> Unit
) {
    val quickColors = listOf(
        Color(0xFF0B57D0), // UniMol / Royal Blue
        Color(0xFFE91E63), // Amaranto Vivo
        Color(0xFF00897B), // Smeraldo Luminoso
        Color(0xFFE65100), // Arancio Caldo
        Color(0xFF7E57C2), // Viola Luminoso
        Color(0xFFC2185B), // Rubino
        Color(0xFF00ACC1), // Turchese Vivo
        Color(0xFFD97706)  // Ambra
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedRectangle(14.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                shape = RoundedRectangle(14.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickColors.forEach { color ->
                val isSelected = currentColor.toHexByteString() == color.toHexByteString()
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(color) }
                        .then(
                            if (isSelected) {
                                Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            } else {
                                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = LiquidIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        LiquidButton(
            text = "Apri spettro completo & HEX",
            onClick = onOpenAdvanced,
            variant = LiquidButtonVariant.Tonal,
            size = LiquidButtonSize.Small,
            modifier = Modifier.fillMaxWidth()
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

private fun Color.toHexByteString(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return "$r-$g-$b"
}

private fun Int.toHexByte(): String {
    val hex = "0123456789ABCDEF"
    val h = hex[(this ushr 4) and 0x0F]
    val l = hex[this and 0x0F]
    return "$h$l"
}

private val ThemeLightIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ThemeLight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 7f)
        curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
        reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
        reflectiveCurveToRelative(5f, -2.24f, 5f, -5f)
        reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
        close()
        moveTo(2f, 13f)
        horizontalLineToRelative(2f)
        curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
        reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f)
        lineTo(2f, 11f)
        curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
        reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
        close()
        moveTo(20f, 13f)
        horizontalLineToRelative(2f)
        curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
        reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f)
        horizontalLineToRelative(-2f)
        curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
        reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
        close()
        moveTo(11f, 2f)
        verticalLineToRelative(2f)
        curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
        reflectiveCurveToRelative(1f, -0.45f, 1f, -1f)
        lineTo(13f, 2f)
        curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
        reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
        close()
        moveTo(11f, 20f)
        verticalLineToRelative(2f)
        curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
        reflectiveCurveToRelative(1f, -0.45f, 1f, -1f)
        verticalLineToRelative(-2f)
        curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
        reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
        close()
        moveTo(5.99f, 4.58f)
        curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.41f, 0f)
        reflectiveCurveToRelative(-0.39f, 1.03f, 0f, 1.41f)
        lineToRelative(1.06f, 1.06f)
        curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.41f, 0f)
        reflectiveCurveToRelative(0.39f, -1.03f, 0f, -1.41f)
        lineTo(5.99f, 4.58f)
        close()
        moveTo(18.36f, 16.95f)
        curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.41f, 0f)
        reflectiveCurveToRelative(-0.39f, 1.03f, 0f, 1.41f)
        lineToRelative(1.06f, 1.06f)
        curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.41f, 0f)
        reflectiveCurveToRelative(0.39f, -1.03f, 0f, -1.41f)
        lineToRelative(-1.06f, -1.06f)
        close()
        moveTo(7.05f, 18.36f)
        curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0f, -1.41f)
        reflectiveCurveToRelative(-1.03f, -0.39f, -1.41f, 0f)
        lineToRelative(-1.06f, 1.06f)
        curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0f, 1.41f)
        reflectiveCurveToRelative(1.03f, 0.39f, 1.41f, 0f)
        lineToRelative(1.06f, -1.06f)
        close()
        moveTo(19.42f, 6.05f)
        curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0f, -1.41f)
        reflectiveCurveToRelative(-1.03f, -0.39f, -1.41f, 0f)
        lineToRelative(-1.06f, 1.06f)
        curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0f, 1.41f)
        reflectiveCurveToRelative(1.03f, 0.39f, 1.41f, 0f)
        lineToRelative(1.06f, -1.06f)
        close()
    }.build()
}

private val ThemeDarkIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ThemeDark",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 3f)
        curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f)
        reflectiveCurveToRelative(4.03f, 9f, 9f, 9f)
        reflectiveCurveToRelative(9f, -4.03f, 9f, -9f)
        curveToRelative(0f, -0.46f, -0.04f, -0.92f, -0.1f, -1.36f)
        curveToRelative(-0.98f, 1.37f, -2.58f, 2.26f, -4.4f, 2.26f)
        curveToRelative(-3.03f, 0f, -5.5f, -2.47f, -5.5f, -5.5f)
        curveToRelative(0f, -1.82f, 0.89f, -3.42f, 2.26f, -4.4f)
        curveToRelative(-0.44f, -0.06f, -0.9f, -0.1f, -1.36f, -0.1f)
        close()
    }.build()
}

private val ThemeSystemIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ThemeSystem",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(16f, 1f)
        lineTo(8f, 1f)
        curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
        verticalLineToRelative(16f)
        curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
        horizontalLineToRelative(8f)
        curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
        lineTo(19f, 4f)
        curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
        close()
        moveTo(14f, 21f)
        horizontalLineToRelative(-4f)
        verticalLineToRelative(-1f)
        horizontalLineToRelative(4f)
        verticalLineToRelative(1f)
        close()
        moveTo(17.25f, 18f)
        lineTo(6.75f, 18f)
        lineTo(6.75f, 4f)
        horizontalLineToRelative(10.5f)
        verticalLineToRelative(14f)
        close()
    }.build()
}
