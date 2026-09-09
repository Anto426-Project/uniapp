package com.anto426.uniapp.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.data.toNewsItems
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewsUiState(
    val selectedTab: Int = 0,
    val newsByTab: List<List<NewsItem>> = listOf(emptyList(), emptyList(), emptyList()),
    val selectedNews: NewsItem? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val visibleNews: List<NewsItem> get() = newsByTab.getOrNull(selectedTab).orEmpty()
}

class NewsViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.News)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests) { snapshot ->
        try {
            val news = snapshot.require(UniAppDataRequests.News)
            val tabs = listOf(
                news.toNewsItems(),
                news.filter { it.category?.contains("dipartiment", ignoreCase = true) == true }.toNewsItems(),
                news.filter { it.category?.contains("event", ignoreCase = true) == true }.toNewsItems(),
            )
            mutableUiState.value = mutableUiState.value.copy(
                newsByTab = tabs,
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
        val lastIndex = mutableUiState.value.newsByTab.lastIndex.coerceAtLeast(0)
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, lastIndex))
    }

    fun showNews(news: NewsItem) {
        mutableUiState.value = mutableUiState.value.copy(selectedNews = news)
    }

    fun dismissNews() {
        mutableUiState.value = mutableUiState.value.copy(selectedNews = null)
    }
}
