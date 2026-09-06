package com.anto426.uniapp.updates.platform

import androidx.compose.runtime.Composable
import com.anto426.uniapp.updates.presentation.AppUpdateUiState

/** Posts once per release when the user enabled notifications and the OS permits them. */
@Composable
internal expect fun NotifyAvailableAppUpdate(state: AppUpdateUiState, enabled: Boolean)
