package com.anto426.uniapp.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toNewsItems
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NewsFilter(val category: String?) {
    All(null), University("Ateneo"), Department("Dipartimento"), Course("Corso di studi"),
}

data class NewsUiState(
    val selectedTab: Int = 0,
    val filters: List<NewsFilter> = listOf(NewsFilter.All),
    val news: List<NewsItem> = emptyList(),
    val selectedNews: NewsItem? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val visibleNews: List<NewsItem> get() = filters.getOrNull(selectedTab)?.category?.let { category ->
        news.filter { it.category.equals(category, ignoreCase = true) }
    } ?: news
}

class NewsViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.News)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val news = snapshot.require(UniAppDataRequests.News)
            val items = news.toNewsItems()
            val filters = listOf(NewsFilter.All) + NewsFilter.entries.drop(1).filter { filter ->
                items.any { it.category.equals(filter.category, ignoreCase = true) }
            }
            val selected = mutableUiState.value.filters.getOrNull(mutableUiState.value.selectedTab)
            mutableUiState.value = mutableUiState.value.copy(
                filters = filters,
                selectedTab = filters.indexOfFirst { it == selected }.coerceAtLeast(0),
                news = items,
                loadState = if (news.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                errorMessage = null,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.ui_news_empty_desc)),
            )
        }
    }

    fun refresh(force: Boolean = false) { sharedData.refresh(dataRequests, force) }

    fun selectTab(index: Int) {
        val lastIndex = mutableUiState.value.filters.lastIndex.coerceAtLeast(0)
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, lastIndex))
    }

    fun showNews(news: NewsItem) {
        mutableUiState.value = mutableUiState.value.copy(selectedNews = news)
    }

    fun dismissNews() {
        mutableUiState.value = mutableUiState.value.copy(selectedNews = null)
    }
}
