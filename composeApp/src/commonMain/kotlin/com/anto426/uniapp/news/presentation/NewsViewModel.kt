package com.anto426.uniapp.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val newsByTab: List<List<NewsItem>> = listOf(
        UniAppInitialData.fallbackNews,
        UniAppInitialData.fallbackNews.filterIndexed { index, _ -> index % 2 == 0 },
        UniAppInitialData.fallbackNews.filterIndexed { index, _ -> index % 2 != 0 },
    ),
    val selectedNews: NewsItem? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Content,
    val errorMessage: String? = null,
) {
    val visibleNews: List<NewsItem> get() = newsByTab.getOrNull(selectedTab).orEmpty()
}

class NewsViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
            try {
                val fetchedNews = runCatching { dataSource.loadUniversityNews(force).toNewsItems() }.getOrNull()
                val news = if (!fetchedNews.isNullOrEmpty()) fetchedNews else UniAppInitialData.fallbackNews
                mutableUiState.value = mutableUiState.value.copy(
                    newsByTab = listOf(news, news.filterIndexed { index, _ -> index % 2 == 0 }, news.filterIndexed { index, _ -> index % 2 != 0 }),
                    selectedNews = null,
                    loadState = FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val news = UniAppInitialData.fallbackNews
                mutableUiState.value = mutableUiState.value.copy(
                    newsByTab = listOf(news, news.filterIndexed { index, _ -> index % 2 == 0 }, news.filterIndexed { index, _ -> index % 2 != 0 }),
                    loadState = FeatureLoadState.Content,
                    errorMessage = null,
                )
            }
        }
    }

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
