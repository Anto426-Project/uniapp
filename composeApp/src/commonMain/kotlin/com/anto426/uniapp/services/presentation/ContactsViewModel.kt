package com.anto426.uniapp.services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.toContacts
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

class ContactsViewModel(
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private var contacts: List<ContactData> = emptyList()
    private var categories: List<String> = emptyList()
    private var selectedCategoryIndex = 0
    private var selectedCategoryName: String? = null
    private var groupByCity = false
    private var unspecifiedCategory = ""
    private var query = ""
    private val mutableUiState = MutableStateFlow(buildUiState())
    val uiState: StateFlow<ContactsUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Contacts)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            contacts = snapshot.require(UniAppDataRequests.Contacts).toContacts()
            updateCategories()
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
        if (categories.isNotEmpty()) {
            selectedCategoryIndex = index.coerceIn(categories.indices)
            selectedCategoryName = categories.getOrNull(selectedCategoryIndex)
        } else {
            selectedCategoryIndex = 0
            selectedCategoryName = null
        }
        publishState()
    }

    fun updateSearchQuery(value: String) {
        if (query == value) return
        query = value
        publishState()
    }

    private suspend fun updateCategories() {
        groupByCity = contacts.any { it.city.isNotBlank() }
        val values = contacts.map { categoryValue(it) }
        val knownCategories = values.filter(String::isNotBlank).distinct().sorted()
        unspecifiedCategory = getString(
            if (groupByCity) Res.string.ui_contact_location_unspecified
            else Res.string.ui_contact_department_unspecified,
        )
        categories = if (knownCategories.isEmpty()) emptyList() else {
            knownCategories + if (values.any(String::isBlank)) listOf(unspecifiedCategory) else emptyList()
        }
        val currentName = selectedCategoryName
        if (currentName != null && categories.contains(currentName)) {
            selectedCategoryIndex = categories.indexOf(currentName)
        } else {
            selectedCategoryIndex = 0
            selectedCategoryName = categories.firstOrNull()
        }
    }

    private fun categoryValue(contact: ContactData): String =
        (if (groupByCity) contact.city else contact.department).trim()

    private fun publishState() {
        mutableUiState.value = buildUiState()
    }

    private fun buildUiState(): ContactsUiState {
        val currentCategory = categories.getOrNull(selectedCategoryIndex).orEmpty()
        val byCategory = if (currentCategory.isNotEmpty()) {
            contacts.filter {
                categoryValue(it).ifBlank { unspecifiedCategory }.equals(currentCategory, ignoreCase = true)
            }
        } else {
            contacts
        }
        val normalizedQuery = query.trim()
        val visible =
            if (normalizedQuery.isEmpty()) {
                byCategory
            } else {
                byCategory.filter { contact ->
                    contact.name.contains(normalizedQuery, ignoreCase = true) ||
                        contact.role.contains(normalizedQuery, ignoreCase = true) ||
                        contact.department.contains(normalizedQuery, ignoreCase = true) ||
                        contact.email.contains(normalizedQuery, ignoreCase = true) ||
                        contact.phoneNumbers.any { it.contains(normalizedQuery, ignoreCase = true) } ||
                        contact.office.contains(normalizedQuery, ignoreCase = true) ||
                        contact.address.contains(normalizedQuery, ignoreCase = true) ||
                        contact.building.contains(normalizedQuery, ignoreCase = true) ||
                        contact.city.contains(normalizedQuery, ignoreCase = true) ||
                        contact.firstName.contains(normalizedQuery, ignoreCase = true) ||
                        contact.lastName.contains(normalizedQuery, ignoreCase = true)
                }
            }
        return ContactsUiState(
            categories = categories,
            selectedCategoryIndex = selectedCategoryIndex,
            visibleContacts = visible,
            loadState = if (contacts.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
        )
    }
}
