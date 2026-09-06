package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.unisdk.backend.model.ProfessorContentItem

@Composable
fun AcademicContentCard(
    item: ProfessorContentItem,
    onClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(20.dp),
        contentPadding = 16.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LiquidIcons.Assignment,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 42.dp,
                        iconSize = 20.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedRectangle(12.dp),
                    ),
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        item.code?.takeIf(String::isNotBlank)?.let { code ->
                            LiquidBadge(
                                text = code,
                                containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = colorScheme.primary,
                            )
                        }
                    }

                    item.subtitle?.takeIf(String::isNotBlank)?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }

                    item.date?.takeIf(String::isNotBlank)?.let { date ->
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
