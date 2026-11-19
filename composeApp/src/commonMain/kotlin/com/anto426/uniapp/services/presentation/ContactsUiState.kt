package com.anto426.uniapp.services.presentation

import androidx.compose.runtime.Immutable
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.presentation.FeatureLoadState

@Immutable
data class ContactsUiState(
    val categories: List<String> = emptyList(),
    val categoryCounts: List<Int> = emptyList(),
    val selectedCategoryIndex: Int = ALL_CATEGORIES_INDEX,
    val visibleContacts: List<ContactData> = emptyList(),
    val totalContactsCount: Int = 0,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    companion object {
        const val ALL_CATEGORIES_INDEX = -1
    }
}
