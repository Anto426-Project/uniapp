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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.model.services.ContactData

@Composable
fun ContactDetailScreen(contact: ContactData) {
    UniScreenColumn {
        // 1. Contact Header Card
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
                    Text(
                        text = contact.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Contact Actions
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_contact_info_group)) {
            if (contact.email.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_email),
                    subtitle = contact.email,
                    icon = LiquidIcons.Share,
                    onClick = { /* Intent to email */ }
                )
            }

            if (contact.email.isNotBlank() && contact.phone.isNotBlank()) {
                LiquidHorizontalDivider()
            }

            if (contact.phone.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_phone),
                    subtitle = contact.phone,
                    icon = LiquidIcons.Phone,
                    onClick = { /* Intent to call */ }
                )
            }
        }

        // 3. Additional Info
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_department_label)) {
            if (contact.department.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_department_label),
                    subtitle = contact.department,
                    icon = LiquidIcons.Info,
                )
                LiquidHorizontalDivider()
            }
            if (contact.city.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_contact_location),
                    subtitle = contact.city,
                    icon = LiquidIcons.Home,
                )
                if (contact.office.isNotBlank() || contact.officeHours.isNotBlank()) {
                    LiquidHorizontalDivider()
                }
            }
            if (contact.office.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_office),
                    subtitle = contact.office,
                    icon = LiquidIcons.Home,
                )
                if (contact.officeHours.isNotBlank()) {
                    LiquidHorizontalDivider()
                }
            }
            if (contact.officeHours.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_office_hours),
                    subtitle = contact.officeHours,
                    icon = LiquidIcons.Time,
                )
            }
        }
    }
}
