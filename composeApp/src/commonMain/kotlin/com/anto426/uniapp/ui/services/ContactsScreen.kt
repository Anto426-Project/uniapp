package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.services.presentation.ContactsUiState
import com.anto426.uniapp.ui.components.items.ContactItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun ContactsScreen(
    backdropState: Backdrop,
    uiState: ContactsUiState,
    onCategorySelected: (Int) -> Unit,
    onContactClick: (ContactData) -> Unit = {},
) {
    val tabs = listOf(
        LiquidNavigationItem(label = "Tutti"),
        LiquidNavigationItem(label = "Docenti"),
        LiquidNavigationItem(label = "Segreterie"),
        LiquidNavigationItem(label = "Uffici")
    )

    val categoryFiltered = uiState.visibleContacts
    val teachers = uiState.teachers
    val secretariat = uiState.secretariat
    val services = uiState.services

    UniScreenColumn {
        // 1. Category Tabs
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedCategoryIndex,
            onTabSelected = onCategorySelected,
            backdropState = backdropState,
        )

        // 2. Contact Sections
        if (categoryFiltered.isEmpty()) {
            LiquidCard(
                backdropState = backdropState,
                contentPadding = 20.dp,
                interactiveGelatin = false
            ) {
                Text(
                    text = "Nessun contatto disponibile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (uiState.selectedCategoryIndex == 0) {
            // All categories grouped
            if (teachers.isNotEmpty()) {
                LiquidSectionHeader(
                    title = "Docenti e Ricercatori",
                    subtitle = "${teachers.size} contatti",
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    teachers.forEach { contact ->
                        ContactItem(
                            contact = contact,
                            backdropState = backdropState,
                            onClick = { onContactClick(contact) }
                        )
                    }
                }
            }

            if (secretariat.isNotEmpty()) {
                LiquidSectionHeader(
                    title = "Segreterie Studenti",
                    subtitle = "${secretariat.size} sportelli",
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    secretariat.forEach { contact ->
                        ContactItem(
                            contact = contact,
                            backdropState = backdropState,
                            onClick = { onContactClick(contact) }
                        )
                    }
                }
            }

            if (services.isNotEmpty()) {
                LiquidSectionHeader(
                    title = "Uffici e Supporto",
                    subtitle = "${services.size} uffici",
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    services.forEach { contact ->
                        ContactItem(
                            contact = contact,
                            backdropState = backdropState,
                            onClick = { onContactClick(contact) }
                        )
                    }
                }
            }
        } else {
            // Specific category selected
            val sectionTitle = when (uiState.selectedCategoryIndex) {
                1 -> "Docenti e Ricercatori"
                2 -> "Segreterie Studenti"
                else -> "Uffici e Supporto"
            }

            LiquidSectionHeader(
                title = sectionTitle,
                subtitle = "${categoryFiltered.size} contatti disponibili",
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categoryFiltered.forEach { contact ->
                    ContactItem(
                        contact = contact,
                        backdropState = backdropState,
                        onClick = { onContactClick(contact) }
                    )
                }
            }
        }
    }
}
