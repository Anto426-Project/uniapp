package com.anto426.uniapp.ui.scanner

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.icons.LiquidIcons
import com.zscanner.BarcodeResult
import com.zscanner.ZScannerCameraMode
import com.zscanner.ZScannerFrameRatio
import com.zscanner.ZScannerScreen
import com.zscanner.permission.rememberZScannerPermissionController
import com.zscanner.rememberZScannerController

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
        showTorchButton = true,
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
                    is BarcodeResult.Cancelled -> {
                        onClose()
                    }
                    is BarcodeResult.Failed -> {
                        // Ignora errore momentaneo di lettura frame
                    }
                }
            },
            onClose = onClose,
            permissionController = permissionController,
            scannerController = scannerController,
            chrome = { content ->
                // Strips DefaultChrome to prevent double UI / top app bar
                content()
            },
        )

        // Top Bar Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent,
                        ),
                    ),
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = LiquidIcons.Close,
                        contentDescription = stringResource(Res.string.ui_attendance_dialog_close),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )

                // Placeholder bilanciamento pulsante chiudi
                Spacer(modifier = Modifier.size(48.dp))
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }

        // Bottom Bar Overlay con azione inserimento manuale
        if (onManualInput != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                            ),
                        ),
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
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
                        text = "Inserisci codice manualmente",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
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
