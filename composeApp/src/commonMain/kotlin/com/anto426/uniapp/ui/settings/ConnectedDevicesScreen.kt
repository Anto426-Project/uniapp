package com.anto426.uniapp.ui.settings

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
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.settings.presentation.ConnectedDevicesUiState
import com.anto426.uniapp.settings.presentation.DeviceSessionsActionUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.display.UniSecurityFooterNote
import com.anto426.uniapp.ui.components.items.ConnectedDevicesSecurityHero
import com.anto426.uniapp.ui.components.items.CurrentDeviceHero
import com.anto426.uniapp.ui.components.items.DevicePreferenceItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ConnectedDevicesScreen(
    uiState: ConnectedDevicesUiState,
    onRequestRevocation: (DeviceInfo) -> Unit,
    onDismissRevocation: () -> Unit,
    onConfirmRevocation: () -> Unit,
    disconnectAllUiState: DeviceSessionsActionUiState,
    onRequestDisconnectAll: () -> Unit,
    onDismissDisconnectAll: () -> Unit,
    onConfirmDisconnectAll: () -> Unit,
) {
    val otherDevices = uiState.otherDevices

    UniScreenColumn {
        ConnectedDevicesSecurityHero()

        uiState.currentDevice?.let { device ->
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_current_device),
                subtitle = stringResource(Res.string.ui_protected_session),
            )
            CurrentDeviceHero(device = device)
        }

        if (otherDevices.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_other_sessions),
                subtitle = stringResource(Res.string.ui_manage_access),
            )
            UniInsetGroup {
                otherDevices.forEachIndexed { index, device ->
                    DevicePreferenceItem(
                        device = device,
                        onRevoke = if (uiState.canManageSessions && !disconnectAllUiState.isDisconnecting) {
                            { onRequestRevocation(device) }
                        } else {
                            null
                        },
                        showDivider = index < otherDevices.lastIndex || uiState.canDisconnectAll,
                    )
                }
                if (uiState.canDisconnectAll) {
                    UniInsetRow(
                        title = stringResource(Res.string.ui_device_disconnect_all_title),
                        icon = LiquidIcons.Close,
                        onClick = onRequestDisconnectAll.takeUnless { disconnectAllUiState.isDisconnecting },
                        showDivider = false,
                        titleMaxLines = 2,
                    )
                }
            }
        }

        if (uiState.canDisconnectAll) {
            UniSecurityFooterNote(text = stringResource(Res.string.ui_devices_security_note))
        }
    }

    val target = uiState.devicePendingRevocation
    if (target != null) {
        DeviceDisconnectDialog(
            title = stringResource(Res.string.ui_revoke_access),
            message = stringResource(Res.string.ui_revoke_question, target.name),
            confirmLabel = stringResource(Res.string.ui_revoke),
            isWorking = uiState.isMutating,
            canConfirm = uiState.canManageSessions,
            onDismiss = onDismissRevocation,
            onConfirm = onConfirmRevocation,
        )
    } else if (disconnectAllUiState.isConfirmationVisible) {
        DeviceDisconnectDialog(
            title = stringResource(Res.string.ui_device_disconnect_all_title),
            message = stringResource(Res.string.ui_device_disconnect_all_confirm),
            confirmLabel = stringResource(Res.string.ui_device_disconnect_all_action),
            isWorking = disconnectAllUiState.isDisconnecting,
            canConfirm = uiState.canDisconnectAll,
            onDismiss = onDismissDisconnectAll,
            onConfirm = onConfirmDisconnectAll,
        )
    }
}

@Composable
private fun DeviceDisconnectDialog(
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
