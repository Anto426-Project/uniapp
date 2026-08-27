package com.anto426.uniapp.android.ui.screens

import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.components.banners.UniAppUpdateBanner

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
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
            title = stringResource(R.string.ui_app_name),
            subtitle = stringResource(R.string.ui_university),
            statusText = stringResource(R.string.ui_updated_version),
            channel = currentChannel
        )

        Spacer(Modifier.height(8.dp))

        // 2. Software Version Group (Settings Style)
        LiquidPreferenceGroup(title = stringResource(R.string.ui_software_version), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_app_version_full),
                subtitle = stringResource(R.string.ui_system_updated),
                icon = LiquidIcons.Info,
                backdropState = backdropState
            )

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            LiquidPreferenceItem(
                title = stringResource(R.string.ui_changelog),
                subtitle = stringResource(R.string.ui_changelog_subtitle),
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
