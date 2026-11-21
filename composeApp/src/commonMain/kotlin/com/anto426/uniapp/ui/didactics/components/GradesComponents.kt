package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.layout.expandablecontent.LiquidExpandableContent
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.didactics.presentation.TargetFeasibility
import com.anto426.uniapp.model.didactics.SimulationItem
import com.anto426.unisdk.model.formatAcademicTwoDecimals as toFixedTwoDecimals
import com.anto426.uniapp.ui.components.display.UniBentoStatTile
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Simulatore interattivo di media ponderata, CFU e base di laurea.
 */
@OptIn(ExperimentalLayoutApi::class)
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
    val glass = LiquidGlassTheme.colors
    val cardShape = remember { RoundedRectangle(24.dp) }
    var isAddDialogOpen by rememberSaveable { mutableStateOf(false) }
    val hasScenario = uiState.activeSimulatedCount > 0
    val deltaAvg = uiState.deltaAverage
    val deltaBase = uiState.deltaGraduationBase
    val accent = when {
        !hasScenario -> colors.primary
        deltaAvg >= 0.0 -> glass.success
        else -> colors.error
    }

    val degreeProgress = (uiState.projectedGraduationBase / 110.0).toFloat().coerceIn(0f, 1f)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Live Impact Summary Hero Card (coerente con Andamento e Obiettivo)
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                if (hasScenario) Res.string.ui_grades_simulation_result else Res.string.ui_grades_current,
                            ).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = gradeMetric(uiState.projectedAverage),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = colors.onSurface,
                                maxLines = 1,
                            )
                            if (uiState.projectedAverage > 0.0) {
                                Text(
                                    text = "/30",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                        }

                        if (hasScenario) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                LiquidBadge(
                                    text = stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(deltaAvg)),
                                    containerColor = accent.copy(alpha = 0.14f),
                                    contentColor = accent,
                                )
                                LiquidBadge(
                                    text = "${signedGradeMetric(deltaBase)} pt",
                                    containerColor = colors.primary.copy(alpha = 0.13f),
                                    contentColor = colors.primary,
                                )
                            }
                            if (uiState.currentAverage > 0.0) {
                                Text(
                                    text = "${stringResource(Res.string.ui_grades_current)}: ${gradeMetric(uiState.currentAverage)}/30",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(Res.string.ui_grades_simulation_to_simulate_sub),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    UniCircularProgressRing(
                        progress = degreeProgress,
                        centerText = gradeMetric(uiState.projectedGraduationBase),
                        subtitleText = "/110",
                        size = 78.dp,
                        strokeWidth = 7.dp,
                        primaryColor = accent,
                        secondaryColor = colors.primary,
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
                            text = if (hasScenario) {
                                stringResource(
                                    Res.string.ui_grades_simulation_summary,
                                    uiState.activeSimulatedCount,
                                    uiState.activeSimulatedCfu,
                                )
                            } else {
                                stringResource(Res.string.ui_stats_study_plan_progress)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${uiState.totalProjectedCfu}/${uiState.degreeCfuTarget} CFU",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = accent,
                            maxLines = 1,
                        )
                    }
                    LiquidLinearProgressIndicator(
                        progress = uiState.careerProgress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // 2. Controls & Exam List
        val activeLabel = stringResource(
            if (uiState.activeSimulatedCount == 1) Res.string.ui_grades_active_singular
            else Res.string.ui_grades_active_plural,
            uiState.activeSimulatedCount,
        )
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_simulation_to_simulate),
            subtitle = if (uiState.simulationItems.isNotEmpty()) {
                stringResource(
                    Res.string.ui_grades_exams_to_simulate_subtitle,
                    uiState.simulationItems.size,
                    activeLabel,
                )
            } else {
                null
            },
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
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                leadingIcon = { Icon(LiquidIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
            )
            UniHorizontalChipRow {
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_all_30), onClick = { onSetAllGrades(30) })
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_all_28), onClick = { onSetAllGrades(28) })
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_all_24), onClick = { onSetAllGrades(24) })
                LiquidChip(label = stringResource(Res.string.ui_grades_preset_current_average), onClick = onApplyCurrentAverage)
                if (hasScenario) {
                    LiquidChip(
                        label = stringResource(Res.string.ui_grades_preset_reset),
                        leadingIcon = LiquidIcons.Refresh,
                        onClick = onResetSimulation,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val cardShape = remember { RoundedRectangle(20.dp) }
    val iconShape = remember { RoundedRectangle(12.dp) }
    val included = item.isEnabled
    val impact = projectWeightedAverage(currentWeightedAverage, currentTotalCfu, item.cfu, item.grade) - currentWeightedAverage
    val impactColor = if (impact >= 0.0) glass.success else colors.error
    val iconTint = when {
        item.isCustom -> colors.tertiary
        included -> colors.primary
        else -> colors.onSurfaceVariant
    }

    LiquidCard(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        shape = cardShape,
        contentPadding = PaddingValues(0.dp),
        clipContent = false,
    ) {
        Column(modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .liquidClickable(shape = cardShape, role = Role.Switch, onClick = onToggle)
                    .semantics(mergeDescendants = true) {}
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = when {
                        item.isCustom -> LiquidIcons.Star
                        included -> LiquidIcons.Check
                        else -> LiquidIcons.MenuBook
                    },
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 38.dp,
                        iconSize = 18.dp,
                        containerColor = iconTint.copy(alpha = 0.14f),
                        shape = iconShape,
                    ),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${item.cfu} CFU",
                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                            color = colors.onSurfaceVariant,
                        )
                        if (included && currentTotalCfu > 0) {
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                            Text(
                                text = stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(impact)),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = impactColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                if (included) {
                    LiquidBadge(
                        text = simulationGradeLabel(item.grade),
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer,
                    )
                }

                LiquidSwitch(
                    checked = included,
                    onCheckedChange = { onToggle() },
                )
            }

            LiquidExpandableContent(visible = included) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LiquidHorizontalDivider(color = colors.onSurface.copy(alpha = 0.08f))

                    CompactParameterRow(
                        label = stringResource(Res.string.ui_grades_custom_exam_grade),
                        formattedValue = simulationGradeLabel(item.grade),
                        value = item.grade,
                        onValueChange = onGradeChange,
                        minValue = 18,
                        maxValue = 31,
                    )

                    UniHorizontalChipRow(bleed = 14.dp, clipToBounds = true) {
                        listOf(18, 21, 24, 26, 27, 28, 29, 30, 31).forEach { option ->
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
                        LiquidHorizontalDivider(color = colors.onSurface.copy(alpha = 0.06f))
                        CompactParameterRow(
                            label = stringResource(Res.string.ui_grades_custom_exam_cfu),
                            formattedValue = "${item.cfu} CFU",
                            value = item.cfu,
                            onValueChange = onCfuChange,
                            minValue = 1,
                            maxValue = 30,
                        )
                    }

                    if (onRemove != null) {
                        LiquidButton(
                            text = stringResource(Res.string.ui_grades_remove_custom_exam),
                            onClick = onRemove,
                            containerColor = colors.errorContainer,
                            contentColor = colors.onErrorContainer,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            leadingIcon = { Icon(LiquidIcons.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
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
    val cardShape = remember { RoundedRectangle(24.dp) }

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
    val feasibilityIcon = when (feasibility) {
        TargetFeasibility.EASY -> LiquidIcons.Check
        TargetFeasibility.FEASIBLE -> LiquidIcons.Star
        TargetFeasibility.HARD -> LiquidIcons.Warning
        TargetFeasibility.IMPOSSIBLE -> LiquidIcons.Close
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
    val ringProgress = (finalEstimate / uiState.targetScoreEffective.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Target Feasibility Hero Card
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = feasibilityIcon,
                                contentDescription = null,
                                tint = feasibilityColor,
                                modifier = Modifier.size(15.dp),
                            )
                            LiquidBadge(
                                text = feasibilityLabel,
                                containerColor = feasibilityColor.copy(alpha = 0.14f),
                                contentColor = feasibilityColor,
                            )
                        }

                        Text(
                            text = stringResource(
                                if (remainingCfu > 0) Res.string.ui_grades_required_average
                                else Res.string.ui_grades_current_estimate,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = when {
                                    remainingCfu == 0 -> gradeMetric(finalEstimate)
                                    uiState.requiredRemainingAverage > 30.0 -> ">30"
                                    else -> gradeMetric(uiState.requiredRemainingAverage)
                                },
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = if (feasibility == TargetFeasibility.IMPOSSIBLE) colorScheme.error else colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text = if (remainingCfu > 0) "/30" else "/110",
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }

                        if (remainingCfu > 0) {
                            Text(
                                text = stringResource(Res.string.ui_grades_cfu_remaining_format, remainingCfu),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    UniCircularProgressRing(
                        progress = ringProgress,
                        centerText = targetLabel,
                        subtitleText = stringResource(Res.string.ui_grades_final_target),
                        size = 78.dp,
                        strokeWidth = 7.dp,
                        primaryColor = feasibilityColor,
                        secondaryColor = colorScheme.primary,
                    )
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )

                if (remainingCfu > 0) {
                    Text(
                        text = stringResource(Res.string.ui_grades_needed_base_format, uiState.neededGraduationBase.toFixedTwoDecimals()),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = colorScheme.primary,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompactBreakdownCell(
                        label = stringResource(Res.string.ui_grades_current_base),
                        value = gradeMetric(uiState.currentGraduationBase),
                        modifier = Modifier.weight(1f),
                    )
                    CompactBreakdownCell(
                        label = stringResource(Res.string.ui_grades_thesis_bonus),
                        value = "+$addedPoints pt",
                        valueColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    CompactBreakdownCell(
                        label = stringResource(Res.string.ui_grades_current_estimate),
                        value = "${gradeMetric(finalEstimate)}/110",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 2. Clean Full-Width Parameters Card (with 1-tap target presets)
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_grades_customize_params_title),
            subtitle = stringResource(Res.string.ui_grades_customize_params_sub),
        )
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactParameterRow(
                        label = stringResource(Res.string.ui_grades_target_degree_vote),
                        formattedValue = targetLabel,
                        value = uiState.targetDegree,
                        onValueChange = onTargetDegreeChange,
                        minValue = 66,
                        maxValue = 111,
                    )
                    UniHorizontalChipRow(bleed = 18.dp, clipToBounds = true) {
                        listOf(100, 105, 108, 110, 111).forEach { preset ->
                            key(preset) {
                                LiquidChip(
                                    label = graduationGradeLabel(preset),
                                    selected = uiState.targetDegree == preset,
                                    onClick = { onTargetDegreeChange(preset) },
                                )
                            }
                        }
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                CompactParameterRow(
                    label = stringResource(Res.string.ui_grades_thesis_points_label),
                    formattedValue = "+${uiState.thesisPoints} pt",
                    value = uiState.thesisPoints,
                    onValueChange = onThesisPointsChange,
                    minValue = 0,
                    maxValue = 15,
                )

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                CompactParameterRow(
                    label = stringResource(Res.string.ui_grades_career_bonus_label),
                    formattedValue = "+${uiState.bonusPoints} pt",
                    value = uiState.bonusPoints,
                    onValueChange = onBonusPointsChange,
                    minValue = 0,
                    maxValue = 10,
                )
            }
        }

        // 3. Concrete Future Scenarios Card (when CFU remain)
        if (remainingCfu > 0) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_grades_scenarios_title),
                subtitle = stringResource(Res.string.ui_grades_scenarios_subtitle),
            )
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val currentPaceRow = uiState.currentAverage.takeIf { it > 0.0 }?.let { avg ->
                        Triple(
                            stringResource(Res.string.ui_grades_current),
                            avg,
                            (uiState.currentGraduationBase + addedPoints).coerceAtMost(113.0),
                        )
                    }
                    val fixedRows = listOf(24, 27, 30).map { futureAvg ->
                        val projectedAvg = projectWeightedAverage(
                            baseAverage = uiState.currentAverage,
                            baseCfu = uiState.totalCurrentCfu,
                            addedCfu = remainingCfu,
                            addedGrade = futureAvg,
                        )
                        val projectedBase = uiState.currentGraduationBase + (projectedAvg - uiState.currentAverage) * 110.0 / 30.0
                        val projectedScore = (projectedBase + addedPoints).coerceAtMost(113.0)
                        Triple(
                            stringResource(Res.string.ui_grades_scenario_average, futureAvg),
                            projectedAvg,
                            projectedScore,
                        )
                    }
                    val allRows = listOfNotNull(currentPaceRow) + fixedRows
                    allRows.forEachIndexed { index, (title, projectedAvg, projectedScore) ->
                        if (index > 0) {
                            LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                        val meetsTarget = projectedScore >= uiState.targetScoreEffective
                        val badgeTint = if (meetsTarget) glassColors.success else colorScheme.onSurfaceVariant

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
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${stringResource(Res.string.ui_weighted_average)}: ${gradeMetric(projectedAvg)}/30",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (meetsTarget) {
                                    Icon(
                                        imageVector = LiquidIcons.Check,
                                        contentDescription = null,
                                        tint = glassColors.success,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                LiquidBadge(
                                    text = "${projectedScore.toFixedTwoDecimals()}/110",
                                    containerColor = badgeTint.copy(alpha = 0.14f),
                                    contentColor = if (meetsTarget) glassColors.success else colorScheme.onSurface,
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
 * Analisi strutturata del rendimento: KPI reali, ripartizione fasce di voto leggibile
 * e un solo grafico focalizzato sull'andamento cronologico (senza grafici ridondanti).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulationChartTab(
    uiState: GradesUiState,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val cardShape = remember { RoundedRectangle(24.dp) }

    val tierColors = listOf(
        colorScheme.primary,
        colorScheme.tertiary,
        colorScheme.secondary,
        colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Concrete Academic Insights (2x2 Bento Grid — real info upfront)
        if (uiState.currentExams.isNotEmpty()) {
            val highestLabel = if (uiState.highestGrade > 0) simulationGradeLabel(uiState.highestGrade) else "—"
            val highestSubtitle = uiState.highestGradeCourses.takeIf(String::isNotBlank)
                ?.let { stringResource(Res.string.ui_grades_highest_grade_courses_format, it) }
            val honorsSubtitle = if (uiState.lodeCount > 0) {
                stringResource(Res.string.ui_grades_honors_count_format, uiState.lodeCount)
            } else {
                stringResource(Res.string.ui_grades_recorded_exams_count, uiState.currentExams.size)
            }
            val trendVal = uiState.recentTrend.toDouble()
            val trendDisplay = if (uiState.currentExams.size < 2) {
                "—"
            } else if (trendVal >= 0.0) {
                stringResource(Res.string.ui_stats_trend_points_positive, trendVal.toFixedTwoDecimals())
            } else {
                stringResource(Res.string.ui_stats_trend_points_negative, trendVal.toFixedTwoDecimals())
            }
            val trendAccent = when {
                uiState.currentExams.size < 2 -> colorScheme.onSurfaceVariant
                trendVal >= 0.0 -> glassColors.success
                else -> colorScheme.error
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    UniBentoStatTile(
                        label = stringResource(Res.string.ui_stats_highest_grade),
                        value = highestLabel,
                        suffix = highestSubtitle,
                        icon = LiquidIcons.Star,
                        accentColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    UniBentoStatTile(
                        label = stringResource(Res.string.ui_stats_passed_exams),
                        value = uiState.currentExams.size.toString(),
                        suffix = honorsSubtitle,
                        icon = LiquidIcons.Check,
                        accentColor = colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    UniBentoStatTile(
                        label = stringResource(Res.string.ui_stats_cfu_summary_title),
                        value = "${uiState.totalCurrentCfu}/${uiState.degreeCfuTarget}",
                        suffix = if (uiState.remainingDegreeCfu > 0) {
                            stringResource(Res.string.ui_grades_cfu_remaining_format, uiState.remainingDegreeCfu)
                        } else {
                            "${(uiState.careerProgress * 100f).roundToInt()}%"
                        },
                        icon = LiquidIcons.MenuBook,
                        accentColor = colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                    )
                    UniBentoStatTile(
                        label = stringResource(Res.string.ui_stats_recent_trend),
                        value = trendDisplay,
                        suffix = stringResource(Res.string.ui_stats_recent_trend_desc),
                        icon = LiquidIcons.Refresh,
                        accentColor = trendAccent,
                        valueColor = trendAccent,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 2. Structured Grade Tier Distribution (replaces unlabeled Donut Chart with exact counts & %)
        if (uiState.gradeTiers.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_stats_grade_tiers_title),
                subtitle = stringResource(Res.string.ui_stats_grade_tiers_sub),
            )

            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Proportional stacked bar for instant part-to-whole overview
                    val totalCount = uiState.gradeTiers.sumOf { it.examCount.toDouble() }.toFloat().coerceAtLeast(1f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedRectangle(5.dp))
                            .background(colorScheme.onSurface.copy(alpha = 0.08f)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        uiState.gradeTiers.forEachIndexed { index, tier ->
                            if (tier.examCount > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight((tier.examCount / totalCount).coerceAtLeast(0.02f))
                                        .fillMaxHeight()
                                        .background(tierColors[index % tierColors.size]),
                                )
                            }
                        }
                    }

                    // Exact tabular breakdown rows
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.gradeTiers.forEachIndexed { index, tier ->
                            val tierColor = tierColors[index % tierColors.size]
                            val examCount = tier.examCount.roundToInt()
                            val ratio = (tier.percentage / 100f).coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(tierColor),
                                        )
                                        Text(
                                            text = stringResource(Res.string.ui_grades_tier_band_format, tier.label),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurface,
                                        )
                                    }
                                    Text(
                                        text = stringResource(Res.string.ui_grades_tier_exams_percent, examCount, tier.percentage),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFeatureSettings = "tnum",
                                        ),
                                        color = if (examCount > 0) tierColor else colorScheme.onSurfaceVariant,
                                    )
                                }
                                LiquidLinearProgressIndicator(
                                    progress = ratio,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    // Compact CFU weight pill summary (replaces heavy 190dp CFU bar chart)
                    if (uiState.cfuEntries.isNotEmpty()) {
                        LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(Res.string.ui_stats_cfu_distribution_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                            )
                            val examSingular = stringResource(Res.string.ui_stats_exam_singular)
                            val examsPlural = stringResource(Res.string.ui_stats_exams_plural)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                uiState.cfuEntries.forEach { entry ->
                                    val count = entry.value.roundToInt()
                                    val unitLabel = if (count == 1) examSingular else examsPlural
                                    LiquidBadge(
                                        text = "${entry.label} · $count $unitLabel",
                                        containerColor = colorScheme.primary.copy(alpha = 0.11f),
                                        contentColor = colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Single Focused Trend Chart (the one high-signal time-series visualization)
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_stats_evolution_title),
            subtitle = stringResource(Res.string.ui_grades_chart_evolution_sub),
        )

        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            contentPadding = PaddingValues(18.dp),
        ) {
            if (uiState.singleGradeEntries.size >= 2) {
                LiquidLineChart(
                    entries = uiState.singleGradeEntries,
                    weightedAverageEntries = uiState.weightedAverageEntries,
                    arithmeticAverageEntries = uiState.arithmeticAverageEntries,
                    height = 200.dp,
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
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                )
            }
        }
    }
}

@Composable
private fun CompactParameterRow(
    label: String,
    formattedValue: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        GradesCompactStepper(
            label = label,
            formattedValue = formattedValue,
            value = value,
            onValueChange = onValueChange,
            minValue = minValue,
            maxValue = maxValue,
        )
    }
}

@Composable
private fun GradesCompactStepper(
    label: String,
    formattedValue: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
) {
    val colors = MaterialTheme.colorScheme
    val canDecrease = value > minValue
    val canIncrease = value < maxValue
    val decreaseDesc = stringResource(Res.string.ui_grades_decrease_value, label)
    val increaseDesc = stringResource(Res.string.ui_grades_increase_value, label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer(clip = false)
                .liquidClickable(
                    shape = CircleShape,
                    enabled = canDecrease,
                    role = Role.Button,
                    onClick = { if (canDecrease) onValueChange(value - 1) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colors.onSurface.copy(alpha = if (canDecrease) 0.08f else 0.03f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LiquidIcons.Remove,
                    contentDescription = decreaseDesc,
                    tint = colors.onSurface.copy(alpha = if (canDecrease) 0.85f else 0.30f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Text(
            text = formattedValue,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Black,
                fontFeatureSettings = "tnum",
            ),
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 52.dp),
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer(clip = false)
                .liquidClickable(
                    shape = CircleShape,
                    enabled = canIncrease,
                    role = Role.Button,
                    onClick = { if (canIncrease) onValueChange(value + 1) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (canIncrease) colors.primaryContainer else colors.onSurface.copy(alpha = 0.03f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LiquidIcons.Add,
                    contentDescription = increaseDesc,
                    tint = if (canIncrease) colors.onPrimaryContainer else colors.onSurface.copy(alpha = 0.30f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun CompactBreakdownCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
            color = valueColor,
            maxLines = 1,
        )
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
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            LiquidButton(
                text = stringResource(Res.string.ui_cancel),
                onClick = onDismiss,
                variant = LiquidButtonVariant.Text,
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
            CompactParameterRow(
                label = stringResource(Res.string.ui_grades_custom_exam_cfu),
                formattedValue = "$cfu CFU",
                value = cfu,
                onValueChange = { cfu = it },
                minValue = 1,
                maxValue = 30,
            )
            CompactParameterRow(
                label = stringResource(Res.string.ui_grades_custom_exam_grade),
                formattedValue = simulationGradeLabel(grade),
                value = grade,
                onValueChange = { grade = it },
                minValue = 18,
                maxValue = 31,
            )
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(18.dp),
                contentPadding = PaddingValues(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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
                            valueColor = colors.primary,
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
