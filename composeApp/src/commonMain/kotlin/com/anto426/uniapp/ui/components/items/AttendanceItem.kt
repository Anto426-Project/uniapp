package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.accordion.LiquidExpandableCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.AttendanceData
import com.anto426.uniapp.data.contract.SingleAttendanceEntry
import com.kyant.shapes.RoundedRectangle
import kotlin.math.round
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AttendanceItem(data: AttendanceData) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    var expanded by rememberSaveable(data.course) { mutableStateOf(false) }

    val rawPercentage = data.percentage.replace("%", "").trim().toIntOrNull()
    val progress = ((rawPercentage ?: 0) / 100f).coerceIn(0f, 1f)

    val accentColor = when {
        rawPercentage == null -> colorScheme.onSurfaceVariant
        rawPercentage >= 75 -> glassColors.success
        rawPercentage >= 50 -> glassColors.warning
        else -> colorScheme.error
    }

    val hoursLabel = when {
        data.totalHours != null && data.totalHours > 0.0 ->
            "${formatAttendanceHours(data.attendedHours)} / ${formatAttendanceHours(data.totalHours)}"
        data.attendedHours > 0.0 -> formatAttendanceHours(data.attendedHours)
        else -> null
    }

    val countLabel = stringResource(Res.string.ui_attendance_prefix, data.count)
    val subtitle = buildList {
        data.percentage.takeIf(String::isNotBlank)?.let(::add)
        add(countLabel)
        if (hoursLabel != null) add(hoursLabel)
    }.joinToString(" • ")

    LiquidExpandableCard(
        title = data.course,
        subtitle = subtitle,
        leadingIcon = LiquidIcons.MenuBook,
        isExpanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
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
                LiquidBadge(
                    text = data.percentage,
                    containerColor = accentColor.copy(alpha = 0.14f),
                    contentColor = accentColor,
                )

                if (hoursLabel != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Time,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = hoursLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                }
            }

            LiquidLinearProgressIndicator(
                progress = progress,
                progressColor = accentColor,
                modifier = Modifier.fillMaxWidth(),
            )

            if (data.records.isNotEmpty()) {
                LiquidHorizontalDivider()

                Text(
                    text = stringResource(Res.string.ui_attendance_lessons_recorded, data.records.size).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant,
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    data.records.forEachIndexed { index, record ->
                        SingleAttendanceRow(
                            record = record,
                            accentColor = accentColor,
                            showDivider = index < data.records.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleAttendanceRow(
    record: SingleAttendanceEntry,
    accentColor: Color,
    showDivider: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    val details = listOfNotNull(
        record.time.takeIf { it.isNotBlank() },
        record.room.takeIf { it.isNotBlank() }?.let { "Aula $it" },
        record.teacher.takeIf { it.isNotBlank() },
    ).joinToString(" • ")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LiquidIcons.Check,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 32.dp,
                    iconSize = 16.dp,
                    containerColor = accentColor.copy(alpha = 0.12f),
                    shape = RoundedRectangle(10.dp),
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = record.date.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            val trailingBadge = when {
                record.hours != null && record.hours > 0.0 -> "+${formatAttendanceHours(record.hours)}"
                record.status.isNotBlank() -> record.status
                else -> null
            }
            if (trailingBadge != null) {
                LiquidBadge(
                    text = trailingBadge,
                    containerColor = accentColor.copy(alpha = 0.12f),
                    contentColor = accentColor,
                )
            }
        }

        if (showDivider) {
            LiquidHorizontalDivider(modifier = Modifier.padding(start = 44.dp))
        }
    }
}

private fun formatAttendanceHours(hours: Double): String {
    val rounded = round(hours * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) "${rounded.toInt()}h" else "${rounded}h"
}
