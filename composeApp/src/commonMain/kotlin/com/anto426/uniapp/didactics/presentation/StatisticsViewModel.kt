package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatisticsViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = mutableUiState.asStateFlow()

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTabIndex = index.coerceIn(0, LAST_TAB_INDEX))
    }

    private companion object {
        const val LAST_TAB_INDEX = 2
    }
}
