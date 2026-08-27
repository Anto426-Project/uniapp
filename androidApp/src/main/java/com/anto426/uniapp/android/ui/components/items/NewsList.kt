package com.anto426.uniapp.android.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.uniapp.android.ui.models.NewsItem
import com.kyant.backdrop.Backdrop

@Composable
fun NewsList(items: List<NewsItem>, backdropState: Backdrop, onNewsClick: (NewsItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            LiquidStatusCard(title = item.title, description = item.description, statusType = item.type, backdropState = backdropState, onClick = { onNewsClick(item) })
        }
    }
}
