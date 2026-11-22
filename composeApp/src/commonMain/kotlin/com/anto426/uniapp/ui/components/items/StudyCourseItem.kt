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
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyCourse
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun StudyCourseItem(
    course: StudyCourse,
    showDivider: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val rowShape = remember { RoundedRectangle(16.dp) }
    val hasDetails = course.id.isNotBlank()

    val statusIcon = when (course.status) {
        CourseStatus.COMPLETED -> LiquidIcons.Check
        CourseStatus.ACTIVE -> LiquidIcons.PlayArrow
        CourseStatus.PLANNED -> LiquidIcons.Calendar
    }

    val iconTint = when (course.status) {
        CourseStatus.COMPLETED -> colorScheme.primary
        CourseStatus.ACTIVE -> colorScheme.secondary
        CourseStatus.PLANNED -> colorScheme.onSurfaceVariant
    }

    val badgeText = when (course.status) {
        CourseStatus.COMPLETED -> course.grade?.takeIf { it.isNotBlank() }
            ?: stringResource(Res.string.ui_status_completed)
        CourseStatus.ACTIVE -> stringResource(Res.string.ui_status_active)
        CourseStatus.PLANNED -> stringResource(Res.string.ui_status_planned)
    }

    val metadata = listOfNotNull(
        course.cfu.takeIf { it.isNotBlank() },
        course.semester.takeIf { it.isNotBlank() },
        course.professor.takeIf { it.isNotBlank() },
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
                imageVector = statusIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 40.dp,
                    iconSize = 20.dp,
                    containerColor = iconTint.copy(alpha = 0.13f),
                    shape = RoundedRectangle(12.dp),
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = course.name,
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
                    text = badgeText,
                    containerColor = when (course.status) {
                        CourseStatus.COMPLETED -> colorScheme.primaryContainer
                        CourseStatus.ACTIVE -> colorScheme.secondaryContainer
                        CourseStatus.PLANNED -> colorScheme.surfaceVariant
                    },
                    contentColor = when (course.status) {
                        CourseStatus.COMPLETED -> colorScheme.primary
                        CourseStatus.ACTIVE -> colorScheme.onSecondaryContainer
                        CourseStatus.PLANNED -> colorScheme.onSurfaceVariant
                    },
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
