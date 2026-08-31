package com.anto426.uniapp.auth.presentation

import com.anto426.unisdk.backend.model.LoginCareerOption

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rememberCredentials: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val careers: List<LoginCareerOption> = emptyList(),
    val isForgotPasswordDialogVisible: Boolean = false,
)
