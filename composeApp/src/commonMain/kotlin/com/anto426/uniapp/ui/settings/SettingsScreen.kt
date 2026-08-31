package com.anto426.uniapp.ui.settings

import com.anto426.uniapp.ui.components.layout.UniScreenColumn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.settings.presentation.SettingsUiState
import com.kyant.backdrop.Backdrop

@Composable
fun SettingsScreen(
    backdropState: Backdrop,
    uiState: SettingsUiState,
    onOpenInfo: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenUpdates: () -> Unit = {},
    onOpenDevices: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    onOpenLogin: () -> Unit = {},
    onSignOut: () -> Unit = onOpenLogin,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onBiometricEnabledChange: (Boolean) -> Unit,
    onRequestSignOut: () -> Unit,
    onDismissSignOut: () -> Unit,
) {
    UniScreenColumn {
        // 1. Profilo Utente Semplificato
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 18.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LiquidAvatar(
                    initials = "AN",
                    size = 56.dp,
                    backdropState = backdropState
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.ui_student_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stringResource(Res.string.ui_matricola_prefix, "123456")} • ${stringResource(Res.string.ui_degree)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Account & Security Section
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_profile_access), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_switch_account),
                icon = LiquidIcons.Refresh,
                onClick = onOpenLogin,
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
                        checked = uiState.biometricEnabled,
                        onCheckedChange = onBiometricEnabledChange,
                        backdropState = backdropState
                    )
                }
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_sign_out),
                icon = LiquidIcons.Close,
                onClick = onRequestSignOut,
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
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onNotificationsEnabledChange,
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

    if (uiState.isSignOutConfirmationVisible) {
        LiquidDialog(
            onDismissRequest = onDismissSignOut,
            title = stringResource(Res.string.ui_sign_out),
            text = "Sei sicuro di voler uscire dal tuo account universitario? Le sessioni attive verranno chiuse.",
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = "Esci",
                    onClick = {
                        onDismissSignOut()
                        onSignOut()
                    },
                    variant = LiquidButtonVariant.Primary,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissSignOut,
                    variant = LiquidButtonVariant.Text,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}
