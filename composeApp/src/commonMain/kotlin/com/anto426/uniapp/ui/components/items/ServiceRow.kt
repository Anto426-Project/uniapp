package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.model.services.ServiceData

@Composable
fun ServiceRow(items: List<ServiceData>, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .graphicsLayer(clip = false),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            val navTarget = item.id.ifBlank { item.title }
            ServiceItem(
                data = item,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                onNavigate(navTarget)
            }
        }
        if (items.size == 1) Spacer(Modifier.weight(1f))
    }
}
