package com.anto426.uniapp.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.R
import com.anto426.uniapp.android.ui.components.layout.UniHeroCard
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.models.ContactData
import com.kyant.backdrop.Backdrop

@Composable
fun ContactDetailScreen(contact: ContactData, backdropState: Backdrop) {
    UniScreenColumn {
        // 1. Hero Card with Avatar
        UniHeroCard(
            backdropState = backdropState,
            eyebrow = stringResource(R.string.ui_contact_label),
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
        LiquidPreferenceGroup(title = stringResource(R.string.ui_contact_info_group), backdropState = backdropState) {
            if (contact.email.isNotBlank()) {
                LiquidPreferenceItem(
                    title = stringResource(R.string.ui_email),
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
                    title = stringResource(R.string.ui_phone),
                    subtitle = contact.phone,
                    icon = LiquidIcons.Phone,
                    backdropState = backdropState,
                    onClick = { /* Intent to call */ }
                )
            }
        }

        // 3. Additional Info
        LiquidPreferenceGroup(title = stringResource(R.string.ui_department_label), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_office),
                subtitle = stringResource(R.string.ui_office_default),
                icon = LiquidIcons.Home,
                backdropState = backdropState
            )
            LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_office_hours),
                subtitle = stringResource(R.string.ui_office_hours_default),
                icon = LiquidIcons.Time,
                backdropState = backdropState
            )
        }
    }
}
