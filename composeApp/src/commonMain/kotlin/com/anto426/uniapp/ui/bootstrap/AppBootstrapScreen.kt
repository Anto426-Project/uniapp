package com.anto426.uniapp.ui.bootstrap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.ui.components.state.AppLoadingState
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.Res
import uniapp.composeapp.generated.resources.ui_unlock_account
import uniapp.composeapp.generated.resources.ui_unlock_action
import uniapp.composeapp.generated.resources.ui_unlock_authenticating
import uniapp.composeapp.generated.resources.ui_unlock_other_account
import uniapp.composeapp.generated.resources.ui_unlock_title

@Composable
internal fun AppBootstrapScreen(
    backdropState: Backdrop,
    accountName: String? = null,
    unlockUiState: AppUnlockUiState = AppUnlockUiState(),
    onRequestUnlock: () -> Unit = {},
    onCancelUnlock: () -> Unit = {},
) {
    if (accountName == null) {
        AppLoadingState(backdropState = backdropState)
        return
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
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
            backdropState = backdropState,
        )
    }
}
