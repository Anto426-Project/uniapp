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

    private fun updateCategories() {
        categories = deriveCategories(contacts)
        val currentName = selectedCategoryName
        if (currentName != null && categories.contains(currentName)) {
            selectedCategoryIndex = categories.indexOf(currentName)
        } else {
            selectedCategoryIndex = 0
            selectedCategoryName = categories.firstOrNull()
        }
    }

    private fun deriveCategories(items: List<ContactData>): List<String> {
        val distinctCities = items.mapNotNull { it.city.trim().takeIf(String::isNotBlank) }.distinct().sorted()
        if (distinctCities.isNotEmpty()) {
            return distinctCities
        }
        val distinctDepts = items.mapNotNull { it.department.trim().takeIf(String::isNotBlank) }.distinct().sorted()
        if (distinctDepts.isNotEmpty()) {
            return distinctDepts
        }
        return emptyList()
    }

    private fun publishState() {
        mutableUiState.value = buildUiState()
    }

    private fun buildUiState(): ContactsUiState {
        val currentCategory = categories.getOrNull(selectedCategoryIndex).orEmpty()
        val byCategory = if (currentCategory.isNotEmpty()) {
            contacts.filter {
                it.city.equals(currentCategory, ignoreCase = true) ||
                    it.department.equals(currentCategory, ignoreCase = true)
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
                        contact.phone.contains(normalizedQuery, ignoreCase = true) ||
                        contact.office.contains(normalizedQuery, ignoreCase = true)
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
