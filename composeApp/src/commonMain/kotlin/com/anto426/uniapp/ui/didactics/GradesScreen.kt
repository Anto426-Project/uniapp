package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.model.didactics.toFixedTwoDecimals
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
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
        // 1. Tab Selector (3 crisp balanced tabs, persistent for liquid click animation)
        LiquidTabBar(
            items = tabs,
            selectedIndex = activeTabIndex,
            onTabSelected = { index ->
                if (index == 0) includeSimulatedAnalytics = false
                onTabSelected(index)
            },
        )

        // 2. Tab Content
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
                    UniHorizontalChipRow {
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
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val trendValue = chart.recentTrend
    val trendLabel = when {
        chart.currentExams.size < 2 -> "—"
        trendValue >= 0f -> "+${trendValue.toDouble().toFixedTwoDecimals()}"
        else -> trendValue.toDouble().toFixedTwoDecimals()
    }
    val highestGradeLabel = when {
        chart.highestGrade >= 31 -> "30L"
        chart.highestGrade > 0 -> "${chart.highestGrade}"
        else -> "—"
    }
    val deltaBadge = if (projected) {
        val sign = if (deltaAverage >= 0.0) "+" else ""
        "$sign${deltaAverage.toFixedTwoDecimals()}"
    } else {
        stringResource(Res.string.ui_career_exams_count, chart.currentExams.size)
    }

    val deltaColor = when {
        !projected -> colorScheme.primary
        deltaAverage >= 0.0 -> glassColors.success
        else -> colorScheme.error
    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Analytics,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 36.dp,
                            iconSize = 18.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedRectangle(11.dp),
                        ),
                    )
                    Text(
                        text = stringResource(
                            if (projected) Res.string.ui_grades_projected
                            else Res.string.ui_grades_official,
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                LiquidBadge(
                    text = deltaBadge,
                    containerColor = deltaColor.copy(alpha = 0.14f),
                    contentColor = deltaColor,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_weighted_average),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (chart.currentAverage > 0) chart.currentAverage.toFixedTwoDecimals() else "—",
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
                        text = stringResource(Res.string.ui_graduation_base),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (chart.currentGraduationBase > 0) chart.currentGraduationBase.toFixedTwoDecimals() else "—",
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

            if (chart.highestGradeCourses.isNotBlank()) {
                Text(
                    text = stringResource(
                        Res.string.ui_grades_highest_grade_courses_format,
                        chart.highestGradeCourses,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                GradesMetricTile(
                    label = stringResource(Res.string.ui_stats_arithmetic_average),
                    value = if (chart.currentArithmeticAverage > 0.0) chart.currentArithmeticAverage.toFixedTwoDecimals() else "—",
                    valueColor = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                GradesMetricTile(
                    label = stringResource(Res.string.ui_stats_highest_grade),
                    value = highestGradeLabel,
                    valueColor = colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                GradesMetricTile(
                    label = stringResource(Res.string.ui_stats_recent_trend),
                    value = trendLabel,
                    valueColor = when {
                        chart.currentExams.size < 2 -> colorScheme.onSurfaceVariant
                        trendValue >= 0f -> glassColors.success
                        else -> colorScheme.error
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
