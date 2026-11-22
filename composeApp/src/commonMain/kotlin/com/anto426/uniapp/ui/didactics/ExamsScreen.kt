package com.anto426.uniapp.ui.didactics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.AcademicContentItem
import com.anto426.uniapp.didactics.presentation.ExamsUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.didactics.components.ProfessorExamsTabContent
import com.anto426.uniapp.ui.didactics.components.StudentExamsTabContent
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

enum class ExamBookableFilter {
    ALL,
    OPEN_NOW,
    OPENING_SOON,
}

@Composable
fun ExamsScreen(
    uiState: ExamsUiState,
    onTabSelected: (Int) -> Unit,
    showTabs: Boolean = true,
    onToggleBooking: (String) -> Unit,
    onProfessorExamClick: (AcademicContentItem) -> Unit = {},
    onAddToCalendar: (String) -> Unit,
) {
    var bookableFilter by rememberSaveable { mutableStateOf(ExamBookableFilter.ALL) }

    val studentTabs = listOf(
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_bookable_exams),
            badge = uiState.bookableCount.takeIf { it > 0 }?.toString(),
            icon = LiquidIcons.Calendar,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_booked_exams),
            badge = uiState.bookedCount.takeIf { it > 0 }?.toString(),
            icon = LiquidIcons.Check,
        ),
    )

    UniScreenColumn {
        if (!uiState.isProfessor && showTabs) {
            LiquidTabBar(
                items = studentTabs,
                selectedIndex = uiState.selectedTab.coerceIn(0, studentTabs.lastIndex),
                onTabSelected = onTabSelected,
            )
        }

        if (uiState.isProfessor) {
            ProfessorExamsTabContent(
                rounds = uiState.visibleProfessorExamRounds,
                onProfessorExamClick = onProfessorExamClick,
            )
        } else {
            UniTabContentHost(
                selectedTab = uiState.selectedTab,
                tabCount = studentTabs.size,
                onTabSelected = if (showTabs) onTabSelected else null,
                label = "ExamsScreenTabs",
            ) { tab ->
                StudentExamsTabContent(
                    uiState = uiState,
                    tabIndex = tab,
                    bookableFilter = bookableFilter,
                    onBookableFilterChange = { bookableFilter = it },
                    onToggleBooking = onToggleBooking,
                    onAddToCalendar = onAddToCalendar,
                )
            }
        }
    }
}
