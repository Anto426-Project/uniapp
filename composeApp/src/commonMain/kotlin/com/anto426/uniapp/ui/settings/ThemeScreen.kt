package com.anto426.uniapp.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.components.selection.LiquidSlider
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.settings.presentation.ThemeUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

private data class MonetPalettePreset(
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val themeIndex: Int,
)

private val palettePresets = listOf(
    MonetPalettePreset(
        name = "Sapphire",
        description = "Cobalto & Oceano",
        primaryColor = Color(0xFF0061A4),
        secondaryColor = Color(0xFF535F70),
        themeIndex = 1,
    ),
    MonetPalettePreset(
        name = "Emerald",
        description = "Smeraldo & Giada",
        primaryColor = Color(0xFF006C4C),
        secondaryColor = Color(0xFF4C6356),
        themeIndex = 2,
    ),
    MonetPalettePreset(
        name = "Crimson",
        description = "Rosso Rubino",
        primaryColor = Color(0xFFBA1A1A),
        secondaryColor = Color(0xFF904A42),
        themeIndex = 1,
    ),
    MonetPalettePreset(
        name = "Violet",
        description = "Ametista Reale",
        primaryColor = Color(0xFF6B4EA2),
        secondaryColor = Color(0xFF625B71),
        themeIndex = 1,
    ),
    MonetPalettePreset(
        name = "Sunset",
        description = "Ambra & Oro Caldo",
        primaryColor = Color(0xFF8B5000),
        secondaryColor = Color(0xFF715B41),
        themeIndex = 1,
    ),
    MonetPalettePreset(
        name = "Cyberpunk",
        description = "Ciano & Magenta Neon",
        primaryColor = Color(0xFF00B4D8),
        secondaryColor = Color(0xFF7209B7),
        themeIndex = 1,
    ),
    MonetPalettePreset(
        name = "Nordic Frost",
        description = "Azzurro Polare & Menta",
        primaryColor = Color(0xFF0077B6),
        secondaryColor = Color(0xFF0096C7),
        themeIndex = 1,
    ),
    MonetPalettePreset(
        name = "Forest Pine",
        description = "Verde Pino & Muschio",
        primaryColor = Color(0xFF1B4332),
        secondaryColor = Color(0xFF2D6A4F),
        themeIndex = 2,
    ),
)

@Composable
fun ThemeScreen(
    backdropState: Backdrop,
    uiState: ThemeUiState? = null,
    onThemeSelected: ((Int) -> Unit)? = null,
    onBackgroundStyleSelected: ((String) -> Unit)? = null,
    onGlassIntensityChanged: ((Float) -> Unit)? = null,
    onEffectSpeedChanged: ((Float) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    val currentThemeIndex = uiState?.selectedThemeIndex ?: 0
    val isMaterialYou = currentThemeIndex == 0
    var selectedThemeMode by remember { mutableIntStateOf(0) } // 0: Sistema, 1: Chiaro, 2: Scuro, 3: AMOLED
    var selectedPaletteIndex by remember { mutableIntStateOf(0) }

    var selectedBgEffect by remember { mutableStateOf(uiState?.selectedBackgroundStyle ?: "Aurora") }
    var bgIntensity by remember { mutableFloatStateOf(uiState?.glassIntensity ?: 0.82f) }
    var bgSpeed by remember { mutableFloatStateOf(uiState?.effectSpeed ?: 0.28f) }
    var bgSaturation by remember { mutableFloatStateOf(1.0f) }

    var showVignette by remember { mutableStateOf(true) }
    var showCyberGrid by remember { mutableStateOf(true) }
    var showHarmonicWaves by remember { mutableStateOf(true) }
    var showAnalogGrain by remember { mutableStateOf(false) }

    var selectedGlassStyle by remember { mutableIntStateOf(0) } // 0: Frosted, 1: Ultra Clear, 2: Smoked, 3: Gelatin
    var glassOpacity by remember { mutableFloatStateOf(0.55f) }
    var blurRadius by remember { mutableFloatStateOf(24f) }
    var edgeSpecularGlow by remember { mutableStateOf(true) }
    var edgeGlowIntensity by remember { mutableFloatStateOf(0.75f) }

    var hapticFeedbackLevel by remember { mutableIntStateOf(1) } // 0: Off, 1: Delicato, 2: Accentuato, 3: Dinamico
    var reducedMotion by remember { mutableStateOf(false) }

    var cornerRadiusType by remember { mutableIntStateOf(1) } // 0: 16dp, 1: 24dp, 2: 32dp, 3: Adattivo
    var fontScaleType by remember { mutableIntStateOf(1) } // 0: Compatto 90%, 1: Normale 100%, 2: Grande 115%, 3: Extra 130%
    var performanceMode by remember { mutableIntStateOf(0) } // 0: Ultra GPU, 1: High 120Hz, 2: Eco, 3: Massime Prestazioni
    var pauseBgOnSleep by remember { mutableStateOf(true) }

    UniScreenColumn {
        // ==========================================
        // 1. MODALITÀ TEMA & CONTRASTO (2 a 2)
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_theme_mode_group),
            backdropState = backdropState,
        ) {
            val themeModes = listOf(
                Triple(stringResource(Res.string.ui_theme_mode_system), stringResource(Res.string.ui_theme_mode_system_sub), LiquidIcons.Settings),
                Triple(stringResource(Res.string.ui_theme_mode_light), stringResource(Res.string.ui_theme_mode_light_sub), LiquidIcons.Star),
                Triple(stringResource(Res.string.ui_theme_mode_dark), stringResource(Res.string.ui_theme_mode_dark_sub), LiquidIcons.Time),
                Triple(stringResource(Res.string.ui_theme_mode_amoled), stringResource(Res.string.ui_theme_mode_amoled_sub), LiquidIcons.Lock),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                themeModes.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEachIndexed { colIndex, (title, subtitle, icon) ->
                            val index = rowIndex * 2 + colIndex
                            val isSelected = selectedThemeMode == index
                            OptionChoiceTile(
                                title = title,
                                subtitle = subtitle,
                                isSelected = isSelected,
                                onClick = { selectedThemeMode = index },
                                leadingIcon = icon,
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_material_you_title),
                subtitle = stringResource(Res.string.ui_theme_material_you_sub),
                icon = LiquidIcons.Star,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = isMaterialYou,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onThemeSelected?.invoke(0)
                            } else {
                                onThemeSelected?.invoke(1)
                            }
                        },
                        backdropState = backdropState,
                    )
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // ==========================================
        // 2. TAVOLOZZA COLORI & MONET PRESETS (2 a 2)
        // ==========================================
        AnimatedVisibility(visible = !isMaterialYou) {
            LiquidPreferenceGroup(
                title = stringResource(Res.string.ui_theme_palette_group),
                backdropState = backdropState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    palettePresets.chunked(2).forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowItems.forEachIndexed { colIndex, preset ->
                                val index = rowIndex * 2 + colIndex
                                val isSelected = selectedPaletteIndex == index
                                OptionChoiceTile(
                                    title = preset.name,
                                    subtitle = preset.description,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedPaletteIndex = index
                                        onThemeSelected?.invoke(preset.themeIndex)
                                    },
                                    gradientColors = listOf(preset.primaryColor, preset.secondaryColor),
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_theme_palette_custom_lab),
                    subtitle = stringResource(Res.string.ui_theme_palette_custom_lab_sub),
                    icon = LiquidIcons.Edit,
                    backdropState = backdropState,
                    onClick = { onThemeSelected?.invoke(3) },
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // ==========================================
        // 3. MOTORE GRAFICO SFONDO LIQUID (2 a 2)
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_theme_engine_group),
            backdropState = backdropState,
        ) {
            val bgEffects = listOf(
                Triple("Aurora", "Cosmic Aurora", "Onde fluide boreali"),
                Triple("Radiant Beam", "Radiant Beam", "Spotlight & Cyber Grid"),
                Triple("Mesh Glow", "Mesh Glow", "Gradienti 4-quadranti"),
                Triple("Orbital Pulse", "Orbital Pulse", "Sfere armoniche"),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                bgEffects.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEach { (styleKey, label, desc) ->
                            val isSelected = selectedBgEffect.equals(styleKey, ignoreCase = true)
                            OptionChoiceTile(
                                title = label,
                                subtitle = desc,
                                isSelected = isSelected,
                                onClick = {
                                    selectedBgEffect = styleKey
                                    onBackgroundStyleSelected?.invoke(styleKey)
                                },
                                leadingIcon = when (styleKey) {
                                    "Aurora" -> LiquidIcons.Star
                                    "Radiant Beam" -> LiquidIcons.Refresh
                                    "Mesh Glow" -> LiquidIcons.Info
                                    else -> LiquidIcons.Settings
                                },
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            // Sliders Sfondo
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_theme_glow_intensity),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = "${(bgIntensity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LiquidSlider(
                    value = bgIntensity,
                    onValueChange = {
                        bgIntensity = it
                        onGlassIntensityChanged?.invoke(it)
                    },
                    backdropState = backdropState,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_theme_anim_speed),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = "${((bgSpeed * 10).toInt() / 10f)}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LiquidSlider(
                    value = bgSpeed / 2f,
                    onValueChange = {
                        val realSpeed = (it * 2f).coerceIn(0.05f, 2f)
                        bgSpeed = realSpeed
                        onEffectSpeedChanged?.invoke(realSpeed)
                    },
                    backdropState = backdropState,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_theme_saturation),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = "${(bgSaturation * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LiquidSlider(
                    value = (bgSaturation - 0.5f) / 1.0f,
                    onValueChange = { bgSaturation = 0.5f + it * 1.0f },
                    backdropState = backdropState,
                )
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_vignette_title),
                subtitle = stringResource(Res.string.ui_theme_vignette_sub),
                icon = LiquidIcons.Home,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = showVignette,
                        onCheckedChange = { showVignette = it },
                        backdropState = backdropState,
                    )
                },
            )
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_cyber_grid_title),
                subtitle = stringResource(Res.string.ui_theme_cyber_grid_sub),
                icon = LiquidIcons.Settings,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = showCyberGrid,
                        onCheckedChange = { showCyberGrid = it },
                        backdropState = backdropState,
                    )
                },
            )
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_waves_title),
                subtitle = stringResource(Res.string.ui_theme_waves_sub),
                icon = LiquidIcons.Time,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = showHarmonicWaves,
                        onCheckedChange = { showHarmonicWaves = it },
                        backdropState = backdropState,
                    )
                },
            )
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_grain_title),
                subtitle = stringResource(Res.string.ui_theme_grain_sub),
                icon = LiquidIcons.Star,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = showAnalogGrain,
                        onCheckedChange = { showAnalogGrain = it },
                        backdropState = backdropState,
                    )
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // ==========================================
        // 4. MATERIALE VETRO & TRASPARENZA (2 a 2)
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_theme_glass_group),
            backdropState = backdropState,
        ) {
            val glassStyles = listOf(
                Triple("Frosted Glass", "Smerigliato morbido", LiquidIcons.Star),
                Triple("Clear Glass", "Cristallino nitido", LiquidIcons.Refresh),
                Triple("Smoked Dark", "Fumé polarizzato", LiquidIcons.Lock),
                Triple("Liquid Gelatin", "Reattivo elastico", LiquidIcons.Info),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                glassStyles.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEachIndexed { colIndex, (name, subtitle, icon) ->
                            val index = rowIndex * 2 + colIndex
                            val isSelected = selectedGlassStyle == index
                            OptionChoiceTile(
                                title = name,
                                subtitle = subtitle,
                                isSelected = isSelected,
                                onClick = { selectedGlassStyle = index },
                                leadingIcon = icon,
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_theme_glass_opacity),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = "${(glassOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LiquidSlider(
                    value = glassOpacity,
                    onValueChange = { glassOpacity = it },
                    backdropState = backdropState,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_theme_glass_blur),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = "${blurRadius.toInt()} dp",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LiquidSlider(
                    value = blurRadius / 48f,
                    onValueChange = { blurRadius = it * 48f },
                    backdropState = backdropState,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_theme_glass_glow),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = "${(edgeGlowIntensity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LiquidSlider(
                    value = edgeGlowIntensity,
                    onValueChange = { edgeGlowIntensity = it },
                    backdropState = backdropState,
                )
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_glass_specular_title),
                subtitle = stringResource(Res.string.ui_theme_glass_specular_sub),
                icon = LiquidIcons.Star,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = edgeSpecularGlow,
                        onCheckedChange = { edgeSpecularGlow = it },
                        backdropState = backdropState,
                    )
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // ==========================================
        // 5. FISICA & FEEDBACK TATTILE (2 a 2)
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_theme_haptics_group),
            backdropState = backdropState,
        ) {
            val hapticOptions = listOf(
                Triple("Disattivato", "Nessuna vibrazione", LiquidIcons.Lock),
                Triple("Delicato", "Tocco morbido sottile", LiquidIcons.Star),
                Triple("Accentuato", "Risposta netta piena", LiquidIcons.Phone),
                Triple("Dinamico", "Adattivo al tocco", LiquidIcons.Refresh),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                hapticOptions.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEachIndexed { colIndex, (title, subtitle, icon) ->
                            val index = rowIndex * 2 + colIndex
                            val isSelected = hapticFeedbackLevel == index
                            OptionChoiceTile(
                                title = title,
                                subtitle = subtitle,
                                isSelected = isSelected,
                                onClick = { hapticFeedbackLevel = index },
                                leadingIcon = icon,
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_reduced_motion_title),
                subtitle = stringResource(Res.string.ui_theme_reduced_motion_sub),
                icon = LiquidIcons.Refresh,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = reducedMotion,
                        onCheckedChange = {
                            reducedMotion = it
                            if (it) onEffectSpeedChanged?.invoke(0.02f)
                            else onEffectSpeedChanged?.invoke(0.28f)
                        },
                        backdropState = backdropState,
                    )
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // ==========================================
        // 6. GEOMETRIA, TIPOGRAFIA E LAYOUT (2 a 2)
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_theme_geometry_group),
            backdropState = backdropState,
        ) {
            val cornerModes = listOf(
                Triple("16 dp", "Standard Android", LiquidIcons.Settings),
                Triple("24 dp", "Liquid Curvature", LiquidIcons.Star),
                Triple("32 dp", "Super-Squircle", LiquidIcons.Refresh),
                Triple("Adattivo", "Scala dinamica schermo", LiquidIcons.Phone),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                cornerModes.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEachIndexed { colIndex, (title, subtitle, icon) ->
                            val index = rowIndex * 2 + colIndex
                            val isSelected = cornerRadiusType == index
                            OptionChoiceTile(
                                title = title,
                                subtitle = subtitle,
                                isSelected = isSelected,
                                onClick = { cornerRadiusType = index },
                                leadingIcon = icon,
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            val fontScaleModes = listOf(
                Triple("Compatto", "90% densità alta", LiquidIcons.Edit),
                Triple("Normale", "100% predefinito", LiquidIcons.Star),
                Triple("Grande", "115% alta leggibilità", LiquidIcons.Settings),
                Triple("Extra", "130% accessibilità", LiquidIcons.AccountCircle),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                fontScaleModes.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEachIndexed { colIndex, (title, subtitle, icon) ->
                            val index = rowIndex * 2 + colIndex
                            val isSelected = fontScaleType == index
                            OptionChoiceTile(
                                title = title,
                                subtitle = subtitle,
                                isSelected = isSelected,
                                onClick = { fontScaleType = index },
                                leadingIcon = icon,
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ==========================================
        // 7. PARAMETRI SISTEMA & RENDERING GPU (2 a 2)
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_theme_rendering_group),
            backdropState = backdropState,
        ) {
            val perfModes = listOf(
                Triple("Ultra GPU", "AGSL Snell Shader", LiquidIcons.Star),
                Triple("High 120Hz", "Refresh rate massimo", LiquidIcons.Refresh),
                Triple("Eco Batteria", "GPU a basso consumo", LiquidIcons.Time),
                Triple("Massime", "Priorità grafica piena", LiquidIcons.Notifications),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                perfModes.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEachIndexed { colIndex, (title, subtitle, icon) ->
                            val index = rowIndex * 2 + colIndex
                            val isSelected = performanceMode == index
                            OptionChoiceTile(
                                title = title,
                                subtitle = subtitle,
                                isSelected = isSelected,
                                onClick = { performanceMode = index },
                                leadingIcon = icon,
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_pause_sleep_title),
                subtitle = stringResource(Res.string.ui_theme_pause_sleep_sub),
                icon = LiquidIcons.Lock,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = pauseBgOnSleep,
                        onCheckedChange = { pauseBgOnSleep = it },
                        backdropState = backdropState,
                    )
                },
            )
        }

        Spacer(Modifier.height(8.dp))

        // ==========================================
        // 8. AZIONI RAPIDE & RESET
        // ==========================================
        LiquidButton(
            text = stringResource(Res.string.ui_theme_reset_button),
            onClick = {
                selectedThemeMode = 0
                selectedPaletteIndex = 0
                selectedBgEffect = "Aurora"
                bgIntensity = 0.82f
                bgSpeed = 0.28f
                bgSaturation = 1.0f
                showVignette = true
                showCyberGrid = true
                showHarmonicWaves = true
                showAnalogGrain = false
                selectedGlassStyle = 0
                glassOpacity = 0.55f
                blurRadius = 24f
                edgeSpecularGlow = true
                edgeGlowIntensity = 0.75f
                hapticFeedbackLevel = 1
                reducedMotion = false
                cornerRadiusType = 1
                fontScaleType = 1
                performanceMode = 0
                pauseBgOnSleep = true
                onThemeSelected?.invoke(0)
                onBackgroundStyleSelected?.invoke("Aurora")
                onGlassIntensityChanged?.invoke(0.82f)
                onEffectSpeedChanged?.invoke(0.28f)
            },
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Secondary,
            backdropState = backdropState,
        )
    }
}

@Composable
private fun OptionChoiceTile(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    gradientColors: List<Color>? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) colorScheme.primary.copy(alpha = 0.12f)
                else colorScheme.onSurface.copy(alpha = 0.04f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (gradientColors != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(gradientColors))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
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
                } else if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 32.dp,
                            iconSize = 16.dp,
                            containerColor = if (isSelected) colorScheme.primary.copy(alpha = 0.2f) else colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                        ),
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                    )
                }
            }

            if (isSelected && gradientColors == null) {
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
