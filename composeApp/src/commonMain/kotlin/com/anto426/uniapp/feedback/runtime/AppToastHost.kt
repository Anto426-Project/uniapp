package com.anto426.uniapp.feedback.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.anto426.liquidmonet.components.feedback.LiquidToastHost
import com.anto426.liquidmonet.components.feedback.LiquidToastType
import com.anto426.liquidmonet.components.feedback.rememberLiquidToastState
import com.kyant.backdrop.Backdrop
import kotlinx.coroutines.flow.collect

/** The only Compose bridge between application feedback events and the Liquid toast renderer. */
@Composable
fun AppToastHost(
    manager: AppToastManager,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    val state = rememberLiquidToastState()
    LaunchedEffect(manager, state) {
        manager.messages.collect { message ->
            state.show(
                message = message.text,
                subtitle = message.subtitle,
                type = message.kind.toLiquidType(),
            )
        }
    }
    LiquidToastHost(
        state = state,
        modifier = modifier.graphicsLayer(clip = false),
        backdropState = backdropState,
    )
}

private fun AppToastKind.toLiquidType(): LiquidToastType =
    when (this) {
        AppToastKind.Success -> LiquidToastType.Success
        AppToastKind.Info -> LiquidToastType.Info
        AppToastKind.Warning -> LiquidToastType.Warning
        AppToastKind.Error -> LiquidToastType.Error
    }
