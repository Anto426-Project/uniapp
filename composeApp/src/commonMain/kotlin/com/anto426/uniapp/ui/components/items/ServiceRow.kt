package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.anto426.uniapp.model.services.ServiceData
import com.anto426.uniapp.ui.components.display.UniBentoTileRow
import com.anto426.uniapp.ui.components.display.UniBlockDefaults

@Composable
fun ServiceRow(items: List<ServiceData>, onNavigate: (String) -> Unit) {
    when (items.size) {
        0 -> return
        1 -> {
            val item = items.first()
            val navTarget = item.id.ifBlank { item.title }
            ServiceWideItem(data = item) {
                onNavigate(navTarget)
            }
        }
        2 -> {
            val first = items[0]
            val second = items[1]
            val firstTarget = first.id.ifBlank { first.title }
            val secondTarget = second.id.ifBlank { second.title }
            UniBentoTileRow(
                item1 = {
                    ServiceItem(data = first) {
                        onNavigate(firstTarget)
                    }
                },
                item2 = {
                    ServiceItem(data = second) {
                        onNavigate(secondTarget)
                    }
                },
            )
        }
        else -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                horizontalArrangement = Arrangement.spacedBy(UniBlockDefaults.TileGap),
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
    }
}
