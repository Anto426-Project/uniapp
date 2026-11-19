package com.anto426.uniapp.ui.news

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.news.presentation.NewsFilter
import com.anto426.uniapp.news.presentation.NewsUiState
import com.anto426.uniapp.ui.components.items.UniNewsCard
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun NewsScreen(
    uiState: NewsUiState,
    onTabSelected: (Int) -> Unit,
    onNewsSelected: (NewsItem) -> Unit,
) {
    val visibleNews = uiState.visibleNews

    UniScreenLazyColumn {
        if (uiState.filters.size > 1) {
            item(key = "news-filter-chips") {
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
                                label = "$label ($count)",
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { onTabSelected(index) },
                            )
                        }
                    }
                }
            }
        }

        if (visibleNews.isEmpty()) {
            item(key = "news-empty-${uiState.selectedTab}") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_news_empty_title),
                    description = stringResource(Res.string.ui_news_empty_desc),
                )
            }
        } else {
            val activeFilter = uiState.filters.getOrNull(uiState.selectedTab)
            item(key = "news-section-header-${uiState.selectedTab}") {
                val activeLabel = when (activeFilter) {
                    NewsFilter.University -> stringResource(Res.string.ui_news_university)
                    NewsFilter.Department -> stringResource(Res.string.ui_news_department)
                    NewsFilter.Course -> stringResource(Res.string.ui_news_course)
                    NewsFilter.All, null -> stringResource(Res.string.ui_news_latest_header)
                }
                val countLabel = if (visibleNews.size == 1) {
                    stringResource(Res.string.ui_news_count_one)
                } else {
                    stringResource(Res.string.ui_news_count_format, visibleNews.size)
                }
                LiquidSectionHeader(
                    title = activeLabel,
                    subtitle = countLabel,
                )
            }

            itemsIndexed(
                items = visibleNews,
                contentType = { _, _ -> "news-card" },
                key = { index, news -> news.key.ifBlank { "${news.title}|$index" } },
            ) { _, news ->
                UniNewsCard(
                    news = news,
                    onClick = { onNewsSelected(news) },
                    homeCard = true,
                )
            }
        }
    }
}
