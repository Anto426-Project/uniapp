package com.anto426.uniapp.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun NewsDetailScreen(
    title: String,
    description: String,
    fullContent: String,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme
    val articleBody = fullContent.ifBlank { description }.ifBlank { title }
    val showSummary = description.isNotBlank() && description.trim() != articleBody.trim()

    UniScreenColumn {
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header with Notification Icon and Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .liquidGlass(
                                    backdrop = backdropState,
                                    shape = Capsule(),
                                    role = LiquidGlassRole.Control,
                                    containerColor = colorScheme.primary.copy(alpha = 0.12f),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Notifications,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    LiquidBadge(
                        text = stringResource(Res.string.ui_news_official_notice),
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }

                // Summary / Subtitle if distinct from full content
                if (showSummary) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                        ),
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                // Main Article Body
                Text(
                    text = articleBody,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface.copy(alpha = 0.92f),
                    lineHeight = 26.sp,
                )
            }
        }
    }
}
