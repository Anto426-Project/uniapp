package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.feedback.sheet.LiquidSheet
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.account.UniAccountCard
import com.anto426.uniapp.ui.components.account.accountDisplayIdentity
import com.anto426.uniapp.ui.components.account.formatSmartDisplayName
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.settings.presentation.SettingsUiState
import com.anto426.uniapp.settings.presentation.AppPasswordSetupError
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextFieldType
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
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
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
    val hasUpdateAvailable = !updateSubtitle.isNullOrBlank()
    val effectiveUpdateSubtitle = "Versione $realVersion"

    UniScreenColumn {
        // 0. Active Account Hero Card (pieno spazio al nome completo + cambio account rapido)
        activeAccount?.let { account ->
            val initials = account.displayName.split(' ').filter(String::isNotBlank).take(2).map { it.first() }.joinToString("")
            val identity = accountDisplayIdentity(
                account.displayName,
                initials,
                accountUiState.profileImages[account.accountId],
            )
            val displayName = remember(identity.name) {
                formatSmartDisplayName(identity.name).ifBlank { identity.name }
            }
            val degreeLabel = account.degreeName.ifBlank {
                if (account.isProfessor) {
                    stringResource(Res.string.ui_professor_role)
                } else {
                    stringResource(Res.string.ui_degree_label)
                }
            }
            val matricolaLabel = account.matricola?.takeIf { it.isNotBlank() }?.let {
                stringResource(Res.string.ui_matricola_prefix, it)
            }
            val accountMeta = listOfNotNull(degreeLabel, matricolaLabel).joinToString(" • ")

            LiquidCard(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {}
                    .graphicsLayer(clip = false),
                shape = RoundedRectangle(24.dp),
                onClick = { isAccountSheetVisible = true },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    UniAccountAvatar(
                        imageData = identity.photo,
                        initials = if (identity.initials.isNotBlank()) identity.initials else stringResource(Res.string.msg_un),
                        size = 48.dp,
                        contentDescription = stringResource(Res.string.ui_profile_picture),
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = colorScheme.primary.copy(alpha = 0.40f),
                                shape = CircleShape,
                            )
                            .padding(2.dp),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = displayName.ifBlank { stringResource(Res.string.ui_student_name_fallback) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = accountMeta,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (accountUiState.accounts.size > 1) {
                            LiquidBadge(
                                text = "${accountUiState.accounts.size}",
                                containerColor = colorScheme.primaryContainer.copy(alpha = 0.55f),
                                contentColor = colorScheme.primary,
                            )
                        }
                        Icon(
                            imageVector = LiquidIcons.SwitchAccount,
                            contentDescription = stringResource(Res.string.ui_switch_account),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Icon(
                            imageVector = LiquidIcons.ChevronRight,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // 1. Sicurezza e accesso
        UniInsetGroup(title = stringResource(Res.string.ui_security)) {
            UniInsetRow(
                title = stringResource(Res.string.ui_connected_devices),
                subtitle = stringResource(Res.string.ui_active_sessions),
                icon = LiquidIcons.Lock,
                iconTint = colorScheme.primary,
                onClick = onOpenDevices,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_biometric_security),
                subtitle = when {
                    uiState.isBiometricAuthenticating -> stringResource(Res.string.ui_biometric_authenticating)
                    uiState.biometricAvailability == BiometricAvailability.NotEnrolled ->
                        stringResource(Res.string.ui_biometric_not_enrolled)
                    uiState.biometricAvailability == BiometricAvailability.Unavailable ->
                        stringResource(Res.string.ui_biometric_unavailable)
                    else -> stringResource(Res.string.ui_biometric_unlock)
                },
                icon = LiquidIcons.Check,
                iconTint = colorScheme.tertiary,
                showDivider = false,
                trailingContent = {
                    key(uiState.biometricEnabled, uiState.isPasswordSetupVisible, uiState.isBiometricAuthenticating) {
                        LiquidSwitch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = onBiometricEnabledChange,
                            enabled =
                                uiState.biometricAvailability == BiometricAvailability.Available &&
                                    !uiState.isBiometricAuthenticating,
                        )
                    }
                },
            )
        }

        // 2. Aspetto e personalizzazione
        UniInsetGroup(title = stringResource(Res.string.ui_appearance)) {
            UniInsetRow(
                title = stringResource(Res.string.ui_theme_colors),
                subtitle = stringResource(Res.string.ui_theme_subtitle),
                icon = LiquidIcons.Palette,
                iconTint = colorScheme.secondary,
                onClick = onOpenTheme,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_push_notifications),
                subtitle = stringResource(Res.string.ui_academic_alerts),
                icon = LiquidIcons.Notifications,
                iconTint = glassColors.warning,
                showDivider = true,
                trailingContent = {
                    LiquidSwitch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onNotificationsEnabledChange,
                    )
                },
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_language_title),
                subtitle = stringResource(Res.string.ui_language_current),
                icon = LiquidIcons.Info,
                iconTint = colorScheme.primary,
                onClick = onOpenLanguage,
                showDivider = false,
            )
        }

        // 3. Sistema e informazioni
        UniInsetGroup(title = stringResource(Res.string.ui_system_and_info)) {
            UniInsetRow(
                title = stringResource(Res.string.ui_updates),
                subtitle = effectiveUpdateSubtitle,
                icon = LiquidIcons.Refresh,
                iconTint = if (hasUpdateAvailable) glassColors.warning else colorScheme.tertiary,
                onClick = onOpenUpdates,
                showDivider = true,
                trailingContent = if (hasUpdateAvailable) {
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LiquidBadge(
                                text = "Nuovo",
                                containerColor = glassColors.warning.copy(alpha = 0.18f),
                                contentColor = glassColors.warning,
                            )
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                } else null,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_app_information),
                subtitle = stringResource(Res.string.ui_app_information_subtitle),
                icon = LiquidIcons.Info,
                iconTint = colorScheme.primary,
                onClick = onOpenInfo,
                showDivider = false,
            )
        }

        // 4. Partecipa al progetto
        UniInsetGroup(title = "Partecipa al progetto") {
            UniInsetRow(
                title = stringResource(Res.string.msg_diventa_un_collaboratore),
                subtitle = stringResource(Res.string.msg_invia_una_pull_request_con_correzioni_traduzioni_o),
                icon = LiquidIcons.Star,
                iconTint = glassColors.warning,
                onClick = onOpenContribute,
                showDivider = true,
            )
            UniInsetRow(
                title = "Segnala idee o anomalie",
                subtitle = stringResource(Res.string.msg_aiuta_a_migliorare_l_esperienza_utente_aprendo_una),
                icon = LiquidIcons.Feedback,
                iconTint = colorScheme.tertiary,
                onClick = onOpenReportBug,
                showDivider = false,
            )
        }

        // 5. Sessione
        UniInsetGroup {
            UniInsetRow(
                title = stringResource(Res.string.ui_sign_out),
                icon = LiquidIcons.Close,
                iconTint = colorScheme.error,
                onClick = onRequestSignOut,
                showDivider = false,
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
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissBiometricPassword,
                    enabled = !uiState.isBiometricAuthenticating,
                    variant = LiquidButtonVariant.Text,
                    modifier = Modifier.fillMaxWidth(),
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
