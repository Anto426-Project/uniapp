package com.anto426.uniapp.ui.bootstrap

import androidx.compose.runtime.Composable
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.ui.auth.AppUnlockScreen

@Composable
internal fun AppBootstrapScreen(
    accountName: String? = null,
    unlockUiState: AppUnlockUiState = AppUnlockUiState(),
    onRequestUnlock: () -> Unit = {},
    onCancelUnlock: () -> Unit = {},
    onPasswordUnlock: (String) -> Unit = {},
    accountId: String? = null,
) {
    AppUnlockScreen(
        accountName = accountName,
        accountId = accountId,
        unlockUiState = unlockUiState,
        onRequestUnlock = onRequestUnlock,
        onCancelUnlock = onCancelUnlock,
        onPasswordUnlock = onPasswordUnlock,
    )
}
