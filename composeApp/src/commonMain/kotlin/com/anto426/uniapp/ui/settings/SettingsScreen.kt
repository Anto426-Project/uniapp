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
import androidx.compose.runtime.LaunchedEffect
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
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.settings.presentation.SettingsUiState
import com.anto426.uniapp.security.biometric.BiometricAvailability
import com.kyant.backdrop.Backdrop

@Composable
fun SettingsScreen(
    backdropState: Backdrop,
    uiState: SettingsUiState,
    accountUiState: AccountSwitcherUiState = AccountSwitcherUiState(),
    onSelectAccount: (String) -> Unit = {},
    onAddAccount: () -> Unit = {},
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
    var isAccountSheetVisible by remember { mutableStateOf(false) }
    var requestedAccountId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accountUiState.activeAccountId, accountUiState.activatingAccountId) {
        val requested = requestedAccountId ?: return@LaunchedEffect
        when {
            accountUiState.activeAccountId == requested -> {
                requestedAccountId = null
                isAccountSheetVisible = false
            }

            accountUiState.activatingAccountId == null -> requestedAccountId = null
        }
    }

    val activeAccount = accountUiState.accounts.firstOrNull { it.accountId == accountUiState.activeAccountId }
        ?: accountUiState.accounts.firstOrNull()

    UniScreenColumn {
        // 0. Active Account Card
        activeAccount?.let { account ->
            val initials = account.displayName.split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")

            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(26.dp),
                contentPadding = 18.dp,
                interactiveGelatin = true,
                onClick = { isAccountSheetVisible = true },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    UniAccountAvatar(
                        imageData = accountUiState.profileImages[account.accountId],
                        initials = if (initials.isNotBlank()) initials else "UN",
                        size = 50.dp,
                        contentDescription = "Foto profilo",
                        backdropState = backdropState,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = account.displayName.ifBlank { stringResource(Res.string.ui_student_name_fallback) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )

                        Text(
                            text = account.degreeName.ifBlank { stringResource(Res.string.ui_degree_label) },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )

                        account.matricola?.takeIf { it.isNotBlank() }?.let { matricola ->
                            Text(
                                text = stringResource(Res.string.ui_matricola_prefix, matricola),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    LiquidButton(
                        text = stringResource(Res.string.ui_switch_account),
                        onClick = { isAccountSheetVisible = true },
                        variant = LiquidButtonVariant.Tonal,
                        size = LiquidButtonSize.Small,
                        backdropState = backdropState,
                    )
                }
            }
        }

        // 1. Sicurezza e Accesso
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_security), backdropState = backdropState) {
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
                subtitle =
                    when {
                        uiState.isBiometricAuthenticating -> stringResource(Res.string.ui_biometric_authenticating)
                        uiState.biometricAvailability == BiometricAvailability.NotEnrolled ->
                            stringResource(Res.string.ui_biometric_not_enrolled)
                        uiState.biometricAvailability == BiometricAvailability.Unavailable ->
                            stringResource(Res.string.ui_biometric_unavailable)
                        else -> stringResource(Res.string.ui_biometric_unlock)
                    },
                icon = LiquidIcons.Check,
                backdropState = backdropState,
                trailingContent = {
                    LiquidSwitch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = onBiometricEnabledChange,
                        enabled =
                            uiState.biometricAvailability == BiometricAvailability.Available &&
                                !uiState.isBiometricAuthenticating,
                        backdropState = backdropState
                    )
                }
            )
        }

        // 2. Aspetto e Personalizzazione
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_appearance), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_colors),
                subtitle = stringResource(Res.string.ui_theme_subtitle),
                icon = LiquidIcons.Star,
                onClick = onOpenTheme,
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
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_language_title),
                subtitle = stringResource(Res.string.ui_language_current),
                icon = LiquidIcons.Info,
                onClick = onOpenLanguage,
                backdropState = backdropState
            )
        }

        // 3. Sistema e Informazioni
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_system_and_info), backdropState = backdropState) {
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

        // 4. Sessione
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_session_title), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_sign_out),
                icon = LiquidIcons.Close,
                onClick = onRequestSignOut,
                backdropState = backdropState
            )
        }
    }

    if (uiState.isSignOutConfirmationVisible) {
        LiquidDialog(
            onDismissRequest = onDismissSignOut,
            title = stringResource(Res.string.ui_sign_out),
            text = stringResource(Res.string.ui_sign_out_confirm_message),
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_sign_out),
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

    if (isAccountSheetVisible) {
        LiquidSheet(
            onDismissRequest = { isAccountSheetVisible = false },
            title = stringResource(Res.string.ui_accounts_saved_title),
            subtitle = stringResource(Res.string.ui_accounts_saved_sub),
            backdropState = backdropState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (accountUiState.accounts.isEmpty()) {
                    LiquidEmptyState(
                        title = stringResource(Res.string.ui_accounts_empty_title),
                        description = accountUiState.errorMessage ?: stringResource(Res.string.ui_accounts_empty_desc),
                        actionButtonText = stringResource(Res.string.ui_accounts_add),
                        onActionClick = {
                            isAccountSheetVisible = false
                            onAddAccount()
                        },
                        backdropState = backdropState,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        accountUiState.accounts.forEach { account ->
                            val isActive = account.accountId == accountUiState.activeAccountId
                            val isActivating = account.accountId == accountUiState.activatingAccountId
                            val isSwitching = accountUiState.activatingAccountId != null
                            val initials = account.displayName.split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")

                            LiquidCard(
                                backdropState = backdropState,
                                onClick = if (isActive || isSwitching) null else ({
                                    requestedAccountId = account.accountId
                                    onSelectAccount(account.accountId)
                                }),
                                interactiveGelatin = !isActive && !isSwitching,
                                contentPadding = 14.dp,
                                shape = RoundedCornerShape(18.dp),
                                containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else null,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    UniAccountAvatar(
                                        imageData = accountUiState.profileImages[account.accountId],
                                        initials = if (initials.isNotBlank()) initials else "UN",
                                        size = 44.dp,
                                        contentDescription = "Foto profilo",
                                        backdropState = backdropState,
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = account.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                        )
                                        Text(
                                            text = account.degreeName.ifBlank { stringResource(Res.string.ui_degree_label) },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                        account.matricola?.let { matricola ->
                                            if (matricola.isNotBlank()) {
                                                Text(
                                                    text = stringResource(Res.string.ui_matricola_prefix, matricola),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }
                                    }

                                    if (isActive || isActivating) {
                                        LiquidBadge(
                                            text = if (isActivating) stringResource(Res.string.ui_account_activating) else stringResource(Res.string.ui_account_active),
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            backdropState = backdropState,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LiquidButton(
                        text = stringResource(Res.string.ui_accounts_add),
                        onClick = {
                            isAccountSheetVisible = false
                            onAddAccount()
                        },
                        variant = LiquidButtonVariant.Primary,
                        backdropState = backdropState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
