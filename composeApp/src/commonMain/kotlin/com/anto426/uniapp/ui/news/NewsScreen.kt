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
                            LiquidChip(
                                label = label,
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
            item(key = "news-section-header-${uiState.selectedTab}", contentType = "section-header") {
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
                UniAnimatedEntrance(order = 0) {
                    LiquidSectionHeader(
                        title = activeLabel,
                        subtitle = countLabel,
                    )
                }
            }

            itemsIndexed(
                items = filteredNews,
                contentType = { _, _ -> "news-card" },
                key = { index, news -> "${uiState.selectedTab}|${news.key.ifBlank { news.title }}|$index" },
            ) { index, news ->
                UniAnimatedEntrance(
                    order = uniListEntranceOrder(index, offset = 0),
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
