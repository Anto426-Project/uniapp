package com.anto426.uniapp.ui.screens

import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner

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
    currentChannel: String,
    onOpenChangelog: () -> Unit
) {
    UniScreenColumn {
        // 1. New High-Fidelity App Update Banner
        UniAppUpdateBanner(
            backdropState = backdropState,
            version = "1.0",
            title = stringResource(Res.string.ui_app_name),
            subtitle = stringResource(Res.string.ui_university),
            statusText = stringResource(Res.string.ui_updated_version),
            channel = currentChannel
        )

        Spacer(Modifier.height(8.dp))

        // 2. Software Version Group (Settings Style)
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_software_version), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_app_version_full),
                subtitle = stringResource(Res.string.ui_system_updated),
                icon = LiquidIcons.Info,
                backdropState = backdropState
            )

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
