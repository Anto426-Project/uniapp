package com.anto426.uniapp.ui.updates

import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner
import com.anto426.uniapp.updates.presentation.AppUpdateUiState

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop

@Composable
fun UpdatesScreen(
    backdropState: Backdrop,
    uiState: AppUpdateUiState,
    onRetry: () -> Unit,
    onOpenUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
) {
    UniScreenColumn {
        // 1. New High-Fidelity App Update Banner
        UniAppUpdateBanner(
            backdropState = backdropState,
            state = uiState.bannerState,
            version = uiState.displayedVersion,
            title = stringResource(Res.string.ui_app_name),
            subtitle = stringResource(Res.string.ui_university),
            statusText = uiState.statusText,
            channel = uiState.channel,
            onDownload = onOpenUpdate,
            onRetry = onRetry,
        )

        Spacer(Modifier.height(8.dp))

        // 2. Software Version Group (Settings Style)
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_software_version), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = "UniApp ${uiState.installedVersion}",
                subtitle = uiState.errorMessage ?: uiState.statusText ?: stringResource(Res.string.ui_system_updated),
                icon = LiquidIcons.Info,
                backdropState = backdropState
            )

            uiState.releaseNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = if (uiState.isMandatory) "Aggiornamento obbligatorio" else "Novità disponibili",
                    subtitle = notes,
                    icon = if (uiState.isMandatory) LiquidIcons.Warning else LiquidIcons.Refresh,
                    backdropState = backdropState,
                    onClick = onOpenUpdate.takeIf { uiState.canOpenUpdate },
                )
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_changelog),
                subtitle = stringResource(Res.string.ui_changelog_subtitle),
                icon = LiquidIcons.Star,
                backdropState = backdropState,
                onClick = onOpenChangelog,
                trailingContent = {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}
