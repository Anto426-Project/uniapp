package com.anto426.uniapp.ui.settings


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.uniapp.settings.presentation.RgbUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.ui.components.items.RgbSliderItem
import com.kyant.backdrop.Backdrop

@Composable
fun RgbScreen(
    backdropState: Backdrop,
    uiState: RgbUiState,
    onRedChanged: (Float) -> Unit,
    onGreenChanged: (Float) -> Unit,
    onBlueChanged: (Float) -> Unit,
) {
    UniScreenColumn {
        LiquidSectionHeader(title = stringResource(Res.string.ui_rgb_selector_title), subtitle = stringResource(Res.string.ui_rgb_selector_subtitle))

        // 1. Color Preview Card
        LiquidCard(
            backdropState = backdropState,
            containerColor = uiState.color,
            modifier = Modifier.height(160.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = uiState.hex,
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
                RgbSliderItem(stringResource(Res.string.ui_red), uiState.red, onRedChanged, Color.Red, backdropState)
                Spacer(Modifier.height(24.dp))
                RgbSliderItem(stringResource(Res.string.ui_green), uiState.green, onGreenChanged, Color.Green, backdropState)
                Spacer(Modifier.height(24.dp))
                RgbSliderItem(stringResource(Res.string.ui_blue), uiState.blue, onBlueChanged, Color.Blue, backdropState)
            }
        }
    }
}
