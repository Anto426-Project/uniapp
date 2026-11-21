package com.anto426.uniapp.feedback.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.anto426.liquidmonet.components.feedback.toast.LiquidToastHost
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import org.jetbrains.compose.resources.rememberResourceEnvironment

/** The only Compose bridge between application feedback events and the Liquid toast renderer. */
@Composable
fun AppToastHost(
    manager: AppToastManager,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val environment = rememberResourceEnvironment()
    LaunchedEffect(manager, environment) { manager.bindPresentation(environment) }
    LiquidToastHost(
        state = manager.liquidState,
        modifier = modifier.graphicsLayer(clip = false),
        backdropState = backdropState,
    )
}
