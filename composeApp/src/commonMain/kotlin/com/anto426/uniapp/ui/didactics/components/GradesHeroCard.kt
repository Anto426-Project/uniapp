package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GradesAnalyticsHeroCard(
    chart: GradesUiState,
    projected: Boolean,
    deltaAverage: Double,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val cardShape = remember { RoundedRectangle(24.dp) }
    val trend = chart.recentTrend.toDouble()
    val weightingSpread = if (chart.currentExams.isNotEmpty() && chart.currentAverage > 0.0 && chart.currentArithmeticAverage > 0.0) {
        chart.currentAverage - chart.currentArithmeticAverage
    } else {
        null
    }
    val accent = when {
        !projected -> colors.primary
        deltaAverage >= 0.0 -> glass.success
        else -> colors.error
    }
    val degreeProgress = (chart.currentGraduationBase / 110.0).toFloat().coerceIn(0f, 1f)
    val cfuPercent = (chart.careerProgress * 100f).roundToInt()

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        contentPadding = PaddingValues(18.dp),
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
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (projected) Res.string.ui_grades_projection else Res.string.ui_grades_scope_real,
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = gradeMetric(chart.currentAverage),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFeatureSettings = "tnum",
                            ),
                            color = colors.onSurface,
                            maxLines = 1,
                        )
                        if (chart.currentAverage > 0.0) {
                            Text(
                                text = "/30",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (projected) {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(deltaAverage)),
                                containerColor = accent.copy(alpha = 0.14f),
                                contentColor = accent,
                            )
                        } else {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_career_exams_count, chart.totalPassedExamsCount),
                                containerColor = colors.primary.copy(alpha = 0.13f),
                                contentColor = colors.primary,
                            )
                        }
                        if (chart.lodeCount > 0) {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_career_lodi_count, chart.lodeCount),
                                containerColor = colors.tertiary.copy(alpha = 0.14f),
                                contentColor = colors.tertiary,
                            )
                        }
                    }
                }

                UniCircularProgressRing(
                    progress = degreeProgress,
                    centerText = gradeMetric(chart.currentGraduationBase),
                    subtitleText = "/110",
                    size = 78.dp,
                    strokeWidth = 7.dp,
                    primaryColor = accent,
                    secondaryColor = colors.tertiary,
                )
            }

            LiquidHorizontalDivider(color = colors.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeroCompactMetric(
                    label = stringResource(Res.string.ui_stats_arithmetic_average),
                    value = gradeMetric(chart.currentArithmeticAverage),
                    valueColor = colors.onSurface,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                HeroCompactMetric(
                    label = stringResource(Res.string.ui_stats_weighting_spread),
                    value = weightingSpread?.let { signedGradeMetric(it) } ?: "—",
                    valueColor = when {
                        weightingSpread == null -> colors.onSurface
                        weightingSpread >= 0.0 -> glass.success
                        else -> colors.error
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                HeroCompactMetric(
                    label = stringResource(Res.string.ui_stats_recent_trend),
                    value = if (chart.currentExams.size < 2) "—" else signedGradeMetric(trend),
                    valueColor = when {
                        chart.currentExams.size < 2 -> colors.onSurface
                        trend >= 0.0 -> glass.success
                        else -> colors.error
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            LiquidHorizontalDivider(color = colors.onSurface.copy(alpha = 0.08f))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            if (projected) Res.string.ui_grades_cfu_progress_projected
                            else Res.string.ui_stats_study_plan_progress,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${chart.totalProjectedCfu}/${chart.degreeCfuTarget} CFU · $cfuPercent%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = accent,
                        maxLines = 1,
                    )
                }
                LiquidLinearProgressIndicator(
                    progress = chart.careerProgress,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (chart.remainingDegreeCfu > 0) {
                    Text(
                        text = stringResource(Res.string.ui_grades_cfu_remaining_format, chart.remainingDegreeCfu),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCompactMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
            color = valueColor,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
