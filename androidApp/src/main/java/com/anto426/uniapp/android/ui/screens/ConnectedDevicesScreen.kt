package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.android.ui.components.items.CurrentDeviceHero
import com.anto426.uniapp.android.ui.components.items.DevicePreferenceItem
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.anto426.uniapp.android.ui.models.DeviceInfo
import com.kyant.backdrop.Backdrop

@Composable
fun ConnectedDevicesScreen(backdropState: Backdrop) {
    var deviceToRevoke by remember { mutableStateOf<DeviceInfo?>(null) }

    val devices = remember { UiInitialData.devices }

    val currentDevice = devices.find { it.isCurrent }
    val otherDevices = devices.filter { !it.isCurrent }

    UniScreenColumn {
        currentDevice?.let { device ->
            LiquidSectionTitle(title = stringResource(R.string.ui_current_device), subtitle = stringResource(R.string.ui_protected_session))
            CurrentDeviceHero(device = device, backdropState = backdropState)
        }

        if (otherDevices.isNotEmpty()) {
            LiquidSectionTitle(title = stringResource(R.string.ui_other_sessions), subtitle = stringResource(R.string.ui_manage_access))
            LiquidPreferenceGroup(backdropState = backdropState) {
                otherDevices.forEachIndexed { index, device ->
                    DevicePreferenceItem(
                        device = device,
                        backdropState = backdropState,
                        onRevoke = { deviceToRevoke = device }
                    )
                    if (index < otherDevices.size - 1) {
                        LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }

    deviceToRevoke?.let { device ->
        LiquidDialog(
            onDismissRequest = { deviceToRevoke = null },
            title = stringResource(R.string.ui_revoke_access),
            text = stringResource(R.string.ui_revoke_question, device.name),
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = stringResource(R.string.ui_revoke),
                    onClick = { deviceToRevoke = null },
                    variant = LiquidButtonVariant.Primary,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(R.string.ui_cancel),
                    onClick = { deviceToRevoke = null },
                    variant = LiquidButtonVariant.Text,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}
