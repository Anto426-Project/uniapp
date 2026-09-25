package com.anto426.uniapp.ui.news

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.news.presentation.NewsUiState
import com.anto426.uniapp.news.presentation.NewsFilter
import com.anto426.uniapp.ui.components.items.UniNewsCard
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun NewsScreen(
    uiState: NewsUiState,
    onTabSelected: (Int) -> Unit,
    onNewsSelected: (NewsItem) -> Unit,
) {
    val tabs = uiState.filters.map { filter ->
        when (filter) {
            NewsFilter.All -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_all), icon = LiquidIcons.MenuBook)
            NewsFilter.University -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_university), icon = LiquidIcons.Home)
            NewsFilter.Department -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_department), icon = LiquidIcons.Star)
            NewsFilter.Course -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_course), icon = LiquidIcons.MenuBook)
        }
    }
    UniScreenLazyColumn {
        item(key = "news-tabs") {
            LiquidTabBar(
                items = tabs,
                selectedIndex = uiState.selectedTab,
                onTabSelected = onTabSelected,
            )
        }
        if (uiState.visibleNews.isEmpty()) {
            item(key = "news-empty-${uiState.selectedTab}") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_news_empty_title),
                    description = stringResource(Res.string.ui_news_empty_desc),
                )
            }
        }
        itemsIndexed(
            items = uiState.visibleNews,
            key = { index, news -> "${uiState.selectedTab}|${news.title}|$index" },
        ) { _, news ->
            UniNewsCard(
                news = news,
                onClick = { onNewsSelected(news) },
            )
        }
    }
}
