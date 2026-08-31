package com.anto426.uniapp.home.presentation

import com.anto426.uniapp.model.home.dashboard.HomeDashboardData

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(val data: HomeDashboardData) : HomeUiState

    data class Error(val message: String, val canRetry: Boolean = true) : HomeUiState
}
