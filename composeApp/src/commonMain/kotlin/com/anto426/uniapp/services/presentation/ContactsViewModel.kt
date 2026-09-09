package com.anto426.uniapp.services.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toContacts
import com.anto426.uniapp.model.services.ContactCategory
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private var contacts: List<ContactData> = emptyList()
    private var selectedCategoryIndex = 0
    private var query = ""
    private val mutableUiState = MutableStateFlow(buildUiState())
    val uiState: StateFlow<ContactsUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Contacts)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            contacts = snapshot.require(UniAppDataRequests.Contacts).toContacts()
            publishState()
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_la_rubrica)),
            )
        }

    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }

    fun selectCategory(index: Int) {
        selectedCategoryIndex = index.coerceIn(0, LAST_CATEGORY_INDEX)
        publishState()
    }

    fun updateSearchQuery(value: String) {
        if (query == value) return
        query = value
        publishState()
    }

    private fun publishState() {
        mutableUiState.value = buildUiState()
    }

    private fun buildUiState(): ContactsUiState {
        val byCategory =
            when (selectedCategoryIndex) {
                1 -> contacts.filter { it.category == ContactCategory.TEACHERS }
                2 -> contacts.filter { it.category == ContactCategory.SECRETARIAT }
                3 -> contacts.filter { it.category == ContactCategory.SERVICES }
                else -> contacts
            }
        val normalizedQuery = query.trim()
        val visible =
            if (normalizedQuery.isEmpty()) {
                byCategory
            } else {
                byCategory.filter { contact ->
                    contact.name.contains(normalizedQuery, ignoreCase = true) ||
                        contact.role.contains(normalizedQuery, ignoreCase = true) ||
                        contact.department.contains(normalizedQuery, ignoreCase = true)
                }
            }
        return ContactsUiState(
            selectedCategoryIndex = selectedCategoryIndex,
            visibleContacts = visible,
            teachers = visible.filter { it.category == ContactCategory.TEACHERS },
            secretariat = visible.filter { it.category == ContactCategory.SECRETARIAT },
            services = visible.filter { it.category == ContactCategory.SERVICES },
            loadState = if (contacts.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
        )
    }

    private companion object {
        const val LAST_CATEGORY_INDEX = 3
    }
}
