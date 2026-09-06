package com.anto426.uniapp.ui.components.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.anto426.uniapp.feedback.runtime.LocalAppToastSink
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun FeatureStateContent(
    state: FeatureLoadState,
    errorMessage: String?,
    onRetry: () -> Unit,
    emptyMessage: String = "",
    content: @Composable () -> Unit,
) {
    val toastSink = LocalAppToastSink.current
    LaunchedEffect(state, errorMessage) {
        if (state == FeatureLoadState.Content && !errorMessage.isNullOrBlank()) {
            toastSink.warning(errorMessage)
        }
    }
    when (state) {
        FeatureLoadState.Loading -> AppLoadingState()

        FeatureLoadState.Error ->
            UniScreenColumn {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_state_error_title),
                    description = errorMessage ?: stringResource(Res.string.ui_state_error_desc),
                    icon = LiquidIcons.Warning,
                    actionButtonText = stringResource(Res.string.ui_retry),
                    onActionClick = onRetry,
                )
            }

        FeatureLoadState.Empty ->
            UniScreenColumn {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_state_empty_content),
                    description = errorMessage ?: emptyMessage.ifBlank { stringResource(Res.string.ui_state_no_data) },
                    actionButtonText = if (errorMessage != null) stringResource(Res.string.ui_retry) else null,
                    onActionClick = if (errorMessage != null) onRetry else null,
                )
            }

        FeatureLoadState.Content -> content()
    }
}
