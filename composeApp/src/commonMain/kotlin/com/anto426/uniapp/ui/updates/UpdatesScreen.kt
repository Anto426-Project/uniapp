package com.anto426.uniapp.ui.updates

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.updates.components.UpdateChangelogRow
import com.anto426.uniapp.ui.updates.components.UpdatePackageVerificationItem
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun UpdatesScreen(
    uiState: AppUpdateUiState,
    onRetry: () -> Unit,
    onOpenUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
    onToggleDebugMandatory: (Boolean) -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val channel = when {
        uiState.channel.isNullOrBlank() -> stringResource(Res.string.ui_update_channel_unknown)
        uiState.channel.equals("stable", ignoreCase = true) -> stringResource(Res.string.ui_update_stable_channel)
        else -> uiState.channel.orEmpty()
    }
    val channelBadge = if (uiState.channel.isNullOrBlank()) null else channel

    UniScreenColumn {
        // 1. Banner Aggiornamento Software
        UniAnimatedEntrance(order = 0) {
            UniAppUpdateBanner(
                height = 370.dp,
                state = uiState.bannerState,
                version = uiState.displayedVersion,
                title = stringResource(Res.string.ui_app_name),
                subtitle = stringResource(Res.string.ui_university),
                statusText = uiState.statusText?.let { stringResource(it) },
                channelLabel = channelBadge,
                isMandatory = uiState.isMandatory,
                onDownload = onOpenUpdate,
                canDownload = uiState.canOpenUpdate,
                progress = uiState.progress,
                downloadedMb = uiState.downloadedMb,
                totalMb = uiState.totalMb,
                onRestart = onOpenUpdate,
                onRetry = onRetry,
            )
        }

        // 2. Canale, Integrità & Note di Rilascio
        UniInsetGroup(
            title = stringResource(Res.string.ui_update_system_information),
            entranceOrder = 1,
        ) {
            val isUpToDate = uiState.bannerState == UpdateState.UP_TO_DATE
            UniInsetRow(
                title = stringResource(Res.string.ui_update_installed_version),
                subtitle = "${uiState.installedVersion} • $channel",
                icon = LiquidIcons.Check,
                iconTint = if (isUpToDate) glassColors.success else colorScheme.primary,
                showDivider = true,
                titleMaxLines = 1,
                subtitleMaxLines = 1,
                minHeight = 64.dp,
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

            UpdateChangelogRow(onOpenChangelog)
        }

        if (uiState.canSimulateMandatoryUpdate) {
            UniInsetGroup(
                title = "Debug",
                entranceOrder = 2,
            ) {
                UniInsetRow(
                    title = "Simula aggiornamento obbligatorio",
                    subtitle = "Mostra il flusso bloccante in build debug",
                    icon = LiquidIcons.Lock,
                    iconTint = if (uiState.isDebugMandatorySimulated) glassColors.warning else colorScheme.primary,
                    showDivider = false,
                    titleMaxLines = 1,
                    subtitleMaxLines = 1,
                    minHeight = 64.dp,
                    onClick = { onToggleDebugMandatory(!uiState.isDebugMandatorySimulated) },
                    trailingContent = {
                        LiquidSwitch(
                            checked = uiState.isDebugMandatorySimulated,
                            onCheckedChange = onToggleDebugMandatory,
                        )
                    },
                )
            }
        }
    }
}
