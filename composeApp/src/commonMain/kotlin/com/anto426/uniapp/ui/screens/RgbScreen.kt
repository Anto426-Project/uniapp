package com.anto426.uniapp.ui.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.ui.components.items.RgbSliderItem
import com.kyant.backdrop.Backdrop

@Composable
fun RgbScreen(backdropState: Backdrop) {
    var red by remember { mutableFloatStateOf(0.16f) }
    var green by remember { mutableFloatStateOf(0.47f) }
    var blue by remember { mutableFloatStateOf(1f) }

    val currentColor = Color(red, green, blue)
    val hexString = "#${(red * 255).toInt().toHexByte()}${(green * 255).toInt().toHexByte()}${(blue * 255).toInt().toHexByte()}"

    UniScreenColumn {
        LiquidSectionTitle(title = stringResource(Res.string.ui_rgb_selector_title), subtitle = stringResource(Res.string.ui_rgb_selector_subtitle))

        // 1. Color Preview Card
        LiquidCard(
            backdropState = backdropState,
            containerColor = currentColor,
            modifier = Modifier.height(160.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = hexString,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Text(
                    text = stringResource(Res.string.ui_preview_color),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 2. RGB Sliders
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_rgb_components), backdropState = backdropState) {
            Column(modifier = Modifier.padding(16.dp)) {
                RgbSliderItem(stringResource(Res.string.ui_red), red, { red = it }, Color.Red, backdropState)
                Spacer(Modifier.height(24.dp))
                RgbSliderItem(stringResource(Res.string.ui_green), green, { green = it }, Color.Green, backdropState)
                Spacer(Modifier.height(24.dp))
                RgbSliderItem(stringResource(Res.string.ui_blue), blue, { blue = it }, Color.Blue, backdropState)
            }
        }
    }
}

private fun Int.toHexByte(): String = coerceIn(0, 255).toString(16).uppercase().padStart(2, '0')
