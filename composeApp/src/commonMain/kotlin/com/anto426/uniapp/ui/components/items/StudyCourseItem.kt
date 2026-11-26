package com.anto426.uniapp.ui.components.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.ui.components.display.UniInsetRow
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun StudyCourseItem(
    course: StudyCourse,
    showDivider: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
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
    ).joinToString(" • ").ifBlank { null }

    UniInsetRow(
        title = course.name,
        subtitle = metadata,
        icon = statusIcon,
        iconTint = iconTint,
        onClick = if (hasDetails) onClick else null,
        clickRole = if (hasDetails) Role.Button else null,
        showDivider = showDivider,
        showTrailingChevron = hasDetails,
        trailingContent = {
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
        },
    )
}
