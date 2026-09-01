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
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ContactsScreen(
    backdropState: Backdrop,
    uiState: ContactsUiState,
    onCategorySelected: (Int) -> Unit,
    onContactClick: (ContactData) -> Unit = {},
) {
    val tabs = listOf(
        LiquidNavigationItem(label = stringResource(Res.string.ui_home_news_all)),
        LiquidNavigationItem(label = stringResource(Res.string.ui_teachers_title)),
        LiquidNavigationItem(label = stringResource(Res.string.ui_secretariat_title)),
        LiquidNavigationItem(label = stringResource(Res.string.ui_offices_support_title))
    )

    val categoryFiltered = uiState.visibleContacts
    val teachers = uiState.teachers
    val secretariat = uiState.secretariat
    val services = uiState.services

    UniScreenLazyColumn {
        item(key = "contact-tabs") {
            LiquidTabBar(
                items = tabs,
                selectedIndex = uiState.selectedCategoryIndex,
                onTabSelected = onCategorySelected,
                backdropState = backdropState,
            )
        }

        // 2. Contact Sections
        if (categoryFiltered.isEmpty()) {
            item(key = "contacts-empty") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_contacts_empty_title),
                    description = stringResource(Res.string.ui_contacts_empty_desc),
                    backdropState = backdropState,
                )
            }
        } else if (uiState.selectedCategoryIndex == 0) {
            if (teachers.isNotEmpty()) {
                item(key = "teachers-header") {
                    LiquidSectionHeader(
                        title = stringResource(Res.string.ui_teachers_title),
                        subtitle = stringResource(Res.string.ui_contacts_count, teachers.size)
                    )
                }
                itemsIndexed(teachers, key = { index, item -> "teacher|${item.email}|${item.name}|$index" }) { _, contact ->
                    ContactItem(contact, backdropState) { onContactClick(contact) }
                }
            }

            if (secretariat.isNotEmpty()) {
                item(key = "secretariat-header") {
                    LiquidSectionHeader(
                        title = stringResource(Res.string.ui_secretariat_title),
                        subtitle = stringResource(Res.string.ui_secretariat_count, secretariat.size)
                    )
                }
                itemsIndexed(secretariat, key = { index, item -> "secretariat|${item.email}|${item.name}|$index" }) { _, contact ->
                    ContactItem(contact, backdropState) { onContactClick(contact) }
                }
            }

            if (services.isNotEmpty()) {
                item(key = "services-header") {
                    LiquidSectionHeader(
                        title = stringResource(Res.string.ui_offices_support_title),
                        subtitle = stringResource(Res.string.ui_offices_count, services.size)
                    )
                }
                itemsIndexed(services, key = { index, item -> "service|${item.email}|${item.name}|$index" }) { _, contact ->
                    ContactItem(contact, backdropState) { onContactClick(contact) }
                }
            }
        } else {
            item(key = "filtered-header") {
                val sectionTitle = when (uiState.selectedCategoryIndex) {
                    1 -> stringResource(Res.string.ui_teachers_title)
                    2 -> stringResource(Res.string.ui_secretariat_title)
                    else -> stringResource(Res.string.ui_offices_support_title)
                }
                LiquidSectionHeader(
                    title = sectionTitle,
                    subtitle = stringResource(Res.string.ui_contacts_available, categoryFiltered.size)
                )
            }
            itemsIndexed(categoryFiltered, key = { index, item -> "filtered|${item.email}|${item.name}|$index" }) { _, contact ->
                ContactItem(contact, backdropState) { onContactClick(contact) }
            }
        }
    }
}
