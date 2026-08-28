package com.anto426.uniapp.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.data.UiCopy
import com.kyant.backdrop.Backdrop

@Composable
fun AppInfoScreen(
    backdropState: Backdrop,
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenCookies: () -> Unit = {},
    onOpenAuthor: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // App identity banner - Unified high-fidelity visual
        UniAppUpdateBanner(
            backdropState = backdropState,
            version = "1.0",
            title = stringResource(Res.string.ui_app_name),
            subtitle = stringResource(Res.string.ui_university),
            statusText = stringResource(Res.string.ui_updated_version)
        )

        Spacer(Modifier.height(8.dp))

        // Project Summary Section
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_project_summary_title), backdropState = backdropState) {
            Text(
                text = stringResource(Res.string.ui_project_summary_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )
        }


        // 2. Legal & System Information
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_app_info_group), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_source),
                subtitle = stringResource(Res.string.ui_source_subtitle),
                icon = LiquidIcons.Share,
                backdropState = backdropState,
                onClick = {}
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_author),
                subtitle = stringResource(Res.string.ui_author_subtitle),
                icon = LiquidIcons.AccountCircle,
                backdropState = backdropState,
                onClick = onOpenAuthor
            )
        }

        LiquidPreferenceGroup(title = stringResource(Res.string.ui_legal_notes), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_privacy),
                icon = LiquidIcons.Lock,
                backdropState = backdropState,
                onClick = onOpenPrivacy
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_terms),
                icon = LiquidIcons.Info,
                backdropState = backdropState,
                onClick = onOpenTerms
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_cookies),
                icon = LiquidIcons.Search,
                backdropState = backdropState,
                onClick = onOpenCookies
            )
        }

        Spacer(Modifier.height(24.dp))

        // Special Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidBadge(
                text = "© 2026 UNIAPP PROJECT",
                containerColor = colorScheme.primary.copy(alpha = 0.1f),
                contentColor = colorScheme.primary,
                backdropState = backdropState
            )

            Text(
                text = "Sviluppato con Liquid Monet SDK",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
