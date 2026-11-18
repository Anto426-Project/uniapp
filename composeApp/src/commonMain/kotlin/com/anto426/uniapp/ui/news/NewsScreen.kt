package com.anto426.uniapp.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.news.presentation.NewsFilter
import com.anto426.uniapp.news.presentation.NewsUiState
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
    val colorScheme = MaterialTheme.colorScheme
    val tabs = uiState.filters.map { filter ->
        when (filter) {
            NewsFilter.All -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_all), icon = LiquidIcons.MenuBook)
            NewsFilter.University -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_university), icon = LiquidIcons.Home)
            NewsFilter.Department -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_department), icon = LiquidIcons.Star)
            NewsFilter.Course -> LiquidNavigationItem(label = stringResource(Res.string.ui_news_course), icon = LiquidIcons.MenuBook)
        }
    }
    val visibleNews = uiState.visibleNews
    val activeTabLabel = tabs.getOrNull(uiState.selectedTab)?.label ?: stringResource(Res.string.ui_news)

    UniScreenLazyColumn {
        item(key = "news-tabs") {
            LiquidTabBar(
                items = tabs,
                selectedIndex = uiState.selectedTab,
                onTabSelected = onTabSelected,
            )
        }

        if (visibleNews.isEmpty()) {
            item(key = "news-empty-${uiState.selectedTab}") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_news_empty_title),
                    description = stringResource(Res.string.ui_news_empty_desc),
                )
            }
        } else {
            item(key = "news-count-${uiState.selectedTab}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = activeTabLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    LiquidBadge(
                        text = "${visibleNews.size}",
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.55f),
                        contentColor = colorScheme.primary,
                    )
                }
            }
        }

        itemsIndexed(
            items = visibleNews,
            contentType = { index, _ -> if (index == 0) "news-featured-card" else "news-card" },
            key = { index, news -> news.key.ifBlank { "${news.title}|$index" } },
        ) { index, news ->
            UniNewsCard(
                news = news,
                onClick = { onNewsSelected(news) },
                featured = index == 0,
            )
        }
    }
}
