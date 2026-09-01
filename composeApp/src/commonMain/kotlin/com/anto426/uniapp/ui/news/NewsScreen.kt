package com.anto426.uniapp.ui.news

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.news.presentation.NewsUiState
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun NewsScreen(
    backdropState: Backdrop,
    uiState: NewsUiState,
    onTabSelected: (Int) -> Unit,
    onNewsSelected: (NewsItem) -> Unit,
) {
    val tabs = listOf(
        LiquidNavigationItem(label = stringResource(Res.string.ui_news_university), icon = LiquidIcons.Home),
        LiquidNavigationItem(label = stringResource(Res.string.ui_news_department), icon = LiquidIcons.Star),
        LiquidNavigationItem(label = stringResource(Res.string.ui_news_events), icon = LiquidIcons.Calendar)
    )
    UniScreenLazyColumn {
        item(key = "news-tabs") {
            LiquidTabBar(
                items = tabs,
                selectedIndex = uiState.selectedTab,
                onTabSelected = onTabSelected,
                backdropState = backdropState,
            )
        }
        item(key = "news-spacing") { Spacer(Modifier.height(8.dp)) }
        if (uiState.visibleNews.isEmpty()) {
            item(key = "news-empty-${uiState.selectedTab}") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_news_empty_title),
                    description = stringResource(Res.string.ui_news_empty_desc),
                    backdropState = backdropState,
                )
            }
        }
        itemsIndexed(
            items = uiState.visibleNews,
            key = { index, news -> "${uiState.selectedTab}|${news.title}|$index" },
        ) { _, news ->
            LiquidStatusCard(
                title = news.title,
                description = news.description,
                statusType = news.type,
                backdropState = backdropState,
                onClick = { onNewsSelected(news) },
            )
        }
    }
}
