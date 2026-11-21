package com.anto426.uniapp.auth.presentation

import com.anto426.uniapp.account.model.UniLoginProfileOption

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rememberCredentials: Boolean = true,
    val isLoading: Boolean = false,
    val careers: List<UniLoginProfileOption> = emptyList(),
    val isForgotPasswordDialogVisible: Boolean = false,
)
