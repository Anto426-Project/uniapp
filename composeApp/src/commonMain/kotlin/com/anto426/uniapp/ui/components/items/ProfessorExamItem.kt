package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.unisdk.model.ProfessorContentItem
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfessorExamItem(
    exam: ProfessorContentItem,
    onClickDetail: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    val bookingsCount = exam.bookings.size.takeIf { it > 0 }
        ?: exam.detail?.let { d ->
            val match = Regex("""(\d+)\s*(?:prenotat|iscritt)""", RegexOption.IGNORE_CASE).find(d)
            match?.groupValues?.get(1)?.toIntOrNull()
        }

    val subtitle = listOfNotNull(
        exam.date?.takeIf(String::isNotBlank),
        exam.subtitle?.takeIf(String::isNotBlank)?.takeIf { sub -> !sub.equals(exam.date, ignoreCase = true) },
    ).joinToString(" • ").ifBlank {
        stringResource(Res.string.ui_professor_exam_session_fallback)
    }

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(22.dp),
        contentPadding = PaddingValues(16.dp),
        onClick = onClickDetail,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = LiquidIcons.Calendar,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 42.dp,
                    iconSize = 20.dp,
                    containerColor = colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedRectangle(12.dp),
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!exam.code.isNullOrBlank() || bookingsCount != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        exam.code?.takeIf(String::isNotBlank)?.let { code ->
                            LiquidBadge(
                                text = code,
                                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                contentColor = colorScheme.primary,
                            )
                        }
                        bookingsCount?.let { count ->
                            LiquidBadge(
                                text = stringResource(Res.string.ui_professor_registered_count, count),
                                containerColor = colorScheme.onSurface.copy(alpha = 0.06f),
                                contentColor = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
