package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.accordion.LiquidExpandableCard
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.ExamSession
import com.anto426.uniapp.ui.components.display.UniDetailMetaRow
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamSessionItem(
    exam: ExamSession,
    isMutating: Boolean = false,
    onToggleBooking: () -> Unit = {},
    onAddToCalendar: () -> Unit,
) {
    val expandKey = "${exam.id}|${exam.date}|${exam.time}|${exam.isBooked}"
    var isExpanded by rememberSaveable(expandKey) { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val statusColor = when {
        exam.isBooked -> glassColors.success
        exam.canBook -> colorScheme.primary
        exam.isBookingFuture -> glassColors.warning
        else -> colorScheme.onSurfaceVariant
    }

    val statusLabel = when {
        exam.isBooked -> stringResource(Res.string.ui_exams_status_booked)
        exam.canBook -> stringResource(Res.string.ui_exams_status_open)
        exam.isBookingFuture -> stringResource(Res.string.ui_exams_status_upcoming)
        else -> stringResource(Res.string.ui_exams_status_closed)
    }

    val leadingIcon = when {
        exam.isBooked -> LiquidIcons.Check
        exam.canBook -> LiquidIcons.Calendar
        exam.isBookingFuture -> LiquidIcons.Time
        else -> LiquidIcons.Lock
    }

    val scheduleText = buildList {
        exam.date.takeIf(String::isNotBlank)?.let(::add)
        exam.time.takeIf(String::isNotBlank)?.let(::add)
    }.joinToString(" • ")

    val accordionSubtitle = buildList {
        if (scheduleText.isNotBlank()) add(scheduleText)
        add(statusLabel)
        exam.room.takeIf(String::isNotBlank)?.let(::add)
    }.joinToString(" • ")

    LiquidExpandableCard(
        title = exam.name,
        subtitle = accordionSubtitle,
        leadingIcon = leadingIcon,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = Modifier.fillMaxWidth(),
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
                exam.type.takeIf(String::isNotBlank)?.let { examType ->
                    LiquidBadge(
                        text = examType,
                        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (exam.professor.isNotBlank()) {
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
                    Text(
                        text = exam.professor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (exam.room.isNotBlank()) {
                    UniDetailMetaRow(value = exam.room, icon = LiquidIcons.Home)
                }

                val bookingWindow = listOf(exam.bookingOpenDate, exam.bookingCloseDate)
                    .filter(String::isNotBlank)
                    .joinToString(" – ")
                if (bookingWindow.isNotBlank()) {
                    UniDetailMetaRow(
                        value = "${stringResource(Res.string.ui_booking_window)}: $bookingWindow",
                        icon = LiquidIcons.Time,
                    )
                }

                val slotsSummary = if (exam.availableSlots > 0) {
                    stringResource(
                        Res.string.ui_exams_booked_slots_summary,
                        exam.bookedUsersCount,
                        exam.availableSlots,
                    )
                } else {
                    stringResource(Res.string.ui_exams_booked_count_only, exam.bookedUsersCount)
                }
                UniDetailMetaRow(value = slotsSummary, icon = LiquidIcons.AccountCircle)

                if (exam.notes.isNotBlank()) {
                    UniDetailMetaRow(value = exam.notes, icon = LiquidIcons.Info)
                }
            }

            if (exam.isBooked) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidButton(
                        text = stringResource(Res.string.ui_add_calendar),
                        onClick = onAddToCalendar,
                        modifier = Modifier.fillMaxWidth(),
                        variant = LiquidButtonVariant.Tonal,
                        size = LiquidButtonSize.Medium,
                    )
                    LiquidButton(
                        text = stringResource(Res.string.ui_cancel_booking),
                        onClick = onToggleBooking,
                        enabled = !isMutating,
                        modifier = Modifier.fillMaxWidth(),
                        variant = LiquidButtonVariant.Secondary,
                        size = LiquidButtonSize.Medium,
                    )
                }
            } else {
                LiquidButton(
                    text = when {
                        exam.canBook -> stringResource(Res.string.ui_book_exam)
                        exam.isBookingFuture -> stringResource(Res.string.ui_exam_booking_not_open)
                        else -> stringResource(Res.string.ui_booking_closed)
                    },
                    onClick = onToggleBooking,
                    enabled = exam.canBook && !isMutating,
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Medium,
                )
            }
        }
    }
}
