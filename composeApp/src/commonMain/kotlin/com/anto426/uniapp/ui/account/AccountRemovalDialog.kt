package com.anto426.uniapp.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.account.accountDisplayIdentity
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AccountRemovalDialog(
    state: AccountSwitcherUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val target = state.accounts.firstOrNull { it.accountId == state.pendingRemovalAccountId } ?: return
    val colorScheme = MaterialTheme.colorScheme
    val removeLabel = stringResource(Res.string.ui_account_remove)
    val cancelLabel = stringResource(Res.string.ui_cancel)
    val profileImage = state.profileImages[target.accountId]
    val identity = remember(target.displayName, profileImage) {
        val name = target.displayName.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .joinToString(" ")
        val initials = name.split(' ')
            .filter(String::isNotBlank)
            .take(2)
            .joinToString("") { it.first().toString() }
        accountDisplayIdentity(name, initials, profileImage)
    }
    val degree = target.degreeName.ifBlank {
        if (target.isProfessor) {
            stringResource(Res.string.ui_professor_role)
        } else {
            stringResource(Res.string.ui_degree_label)
        }
    }
    val identifier = target.matricola?.takeIf(String::isNotBlank)?.let {
        stringResource(Res.string.ui_matricola_prefix, it)
    } ?: target.email?.takeIf { it.isNotBlank() && target.isProfessor }
    val details = if (identifier != null) "$identifier · $degree" else degree

    LiquidDialog(
        title = removeLabel,
        onDismissRequest = { if (!state.isRemovingAccount) onDismiss() },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LiquidButton(
                    onClick = onConfirm,
                    enabled = !state.isRemovingAccount,
                    isLoading = state.isRemovingAccount,
                    containerColor = colorScheme.error,
                    contentColor = colorScheme.onError,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = removeLabel,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                LiquidButton(
                    onClick = onDismiss,
                    enabled = !state.isRemovingAccount,
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UniAccountAvatar(
                    imageData = identity.photo,
                    initials = identity.initials,
                    size = 52.dp,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = identity.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (details.isNotBlank()) {
                        Text(
                            text = details,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    text = stringResource(Res.string.ui_account_remove_confirmation, identity.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}
