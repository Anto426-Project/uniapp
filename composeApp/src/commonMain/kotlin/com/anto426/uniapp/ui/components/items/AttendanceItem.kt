package com.anto426.uniapp.ui.components.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.AttendanceData
import com.anto426.uniapp.model.didactics.SingleAttendanceEntry
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AttendanceItem(data: AttendanceData, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    val rawPercentage = try {
        data.percentage.replace("%", "").trim().toIntOrNull()
    } catch (_: Exception) { null }

    val progress = (rawPercentage ?: 0) / 100f

    val (badgeBg, badgeFg) = when {
        rawPercentage == null -> Pair(colorScheme.surfaceVariant.copy(alpha = 0.5f), colorScheme.onSurfaceVariant)
        rawPercentage >= 75 -> Pair(colorScheme.primaryContainer, colorScheme.primary)
        rawPercentage >= 50 -> Pair(colorScheme.secondaryContainer, colorScheme.secondary)
        else -> Pair(colorScheme.errorContainer.copy(alpha = 0.5f), colorScheme.error)
    }

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp,
        onClick = if (data.records.isNotEmpty()) { { expanded = !expanded } } else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = LiquidIcons.MenuBook,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 40.dp,
                            iconSize = 20.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                        ),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = data.course,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_attendance_prefix, data.count),
                                color = colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                            )
                            if (data.totalHours != null && data.totalHours > 0.0) {
                                Text(
                                    text = "•",
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                )
                                Text(
                                    text = "${data.attendedHours.toInt()}h / ${data.totalHours.toInt()}h",
                                    color = colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                LiquidBadge(
                    text = data.percentage,
                    containerColor = badgeBg,
                    contentColor = badgeFg,
                    backdropState = backdropState,
                )
            }

            LiquidLinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                backdropState = backdropState,
            )

            if (data.records.isNotEmpty()) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                        Text(
                            text = stringResource(Res.string.ui_attendance_lessons_recorded, data.records.size),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp,
                        )

                        data.records.forEach { record ->
                            SingleAttendanceRow(record = record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleAttendanceRow(record: SingleAttendanceEntry) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = record.date.ifBlank { "Lezione" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    if (record.time.isNotBlank() || record.room.isNotBlank()) {
                        Text(
                            text = listOf(record.time, record.room.takeIf { it.isNotBlank() }?.let { "Aula $it" })
                                .filterNotNull()
                                .filter { it.isNotBlank() }
                                .joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            if (record.hours != null && record.hours > 0.0) {
                Text(
                    text = "+${record.hours.toInt()}h",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                )
            }
        }
    }
}
