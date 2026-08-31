package com.anto426.uniapp.account.platform

import androidx.compose.runtime.Composable
import com.anto426.uniapp.account.storage.UniAccountStore

@Composable
internal expect fun rememberPlatformUniAccountStore(): UniAccountStore
