package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.toFixedTwoDecimals
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.didactics.presentation.GradesViewModel
import com.anto426.uniapp.didactics.presentation.TargetFeasibility
import com.anto426.uniapp.ui.components.display.UniBlockDefaults
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun GradesTargetTab(
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
    val scenarios = uiState.graduationScenarios()

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 1. Unified Feasibility Hero Card
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                            maxLines = 1,
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = when {
                                    remainingCfu == 0 -> gradeMetric(finalEstimate)
                                    uiState.requiredRemainingAverage > 30.0 -> ">30"
                                    else -> gradeMetric(uiState.requiredRemainingAverage)
                                },
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 38.sp,
                                    lineHeight = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1.4).sp,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = if (feasibility == TargetFeasibility.IMPOSSIBLE) colorScheme.error else colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text = if (remainingCfu == 0) "/ 110" else "/ 30",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp),
                                maxLines = 1,
                            )
                        }

                        if (remainingCfu > 0) {
                            Text(
                                text = "${stringResource(Res.string.ui_grades_cfu_remaining_format, remainingCfu)} · ${
                                    stringResource(Res.string.ui_grades_needed_base_format, uiState.neededGraduationBase.toFixedTwoDecimals())
                                }",
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    UniCircularProgressRing(
                        progress = ringProgress,
                        centerText = targetLabel,
                        subtitleText = "/110",
                        size = 78.dp,
                        strokeWidth = 6.dp,
                        primaryColor = feasibilityColor,
                        secondaryColor = colorScheme.primary,
                    )
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    color = colorScheme.onSurfaceVariant,
                )

                LiquidHorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GradesCompactStat(
                        value = gradeMetric(uiState.currentGraduationBase),
                        label = stringResource(Res.string.ui_grades_current_base),
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesVerticalHairline()
                    GradesCompactStat(
                        value = "+$addedPoints pt",
                        label = stringResource(Res.string.ui_grades_thesis_bonus),
                        valueColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    GradesVerticalHairline()
                    GradesCompactStat(
                        value = "${gradeMetric(finalEstimate)}/110",
                        label = stringResource(Res.string.ui_grades_current_estimate),
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 2. Direct-Manipulation Parameters Card (1:1 LiquidSliders + quick target presets)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_grades_customize_params_title),
                subtitle = stringResource(Res.string.ui_grades_customize_params_sub),
            )
            UniInsetGroup(
                contentPadding = UniBlockDefaults.ContentPadding,
                animateEntrance = false,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradesSliderParameter(
                            label = stringResource(Res.string.ui_grades_target_degree_vote),
                            formattedValue = targetLabel,
                            value = uiState.targetDegree,
                            minValue = GradesViewModel.MIN_TARGET_DEGREE,
                            maxValue = GradesViewModel.MAX_TARGET_DEGREE,
                            onValueChange = onTargetDegreeChange,
                            accentColor = feasibilityColor,
                        )
                        GradesPresetStrip(
                            values = quickTargetDegreePresets,
                            selectedValue = uiState.targetDegree,
                            labelFor = ::graduationGradeLabel,
                            onSelect = onTargetDegreeChange,
                        )
                    }

                    LiquidHorizontalDivider()

                    GradesSliderParameter(
                        label = stringResource(Res.string.ui_grades_thesis_points_label),
                        formattedValue = "+${uiState.thesisPoints} pt",
                        value = uiState.thesisPoints,
                        minValue = 0,
                        maxValue = GradesViewModel.MAX_THESIS_POINTS,
                        onValueChange = onThesisPointsChange,
                        accentColor = colorScheme.primary,
                    )

                    LiquidHorizontalDivider()

                    GradesSliderParameter(
                        label = stringResource(Res.string.ui_grades_career_bonus_label),
                        formattedValue = "+${uiState.bonusPoints} pt",
                        value = uiState.bonusPoints,
                        minValue = 0,
                        maxValue = GradesViewModel.MAX_BONUS_POINTS,
                        onValueChange = onBonusPointsChange,
                        accentColor = colorScheme.tertiary,
                    )
                }
            }
        }

        // 3. Future Scenarios Standardized Inset List
        if (scenarios.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_grades_scenarios_title),
                    subtitle = stringResource(Res.string.ui_grades_scenarios_subtitle),
                )
                UniInsetGroup(animateEntrance = false) {
                    scenarios.forEachIndexed { index, scenario ->
                        val rowTitle = scenario.averageGrade?.let { avg ->
                            stringResource(Res.string.ui_grades_scenario_average, avg)
                        } ?: stringResource(Res.string.ui_grades_current)
                        val badgeTint = if (scenario.meetsTarget) glassColors.success else colorScheme.onSurfaceVariant

                        UniInsetRow(
                            title = rowTitle,
                            subtitle = "${stringResource(Res.string.ui_weighted_average)}: ${gradeMetric(scenario.projectedAverage)}",
                            icon = if (scenario.meetsTarget) LiquidIcons.Check else LiquidIcons.Analytics,
                            iconTint = if (scenario.meetsTarget) glassColors.success else colorScheme.primary,
                            showDivider = index < scenarios.lastIndex,
                            trailingContent = {
                                LiquidBadge(
                                    text = "${scenario.projectedScore.toFixedTwoDecimals()}/110",
                                    containerColor = badgeTint.copy(alpha = 0.14f),
                                    contentColor = if (scenario.meetsTarget) glassColors.success else colorScheme.onSurface,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
