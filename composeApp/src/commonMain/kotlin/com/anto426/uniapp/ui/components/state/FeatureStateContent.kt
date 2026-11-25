package com.anto426.uniapp.ui.components.state

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.feedback.runtime.LocalAppToastSink
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.platform.LocalAppIsOffline
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.ui.components.display.UniExpressiveHeroFrame
import com.anto426.uniapp.ui.components.layout.LocalUniScreenPadding
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.motion.UniMotion
import com.kyant.shapes.RoundedRectangle
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
    val colorScheme = MaterialTheme.colorScheme
    val accentTint = colorScheme.primary

    val containerModifier = if (inline) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 20.dp)
    } else {
        Modifier
            .fillMaxSize()
            .padding(LocalUniScreenPadding.current)
            .padding(horizontal = 24.dp)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            UniExpressiveHeroFrame(
                mainContent = {
                    Box(
                        modifier = Modifier
                            .offset(x = (-16).dp, y = (-14).dp)
                            .size(34.dp)
                            .background(accentTint.copy(alpha = 0.20f), RoundedRectangle(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = mainIcon ?: if (isError) LiquidIcons.Warning else LiquidIcons.MenuBook,
                            contentDescription = null,
                            tint = accentTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Stylized signal/document geometric motif in the main ring
                    Canvas(
                        modifier = Modifier
                            .offset(x = 12.dp, y = 4.dp)
                            .size(34.dp),
                    ) {
                        val stroke = 2.4.dp.toPx()
                        drawRoundRect(
                            color = accentTint.copy(alpha = 0.88f),
                            topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
                            size = Size(size.width * 0.72f, size.height * 0.72f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = stroke),
                        )
                        if (isError) {
                            drawLine(
                                color = accentTint.copy(alpha = 0.72f),
                                start = Offset(size.width * 0.26f, size.height * 0.74f),
                                end = Offset(size.width * 0.74f, size.height * 0.26f),
                                strokeWidth = 2.2.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        } else {
                            drawLine(
                                color = accentTint.copy(alpha = 0.72f),
                                start = Offset(size.width * 0.30f, size.height * 0.42f),
                                end = Offset(size.width * 0.70f, size.height * 0.42f),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                            drawLine(
                                color = accentTint.copy(alpha = 0.56f),
                                start = Offset(size.width * 0.30f, size.height * 0.58f),
                                end = Offset(size.width * 0.56f, size.height * 0.58f),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                },
                badgeContent = {
                    Icon(
                        imageVector = badgeIcon ?: if (isError) LiquidIcons.Refresh else LiquidIcons.Info,
                        contentDescription = null,
                        tint = accentTint,
                        modifier = Modifier.size(21.dp),
                    )
                },
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(6.dp))
                LiquidButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    variant = LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Medium,
                )
            }
        }
    }
}
