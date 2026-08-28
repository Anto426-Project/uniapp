package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.ui.components.items.ContactItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.data.UiInitialData
import com.anto426.uniapp.ui.models.ContactData
import com.kyant.backdrop.Backdrop

@Composable
fun ContactsScreen(
    backdropState: Backdrop,
    searchQuery: String = "",
    onContactClick: (ContactData) -> Unit = {}
) {
    val contacts = UiInitialData.contacts
    val filteredContacts = if (searchQuery.isBlank()) {
        contacts
    } else {
        contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.role.contains(searchQuery, ignoreCase = true)
        }
    }

    UniScreenColumn {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredContacts.forEach { contact ->
                ContactItem(
                    contact = contact,
                    backdropState = backdropState,
                    onClick = { onContactClick(contact) }
                )
            }
        }
    }
}
