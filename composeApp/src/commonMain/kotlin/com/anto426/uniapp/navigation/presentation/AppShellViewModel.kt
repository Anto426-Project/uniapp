package com.anto426.uniapp.navigation.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.navigation.model.AppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppShellViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(AppShellUiState())
    val uiState: StateFlow<AppShellUiState> = mutableUiState.asStateFlow()

    fun routeChanged(route: AppRoute) {
        mutableUiState.update {
            it.copy(searchQuery = "", isSearchActive = false, isNavigationBarVisible = true)
        }
    }

    fun updateSearchQuery(query: String) {
        mutableUiState.update { it.copy(searchQuery = query) }
    }

    fun setSearchActive(active: Boolean) {
        mutableUiState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = it.searchQuery.takeIf { active }.orEmpty(),
            )
        }
    }

    fun updateNavigationBarVisibility(visible: Boolean) {
        mutableUiState.update { it.copy(isNavigationBarVisible = visible) }
    }
}
