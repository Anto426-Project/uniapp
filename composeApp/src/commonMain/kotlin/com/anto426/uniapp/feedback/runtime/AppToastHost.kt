package com.anto426.uniapp.feedback.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.anto426.liquidmonet.components.feedback.toast.LiquidToastHost

/** The only Compose bridge between application feedback events and the Liquid toast renderer. */
@Composable
fun AppToastHost(
    manager: AppToastManager,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(manager) { manager.startPresentation() }
    LiquidToastHost(
        state = manager.liquidState,
        modifier = modifier.graphicsLayer(clip = false),
    )
}
