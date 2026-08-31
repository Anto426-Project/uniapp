package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.model.services.ServiceData
import com.kyant.backdrop.Backdrop

@Composable
fun ServiceRow(items: List<ServiceData>, backdropState: Backdrop, onNavigate: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().graphicsLayer(clip = false), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            ServiceItem(item, backdropState, Modifier.weight(1f)) { onNavigate(item.title) }
        }
        if (items.size == 1) Spacer(Modifier.weight(1f))
    }
}
