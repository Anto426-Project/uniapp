package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.ExamRecord
import com.kyant.shapes.RoundedRectangle

@Composable
fun ExamRecordItem(
    exam: ExamRecord,
    showDivider: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val rowShape = remember { RoundedRectangle(16.dp) }
    val numericGrade = exam.grade.trim().toIntOrNull()
    val hasDetails = exam.code.isNotBlank()

    val accentColor = when {
        exam.lode -> colorScheme.tertiary
        numericGrade != null && numericGrade >= 28 -> colorScheme.primary
        else -> glassColors.success
    }
    val badgeBg = when {
        exam.lode -> colorScheme.tertiaryContainer
        numericGrade != null && numericGrade >= 28 -> colorScheme.primaryContainer
        else -> glassColors.success.copy(alpha = 0.16f)
    }

    val metadata = listOfNotNull(
        exam.cfu.takeIf { it.isNotBlank() },
        exam.date.takeIf { it.isNotBlank() && it != "-" },
        exam.code.takeIf { it.isNotBlank() },
    ).joinToString(" • ")

    Column(modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .liquidClickable(
                    shape = rowShape,
                    role = Role.Button,
                    onClick = if (hasDetails) onClick else null,
                )
                .semantics(mergeDescendants = true) {}
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = if (exam.lode) LiquidIcons.Star else LiquidIcons.Check,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 40.dp,
                    iconSize = 20.dp,
                    containerColor = accentColor.copy(alpha = 0.14f),
                    shape = RoundedRectangle(12.dp),
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = exam.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (metadata.isNotBlank()) {
                    Text(
                        text = metadata,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LiquidBadge(
                    text = exam.grade,
                    containerColor = badgeBg,
                    contentColor = accentColor,
                )
                if (hasDetails) {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        if (showDivider) {
            LiquidHorizontalDivider(
                modifier = Modifier.padding(start = 70.dp, end = 16.dp),
                color = colorScheme.outlineVariant.copy(alpha = 0.25f),
            )
        }
    }
}
