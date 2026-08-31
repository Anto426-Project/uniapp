package com.anto426.uniapp.services.presentation

import androidx.compose.runtime.Immutable
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.presentation.FeatureLoadState

@Immutable
data class ContactsUiState(
    val selectedCategoryIndex: Int = 0,
    val visibleContacts: List<ContactData> = emptyList(),
    val teachers: List<ContactData> = emptyList(),
    val secretariat: List<ContactData> = emptyList(),
    val services: List<ContactData> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)
