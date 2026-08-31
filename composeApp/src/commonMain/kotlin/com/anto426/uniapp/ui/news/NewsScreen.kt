package com.anto426.uniapp.ui.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.news.presentation.NewsUiState
import com.anto426.uniapp.ui.components.items.NewsList
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun NewsScreen(
    backdropState: Backdrop,
    uiState: NewsUiState,
    onTabSelected: (Int) -> Unit,
    onNewsSelected: (com.anto426.uniapp.model.news.NewsItem) -> Unit,
    onDismissNews: () -> Unit,
) {
    val tabs = listOf(
        LiquidNavigationItem(label = "Ateneo", icon = LiquidIcons.Home),
        LiquidNavigationItem(label = "Dipartimento", icon = LiquidIcons.Star),
        LiquidNavigationItem(label = "Eventi", icon = LiquidIcons.Calendar)
    )
    UniScreenColumn {
        LiquidTabBar(items = tabs, selectedIndex = uiState.selectedTab, onTabSelected = onTabSelected, backdropState = backdropState)
        Spacer(Modifier.height(8.dp))
        NewsList(uiState.visibleNews, backdropState, onNewsSelected)
    }
    uiState.selectedNews?.let { news ->
        LiquidSheet(onDismissRequest = onDismissNews, title = news.title, subtitle = news.description, backdropState = backdropState) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(news.fullContent, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
            }
        }
    }
}
