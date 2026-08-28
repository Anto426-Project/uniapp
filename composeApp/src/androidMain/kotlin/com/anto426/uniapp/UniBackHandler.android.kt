package com.anto426.uniapp

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun UniBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
