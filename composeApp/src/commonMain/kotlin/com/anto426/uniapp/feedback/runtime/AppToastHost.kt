package com.anto426.uniapp.feedback.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.anto426.liquidmonet.components.feedback.toast.LiquidToastHost
import com.anto426.liquidmonet.components.feedback.toast.rememberLiquidToastState
import com.anto426.liquidmonet.icons.LiquidIcons
import kotlinx.coroutines.flow.collect
import com.anto426.uniapp.presentation.sdkFeedbackMessage

/** The only Compose bridge between application feedback events and the Liquid toast renderer. */
@Composable
fun AppToastHost(
    manager: AppToastManager,
    modifier: Modifier = Modifier,
) {
    val state = rememberLiquidToastState()
    LaunchedEffect(manager, state) {
        manager.messages.collect { message ->
            state.show(
                message = sdkFeedbackMessage(message.text),
                subtitle = message.subtitle?.let { sdkFeedbackMessage(it) },
                icon = message.kind.toIcon(),
            )
        }
    }
    LiquidToastHost(
        state = state,
        modifier = modifier.graphicsLayer(clip = false),
    )
}

private fun AppToastKind.toIcon(): ImageVector =
    when (this) {
        AppToastKind.Success -> LiquidIcons.Check
        AppToastKind.Info -> LiquidIcons.Info
        AppToastKind.Warning -> LiquidIcons.Warning
        AppToastKind.Error -> LiquidIcons.Close
    }
