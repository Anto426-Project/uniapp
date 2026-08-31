package com.anto426.uniapp.home.presentation

import androidx.compose.runtime.Immutable
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.news.NewsItem

@Immutable
data class HomeDashboardUiState(
    val news: List<NewsItem>,
    val quickActions: List<QuickActionItem>,
    val selectedNews: NewsItem? = null,
    val activeNewsIndex: Int = 0,
    val selectedActionIds: Set<String> = DEFAULT_ACTION_IDS,
    val isCustomizing: Boolean = false,
) {
    val activeNews: NewsItem?
        get() = news.getOrNull(activeNewsIndex.coerceIn(0, news.lastIndex.coerceAtLeast(0)))

    val visibleQuickActions: List<QuickActionItem>
        get() = quickActions.filter { it.id in selectedActionIds }

    companion object {
        val DEFAULT_ACTION_IDS =
            setOf("libretto", "appelli", "didattica", "trasporti", "tasse", "rubrica")
    }
}
