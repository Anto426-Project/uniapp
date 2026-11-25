package com.anto426.uniapp.services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.data.contract.toContacts
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

class ContactsViewModel(
    private val dataSource: UniGateway,
) : ViewModel() {
    private var contacts: List<ContactData> = emptyList()
    private var categories: List<String> = emptyList()
    private var selectedCategoryIndex = ContactsUiState.ALL_CATEGORIES_INDEX
    private var selectedCategoryName: String? = null
    private var groupByCity = false
    private var unspecifiedCategory = ""
    private var query = ""
    private val mutableUiState = MutableStateFlow(buildUiState())
    val uiState: StateFlow<ContactsUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Contacts)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            contacts = snapshot.require(UniDatasets.Contacts).toContacts()
            updateCategories()
            publishState()
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_la_rubrica) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }

    fun selectCategory(index: Int) {
        if (index == ContactsUiState.ALL_CATEGORIES_INDEX || categories.isEmpty()) {
            selectedCategoryIndex = ContactsUiState.ALL_CATEGORIES_INDEX
            selectedCategoryName = null
        } else {
            selectedCategoryIndex = index.coerceIn(categories.indices)
            selectedCategoryName = categories.getOrNull(selectedCategoryIndex)
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
        val knownCategories = values
            .filter(String::isNotBlank)
            .distinctBy { it.lowercase() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        unspecifiedCategory = readAppString(
            if (groupByCity) Res.string.ui_contact_location_unspecified
            else Res.string.ui_contact_department_unspecified,
        )
        categories = if (knownCategories.isEmpty()) emptyList() else {
            knownCategories + if (values.any(String::isBlank)) listOf(unspecifiedCategory) else emptyList()
        }
        val currentName = selectedCategoryName
        val matchedIndex = currentName?.let { target ->
            categories.indexOfFirst { it.equals(target, ignoreCase = true) }
        } ?: -1
        if (matchedIndex >= 0) {
            selectedCategoryIndex = matchedIndex
            selectedCategoryName = categories[matchedIndex]
        } else {
            selectedCategoryIndex = ContactsUiState.ALL_CATEGORIES_INDEX
            selectedCategoryName = null
        }
    }

    private fun categoryValue(contact: ContactData): String =
        (if (groupByCity) contact.city else contact.department).trim()

    private fun publishState() {
        mutableUiState.value = buildUiState()
    }

    private fun buildUiState(): ContactsUiState {
        val currentCategory = if (selectedCategoryIndex == ContactsUiState.ALL_CATEGORIES_INDEX) {
            ""
        } else {
            categories.getOrNull(selectedCategoryIndex).orEmpty()
        }
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
        val categoryCounts = categories.map { categoryName ->
            contacts.count {
                categoryValue(it).ifBlank { unspecifiedCategory }.equals(categoryName, ignoreCase = true)
            }
        }
        return ContactsUiState(
            categories = categories,
            categoryCounts = categoryCounts,
            selectedCategoryIndex = selectedCategoryIndex,
            visibleContacts = visible,
            totalContactsCount = contacts.size,
            loadState = if (contacts.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
        )
    }
}
