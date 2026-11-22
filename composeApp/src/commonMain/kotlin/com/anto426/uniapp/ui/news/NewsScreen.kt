package com.anto426.uniapp.ui.news

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.NewsItem
import com.anto426.uniapp.news.presentation.NewsFilter
import com.anto426.uniapp.news.presentation.NewsUiState
import com.anto426.uniapp.ui.components.items.UniNewsCard
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun NewsScreen(
    uiState: NewsUiState,
    searchQuery: String = "",
    onTabSelected: (Int) -> Unit,
    onNewsSelected: (NewsItem) -> Unit,
) {
    val baseNews = uiState.visibleNews
    val trimmedQuery = searchQuery.trim()
    val filteredNews = remember(baseNews, trimmedQuery) {
        if (trimmedQuery.isBlank()) {
            baseNews
        } else {
            baseNews.filter { item ->
                item.title.contains(trimmedQuery, ignoreCase = true) ||
                    item.description.contains(trimmedQuery, ignoreCase = true) ||
                    item.fullContent.contains(trimmedQuery, ignoreCase = true) ||
                    item.category.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    val showFeaturedLead = trimmedQuery.isBlank() && filteredNews.size >= 2
    val featuredNews = if (showFeaturedLead) filteredNews.first() else null
    val listNews = if (showFeaturedLead) filteredNews.drop(1) else filteredNews
    val activeFilter = uiState.filters.getOrNull(uiState.selectedTab)

    UniScreenLazyColumn {
        if (uiState.filters.size > 1) {
            item(key = "news-filter-chips", contentType = "filter-chips") {
                UniHorizontalChipRow {
                    uiState.filters.forEachIndexed { index, filter ->
                        key(filter) {
                            val selected = uiState.selectedTab == index
                            val label = when (filter) {
                                NewsFilter.All -> stringResource(Res.string.ui_news_all)
                                NewsFilter.University -> stringResource(Res.string.ui_news_university)
                                NewsFilter.Department -> stringResource(Res.string.ui_news_department)
                                NewsFilter.Course -> stringResource(Res.string.ui_news_course)
                            }
                            val count = filter.category?.let { category ->
                                uiState.feed.byCategory[category.lowercase()]?.size ?: 0
                            } ?: uiState.news.size
                            LiquidChip(
                                label = label,
                                badge = count.toString(),
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { onTabSelected(index) },
                            )
                        }
                    }
                }
            }
        }

        if (filteredNews.isEmpty()) {
            item(key = "news-empty-${uiState.selectedTab}-$trimmedQuery", contentType = "empty-state") {
                UniExpressiveStatusState(
                    title = stringResource(Res.string.ui_news_empty_title),
                    description = if (trimmedQuery.isNotBlank()) {
                        stringResource(Res.string.ui_contacts_empty_desc)
                    } else {
                        stringResource(Res.string.ui_news_empty_desc)
                    },
                    inline = true,
                    mainIcon = if (trimmedQuery.isNotBlank()) LiquidIcons.Search else LiquidIcons.Notifications,
                )
            }
        } else {
            if (featuredNews != null) {
                item(key = "news-featured-${uiState.selectedTab}-${featuredNews.key}", contentType = "featured-news") {
                    UniAnimatedEntrance(order = 0) {
                        UniNewsCard(
                            news = featuredNews,
                            onClick = { onNewsSelected(featuredNews) },
                            featured = true,
                        )
                    }
                }
            }

            if (listNews.isNotEmpty()) {
                item(key = "news-section-header-${uiState.selectedTab}-$showFeaturedLead", contentType = "section-header") {
                    val activeLabel = when (activeFilter) {
                        NewsFilter.University -> stringResource(Res.string.ui_news_university)
                        NewsFilter.Department -> stringResource(Res.string.ui_news_department)
                        NewsFilter.Course -> stringResource(Res.string.ui_news_course)
                        NewsFilter.All, null -> stringResource(Res.string.ui_news_latest_header)
                    }
                    val countLabel = if (filteredNews.size == 1) {
                        stringResource(Res.string.ui_news_count_one)
                    } else {
                        stringResource(Res.string.ui_news_count_format, filteredNews.size)
                    }
                    UniAnimatedEntrance(order = if (featuredNews != null) 1 else 0) {
                        LiquidSectionHeader(
                            title = activeLabel,
                            subtitle = countLabel,
                        )
                    }
                }

                itemsIndexed(
                    items = listNews,
                    contentType = { _, _ -> "news-card" },
                    key = { index, news -> "${uiState.selectedTab}|${news.key.ifBlank { news.title }}|$index" },
                ) { index, news ->
                    UniAnimatedEntrance(
                        order = uniListEntranceOrder(index, offset = if (featuredNews != null) 1 else 0),
                    ) {
                        UniNewsCard(
                            news = news,
                            onClick = { onNewsSelected(news) },
                            homeCard = false,
                            featured = false,
                        )
                    }
                }
            }
        }
    }
}
