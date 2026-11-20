package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.accordion.LiquidExpandableCard
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart
import com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.stepper.LiquidStepper
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.didactics.presentation.TargetFeasibility
import com.anto426.uniapp.model.didactics.SimulationItem
import com.anto426.uniapp.model.didactics.toFixedTwoDecimals
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Simulatore interattivo di media ponderata, CFU e base di laurea.
 */
@Composable
fun SimulationTab(
    uiState: GradesUiState,
    onToggleItem: (String) -> Unit,
    onGradeChange: (String, Int) -> Unit,
    onCfuChange: (String, Int) -> Unit,
    onAddCustomExam: (String, Int, Int) -> Unit,
    onRemoveCustomExam: (String) -> Unit,
    onSetAllGrades: (Int) -> Unit,
    onApplyCurrentAverage: () -> Unit,
    onResetSimulation: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var isAddDialogOpen by rememberSaveable { mutableStateOf(false) }
    val hasScenario = uiState.activeSimulatedCount > 0

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidSectionHeader(
                    title = stringResource(
                        if (hasScenario) Res.string.ui_grades_simulation_result else Res.string.ui_grades_current,
                    ),
                )
                GradesMetricGrid(columns = 2) {
                    GradesMetricTile(
                        label = stringResource(if (hasScenario) Res.string.ui_grades_simulation_estimated else Res.string.ui_weighted_average),
                        value = gradeMetric(uiState.projectedAverage),
                        unit = "/30",
                        valueColor = colors.onSurface,
                        prominent = true,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(if (hasScenario) Res.string.ui_grades_projected_degree_base else Res.string.ui_grades_base_degree),
                        value = gradeMetric(uiState.projectedGraduationBase),
                        unit = "/110",
                        valueColor = colors.primary,
                        prominent = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (hasScenario) {
                    val delta = uiState.deltaAverage
                    val tint = if (delta >= 0.0) LiquidGlassTheme.colors.success else colors.error
                    LiquidBadge(
                        text = stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(delta)),
                        containerColor = tint.copy(alpha = 0.14f),
                        contentColor = colors.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_grades_simulation_summary, uiState.activeSimulatedCount, uiState.activeSimulatedCfu),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }

                val progressLabel = stringResource(
                    if (hasScenario) Res.string.ui_grades_cfu_progress_projected
                    else Res.string.ui_stats_study_plan_progress,
                )
                Text(
                    text = "$progressLabel · ${uiState.totalProjectedCfu}/${uiState.degreeCfuTarget} CFU",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                LiquidLinearProgressIndicator(progress = uiState.careerProgress, modifier = Modifier.fillMaxWidth())
            }
        }

        val activeLabel = stringResource(
            if (uiState.activeSimulatedCount == 1) Res.string.ui_grades_active_singular
            else Res.string.ui_grades_active_plural,
            uiState.activeSimulatedCount,
        )
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_simulation_to_simulate),
            subtitle = if (uiState.simulationItems.isNotEmpty()) stringResource(
                Res.string.ui_grades_exams_to_simulate_subtitle,
                uiState.simulationItems.size,
                activeLabel,
            ) else null,
        )
        if (uiState.simulationItems.isEmpty()) {
            UniExpressiveStatusState(
                title = stringResource(Res.string.ui_grades_no_residual_exams),
                description = stringResource(Res.string.ui_grades_simulation_empty),
                actionLabel = stringResource(Res.string.ui_grades_add_custom_exam_title),
                onActionClick = { isAddDialogOpen = true },
                inline = true,
            )
        } else {
            LiquidButton(
                text = stringResource(Res.string.ui_grades_add_custom_exam_title),
                onClick = { isAddDialogOpen = true },
                leadingIcon = { Icon(LiquidIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                variant = LiquidButtonVariant.Primary,
            )
            UniHorizontalChipRow {
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_all_30), onClick = { onSetAllGrades(30) })
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_all_28), onClick = { onSetAllGrades(28) })
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_all_24), onClick = { onSetAllGrades(24) })
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_current_average), onClick = onApplyCurrentAverage)
                if (hasScenario) {
                    LiquidChip(label = stringResource(Res.string.ui_grades_preset_reset), onClick = onResetSimulation)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.simulationItems.forEach { item ->
                    key(item.id) {
                        SimulationExamCard(
                            item = item,
                            currentWeightedAverage = uiState.currentAverage,
                            currentTotalCfu = uiState.totalCurrentCfu,
                            onToggle = { onToggleItem(item.id) },
                            onGradeChange = { onGradeChange(item.id, it) },
                            onCfuChange = { onCfuChange(item.id, it) },
                            onRemove = if (item.isCustom) { { onRemoveCustomExam(item.id) } } else null,
                        )
                    }
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AddCustomExamDialog(
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { name, cfu, grade ->
                onAddCustomExam(name, cfu, grade)
                isAddDialogOpen = false
            },
            baseAverage = uiState.projectedAverage,
            baseCfu = uiState.totalProjectedCfu,
            baseGraduation = uiState.projectedGraduationBase,
        )
    }
}

private fun projectWeightedAverage(
    baseAverage: Double,
    baseCfu: Int,
    addedCfu: Int,
    addedGrade: Int,
): Double {
    val gradeToUse = addedGrade.coerceAtMost(30)
    if (baseCfu <= 0 || baseAverage <= 0.0) return gradeToUse.toDouble()
    val totalCfu = baseCfu + addedCfu
    return if (totalCfu <= 0) baseAverage else ((baseAverage * baseCfu) + (gradeToUse * addedCfu)) / totalCfu
}

@Composable
private fun SimulationExamCard(
    item: SimulationItem,
    currentWeightedAverage: Double,
    currentTotalCfu: Int,
    onToggle: () -> Unit,
    onGradeChange: (Int) -> Unit,
    onCfuChange: (Int) -> Unit,
    onRemove: (() -> Unit)?,
) {
    val included = item.isEnabled
    val impact = projectWeightedAverage(currentWeightedAverage, currentTotalCfu, item.cfu, item.grade) - currentWeightedAverage
    val subtitle = buildList {
        add("${item.cfu} CFU")
        if (included) {
            add(simulationGradeLabel(item.grade))
            if (currentTotalCfu > 0) {
                add(stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(impact)))
            }
        } else {
            add(stringResource(Res.string.ui_grades_simulate_badge))
        }
    }.joinToString(" · ")

    LiquidExpandableCard(
        title = item.name,
        subtitle = subtitle,
        leadingIcon = when {
            item.isCustom -> LiquidIcons.Star
            included -> LiquidIcons.Check
            else -> LiquidIcons.MenuBook
        },
        isExpanded = included,
        onExpandedChange = { expanded ->
            if (expanded != included) onToggle()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidStepper(
                value = item.grade,
                onValueChange = onGradeChange,
                label = stringResource(Res.string.ui_grades_adjust_grade),
                minValue = 18,
                maxValue = 31,
            )
            UniHorizontalChipRow(bleed = 14.dp, clipToBounds = true) {
                listOf(18, 20, 22, 24, 26, 27, 28, 30, 31).forEach { option ->
                    key(option) {
                        LiquidChip(
                            label = simulationGradeLabel(option),
                            selected = item.grade == option,
                            onClick = { onGradeChange(option) },
                        )
                    }
                }
            }
            if (item.isCustom) {
                LiquidStepper(
                    value = item.cfu,
                    onValueChange = onCfuChange,
                    label = stringResource(Res.string.ui_grades_custom_exam_cfu),
                    minValue = 1,
                    maxValue = 30,
                    unit = "CFU",
                )
            }
            if (onRemove != null) {
                LiquidButton(
                    text = stringResource(Res.string.ui_grades_remove_custom_exam),
                    onClick = onRemove,
                    variant = LiquidButtonVariant.Glass,
                    size = LiquidButtonSize.Medium,
                    leadingIcon = { Icon(LiquidIcons.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }
    }
}

/**
 * Obiettivo e Calcolatore Target di Laurea.
 */
@Composable
fun GraduationTargetTab(
    uiState: GradesUiState,
    onTargetDegreeChange: (Int) -> Unit,
    onThesisPointsChange: (Int) -> Unit,
    onBonusPointsChange: (Int) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val remainingCfu = uiState.remainingDegreeCfu
    val addedPoints = uiState.thesisPoints + uiState.bonusPoints
    val finalEstimate = uiState.currentGraduationBase + addedPoints
    val targetReached = finalEstimate >= uiState.targetScoreEffective
    val feasibility = when {
        remainingCfu > 0 -> uiState.feasibilityLevel
        targetReached -> TargetFeasibility.EASY
        else -> TargetFeasibility.IMPOSSIBLE
    }
    val feasibilityColor = when (feasibility) {
        TargetFeasibility.EASY -> glassColors.success
        TargetFeasibility.FEASIBLE -> colorScheme.primary
        TargetFeasibility.HARD -> glassColors.warning
        TargetFeasibility.IMPOSSIBLE -> colorScheme.error
    }

    val feasibilityLabel = when (feasibility) {
        TargetFeasibility.EASY -> stringResource(Res.string.ui_grades_feasibility_easy)
        TargetFeasibility.FEASIBLE -> stringResource(Res.string.ui_grades_feasibility_feasible)
        TargetFeasibility.HARD -> stringResource(Res.string.ui_grades_feasibility_hard)
        TargetFeasibility.IMPOSSIBLE -> stringResource(Res.string.ui_grades_feasibility_impossible)
    }

    val statusText = when {
        remainingCfu == 0 -> {
            if (targetReached) {
                stringResource(Res.string.ui_grades_target_done_reached, addedPoints, uiState.targetDegree.coerceAtMost(110))
            } else {
                stringResource(Res.string.ui_grades_target_done_estimate, addedPoints, finalEstimate.toFixedTwoDecimals())
            }
        }
        feasibility == TargetFeasibility.EASY -> stringResource(
            Res.string.ui_grades_target_easy_msg,
            uiState.requiredRemainingAverage.toFixedTwoDecimals(),
            remainingCfu,
            uiState.currentAverage.toFixedTwoDecimals(),
        )
        feasibility == TargetFeasibility.FEASIBLE -> stringResource(
            Res.string.ui_grades_target_feasible_msg,
            uiState.requiredRemainingAverage.toFixedTwoDecimals(),
            remainingCfu,
        )
        feasibility == TargetFeasibility.HARD -> stringResource(
            Res.string.ui_grades_target_hard_msg,
            uiState.requiredRemainingAverage.toFixedTwoDecimals(),
        )
        else -> stringResource(Res.string.msg_obiettivo_non_matematicamente_possibile_con_i_parametri_correnti)
    }

    val targetLabel = graduationGradeLabel(uiState.targetDegree)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidBadge(
                    text = feasibilityLabel,
                    containerColor = feasibilityColor.copy(alpha = 0.16f),
                    contentColor = colorScheme.onSurface,
                )
                GradesMetricGrid(columns = 2) {
                    GradesMetricTile(
                        label = stringResource(if (remainingCfu > 0) Res.string.ui_grades_required_average else Res.string.ui_grades_current_estimate),
                        value = when {
                            remainingCfu == 0 -> gradeMetric(finalEstimate)
                            uiState.requiredRemainingAverage > 30.0 -> ">30"
                            else -> gradeMetric(uiState.requiredRemainingAverage)
                        },
                        unit = if (remainingCfu > 0) "/30" else "/110",
                        valueColor = colorScheme.onSurface,
                        prominent = true,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_final_target),
                        value = targetLabel,
                        unit = if (uiState.targetDegree < 111) "/110" else null,
                        valueColor = colorScheme.primary,
                        prominent = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(statusText, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                if (remainingCfu > 0) {
                    Text(
                        text = stringResource(Res.string.ui_grades_needed_base_format, uiState.neededGraduationBase.toFixedTwoDecimals()),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
                LiquidHorizontalDivider(color = colorScheme.outlineVariant)
                GradesMetricGrid(columns = 3) {
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_current_base),
                        value = gradeMetric(uiState.currentGraduationBase),
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_thesis_bonus),
                        value = "+$addedPoints",
                        unit = "pt",
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_current_estimate),
                        value = gradeMetric(finalEstimate),
                        unit = "/110",
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_customize_params_title),
            subtitle = stringResource(Res.string.ui_grades_customize_params_sub),
        )
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(22.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidStepper(
                    value = uiState.targetDegree,
                    onValueChange = onTargetDegreeChange,
                    label = stringResource(Res.string.ui_grades_target_degree_vote),
                    minValue = 66,
                    maxValue = 111,
                )
                GradesActions {
                    listOf(100, 105, 110, 111).forEach { target ->
                        key(target) {
                            val selected = target == uiState.targetDegree
                            LiquidChip(
                                label = if (target == 111) graduationGradeLabel(target) else "$target / 110",
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { onTargetDegreeChange(target) },
                            )
                        }
                    }
                }
                LiquidHorizontalDivider(color = colorScheme.outlineVariant)
                LiquidStepper(
                    value = uiState.thesisPoints,
                    onValueChange = onThesisPointsChange,
                    label = stringResource(Res.string.ui_grades_thesis_points_label),
                    minValue = 0,
                    maxValue = 15,
                    unit = "pt",
                )
                LiquidStepper(
                    value = uiState.bonusPoints,
                    onValueChange = onBonusPointsChange,
                    label = stringResource(Res.string.ui_grades_career_bonus_label),
                    minValue = 0,
                    maxValue = 10,
                    unit = "pt",
                )
            }
        }
    }
}

/**
 * Grafici di andamento storico, distribuzione CFU e fasce di voto.
 */
@Composable
fun SimulationChartTab(
    uiState: GradesUiState,
) {
    val colorScheme = MaterialTheme.colorScheme

    val tierColors = listOf(
        colorScheme.primary,
        colorScheme.tertiary,
        colorScheme.secondary,
        colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )

    val donutEntries = uiState.gradeTiers.mapIndexed { index, tier ->
        LiquidPieEntry(
            label = stringResource(Res.string.ui_grades_tier_band_format, tier.label),
            value = tier.examCount,
            color = tierColors[index % tierColors.size],
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Evoluzione Voti & Medie
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_stats_evolution_title),
            subtitle = stringResource(Res.string.ui_grades_chart_evolution_sub),
        )

        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
        ) {
            if (uiState.singleGradeEntries.isNotEmpty()) {
                LiquidLineChart(
                    entries = uiState.singleGradeEntries,
                    weightedAverageEntries = uiState.weightedAverageEntries,
                    arithmeticAverageEntries = uiState.arithmeticAverageEntries,
                    height = 210.dp,
                    minValue = uiState.gradeMin,
                    maxValue = uiState.gradeMax,
                    showLegend = true,
                    primarySeriesLabel = stringResource(Res.string.ui_stats_series_grade),
                    secondarySeriesLabel = stringResource(Res.string.ui_stats_series_weighted),
                    tertiarySeriesLabel = stringResource(Res.string.ui_stats_series_arithmetic),
                )
            } else {
                Text(
                    text = stringResource(Res.string.ui_grades_no_chart_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
        }

        // 2. Distribuzione Crediti
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_stats_cfu_distribution_title),
            subtitle = stringResource(Res.string.ui_stats_cfu_distribution_sub),
        )

        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (uiState.cfuEntries.isNotEmpty()) {
                    val examSingular = stringResource(Res.string.ui_stats_exam_singular)
                    val examsPlural = stringResource(Res.string.ui_stats_exams_plural)
                    LiquidBarChart(
                        entries = uiState.cfuEntries,
                        height = 190.dp,
                        maxValue = (kotlin.math.ceil(uiState.cfuMax * 1.15f / 2f) * 2f).coerceAtLeast(2f),
                        valueFormatter = { value ->
                            val count = value.toInt()
                            "$count ${if (count == 1) examSingular else examsPlural}"
                        },
                        valueSuffix = "",
                        detailDescription = stringResource(Res.string.ui_stats_cfu_bar_detail),
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.ui_grades_no_cfu_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

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
                            text = stringResource(Res.string.ui_stats_cfu_summary_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                Res.string.ui_stats_cfu_summary_sub,
                                uiState.totalCurrentCfu,
                                uiState.degreeCfuTarget,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }

                    LiquidBadge(
                        text = "${(uiState.careerProgress * 100).roundToInt()}%",
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                }
            }
        }

        // 3. Ripartizione Fasce di Voto (Donut + Unified Tier Breakdown)
        if (donutEntries.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_stats_grade_tiers_title),
                subtitle = stringResource(Res.string.ui_stats_grade_tiers_sub),
            )

            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(24.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                LiquidDonutChart(
                    entries = donutEntries,
                    size = 186.dp,
                    centerLabel = stringResource(Res.string.ui_stats_exams_plural),
                    centerValue = uiState.currentExams.size.toString(),
                )
            }
        }
    }
}

/**
 * Dialog per aggiungere un esame ipotetico personalizzato nella simulazione con anteprima live.
 */
@Composable
fun AddCustomExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit,
    baseAverage: Double = 0.0,
    baseCfu: Int = 0,
    baseGraduation: Double = baseAverage * 110.0 / 30.0,
) {
    val colors = MaterialTheme.colorScheme
    var name by rememberSaveable { mutableStateOf("") }
    var cfu by rememberSaveable { mutableIntStateOf(6) }
    var grade by rememberSaveable { mutableIntStateOf(28) }

    val defaultName = stringResource(Res.string.ui_grades_custom_exam_default)
    val nameLabel = stringResource(Res.string.ui_grades_custom_exam_name)
    val previewAverage = projectWeightedAverage(baseAverage, baseCfu, cfu, grade)
    val delta = previewAverage - baseAverage
    val previewBase = baseGraduation + delta * 110.0 / 30.0

    LiquidDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.ui_grades_add_custom_exam_title),
        modifier = Modifier.imePadding(),
        confirmButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_add),
                onClick = { onConfirm(name.trim().ifBlank { defaultName }, cfu, grade) },
                variant = LiquidButtonVariant.Primary,
                size = LiquidButtonSize.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_cancel),
                onClick = onDismiss,
                variant = LiquidButtonVariant.Glass,
                size = LiquidButtonSize.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidTextField(
                value = name,
                onValueChange = { name = it },
                label = nameLabel,
                placeholder = stringResource(Res.string.ui_grades_custom_exam_name_placeholder),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = nameLabel },
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_grades_custom_exam_cfu),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                LiquidStepper(
                    value = cfu,
                    onValueChange = { cfu = it },
                    minValue = 1,
                    maxValue = 30,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_grades_custom_exam_grade),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                LiquidStepper(
                    value = grade,
                    onValueChange = { grade = it },
                    minValue = 18,
                    maxValue = 31,
                )
            }
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(18.dp),
                contentPadding = PaddingValues(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GradesMetricGrid(columns = 2) {
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_grades_simulation_estimated),
                            value = gradeMetric(previewAverage),
                            unit = "/30",
                            valueColor = colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_grades_projected_degree_base),
                            value = gradeMetric(previewBase),
                            unit = "/110",
                            valueColor = colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (baseCfu > 0 && baseAverage > 0.0) {
                        Text(
                            stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(delta)),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GradesActions(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GradesMetricGrid(columns: Int, content: @Composable FlowRowScope.() -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val minCellWidth = 96.dp * LocalDensity.current.fontScale
        val availableColumns = (maxWidth / minCellWidth).toInt().coerceIn(1, columns)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = availableColumns,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
internal fun GradesMetricTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    unit: String? = null,
    prominent: Boolean = false,
) {
    val typography = MaterialTheme.typography
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = typography.labelMedium, color = scheme.onSurfaceVariant)
        Text(
            text = buildAnnotatedString {
                append(value)
                if (unit != null && value != "—") {
                    withStyle(SpanStyle(fontSize = typography.labelMedium.fontSize, color = scheme.onSurfaceVariant)) {
                        append(" $unit")
                    }
                }
            },
            style = (if (prominent) typography.headlineMedium else typography.titleLarge).copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
            color = valueColor,
        )
    }
}

internal fun gradeMetric(value: Double): String = if (value > 0.0) value.toFixedTwoDecimals() else "—"

internal fun signedGradeMetric(value: Double): String =
    if (value > 0.0) "+${value.toFixedTwoDecimals()}" else value.toFixedTwoDecimals()

private fun simulationGradeLabel(value: Int): String = if (value >= 31) "30L" else value.toString()

private fun graduationGradeLabel(value: Int): String = if (value >= 111) "110L" else value.toString()
