package com.anto426.uniapp.ui.didactics.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.icons.LiquidIcons
import com.zscanner.BarcodeResult
import com.zscanner.ZScannerCameraMode
import com.zscanner.ZScannerFrameRatio
import com.zscanner.ZScannerScreen
import com.zscanner.permission.ZCameraPermissionState
import com.zscanner.permission.rememberZScannerPermissionController
import com.zscanner.rememberZScannerController
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AttendanceQrScannerDialog(
    isRegistering: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmCode: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var qrInput by remember { mutableStateOf("") }

    // Laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "laserTransition")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "laserScan",
    )

    LiquidDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.ui_attendance_dialog_title),
        confirmButton = {
            LiquidButton(
                onClick = { onConfirmCode(qrInput) },
                variant = LiquidButtonVariant.Primary,
                isLoading = isRegistering,
                enabled = qrInput.isNotBlank() && !isRegistering,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(Res.string.ui_attendance_dialog_confirm), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            LiquidButton(
                onClick = onDismiss,
                variant = LiquidButtonVariant.Glass,
                enabled = !isRegistering,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.ui_attendance_dialog_close))
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Optical Viewfinder Animation Box con scanner incorporato
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedRectangle(24.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(2.dp, colorScheme.primary.copy(alpha = 0.4f), RoundedRectangle(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // Background subtle QR Icon in fase di avvio fotocamera
                Icon(
                    imageVector = LiquidIcons.QrCode,
                    contentDescription = null,
                    tint = colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(72.dp),
                )

                // Scanner fotocamera incorporato
                val permissionController = rememberZScannerPermissionController()
                val scannerController = rememberZScannerController(
                    cameraMode = ZScannerCameraMode.FullScreen,
                    frameRatio = ZScannerFrameRatio.Ratio_1_1,
                    frameColor = colorScheme.primary,
                    showTorchButton = false,
                    showGalleryButton = false,
                )

                ZScannerScreen(
                    onResult = { result ->
                        if (result is BarcodeResult.Success) {
                            val code = result.barcode.data.trim()
                            if (code.isNotBlank()) {
                                qrInput = code
                                onConfirmCode(code)
                            }
                        }
                    },
                    onClose = onDismiss,
                    permissionController = permissionController,
                    scannerController = scannerController,
                    modifier = Modifier.fillMaxSize(),
                    chrome = { content ->
                        // Rimuove DefaultChrome: NESSUNA doppia UI o top bar predefinita!
                        content()
                    },
                    camera = {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Laser scan animato sul feed video
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp)
                                    .graphicsLayer(translationY = (laserPosition - 0.5f) * 140f)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                colorScheme.primary,
                                                colorScheme.primary,
                                                Color.Transparent,
                                            )
                                        )
                                    )
                            )

                            // Tasto torcia / flash incorporato in stile Liquid
                            if (torchAvailable) {
                                IconButton(
                                    onClick = ::toggleTorch,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.45f), RoundedRectangle(12.dp))
                                        .size(34.dp),
                                ) {
                                    Icon(
                                        imageVector = LiquidIcons.Settings,
                                        contentDescription = "Flash",
                                        tint = if (torchEnabled) colorScheme.primary else Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }

                            // Corner brackets visual indicator
                            Text(
                                text = stringResource(Res.string.ui_attendance_dialog_frame_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                            )
                        }
                    },
                    permission = {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = LiquidIcons.QrCode,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Permesso fotocamera richiesto",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LiquidButton(
                                onClick = {
                                    if (state == ZCameraPermissionState.DeniedAlways) {
                                        openAppSettings()
                                    } else {
                                        requestPermission()
                                    }
                                },
                                variant = LiquidButtonVariant.Secondary,
                                modifier = Modifier.fillMaxWidth(0.9f),
                            ) {
                                Text(
                                    text = if (state == ZCameraPermissionState.DeniedAlways) "Apri impostazioni" else "Attiva fotocamera",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                )
            }

            // Input field for manual or scanned code
            LiquidTextField(
                value = qrInput,
                onValueChange = { qrInput = it },
                label = stringResource(Res.string.ui_attendance_dialog_input_label),
                placeholder = stringResource(Res.string.ui_attendance_dialog_input_placeholder),
            )

            Text(
                text = stringResource(Res.string.ui_attendance_dialog_input_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AttendanceKpiCard(
    label: String,
    value: String,
    subvalue: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        shape = RoundedRectangle(20.dp),
        contentPadding = 12.dp,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.primary,
            )
            Text(
                text = subvalue,
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
