package com.anto426.uniapp.ui.components.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.uniapp.feedback.runtime.LocalAppToastSink
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.platform.LocalAppIsOffline
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.ui.components.layout.LocalUniScreenPadding
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.motion.UniMotion
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun FeatureStateContent(
    state: FeatureLoadState,
    errorMessage: String?,
    onRetry: () -> Unit,
    emptyTitle: String? = null,
    emptyMessage: String = "",
    emptyActionLabel: String? = null,
    onEmptyActionClick: (() -> Unit)? = null,
    emptyIcon: ImageVector? = null,
    inline: Boolean = false,
    content: @Composable () -> Unit,
) {
    val toastSink = LocalAppToastSink.current
    val isOffline = LocalAppIsOffline.current
    LaunchedEffect(state, errorMessage, isOffline) {
        if (state == FeatureLoadState.Content && !errorMessage.isNullOrBlank() && !isOffline) {
            toastSink.warning(errorMessage)
        }
    }
    UniContentHost(
        targetState = state,
        modifier = if (inline) Modifier.fillMaxWidth() else Modifier.fillMaxSize(),
        transition = UniMotion.inlineContentTransition,
        suppressElementEntrance = false,
        contentAlignment = if (inline) Alignment.TopStart else Alignment.Center,
        label = "FeatureStateContent",
    ) { currentState ->
        when (currentState) {
            FeatureLoadState.Loading -> {
                if (inline) {
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LiquidLoading(
                            style = LiquidLoadingStyle.Dots,
                            size = LiquidLoadingSize.Large,
                            message = stringResource(Res.string.ui_loading),
                        )
                    }
                } else {
                    AppLoadingState()
                }
            }

            FeatureLoadState.Error -> UniExpressiveStatusState(
                isError = true,
                title = stringResource(Res.string.ui_state_error_title),
                description = errorMessage ?: stringResource(Res.string.ui_state_error_desc),
                actionLabel = stringResource(Res.string.ui_retry),
                onActionClick = onRetry,
                inline = inline,
            )

            FeatureLoadState.Empty -> UniExpressiveStatusState(
                isError = false,
                title = emptyTitle?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.ui_state_empty_content),
                description = errorMessage ?: emptyMessage.ifBlank { stringResource(Res.string.ui_state_no_data) },
                actionLabel = if (errorMessage != null) stringResource(Res.string.ui_retry) else emptyActionLabel,
                onActionClick = if (errorMessage != null) onRetry else onEmptyActionClick,
                inline = inline,
                mainIcon = emptyIcon,
            )

            FeatureLoadState.Content -> content()
        }
    }
}

@Composable
fun UniExpressiveStatusState(
    isError: Boolean = false,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    inline: Boolean = false,
    mainIcon: ImageVector? = null,
    badgeIcon: ImageVector? = null,
) {
    val containerModifier = if (inline) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxSize()
            .padding(LocalUniScreenPadding.current)
            .padding(horizontal = 24.dp)
    }

    LiquidEmptyState(
        title = title,
        modifier = containerModifier,
        description = description,
        icon = mainIcon,
        badgeIcon = badgeIcon,
        isError = isError,
        actionButtonText = actionLabel,
        onActionClick = onActionClick,
        contentPadding = if (inline) PaddingValues(horizontal = 8.dp, vertical = 20.dp) else PaddingValues(0.dp),
    )
}
