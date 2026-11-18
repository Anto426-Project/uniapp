package com.anto426.uniapp.ui.didactics


import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.GraduationTargetTab
import com.anto426.uniapp.ui.didactics.components.SimulationChartTab
import com.anto426.uniapp.ui.didactics.components.SimulationTab
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import com.anto426.uniapp.model.didactics.toFixedTwoDecimals

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
    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_grades_real), icon = LiquidIcons.Analytics),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_tab_simula), icon = LiquidIcons.Edit),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_tab_obiettivo), icon = LiquidIcons.MenuBook),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_projection), icon = LiquidIcons.Analytics),
    )

    UniScreenColumn {
        // 1. Tab Selector (scrolls naturally with screen content)
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedTab,
            onTabSelected = onTabSelected,
        )

        // 2. Tab Content
        when (uiState.selectedTab) {
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
                val projected = uiState.selectedTab == 3
                val chart = uiState.chartState(projected)
                Text(
                    text = stringResource(if (projected) Res.string.ui_grades_projected_description else Res.string.ui_grades_real_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                LiquidPreferenceGroup(title = stringResource(if (projected) Res.string.ui_grades_projected else Res.string.ui_grades_official)) {
                    LiquidPreferenceItem(title = stringResource(Res.string.ui_weighted_average),
                        subtitle = if (chart.currentAverage > 0) chart.currentAverage.toFixedTwoDecimals() else "—")
                    LiquidPreferenceItem(title = stringResource(Res.string.ui_graduation_base),
                        subtitle = if (chart.currentGraduationBase > 0) chart.currentGraduationBase.toFixedTwoDecimals() else "—")
                    LiquidPreferenceItem(title = stringResource(Res.string.ui_stats_highest_grade),
                        subtitle = chart.highestGradeCourses.ifBlank { "—" })
                    LiquidPreferenceItem(title = stringResource(Res.string.ui_grades_recent_trend),
                        subtitle = chart.recentTrend.toDouble().toFixedTwoDecimals())
                }
                SimulationChartTab(uiState = chart)
            }
        }
    }
}
