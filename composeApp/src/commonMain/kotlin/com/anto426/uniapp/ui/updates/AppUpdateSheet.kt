package com.anto426.uniapp.ui.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.sheet.LiquidSheet
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AppUpdateSheet(
    state: AppUpdateUiState,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val channel = state.channel?.takeIf(String::isNotBlank)
    val channelLabel = if (channel.equals("stable", ignoreCase = true)) {
        stringResource(Res.string.ui_update_stable_channel)
    } else {
        channel
    }
    val appName = stringResource(Res.string.ui_app_name)

    LiquidSheet(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.ui_update_new_available),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UniInsetGroup {
                    UniInsetRow(
                        title = "$appName ${state.displayedVersion}".trimEnd(),
                        subtitle = channelLabel,
                        icon = LiquidIcons.Refresh,
                        titleMaxLines = 2,
                    )
                    UniInsetRow(
                        title = stringResource(Res.string.nav_route_changelog_title),
                        icon = LiquidIcons.MenuBook,
                        onClick = onOpenChangelog,
                        showDivider = false,
                        titleMaxLines = 2,
                    )
                }
                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.error,
                    )
                }
                LiquidButton(
                    onClick = onUpdate,
                    enabled = state.canOpenUpdate,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_update_download),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                LiquidButton(
                    onClick = onDismiss,
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
        }
    }
}
