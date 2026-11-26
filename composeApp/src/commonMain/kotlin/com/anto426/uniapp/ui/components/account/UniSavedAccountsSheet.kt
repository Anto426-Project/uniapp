package com.anto426.uniapp.ui.components.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.sheet.LiquidSheet
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.ui.components.display.UniBlockDefaults
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun UniSavedAccountsSheet(
    state: AccountSwitcherUiState,
    onDismiss: () -> Unit,
    onSelectAccount: (String) -> Unit,
    onRemoveAccount: (String) -> Unit,
    onAddAccount: (() -> Unit)? = null,
    activeAccountId: String? = state.activeAccountId,
) {
    val isBusy = state.activatingAccountId != null ||
        state.activatingProfileId != null ||
        state.pendingRemovalAccountId != null ||
        state.isRemovingAccount
    val addAccountLabel = stringResource(Res.string.ui_accounts_add)

    LiquidSheet(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.ui_accounts_saved_title),
        subtitle = stringResource(Res.string.ui_accounts_saved_sub),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.accounts.isEmpty()) {
                    LiquidEmptyState(
                        title = stringResource(Res.string.ui_accounts_empty_title),
                        description = state.errorMessage ?: stringResource(Res.string.ui_accounts_empty_desc),
                        actionButtonText = if (onAddAccount != null) addAccountLabel else null,
                        onActionClick = onAddAccount,
                    )
                } else {
                    UniInsetGroup(animateEntrance = false) {
                        state.accounts.forEachIndexed { index, account ->
                            key(account.accountId) {
                                SavedAccountRow(
                                    account = account,
                                    isActive = account.accountId == activeAccountId,
                                    isActivating = account.accountId == state.activatingAccountId,
                                    profileImage = state.profileImages[account.accountId],
                                    enabled = !isBusy,
                                    showDivider = index != state.accounts.lastIndex,
                                    onSelect = { onSelectAccount(account.accountId) },
                                    onRemove = { onRemoveAccount(account.accountId) },
                                )
                            }
                        }
                    }

                    if (onAddAccount != null) {
                        LiquidButton(
                            text = addAccountLabel,
                            onClick = onAddAccount,
                            enabled = !isBusy,
                            variant = LiquidButtonVariant.Outlined,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedAccountRow(
    account: UniAccountSummary,
    isActive: Boolean,
    isActivating: Boolean,
    profileImage: ByteArray?,
    enabled: Boolean,
    showDivider: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rowShape = remember { RoundedRectangle(16.dp) }
    val identity = remember(account.displayName, profileImage) {
        val name = account.displayName.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .joinToString(" ")
        val initials = name.split(' ')
            .filter(String::isNotBlank)
            .take(2)
            .joinToString("") { it.first().toString() }
        accountDisplayIdentity(name, initials, profileImage)
    }
    val degree = account.degreeName.ifBlank {
        if (account.isProfessor) {
            stringResource(Res.string.ui_professor_role)
        } else {
            stringResource(Res.string.ui_degree_label)
        }
    }
    val identifier = account.matricola?.takeIf(String::isNotBlank)?.let {
        stringResource(Res.string.ui_matricola_prefix, it)
    } ?: account.email?.takeIf { it.isNotBlank() && account.isProfessor }
    val details = if (identifier != null) "$identifier · $degree" else degree
    val status = when {
        isActive -> stringResource(Res.string.ui_account_active)
        isActivating -> stringResource(Res.string.ui_account_activating)
        else -> null
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = UniBlockDefaults.RowMinHeight)
                .liquidClickable(
                    shape = rowShape,
                    enabled = enabled,
                    onClick = if (isActive) null else onSelect,
                )
                .semantics(mergeDescendants = true) {
                    selected = isActive
                    if (status != null) stateDescription = status
                }
                .padding(
                    start = UniBlockDefaults.RowHorizontalPadding,
                    end = 8.dp,
                    top = UniBlockDefaults.RowVerticalPadding,
                    bottom = UniBlockDefaults.RowVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UniAccountAvatar(
                imageData = identity.photo,
                initials = identity.initials,
                size = 40.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = identity.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (isActivating) {
                LiquidLoading(
                    modifier = Modifier.size(16.dp),
                    size = LiquidLoadingSize.Small,
                )
            } else {
                Icon(
                    imageVector = if (isActive) LiquidIcons.Check else LiquidIcons.ChevronRight,
                    contentDescription = null,
                    tint = if (isActive) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        LiquidIconButton(
            icon = LiquidIcons.Delete,
            onClick = onRemove,
            enabled = enabled,
            size = 48.dp,
            iconSize = 18.dp,
            variant = LiquidIconButtonVariant.Ghost,
            contentDescription = stringResource(Res.string.ui_account_remove),
        )
    }

    if (showDivider) {
        LiquidHorizontalDivider(
            modifier = Modifier.padding(start = 68.dp, end = UniBlockDefaults.DividerEndInset),
        )
    }
}
