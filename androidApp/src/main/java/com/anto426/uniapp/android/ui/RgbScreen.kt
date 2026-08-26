package com.anto426.uniapp.android.ui

import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.anto426.antoui.components.cards.AntoCard
import com.kyant.backdrop.Backdrop

@Composable
fun RgbScreen(backdropState: Backdrop) {
    var red by remember { mutableFloatStateOf(1f) }
    var green by remember { mutableFloatStateOf(0f) }
    var blue by remember { mutableFloatStateOf(.85f) }
    UniScreenColumn {
        UniSectionTitle("Primario", "Seleziona Colore RGB")
        AntoCard(backdropState = backdropState, containerColor = Color(red, green, blue)) { Text("#FF00D9", color = Color.White) }
        Text("ROSSO", color = Color.White); Slider(red, { red = it })
        Text("VERDE", color = Color.White); Slider(green, { green = it })
        Text("BLU", color = Color.White); Slider(blue, { blue = it })
    }
}
