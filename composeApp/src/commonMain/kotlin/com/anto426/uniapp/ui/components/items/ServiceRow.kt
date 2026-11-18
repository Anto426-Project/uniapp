package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.model.services.ServiceData

@Composable
fun ServiceRow(items: List<ServiceData>, onNavigate: (String) -> Unit) {
    if (items.size == 1) {
        val item = items.first()
        val navTarget = item.id.ifBlank { item.title }
        ServiceWideItem(data = item) {
            onNavigate(navTarget)
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { item ->
            val navTarget = item.id.ifBlank { item.title }
            ServiceItem(
                data = item,
                modifier = Modifier.weight(1f),
            ) {
                onNavigate(navTarget)
            }
        }
    }
}
