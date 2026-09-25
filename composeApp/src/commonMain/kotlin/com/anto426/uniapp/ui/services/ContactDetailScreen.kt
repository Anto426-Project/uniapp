package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.feedback.runtime.LocalAppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.model.services.contactDialUri
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import io.ktor.http.encodeURLParameter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ContactDetailScreen(contact: ContactData) {
    val uriHandler = LocalUriHandler.current
    val toastSink = LocalAppToastSink.current
    val colorScheme = MaterialTheme.colorScheme
    val openFailed = stringResource(Res.string.ui_contact_open_failed)

    val emailUri = if (contact.email.isNotBlank()) "mailto:${contact.email.encodeURLParameter(spaceToPlus = false)}" else null
    val firstPhoneUri = contact.phoneNumbers.firstNotNullOfOrNull(String::contactDialUri)

    val contactFields = buildList {
        if (contact.email.isNotBlank()) {
            add(
                ContactDetailField(
                    Res.string.ui_email,
                    contact.email,
                    LiquidIcons.Feedback,
                    "mailto:${contact.email.encodeURLParameter(spaceToPlus = false)}",
                ),
            )
        }
        contact.phoneNumbers.forEach { phone ->
            add(
                ContactDetailField(
                    if (phone.count(Char::isDigit) < 7) Res.string.ui_contact_internal_phone else Res.string.ui_phone,
                    phone,
                    LiquidIcons.Phone,
                    phone.contactDialUri(),
                ),
            )
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
        // Hero Card Contatto
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LiquidAvatar(
                        initials = contact.initials,
                        size = 60.dp,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )

                        if (contact.role.isNotBlank()) {
                            Text(
                                text = contact.role,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }

                        if (contact.department.isNotBlank()) {
                            LiquidBadge(
                                text = contact.department,
                                containerColor = colorScheme.primary.copy(alpha = 0.12f),
                                contentColor = colorScheme.primary,
                            )
                        }
                    }
                }

                // Pulsanti Azione Rapida Inline (Scrivi Email / Chiama)
                if (emailUri != null || firstPhoneUri != null) {
                    LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (emailUri != null) {
                            LiquidButton(
                                text = stringResource(Res.string.ui_email),
                                onClick = {
                                    try {
                                        uriHandler.openUri(emailUri)
                                    } catch (_: Exception) {
                                        toastSink.error(openFailed)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                variant = LiquidButtonVariant.Primary,
                                size = LiquidButtonSize.Small,
                            )
                        }

                        if (firstPhoneUri != null) {
                            LiquidButton(
                                text = stringResource(Res.string.ui_phone),
                                onClick = {
                                    try {
                                        uriHandler.openUri(firstPhoneUri)
                                    } catch (_: Exception) {
                                        toastSink.error(openFailed)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                variant = LiquidButtonVariant.Tonal,
                                size = LiquidButtonSize.Small,
                            )
                        }
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
