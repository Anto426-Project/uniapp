package com.anto426.uniapp.ui.bootstrap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.components.inputs.LiquidTextFieldType
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.session.presentation.AppPasswordUnlockError
import com.anto426.uniapp.ui.components.state.AppLoadingState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AppBootstrapScreen(
    accountName: String? = null,
    unlockUiState: AppUnlockUiState = AppUnlockUiState(),
    onRequestUnlock: () -> Unit = {},
    onCancelUnlock: () -> Unit = {},
    onPasswordUnlock: (String) -> Unit = {},
    accountId: String? = null,
) {
    if (accountName == null) {
        AppLoadingState()
        return
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_unlock_title),
                description =
                    unlockUiState.errorMessage
                        ?: if (unlockUiState.isAuthenticating) {
                            stringResource(Res.string.ui_unlock_authenticating)
                        } else {
                            stringResource(Res.string.ui_unlock_account, accountName)
                        },
                icon = LiquidIcons.Lock,
                actionButtonText =
                    if (unlockUiState.isAuthenticating) null else stringResource(Res.string.ui_unlock_action),
                onActionClick = if (unlockUiState.isAuthenticating) null else onRequestUnlock,
                secondaryActionButtonText =
                    if (unlockUiState.isAuthenticating) null
                    else stringResource(Res.string.ui_unlock_other_account),
                onSecondaryActionClick = if (unlockUiState.isAuthenticating) null else onCancelUnlock,
            )
            var password by remember(accountId) { mutableStateOf("") }
            LiquidTextField(
                value = password,
                onValueChange = { password = it },
                type = LiquidTextFieldType.Password,
                label = stringResource(Res.string.ui_app_password_label),
                enabled = !unlockUiState.isAuthenticating,
            )
            unlockUiState.passwordError?.let { error ->
                Text(
                    stringResource(when (error) {
                        AppPasswordUnlockError.Invalid -> Res.string.ui_app_password_invalid
                        AppPasswordUnlockError.RetryLater -> Res.string.ui_app_password_retry_later
                        AppPasswordUnlockError.Failed -> Res.string.ui_app_password_unlock_failed
                    }),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            LiquidButton(
                text = stringResource(Res.string.ui_app_password_unlock),
                enabled = !unlockUiState.isAuthenticating && password.isNotEmpty(),
                onClick = {
                    onPasswordUnlock(password)
                    password = ""
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
