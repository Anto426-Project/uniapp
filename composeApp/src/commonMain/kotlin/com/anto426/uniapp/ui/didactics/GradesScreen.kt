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
import com.anto426.uniapp.navigation.model.GradesDetailSection
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.didactics.components.GradesOverviewTab
import com.anto426.uniapp.ui.didactics.components.GradesSimulatorTab
import com.anto426.uniapp.ui.didactics.components.GradesTargetTab
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
    onOpenDetail: (GradesDetailSection, Boolean) -> Unit = { _, _ -> },
) {
    var includeSimulatedScope by rememberSaveable { mutableStateOf(false) }

    val activeTabIndex = uiState.selectedTab.coerceIn(0, 2)
    val hasActiveSimulation = uiState.activeSimulatedCount > 0
    val effectiveProjected = hasActiveSimulation && includeSimulatedScope

    val tabs = listOf(
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_grades_real),
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_grades_tab_simula),
            badge = uiState.activeSimulatedCount.takeIf { it > 0 }?.toString(),
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_grades_tab_obiettivo),
        ),
    )

    UniScreenColumn {
        LiquidTabBar(
            items = tabs,
            selectedIndex = activeTabIndex,
            onTabSelected = onTabSelected,
        )

        UniTabContentHost(
            selectedTab = activeTabIndex,
            tabCount = tabs.size,
            onTabSelected = onTabSelected,
            swipeEnabled = false,
            label = "GradesTabs",
        ) { tab ->
            when (tab) {
                1 -> GradesSimulatorTab(
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

                2 -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (hasActiveSimulation) {
                        GradesScopeChipRow(
                            projected = effectiveProjected,
                            onProjectedChange = { includeSimulatedScope = it },
                        )
                    }

                    UniContentHost(
                        targetState = effectiveProjected,
                        modifier = Modifier.fillMaxWidth(),
                        transition = UniMotion.inlineContentTransition,
                        label = "GradesTargetScope",
                    ) { projected ->
                        GradesTargetTab(
                            uiState = uiState.chartState(projected),
                            onTargetDegreeChange = onTargetDegreeChanged,
                            onThesisPointsChange = onThesisPointsChanged,
                            onBonusPointsChange = onBonusPointsChanged,
                        )
                    }
                }

                else -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (hasActiveSimulation) {
                        GradesScopeChipRow(
                            projected = effectiveProjected,
                            onProjectedChange = { includeSimulatedScope = it },
                        )
                    }

                    UniContentHost(
                        targetState = effectiveProjected,
                        modifier = Modifier.fillMaxWidth(),
                        transition = UniMotion.inlineContentTransition,
                        label = "GradesOverviewScope",
                    ) { projected ->
                        GradesOverviewTab(
                            chart = uiState.chartState(projected),
                            projected = projected,
                            deltaAverage = uiState.deltaAverage,
                            onOpenDetail = { section -> onOpenDetail(section, projected) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradesScopeChipRow(
    projected: Boolean,
    onProjectedChange: (Boolean) -> Unit,
) {
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
                    onClick = { onProjectedChange(isProjected) },
                )
            }
        }
    }
}
