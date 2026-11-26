package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart
import com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.charts.model.LiquidBarChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidDonutChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidLineChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidLineSeries
import com.anto426.liquidmonet.components.charts.model.LiquidLineStyle
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.navigation.model.GradesDetailSection
import com.anto426.uniapp.ui.components.display.UniBlockDefaults
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.anto426.uniapp.ui.didactics.components.GradesCompactStat
import com.anto426.uniapp.ui.didactics.components.GradesVerticalHairline
import com.anto426.uniapp.ui.didactics.components.gradeMetric
import com.anto426.uniapp.ui.didactics.components.signedGradeMetric
import com.anto426.uniapp.ui.didactics.components.simulationGradeLabel
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun GradesDetailScreen(
    uiState: GradesUiState,
    section: GradesDetailSection,
    initialProjected: Boolean = false,
) {
    var includeSimulated by rememberSaveable(initialProjected) { mutableStateOf(initialProjected) }
    val hasActiveSimulation = uiState.activeSimulatedCount > 0
    val projected = hasActiveSimulation && includeSimulated
    val chart = uiState.chartState(projected)

    UniScreenColumn {
        if (hasActiveSimulation) {
            val scopeOptions = listOf(
                false to stringResource(Res.string.ui_grades_scope_real),
                true to stringResource(Res.string.ui_grades_projection),
            )
            UniHorizontalChipRow {
                scopeOptions.forEach { (isProjected, label) ->
                    key(isProjected) {
                        val selected = projected == isProjected
                        LiquidChip(
                            label = label,
                            selected = selected,
                            leadingIcon = if (selected) LiquidIcons.Check else null,
                            onClick = { includeSimulated = isProjected },
                        )
                    }
                }
            }
        }

        when (section) {
            GradesDetailSection.EVOLUTION -> EvolutionDetailContent(chart = chart, projected = projected, deltaAverage = uiState.deltaAverage)
            GradesDetailSection.DISTRIBUTION -> DistributionDetailContent(chart = chart)
            GradesDetailSection.CREDITS -> CreditsDetailContent(chart = chart, projected = projected, deltaAverage = uiState.deltaAverage)
        }
    }
}

@Composable
private fun EvolutionDetailContent(
    chart: GradesUiState,
    projected: Boolean,
    deltaAverage: Double,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val accent = when {
        !projected -> colors.primary
        deltaAverage >= 0.0 -> glass.success
        else -> colors.error
    }
    val trend = chart.recentTrend.toDouble()
    val spread = chart.weightedArithmeticSpread
    val progression = remember(chart) { chart.examProgression.reversed() }

    // 1. Key Trend Summary Strip
    UniInsetGroup(
        contentPadding = PaddingValues(18.dp),
        animateEntrance = false,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GradesCompactStat(
                value = gradeMetric(chart.currentAverage),
                label = stringResource(Res.string.ui_weighted_average),
                valueColor = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            GradesVerticalHairline()
            GradesCompactStat(
                value = gradeMetric(chart.currentArithmeticAverage),
                label = stringResource(Res.string.ui_stats_arithmetic_average),
                valueColor = colors.onSurface,
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
    }

    // 2. Full-Width Frameless Interactive Line Chart
    if (chart.singleGradeEntries.size >= 2) {
        val gradeSeriesLabel = stringResource(Res.string.ui_stats_series_grade)
        val weightedSeriesLabel = stringResource(Res.string.ui_stats_series_weighted)
        val arithmeticSeriesLabel = stringResource(Res.string.ui_stats_series_arithmetic)
        val lineSeries = remember(
            chart.singleGradeEntries,
            chart.weightedAverageEntries,
            chart.arithmeticAverageEntries,
            gradeSeriesLabel,
            weightedSeriesLabel,
            arithmeticSeriesLabel,
            accent,
            colors.tertiary,
            colors.secondary,
        ) {
            listOf(
                LiquidLineSeries(
                    label = gradeSeriesLabel,
                    entries = chart.singleGradeEntries,
                    color = accent,
                    lineStyle = LiquidLineStyle.Solid,
                    showArea = true,
                    showPoints = true,
                    initiallyVisible = true,
                ),
                LiquidLineSeries(
                    label = weightedSeriesLabel,
                    entries = chart.weightedAverageEntries,
                    color = colors.tertiary,
                    lineStyle = LiquidLineStyle.Dashed,
                    initiallyVisible = true,
                ),
                LiquidLineSeries(
                    label = arithmeticSeriesLabel,
                    entries = chart.arithmeticAverageEntries,
                    color = colors.secondary,
                    lineStyle = LiquidLineStyle.Dotted,
                    initiallyVisible = false,
                ),
            )
        }

        LiquidLineChart(
            series = lineSeries,
            height = 248.dp,
            minValue = chart.gradeMin,
            maxValue = chart.gradeMax,
            showLegend = true,
            showSelection = true,
            style = LiquidLineChartStyle(
                dashedGridLines = true,
                smoothCurves = true,
                areaTopAlpha = 0.22f,
                dropletRadius = 5.5.dp,
            ),
            selectionContent = { _, values ->
                val title = values.firstOrNull()?.second?.label.orEmpty()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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

    // 3. Exam-by-Exam Chronological Progression
    if (progression.isNotEmpty()) {
        val subtitleSummary = buildString {
            append(stringResource(Res.string.ui_career_exams_count, progression.size))
            if (spread != null) {
                append(" · ")
                append(stringResource(Res.string.ui_stats_weighting_spread))
                append(" ")
                append(signedGradeMetric(spread))
            }
        }
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_real),
            subtitle = subtitleSummary,
        )

        UniInsetGroup(animateEntrance = false) {
            progression.forEachIndexed { index, point ->
                val delta = point.deltaFromPrevious
                val deltaColor = when {
                    delta == null -> colors.onSurfaceVariant
                    delta >= 0.0 -> glass.success
                    else -> colors.error
                }
                val rowSub = buildString {
                    append("${point.cfu} CFU · ")
                    append(stringResource(Res.string.ui_weighted_average))
                    append(" ")
                    append(gradeMetric(point.runningWeightedAverage))
                    if (delta != null) {
                        append(" (${signedGradeMetric(delta)})")
                    }
                }
                val numberTint = if (point.isSimulated) colors.tertiary else accent

                UniInsetRow(
                    title = point.name,
                    subtitle = rowSub,
                    showDivider = index < progression.lastIndex,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(UniBlockDefaults.IconContainerSize)
                                .clip(RoundedRectangle(UniBlockDefaults.IconCornerRadius))
                                .background(numberTint.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "#${point.index}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = numberTint,
                                maxLines = 1,
                            )
                        }
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (delta != null) {
                                Text(
                                    text = signedGradeMetric(delta),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = deltaColor,
                                    maxLines = 1,
                                )
                            }
                            LiquidBadge(
                                text = simulationGradeLabel(point.grade),
                                containerColor = numberTint.copy(alpha = 0.14f),
                                contentColor = numberTint,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DistributionDetailContent(chart: GradesUiState) {
    val colors = MaterialTheme.colorScheme
    val tierColors = listOf(
        colors.primary,
        colors.tertiary,
        colors.secondary,
        colors.onSurfaceVariant.copy(alpha = 0.72f),
    )
    val activeTiers = remember(chart.gradeTiers) {
        chart.gradeTiers.filter { it.examCount > 0f }
    }
    val donutEntries = remember(activeTiers, tierColors) {
        activeTiers.mapIndexed { index, tier ->
            LiquidPieEntry(
                label = tier.label,
                value = tier.examCount,
                color = tierColors[index % tierColors.size],
            )
        }
    }
    var selectedTierLabel by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTierIndex = selectedTierLabel?.let { label ->
        activeTiers.indexOfFirst { it.label == label }.takeIf { it >= 0 }
    }
    val filteredExams = remember(chart.currentExams, selectedTierLabel) {
        chart.examsInTier(selectedTierLabel).sortedByDescending { it.grade }
    }

    if (donutEntries.isEmpty()) {
        UniExpressiveStatusState(
            title = stringResource(Res.string.ui_grades_no_chart_data),
            description = stringResource(Res.string.ui_stats_grade_tiers_sub),
            inline = true,
            mainIcon = LiquidIcons.Star,
        )
        return
    }

    val examsUnit = if (chart.currentExams.size == 1) {
        stringResource(Res.string.ui_stats_exam_singular)
    } else {
        stringResource(Res.string.ui_stats_exams_plural)
    }

    // 1. Frameless Large Donut Chart
    LiquidDonutChart(
        entries = donutEntries,
        size = 196.dp,
        selectedIndex = selectedTierIndex,
        showLegend = false,
        style = LiquidDonutChartStyle(
            strokeWidth = 20.dp,
            sliceGapDegrees = 4f,
        ),
        centerContent = { selectedEntry, _ ->
            val selectedTier = selectedTierIndex?.let { activeTiers.getOrNull(it) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (selectedTier != null) "${selectedTier.percentage}%" else "${chart.currentExams.size}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.0).sp,
                        fontFeatureSettings = "tnum",
                    ),
                    color = selectedEntry?.color ?: colors.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = selectedEntry?.label ?: examsUnit,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onEntrySelected = { entry ->
            selectedTierLabel = entry?.label
        },
    )

    // 2. Filter Chips by Grade Band
    UniHorizontalChipRow {
        LiquidChip(
            label = stringResource(Res.string.ui_career_all_statuses),
            selected = selectedTierLabel == null,
            leadingIcon = if (selectedTierLabel == null) LiquidIcons.Check else null,
            onClick = { selectedTierLabel = null },
        )
        activeTiers.forEach { tier ->
            key(tier.label) {
                val isSelected = selectedTierLabel == tier.label
                LiquidChip(
                    label = "${tier.label} (${tier.percentage}%)",
                    selected = isSelected,
                    leadingIcon = if (isSelected) LiquidIcons.Check else null,
                    onClick = { selectedTierLabel = if (isSelected) null else tier.label },
                )
            }
        }
    }

    // 3. Filtered Exams List
    LiquidSectionHeader(
        title = selectedTierLabel?.let { stringResource(Res.string.ui_grades_tier_band_format, it) }
            ?: stringResource(Res.string.ui_career_completed),
        subtitle = stringResource(Res.string.ui_career_exams_count, filteredExams.size),
    )

    UniInsetGroup(animateEntrance = false) {
        filteredExams.forEachIndexed { index, exam ->
            val accent = if (exam.grade >= 30) colors.primary else colors.tertiary
            UniInsetRow(
                title = exam.name,
                subtitle = "${exam.cfu} CFU",
                icon = if (exam.grade >= 31) LiquidIcons.Star else LiquidIcons.Check,
                iconTint = accent,
                showDivider = index < filteredExams.lastIndex,
                trailingContent = {
                    LiquidBadge(
                        text = simulationGradeLabel(exam.grade),
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent,
                    )
                },
            )
        }
    }
}

@Composable
private fun CreditsDetailContent(
    chart: GradesUiState,
    projected: Boolean,
    deltaAverage: Double,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val accent = when {
        !projected -> colors.primary
        deltaAverage >= 0.0 -> glass.success
        else -> colors.error
    }
    val examSuffix = stringResource(Res.string.ui_stats_exams_plural)
    val impacts = remember(chart) { chart.examWeightImpacts }

    // 1. Frameless Interactive CFU Distribution Bar Chart
    if (chart.cfuEntries.isNotEmpty()) {
        LiquidBarChart(
            entries = chart.cfuEntries,
            height = 196.dp,
            barColor = accent,
            style = LiquidBarChartStyle(
                barWidth = 28.dp,
                showXAxisLabels = true,
                dashedGridLines = true,
            ),
            maxValue = chart.cfuMax.takeIf { it > 0f }?.let { it + 1f },
            valueSuffix = examSuffix,
            valueFormatter = { it.roundToInt().toString() },
            showSelection = true,
            selectionContent = { _, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = "${entry.value.roundToInt()} $examSuffix",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = accent,
                        maxLines = 1,
                    )
                }
            },
        )
    }

    // 2. Exam Impact on Weighted Average Ranking
    if (impacts.isNotEmpty()) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_stats_weighting_spread),
            subtitle = "${chart.totalCurrentCfu} CFU · ${stringResource(Res.string.ui_weighted_average)} ${gradeMetric(chart.currentAverage)}",
        )

        UniInsetGroup(animateEntrance = false) {
            impacts.forEachIndexed { index, item ->
                val isPositive = item.impactOnAverage >= 0.0
                val impactColor = if (isPositive) glass.success else colors.error
                UniInsetRow(
                    title = item.name,
                    subtitle = "${item.cfu} CFU (${item.cfuSharePercent}%) · ${simulationGradeLabel(item.grade)}",
                    icon = if (isPositive) LiquidIcons.Star else LiquidIcons.KeyboardArrowDown,
                    iconTint = impactColor,
                    showDivider = index < impacts.lastIndex,
                    trailingContent = {
                        LiquidBadge(
                            text = signedGradeMetric(item.impactOnAverage),
                            containerColor = impactColor.copy(alpha = 0.14f),
                            contentColor = impactColor,
                        )
                    },
                )
            }
        }
    } else {
        UniExpressiveStatusState(
            title = stringResource(Res.string.ui_grades_no_chart_data),
            description = stringResource(Res.string.ui_stats_cfu_distribution_title),
            inline = true,
            mainIcon = LiquidIcons.MenuBook,
        )
    }
}
