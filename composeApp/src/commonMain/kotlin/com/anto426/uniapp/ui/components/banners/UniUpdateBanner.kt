package com.anto426.uniapp.ui.components.banners

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.ui.components.cards.UniHeroCardPalette
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.cards.heroTextAccent
import com.anto426.uniapp.ui.components.cards.rememberUniHeroCardPalette
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

internal val HeroBannerTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.displayLarge

@Composable
fun UniAppUpdateBanner(
    modifier: Modifier = Modifier,
    height: Dp = 370.dp,
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
    channelLabel: String? = null,
    isMandatory: Boolean = false,
    palette: UniHeroCardPalette = rememberUniHeroCardPalette(),
) {
    val heroFontSize = when {
        height <= 300.dp -> 68
        height < 380.dp -> 84
        else -> 92
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
                channelLabel = channelLabel,
                isMandatory = isMandatory,
                progress = progress,
                downloadedMb = downloadedMb,
                totalMb = totalMb,
                onDownload = onDownload,
                canDownload = canDownload,
                onRestart = onRestart,
                onRetry = onRetry,
                fontSize = heroFontSize,
                cardHeight = height,
                palette = palette,
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
    channelLabel: String?,
    isMandatory: Boolean,
    progress: Float?,
    downloadedMb: Float,
    totalMb: Float,
    onDownload: () -> Unit,
    canDownload: Boolean,
    onRestart: () -> Unit,
    onRetry: () -> Unit,
    fontSize: Int,
    cardHeight: Dp,
    palette: UniHeroCardPalette,
) {
    val accent = remember(palette) { palette.cool.heroTextAccent() }
    val warmAccent = remember(palette) { palette.warm.heroTextAccent() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = cardHeight)
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // 1. Micro-etichetta di stato minimale (punto colore + testo, senza cornici)
        LiquidAnimatedSwitcher(
            targetState = state,
            modifier = Modifier.fillMaxWidth(),
            mode = LiquidSwitcherMode.Inline,
            label = "UpdateBannerTopStatus",
        ) { currentState ->
            UpdateBannerMinimalStatusLine(
                state = currentState,
                channelLabel = channelLabel,
                isMandatory = isMandatory,
                accent = accent,
                warmAccent = warmAccent,
            )
        }

        // 2. Identità tipografica centrale
        UpdateBannerHeader(
            version = version,
            title = title,
            subtitle = subtitle,
            fontSize = fontSize,
            maxVersionHeight = cardHeight * 0.40f,
        )

        // 3. Azione o avanzamento essenziale senza box contenitori
        LiquidAnimatedSwitcher(
            targetState = state,
            modifier = Modifier.fillMaxWidth(),
            mode = LiquidSwitcherMode.Inline,
            label = "UpdateBannerFooter",
        ) { currentState ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when (currentState) {
                    UpdateState.CHECKING -> UpdateBannerMinimalCaption(
                        text = stringResource(Res.string.ui_update_checking),
                        leading = {
                            LiquidCircularProgressIndicator(
                                indicatorSize = 15.dp,
                                strokeWidth = 2.dp,
                                color = accent,
                            )
                        },
                    )

                    UpdateState.UP_TO_DATE -> UpToDateFooter(
                        message = statusText ?: stringResource(Res.string.ui_updated_version),
                        onCheckAgain = onRetry,
                    )

                    UpdateState.AVAILABLE -> UpdateBannerActionFooter(
                        message = statusText ?: stringResource(Res.string.ui_update_new_available),
                        messageColor = if (isMandatory) warmAccent else accent,
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

                    UpdateState.VERIFYING -> UpdateBannerProgressFooter(
                        text = stringResource(Res.string.ui_update_integrity),
                        icon = LiquidIcons.Lock,
                        accent = accent,
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
                        messageColor = Color(0xFFFFB4AB),
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
}

@Composable
private fun UpdateBannerMinimalStatusLine(
    state: UpdateState,
    channelLabel: String?,
    isMandatory: Boolean,
    accent: Color,
    warmAccent: Color,
) {
    val glassColors = LiquidGlassTheme.colors
    val label = when (state) {
        UpdateState.CHECKING -> stringResource(Res.string.ui_update_search)
        UpdateState.UP_TO_DATE -> stringResource(Res.string.ui_update_up_to_date_badge)
        UpdateState.AVAILABLE -> stringResource(
            if (isMandatory) Res.string.ui_update_mandatory
            else Res.string.ui_update_news,
        )
        UpdateState.DOWNLOADING -> stringResource(Res.string.ui_update_downloading)
        UpdateState.VERIFYING -> stringResource(Res.string.ui_update_verifying)
        UpdateState.INSTALLING -> stringResource(Res.string.ui_update_installing)
        UpdateState.RESTART_REQUIRED -> stringResource(Res.string.ui_update_ready)
        UpdateState.ERROR -> stringResource(Res.string.ui_update_error_title)
    }

    val dotColor = when (state) {
        UpdateState.UP_TO_DATE,
        UpdateState.RESTART_REQUIRED,
        -> glassColors.success
        UpdateState.AVAILABLE -> if (isMandatory) glassColors.warning else warmAccent
        UpdateState.ERROR -> Color(0xFFFFB4AB)
        UpdateState.CHECKING,
        UpdateState.DOWNLOADING,
        UpdateState.VERIFYING,
        UpdateState.INSTALLING,
        -> accent
    }

    val fullText = if (!channelLabel.isNullOrBlank()) "$label · $channelLabel" else label
    MinimalStatusIndicatorRow(
        text = fullText,
        dotColor = dotColor,
        isBusy = state == UpdateState.CHECKING,
    )
}

@Composable
internal fun MinimalStatusIndicatorRow(
    text: String,
    dotColor: Color,
    isBusy: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isBusy) {
            LiquidCircularProgressIndicator(
                indicatorSize = 11.dp,
                strokeWidth = 1.8.dp,
                color = dotColor,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.84f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
            color = Color.White.copy(alpha = 0.96f),
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun UpdateBannerMinimalCaption(
    text: String,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        leading()
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun UpToDateFooter(
    message: String,
    onCheckAgain: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.84f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        LiquidButton(
            text = stringResource(Res.string.ui_update_check_action),
            onClick = onCheckAgain,
            variant = LiquidButtonVariant.Secondary,
            size = LiquidButtonSize.Small,
            modifier = Modifier.widthIn(min = 196.dp, max = 270.dp),
        )
    }
}

@Composable
private fun UpdateBannerProgressFooter(
    text: String,
    icon: ImageVector,
    accent: Color,
) {
    Column(
        modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UpdateBannerMinimalCaption(
            text = text,
            leading = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp),
                )
            },
        )
        LiquidLinearProgressIndicator(
            progress = null,
            modifier = Modifier.fillMaxWidth(),
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
    leadingIcon: ImageVector? = null,
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
                    modifier = Modifier.size(15.dp),
                )
            }
            Text(
                text = message,
                color = messageColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        LiquidButton(
            text = actionText,
            onClick = onAction,
            enabled = actionEnabled,
            variant = buttonVariant,
            modifier = Modifier.widthIn(min = 200.dp, max = 280.dp),
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
        modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
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
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            if (totalMb > 0f) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${formatOneDecimalMb(downloadedMb)} / ${formatOneDecimalMb(totalMb)} MB",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp,
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InstallingFooter(progress: Float?) {
    Column(
        modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
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
 * Retro minimale della Card ruotata a 180° ("NEVER SETTLE").
 */
@Composable
internal fun UpdateBannerNeverSettleBackFace() {
    val palette = rememberUniHeroCardPalette()
    val accent = remember(palette) { palette.warm.heroTextAccent() }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp),
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
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.warm.copy(alpha = 0.16f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.maxDimension * 0.48f,
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
                lineHeight = (fittedSp * 1.02f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = letterSpacingSp.sp,
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "SETTLE",
                fontSize = fittedSp.sp,
                lineHeight = (fittedSp * 1.02f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = letterSpacingSp.sp,
                color = accent,
                style = MaterialTheme.typography.displayMedium,
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
    fontSize: Int = 68,
    maxHeight: Dp = Dp.Unspecified,
) {
    val palette = rememberUniHeroCardPalette()
    val strongAccent = palette.accent
    val density = LocalDensity.current
    val baseStyle = MaterialTheme.typography.displayLarge
    val styledVersion = remember(version, strongAccent) {
        val firstDigitIndex = version.indexOfFirst { it.isDigit() }
        buildAnnotatedString {
            if (firstDigitIndex < 0) {
                withStyle(SpanStyle(color = Color.White)) { append(version) }
            } else {
                if (firstDigitIndex > 0) {
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.94f))) {
                        append(version.substring(0, firstDigitIndex))
                    }
                }
                withStyle(SpanStyle(color = strongAccent)) {
                    append(version[firstDigitIndex])
                }
                if (firstDigitIndex + 1 < version.length) {
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.96f))) {
                        append(version.substring(firstDigitIndex + 1))
                    }
                }
            }
        }
    }

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
            fontWeight = FontWeight.Bold,
            letterSpacing = -(fontSize * 0.035f),
            baseStyle = baseStyle,
        )
        val letterSpacingSp = -(fittedSp * 0.035f)

        Text(
            text = styledVersion,
            fontSize = fittedSp.sp,
            lineHeight = (fittedSp * 1.02f).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = letterSpacingSp.sp,
            style = baseStyle,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .drawBehind {
                    scale(scaleX = 1.45f, scaleY = 0.9f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    palette.glow.copy(alpha = 0.16f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = size.maxDimension * 0.62f,
                            ),
                        )
                    }
                },
        )
    }
}

private fun formatOneDecimalMb(value: Float): String {
    val tenths = (value.coerceAtLeast(0f) * 10f).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
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
    baseStyle: TextStyle = HeroBannerTextStyle,
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
        baseStyle,
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
                style = baseStyle.copy(
                    fontSize = mid.sp,
                    lineHeight = (mid * 1.02f).sp,
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
