package com.anto426.uniapp.android.ui.components.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.selection.LiquidSlider
import com.kyant.backdrop.Backdrop

@Composable
fun RgbSliderItem(label: String, value: Float, onValueChange: (Float) -> Unit, tint: Color, backdropState: Backdrop) {
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
        Text("${(value * 255).toInt()}", color = tint, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LiquidSlider(value = value, onValueChange = onValueChange, tint = tint, backdropState = backdropState)
    }
}
