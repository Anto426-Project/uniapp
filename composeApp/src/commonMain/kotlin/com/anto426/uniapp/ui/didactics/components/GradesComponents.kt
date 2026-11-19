package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.cards.accordion.LiquidAccordionItem
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart
import com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
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
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
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
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    var isAddDialogOpen by remember { mutableStateOf(false) }

    val delta = uiState.deltaAverage
    val deltaString = if (delta >= 0.0) "+${delta.toFixedTwoDecimals()}" else delta.toFixedTwoDecimals()
    val hasActiveSimulation = uiState.activeSimulatedCount > 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Hero Card: Live Projections & Quick Presets
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            if (hasActiveSimulation) Res.string.ui_grades_active_projection
                            else Res.string.ui_grades_current_state,
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    LiquidBadge(
                        text = if (hasActiveSimulation) {
                            val examWord = stringResource(
                                if (uiState.activeSimulatedCount == 1) Res.string.ui_stats_exam_singular
                                else Res.string.ui_stats_exams_plural,
                            )
                            stringResource(
                                Res.string.ui_grades_simulation_summary_badge,
                                deltaString,
                                uiState.activeSimulatedCount,
                                examWord,
                            )
                        } else {
                            stringResource(Res.string.ui_grades_no_simulation)
                        },
                        containerColor = if (hasActiveSimulation) {
                            (if (delta >= 0.0) glassColors.success else colorScheme.error).copy(alpha = 0.14f)
                        } else {
                            colorScheme.primary.copy(alpha = 0.10f)
                        },
                        contentColor = if (hasActiveSimulation) {
                            if (delta >= 0.0) glassColors.success else colorScheme.error
                        } else {
                            colorScheme.primary
                        },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(
                                if (hasActiveSimulation) Res.string.ui_grades_projected
                                else Res.string.ui_weighted_average,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.projectedAverage > 0.0) uiState.projectedAverage.toFixedTwoDecimals() else "—",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text = " / 30",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp),
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (hasActiveSimulation) Res.string.ui_grades_projected_degree_base
                                else Res.string.ui_grades_base_degree,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.projectedGraduationBase > 0.0) uiState.projectedGraduationBase.toFixedTwoDecimals() else "—",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary,
                                maxLines = 1,
                            )
                            Text(
                                text = " / 110",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                if (hasActiveSimulation) Res.string.ui_grades_cfu_progress_projected
                                else Res.string.ui_stats_study_plan_progress,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${(uiState.careerProgress * 100).toInt()}% • ${uiState.totalProjectedCfu}/${uiState.degreeCfuTarget} CFU",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    LiquidLinearProgressIndicator(
                        progress = uiState.careerProgress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                UniHorizontalChipRow(bleed = 16.dp, spacing = 8.dp, clipToBounds = true) {
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_all_30),
                        onClick = { onSetAllGrades(30) },
                    )
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_all_28),
                        onClick = { onSetAllGrades(28) },
                    )
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_all_24),
                        onClick = { onSetAllGrades(24) },
                    )
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_current_average),
                        onClick = onApplyCurrentAverage,
                    )
                    if (hasActiveSimulation) {
                        LiquidChip(
                            label = stringResource(Res.string.ui_grades_preset_reset),
                            onClick = onResetSimulation,
                        )
                    }
                }
            }
        }

        // 2. Exams Header + Add Custom Exam CTA
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
                    text = stringResource(Res.string.ui_grades_exams_to_simulate_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        Res.string.ui_grades_exams_to_simulate_subtitle,
                        uiState.simulationItems.size,
                        stringResource(
                            if (uiState.activeSimulatedCount == 1) Res.string.ui_grades_active_singular
                            else Res.string.ui_grades_active_plural,
                            uiState.activeSimulatedCount,
                        ),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(8.dp))

            LiquidChip(
                label = stringResource(Res.string.ui_add),
                leadingIcon = LiquidIcons.Add,
                selected = false,
                onClick = { isAddDialogOpen = true },
            )
        }

        // 3. Simulation Exam Cards
        if (uiState.simulationItems.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_grades_no_residual_exams),
                description = stringResource(Res.string.ui_grades_custom_exam_name_placeholder),
                icon = LiquidIcons.MenuBook,
                actionButtonText = stringResource(Res.string.ui_grades_add_custom_exam_title),
                onActionClick = { isAddDialogOpen = true },
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.simulationItems.forEach { item ->
                    key(item.id) {
                        SimulationExamCard(
                            item = item,
                            currentWeightedAverage = uiState.currentAverage,
                            currentTotalCfu = uiState.totalCurrentCfu,
                            onToggle = { onToggleItem(item.id) },
                            onGradeChange = { newGrade -> onGradeChange(item.id, newGrade) },
                            onCfuChange = { newCfu -> onCfuChange(item.id, newCfu) },
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
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val isIncluded = item.isEnabled

    val itemImpact = if (currentTotalCfu > 0) {
        val gradeToUse = if (item.grade >= 31) 30 else item.grade
        val newAvg = ((currentWeightedAverage * currentTotalCfu) + (gradeToUse * item.cfu)) / (currentTotalCfu + item.cfu)
        newAvg - currentWeightedAverage
    } else 0.0

    val impactColor = if (itemImpact >= 0.0) glassColors.success else colorScheme.error
    val gradeLabel = if (item.grade >= 31) "30L" else "${item.grade}"
    val impactLabel = stringResource(
        Res.string.ui_grades_impact_format,
        if (itemImpact >= 0) "+" else "",
        itemImpact.toFixedTwoDecimals(),
    )
    val accordionSubtitle = if (isIncluded) {
        buildList {
            add("${item.cfu} CFU")
            add(gradeLabel)
            if (currentTotalCfu > 0) add(impactLabel)
        }.joinToString(" • ")
    } else {
        "${item.cfu} CFU • ${stringResource(Res.string.ui_grades_simulate_badge)}"
    }

    LiquidAccordionItem(
        title = item.name,
        subtitle = accordionSubtitle,
        leadingIcon = when {
            item.isCustom -> LiquidIcons.Star
            isIncluded -> LiquidIcons.Check
            else -> LiquidIcons.MenuBook
        },
        isExpanded = isIncluded,
        onExpandedChange = { expanded ->
            if (expanded != isIncluded) onToggle()
        },
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    LiquidBadge(
                        text = "${item.cfu} CFU",
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                    if (currentTotalCfu > 0) {
                        LiquidBadge(
                            text = impactLabel,
                            containerColor = impactColor.copy(alpha = 0.14f),
                            contentColor = impactColor,
                        )
                    }
                }

                if (onRemove != null) {
                    Spacer(Modifier.width(8.dp))
                    LiquidIconButton(
                        icon = LiquidIcons.Delete,
                        onClick = onRemove,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.ui_grades_adjust_grade),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidIconButton(
                        icon = LiquidIcons.Remove,
                        onClick = { if (item.grade > 18) onGradeChange(item.grade - 1) },
                        enabled = item.grade > 18,
                    )
                    Text(
                        text = gradeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(42.dp),
                    )
                    LiquidIconButton(
                        icon = LiquidIcons.Add,
                        onClick = { if (item.grade < 31) onGradeChange(item.grade + 1) },
                        enabled = item.grade < 31,
                    )
                }
            }

            UniHorizontalChipRow(bleed = 14.dp, spacing = 6.dp, clipToBounds = true) {
                listOf(18, 20, 22, 24, 26, 27, 28, 30, 31).forEach { gradeOption ->
                    key(gradeOption) {
                        val selected = item.grade == gradeOption
                        LiquidChip(
                            label = if (gradeOption == 31) "30L" else "$gradeOption",
                            selected = selected,
                            onClick = { onGradeChange(gradeOption) },
                        )
                    }
                }
            }

            if (item.isCustom) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_grades_custom_exam_cfu),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LiquidIconButton(
                            icon = LiquidIcons.Remove,
                            onClick = { if (item.cfu > 1) onCfuChange(item.cfu - 1) },
                            enabled = item.cfu > 1,
                        )
                        Text(
                            text = "${item.cfu}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(32.dp),
                        )
                        LiquidIconButton(
                            icon = LiquidIcons.Add,
                            onClick = { if (item.cfu < 30) onCfuChange(item.cfu + 1) },
                            enabled = item.cfu < 30,
                        )
                    }
                }
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

    val isFeasible = uiState.isTargetFeasible
    val feasibility = uiState.feasibilityLevel
    val remainingCfu = uiState.remainingDegreeCfu
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
            val totalBonus = uiState.thesisPoints + uiState.bonusPoints
            val currentFinal = uiState.currentGraduationBase + totalBonus
            if (currentFinal >= uiState.targetScoreEffective) {
                stringResource(Res.string.ui_grades_target_done_reached, totalBonus, uiState.targetDegree)
            } else {
                stringResource(Res.string.ui_grades_target_done_estimate, totalBonus, currentFinal.toFixedTwoDecimals())
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Hero Calculation Card
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
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
                        text = stringResource(Res.string.ui_grades_graduation_target_header).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = feasibilityColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    LiquidBadge(
                        text = feasibilityLabel,
                        containerColor = feasibilityColor.copy(alpha = 0.14f),
                        contentColor = feasibilityColor,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(Res.string.ui_grades_required_average),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.requiredRemainingAverage > 30.0) "> 30" else uiState.requiredRemainingAverage.toFixedTwoDecimals(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = if (isFeasible) colorScheme.onSurface else colorScheme.error,
                                maxLines = 1,
                            )
                            Text(
                                text = " / 30",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp),
                            )
                        }
                        Text(
                            text = stringResource(Res.string.ui_grades_cfu_remaining_format, remainingCfu),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_grades_final_target),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.targetDegree >= 111) "110L" else "${uiState.targetDegree}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary,
                                maxLines = 1,
                            )
                            if (uiState.targetDegree < 111) {
                                Text(
                                    text = " / 110",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                        }
                        Text(
                            text = stringResource(Res.string.ui_grades_needed_base_format, uiState.neededGraduationBase.toFixedTwoDecimals()),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isFeasible) LiquidIcons.Check else LiquidIcons.Warning,
                        contentDescription = null,
                        tint = feasibilityColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_current_base),
                        value = uiState.currentGraduationBase.toFixedTwoDecimals(),
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_thesis_bonus),
                        value = "+${uiState.thesisPoints + uiState.bonusPoints} pt",
                        valueColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_grades_current_estimate),
                        value = (uiState.currentGraduationBase + uiState.thesisPoints + uiState.bonusPoints).toFixedTwoDecimals(),
                        valueColor = feasibilityColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 2. Interactive Target Configuration
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
                Text(
                    text = stringResource(Res.string.ui_grades_target_degree_vote),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )

                UniHorizontalChipRow(bleed = 14.dp, spacing = 8.dp, clipToBounds = true) {
                    listOf(
                        100 to "100 / 110",
                        105 to "105 / 110",
                        110 to "110 / 110",
                        111 to "110 e Lode",
                    ).forEach { (targetVal, label) ->
                        key(targetVal) {
                            val selected = uiState.targetDegree == targetVal
                            LiquidChip(
                                label = label,
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { onTargetDegreeChange(targetVal) },
                            )
                        }
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_grades_custom_target),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LiquidIconButton(
                            icon = LiquidIcons.Remove,
                            onClick = { if (uiState.targetDegree > 66) onTargetDegreeChange(uiState.targetDegree - 1) },
                            enabled = uiState.targetDegree > 66,
                        )
                        Text(
                            text = if (uiState.targetDegree >= 111) "110L" else "${uiState.targetDegree}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(52.dp),
                        )
                        LiquidIconButton(
                            icon = LiquidIcons.Add,
                            onClick = { if (uiState.targetDegree < 111) onTargetDegreeChange(uiState.targetDegree + 1) },
                            enabled = uiState.targetDegree < 111,
                        )
                    }
                }
            }
        }

        LiquidStepper(
            value = uiState.thesisPoints,
            onValueChange = onThesisPointsChange,
            minValue = 0,
            maxValue = 15,
            label = stringResource(Res.string.ui_grades_thesis_points_label),
            unit = "pt",
        )

        LiquidStepper(
            value = uiState.bonusPoints,
            onValueChange = onBonusPointsChange,
            minValue = 0,
            maxValue = 10,
            label = stringResource(Res.string.ui_grades_career_bonus_label),
            unit = "pt",
        )
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
        LiquidPieEntry(tier.label, tier.examCount, color = tierColors[index % tierColors.size])
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
                        maxValue = (uiState.cfuMax * 1.15f).coerceAtLeast(1f),
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
                        text = stringResource(
                            if (uiState.totalCurrentCfu >= 60) Res.string.ui_status_in_order
                            else Res.string.ui_status_in_progress,
                        ),
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LiquidDonutChart(
                        entries = donutEntries,
                        size = 186.dp,
                        centerLabel = stringResource(Res.string.ui_exams),
                        centerValue = uiState.currentExams.size.toString(),
                        showLegend = false,
                    )

                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        donutEntries.forEach { entry ->
                            val tierRatio = if (uiState.currentExams.isNotEmpty()) entry.value / uiState.currentExams.size else 0f
                            val tierPercentage = (tierRatio * 100).toInt()
                            val tierColor = entry.color ?: colorScheme.primary

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(Capsule())
                                                .background(tierColor),
                                        )
                                        Text(
                                            text = stringResource(Res.string.ui_grades_tier_band_format, entry.label),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurface,
                                        )
                                    }

                                    Text(
                                        text = stringResource(
                                            Res.string.ui_grades_tier_exams_percent,
                                            entry.value.toInt(),
                                            tierPercentage,
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = tierColor,
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(Capsule())
                                        .background(colorScheme.onSurface.copy(alpha = 0.08f)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(tierRatio.coerceIn(0f, 1f))
                                            .height(5.dp)
                                            .clip(Capsule())
                                            .background(tierColor),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog per aggiungere un esame ipotetico personalizzato nella simulazione.
 */
@Composable
fun AddCustomExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var cfu by remember { mutableIntStateOf(6) }
    var grade by remember { mutableIntStateOf(28) }

    val defaultExamName = stringResource(Res.string.ui_grades_custom_exam_default)

    LiquidDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.ui_grades_add_custom_exam_title),
        confirmButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_add),
                onClick = { onConfirm(name.trim().ifBlank { defaultExamName }, cfu, grade) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LiquidTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.ui_grades_custom_exam_name),
                placeholder = stringResource(Res.string.ui_grades_custom_exam_name_placeholder),
            )

            LiquidStepper(
                value = cfu,
                onValueChange = { cfu = it },
                minValue = 1,
                maxValue = 30,
                label = stringResource(Res.string.ui_grades_custom_exam_cfu),
                unit = "CFU",
            )

            LiquidStepper(
                value = grade,
                onValueChange = { grade = it },
                minValue = 18,
                maxValue = 30,
                label = stringResource(Res.string.ui_grades_custom_exam_grade),
                unit = "/30",
            )
        }
    }
}

@Composable
internal fun GradesMetricTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
