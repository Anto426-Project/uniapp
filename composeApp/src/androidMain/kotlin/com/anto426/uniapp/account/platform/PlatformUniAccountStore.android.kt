package com.anto426.uniapp.account.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.anto426.uniapp.account.storage.UniAccountStore

@Composable
internal actual fun rememberPlatformUniAccountStore(): UniAccountStore {
    val applicationContext = LocalContext.current.applicationContext
    return remember(applicationContext) { createAndroidUniAccountStore(applicationContext) }
}
