package com.anto426.uniapp.ui.scanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
@Composable
expect fun UniQrScannerScreen(
    title: String, subtitle: String? = null, onScanned: (String) -> Unit,
    onClose: () -> Unit, onManualInput: (() -> Unit)? = null, modifier: Modifier = Modifier,
)

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
