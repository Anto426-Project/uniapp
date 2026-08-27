package com.anto426.uniapp.android.ui.screens


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
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.android.ui.components.items.RgbSliderItem
import com.kyant.backdrop.Backdrop

@Composable
fun RgbScreen(backdropState: Backdrop) {
    var red by remember { mutableFloatStateOf(0.16f) }
    var green by remember { mutableFloatStateOf(0.47f) }
    var blue by remember { mutableFloatStateOf(1f) }

    val currentColor = Color(red, green, blue)
    val hexString = String.format("#%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())

    UniScreenColumn {
        LiquidSectionTitle(title = stringResource(R.string.ui_rgb_selector_title), subtitle = stringResource(R.string.ui_rgb_selector_subtitle))

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
                    text = stringResource(R.string.ui_preview_color),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 2. RGB Sliders
        LiquidPreferenceGroup(title = stringResource(R.string.ui_rgb_components), backdropState = backdropState) {
            Column(modifier = Modifier.padding(16.dp)) {
                RgbSliderItem(stringResource(R.string.ui_red), red, { red = it }, Color.Red, backdropState)
                Spacer(Modifier.height(24.dp))
                RgbSliderItem(stringResource(R.string.ui_green), green, { green = it }, Color.Green, backdropState)
                Spacer(Modifier.height(24.dp))
                RgbSliderItem(stringResource(R.string.ui_blue), blue, { blue = it }, Color.Blue, backdropState)
            }
        }
    }
}
