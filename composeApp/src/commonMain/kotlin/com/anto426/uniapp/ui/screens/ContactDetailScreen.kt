package com.anto426.uniapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniHeroCard
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.models.ContactData
import com.kyant.backdrop.Backdrop

@Composable
fun ContactDetailScreen(contact: ContactData, backdropState: Backdrop) {
    UniScreenColumn {
        // 1. Hero Card with Avatar
        UniHeroCard(
            backdropState = backdropState,
            eyebrow = stringResource(Res.string.ui_contact_label),
            title = contact.name,
            subtitle = contact.role,
            leadingContent = {
                LiquidAvatar(
                    initials = contact.initials,
                    size = 64.dp,
                    backdropState = backdropState
                )
            }
        )

        // 2. Contact Actions
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_contact_info_group), backdropState = backdropState) {
            if (contact.email.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_email),
                    subtitle = contact.email,
                    icon = LiquidIcons.Share,
                    backdropState = backdropState,
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
                    backdropState = backdropState,
                    onClick = { /* Intent to call */ }
                )
            }
        }

        // 3. Additional Info
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_department_label), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_office),
                subtitle = stringResource(Res.string.ui_office_default),
                icon = LiquidIcons.Home,
                backdropState = backdropState
            )
            LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_office_hours),
                subtitle = stringResource(Res.string.ui_office_hours_default),
                icon = LiquidIcons.Time,
                backdropState = backdropState
            )
        }
    }
}
