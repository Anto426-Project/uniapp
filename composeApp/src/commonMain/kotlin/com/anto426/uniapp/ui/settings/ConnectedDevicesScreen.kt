package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniSecurityFooterNote
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.items.ConnectedDevicesSecurityHero
import com.anto426.uniapp.ui.components.items.CurrentDeviceHero
import com.anto426.uniapp.ui.components.items.DevicePreferenceItem
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.settings.presentation.ConnectedDevicesUiState

@Composable
fun ConnectedDevicesScreen(
    uiState: ConnectedDevicesUiState,
    onRequestRevocation: (DeviceInfo) -> Unit,
    onDismissRevocation: () -> Unit,
    onConfirmRevocation: () -> Unit,
) {
    UniScreenColumn {
        ConnectedDevicesSecurityHero()

        uiState.currentDevice?.let { device ->
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_current_device),
                subtitle = stringResource(Res.string.ui_protected_session),
            )
            CurrentDeviceHero(device = device)
        }

        if (uiState.otherDevices.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_other_sessions),
                subtitle = stringResource(Res.string.ui_manage_access),
            )
            UniInsetGroup {
                uiState.otherDevices.forEachIndexed { index, device ->
                    DevicePreferenceItem(
                        device = device,
                        onRevoke = { onRequestRevocation(device) },
                        showDivider = index < uiState.otherDevices.lastIndex,
                    )
                }
            }
        }

        UniSecurityFooterNote(text = stringResource(Res.string.ui_devices_security_note))
    }

    uiState.devicePendingRevocation?.let { device ->
        LiquidDialog(
            onDismissRequest = onDismissRevocation,
            title = stringResource(Res.string.ui_revoke_access),
            text = stringResource(Res.string.ui_revoke_question, device.name),
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_revoke),
                    onClick = onConfirmRevocation,
                    variant = LiquidButtonVariant.Primary,
                    isLoading = uiState.isMutating,
                    enabled = !uiState.isMutating,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissRevocation,
                    variant = LiquidButtonVariant.Text,
                    enabled = !uiState.isMutating,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}
