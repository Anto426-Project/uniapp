package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.banners.UniAppUpdateBanner
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AppInfoScreen(
    backdropState: Backdrop,
    installedVersion: String,
    onOpenSource: () -> Unit,
    onOpenAboutUniApp: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenCookies: () -> Unit = {},
    onOpenAuthor: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. App Identity Banner
        UniAppUpdateBanner(
            backdropState = backdropState,
            version = installedVersion.ifBlank { "—" },
            title = stringResource(Res.string.ui_app_name),
            subtitle = stringResource(Res.string.ui_university),
            statusText = stringResource(Res.string.ui_updated_version),
        )

        Spacer(Modifier.height(8.dp))

        // 2. Clickable Project Summary Hero Card (Navigates to Full Info)
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 18.dp,
            onClick = onOpenAboutUniApp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Info,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 44.dp,
                        iconSize = 22.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                    ),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_app_info_about_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_app_info_about_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp,
                    )
                }

                Icon(
                    imageVector = LiquidIcons.ArrowForward,
                    contentDescription = stringResource(Res.string.ui_read_details),
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 3. Source Code & Author Section
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_app_info_group),
            backdropState = backdropState,
        ) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_source),
                subtitle = stringResource(Res.string.ui_source_subtitle),
                icon = LiquidIcons.Share,
                backdropState = backdropState,
                onClick = onOpenSource,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_author),
                subtitle = stringResource(Res.string.ui_author_subtitle),
                icon = LiquidIcons.AccountCircle,
                backdropState = backdropState,
                onClick = onOpenAuthor,
            )
        }

        // 4. Policy e Note Legali
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_legal_notes),
            backdropState = backdropState,
        ) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_privacy),
                subtitle = stringResource(Res.string.ui_app_info_gdpr),
                icon = LiquidIcons.Lock,
                backdropState = backdropState,
                onClick = onOpenPrivacy,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_terms),
                subtitle = stringResource(Res.string.ui_app_info_academic_use),
                icon = LiquidIcons.Info,
                backdropState = backdropState,
                onClick = onOpenTerms,
            )
        }

        Spacer(Modifier.height(20.dp))

        // 5. Ecosystem Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidBadge(
                text = stringResource(Res.string.ui_app_credit),
                containerColor = colorScheme.primary.copy(alpha = 0.12f),
                contentColor = colorScheme.primary,
                backdropState = backdropState,
            )

            Text(
                text = stringResource(Res.string.ui_app_info_for_students),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )
        }
    }
}
