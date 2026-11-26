package com.anto426.uniapp.ui.components.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow

@Composable
fun ContactItem(
    contact: ContactData,
    onClick: () -> Unit = {},
) {
    val secondaryText = listOfNotNull(
        contact.role.trim().takeIf { it.isNotBlank() }
            ?: contact.department.trim().takeIf { it.isNotBlank() },
        contact.phoneNumbers.firstOrNull()?.trim()?.takeIf { it.isNotBlank() }
            ?: contact.email.trim().takeIf { it.isNotBlank() }
            ?: contact.city.trim().takeIf { it.isNotBlank() },
    ).distinct().joinToString(" • ").ifBlank { "—" }

    UniInsetGroup(animateEntrance = false) {
        UniInsetRow(
            title = contact.name,
            subtitle = secondaryText,
            onClick = onClick,
            showDivider = false,
            leadingContent = {
                LiquidAvatar(
                    initials = contact.initials,
                    size = 40.dp,
                )
            },
        )
    }
}
