package com.anto426.uniapp.account.presentation

import com.anto426.uniapp.account.model.UniAccountSummary

data class AccountSwitcherUiState(
    val accounts: List<UniAccountSummary> = emptyList(),
    val activeAccountId: String? = null,
    val profileImages: Map<String, ByteArray> = emptyMap(),
    val activatingAccountId: String? = null,
    val activatingProfileId: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
