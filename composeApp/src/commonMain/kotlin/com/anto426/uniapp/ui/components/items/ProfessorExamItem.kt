package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kyant.backdrop.Backdrop

@Composable
fun ProfessorExamItem(
    exam: ProfessorContentItem,
    backdropState: Backdrop,
    onClickDetail: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    val bookingsCount = exam.bookings.size.takeIf { it > 0 }
        ?: exam.detail?.let { d ->
            val match = Regex("""(\d+)\s*(?:prenotat|iscritt)""", RegexOption.IGNORE_CASE).find(d)
            match?.groupValues?.get(1)?.toIntOrNull()
        }

    val bookingsText = bookingsCount?.let { "$it iscritti" }

    val subtitle = buildList {
        exam.date?.takeIf(String::isNotBlank)?.let(::add)
        bookingsText?.let(::add)
        if (isEmpty()) {
            exam.subtitle?.takeIf(String::isNotBlank)?.let(::add)
        }
    }.joinToString(" • ").ifBlank { "Sessione d'appello" }

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        onClick = onClickDetail,
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
                    imageVector = LiquidIcons.Calendar,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 42.dp,
                        iconSize = 20.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = exam.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        exam.code?.takeIf(String::isNotBlank)?.let { code ->
                            LiquidBadge(
                                text = code,
                                containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = colorScheme.primary,
                                backdropState = backdropState,
                            )
                        }
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
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
