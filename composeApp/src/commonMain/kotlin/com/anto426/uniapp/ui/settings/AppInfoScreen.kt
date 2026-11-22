package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.ui.components.banners.UniAppInfoBanner
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.UniAppModuleBuildInfo
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AppInfoScreen(
    updateUiState: AppUpdateUiState? = null,
    installedBuild: InstalledAppBuild = InstalledAppBuild(),
    onOpenAboutUniApp: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenCookies: () -> Unit,
    onOpenCreatorCredits: () -> Unit,
    onOpenUpdates: () -> Unit,
    onReportBug: () -> Unit = {},
    onOpenSource: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val appInfo = installedBuild
    val isUpdateAvailable = updateUiState?.isUpdateAvailable == true
    val rawVersion = appInfo.versionName.takeUnless { it == "unknown" || it.isBlank() }
        ?: updateUiState?.installedVersion.orEmpty().ifBlank { "2.0.0" }
    val displayVersion = rawVersion.substringBefore('-').substringBefore('+')
    val bannerStatusBadge = when {
        isUpdateAvailable -> stringResource(
            if (updateUiState.isMandatory) Res.string.ui_update_mandatory
            else Res.string.ui_update_news,
        )
        updateUiState?.bannerState == UpdateState.RESTART_REQUIRED -> stringResource(Res.string.ui_update_ready)
        updateUiState != null -> stringResource(Res.string.ui_update_up_to_date_badge)
        else -> null
    }

    UniScreenColumn {
        // 1. Hero Brand Card (310dp con flip 3D)
        UniAnimatedEntrance(order = 0) {
            UniAppInfoBanner(
                modifier = Modifier.fillMaxWidth(),
                height = 310.dp,
                version = displayVersion,
                title = stringResource(Res.string.ui_app_name),
                subtitle = stringResource(Res.string.ui_university),
                buildInfo = stringResource(
                    Res.string.ui_info_installed_build,
                    appInfo.versionName,
                    appInfo.versionCode?.toString() ?: "—",
                ),
                statusBadge = bannerStatusBadge,
                statusIsHighlight = isUpdateAvailable,
                statusIsMandatory = updateUiState?.isMandatory == true,
                onClick = onOpenUpdates,
            )
        }

        // 2. Progetto & Sviluppo
        UniInsetGroup(
            title = stringResource(Res.string.ui_app_info_group),
            entranceOrder = 1,
        ) {
            UniInsetRow(
                title = stringResource(Res.string.ui_info_about_project),
                subtitle = stringResource(Res.string.ui_info_independent),
                icon = LiquidIcons.Info,
                iconTint = colorScheme.primary,
                onClick = onOpenAboutUniApp,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_creator_credits),
                subtitle = stringResource(Res.string.ui_creator_credits_subtitle),
                icon = LiquidIcons.AccountCircle,
                iconTint = colorScheme.tertiary,
                onClick = onOpenCreatorCredits,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_source),
                subtitle = stringResource(Res.string.ui_source_subtitle),
                icon = LiquidIcons.Share,
                iconTint = colorScheme.secondary,
                onClick = onOpenSource,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_report_bug_github),
                subtitle = stringResource(Res.string.ui_info_public_issue),
                icon = LiquidIcons.Feedback,
                iconTint = glassColors.warning,
                onClick = onReportBug,
                showDivider = false,
            )
        }

        // 3. Architettura & Componenti
        UniInsetGroup(
            title = stringResource(Res.string.ui_info_components),
            entranceOrder = 2,
        ) {
            val defaultModules = listOf(
                UniAppModuleBuildInfo("liquid-monet", "2.0.0", "", false),
                UniAppModuleBuildInfo("uni-sdk", "2.0.0", "", false),
                UniAppModuleBuildInfo("secure-storage-sdk", "2.0.0", "", false),
                UniAppModuleBuildInfo("firebase-connector-sdk", "2.0.0", "", false),
            )
            val modulesToDisplay = appInfo.modules.ifEmpty { defaultModules }

            modulesToDisplay.forEachIndexed { index, module ->
                val accent = when (module.name) {
                    "liquid-monet" -> colorScheme.primary
                    "uni-sdk" -> colorScheme.tertiary
                    "secure-storage-sdk" -> glassColors.success
                    "firebase-connector-sdk" -> glassColors.warning
                    else -> colorScheme.secondary
                }
                UniInsetRow(
                    title = when (module.name) {
                        "liquid-monet" -> "Liquid Monet"
                        "uni-sdk" -> "Uni SDK"
                        "secure-storage-sdk" -> "Secure Storage SDK"
                        "firebase-connector-sdk" -> "Firebase Connector SDK"
                        else -> module.name
                    },
                    subtitle = when (module.name) {
                        "liquid-monet" -> stringResource(Res.string.ui_sdk_liquid_description)
                        "uni-sdk" -> stringResource(Res.string.ui_sdk_uni_description)
                        "secure-storage-sdk" -> stringResource(Res.string.ui_sdk_storage_description)
                        "firebase-connector-sdk" -> stringResource(Res.string.ui_sdk_firebase_description)
                        else -> stringResource(Res.string.ui_sdk_internal_description)
                    },
                    icon = when (module.name) {
                        "liquid-monet" -> LiquidIcons.Star
                        "uni-sdk" -> LiquidIcons.MenuBook
                        "secure-storage-sdk" -> LiquidIcons.Lock
                        "firebase-connector-sdk" -> LiquidIcons.Notifications
                        else -> LiquidIcons.Settings
                    },
                    iconTint = accent,
                    showDivider = index < modulesToDisplay.lastIndex,
                    trailingContent = {
                        LiquidBadge(
                            text = "v${module.version}",
                            containerColor = accent.copy(alpha = 0.14f),
                            contentColor = accent,
                        )
                    },
                )
            }
        }

        // 4. Note Legali e Policy
        UniInsetGroup(
            title = stringResource(Res.string.ui_legal_notes),
            entranceOrder = 3,
        ) {
            UniInsetRow(
                title = stringResource(Res.string.ui_privacy),
                subtitle = stringResource(Res.string.ui_app_info_gdpr),
                icon = LiquidIcons.Lock,
                iconTint = colorScheme.primary,
                onClick = onOpenPrivacy,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_terms),
                subtitle = stringResource(Res.string.ui_app_info_academic_use),
                icon = LiquidIcons.Assignment,
                iconTint = colorScheme.tertiary,
                onClick = onOpenTerms,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_cookies),
                subtitle = stringResource(Res.string.ui_app_info_tech_cookies),
                icon = LiquidIcons.Settings,
                iconTint = colorScheme.secondary,
                onClick = onOpenCookies,
                showDivider = false,
            )
        }
    }
}
