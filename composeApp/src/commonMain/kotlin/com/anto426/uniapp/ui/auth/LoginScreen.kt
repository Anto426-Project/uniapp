package com.anto426.uniapp.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidAvatarPresence
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.components.inputs.LiquidTextFieldType
import com.anto426.liquidmonet.components.selection.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.auth.presentation.LoginUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.backend.model.LoginCareerOption
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Access & Authentication Screen built 100% with official Liquid Monet SDK components.
 */
@Composable
fun LoginScreen(
    backdropState: Backdrop,
    uiState: LoginUiState,
    accountUiState: AccountSwitcherUiState = AccountSwitcherUiState(),
    onSelectAccount: (String) -> Unit = {},
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberCredentialsChange: (Boolean) -> Unit = {},
    onSubmit: () -> Unit,
    onCareerSelected: (LoginCareerOption) -> Unit,
    onCancelCareerSelection: () -> Unit,
    onShowForgotPassword: () -> Unit,
    onDismissForgotPassword: () -> Unit,
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    var isAccountSheetVisible by remember { mutableStateOf(false) }

    UniScreenColumn {
        Spacer(Modifier.height(12.dp))

        // 1. Brand Presentation Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LiquidAvatar(
                size = 80.dp,
                icon = LiquidIcons.AccountCircle,
                presence = LiquidAvatarPresence.Online,
                backdropState = backdropState,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        fontSize = 32.sp,
                    ),
                    color = colorScheme.onSurface,
                )

                Text(
                    text = stringResource(Res.string.ui_login_portal_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colorScheme.primary,
                )

                Text(
                    text = stringResource(Res.string.ui_login_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // 2. Saved Accounts Switcher Shortcut Card (if any saved accounts exist)
        if (accountUiState.accounts.isNotEmpty()) {
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(22.dp),
                contentPadding = 14.dp,
                onClick = { isAccountSheetVisible = true },
                interactiveGelatin = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Refresh,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                        Column {
                            Text(
                                text = stringResource(Res.string.ui_login_quick_switch),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(Res.string.ui_accounts_saved_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    LiquidBadge(
                        text = "${accountUiState.accounts.size}",
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }

        // 3. Master Glass Authentication Form Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(30.dp),
            contentPadding = 24.dp,
            interactiveGelatin = false,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Header Form Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_login_credentials_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colorScheme.onSurface,
                    )
                    LiquidBadge(
                        text = stringResource(Res.string.ui_login_title),
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.50f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }

                // Username / Matricola Field
                LiquidTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    type = LiquidTextFieldType.Text,
                    label = stringResource(Res.string.ui_login_username_label),
                    placeholder = stringResource(Res.string.ui_login_username_placeholder),
                    leadingIcon = LiquidIcons.AccountCircle,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                    backdropState = backdropState,
                )

                // Password Field
                LiquidTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    type = LiquidTextFieldType.Password,
                    label = stringResource(Res.string.ui_login_password_label),
                    placeholder = stringResource(Res.string.ui_login_password_placeholder),
                    leadingIcon = LiquidIcons.Lock,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit() },
                    ),
                    backdropState = backdropState,
                )

                // Remember Credentials Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = stringResource(Res.string.ui_login_remember_credentials),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(Res.string.ui_remember_credentials_desc),
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }

                    LiquidSwitch(
                        checked = uiState.rememberCredentials,
                        onCheckedChange = onRememberCredentialsChange,
                        backdropState = backdropState,
                    )
                }

                Spacer(Modifier.height(2.dp))

                // Primary Action Button
                LiquidButton(
                    text = stringResource(Res.string.ui_login_button),
                    onClick = onSubmit,
                    isLoading = uiState.isLoading,
                    variant = LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Large,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(LiquidIcons.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )

                // Secondary Forgot Password Link
                LiquidButton(
                    text = stringResource(Res.string.ui_login_forgot_password),
                    onClick = onShowForgotPassword,
                    variant = LiquidButtonVariant.Text,
                    size = LiquidButtonSize.Small,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 4. Legal Links Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidButton(
                text = stringResource(Res.string.ui_privacy),
                onClick = onOpenPrivacy,
                variant = LiquidButtonVariant.Text,
                size = LiquidButtonSize.Small,
                backdropState = backdropState,
            )
            Text(
                text = "•",
                color = colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            LiquidButton(
                text = stringResource(Res.string.ui_terms),
                onClick = onOpenTerms,
                variant = LiquidButtonVariant.Text,
                size = LiquidButtonSize.Small,
                backdropState = backdropState,
            )
        }
    }

    // Saved Accounts Bottom Sheet
    if (isAccountSheetVisible) {
        LiquidSheet(
            onDismissRequest = { isAccountSheetVisible = false },
            backdropState = backdropState,
            title = stringResource(Res.string.ui_accounts_saved_title),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                accountUiState.accounts.forEach { account ->
                    val isActivating = account.accountId == accountUiState.activatingAccountId
                    val initials =
                        account.displayName
                            .split(' ')
                            .filter(String::isNotBlank)
                            .take(2)
                            .map { it.first() }
                            .joinToString("")

                    LiquidCard(
                        backdropState = backdropState,
                        onClick = {
                            isAccountSheetVisible = false
                            onSelectAccount(account.accountId)
                        },
                        interactiveGelatin = true,
                        contentPadding = 14.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
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
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = account.degreeName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                account.matricola?.takeIf { it.isNotBlank() }?.let { matricola ->
                                    Text(
                                        text = stringResource(Res.string.ui_matricola_prefix, matricola),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            if (isActivating) {
                                LiquidBadge(
                                    text = stringResource(Res.string.ui_account_activating),
                                    backdropState = backdropState,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Forgot Password Dialog
    if (uiState.isForgotPasswordDialogVisible) {
        LiquidDialog(
            onDismissRequest = onDismissForgotPassword,
            title = stringResource(Res.string.ui_forgot_password_title),
            text = stringResource(Res.string.ui_forgot_password_desc),
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_understand),
                    onClick = onDismissForgotPassword,
                    variant = LiquidButtonVariant.Primary,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    // Career Selection Dialog (for multi-career users)
    if (uiState.careers.isNotEmpty()) {
        LiquidDialog(
            onDismissRequest = onCancelCareerSelection,
            title = stringResource(Res.string.ui_login_career_selection_title),
            text = stringResource(Res.string.ui_login_career_selection_subtitle),
            backdropState = backdropState,
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    uiState.careers.forEach { career ->
                        LiquidCard(
                            backdropState = backdropState,
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = 16.dp,
                            onClick = {
                                onCareerSelected(career)
                            },
                            interactiveGelatin = true,
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = career.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = colorScheme.primary,
                                    )
                                    career.matricola?.let { matr ->
                                        LiquidBadge(
                                            text = stringResource(Res.string.ui_matricola_prefix, matr),
                                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.50f),
                                            contentColor = colorScheme.primary,
                                            backdropState = backdropState,
                                        )
                                    }
                                }
                                Text(
                                    text = career.degreeName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurface,
                                )
                                if (!career.departmentName.isNullOrBlank()) {
                                    Text(
                                        text = career.departmentName.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    LiquidButton(
                        text = stringResource(Res.string.ui_cancel),
                        onClick = onCancelCareerSelection,
                        variant = LiquidButtonVariant.Text,
                        backdropState = backdropState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }
}
