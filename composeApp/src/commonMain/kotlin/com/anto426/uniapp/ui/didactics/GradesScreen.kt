package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.GradesActions
import com.anto426.uniapp.ui.didactics.components.GradesMetricGrid
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.anto426.uniapp.ui.didactics.components.gradeMetric
import com.anto426.uniapp.ui.didactics.components.signedGradeMetric
import com.anto426.uniapp.ui.didactics.components.GraduationTargetTab
import com.anto426.uniapp.ui.didactics.components.SimulationChartTab
import com.anto426.uniapp.ui.didactics.components.SimulationTab
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun GradesScreen(
    uiState: GradesUiState,
    onTabSelected: (Int) -> Unit,
    onToggleSimulationItem: (String) -> Unit,
    onSimulatedGradeChanged: (String, Int) -> Unit,
    onSimulatedCfuChanged: (String, Int) -> Unit = { _, _ -> },
    onAddCustomExam: (String, Int, Int) -> Unit = { _, _, _ -> },
    onRemoveCustomExam: (String) -> Unit = {},
    onSetAllGrades: (Int) -> Unit = {},
    onApplyCurrentAverage: () -> Unit = {},
    onResetSimulation: () -> Unit = {},
    onTargetDegreeChanged: (Int) -> Unit = {},
    onThesisPointsChanged: (Int) -> Unit = {},
    onBonusPointsChanged: (Int) -> Unit = {},
) {
    var includeSimulatedAnalytics by rememberSaveable { mutableStateOf(false) }

    val activeTabIndex = when (uiState.selectedTab) {
        1 -> 1
        2 -> 2
        else -> 0
    }
    val showProjectedAnalytics = uiState.activeSimulatedCount > 0 &&
        (uiState.selectedTab == 3 || includeSimulatedAnalytics)

    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_grades_real)),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_grades_tab_simula),
            badge = uiState.activeSimulatedCount.takeIf { it > 0 }?.toString(),
        ),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_tab_obiettivo)),
    )

    UniScreenColumn {
        LiquidTabBar(
            items = tabs,
            selectedIndex = activeTabIndex,
            onTabSelected = { index ->
                if (index == 0) includeSimulatedAnalytics = false
                onTabSelected(index)
            },
        )

        when (activeTabIndex) {
            1 -> SimulationTab(
                uiState = uiState,
                onToggleItem = onToggleSimulationItem,
                onGradeChange = onSimulatedGradeChanged,
                onCfuChange = onSimulatedCfuChanged,
                onAddCustomExam = onAddCustomExam,
                onRemoveCustomExam = onRemoveCustomExam,
                onSetAllGrades = onSetAllGrades,
                onApplyCurrentAverage = onApplyCurrentAverage,
                onResetSimulation = onResetSimulation,
            )

            2 -> GraduationTargetTab(
                uiState = uiState,
                onTargetDegreeChange = onTargetDegreeChanged,
                onThesisPointsChange = onThesisPointsChanged,
                onBonusPointsChange = onBonusPointsChanged,
            )

            else -> {
                val chart = uiState.chartState(showProjectedAnalytics)

                if (uiState.activeSimulatedCount > 0) {
                    val scopeOptions = listOf(
                        false to stringResource(Res.string.ui_grades_scope_real),
                        true to "${stringResource(Res.string.ui_grades_projection)} (${uiState.activeSimulatedCount})",
                    )
                    GradesActions {
                        scopeOptions.forEach { (isProjected, label) ->
                            key(isProjected) {
                                val selected = showProjectedAnalytics == isProjected
                                LiquidChip(
                                    label = label,
                                    selected = selected,
                                    leadingIcon = if (selected) LiquidIcons.Check else null,
                                    onClick = {
                                        includeSimulatedAnalytics = isProjected
                                        if (uiState.selectedTab == 3 && !isProjected) {
                                            onTabSelected(0)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                GradesAnalyticsHeroCard(
                    chart = chart,
                    projected = showProjectedAnalytics,
                    deltaAverage = uiState.deltaAverage,
                )

                SimulationChartTab(uiState = chart)
            }
        }
    }
}

@Composable
private fun GradesAnalyticsHeroCard(
    chart: GradesUiState,
    projected: Boolean,
    deltaAverage: Double,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LiquidGlassTheme.colors
    val trend = chart.recentTrend.toDouble()
    val highest = when {
        chart.highestGrade >= 31 -> "30L"
        chart.highestGrade > 0 -> chart.highestGrade.toString()
        else -> "—"
    }
    val badgeText = if (projected) {
        stringResource(Res.string.ui_grades_simulation_delta, signedGradeMetric(deltaAverage))
    } else {
        stringResource(Res.string.ui_career_exams_count, chart.currentExams.size)
    }
    val accent = when {
        !projected -> colors.primary
        deltaAverage >= 0.0 -> glass.success
        else -> colors.error
    }

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidSectionHeader(
                title = stringResource(if (projected) Res.string.ui_grades_projection else Res.string.ui_grades_scope_real),
                trailingContent = {
                    LiquidBadge(
                        text = badgeText,
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = colors.onSurface,
                    )
                },
            )
            GradesMetricGrid(columns = 2) {
                GradesMetricTile(
                    label = stringResource(Res.string.ui_weighted_average),
                    value = gradeMetric(chart.currentAverage),
                    unit = "/30",
                    valueColor = colors.onSurface,
                    prominent = true,
                    modifier = Modifier.weight(1f),
                )
                GradesMetricTile(
                    label = stringResource(Res.string.ui_graduation_base),
                    value = gradeMetric(chart.currentGraduationBase),
                    unit = "/110",
                    valueColor = colors.primary,
                    prominent = true,
                    modifier = Modifier.weight(1f),
                )
            }
            LiquidHorizontalDivider(color = colors.outlineVariant)
            GradesMetricGrid(columns = 3) {
                GradesMetricTile(
                    label = stringResource(Res.string.ui_stats_arithmetic_average),
                    value = gradeMetric(chart.currentArithmeticAverage),
                    valueColor = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                GradesMetricTile(
                    label = stringResource(Res.string.ui_stats_highest_grade),
                    value = highest,
                    valueColor = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                GradesMetricTile(
                    label = stringResource(Res.string.ui_stats_recent_trend),
                    value = if (chart.currentExams.size < 2) "—" else signedGradeMetric(trend),
                    valueColor = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
            if (chart.highestGradeCourses.isNotBlank()) {
                Text(
                    text = stringResource(Res.string.ui_grades_highest_grade_courses_format, chart.highestGradeCourses),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}
