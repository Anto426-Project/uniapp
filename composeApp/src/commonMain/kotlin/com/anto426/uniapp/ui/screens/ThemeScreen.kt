package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.selection.LiquidSelect
import com.anto426.liquidmonet.components.selection.LiquidSlider
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.kyant.backdrop.Backdrop

@Composable
fun ThemeScreen(backdropState: Backdrop, onOpenColors: () -> Unit) {
    var selectedThemeIndex by remember { mutableIntStateOf(0) }
    var glassIntensity by remember { mutableFloatStateOf(0.82f) }
    var effectSpeed by remember { mutableFloatStateOf(0.28f) }

    val backgroundStyles = listOf("Aurora", "Mesh Glow", "Orbital Pulse", "Radiant Beam")
    var selectedBackgroundStyle by remember { mutableStateOf(backgroundStyles[0]) }

    val themes = listOf(
        ThemeOption("Material You", "Dinamico", null),
        ThemeOption("Sapphire", "Cristallo", Color(0xFF4A90D9)),
        ThemeOption("Emerald", "Natura", Color(0xFF2ECC71)),
        ThemeOption("Personalizzato", "Tuo Stile", null, isCustom = true)
    )

    UniScreenColumn {
        // 1. Theme Cards - Horizontal Selection
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(themes) { index, theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = selectedThemeIndex == index,
                    onClick = {
                        selectedThemeIndex = index
                        if (theme.isCustom) onOpenColors()
                    },
                    backdropState = backdropState
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 2. Liquid & Effects Regulation Group (Including Background Style)
        LiquidPreferenceGroup(title = "Personalizzazione Visiva", backdropState = backdropState) {
            // Background Style as a Select but integrated in the group
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                LiquidSelect(
                    items = backgroundStyles,
                    selectedItem = selectedBackgroundStyle,
                    onItemSelected = { selectedBackgroundStyle = it },
                    label = stringResource(Res.string.ui_background_style),
                    backdropState = backdropState
                )
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.ui_liquid_intensity),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                LiquidSlider(
                    value = glassIntensity,
                    onValueChange = { glassIntensity = it },
                    backdropState = backdropState
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.ui_effect_speed),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                LiquidSlider(
                    value = effectSpeed,
                    onValueChange = { effectSpeed = it },
                    backdropState = backdropState
                )
            }
        }

        Text(
            text = stringResource(Res.string.ui_effects_info),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

data class ThemeOption(
    val name: String,
    val description: String,
    val color: Color?,
    val isCustom: Boolean = false
)

@Composable
fun ThemeCard(
    theme: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    backdropState: Backdrop
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        backdropState = backdropState,
        modifier = Modifier.width(140.dp).height(180.dp),
        shape = RoundedCornerShape(28.dp),
        onClick = onClick,
        containerColor = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent,
        contentPadding = 16.dp,
        interactiveGelatin = true
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Visual Indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (theme.color != null) {
                            Brush.sweepGradient(listOf(theme.color, theme.color.copy(alpha = 0.5f), theme.color))
                        } else if (theme.isCustom) {
                            Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary, colorScheme.tertiary))
                        } else {
                            // Material You multi-color representation
                            Brush.linearGradient(listOf(colorScheme.primary, colorScheme.tertiary))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (theme.isCustom) {
                    Icon(
                        imageVector = LiquidIcons.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface
                )
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
