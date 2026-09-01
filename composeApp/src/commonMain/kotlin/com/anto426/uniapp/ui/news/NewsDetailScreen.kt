package com.anto426.uniapp.ui.news

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule

@Composable
fun NewsDetailScreen(
    title: String,
    description: String,
    fullContent: String,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Header Card with Notification Badge & Title
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
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

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface,
                    )
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlassTheme.colors.secondaryContent,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // 2. Full Article Content Card
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_news_notice_body),
            backdropState = backdropState,
        ) {
            Text(
                text = fullContent.ifBlank { description },
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(18.dp),
                lineHeight = 26.sp,
            )
        }
    }
}
