package com.anto426.uniapp.ui.components.banners

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.ui.components.cards.UniHeroCardPalette
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.cards.heroTextAccent
import com.anto426.uniapp.ui.components.cards.rememberUniHeroCardPalette
import com.anto426.uniapp.ui.motion.LocalUniContentTransition
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

internal val HeroBannerTextStyle = TextStyle(
    shadow = Shadow(Color.Black.copy(alpha = 0.72f), Offset(0f, 2f), blurRadius = 7f),
)

@Composable
fun UniAppUpdateBanner(
    modifier: Modifier = Modifier,
    height: Dp = 480.dp,
    state: UpdateState = UpdateState.UP_TO_DATE,
    version: String,
    progress: Float? = null,
    downloadedMb: Float = 0f,
    totalMb: Float = 0f,
    onDownload: () -> Unit = {},
    canDownload: Boolean = true,
    onRestart: () -> Unit = {},
    onRetry: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    title: String,
    subtitle: String,
    statusText: String? = null,
    palette: UniHeroCardPalette = rememberUniHeroCardPalette(),
) {
    val heroFontSize = when {
        height <= 320.dp -> 74
        height < 400.dp -> 86
        else -> 104
    }

    UniHeroGlassCard(
        modifier = modifier,
        height = height,
        flipTrigger = UniHeroFlipTrigger.LONG_PRESS,
        onClick = onClick,
        palette = palette,
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
                onDownload = onDownload,
                canDownload = canDownload,
                onRestart = onRestart,
                onRetry = onRetry,
                fontSize = heroFontSize,
                cardHeight = height,
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
    progress: Float?,
    downloadedMb: Float,
    totalMb: Float,
    onDownload: () -> Unit,
    canDownload: Boolean,
    onRestart: () -> Unit,
    onRetry: () -> Unit,
    fontSize: Int,
    cardHeight: Dp,
) {
    LiquidAnimatedSwitcher(
        targetState = state,
        modifier = Modifier.fillMaxWidth().heightIn(min = cardHeight),
        transition = LocalUniContentTransition.current,
        label = "UpdateBannerContent",
    ) { currentState ->
        val palette = rememberUniHeroCardPalette()
        val accent = remember(palette) { palette.cool.heroTextAccent() }

        UpdateBannerScaffold(
            version = version,
            title = title,
            subtitle = subtitle,
            fontSize = fontSize,
            cardHeight = cardHeight,
        ) {
            when (currentState) {
                UpdateState.CHECKING -> UpdateBannerStatusPill(
                    text = stringResource(Res.string.ui_update_checking),
                    leading = {
                        LiquidCircularProgressIndicator(
                            indicatorSize = 16.dp,
                            strokeWidth = 2.5.dp,
                            color = accent,
                        )
                    },
                )

                UpdateState.UP_TO_DATE -> Text(
                    text = statusText ?: stringResource(Res.string.ui_updated_version),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                UpdateState.AVAILABLE -> UpdateBannerActionFooter(
                    message = statusText ?: stringResource(Res.string.ui_update_new_available),
                    messageColor = accent,
                    actionText = stringResource(Res.string.ui_update_download),
                    onAction = onDownload,
                    actionEnabled = canDownload,
                    buttonVariant = LiquidButtonVariant.Primary,
                )

                UpdateState.DOWNLOADING -> DownloadingFooter(
                    progress = progress,
                    downloadedMb = downloadedMb,
                    totalMb = totalMb,
                    accent = accent,
                )

                UpdateState.VERIFYING -> UpdateBannerStatusPill(
                    text = stringResource(Res.string.ui_update_integrity),
                    leading = {
                        Icon(
                            imageVector = LiquidIcons.Lock,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )

                UpdateState.INSTALLING -> InstallingFooter(progress = progress)

                UpdateState.RESTART_REQUIRED -> UpdateBannerActionFooter(
                    message = stringResource(Res.string.ui_update_restart_info),
                    messageColor = Color.White.copy(alpha = 0.88f),
                    actionText = stringResource(Res.string.ui_update_restart),
                    onAction = onRestart,
                    actionEnabled = true,
                    buttonVariant = LiquidButtonVariant.Primary,
                )

                UpdateState.ERROR -> UpdateBannerActionFooter(
                    message = stringResource(Res.string.ui_update_error_desc),
                    messageColor = Color(0xFFFF8A80),
                    leadingIcon = LiquidIcons.Warning,
                    actionText = stringResource(Res.string.ui_retry),
                    onAction = onRetry,
                    actionEnabled = true,
                    buttonVariant = LiquidButtonVariant.Secondary,
                )
            }
        }
    }
}

@Composable
private fun UpdateBannerScaffold(
    version: String,
    title: String,
    subtitle: String,
    fontSize: Int,
    cardHeight: Dp,
    footer: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = cardHeight)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        UpdateBannerHeader(
            version = version,
            title = title,
            subtitle = subtitle,
            fontSize = fontSize,
            maxVersionHeight = cardHeight * 0.36f,
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            footer()
        }
    }
}

@Composable
private fun UpdateBannerHeader(
    version: String,
    title: String,
    subtitle: String,
    fontSize: Int,
    maxVersionHeight: Dp,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        VersionText(
            version = version,
            fontSize = fontSize,
            maxHeight = maxVersionHeight,
        )
        Text(
            text = title,
            color = Color.White,
            style = HeroBannerTextStyle,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.94f),
            style = HeroBannerTextStyle,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun UpdateBannerStatusPill(
    text: String,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        leading()
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun UpdateBannerActionFooter(
    message: String,
    messageColor: Color,
    actionText: String,
    onAction: () -> Unit,
    actionEnabled: Boolean,
    buttonVariant: LiquidButtonVariant,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = messageColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = message,
                color = messageColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        LiquidButton(
            text = actionText,
            onClick = onAction,
            enabled = actionEnabled,
            variant = buttonVariant,
            modifier = Modifier.widthIn(min = 200.dp, max = 300.dp),
        )
    }
}

@Composable
private fun DownloadingFooter(
    progress: Float?,
    downloadedMb: Float,
    totalMb: Float,
    accent: Color,
) {
    val percent = progress?.let { (it.coerceIn(0f, 1f) * 100).roundToInt() }

    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = percent?.let { "$it%" } ?: "…",
                color = accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            if (totalMb > 0f) {
                val downloadedStr = ((downloadedMb * 10).roundToInt() / 10f).toString()
                val totalStr = ((totalMb * 10).roundToInt() / 10f).toString()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$downloadedStr MB / $totalStr MB",
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        LiquidLinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(Res.string.ui_update_status_downloading),
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InstallingFooter(progress: Float?) {
    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LiquidLinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(Res.string.ui_update_do_not_close),
            color = Color.White.copy(alpha = 0.76f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Retro della Card ruotata a 180° ("NEVER SETTLE") con profondità ottica cinematografica.
 */
@Composable
internal fun UpdateBannerNeverSettleBackFace() {
    val palette = rememberUniHeroCardPalette()
    val accent = remember(palette) { palette.warm.heroTextAccent() }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxLineHeightPx = with(density) { 100.dp.toPx() }
        val fittedSp = rememberFittedSingleLineSp(
            text = "SETTLE",
            targetSp = 56f,
            minSp = 22f,
            maxWidthPx = maxWidthPx,
            maxHeightPx = maxLineHeightPx,
            fontWeight = FontWeight.Black,
            letterSpacing = 7f,
        )
        val letterSpacingSp = (fittedSp / 56f) * 7f

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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "NEVER",
                fontSize = fittedSp.sp,
                lineHeight = (fittedSp * 1.04f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = letterSpacingSp.sp,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "SETTLE",
                fontSize = fittedSp.sp,
                lineHeight = (fittedSp * 1.04f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = letterSpacingSp.sp,
                color = accent,
                style = HeroBannerTextStyle,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun VersionText(
    version: String,
    fontSize: Int = 88,
    maxHeight: Dp = Dp.Unspecified,
) {
    val palette = rememberUniHeroCardPalette()
    val accent = remember(palette) { palette.warm.heroTextAccent() }
    val targetHighlightIndex = version.indexOfFirst { it.isDigit() }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = if (maxHeight != Dp.Unspecified) {
            with(density) { maxHeight.toPx() }
        } else {
            Float.POSITIVE_INFINITY
        }
        val fittedSp = rememberFittedSingleLineSp(
            text = version,
            targetSp = fontSize.toFloat(),
            minSp = 28f,
            maxWidthPx = maxWidthPx,
            maxHeightPx = maxHeightPx,
            fontWeight = FontWeight.Black,
            letterSpacing = -(fontSize * 0.03f),
        )
        val letterSpacingSp = -(fittedSp * 0.03f)

        // Alone di luce posteriore radiale diffuso proporzionale alla dimensione reale
        val glowWidth = (fittedSp * 2.1f).dp.coerceAtMost(maxWidth)
        val glowHeight = (fittedSp * 1.25f).dp
        Canvas(modifier = Modifier.size(glowWidth, glowHeight)) {
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
                        withStyle(SpanStyle(color = accent)) {
                            append(character)
                        }
                    } else {
                        withStyle(SpanStyle(color = Color.White)) {
                            append(character)
                        }
                    }
                }
            },
            fontSize = fittedSp.sp,
            lineHeight = (fittedSp * 1.04f).sp,
            fontWeight = FontWeight.Black,
            letterSpacing = letterSpacingSp.sp,
            style = HeroBannerTextStyle,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun rememberFittedSingleLineSp(
    text: String,
    targetSp: Float,
    minSp: Float,
    maxWidthPx: Float,
    maxHeightPx: Float,
    fontWeight: FontWeight,
    letterSpacing: Float,
): Float {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val safeTarget = targetSp.coerceAtLeast(minSp)

    return remember(
        text,
        safeTarget,
        minSp,
        maxWidthPx,
        maxHeightPx,
        fontWeight,
        letterSpacing,
        density.density,
        density.fontScale,
    ) {
        if (text.isBlank() || maxWidthPx <= 0f) {
            return@remember safeTarget
        }
        val availableWidth = maxWidthPx * 0.94f
        var low = minSp
        var high = safeTarget
        var best = minSp
        val spacingRatio = letterSpacing / safeTarget

        repeat(12) {
            val mid = (low + high) * 0.5f
            val measured = textMeasurer.measure(
                text = AnnotatedString(text),
                style = HeroBannerTextStyle.copy(
                    fontSize = mid.sp,
                    lineHeight = (mid * 1.04f).sp,
                    fontWeight = fontWeight,
                    letterSpacing = (mid * spacingRatio).sp,
                ),
                maxLines = 1,
                softWrap = false,
                constraints = Constraints(maxWidth = Int.MAX_VALUE),
            )
            if (measured.size.width <= availableWidth && measured.size.height <= maxHeightPx) {
                best = mid
                low = mid
            } else {
                high = mid
            }
        }
        best
    }
}
