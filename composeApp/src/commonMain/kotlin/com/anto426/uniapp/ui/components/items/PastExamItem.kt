package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.accordion.LiquidAccordionItem
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.didactics.PastExam
import com.anto426.uniapp.model.didactics.PastExamStatus
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PastExamItem(exam: PastExam) {
    var isExpanded by rememberSaveable("${exam.id}|${exam.date}|${exam.name}") { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val isVerbalized = exam.status == PastExamStatus.VERBALIZED

    val statusColor = if (isVerbalized) glassColors.success else colorScheme.onSurfaceVariant
    val statusLabel = when (exam.status) {
        PastExamStatus.VERBALIZED -> stringResource(Res.string.ui_exam_status_verbalized)
        PastExamStatus.BOOKED_PAST -> stringResource(Res.string.ui_exam_status_booked_past)
    }

    val gradeLabel = stringResource(Res.string.ui_exam_grade)
    val subtitle = buildList {
        exam.date.takeIf(String::isNotBlank)?.let(::add)
        exam.time.takeIf(String::isNotBlank)?.let(::add)
        if (exam.grade.isNotBlank()) {
            add("$gradeLabel: ${exam.grade}")
        } else {
            add(statusLabel)
        }
        exam.cfu?.let { add("$it CFU") }
    }.joinToString(" • ")

    LiquidAccordionItem(
        title = exam.name,
        subtitle = subtitle,
        leadingIcon = if (isVerbalized) LiquidIcons.Check else LiquidIcons.Calendar,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LiquidBadge(
                    text = statusLabel,
                    containerColor = statusColor.copy(alpha = 0.14f),
                    contentColor = statusColor,
                )
                exam.cfu?.let { cfu ->
                    LiquidBadge(
                        text = "$cfu CFU",
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                }
                if (exam.grade.isNotBlank()) {
                    LiquidBadge(
                        text = "$gradeLabel ${exam.grade}",
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                }
            }

            if (!exam.professor.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LiquidAvatar(
                        initials = exam.professor
                            .split(" ")
                            .filter(String::isNotEmpty)
                            .map { it.first() }
                            .joinToString("")
                            .take(2),
                        size = 34.dp,
                    )
                    Column {
                        Text(
                            text = exam.professor,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        exam.code?.takeIf(String::isNotBlank)?.let { code ->
                            Text(
                                text = code,
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (exam.professor.isNullOrBlank()) {
                    exam.code?.takeIf(String::isNotBlank)?.let { code ->
                        PastExamDetailRow(
                            value = "${stringResource(Res.string.ui_exam_code)}: $code",
                            icon = LiquidIcons.Assignment,
                        )
                    }
                }

                exam.type?.takeIf(String::isNotBlank)?.let { type ->
                    PastExamDetailRow(
                        value = "${stringResource(Res.string.ui_exam_type)}: $type",
                        icon = LiquidIcons.Edit,
                    )
                }

                exam.room?.takeIf(String::isNotBlank)?.let { room ->
                    PastExamDetailRow(value = room, icon = LiquidIcons.Home)
                }

                val bookingWindow = listOfNotNull(
                    exam.bookingOpenDate?.takeIf(String::isNotBlank),
                    exam.bookingCloseDate?.takeIf(String::isNotBlank),
                ).joinToString(" – ")
                if (bookingWindow.isNotBlank()) {
                    PastExamDetailRow(
                        value = "${stringResource(Res.string.ui_booking_window)}: $bookingWindow",
                        icon = LiquidIcons.Time,
                    )
                }

                exam.bookedUsersCount?.let { count ->
                    val slotSuffix = exam.availableSlots?.let { slots ->
                        " • ${stringResource(Res.string.ui_available_slots)}: $slots"
                    }.orEmpty()
                    PastExamDetailRow(
                        value = "${stringResource(Res.string.ui_booked_students)} $count$slotSuffix",
                        icon = LiquidIcons.AccountCircle,
                    )
                }

                exam.notes?.takeIf(String::isNotBlank)?.let { notes ->
                    PastExamDetailRow(notes, LiquidIcons.Info)
                }
            }
        }
    }
}

@Composable
private fun PastExamDetailRow(value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
