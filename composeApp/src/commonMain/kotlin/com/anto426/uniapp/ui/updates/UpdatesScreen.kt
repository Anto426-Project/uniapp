package com.anto426.uniapp.ui.updates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Schermata Aggiornamenti Software UniApp.
 *
 * Presenta il banner monumentale a 480dp e i dettagli di sistema essenziali:
 * - Versione ufficiale installata e canale di rilascio
 * - Stato della verifica del pacchetto, quando gestita dall'updater Android
 * - Accesso alle Note di Rilascio ufficiali
 * - Controllo manuale disponibilità aggiornamenti
 */
@Composable
fun UpdatesScreen(
    uiState: AppUpdateUiState,
    onRetry: () -> Unit,
    onOpenUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
) {
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

        Spacer(Modifier.height(8.dp))

        // 2. Canale, Integrità & Note di Rilascio
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_update_system_information)) {
            val channel = when {
                uiState.channel.isNullOrBlank() -> stringResource(Res.string.ui_update_channel_unknown)
                uiState.channel.equals("stable", ignoreCase = true) -> stringResource(Res.string.ui_update_stable_channel)
                else -> uiState.channel.orEmpty()
            }
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_update_installed_version),
                subtitle = stringResource(
                    Res.string.ui_update_installed_details,
                    stringResource(Res.string.ui_app_name),
                    uiState.installedVersion,
                    channel,
                ),
                icon = LiquidIcons.Check,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(Capsule())
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (uiState.bannerState == UpdateState.UP_TO_DATE)
                                    Res.string.ui_update_up_to_date_badge
                                else Res.string.ui_update_installed_badge,
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            if (uiState.supportsPackageVerification) {
                UpdatePackageVerificationItem(uiState.bannerState)
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            }

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_changelog),
                subtitle = stringResource(Res.string.ui_changelog_subtitle),
                icon = LiquidIcons.MenuBook,
                onClick = onOpenChangelog,
                trailingContent = {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_update_check_action),
                subtitle = stringResource(
                    if (uiState.isBusy) Res.string.ui_update_check_in_progress
                    else Res.string.ui_update_check_description,
                ),
                icon = LiquidIcons.Refresh,
                onClick = if (uiState.isBusy) null else onRetry,
                trailingContent = {
                    if (uiState.isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Icon(
                            imageVector = LiquidIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
        }
    }
}
