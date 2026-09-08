package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.anto426.unisdk.platform.AppInfoProvider
import com.anto426.unisdk.platform.AppModuleInfo
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AppInfoScreen(
    updateUiState: AppUpdateUiState,
    onOpenAboutUniApp: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenCookies: () -> Unit,
    onOpenCreatorCredits: () -> Unit,
    onOpenUpdates: () -> Unit,
    onReportBug: () -> Unit = {},
    onOpenChangelog: () -> Unit = {},
    onOpenSource: () -> Unit = {},
) {
    val appInfo by AppInfoProvider.info.collectAsState()

    UniScreenColumn {
        UniAppUpdateBanner(
            modifier = Modifier.fillMaxWidth(),
            height = 360.dp,
            state = updateUiState.bannerState,
            version = updateUiState.displayedVersion.ifBlank { appInfo.versionName },
            title = stringResource(Res.string.ui_app_name),
            subtitle = stringResource(Res.string.ui_university),
            statusText = updateUiState.statusText?.let { stringResource(it) },
            progress = updateUiState.progress,
            downloadedMb = updateUiState.downloadedMb,
            totalMb = updateUiState.totalMb,
            onDownload = onOpenUpdates,
            canDownload = updateUiState.canOpenUpdate,
            onClick = onOpenUpdates,
        )

        // 2. Project Mission Card with link to detailed Info document
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 20.dp,
            onClick = onOpenAboutUniApp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_project_summary_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = LiquidIcons.ArrowForward,
                        contentDescription = stringResource(Res.string.ui_info_about_project),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = stringResource(Res.string.ui_project_summary_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                )
                Text(
                    text = stringResource(Res.string.ui_info_about_project),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        LiquidPreferenceGroup(title = stringResource(Res.string.ui_software_version)) {
            LiquidPreferenceItem(
                title = "${stringResource(Res.string.ui_app_name)} ${appInfo.versionName}".trim(),
                subtitle = stringResource(
                    Res.string.ui_info_installed_build,
                    appInfo.versionName,
                    appInfo.versionCode?.toString() ?: "—",
                ),
                icon = LiquidIcons.Info,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "v${appInfo.versionName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            )

            val defaultModules = listOf(
                AppModuleInfo("liquid-monet", "2.0.0", "", false),
                AppModuleInfo("uni-sdk", "2.0.0", "", false),
                AppModuleInfo("secure-storage-sdk", "2.0.0", "", false),
                AppModuleInfo("firebase-connector-sdk", "2.0.0", "", false),
            )
            val modulesToDisplay = appInfo.modules.ifEmpty { defaultModules }

            modulesToDisplay.forEach { module ->
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = when (module.name) {
                        "liquid-monet" -> "Liquid Monet"
                        "uni-sdk" -> "Uni SDK"
                        "secure-storage-sdk" -> "Secure Storage SDK"
                        "firebase-connector-sdk" -> "Firebase Connector SDK"
                        else -> module.name
                    },
                    subtitle = when (module.name) {
                        "liquid-monet" -> "Motore ottico e componenti glass"
                        "uni-sdk" -> "Servizi Esse3 e logica d'Ateneo"
                        "secure-storage-sdk" -> "Archiviazione cifrata hardware"
                        "firebase-connector-sdk" -> "Notifiche e connettore cloud"
                        else -> "Modulo interno"
                    },
                    icon = when (module.name) {
                        "liquid-monet" -> LiquidIcons.Star
                        "uni-sdk" -> LiquidIcons.MenuBook
                        "secure-storage-sdk" -> LiquidIcons.Lock
                        "firebase-connector-sdk" -> LiquidIcons.Notifications
                        else -> LiquidIcons.Settings
                    },
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "v${module.version}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }

        // 4. Developer & Credits Category (dedicated screen)
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_creator_credits)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_creator_credits),
                subtitle = stringResource(Res.string.ui_creator_credits_subtitle),
                icon = LiquidIcons.AccountCircle,
                onClick = onOpenCreatorCredits,
            )
        }

        // 5. Legal Notes Category
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_legal_notes)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_privacy),
                subtitle = stringResource(Res.string.ui_app_info_gdpr),
                icon = LiquidIcons.Lock,
                onClick = onOpenPrivacy,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_terms),
                subtitle = stringResource(Res.string.ui_app_info_academic_use),
                icon = LiquidIcons.Assignment,
                onClick = onOpenTerms,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_cookies),
                subtitle = stringResource(Res.string.ui_app_info_tech_cookies),
                icon = LiquidIcons.Settings,
                onClick = onOpenCookies,
            )
        }
    }
}
