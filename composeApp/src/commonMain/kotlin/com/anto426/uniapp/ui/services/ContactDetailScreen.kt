package com.anto426.uniapp.ui.services

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.feedback.runtime.LocalAppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.model.services.contactDialUri
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import io.ktor.http.encodeURLParameter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
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

    val combinedLocation = listOfNotNull(
        contact.building.trim().takeIf { it.isNotBlank() },
        contact.address.trim().takeIf { it.isNotBlank() },
    ).joinToString(" • ")
    val locationFields = listOf(
        ContactDetailField(Res.string.ui_contact_organization, contact.department, LiquidIcons.Info),
        ContactDetailField(Res.string.ui_contact_location, contact.city, LiquidIcons.Home),
        ContactDetailField(Res.string.ui_contact_address, contact.address, LiquidIcons.Home),
        ContactDetailField(Res.string.ui_contact_building, contact.building, LiquidIcons.Home),
        ContactDetailField(
            Res.string.ui_office,
            contact.office.takeIf { it.isNotBlank() && !it.equals(combinedLocation, ignoreCase = true) }.orEmpty(),
            LiquidIcons.Home,
        ),
        ContactDetailField(Res.string.ui_office_hours, contact.officeHours, LiquidIcons.Time),
    ).filter { it.value.isNotBlank() }

    UniScreenColumn {
        // Hero Card Contatto
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
            border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.20f)),
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
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        if (contact.role.isNotBlank()) {
                            Text(
                                text = contact.role,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        if (contact.department.isNotBlank() && contact.department != contact.role) {
                            Text(
                                text = contact.department,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        if (contact.city.isNotBlank() || contact.building.isNotBlank()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (contact.city.isNotBlank()) {
                                    LiquidBadge(
                                        text = contact.city,
                                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                                        contentColor = colorScheme.primary,
                                    )
                                }
                                if (contact.building.isNotBlank()) {
                                    LiquidBadge(
                                        text = contact.building,
                                        containerColor = colorScheme.secondary.copy(alpha = 0.12f),
                                        contentColor = colorScheme.secondary,
                                    )
                                }
                            }
                        }
                    }
                }

                if (emailUri != null || firstPhoneUri != null) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

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
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                variant = LiquidButtonVariant.Primary,
                                size = LiquidButtonSize.Medium,
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
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                variant = LiquidButtonVariant.Tonal,
                                size = LiquidButtonSize.Medium,
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
    UniInsetGroup(title = stringResource(title)) {
        fields.forEachIndexed { index, field ->
            UniInsetRow(
                title = stringResource(field.label),
                subtitle = field.value,
                icon = field.icon,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = field.uri?.let { uri -> { onOpenUri(uri) } },
                showDivider = index < fields.lastIndex,
            )
        }
    }
}
