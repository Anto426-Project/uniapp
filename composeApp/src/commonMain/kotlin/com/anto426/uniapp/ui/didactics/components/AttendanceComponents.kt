package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.AttendanceUiState
import com.anto426.uniapp.ui.components.display.UniBentoStatTile
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun AttendanceSummaryHeroCard(
    uiState: AttendanceUiState,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val average = uiState.averageAttendancePercent.coerceIn(0, 100)
    val progress = average / 100f
    val accentColor = attendanceAccentColor(average, colorScheme.primary, glassColors.success, glassColors.warning, colorScheme.error)

    LiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
        contentPadding = PaddingValues(18.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_attendance_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$average%",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontFeatureSettings = "tnum",
                        ),
                        color = colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(Res.string.ui_attendance_kpi_average),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                UniCircularProgressRing(
                    progress = progress,
                    centerText = "$average%",
                    subtitleText = stringResource(Res.string.ui_attendance_kpi_average_sub),
                    size = 74.dp,
                    strokeWidth = 7.dp,
                    primaryColor = accentColor,
                    secondaryColor = colorScheme.tertiary,
                )
            }

            LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UniBentoStatTile(
                    label = stringResource(Res.string.ui_attendance_kpi_courses),
                    value = "${uiState.totalCoursesCount}",
                    suffix = stringResource(Res.string.ui_attendance_kpi_courses_sub),
                    icon = LiquidIcons.MenuBook,
                    accentColor = colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                UniBentoStatTile(
                    label = stringResource(Res.string.ui_attendance_kpi_lectures),
                    value = "${uiState.totalAttendedLectures}",
                    suffix = stringResource(Res.string.ui_attendance_kpi_lectures_sub),
                    icon = LiquidIcons.Check,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun attendanceAccentColor(
    percent: Int,
    fallback: Color,
    success: Color,
    warning: Color,
    error: Color,
): Color = when {
    percent >= 75 -> success
    percent >= 50 -> warning
    percent > 0 -> error
    else -> fallback
}
