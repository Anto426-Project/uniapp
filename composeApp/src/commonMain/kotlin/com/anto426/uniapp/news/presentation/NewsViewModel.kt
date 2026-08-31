package com.anto426.uniapp.news.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.news.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NewsUiState(
    val selectedTab: Int = 0,
    val newsByTab: List<List<NewsItem>> = emptyList(),
    val selectedNews: NewsItem? = null,
) {
    val visibleNews: List<NewsItem> get() = newsByTab.getOrNull(selectedTab).orEmpty()
}

class NewsViewModel(newsByTab: List<List<NewsItem>>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(NewsUiState(newsByTab = newsByTab))
    val uiState: StateFlow<NewsUiState> = mutableUiState.asStateFlow()

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
