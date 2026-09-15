package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.StringResource
import io.ktor.http.encodeURLParameter
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.feedback.runtime.LocalAppToastSink
import com.anto426.uniapp.feedback.runtime.error

@Composable
fun ContactDetailScreen(contact: ContactData) {
    val uriHandler = LocalUriHandler.current
    val toastSink = LocalAppToastSink.current
    val openFailed = stringResource(Res.string.ui_contact_open_failed)
    val contactFields = buildList {
        if (contact.email.isNotBlank()) add(ContactDetailField(
            Res.string.ui_email, contact.email, LiquidIcons.Share,
            "mailto:${contact.email.encodeURLParameter(spaceToPlus = false)}",
        ))
        contact.phoneNumbers.forEach { phone ->
            add(ContactDetailField(
                Res.string.ui_phone, phone, LiquidIcons.Phone,
                "tel:${phone.encodeURLParameter(spaceToPlus = false)}",
            ))
        }
    }
    val locationFields = listOf(
        ContactDetailField(Res.string.ui_contact_organization, contact.department, LiquidIcons.Info),
        ContactDetailField(Res.string.ui_contact_location, contact.city, LiquidIcons.Home),
        ContactDetailField(Res.string.ui_contact_address, contact.address, LiquidIcons.Home),
        ContactDetailField(Res.string.ui_contact_building, contact.building, LiquidIcons.Home),
        ContactDetailField(
            Res.string.ui_office,
            contact.office.takeIf { contact.address.isBlank() && contact.building.isBlank() }.orEmpty(),
            LiquidIcons.Home,
        ),
        ContactDetailField(Res.string.ui_office_hours, contact.officeHours, LiquidIcons.Time),
    ).filter { it.value.isNotBlank() }

    UniScreenColumn {
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 18.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LiquidAvatar(
                    initials = contact.initials,
                    size = 56.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (contact.role.isNotBlank()) {
                        Text(
                            text = contact.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (contactFields.isNotEmpty()) {
            ContactDetailGroup(Res.string.ui_contact_info_group, contactFields) { uri ->
                try {
                    uriHandler.openUri(uri)
                } catch (_: Exception) {
                    toastSink.error(openFailed)
                }
            }
        }
        if (locationFields.isNotEmpty()) {
            ContactDetailGroup(Res.string.ui_contact_location_group, locationFields)
        }
    }
}

private data class ContactDetailField(
    val label: StringResource,
    val value: String,
    val icon: ImageVector,
    val uri: String? = null,
)

@Composable
private fun ContactDetailGroup(
    title: StringResource,
    fields: List<ContactDetailField>,
    onOpenUri: (String) -> Unit = {},
) {
    LiquidPreferenceGroup(title = stringResource(title)) {
        fields.forEachIndexed { index, field ->
            if (index > 0) LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = stringResource(field.label),
                subtitle = field.value,
                icon = field.icon,
                onClick = field.uri?.let { uri -> { onOpenUri(uri) } },
            )
        }
    }
}
