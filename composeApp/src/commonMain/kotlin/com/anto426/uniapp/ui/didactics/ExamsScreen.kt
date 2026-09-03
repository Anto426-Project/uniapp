package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.ExamsUiState
import com.anto426.uniapp.ui.components.items.ExamSessionItem
import com.anto426.uniapp.ui.components.items.ProfessorExamItem
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.unisdk.backend.model.ProfessorContentItem
import com.kyant.backdrop.Backdrop

@Composable
fun ExamsScreen(
    backdropState: Backdrop,
    uiState: ExamsUiState,
    onTabSelected: (Int) -> Unit,
    onToggleBooking: (String) -> Unit,
    onProfessorExamClick: (ProfessorContentItem) -> Unit = {},
) {
    val studentTabs = listOf(
        LiquidNavigationItem(
            stringResource(Res.string.ui_bookable_exams),
            badge = uiState.bookableCount.toString(),
            icon = LiquidIcons.Calendar,
        ),
        LiquidNavigationItem(
            stringResource(Res.string.ui_booked_exams),
            badge = uiState.bookedCount.toString(),
            icon = LiquidIcons.Check,
        )
    )
    UniScreenLazyColumn {
        if (!uiState.isProfessor) {
            item(key = "exam-tabs") {
                LiquidTabBar(
                    items = studentTabs,
                    selectedIndex = uiState.selectedTab,
                    onTabSelected = onTabSelected,
                    backdropState = backdropState,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        val isEmpty =
            if (uiState.isProfessor) uiState.visibleProfessorExamRounds.isEmpty()
            else uiState.visibleExams.isEmpty()
        if (isEmpty) {
            item(key = "exam-empty-${uiState.selectedTab}") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_no_exams_in_section),
                    description = stringResource(Res.string.ui_no_exams_in_section_description),
                    backdropState = backdropState,
                )
            }
        }
        if (uiState.isProfessor) {
            itemsIndexed(
                items = uiState.visibleProfessorExamRounds,
                key = { index, exam -> "professor-exam|${exam.id}|${exam.title}|$index" },
            ) { _, exam ->
                ProfessorExamItem(
                    exam = exam,
                    backdropState = backdropState,
                    onClickDetail = { onProfessorExamClick(exam) },
                )
            }
        } else {
            itemsIndexed(
                items = uiState.visibleExams,
                key = { index, exam ->
                    "exam|${exam.id}|${exam.date}|${exam.time}|$index"
                },
            ) { _, exam ->
                ExamSessionItem(
                    exam = exam,
                    backdropState = backdropState,
                    isMutating = uiState.mutatingExamId == exam.id,
                    onToggleBooking = { onToggleBooking(exam.id) },
                )
            }
        }
    }
}
