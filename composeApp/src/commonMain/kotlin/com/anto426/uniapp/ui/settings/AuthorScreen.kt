package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun AuthorScreen(
    backdropState: Backdrop,
    onOpenGitHub: () -> Unit,
    onOpenProject: () -> Unit,
) {
    UniScreenColumn {
        // 1. Author Header Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 18.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LiquidAvatar(
                    initials = "A4",
                    size = 56.dp,
                    backdropState = backdropState
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.ui_author),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.ui_author_role),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Social & Links
        LiquidPreferenceGroup(title = stringResource(Res.string.ui_author_links), backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_github),
                subtitle = "@Anto426",
                icon = LiquidIcons.Share,
                backdropState = backdropState,
                onClick = onOpenGitHub
            )
            LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_author_project_name),
                subtitle = "Anto426-Project/uniapp",
                icon = LiquidIcons.Share,
                backdropState = backdropState,
                onClick = onOpenProject
            )
        }

        // 3. Location info
        LiquidPreferenceGroup(backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_author_location),
                subtitle = stringResource(Res.string.ui_university),
                icon = LiquidIcons.Home,
                backdropState = backdropState
            )
        }
    }
}
