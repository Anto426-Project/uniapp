package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.charts.model.LiquidLineChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidLineSeries
import com.anto426.liquidmonet.components.charts.model.LiquidLineStyle
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.navigation.model.GradesDetailSection
import com.anto426.uniapp.ui.components.display.UniBlockDefaults
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.kyant.shapes.Capsule
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GradesOverviewTab(
    chart: GradesUiState,
    projected: Boolean,
    deltaAverage: Double,
    onOpenDetail: (GradesDetailSection) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val trend = chart.recentTrend.toDouble()
    val weightingSpread = chart.weightedArithmeticSpread
    val accent = when {
        !projected -> colors.primary
        deltaAverage >= 0.0 -> glass.success
        else -> colors.error
    }
    val degreeProgress = (chart.currentGraduationBase / 110.0).toFloat().coerceIn(0f, 1f)
    val cfuPercent = (chart.careerProgress * 100f).roundToInt().coerceIn(0, 100)

    val topTier = remember(chart.gradeTiers) {
        chart.gradeTiers.filter { it.examCount > 0f }.maxByOrNull { it.examCount }
    }
    val topCfuBucket = remember(chart.cfuEntries) {
        chart.cfuEntries.maxByOrNull { it.value }
    }

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        // 1. Compact, Balanced Career Summary Hero Card
        UniInsetGroup(
            contentPadding = PaddingValues(18.dp),
            animateEntrance = false,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            if (projected) Res.string.ui_grades_projection else Res.string.ui_grades_scope_real,
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (projected) {
                            LiquidBadge(
                                text = signedGradeMetric(deltaAverage),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_weighted_average),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = gradeMetric(chart.currentAverage),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 38.sp,
                                    lineHeight = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1.4).sp,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = colors.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text = "/ 30",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp),
                                maxLines = 1,
                            )
                        }
                    }

                    UniCircularProgressRing(
                        progress = degreeProgress,
                        centerText = gradeMetric(chart.currentGraduationBase),
                        subtitleText = "/110",
                        size = 78.dp,
                        strokeWidth = 6.dp,
                        primaryColor = accent,
                        secondaryColor = colors.tertiary,
                    )
                }

                LiquidHorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GradesCompactStat(
                        value = gradeMetric(chart.currentArithmeticAverage),
                        label = stringResource(Res.string.ui_stats_arithmetic_average),
                        valueColor = colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesVerticalHairline()
                    GradesCompactStat(
                        value = weightingSpread?.let { signedGradeMetric(it) } ?: "—",
                        label = stringResource(Res.string.ui_stats_weighting_spread),
                        valueColor = when {
                            weightingSpread == null -> colors.onSurface
                            weightingSpread >= 0.0 -> glass.success
                            else -> colors.error
                        },
                        modifier = Modifier.weight(1f),
                    )
                    GradesVerticalHairline()
                    GradesCompactStat(
                        value = if (chart.currentExams.size < 2) "—" else signedGradeMetric(trend),
                        label = stringResource(Res.string.ui_stats_recent_trend),
                        valueColor = when {
                            chart.currentExams.size < 2 -> colors.onSurface
                            trend >= 0.0 -> glass.success
                            else -> colors.error
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${chart.totalProjectedCfu}/${chart.degreeCfuTarget} CFU",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = "$cfuPercent%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = accent,
                            maxLines = 1,
                        )
                    }
                    LiquidLinearProgressIndicator(
                        progress = chart.careerProgress,
                        progressColor = if (projected) accent else null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // 2. Frameless Interactive Grade Evolution Chart + Direct Link to Sub-page
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LiquidSectionHeader(
                        title = stringResource(Res.string.ui_stats_evolution_title),
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(Capsule())
                        .clickable(role = Role.Button) { onOpenDetail(GradesDetailSection.EVOLUTION) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_details),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }

            if (chart.singleGradeEntries.size >= 2) {
                val gradeSeriesLabel = stringResource(Res.string.ui_stats_series_grade)
                val weightedSeriesLabel = stringResource(Res.string.ui_stats_series_weighted)
                val lineSeries = remember(
                    chart.singleGradeEntries,
                    chart.weightedAverageEntries,
                    gradeSeriesLabel,
                    weightedSeriesLabel,
                    accent,
                    colors.tertiary,
                ) {
                    listOf(
                        LiquidLineSeries(
                            label = gradeSeriesLabel,
                            entries = chart.singleGradeEntries,
                            color = accent,
                            lineStyle = LiquidLineStyle.Solid,
                            showArea = true,
                            showPoints = true,
                        ),
                        LiquidLineSeries(
                            label = weightedSeriesLabel,
                            entries = chart.weightedAverageEntries,
                            color = colors.tertiary,
                            lineStyle = LiquidLineStyle.Dashed,
                        ),
                    )
                }

                LiquidLineChart(
                    series = lineSeries,
                    height = 200.dp,
                    minValue = chart.gradeMin,
                    maxValue = chart.gradeMax,
                    showLegend = true,
                    showSelection = true,
                    style = LiquidLineChartStyle(
                        dashedGridLines = true,
                        smoothCurves = true,
                        areaTopAlpha = 0.20f,
                        dropletRadius = 5.dp,
                    ),
                    selectionContent = { _, values ->
                        val title = values.firstOrNull()?.second?.label.orEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                values.forEach { (seriesItem, entry) ->
                                    val seriesColor = seriesItem.color ?: colors.primary
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(seriesColor),
                                        )
                                        Text(
                                            text = formatLiquidChartValue(entry.value),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFeatureSettings = "tnum",
                                            ),
                                            color = seriesColor,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
            } else {
                UniInsetGroup(
                    contentPadding = PaddingValues(20.dp),
                    animateEntrance = false,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_grades_no_chart_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // 3. Data Analysis Sub-Pages Navigation Group (uniform 64.dp rows)
        val evolutionSub = if (chart.currentExams.size >= 2) {
            "${stringResource(Res.string.ui_career_exams_count, chart.currentExams.size)} · ${stringResource(Res.string.ui_stats_recent_trend)} ${signedGradeMetric(trend)}"
        } else {
            stringResource(Res.string.ui_career_exams_count, chart.currentExams.size)
        }
        val distributionSub = topTier?.let { tier ->
            "${stringResource(Res.string.ui_grades_tier_band_format, tier.label)} · ${tier.percentage}%"
        } ?: stringResource(Res.string.ui_stats_grade_tiers_sub)
        val creditsSub = topCfuBucket?.let { bucket ->
            "${chart.totalProjectedCfu}/${chart.degreeCfuTarget} CFU · ${bucket.label}"
        } ?: "${chart.totalProjectedCfu}/${chart.degreeCfuTarget} CFU"

        UniInsetGroup(animateEntrance = false) {
            UniInsetRow(
                title = stringResource(Res.string.ui_stats_evolution_title),
                subtitle = evolutionSub,
                icon = LiquidIcons.Analytics,
                iconTint = colors.primary,
                onClick = { onOpenDetail(GradesDetailSection.EVOLUTION) },
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_stats_grade_tiers_title),
                subtitle = distributionSub,
                icon = LiquidIcons.Star,
                iconTint = colors.tertiary,
                onClick = { onOpenDetail(GradesDetailSection.DISTRIBUTION) },
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_stats_cfu_distribution_title),
                subtitle = creditsSub,
                icon = LiquidIcons.MenuBook,
                iconTint = colors.secondary,
                onClick = { onOpenDetail(GradesDetailSection.CREDITS) },
                showDivider = false,
            )
        }
    }
}
