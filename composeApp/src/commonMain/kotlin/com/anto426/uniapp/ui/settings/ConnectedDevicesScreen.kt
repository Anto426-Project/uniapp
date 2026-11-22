package com.anto426.uniapp.ui.settings

import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.uniapp.data.contract.DeviceInfo
import com.anto426.uniapp.settings.presentation.ConnectedDevicesUiState
import com.anto426.uniapp.settings.presentation.DeviceSessionsActionUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniSecurityFooterNote
import com.anto426.uniapp.ui.components.items.ConnectedDevicesSecurityHero
import com.anto426.uniapp.ui.components.items.CurrentDeviceHero
import com.anto426.uniapp.ui.components.items.DevicePreferenceItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.settings.components.DeviceDisconnectDialog
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*


@Composable
fun ConnectedDevicesScreen(
    uiState: ConnectedDevicesUiState,
    onRequestRevocation: (DeviceInfo) -> Unit,
    onDismissRevocation: () -> Unit,
    onConfirmRevocation: () -> Unit,
    disconnectAllUiState: DeviceSessionsActionUiState,
    onDismissDisconnectAll: () -> Unit,
    onConfirmDisconnectAll: () -> Unit,
) {
    val otherDevices = uiState.otherDevices

    UniScreenColumn {
        UniAnimatedEntrance(order = 0) {
            ConnectedDevicesSecurityHero()
        }

        uiState.currentDevice?.let { device ->
            UniAnimatedEntrance(order = 1) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_current_device),
                    subtitle = stringResource(Res.string.ui_protected_session),
                )
            }
            UniAnimatedEntrance(order = 1) {
                CurrentDeviceHero(device = device)
            }
        }

        if (otherDevices.isNotEmpty()) {
            UniAnimatedEntrance(order = 2) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_other_sessions),
                    subtitle = stringResource(Res.string.ui_manage_access),
                )
            }
            UniInsetGroup(entranceOrder = 2) {
                otherDevices.forEachIndexed { index, device ->
                    DevicePreferenceItem(
                        device = device,
                        onRevoke = if (uiState.canManageSessions && !disconnectAllUiState.isDisconnecting) {
                            { onRequestRevocation(device) }
                        } else {
                            null
                        },
                        showDivider = index < otherDevices.lastIndex,
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

