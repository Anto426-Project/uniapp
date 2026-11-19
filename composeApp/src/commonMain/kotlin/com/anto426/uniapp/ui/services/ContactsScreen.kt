package com.anto426.uniapp.ui.services

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.services.presentation.ContactsUiState
import com.anto426.uniapp.ui.components.items.ContactItem
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ContactsScreen(
    uiState: ContactsUiState,
    onCategorySelected: (Int) -> Unit,
    onContactClick: (ContactData) -> Unit = {},
) {
    val categoryFiltered = uiState.visibleContacts
    val isAllSelected = uiState.selectedCategoryIndex == ContactsUiState.ALL_CATEGORIES_INDEX
    val selectedCategory = if (isAllSelected) "" else uiState.categories.getOrNull(uiState.selectedCategoryIndex).orEmpty()

    UniScreenLazyColumn {
        if (uiState.categories.size > 1) {
            item(key = "contact-categories") {
                UniHorizontalChipRow {
                    LiquidChip(
                        label = stringResource(Res.string.ui_contacts_all_categories),
                        badge = uiState.totalContactsCount.toString(),
                        selected = isAllSelected,
                        leadingIcon = if (isAllSelected) LiquidIcons.Check else null,
                        onClick = { onCategorySelected(ContactsUiState.ALL_CATEGORIES_INDEX) },
                    )
                    uiState.categories.forEachIndexed { index, categoryName ->
                        val selected = index == uiState.selectedCategoryIndex
                        val count = uiState.categoryCounts.getOrNull(index)
                        LiquidChip(
                            label = categoryName,
                            badge = count?.toString(),
                            selected = selected,
                            leadingIcon = if (selected) LiquidIcons.Check else null,
                            onClick = { onCategorySelected(index) },
                        )
                    }
                }
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
                val subtitle = if (categoryFiltered.size == 1) {
                    stringResource(Res.string.ui_contacts_available_one)
                } else {
                    stringResource(Res.string.ui_contacts_available, categoryFiltered.size)
                }
                LiquidSectionHeader(
                    title = selectedCategory.ifBlank { stringResource(Res.string.ui_contacts_title) },
                    subtitle = subtitle,
                )
            }
            itemsIndexed(
                items = categoryFiltered,
                key = { index, item -> "contact|${item.email}|${item.name}|$index" },
            ) { _, contact ->
                ContactItem(contact) { onContactClick(contact) }
            }
        }
    }
}
