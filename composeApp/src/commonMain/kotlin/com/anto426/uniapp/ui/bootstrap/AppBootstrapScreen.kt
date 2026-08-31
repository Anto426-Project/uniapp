package com.anto426.uniapp.ui.bootstrap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.feedback.LiquidLoading
import com.anto426.liquidmonet.components.feedback.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.LiquidLoadingStyle
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.Res
import uniapp.composeapp.generated.resources.ui_app_name

@Composable
internal fun AppBootstrapScreen(backdropState: Backdrop) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LiquidLoading(
                style = LiquidLoadingStyle.Circular,
                size = LiquidLoadingSize.Large,
                backdropState = backdropState,
            )
            Text(
                text = stringResource(Res.string.ui_app_name),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
