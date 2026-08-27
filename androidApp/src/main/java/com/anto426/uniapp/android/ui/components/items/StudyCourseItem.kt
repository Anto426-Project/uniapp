package com.anto426.uniapp.android.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.models.CourseStatus
import com.anto426.uniapp.android.ui.models.StudyCourse
import com.kyant.backdrop.Backdrop

@Composable
fun StudyCourseItem(
    course: StudyCourse,
    backdropState: Backdrop,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = when(course.status) {
        CourseStatus.COMPLETED -> colorScheme.primary
        CourseStatus.ACTIVE -> Color(0xFFFFB74D) // Amber
        CourseStatus.PLANNED -> colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    val statusIcon = when(course.status) {
        CourseStatus.COMPLETED -> LiquidIcons.Check
        CourseStatus.ACTIVE -> LiquidIcons.PlayArrow
        CourseStatus.PLANNED -> LiquidIcons.Calendar
    }

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp,
        onClick = onClick,
        interactiveGelatin = true
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = course.name,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = course.cfu,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            if (course.status == CourseStatus.ACTIVE) {
                LiquidBadge(text = "In Corso", backdropState = backdropState)
            }
        }
    }
}
