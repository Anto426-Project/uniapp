package com.anto426.uniapp.ui.components.items

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.StudyCourse
import com.kyant.backdrop.Backdrop

@Composable
fun StudyCourseItem(
    course: StudyCourse,
    backdropState: Backdrop,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    val statusIcon = when (course.status) {
        CourseStatus.COMPLETED -> LiquidIcons.Check
        CourseStatus.ACTIVE -> LiquidIcons.PlayArrow
        CourseStatus.PLANNED -> LiquidIcons.Calendar
    }

    val iconContainerColor = when (course.status) {
        CourseStatus.COMPLETED -> colorScheme.primary.copy(alpha = 0.12f)
        CourseStatus.ACTIVE -> colorScheme.secondary.copy(alpha = 0.12f)
        CourseStatus.PLANNED -> colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val iconTint = when (course.status) {
        CourseStatus.COMPLETED -> colorScheme.primary
        CourseStatus.ACTIVE -> colorScheme.secondary
        CourseStatus.PLANNED -> colorScheme.onSurfaceVariant
    }

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 40.dp,
                        iconSize = 20.dp,
                        containerColor = iconContainerColor,
                        shape = RoundedCornerShape(12.dp),
                    ),
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = course.name,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = buildString {
                            if (course.semester.isNotBlank()) append("${course.semester} • ")
                            append(course.cfu)
                            if (course.professor.isNotBlank()) append(" • ${course.professor}")
                        },
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(contentAlignment = Alignment.Center) {
                LiquidBadge(
                    text = when (course.status) {
                        CourseStatus.COMPLETED -> stringResource(Res.string.ui_status_completed)
                        CourseStatus.ACTIVE -> stringResource(Res.string.ui_status_active)
                        CourseStatus.PLANNED -> stringResource(Res.string.ui_status_planned)
                    },
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
                    backdropState = backdropState,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 1.02f
                        scaleY = 1.02f
                    }
                )
            }
        }
    }
}
