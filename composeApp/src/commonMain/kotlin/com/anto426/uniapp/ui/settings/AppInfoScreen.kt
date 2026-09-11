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
import com.anto426.uniapp.ui.components.banners.UniAppInfoBanner
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import com.anto426.unisdk.platform.AppInfoProvider
import com.anto426.unisdk.platform.AppModuleInfo
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AppInfoScreen(
    updateUiState: AppUpdateUiState? = null,
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
    val isUpdateAvailable = updateUiState?.isUpdateAvailable == true

    UniScreenColumn {
        // 1. Hero Brand Card (360dp con flip 3D)
        UniAppInfoBanner(
            modifier = Modifier.fillMaxWidth(),
            height = 360.dp,
            version = appInfo.versionName,
            title = stringResource(Res.string.ui_app_name),
            subtitle = stringResource(Res.string.ui_university),
            buildInfo = stringResource(
                Res.string.ui_info_installed_build,
                appInfo.versionName,
                appInfo.versionCode?.toString() ?: "—",
            ),
            onClick = onOpenUpdates,
        )

        // 2. Applicazione & Aggiornamenti
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_info_application)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_updates),
                subtitle = if (isUpdateAvailable) {
                    stringResource(Res.string.msg_e_disponibile_un_nuovo_aggiornamento)
                } else {
                    stringResource(Res.string.msg_lapp_e_aggiornata)
                },
                icon = LiquidIcons.Refresh,
                trailingContent = if (isUpdateAvailable) {
                    {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = "Nuovo",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                } else null,
                onClick = onOpenUpdates,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.nav_route_changelog_title),
                subtitle = stringResource(Res.string.ui_changelog_subtitle),
                icon = LiquidIcons.Star,
                onClick = onOpenChangelog,
            )
        }

        // 3. Progetto & Sviluppo
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_app_info_group)) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_info_about_project),
                subtitle = stringResource(Res.string.ui_info_independent),
                icon = LiquidIcons.Info,
                onClick = onOpenAboutUniApp,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_creator_credits),
                subtitle = stringResource(Res.string.ui_creator_credits_subtitle),
                icon = LiquidIcons.AccountCircle,
                onClick = onOpenCreatorCredits,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_source),
                subtitle = stringResource(Res.string.ui_source_subtitle),
                icon = LiquidIcons.Share,
                onClick = onOpenSource,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_report_bug_github),
                subtitle = stringResource(Res.string.ui_info_public_issue),
                icon = LiquidIcons.Warning,
                onClick = onReportBug,
            )
        }

        // 4. Architettura & Componenti
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_info_components)) {
            val defaultModules = listOf(
                AppModuleInfo("liquid-monet", "2.0.0", "", false),
                AppModuleInfo("uni-sdk", "2.0.0", "", false),
                AppModuleInfo("secure-storage-sdk", "2.0.0", "", false),
                AppModuleInfo("firebase-connector-sdk", "2.0.0", "", false),
            )
            val modulesToDisplay = appInfo.modules.ifEmpty { defaultModules }

            modulesToDisplay.forEachIndexed { index, module ->
                if (index > 0) {
                    LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                }
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
                                .padding(horizontal = 8.dp, vertical = 3.dp),
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

        // 5. Note Legali e Policy
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
