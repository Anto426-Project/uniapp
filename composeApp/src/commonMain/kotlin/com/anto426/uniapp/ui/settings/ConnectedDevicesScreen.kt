package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.ui.components.items.CurrentDeviceHero
import com.anto426.uniapp.ui.components.items.DevicePreferenceItem
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.settings.presentation.ConnectedDevicesUiState
import com.kyant.backdrop.Backdrop

@Composable
fun ConnectedDevicesScreen(
    backdropState: Backdrop,
    uiState: ConnectedDevicesUiState,
    onRequestRevocation: (DeviceInfo) -> Unit,
    onDismissRevocation: () -> Unit,
    onConfirmRevocation: () -> Unit,
) {
    UniScreenColumn {
        uiState.currentDevice?.let { device ->
            LiquidSectionHeader(title = stringResource(Res.string.ui_current_device), subtitle = stringResource(Res.string.ui_protected_session))
            CurrentDeviceHero(device = device, backdropState = backdropState)
        }

        if (uiState.otherDevices.isNotEmpty()) {
            LiquidPreferenceGroup(
                title = stringResource(Res.string.ui_other_sessions),
                subtitle = stringResource(Res.string.ui_manage_access),
                backdropState = backdropState
            ) {
                uiState.otherDevices.forEachIndexed { index, device ->
                    DevicePreferenceItem(
                        device = device,
                        backdropState = backdropState,
                        onRevoke = { onRequestRevocation(device) }
                    )
                    if (index < uiState.otherDevices.size - 1) {
                        LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }

    uiState.devicePendingRevocation?.let { device ->
        LiquidDialog(
            onDismissRequest = onDismissRevocation,
            title = stringResource(Res.string.ui_revoke_access),
            text = stringResource(Res.string.ui_revoke_question, device.name),
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_revoke),
                    onClick = onConfirmRevocation,
                    variant = LiquidButtonVariant.Primary,
                    isLoading = uiState.isMutating,
                    enabled = !uiState.isMutating,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissRevocation,
                    variant = LiquidButtonVariant.Text,
                    enabled = !uiState.isMutating,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}
