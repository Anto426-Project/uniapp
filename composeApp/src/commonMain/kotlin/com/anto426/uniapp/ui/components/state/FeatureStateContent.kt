package com.anto426.uniapp.ui.components.state

import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun FeatureStateContent(
    state: FeatureLoadState,
    errorMessage: String?,
    backdropState: Backdrop,
    onRetry: () -> Unit,
    emptyMessage: String = "",
    content: @Composable () -> Unit,
) {
    when (state) {
        FeatureLoadState.Loading -> AppLoadingState(backdropState = backdropState)

        FeatureLoadState.Error ->
            UniScreenColumn {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_state_error_title),
                    description = errorMessage ?: stringResource(Res.string.ui_state_error_desc),
                    icon = LiquidIcons.Warning,
                    actionButtonText = stringResource(Res.string.ui_retry),
                    onActionClick = onRetry,
                    backdropState = backdropState,
                )
            }

        FeatureLoadState.Empty ->
            UniScreenColumn {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_state_empty_content),
                    description = emptyMessage.ifBlank { stringResource(Res.string.ui_state_no_data) },
                    backdropState = backdropState,
                )
            }

        FeatureLoadState.Content -> content()
    }
}
