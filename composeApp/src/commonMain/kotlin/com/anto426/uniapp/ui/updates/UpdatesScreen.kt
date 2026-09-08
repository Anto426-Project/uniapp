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

@Composable
fun UpdatesScreen(
    uiState: AppUpdateUiState,
    onRetry: () -> Unit,
    onOpenUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
) {
    UniScreenColumn {
        // 1. New High-Fidelity App Update Banner
        UniAppUpdateBanner(
            state = uiState.bannerState,
            version = uiState.displayedVersion,
            title = stringResource(Res.string.ui_app_name),
            subtitle = stringResource(Res.string.ui_university),
            statusText = uiState.statusText?.let { stringResource(it) },
            onDownload = onOpenUpdate,
            canDownload = uiState.canOpenUpdate,
            progress = uiState.progress,
            downloadedMb = uiState.downloadedMb,
            totalMb = uiState.totalMb,
            onRetry = onRetry,
        )

        Spacer(Modifier.height(8.dp))

        // 2. Software Version Group (Settings Style)
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_software_version)) {
            LiquidPreferenceItem(
                title = "${stringResource(Res.string.ui_app_name)} ${uiState.installedVersion}".trim(),
                subtitle = uiState.errorMessage ?: uiState.statusText?.let { stringResource(it) } ?: stringResource(Res.string.ui_system_updated),
                icon = LiquidIcons.Info,
            )

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_changelog),
                subtitle = stringResource(Res.string.ui_changelog_subtitle),
                icon = LiquidIcons.Star,
                onClick = onOpenChangelog,
            )
        }
    }
}
