package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
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
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidAccordionItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.ExamSession
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ExamSessionItem(
    exam: ExamSession,
    backdropState: Backdrop,
    isMutating: Boolean = false,
    onToggleBooking: () -> Unit = {},
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    LiquidAccordionItem(
        title = exam.name,
        subtitle = "${exam.date} • ${exam.time}",
        leadingIcon = if (exam.isBooked) LiquidIcons.Check else LiquidIcons.Calendar,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        backdropState = backdropState
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (exam.professor.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidAvatar(
                        initials = exam.professor.split(" ").filter(String::isNotEmpty).map { it.first() }.joinToString("").take(2),
                        size = 36.dp,
                        backdropState = backdropState
                    )
                    Text(
                        text = exam.professor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Details Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (exam.type.isNotBlank() || exam.room.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (exam.type.isNotBlank()) {
                            Box(modifier = Modifier.weight(1f)) {
                                ExamDetailRow(exam.type, LiquidIcons.Edit)
                            }
                        }
                        if (exam.room.isNotBlank()) {
                            Box(modifier = Modifier.weight(1f)) {
                                ExamDetailRow(exam.room, LiquidIcons.Info)
                            }
                        }
                    }
                }

                val bookingWindow =
                    listOf(exam.bookingOpenDate, exam.bookingCloseDate)
                        .filter(String::isNotBlank)
                        .joinToString(" – ")
                if (bookingWindow.isNotBlank()) {
                    ExamDetailRow(bookingWindow, LiquidIcons.Time)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = LiquidIcons.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${stringResource(Res.string.ui_booked_students)} ${exam.bookedUsersCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ExamDetailRow(
                    "${stringResource(Res.string.ui_available_slots)}: ${exam.availableSlots}",
                    LiquidIcons.AccountCircle,
                )
                if (exam.notes.isNotBlank()) {
                    ExamDetailRow(exam.notes, LiquidIcons.Info)
                }
            }

            if (exam.isBooked) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidButton(
                        text = stringResource(Res.string.ui_add_calendar),
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        variant = LiquidButtonVariant.Tonal,
                        size = LiquidButtonSize.Small,
                        backdropState = backdropState
                    )
                    LiquidButton(
                        text = stringResource(Res.string.ui_cancel_booking),
                        onClick = onToggleBooking,
                        enabled = !isMutating,
                        modifier = Modifier.fillMaxWidth(),
                        variant = LiquidButtonVariant.Secondary,
                        size = LiquidButtonSize.Small,
                        backdropState = backdropState
                    )
                }
            } else {
                LiquidButton(
                    text =
                        if (exam.canBook) stringResource(Res.string.ui_book_exam)
                        else stringResource(Res.string.ui_booking_closed),
                    onClick = onToggleBooking,
                    enabled = exam.canBook && !isMutating,
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Small,
                    backdropState = backdropState
                )
            }
        }
    }
}

@Composable
private fun ExamDetailRow(value: String, icon: ImageVector) {
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
