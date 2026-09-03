package com.anto426.uniapp.ui.components.banners

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.layout.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.LiquidSwitcherTransition
import com.anto426.liquidmonet.icons.LiquidIcons
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.cards.rememberUniHeroCardPalette
import com.kyant.backdrop.Backdrop
import kotlin.math.roundToInt

@Composable
fun UniAppUpdateBanner(
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
    state: UpdateState = UpdateState.UP_TO_DATE,
    version: String = "2.0",
    progress: Float = 0f,
    downloadedMb: Float = 0f,
    totalMb: Float = 0f,
    onDownload: () -> Unit = {},
    onRestart: () -> Unit = {},
    onRetry: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    title: String = "UniApp",
    subtitle: String = "Università degli Studi del Molise",
    statusText: String? = null,
    channel: String? = null,
) {
    UniHeroGlassCard(
        backdropState = backdropState,
        modifier = modifier,
        height = 480.dp,
        flipTrigger = UniHeroFlipTrigger.LONG_PRESS,
        onClick = onClick,
        frontContent = {
            UpdateBannerFrontFace(
                state = state,
                version = version,
                title = title,
                subtitle = subtitle,
                statusText = statusText,
                progress = progress,
                downloadedMb = downloadedMb,
                totalMb = totalMb,
                backdropState = backdropState,
                onDownload = onDownload,
                onRestart = onRestart,
                onRetry = onRetry,
            )
        },
        backContent = {
            UpdateBannerNeverSettleBackFace()
        },
    )
}

@Composable
private fun UpdateBannerFrontFace(
    state: UpdateState,
    version: String,
    title: String,
    subtitle: String,
    statusText: String?,
    progress: Float,
    downloadedMb: Float,
    totalMb: Float,
    backdropState: Backdrop,
    onDownload: () -> Unit,
    onRestart: () -> Unit,
    onRetry: () -> Unit,
) {
    LiquidAnimatedSwitcher(
        targetState = state,
        modifier = Modifier.fillMaxSize(),
        transition = LiquidSwitcherTransition.LiquidMorph,
        isForward = { initialState, targetState ->
            targetState.ordinal >= initialState.ordinal
        },
        label = "UpdateBannerContent",
    ) { currentState ->
        when (currentState) {
            UpdateState.CHECKING -> CheckingContent()
            UpdateState.UP_TO_DATE -> UpToDateContent(
                version = version,
                title = title,
                subtitle = subtitle,
                statusText = statusText ?: stringResource(Res.string.ui_updated_version),
            )
            UpdateState.AVAILABLE -> AvailableContent(
                version = version,
                title = title,
                subtitle = statusText ?: stringResource(Res.string.ui_update_new_available),
                backdropState = backdropState,
                onDownload = onDownload,
            )
            UpdateState.DOWNLOADING -> DownloadingContent(
                version = version,
                progress = progress,
                downloadedMb = downloadedMb,
                totalMb = totalMb,
                backdropState = backdropState,
            )
            UpdateState.VERIFYING -> VerifyingContent()
            UpdateState.INSTALLING -> InstallingContent(
                progress = progress,
                backdropState = backdropState,
            )
            UpdateState.RESTART_REQUIRED -> RestartContent(
                onRestart = onRestart,
                backdropState = backdropState,
            )
            UpdateState.ERROR -> ErrorContent(
                onRetry = onRetry,
                backdropState = backdropState,
            )
        }
    }
}

/**
 * Retro della Card ruotata a 180° ("NEVER SETTLE") con profondità ottica cinematografica.
 */
@Composable
private fun UpdateBannerNeverSettleBackFace() {
    val scheme = MaterialTheme.colorScheme
    val palette = rememberUniHeroCardPalette()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Alone di luce posteriore
        Canvas(modifier = Modifier.size(280.dp, 200.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.warm.copy(alpha = 0.28f),
                        palette.violet.copy(alpha = 0.12f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.maxDimension * 0.55f,
                ),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "NEVER",
                fontSize = 56.sp,
                lineHeight = 58.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 7.sp,
                color = scheme.onSurface,
            )
            Text(
                text = "SETTLE",
                fontSize = 56.sp,
                lineHeight = 58.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 7.sp,
                color = palette.warm,
            )
        }
    }
}

@Composable
private fun UpToDateContent(
    version: String,
    title: String,
    subtitle: String,
    statusText: String,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            VersionText(version = version, fontSize = 104)
            Text(
                text = title,
                color = scheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = subtitle,
                color = scheme.onSurface.copy(alpha = 0.72f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Text(
            text = statusText,
            color = scheme.onSurface.copy(alpha = 0.66f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AvailableContent(
    version: String,
    title: String,
    subtitle: String,
    backdropState: Backdrop,
    onDownload: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 36.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            VersionText(version = version, fontSize = 92)
            Text(
                text = title,
                color = scheme.onSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                color = scheme.onSurface.copy(alpha = 0.72f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        LiquidButton(
            text = stringResource(Res.string.ui_update_download),
            onClick = onDownload,
            variant = LiquidButtonVariant.Glass,
            backdropState = backdropState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(200.dp),
        )
    }
}

@Composable
private fun DownloadingContent(
    version: String,
    progress: Float,
    downloadedMb: Float,
    totalMb: Float,
    backdropState: Backdrop,
) {
    val scheme = MaterialTheme.colorScheme
    val percent = (progress.coerceIn(0f, 1f) * 100).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Download Status Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = LiquidIcons.Refresh,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(Res.string.ui_update_downloading),
                color = scheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Center Percentage and Version
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "$percent%",
                color = scheme.onSurface,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1.5).sp,
            )
            VersionText(version = version, fontSize = 24)
        }

        // Bottom Progress Bar & MB Details
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            LiquidLinearProgressIndicator(
                progress = progress,
                backdropState = backdropState,
                modifier = Modifier.fillMaxWidth(),
            )
            if (totalMb > 0f) {
                Text(
                    text = "${downloadedMb} MB / ${totalMb} MB",
                    color = scheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "updateCheckingTransition")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1600, easing = LinearEasing)),
        label = "updateCheckingRotation",
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(modifier = Modifier.size(68.dp)) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, scheme.primary.copy(alpha = 0.25f), scheme.primary),
                ),
                startAngle = rotation,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(Res.string.ui_update_search),
            color = scheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.ui_update_checking),
            color = scheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun VerifyingContent() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(scheme.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LiquidIcons.Lock,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.ui_update_verifying),
            color = scheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.ui_update_integrity),
            color = scheme.onSurface.copy(alpha = 0.65f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun InstallingContent(
    progress: Float,
    backdropState: Backdrop,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.ui_update_installing),
                color = scheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.ui_update_do_not_close),
                color = scheme.onSurface.copy(alpha = 0.65f),
                fontSize = 13.sp,
            )
        }

        // Center Pulsing Settings Icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(scheme.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LiquidIcons.Settings,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }

        // Bottom Progress Indicator
        LiquidLinearProgressIndicator(
            progress = progress,
            backdropState = backdropState,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RestartContent(
    onRestart: () -> Unit,
    backdropState: Backdrop,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(scheme.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LiquidIcons.Check,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(30.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.ui_update_ready),
                color = scheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.ui_update_restart_info),
                color = scheme.onSurface.copy(alpha = 0.65f),
                fontSize = 13.sp,
            )
        }

        LiquidButton(
            text = stringResource(Res.string.ui_update_restart),
            onClick = onRestart,
            variant = LiquidButtonVariant.Glass,
            backdropState = backdropState,
            modifier = Modifier.width(200.dp),
        )
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    backdropState: Backdrop,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(scheme.error.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LiquidIcons.Warning,
                contentDescription = null,
                tint = scheme.error,
                modifier = Modifier.size(28.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.ui_update_error_title),
                color = scheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.ui_update_error_desc),
                color = scheme.onSurface.copy(alpha = 0.65f),
                fontSize = 13.sp,
            )
        }

        LiquidButton(
            text = stringResource(Res.string.ui_retry),
            onClick = onRetry,
            variant = LiquidButtonVariant.Glass,
            backdropState = backdropState,
            modifier = Modifier.width(200.dp),
        )
    }
}

@Composable
private fun VersionText(
    version: String,
    fontSize: Int = 88,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = rememberUniHeroCardPalette()
    val targetHighlightIndex = version.indexOf('1').takeIf { it != -1 } ?: version.indexOfFirst { it.isDigit() }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        // Alone di luce posteriore radiale diffuso
        Canvas(modifier = Modifier.size((fontSize * 2.2).dp, (fontSize * 1.3).dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.warm.copy(alpha = 0.22f),
                        palette.violet.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.maxDimension * 0.52f,
                ),
            )
        }

        Text(
            text = buildAnnotatedString {
                version.forEachIndexed { index, character ->
                    if (index == targetHighlightIndex) {
                        withStyle(SpanStyle(color = palette.warm)) {
                            append(character)
                        }
                    } else {
                        withStyle(SpanStyle(color = scheme.onSurface)) {
                            append(character)
                        }
                    }
                }
            },
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-3).sp,
        )
    }
}
