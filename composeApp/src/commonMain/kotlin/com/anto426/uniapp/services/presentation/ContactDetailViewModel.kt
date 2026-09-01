package com.anto426.uniapp.services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toContacts
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactDetailUiState(
    val contact: ContactData? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class ContactDetailViewModel(
    private val contactId: String,
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            try {
                val contact = dataSource.loadUniversityContacts(force).toContacts()
                    .firstOrNull { it.email == contactId || it.name == contactId }
                mutableUiState.value = ContactDetailUiState(
                    contact = contact,
                    loadState = if (contact == null) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = ContactDetailUiState(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare il contatto."),
                )
            }
        }
    }
}
