package com.anto426.uniapp.account.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.uniapp.account.storage.UniAccountStore

@Composable
internal actual fun rememberPlatformUniAccountStore(): UniAccountStore =
    remember { createIosUniAccountStore() }
