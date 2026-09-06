package com.anto426.uniapp.ui.account

import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AccountRemovalDialog(
    state: AccountSwitcherUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val target = state.accounts.firstOrNull { it.accountId == state.pendingRemovalAccountId } ?: return
    LiquidDialog(
        title = stringResource(Res.string.ui_account_remove),
        text = stringResource(Res.string.ui_account_remove_confirmation, target.displayName),
        onDismissRequest = onDismiss,
        confirmButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_account_remove),
                onClick = onConfirm,
                enabled = !state.isRemovingAccount,
                isLoading = state.isRemovingAccount,
            )
        },
        dismissButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_cancel),
                onClick = onDismiss,
                enabled = !state.isRemovingAccount,
                variant = LiquidButtonVariant.Text,
            )
        },
    )
}
