package com.anto426.uniapp.ui.updates

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun UpdatesScreen(
    uiState: AppUpdateUiState,
    onRetry: () -> Unit,
    onOpenUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    UniScreenColumn {
        // 1. Banner Aggiornamento Software Flagship
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
            onRestart = onOpenUpdate,
            onRetry = onRetry,
        )

        Spacer(Modifier.height(4.dp))

        // 2. Canale, Integrità & Note di Rilascio
        UniInsetGroup(title = stringResource(Res.string.ui_update_system_information)) {
            val channel = when {
                uiState.channel.isNullOrBlank() -> stringResource(Res.string.ui_update_channel_unknown)
                uiState.channel.equals("stable", ignoreCase = true) -> stringResource(Res.string.ui_update_stable_channel)
                else -> uiState.channel.orEmpty()
            }
            val isUpToDate = uiState.bannerState == UpdateState.UP_TO_DATE
            UniInsetRow(
                title = stringResource(Res.string.ui_update_installed_version),
                subtitle = stringResource(
                    Res.string.ui_update_installed_details,
                    stringResource(Res.string.ui_app_name),
                    uiState.installedVersion,
                    channel,
                ),
                icon = LiquidIcons.Check,
                iconTint = if (isUpToDate) glassColors.success else colorScheme.primary,
                showDivider = true,
                trailingContent = {
                    val accent = if (isUpToDate) glassColors.success else colorScheme.primary
                    LiquidBadge(
                        text = stringResource(
                            if (isUpToDate) Res.string.ui_update_up_to_date_badge
                            else Res.string.ui_update_installed_badge,
                        ),
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent,
                    )
                },
            )

            if (uiState.supportsPackageVerification) {
                UpdatePackageVerificationItem(uiState.bannerState, showDivider = true)
            }

            UniInsetRow(
                title = stringResource(Res.string.ui_changelog),
                subtitle = stringResource(Res.string.ui_changelog_subtitle),
                icon = LiquidIcons.MenuBook,
                iconTint = colorScheme.secondary,
                onClick = onOpenChangelog,
                showDivider = true,
            )

            UniInsetRow(
                title = stringResource(Res.string.ui_update_check_action),
                subtitle = stringResource(
                    if (uiState.isBusy) Res.string.ui_update_check_in_progress
                    else Res.string.ui_update_check_description,
                ),
                icon = LiquidIcons.Refresh,
                iconTint = colorScheme.primary,
                onClick = if (uiState.isBusy) null else onRetry,
                showDivider = false,
                trailingContent = if (uiState.isBusy) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = colorScheme.primary,
                        )
                    }
                } else null,
            )
        }
    }
}
