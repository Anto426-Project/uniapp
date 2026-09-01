package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidAccordionItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.PastExam
import com.anto426.uniapp.model.didactics.PastExamStatus
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun PastExamItem(exam: PastExam, backdropState: Backdrop) {
    var isExpanded by rememberSaveable(exam.id) { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val isVerbalized = exam.status == PastExamStatus.VERBALIZED

    val statusLabel =
        when (exam.status) {
            PastExamStatus.VERBALIZED -> stringResource(Res.string.ui_exam_status_verbalized)
            PastExamStatus.BOOKED_PAST -> stringResource(Res.string.ui_exam_status_booked_past)
        }
    val gradeLabel = stringResource(Res.string.ui_exam_grade)
    val subtitle =
        buildList {
            add(exam.date)
            exam.time.takeIf(String::isNotBlank)?.let(::add)
            exam.grade.takeIf(String::isNotBlank)?.let {
                add("$gradeLabel: $it")
            }
            if (exam.grade.isBlank()) add(statusLabel)
        }.joinToString(" • ")

    LiquidAccordionItem(
        title = exam.name,
        subtitle = subtitle,
        leadingIcon = if (isVerbalized) LiquidIcons.Check else LiquidIcons.Calendar,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        backdropState = backdropState
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!exam.professor.isNullOrBlank() || exam.grade.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!exam.professor.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LiquidAvatar(
                                initials = exam.professor.split(" ").filter(String::isNotEmpty).map { it.first() }.joinToString("").take(2),
                                size = 36.dp,
                                backdropState = backdropState
                            )
                            Column {
                                Text(
                                    text = exam.professor,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                exam.code?.takeIf(String::isNotBlank)?.let { code ->
                                    Text(
                                        text = code,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (exam.grade.isNotBlank()) {
                        LiquidBadge(
                            text = "${stringResource(Res.string.ui_exam_grade)} ${exam.grade}",
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.55f),
                            contentColor = colorScheme.primary,
                            backdropState = backdropState
                        )
                    }
                }
            }

            // Details Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (exam.cfu != null || isVerbalized) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        exam.cfu?.let { cfu ->
                            Box(modifier = Modifier.weight(1f)) {
                                PastExamDetailRow("$cfu CFU", LiquidIcons.MenuBook)
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PastExamDetailRow(statusLabel, if (isVerbalized) LiquidIcons.Check else LiquidIcons.Time)
                        }
                    }
                }

                if (!exam.type.isNullOrBlank() || !exam.room.isNullOrBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        exam.type?.takeIf(String::isNotBlank)?.let { type ->
                            Box(modifier = Modifier.weight(1f)) {
                                PastExamDetailRow(type, LiquidIcons.Assignment)
                            }
                        }
                        exam.room?.takeIf(String::isNotBlank)?.let { room ->
                            Box(modifier = Modifier.weight(1f)) {
                                PastExamDetailRow(room, LiquidIcons.Home)
                            }
                        }
                    }
                }

                val bookingWindow =
                    listOfNotNull(
                        exam.bookingOpenDate?.takeIf(String::isNotBlank),
                        exam.bookingCloseDate?.takeIf(String::isNotBlank),
                    ).joinToString(" – ")
                if (bookingWindow.isNotBlank()) {
                    PastExamDetailRow(bookingWindow, LiquidIcons.Calendar)
                }

                exam.bookedUsersCount?.let { count ->
                    PastExamDetailRow("${stringResource(Res.string.ui_booked_students)} $count", LiquidIcons.AccountCircle)
                }

                exam.availableSlots?.let { slots ->
                    PastExamDetailRow("${stringResource(Res.string.ui_available_slots)}: $slots", LiquidIcons.AccountCircle)
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
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
