package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.didactics.components.GradesActions
import com.anto426.uniapp.ui.didactics.components.GradesAnalyticsHeroCard
import com.anto426.uniapp.ui.didactics.components.GraduationTargetTab
import com.anto426.uniapp.ui.didactics.components.SimulationChartTab
import com.anto426.uniapp.ui.didactics.components.SimulationTab
import com.anto426.uniapp.ui.motion.UniMotion
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

        UniTabContentHost(
            selectedTab = activeTabIndex,
            tabCount = tabs.size,
            onTabSelected = { index ->
                if (index == 0) includeSimulatedAnalytics = false
                onTabSelected(index)
            },
            swipeEnabled = false,
            label = "GradesTabs",
        ) { tab ->
            when (tab) {
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

                else -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
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

                    UniContentHost(
                        targetState = showProjectedAnalytics,
                        modifier = Modifier.fillMaxWidth(),
                        transition = UniMotion.inlineContentTransition,
                        label = "GradesAnalyticsScope",
                    ) { projected ->
                        val chart = uiState.chartState(projected)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            UniAnimatedEntrance(order = 0) {
                                GradesAnalyticsHeroCard(
                                    chart = chart,
                                    projected = projected,
                                    deltaAverage = uiState.deltaAverage,
                                )
                            }

                            UniAnimatedEntrance(order = 1) {
                                SimulationChartTab(uiState = chart)
                            }
                        }
                    }
                }
            }
        }
    }
}
