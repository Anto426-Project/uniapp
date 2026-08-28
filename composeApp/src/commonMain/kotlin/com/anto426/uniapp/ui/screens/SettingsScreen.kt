package com.anto426.uniapp.ui.screens

import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniHeroCard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop

@Composable
fun SettingsScreen(
    backdropState: Backdrop,
    onOpenInfo: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenUpdates: () -> Unit = {},
    onOpenDevices: () -> Unit = {},
    onOpenLanguage: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }

    UniScreenColumn {
        // 1. Account Hero — Modern and prominent
        UniHeroCard(
            backdropState = backdropState,
            eyebrow = stringResource(Res.string.ui_student_card),
            title = stringResource(Res.string.ui_student_name),
            subtitle = "${stringResource(Res.string.ui_matricola_prefix, "123456")} • ${stringResource(Res.string.ui_degree)}",
            leadingContent = {
                LiquidAvatar(
                    initials = "AN",
                    size = 64.dp,
                    backdropState = backdropState
                )
            }
        )

        // 2. Account & Security Section
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_profile_access), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_switch_account),
                icon = LiquidIcons.Refresh,
                onClick = { /* Switch account logic */ },
                backdropState = backdropState
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_connected_devices),
                subtitle = stringResource(Res.string.ui_active_sessions),
                icon = LiquidIcons.Lock,
                onClick = onOpenDevices,
                backdropState = backdropState
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_biometric_security),
                subtitle = stringResource(Res.string.ui_biometric_unlock),
                icon = LiquidIcons.Check,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it },
                        backdropState = backdropState
                    )
                }
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_sign_out),
                icon = LiquidIcons.Close,
                onClick = { /* Logout logic */ },
                backdropState = backdropState
            )
        }

        // 3. App Customization
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_application_preferences), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_colors),
                subtitle = stringResource(Res.string.ui_theme_subtitle),
                icon = LiquidIcons.Star,
                onClick = onOpenTheme,
                backdropState = backdropState
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_language_title),
                subtitle = stringResource(Res.string.ui_language_current),
                icon = LiquidIcons.Info,
                onClick = onOpenLanguage,
                backdropState = backdropState
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_push_notifications),
                subtitle = stringResource(Res.string.ui_academic_alerts),
                icon = LiquidIcons.Notifications,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        backdropState = backdropState
                    )
                }
            )
        }

        // 4. System & Support
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_system), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_updates),
                subtitle = stringResource(Res.string.ui_update_version),
                icon = LiquidIcons.Refresh,
                onClick = onOpenUpdates,
                backdropState = backdropState
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_app_information),
                subtitle = stringResource(Res.string.ui_app_information_subtitle),
                icon = LiquidIcons.Info,
                onClick = onOpenInfo,
                backdropState = backdropState
            )
        }
    }
}
