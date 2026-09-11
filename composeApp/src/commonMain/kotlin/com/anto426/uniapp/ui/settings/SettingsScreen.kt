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
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
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
import com.anto426.uniapp.ui.components.account.UniAccountCard
import com.anto426.uniapp.settings.presentation.SettingsUiState
import com.anto426.uniapp.settings.presentation.AppPasswordSetupError
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.components.inputs.LiquidTextFieldType
import com.anto426.uniapp.security.biometric.BiometricAvailability
import com.anto426.unisdk.platform.AppInfoProvider

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    accountUiState: AccountSwitcherUiState = AccountSwitcherUiState(),
    installedVersion: String = "",
    updateSubtitle: String? = null,
    onSelectAccount: (String) -> Unit = {},
    onRemoveAccount: (String) -> Unit,
    onAddAccount: () -> Unit = {},
    onOpenInfo: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenUpdates: () -> Unit = {},
    onOpenDevices: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    onOpenContribute: () -> Unit = {},
    onOpenReportBug: () -> Unit = {},
    onOpenLogin: () -> Unit = {},
    onSignOut: () -> Unit = onOpenLogin,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onBiometricEnabledChange: (Boolean) -> Unit,
    onSubmitBiometricPassword: (String, String) -> Unit,
    onDismissBiometricPassword: () -> Unit,
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

    val appInfo by AppInfoProvider.info.collectAsState()
    val realVersion = installedVersion.ifBlank { appInfo.versionName.ifBlank { "2.0" } }
    val effectiveUpdateSubtitle = updateSubtitle ?: "Versione $realVersion"

    UniScreenColumn {
        // 0. Active Account Card
        activeAccount?.let { account ->
            val initials = account.displayName.split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")

            LiquidCard(
                shape = RoundedRectangle(26.dp),
                contentPadding = 18.dp,
                onClick = { isAccountSheetVisible = true },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    UniAccountAvatar(
                        imageData = accountUiState.profileImages[account.accountId],
                        initials = if (initials.isNotBlank()) initials else stringResource(Res.string.msg_un),
                        size = 50.dp,
                        contentDescription = stringResource(Res.string.ui_profile_picture),
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
                    )
                }
            }
        }

        // 1. Sicurezza e Accesso
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_security)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_connected_devices),
                subtitle = stringResource(Res.string.ui_active_sessions),
                icon = LiquidIcons.Lock,
                onClick = onOpenDevices,
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
                trailingContent = {
                    LiquidSwitch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = onBiometricEnabledChange,
                        enabled =
                            uiState.biometricAvailability == BiometricAvailability.Available &&
                                !uiState.isBiometricAuthenticating,
                    )
                }
            )
        }

        // 2. Aspetto e Personalizzazione
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_appearance)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_theme_colors),
                subtitle = stringResource(Res.string.ui_theme_subtitle),
                icon = LiquidIcons.Star,
                onClick = onOpenTheme,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_push_notifications),
                subtitle = stringResource(Res.string.ui_academic_alerts),
                icon = LiquidIcons.Notifications,
                trailingContent = {
                    LiquidSwitch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onNotificationsEnabledChange,
                    )
                }
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_language_title),
                subtitle = stringResource(Res.string.ui_language_current),
                icon = LiquidIcons.Info,
                onClick = onOpenLanguage,
            )
        }

        // 3. Sistema e Informazioni
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_system_and_info)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_updates),
                subtitle = effectiveUpdateSubtitle,
                icon = LiquidIcons.Refresh,
                onClick = onOpenUpdates,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_app_information),
                subtitle = stringResource(Res.string.ui_app_information_subtitle),
                icon = LiquidIcons.Info,
                onClick = onOpenInfo,
            )
        }

        // 4. Partecipa al progetto
        LiquidPreferenceGroup(title = "Partecipa al progetto") {
            LiquidPreferenceItem(
                title = stringResource(Res.string.msg_diventa_un_collaboratore),
                subtitle = stringResource(Res.string.msg_invia_una_pull_request_con_correzioni_traduzioni_o),
                icon = LiquidIcons.Star,
                onClick = onOpenContribute,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = "Segnala idee o anomalie",
                subtitle = stringResource(Res.string.msg_aiuta_a_migliorare_l_esperienza_utente_aprendo_una),
                icon = LiquidIcons.Feedback,
                onClick = onOpenReportBug,
            )
        }

        // 5. Sessione
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_session_title)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_sign_out),
                icon = LiquidIcons.Close,
                onClick = onRequestSignOut,
            )
        }
    }

    if (uiState.isPasswordSetupVisible) {
        var password by remember { mutableStateOf("") }
        var confirmation by remember { mutableStateOf("") }
        LiquidDialog(
            title = stringResource(Res.string.ui_app_password_setup_title),
            onDismissRequest = onDismissBiometricPassword,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.ui_app_password_setup_description))
                    LiquidTextField(
                        value = password,
                        onValueChange = { password = it },
                        type = LiquidTextFieldType.Password,
                        label = stringResource(Res.string.ui_app_password_label),
                        enabled = !uiState.isBiometricAuthenticating,
                    )
                    LiquidTextField(
                        value = confirmation,
                        onValueChange = { confirmation = it },
                        type = LiquidTextFieldType.Password,
                        label = stringResource(Res.string.ui_app_password_confirm),
                        enabled = !uiState.isBiometricAuthenticating,
                    )
                    uiState.passwordSetupError?.let { error ->
                        Text(
                            stringResource(when (error) {
                                AppPasswordSetupError.TooShort -> Res.string.ui_app_password_too_short
                                AppPasswordSetupError.TooLong -> Res.string.ui_app_password_too_long
                                AppPasswordSetupError.Mismatch -> Res.string.ui_app_password_mismatch
                            }),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_app_password_enable),
                    onClick = { onSubmitBiometricPassword(password, confirmation) },
                    enabled = !uiState.isBiometricAuthenticating,
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissBiometricPassword,
                    enabled = !uiState.isBiometricAuthenticating,
                    variant = LiquidButtonVariant.Text,
                )
            },
        )
    }

    if (uiState.isSignOutConfirmationVisible) {
        LiquidDialog(
            onDismissRequest = onDismissSignOut,
            title = stringResource(Res.string.ui_sign_out),
            text = stringResource(Res.string.ui_sign_out_confirm_message),
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_sign_out),
                    onClick = {
                        onDismissSignOut()
                        onSignOut()
                    },
                    variant = LiquidButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissSignOut,
                    variant = LiquidButtonVariant.Text,
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
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        accountUiState.accounts.forEach { account ->
                            UniAccountCard(
                                account = account,
                                isActive = account.accountId == accountUiState.activeAccountId,
                                isActivating = account.accountId == accountUiState.activatingAccountId,
                                isSwitching = accountUiState.activatingAccountId != null,
                                profileImage = accountUiState.profileImages[account.accountId],
                                onSelect = {
                                    requestedAccountId = account.accountId
                                    onSelectAccount(account.accountId)
                                },
                                onRemove = {
                                    isAccountSheetVisible = false
                                    onRemoveAccount(account.accountId)
                                },
                                canRemove = accountUiState.activatingAccountId == null && !accountUiState.isRemovingAccount,
                            )
                        }
                    }

                    LiquidButton(
                        text = stringResource(Res.string.ui_accounts_add),
                        onClick = {
                            isAccountSheetVisible = false
                            onAddAccount()
                        },
                        variant = LiquidButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
