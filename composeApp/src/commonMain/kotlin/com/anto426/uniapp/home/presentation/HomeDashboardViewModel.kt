package com.anto426.uniapp.home.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.news.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeDashboardViewModel(
    news: List<NewsItem>,
    quickActions: List<QuickActionItem>,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            HomeDashboardUiState(
                news = news,
                quickActions = quickActions,
            ),
        )
    val uiState: StateFlow<HomeDashboardUiState> = mutableUiState.asStateFlow()

    fun showNews(news: NewsItem) {
        mutableUiState.update { it.copy(selectedNews = news) }
    }

    fun dismissNews() {
        mutableUiState.update { it.copy(selectedNews = null) }
    }

    fun showNextNews() {
        mutableUiState.update { state ->
            state.copy(
                activeNewsIndex =
                    if (state.news.isEmpty()) 0 else (state.activeNewsIndex + 1) % state.news.size,
            )
        }
    }

    fun showPreviousNews() {
        mutableUiState.update { state ->
            state.copy(
                activeNewsIndex =
                    when {
                        state.news.isEmpty() -> 0
                        state.activeNewsIndex > 0 -> state.activeNewsIndex - 1
                        else -> state.news.lastIndex
                    },
            )
        }
    }

    fun toggleCustomization() {
        mutableUiState.update { it.copy(isCustomizing = !it.isCustomizing) }
    }

    fun finishCustomization() {
        mutableUiState.update { it.copy(isCustomizing = false) }
    }

    fun toggleQuickAction(actionId: String) {
        mutableUiState.update { state ->
            val selected = state.selectedActionIds.toMutableSet()
            if (actionId in selected) {
                if (selected.size > MIN_ACTIONS) selected.remove(actionId)
            } else {
                selected.add(actionId)
            }
            state.copy(selectedActionIds = selected)
        }
    }

    private companion object {
        const val MIN_ACTIONS = 2
    }
}
