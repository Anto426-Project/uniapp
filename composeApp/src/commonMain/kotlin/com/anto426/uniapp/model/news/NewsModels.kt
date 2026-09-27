package com.anto426.uniapp.model.news

import androidx.compose.runtime.Immutable
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType

@Immutable
data class NewsItem(
    val title: String,
    val description: String,
    val fullContent: String,
    val type: LiquidStatusType = LiquidStatusType.Info,
    val category: String = "",
    val publishedAt: String = "",
    val key: String = "",
    val sourceUrl: String? = null,
)

/** Prepared once per feed update; category selection reuses the existing item lists. */
data class NewsFeed(
    val items: List<NewsItem> = emptyList(),
    val byCategory: Map<String, List<NewsItem>> = emptyMap(),
)
