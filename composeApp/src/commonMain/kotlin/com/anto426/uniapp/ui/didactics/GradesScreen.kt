package com.anto426.uniapp.ui.didactics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.GradesUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.GraduationTargetTab
import com.anto426.uniapp.ui.didactics.components.SimulationChartTab
import com.anto426.uniapp.ui.didactics.components.SimulationTab
import com.kyant.backdrop.Backdrop

@Composable
fun GradesScreen(
    backdropState: Backdrop,
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
        LiquidNavigationItem(stringResource(Res.string.ui_grades_tab_simula), icon = LiquidIcons.Edit),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_tab_obiettivo), icon = LiquidIcons.MenuBook),
        LiquidNavigationItem(stringResource(Res.string.ui_grades_tab_grafici), icon = LiquidIcons.Analytics),
    )

    UniScreenColumn {
        // 1. Tab Selector (scrolls naturally with screen content)
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedTab,
            onTabSelected = onTabSelected,
            backdropState = backdropState,
        )

        // 2. Tab Content
        AnimatedContent(
            targetState = uiState.selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "gradesTabContent",
        ) { currentTab ->
            when (currentTab) {
                0 -> SimulationTab(
                    uiState = uiState,
                    onToggleItem = onToggleSimulationItem,
                    onGradeChange = onSimulatedGradeChanged,
                    onCfuChange = onSimulatedCfuChanged,
                    onAddCustomExam = onAddCustomExam,
                    onRemoveCustomExam = onRemoveCustomExam,
                    onSetAllGrades = onSetAllGrades,
                    onApplyCurrentAverage = onApplyCurrentAverage,
                    onResetSimulation = onResetSimulation,
                    backdropState = backdropState,
                )
                1 -> GraduationTargetTab(
                    uiState = uiState,
                    onTargetDegreeChange = onTargetDegreeChanged,
                    onThesisPointsChange = onThesisPointsChanged,
                    onBonusPointsChange = onBonusPointsChanged,
                    backdropState = backdropState,
                )
                else -> SimulationChartTab(
                    uiState = uiState,
                    backdropState = backdropState,
                )
            }
        }
    }
}
