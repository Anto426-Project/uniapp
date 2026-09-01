package com.anto426.uniapp.navigation.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class AppShellUiState(
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isNavigationBarVisible: Boolean = true,
)
