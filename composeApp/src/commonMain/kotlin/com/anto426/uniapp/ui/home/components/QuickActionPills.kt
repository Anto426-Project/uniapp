package com.anto426.uniapp.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.uniapp.ui.home.QuickActionItem
import com.anto426.uniapp.ui.theme.UniColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionPills(
    actions: List<QuickActionItem>,
    modifier: Modifier = Modifier,
    onActionClick: (QuickActionItem) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header with Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(UniColors.PrimaryViolet.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚡", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Accesso rapido",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = UniColors.TextPrimary
            )
        }

        // Actions Grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            actions.forEach { action ->
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0x28231535),
                    borderBrush = UniColors.GlassBorderGradient,
                    contentPadding = 14.dp,
                    onClick = { onActionClick(action) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(UniColors.PrimaryPurple.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = action.iconKey,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = action.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = UniColors.TextPrimary,
                                maxLines = 1
                            )

                            if (action.subtitle != null) {
                                Text(
                                    text = action.subtitle,
                                    fontSize = 11.sp,
                                    color = UniColors.TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
