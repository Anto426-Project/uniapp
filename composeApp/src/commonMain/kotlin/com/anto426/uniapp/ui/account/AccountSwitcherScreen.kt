package com.anto426.uniapp.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.state.AppLoadingState
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AccountSwitcherScreen(
    backdropState: Backdrop,
    uiState: AccountSwitcherUiState,
    onSelectAccount: (String) -> Unit,
    onSelectProfile: (String) -> Unit,
    onAddAccount: () -> Unit,
) {
    if (uiState.isLoading && uiState.accounts.isEmpty()) {
        AppLoadingState(backdropState = backdropState)
        return
    }

    UniScreenColumn {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_accounts_saved_title),
            subtitle = stringResource(Res.string.ui_accounts_saved_sub),
        )

        when {
            uiState.accounts.isEmpty() ->
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_accounts_empty_title),
                    description = uiState.errorMessage ?: stringResource(Res.string.ui_accounts_empty_desc),
                    actionButtonText = stringResource(Res.string.ui_accounts_add),
                    onActionClick = onAddAccount,
                    backdropState = backdropState,
                )

            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.accounts.forEach { account ->
                val isActive = account.accountId == uiState.activeAccountId
                val isActivating = account.accountId == uiState.activatingAccountId
                val isSwitching = uiState.activatingAccountId != null || uiState.activatingProfileId != null
                LiquidCard(
                    backdropState = backdropState,
                    onClick = if (isActive || isSwitching) null else ({ onSelectAccount(account.accountId) }),
                    interactiveGelatin = !isActive && !isSwitching,
                    contentPadding = 16.dp,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                        val initials =
                            account.displayName
                                .split(' ')
                                .filter(String::isNotBlank)
                                .take(2)
                                .map { it.first() }
                                .joinToString("")
                        UniAccountAvatar(
                            imageData = uiState.profileImages[account.accountId],
                            initials = initials,
                            size = 46.dp,
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
                            account.matricola?.let { matricola ->
                                Text(
                                    text = stringResource(Res.string.ui_matricola_prefix, matricola),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                            if (isActive || isActivating) {
                                LiquidBadge(
                                    text = if (isActivating) stringResource(Res.string.ui_account_activating) else stringResource(Res.string.ui_account_active),
                                    backdropState = backdropState,
                                )
                            }
                        }
                        if (isActive && account.profiles.size > 1) {
                            Text(
                                text = stringResource(Res.string.ui_account_profiles_title),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            account.profiles.forEach { profile ->
                                val profileIsActive = profile.profileId == account.activeProfileId
                                val profileIsActivating = profile.profileId == uiState.activatingProfileId
                                val role =
                                    if (profile.type == com.anto426.unisdk.backend.model.BackendCareerType.PROFESSOR) {
                                        "Docente"
                                    } else {
                                        "Studente"
                                    }
                                LiquidButton(
                                    text = "$role · ${profile.degreeName.ifBlank { profile.displayName }}",
                                    onClick = { onSelectProfile(profile.profileId) },
                                    enabled = !profileIsActive && uiState.activatingProfileId == null,
                                    isLoading = profileIsActivating,
                                    variant = if (profileIsActive) LiquidButtonVariant.Primary else LiquidButtonVariant.Secondary,
                                    backdropState = backdropState,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                }
            }
        }

        if (uiState.accounts.isNotEmpty()) {
            LiquidButton(
                text = stringResource(Res.string.ui_accounts_add),
                onClick = onAddAccount,
                variant = LiquidButtonVariant.Primary,
                backdropState = backdropState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
