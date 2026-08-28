package com.anto426.uniapp

import androidx.compose.runtime.Composable

@Composable
internal expect fun UniBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
)
