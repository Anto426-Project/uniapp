package com.anto426.uniapp.ui.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.rememberLiquidAmbientPhase
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import com.zscanner.BarcodeResult
import com.zscanner.ZScannerCameraMode
import com.zscanner.ZScannerCameraScope
import com.zscanner.ZScannerFrameRatio
import com.zscanner.ZScannerScreen
import com.zscanner.frame.ScanFrameSpec
import com.zscanner.permission.ZCameraPermissionState
import com.zscanner.permission.ZScannerPermissionScope
import com.zscanner.permission.rememberZScannerPermissionController
import com.zscanner.rememberZScannerController
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private val ScannerScrimColor = Color(0xAD070A10)
private val ScannerGlassSurface = Color(0x9E0E121A)
private val ScannerGlassBorder = Color.White.copy(alpha = 0.18f)
private val ScannerFrameSpec = ScanFrameSpec(
    widthFraction = 0.74f,
    heightFraction = 0.48f,
    cornerRadius = 28.dp,
    verticalCenterFraction = 0.44f,
)

@Composable
fun UniQrScannerScreen(
    title: String,
    subtitle: String? = null,
    onScanned: (String) -> Unit,
    onClose: () -> Unit,
    onManualInput: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val permissionController = rememberZScannerPermissionController()
    val scannerController = rememberZScannerController(
        cameraMode = ZScannerCameraMode.FullScreen,
        frameRatio = ZScannerFrameRatio.Ratio_1_1,
        frameColor = colorScheme.primary,
        showTorchButton = false,
        showGalleryButton = false,
    )

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        ZScannerScreen(
            onResult = { result ->
                when (result) {
                    is BarcodeResult.Success -> {
                        val raw = result.barcode.data
                        if (raw.isNotBlank()) {
                            onScanned(raw)
                        }
                    }

                    is BarcodeResult.Cancelled -> onClose()

                    is BarcodeResult.Failed -> {
                        // Ignore transient frame decode failure while scanning
                    }
                }
            },
            onClose = onClose,
            permissionController = permissionController,
            scannerController = scannerController,
            frameSpec = ScannerFrameSpec,
            chrome = { content -> content() },
            permission = {
                UniQrPermissionOverlay(
                    onClose = onClose,
                    onManualInput = onManualInput,
                )
            },
            camera = {
                UniQrCameraOverlay(
                    title = title,
                    subtitle = subtitle,
                    onClose = onClose,
                    onManualInput = onManualInput,
                )
            },
            loader = {
                UniQrProcessingOverlay()
            },
        )
    }
}

@Composable
private fun ZScannerCameraScope.UniQrCameraOverlay(
    title: String,
    subtitle: String?,
    onClose: () -> Unit,
    onManualInput: (() -> Unit)?,
) {
    val colorScheme = MaterialTheme.colorScheme
    val hintText = subtitle?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.ui_qr_scanner_hint)

    Box(modifier = Modifier.fillMaxSize()) {
        UniQrViewfinderCanvas(
            frameBounds = frameBounds,
            cornerRadiusDp = frameSpec.cornerRadius,
            accentColor = colorScheme.primary,
            isProcessing = controller.isProcessing,
            modifier = Modifier.fillMaxSize(),
        )

        // Floating Liquid Glass Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ScannerGlassCircleButton(
                icon = LiquidIcons.Close,
                contentDescription = stringResource(Res.string.ui_attendance_dialog_close),
                active = false,
                onClick = onClose,
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .clip(Capsule())
                        .background(ScannerGlassSurface)
                        .border(1.dp, ScannerGlassBorder, Capsule())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.QrCode,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (torchAvailable) {
                ScannerGlassCircleButton(
                    icon = if (torchEnabled) LiquidIcons.FlashOn else LiquidIcons.FlashOff,
                    contentDescription = stringResource(
                        if (torchEnabled) Res.string.ui_qr_scanner_torch_on else Res.string.ui_qr_scanner_torch_off,
                    ),
                    active = torchEnabled,
                    onClick = ::toggleTorch,
                )
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
        }

        // Floating Liquid Glass Bottom Dock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .clip(RoundedRectangle(24.dp))
                    .background(ScannerGlassSurface)
                    .border(1.dp, ScannerGlassBorder, RoundedRectangle(24.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                )

                if (onManualInput != null) {
                    LiquidButton(
                        onClick = onManualInput,
                        variant = LiquidButtonVariant.Glass,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.ui_qr_scanner_manual_input),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerGlassCircleButton(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val performance = LocalLiquidGlassPerformance.current
    val colorSpec = remember(performance) { LiquidMotion.snappySpring<Color>(performance) }
    val containerColor by animateLiquidColorAsState(
        targetValue = if (active) colorScheme.primary else ScannerGlassSurface,
        animationSpec = colorSpec,
        label = "scannerButtonContainer",
    )
    val contentColor by animateLiquidColorAsState(
        targetValue = if (active) colorScheme.onPrimary else Color.White,
        animationSpec = colorSpec,
        label = "scannerButtonContent",
    )
    val borderColor by animateLiquidColorAsState(
        targetValue = if (active) colorScheme.primary.copy(alpha = 0.7f) else ScannerGlassBorder,
        animationSpec = colorSpec,
        label = "scannerButtonBorder",
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun UniQrViewfinderCanvas(
    frameBounds: Rect,
    cornerRadiusDp: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val ambientPhase = rememberLiquidAmbientPhase(
        active = !isProcessing,
        label = "qrScannerMotion",
    )

    val cornerRadiusPx = remember(frameBounds, cornerRadiusDp, density) {
        val requested = with(density) { cornerRadiusDp.toPx() }
        minOf(requested, minOf(frameBounds.width, frameBounds.height) / 2f).coerceAtLeast(0f)
    }

    Canvas(modifier = modifier) {
        if (frameBounds.width <= 0f || frameBounds.height <= 0f) return@Canvas

        val phase = ambientPhase.value
        val beamProgress = (1f - cos(phase * 4f * PI).toFloat()) * 0.5f
        val bracketPulse = 0.80f + 0.20f * ((1f + sin(phase * 6f * PI).toFloat()) * 0.5f)

        val cutoutRoundRect = RoundRect(
            rect = frameBounds,
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        )
        val cutoutPath = Path().apply { addRoundRect(cutoutRoundRect) }

        // Dark translucent scrim with EvenOdd rounded cutout
        val scrimPath = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(cutoutRoundRect)
            fillType = PathFillType.EvenOdd
        }
        drawPath(scrimPath, ScannerScrimColor)

        // Subtle frosted rim around the active window
        drawRoundRect(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(frameBounds.left, frameBounds.top),
            size = Size(frameBounds.width, frameBounds.height),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            style = Stroke(width = 1.dp.toPx()),
        )

        // Animated liquid scan beam clipped cleanly inside the cutout
        if (!isProcessing) {
            val inset = 14.dp.toPx()
            val beamY = frameBounds.top + inset + (frameBounds.height - inset * 2f).coerceAtLeast(0f) * beamProgress
            val trailHeight = 42.dp.toPx()

            clipPath(cutoutPath) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0.16f),
                            accentColor.copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                        startY = beamY - trailHeight,
                        endY = beamY + trailHeight,
                    ),
                    topLeft = Offset(frameBounds.left, beamY - trailHeight),
                    size = Size(frameBounds.width, trailHeight * 2f),
                )
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0.92f),
                            Color.White.copy(alpha = 0.95f),
                            accentColor.copy(alpha = 0.92f),
                            Color.Transparent,
                        ),
                        startX = frameBounds.left + inset,
                        endX = frameBounds.right - inset,
                    ),
                    start = Offset(frameBounds.left + inset, beamY),
                    end = Offset(frameBounds.right - inset, beamY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        // Refined Monet corner brackets (soft glow + crisp foreground stroke)
        val effectiveAlpha = if (isProcessing) 1f else bracketPulse
        val bracketLegPx = minOf(28.dp.toPx(), minOf(frameBounds.width, frameBounds.height) * 0.16f)
        drawCornerBrackets(
            frameBounds = frameBounds,
            cornerRadiusPx = cornerRadiusPx,
            legLengthPx = bracketLegPx,
            color = accentColor.copy(alpha = 0.26f * effectiveAlpha),
            strokeWidthPx = 8.dp.toPx(),
        )
        drawCornerBrackets(
            frameBounds = frameBounds,
            cornerRadiusPx = cornerRadiusPx,
            legLengthPx = bracketLegPx,
            color = accentColor.copy(alpha = effectiveAlpha),
            strokeWidthPx = 3.5.dp.toPx(),
        )
    }
}

private fun DrawScope.drawCornerBrackets(
    frameBounds: Rect,
    cornerRadiusPx: Float,
    legLengthPx: Float,
    color: Color,
    strokeWidthPx: Float,
) {
    val r = cornerRadiusPx
    val d = r * 2f
    val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

    // Top-left
    val topLeft = Path().apply {
        moveTo(frameBounds.left, frameBounds.top + r + legLengthPx)
        lineTo(frameBounds.left, frameBounds.top + r)
        arcTo(
            rect = Rect(frameBounds.left, frameBounds.top, frameBounds.left + d, frameBounds.top + d),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(frameBounds.left + r + legLengthPx, frameBounds.top)
    }
    drawPath(topLeft, color = color, style = stroke)

    // Top-right
    val topRight = Path().apply {
        moveTo(frameBounds.right - r - legLengthPx, frameBounds.top)
        lineTo(frameBounds.right - r, frameBounds.top)
        arcTo(
            rect = Rect(frameBounds.right - d, frameBounds.top, frameBounds.right, frameBounds.top + d),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(frameBounds.right, frameBounds.top + r + legLengthPx)
    }
    drawPath(topRight, color = color, style = stroke)

    // Bottom-right
    val bottomRight = Path().apply {
        moveTo(frameBounds.right, frameBounds.bottom - r - legLengthPx)
        lineTo(frameBounds.right, frameBounds.bottom - r)
        arcTo(
            rect = Rect(frameBounds.right - d, frameBounds.bottom - d, frameBounds.right, frameBounds.bottom),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(frameBounds.right - r - legLengthPx, frameBounds.bottom)
    }
    drawPath(bottomRight, color = color, style = stroke)

    // Bottom-left
    val bottomLeft = Path().apply {
        moveTo(frameBounds.left + r + legLengthPx, frameBounds.bottom)
        lineTo(frameBounds.left + r, frameBounds.bottom)
        arcTo(
            rect = Rect(frameBounds.left, frameBounds.bottom - d, frameBounds.left + d, frameBounds.bottom),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(frameBounds.left, frameBounds.bottom - r - legLengthPx)
    }
    drawPath(bottomLeft, color = color, style = stroke)
}

@Composable
private fun ZScannerCameraScope.UniQrProcessingOverlay() {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val cornerRadiusPx = remember(frameBounds, frameSpec.cornerRadius, density) {
        val requested = with(density) { frameSpec.cornerRadius.toPx() }
        minOf(requested, minOf(frameBounds.width, frameBounds.height) / 2f).coerceAtLeast(0f)
    }
    val shape = RoundedRectangle(with(density) { cornerRadiusPx.toDp() })

    Box(
        modifier = Modifier
            .offset {
                IntOffset(frameBounds.left.roundToInt(), frameBounds.top.roundToInt())
            }
            .size(
                width = with(density) { frameBounds.width.toDp() },
                height = with(density) { frameBounds.height.toDp() },
            )
            .clip(shape)
            .background(Color(0xA6070A10)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(20.dp),
        ) {
            LiquidLoading(
                style = LiquidLoadingStyle.Circular,
                size = LiquidLoadingSize.Medium,
                tint = colorScheme.primary,
            )
            Box(
                modifier = Modifier
                    .clip(Capsule())
                    .background(ScannerGlassSurface)
                    .border(1.dp, ScannerGlassBorder, Capsule())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_qr_scanner_processing),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ZScannerPermissionScope.UniQrPermissionOverlay(
    onClose: () -> Unit,
    onManualInput: (() -> Unit)?,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDeniedAlways = state == ZCameraPermissionState.DeniedAlways
    val accent = if (isDeniedAlways) colorScheme.error else colorScheme.primary
    val titleText = stringResource(
        if (isDeniedAlways) {
            Res.string.ui_qr_scanner_permission_denied_title
        } else {
            Res.string.ui_qr_scanner_permission_title
        },
    )
    val bodyText = stringResource(
        if (isDeniedAlways) {
            Res.string.ui_qr_scanner_permission_denied_body
        } else {
            Res.string.ui_qr_scanner_permission_body
        },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidIconButton(
                icon = LiquidIcons.Close,
                contentDescription = stringResource(Res.string.ui_attendance_dialog_close),
                onClick = onClose,
                variant = LiquidIconButtonVariant.Standard,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            LiquidCard(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                shape = RoundedRectangle(28.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isDeniedAlways) LiquidIcons.Lock else LiquidIcons.Camera,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(30.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = bodyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LiquidButton(
                        onClick = {
                            if (isDeniedAlways) openAppSettings() else requestPermission()
                        },
                        variant = LiquidButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = if (isDeniedAlways) LiquidIcons.Settings else LiquidIcons.Camera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                if (isDeniedAlways) {
                                    Res.string.ui_qr_scanner_open_settings
                                } else {
                                    Res.string.ui_qr_scanner_permission_allow
                                },
                            ),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    if (onManualInput != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LiquidButton(
                            onClick = onManualInput,
                            variant = LiquidButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.ui_qr_scanner_manual_input),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LiquidButton(
                        onClick = onClose,
                        variant = LiquidButtonVariant.Text,
                        text = stringResource(Res.string.ui_attendance_dialog_close),
                    )
                }
            }
        }
    }
}

@Composable
fun UniQrScannerDialog(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit,
    onManualInput: (() -> Unit)? = null,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        UniQrScannerScreen(
            title = title,
            subtitle = subtitle,
            onScanned = onScanned,
            onClose = onDismiss,
            onManualInput = onManualInput,
        )
    }
}
