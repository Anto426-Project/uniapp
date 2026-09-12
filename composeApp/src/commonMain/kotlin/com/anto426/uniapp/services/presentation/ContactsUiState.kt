package com.anto426.uniapp.services.presentation

import androidx.compose.runtime.Immutable
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.presentation.FeatureLoadState

@Immutable
data class ContactsUiState(
    val categories: List<String> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val visibleContacts: List<ContactData> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)
