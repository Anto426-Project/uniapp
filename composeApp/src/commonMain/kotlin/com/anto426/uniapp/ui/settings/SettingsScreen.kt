package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.security.biometric.BiometricAvailability
import com.anto426.uniapp.settings.presentation.SettingsUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.account.UniSavedAccountsSheet
import com.anto426.uniapp.ui.components.account.accountDisplayIdentity
import com.anto426.uniapp.ui.components.account.formatSmartDisplayName
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    accountUiState: AccountSwitcherUiState = AccountSwitcherUiState(),
    installedVersion: String = "",
    hasUpdateAvailable: Boolean = false,
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

    val realVersion = installedVersion.ifBlank { "2.0" }
    val effectiveUpdateSubtitle = stringResource(Res.string.ui_version_number, realVersion)

    UniScreenColumn {
        // 0. Active Account Block (shared UniInsetGroup + UniInsetRow)
        activeAccount?.let { account ->
            val initials = remember(account.displayName) {
                account.displayName.split(' ')
                    .filter(String::isNotBlank)
                    .take(2)
                    .map { it.first() }
                    .joinToString("")
            }
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
            } ?: account.email?.takeIf { it.isNotBlank() && account.isProfessor }
            val accountMeta = listOfNotNull(
                degreeLabel.takeIf { it.isNotBlank() },
                matricolaLabel?.takeIf { it.isNotBlank() },
            ).joinToString(" • ")

            UniInsetGroup(entranceOrder = 0) {
                UniInsetRow(
                    title = displayName.ifBlank { stringResource(Res.string.ui_student_name_fallback) },
                    subtitle = accountMeta.ifBlank { null },
                    onClick = { isAccountSheetVisible = true },
                    showDivider = false,
                    leadingContent = {
                        UniAccountAvatar(
                            imageData = identity.photo,
                            initials = if (identity.initials.isNotBlank()) {
                                identity.initials
                            } else {
                                stringResource(Res.string.msg_un)
                            },
                            size = 40.dp,
                            contentDescription = stringResource(Res.string.ui_profile_picture),
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    color = colorScheme.primary.copy(alpha = 0.42f),
                                    shape = CircleShape,
                                )
                                .padding(2.dp),
                        )
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(Capsule())
                                    .background(colorScheme.primary.copy(alpha = 0.12f), Capsule())
                                    .border(1.dp, colorScheme.primary.copy(alpha = 0.20f), Capsule())
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(
                                    imageVector = LiquidIcons.SwitchAccount,
                                    contentDescription = stringResource(Res.string.ui_switch_account),
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                if (accountUiState.accounts.size > 1) {
                                    Text(
                                        text = "${accountUiState.accounts.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary,
                                    )
                                }
                            }
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    },
                )
            }
        }

        // 1. Sicurezza e accesso
        UniInsetGroup(
            title = stringResource(Res.string.ui_security),
            entranceOrder = 1,
        ) {
            UniInsetRow(
                title = stringResource(Res.string.ui_connected_devices),
                subtitle = stringResource(Res.string.ui_active_sessions),
                icon = LiquidIcons.Smartphone,
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
                icon = LiquidIcons.Lock,
                iconTint = glassColors.success,
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
        UniInsetGroup(
            title = stringResource(Res.string.ui_appearance),
            entranceOrder = 2,
        ) {
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
                icon = LiquidIcons.Globe,
                iconTint = colorScheme.primary,
                onClick = onOpenLanguage,
                showDivider = false,
            )
        }

        // 3. Sistema e informazioni
        UniInsetGroup(
            title = stringResource(Res.string.ui_system_and_info),
            entranceOrder = 3,
        ) {
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
                                text = stringResource(Res.string.ui_new_badge),
                                containerColor = glassColors.warning.copy(alpha = 0.16f),
                                contentColor = glassColors.warning,
                            )
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                } else {
                    null
                },
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
        UniInsetGroup(
            title = stringResource(Res.string.ui_project_contribute_group),
            entranceOrder = 4,
        ) {
            UniInsetRow(
                title = stringResource(Res.string.msg_diventa_un_collaboratore),
                subtitle = stringResource(Res.string.msg_invia_una_pull_request_con_correzioni_traduzioni_o),
                icon = LiquidIcons.Star,
                iconTint = glassColors.warning,
                onClick = onOpenContribute,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_project_report_ideas),
                subtitle = stringResource(Res.string.msg_aiuta_a_migliorare_l_esperienza_utente_aprendo_una),
                icon = LiquidIcons.Feedback,
                iconTint = colorScheme.tertiary,
                onClick = onOpenReportBug,
                showDivider = false,
            )
        }

        // 5. Sessione (azione distruttiva con gerarchia semantica chiara)
        UniInsetGroup(
            title = stringResource(Res.string.ui_session_title),
            entranceOrder = 5,
        ) {
            UniInsetRow(
                title = stringResource(Res.string.ui_sign_out),
                icon = LiquidIcons.Close,
                iconTint = glassColors.error,
                titleColor = glassColors.error,
                onClick = onRequestSignOut,
                showDivider = false,
                trailingContent = {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = glassColors.error.copy(alpha = 0.65f),
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }

    if (uiState.isSignOutConfirmationVisible) {
        val signOutLabel = stringResource(Res.string.ui_sign_out)
        val cancelLabel = stringResource(Res.string.ui_cancel)
        LiquidDialog(
            onDismissRequest = onDismissSignOut,
            title = signOutLabel,
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LiquidButton(
                        onClick = {
                            onDismissSignOut()
                            onSignOut()
                        },
                        containerColor = colorScheme.error,
                        contentColor = colorScheme.onError,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = signOutLabel,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                    LiquidButton(
                        onClick = onDismissSignOut,
                        variant = LiquidButtonVariant.Text,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = cancelLabel,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val sessionAccount = activeAccount?.takeIf { it.accountId == accountUiState.activeAccountId }
                    if (sessionAccount != null) {
                        val initials = remember(sessionAccount.displayName) {
                            sessionAccount.displayName.split(' ')
                                .filter(String::isNotBlank)
                                .take(2)
                                .joinToString("") { it.first().toString() }
                        }
                        val formattedName = remember(sessionAccount.displayName) {
                            formatSmartDisplayName(sessionAccount.displayName).ifBlank { sessionAccount.displayName }
                        }
                        UniAccountAvatar(
                            imageData = accountUiState.profileImages[sessionAccount.accountId],
                            initials = initials,
                            size = 52.dp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    color = colorScheme.error.copy(alpha = 0.35f),
                                    shape = CircleShape,
                                )
                                .padding(2.dp),
                        )
                        Text(
                            text = formattedName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Icon(
                            imageVector = LiquidIcons.AccountCircle,
                            contentDescription = null,
                            tint = colorScheme.error,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    Text(
                        text = stringResource(Res.string.ui_sign_out_confirm_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            },
        )
    }

    if (isAccountSheetVisible) {
        UniSavedAccountsSheet(
            state = accountUiState,
            onDismiss = { isAccountSheetVisible = false },
            onSelectAccount = { accountId ->
                requestedAccountId = accountId
                onSelectAccount(accountId)
            },
            onRemoveAccount = { accountId ->
                isAccountSheetVisible = false
                onRemoveAccount(accountId)
            },
            onAddAccount = {
                isAccountSheetVisible = false
                onAddAccount()
            },
        )
    }
}
