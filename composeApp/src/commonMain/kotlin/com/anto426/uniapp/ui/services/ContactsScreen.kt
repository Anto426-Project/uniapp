package com.anto426.uniapp.ui.services

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.services.presentation.ContactsUiState
import com.anto426.uniapp.ui.components.items.ContactItem
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ContactsScreen(
    uiState: ContactsUiState,
    onCategorySelected: (Int) -> Unit,
    onContactClick: (ContactData) -> Unit = {},
) {
    val tabs = uiState.categories.map { categoryName ->
        LiquidNavigationItem(label = categoryName)
    }

    val categoryFiltered = uiState.visibleContacts
    val selectedCategory = uiState.categories.getOrNull(uiState.selectedCategoryIndex).orEmpty()

    UniScreenLazyColumn {
        if (tabs.size > 1) {
            item(key = "contact-tabs") {
                LiquidTabBar(
                    items = tabs,
                    selectedIndex = uiState.selectedCategoryIndex,
                    onTabSelected = onCategorySelected,
                )
            }
        }

        // Contact Sections
        if (categoryFiltered.isEmpty()) {
            item(key = "contacts-empty") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_contacts_empty_title),
                    description = stringResource(Res.string.ui_contacts_empty_desc),
                )
            }
        } else {
            item(key = "filtered-header") {
                LiquidSectionHeader(
                    title = selectedCategory.ifBlank { stringResource(Res.string.ui_contacts_title) },
                    subtitle = stringResource(Res.string.ui_contacts_available, categoryFiltered.size),
                )
            }
            itemsIndexed(categoryFiltered, key = { index, item -> "contact|${item.email}|${item.name}|$index" }) { _, contact ->
                ContactItem(contact) { onContactClick(contact) }
            }
        }
    }
}
