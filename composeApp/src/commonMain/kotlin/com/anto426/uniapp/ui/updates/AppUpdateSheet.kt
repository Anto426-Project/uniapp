package com.anto426.uniapp.ui.updates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AppUpdateSheet(state: AppUpdateUiState, onDismiss: () -> Unit, onUpdate: () -> Unit) {
    LiquidSheet(
        onDismissRequest = onDismiss,
        title = stringResource(if (state.isMandatory) Res.string.ui_update_required_title else Res.string.ui_update_new_available),
        subtitle = "UniApp ${state.displayedVersion}",
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(if (state.isMandatory) Res.string.ui_update_required_body else Res.string.ui_update_available_body))
            state.releaseNotes?.takeIf { it.isNotBlank() }?.let { Text(it) }
            state.errorMessage?.let { Text(it) }
            LiquidButton(
                text = stringResource(Res.string.ui_update_download),
                onClick = onUpdate,
                enabled = state.canOpenUpdate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
