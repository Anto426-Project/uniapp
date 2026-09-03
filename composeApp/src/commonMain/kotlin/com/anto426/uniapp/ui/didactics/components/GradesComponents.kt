package com.anto426.uniapp.ui.didactics.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
import com.anto426.liquidmonet.components.charts.LiquidBarChart
import com.anto426.liquidmonet.components.charts.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.LiquidLineChart
import com.anto426.liquidmonet.components.charts.LiquidPieEntry
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.inputs.LiquidTextField
import com.anto426.liquidmonet.components.selection.LiquidChip
import com.anto426.liquidmonet.components.selection.LiquidStepper
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.didactics.presentation.TargetFeasibility
import com.anto426.uniapp.model.didactics.SimulationItem
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import kotlin.math.abs
import kotlin.math.round

/**
 * Tab 0: Simulatore di Carriera & Media Dinamico e Interattivo.
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
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    val delta = uiState.deltaAverage
    val isPositive = delta >= 0.0
    val deltaString = if (isPositive) "+${delta.toFixedTwoDecimals()}" else delta.toFixedTwoDecimals()
    val hasActiveSimulation = uiState.activeSimulatedCount > 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Hero Hub Card: Live Projections & Presets (Light & Clean)
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 18.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top row with status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (hasActiveSimulation) stringResource(Res.string.ui_grades_active_projection) else stringResource(Res.string.ui_grades_current_state),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        letterSpacing = 0.8.sp,
                        fontSize = 10.sp,
                    )
                    if (hasActiveSimulation) {
                        LiquidBadge(
                            text = "$deltaString • ${uiState.activeSimulatedCount} esami (${uiState.activeSimulatedCfu} CFU)",
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.primary,
                            backdropState = backdropState,
                        )
                    } else {
                        LiquidBadge(
                            text = stringResource(Res.string.ui_grades_no_simulation),
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = colorScheme.onSurfaceVariant,
                            backdropState = backdropState,
                        )
                    }
                }

                // Dual Hero KPIs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = if (hasActiveSimulation) stringResource(Res.string.ui_grades_projected) else stringResource(Res.string.ui_weighted_average),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.projectedAverage > 0.0) uiState.projectedAverage.toFixedTwoDecimals() else "—",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = " / 30",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp),
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (hasActiveSimulation) stringResource(Res.string.ui_grades_projected_degree_base) else stringResource(Res.string.ui_grades_base_degree),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.projectedGraduationBase > 0.0) uiState.projectedGraduationBase.toFixedTwoDecimals() else "—",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary,
                            )
                            Text(
                                text = " / 110",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp),
                            )
                        }
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.06f))

                // Career Progress with Simulation
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_grades_cfu_progress_projected),
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
                        backdropState = backdropState,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.06f))

                // Batch Presets Bar (Smooth Horizontal Scrollable Chips)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_all_30),
                        onClick = { onSetAllGrades(30) },
                        backdropState = backdropState,
                    )
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_all_28),
                        onClick = { onSetAllGrades(28) },
                        backdropState = backdropState,
                    )
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_all_24),
                        onClick = { onSetAllGrades(24) },
                        backdropState = backdropState,
                    )
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_current_average),
                        onClick = onApplyCurrentAverage,
                        backdropState = backdropState,
                    )
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_reset),
                        onClick = onResetSimulation,
                        backdropState = backdropState,
                    )
                }
            }
        }

        // Section: Esami da Simulare
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.ui_grades_exams_to_simulate_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.ui_grades_exams_to_simulate_subtitle, uiState.simulationItems.size, uiState.activeSimulatedCount),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }

        if (uiState.simulationItems.isEmpty()) {
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(20.dp),
                contentPadding = 24.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.MenuBook,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_grades_no_residual_exams),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.simulationItems.forEach { item ->
                    SimulationExamCard(
                        item = item,
                        currentWeightedAverage = uiState.currentAverage,
                        currentTotalCfu = uiState.totalCurrentCfu,
                        onToggle = { onToggleItem(item.id) },
                        onGradeChange = { newGrade -> onGradeChange(item.id, newGrade) },
                        onRemove = if (item.isCustom) { { onRemoveCustomExam(item.id) } } else null,
                        backdropState = backdropState,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulationExamCard(
    item: SimulationItem,
    currentWeightedAverage: Double,
    currentTotalCfu: Int,
    onToggle: () -> Unit,
    onGradeChange: (Int) -> Unit,
    onRemove: (() -> Unit)?,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isIncluded = item.isEnabled

    // Calculate individual exam impact on average
    val itemImpact = if (currentTotalCfu > 0) {
        val gradeToUse = if (item.grade >= 31) 30 else item.grade
        val newAvg = ((currentWeightedAverage * currentTotalCfu) + (gradeToUse * item.cfu)) / (currentTotalCfu + item.cfu)
        newAvg - currentWeightedAverage
    } else 0.0

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        onClick = onToggle,
        colors = if (isIncluded) {
            LiquidCardDefaults.colors(
                containerColor = colorScheme.primary.copy(alpha = 0.08f)
            )
        } else {
            LiquidCardDefaults.colors()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Main Top Row
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
                        imageVector = if (item.isCustom) LiquidIcons.Star else if (isIncluded) LiquidIcons.Check else LiquidIcons.MenuBook,
                        contentDescription = null,
                        tint = if (isIncluded) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 40.dp,
                            iconSize = 20.dp,
                            containerColor = if (isIncluded) colorScheme.primary.copy(alpha = 0.14f) else colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                        ),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = item.name,
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
                                text = "${item.cfu} CFU",
                                color = if (isIncluded) colorScheme.primary else colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                            )
                            if (isIncluded) {
                                Text(
                                    text = "•",
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                )
                                Text(
                                    text = stringResource(Res.string.ui_grades_impact_format, if (itemImpact >= 0) "+" else "", itemImpact.toFixedTwoDecimals()),
                                    color = if (itemImpact >= 0) colorScheme.primary else colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isIncluded) {
                        LiquidBadge(
                            text = if (item.grade >= 31) "30L" else "${item.grade}",
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.primary,
                            backdropState = backdropState,
                        )
                    } else {
                        LiquidBadge(
                            text = stringResource(Res.string.ui_grades_simulate_badge),
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = colorScheme.onSurfaceVariant,
                            backdropState = backdropState,
                        )
                    }

                    if (onRemove != null) {
                        LiquidIconButton(
                            icon = LiquidIcons.Delete,
                            onClick = onRemove,
                            backdropState = backdropState,
                        )
                    }
                }
            }

            // Expanded Controls when active
            AnimatedVisibility(
                visible = isIncluded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                    // Compact Stepper Control Row
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            LiquidIconButton(
                                icon = LiquidIcons.Remove,
                                onClick = {
                                    if (item.grade > 18) onGradeChange(item.grade - 1)
                                },
                                backdropState = backdropState,
                            )

                            Box(
                                modifier = Modifier
                                    .size(width = 56.dp, height = 36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (item.grade >= 31) "30L" else "${item.grade}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.grade >= 30) colorScheme.primary else colorScheme.onSurface,
                                )
                            }

                            LiquidIconButton(
                                icon = LiquidIcons.Add,
                                onClick = {
                                    if (item.grade < 31) onGradeChange(item.grade + 1)
                                },
                                backdropState = backdropState,
                            )
                        }
                    }

                    // Quick Vote Pills Bar (Smooth Horizontal Scroll)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(18, 20, 22, 24, 26, 27, 28, 30, 31).forEach { gradeOption ->
                            val isCurrent = item.grade == gradeOption
                            val label = if (gradeOption == 31) "30L 🏆" else "$gradeOption"

                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(Capsule())
                                    .background(
                                        if (isCurrent) colorScheme.primary
                                        else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { onGradeChange(gradeOption) }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) colorScheme.onPrimary else colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Obiettivo e Calcolatore Target di Laurea.
 */
@Composable
fun GraduationTargetTab(
    uiState: GradesUiState,
    onTargetDegreeChange: (Int) -> Unit,
    onThesisPointsChange: (Int) -> Unit,
    onBonusPointsChange: (Int) -> Unit,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    val isFeasible = uiState.isTargetFeasible
    val feasibility = uiState.feasibilityLevel
    val remainingCfu = uiState.remainingDegreeCfu

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Hero Calculation Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Top row with feasibility badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_grades_graduation_target_header),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        letterSpacing = 0.8.sp,
                        fontSize = 10.sp,
                    )

                    val (badgeText, badgeBg, badgeFg) = when (feasibility) {
                        TargetFeasibility.EASY -> Triple(stringResource(Res.string.ui_grades_feasibility_easy), colorScheme.primaryContainer, colorScheme.primary)
                        TargetFeasibility.FEASIBLE -> Triple(stringResource(Res.string.ui_grades_feasibility_feasible), colorScheme.secondaryContainer, colorScheme.secondary)
                        TargetFeasibility.HARD -> Triple(stringResource(Res.string.ui_grades_feasibility_hard), colorScheme.errorContainer.copy(alpha = 0.5f), colorScheme.error)
                        TargetFeasibility.IMPOSSIBLE -> Triple(stringResource(Res.string.ui_grades_feasibility_impossible), colorScheme.errorContainer, colorScheme.error)
                    }

                    LiquidBadge(
                        text = badgeText,
                        containerColor = badgeBg,
                        contentColor = badgeFg,
                        backdropState = backdropState,
                    )
                }

                // Dual KPI: Media Richiesta vs Target Finale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.ui_grades_required_average),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.requiredRemainingAverage > 30.0) "> 30" else uiState.requiredRemainingAverage.toFixedTwoDecimals(),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isFeasible) colorScheme.onSurface else colorScheme.error,
                            )
                            Text(
                                text = " / 30",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp),
                            )
                        }
                        Text(
                            text = stringResource(Res.string.ui_grades_cfu_remaining_format, remainingCfu),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(Res.string.ui_grades_final_target),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (uiState.targetDegree >= 111) "110L" else "${uiState.targetDegree}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.primary,
                            )
                            Text(
                                text = " / 110",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp),
                            )
                        }
                        Text(
                            text = stringResource(Res.string.ui_grades_needed_base_format, uiState.neededGraduationBase.toFixedTwoDecimals()),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.06f))

                // Explanatory Guidance Notice
                val statusText = when {
                    remainingCfu == 0 -> {
                        val currentFinal = uiState.currentGraduationBase + uiState.thesisPoints + uiState.bonusPoints
                        if (currentFinal >= uiState.targetScoreEffective) "Carriera completata! Con ${uiState.thesisPoints + uiState.bonusPoints} punti bonus raggiungi l'obiettivo di ${uiState.targetDegree}/110."
                        else "Carriera completata. Con ${uiState.thesisPoints + uiState.bonusPoints} punti bonus il tuo punteggio stimato è ${currentFinal.toFixedTwoDecimals()}/110."
                    }
                    feasibility == TargetFeasibility.EASY -> "Traguardo ampiamente alla portata: ti basta mantenere una media di ${uiState.requiredRemainingAverage.toFixedTwoDecimals()} sui restanti $remainingCfu CFU (uguale o inferiore alla tua media attuale di ${uiState.currentAverage.toFixedTwoDecimals()})."
                    feasibility == TargetFeasibility.FEASIBLE -> "Obiettivo raggiungibile con impegno: ti occorre una media del ${uiState.requiredRemainingAverage.toFixedTwoDecimals()} nei restanti $remainingCfu CFU."
                    feasibility == TargetFeasibility.HARD -> "Molto impegnativo: serve una media quasi perfetta (${uiState.requiredRemainingAverage.toFixedTwoDecimals()}). Puoi alzare la stima dei punti tesi se previsto."
                    else -> "Obiettivo non matematicamente possibile con i parametri correnti (media richiesta > 30). Valuta di incrementare la stima dei punti tesi o bonus carriera."
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isFeasible) LiquidIcons.Check else LiquidIcons.Warning,
                        contentDescription = null,
                        tint = if (isFeasible) colorScheme.primary else colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                // 3 Pillars Overview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GraduationPillarTile(
                        label = stringResource(Res.string.ui_grades_current_base),
                        value = "${uiState.currentGraduationBase.toFixedTwoDecimals()}",
                        subvalue = "/ 110",
                        backdropState = backdropState,
                        modifier = Modifier.weight(1f),
                    )
                    GraduationPillarTile(
                        label = stringResource(Res.string.ui_grades_thesis_bonus),
                        value = "+${uiState.thesisPoints + uiState.bonusPoints}",
                        subvalue = "pt",
                        backdropState = backdropState,
                        modifier = Modifier.weight(1f),
                    )
                    GraduationPillarTile(
                        label = stringResource(Res.string.ui_grades_current_estimate),
                        value = "${(uiState.currentGraduationBase + uiState.thesisPoints + uiState.bonusPoints).toFixedTwoDecimals()}",
                        subvalue = "/ 110",
                        backdropState = backdropState,
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

        // Target Final Score Selector (Chips + Stepper)
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(22.dp),
            contentPadding = 16.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(Res.string.ui_grades_target_degree_vote),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )

                // Quick Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(
                        100 to "100 / 110",
                        105 to "105 / 110",
                        110 to "110 / 110",
                        111 to "110 e Lode 🏆",
                    ).forEach { (targetVal, label) ->
                        LiquidChip(
                            label = label,
                            selected = uiState.targetDegree == targetVal,
                            onClick = { onTargetDegreeChange(targetVal) },
                            backdropState = backdropState,
                        )
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                // Custom Target Stepper Row
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LiquidIconButton(
                            icon = LiquidIcons.Remove,
                            onClick = {
                                val current = if (uiState.targetDegree >= 111) 110 else uiState.targetDegree
                                if (current > 66) onTargetDegreeChange(current - 1)
                            },
                            backdropState = backdropState,
                        )

                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (uiState.targetDegree >= 111) "110L" else "${uiState.targetDegree}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                            )
                        }

                        LiquidIconButton(
                            icon = LiquidIcons.Add,
                            onClick = {
                                val current = if (uiState.targetDegree >= 111) 110 else uiState.targetDegree
                                if (current < 110) onTargetDegreeChange(current + 1)
                            },
                            backdropState = backdropState,
                        )
                    }
                }
            }
        }

        // Punti Tesi Stepper (Standalone Glass Stepper)
        LiquidStepper(
            value = uiState.thesisPoints,
            onValueChange = onThesisPointsChange,
            minValue = 0,
            maxValue = 15,
            label = stringResource(Res.string.ui_grades_thesis_points_label),
            unit = "pt",
            backdropState = backdropState,
        )

        // Bonus Carriera Stepper (Standalone Glass Stepper)
        LiquidStepper(
            value = uiState.bonusPoints,
            onValueChange = onBonusPointsChange,
            minValue = 0,
            maxValue = 10,
            label = stringResource(Res.string.ui_grades_career_bonus_label),
            unit = "pt",
            backdropState = backdropState,
        )
    }
}

/**
 * Tab 2: Grafici e Andamento Storico Preservato e Ottimizzato.
 */
@Composable
fun SimulationChartTab(
    uiState: GradesUiState,
    backdropState: Backdrop,
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
        // Section 1: Evoluzione Voti & Medie
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_stats_evolution_title),
            subtitle = stringResource(Res.string.ui_grades_chart_evolution_sub),
        )

        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 18.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (uiState.singleGradeEntries.isNotEmpty()) {
                    LiquidLineChart(
                        entries = uiState.singleGradeEntries,
                        weightedAverageEntries = uiState.weightedAverageEntries,
                        arithmeticAverageEntries = uiState.arithmeticAverageEntries,
                        height = 210.dp,
                        backdropState = backdropState,
                        minValue = uiState.gradeMin,
                        maxValue = uiState.gradeMax,
                        showLegend = true,
                        primarySeriesLabel = stringResource(Res.string.ui_stats_series_grade),
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.ui_grades_no_chart_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
            }
        }

        // Section 2: Performance KPIs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(22.dp),
                contentPadding = 14.dp,
                modifier = Modifier.weight(1f),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Star,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = stringResource(Res.string.ui_stats_highest_grade),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = if (uiState.highestGrade >= 31) "30L" else if (uiState.highestGrade > 0) "${uiState.highestGrade}" else "—",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary,
                    )
                    Text(
                        text = stringResource(Res.string.ui_grades_honors_count_format, uiState.lodeCount),
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(22.dp),
                contentPadding = 14.dp,
                modifier = Modifier.weight(1f),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Analytics,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = stringResource(Res.string.ui_stats_arithmetic_average),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = if (uiState.currentArithmeticAverage > 0.0) uiState.currentArithmeticAverage.toFixedTwoDecimals() else "—",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_grades_recorded_exams_count, uiState.currentExams.size),
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Section 3: Distribuzione Crediti per Materia
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_stats_cfu_distribution_title),
            subtitle = stringResource(Res.string.ui_grades_cfu_chart_subtitle),
        )

        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(26.dp),
            contentPadding = 20.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.cfuEntries.isNotEmpty()) {
                    LiquidBarChart(
                        entries = uiState.cfuEntries,
                        height = 195.dp,
                        backdropState = backdropState,
                        maxValue = uiState.cfuMax,
                        valueSuffix = "CFU",
                        detailDescription = stringResource(Res.string.ui_grades_cfu_chart_detail),
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.ui_grades_no_cfu_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
            }
        }

        // Section 4: Ripartizione Fasce di Voto
        if (donutEntries.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_stats_grade_tiers_title),
                subtitle = stringResource(Res.string.ui_stats_grade_tiers_sub),
            )

            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(26.dp),
                contentPadding = 20.dp,
            ) {
                LiquidDonutChart(
                    entries = donutEntries,
                    size = 190.dp,
                    centerLabel = stringResource(Res.string.ui_exams),
                    centerValue = uiState.currentExams.size.toString(),
                    backdropState = backdropState,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                donutEntries.forEach { entry ->
                    val tierRatio = if (uiState.currentExams.isNotEmpty()) entry.value / uiState.currentExams.size else 0f
                    val tierPercentage = (tierRatio * 100).toInt()
                    val tierColor = entry.color ?: colorScheme.primary

                    LiquidCard(
                        backdropState = backdropState,
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = 14.dp,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                            .size(12.dp)
                                            .clip(Capsule())
                                            .background(tierColor),
                                    )
                                    Text(
                                        text = stringResource(Res.string.ui_grades_tier_band_format, entry.label),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface,
                                    )
                                }

                                Text(
                                    text = "${entry.value.toInt()} esami • $tierPercentage%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(Capsule())
                                    .background(colorScheme.onSurface.copy(alpha = 0.08f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(tierRatio.coerceIn(0f, 1f))
                                        .height(4.dp)
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

/**
 * Modal Dialog for adding a custom mock exam to the simulation.
 */
@Composable
fun AddCustomExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit,
    backdropState: Backdrop,
) {
    var name by remember { mutableStateOf("") }
    var cfu by remember { mutableIntStateOf(6) }
    var grade by remember { mutableIntStateOf(28) }

    LiquidDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.ui_grades_add_custom_exam_title),
        backdropState = backdropState,
        confirmButton = {
            LiquidButton(
                onClick = { onConfirm(name, cfu, grade) },
                variant = LiquidButtonVariant.Primary,
                backdropState = backdropState,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.ui_add), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            LiquidButton(
                onClick = onDismiss,
                variant = LiquidButtonVariant.Glass,
                backdropState = backdropState,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.ui_cancel))
            }
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
                backdropState = backdropState,
            )

            LiquidStepper(
                value = cfu,
                onValueChange = { cfu = it },
                minValue = 1,
                maxValue = 30,
                label = stringResource(Res.string.ui_grades_custom_exam_cfu),
                unit = "CFU",
                backdropState = backdropState,
            )

            LiquidStepper(
                value = grade,
                onValueChange = { grade = it },
                minValue = 18,
                maxValue = 30,
                label = stringResource(Res.string.ui_grades_custom_exam_grade),
                unit = "/30",
                backdropState = backdropState,
            )
        }
    }
}

@Composable
private fun GraduationPillarTile(
    label: String,
    value: String,
    subvalue: String,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface.copy(alpha = 0.65f))
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = subvalue,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 1.dp),
                )
            }
        }
    }
}

fun Double.toFixedTwoDecimals(): String {
    val scaled = round(this * 100.0).toLong()
    val magnitude = abs(scaled)
    val sign = if (scaled < 0) "-" else ""
    val fraction = (magnitude % 100).toString().padStart(2, '0')
    return "$sign${magnitude / 100}.$fraction"
}
