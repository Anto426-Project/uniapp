package com.anto426.uniapp.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.model.news.NewsFeed
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NewsFilter(val category: String?) {
    All(null), University("Ateneo"), Department("Dipartimento"), Course("Corso di studi"),
}

data class NewsUiState(
    val selectedTab: Int = 0,
    val feed: NewsFeed = NewsFeed(),
    val selectedNews: NewsItem? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val news: List<NewsItem> get() = feed.items
    val filters: List<NewsFilter> = feed.filters()
    val visibleNews: List<NewsItem> get() = filters.getOrNull(selectedTab)?.category?.let { category ->
        feed.byCategory[category.lowercase()].orEmpty()
    } ?: news
}

private fun NewsFeed.filters(): List<NewsFilter> =
    listOf(NewsFilter.All) + NewsFilter.entries.drop(1).filter {
        !byCategory[it.category?.lowercase()].isNullOrEmpty()
    }

class NewsViewModel(
    dataSource: UniAppDataSource,
    private val preparationDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val sharedData = dataSource.sharedData(viewModelScope)
    private val mutableUiState = MutableStateFlow(
        sharedData.newsFeed?.let { feed ->
            NewsUiState(feed = feed, loadState = if (feed.items.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content)
        } ?: NewsUiState(),
    )
    val uiState: StateFlow<NewsUiState> = mutableUiState.asStateFlow()

    private val dataRequests = listOf(UniAppDataRequests.News)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val news = snapshot.require(UniAppDataRequests.News)
            val feed = sharedData.prepareNews(news, preparationDispatcher)
            val filters = feed.filters()
            val selected = mutableUiState.value.filters.getOrNull(mutableUiState.value.selectedTab)
            mutableUiState.value = mutableUiState.value.copy(
                selectedTab = filters.indexOfFirst { it == selected }.coerceAtLeast(0),
                feed = feed,
                loadState = if (news.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                errorMessage = null,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { getString(Res.string.ui_news_empty_desc) },
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
