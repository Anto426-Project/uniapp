package com.anto426.uniapp.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun DeviceDisconnectDialog(
    title: String,
    message: String,
    confirmLabel: String,
    isWorking: Boolean,
    canConfirm: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    LiquidDialog(
        onDismissRequest = { if (!isWorking) onDismiss() },
        title = title,
        text = message,
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LiquidButton(
                    onClick = onConfirm,
                    enabled = canConfirm && !isWorking,
                    isLoading = isWorking,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = confirmLabel,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                LiquidButton(
                    onClick = onDismiss,
                    enabled = !isWorking,
                    variant = LiquidButtonVariant.Text,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_cancel),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}
