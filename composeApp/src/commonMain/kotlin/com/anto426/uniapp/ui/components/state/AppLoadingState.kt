package com.anto426.uniapp.ui.components.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.feedback.LiquidLoading
import com.anto426.liquidmonet.components.feedback.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.LiquidLoadingStyle
import com.anto426.uniapp.ui.components.layout.LocalUniScreenPadding
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.Res
import uniapp.composeapp.generated.resources.ui_loading

/** Single application-wide loader used by every full-content loading state. */
@Composable
fun AppLoadingState(
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(LocalUniScreenPadding.current),
        contentAlignment = Alignment.Center,
    ) {
        LiquidLoading(
            style = LiquidLoadingStyle.Dots,
            size = LiquidLoadingSize.Large,
            message = stringResource(Res.string.ui_loading),
            backdropState = backdropState,
        )
    }
}
