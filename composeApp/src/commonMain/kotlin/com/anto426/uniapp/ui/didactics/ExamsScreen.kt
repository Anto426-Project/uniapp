package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.ExamsUiState
import com.anto426.uniapp.ui.components.items.ExamSessionItem
import com.anto426.uniapp.ui.components.items.ProfessorExamItem
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.anto426.unisdk.model.ProfessorContentItem
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
    onProfessorExamClick: (ProfessorContentItem) -> Unit = {},
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

    UniScreenLazyColumn {
        if (!uiState.isProfessor && showTabs) {
            item(key = "exam-tabs") {
                LiquidTabBar(
                    items = studentTabs,
                    selectedIndex = uiState.selectedTab,
                    onTabSelected = onTabSelected,
                )
            }
        }

        if (uiState.isProfessor) {
            professorExamsContentItems(
                rounds = uiState.visibleProfessorExamRounds,
                onProfessorExamClick = onProfessorExamClick,
            )
        } else {
            studentExamsContentItems(
                uiState = uiState,
                bookableFilter = bookableFilter,
                onBookableFilterChange = { bookableFilter = it },
                onToggleBooking = onToggleBooking,
                onAddToCalendar = onAddToCalendar,
            )
        }
    }
}

fun LazyListScope.professorExamsContentItems(
    rounds: List<ProfessorContentItem>,
    onProfessorExamClick: (ProfessorContentItem) -> Unit,
) {
    if (rounds.isEmpty()) {
        item(key = "professor-exam-empty") {
            UniExpressiveStatusState(
                title = stringResource(Res.string.ui_no_exams_in_section),
                description = stringResource(Res.string.ui_no_exams_in_section_description),
                inline = true,
                mainIcon = LiquidIcons.Calendar,
            )
        }
    } else {
        item(key = "professor-exams-header") {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_professor_exam_rounds),
                subtitle = stringResource(
                    Res.string.ui_professor_rounds_count,
                    rounds.size,
                ),
            )
        }
        itemsIndexed(
            items = rounds,
            key = { index, exam -> "professor-exam|${exam.id}|${exam.title}|$index" },
        ) { _, exam ->
            ProfessorExamItem(
                exam = exam,
                onClickDetail = { onProfessorExamClick(exam) },
            )
        }
    }
}

fun LazyListScope.studentExamsContentItems(
    uiState: ExamsUiState,
    bookableFilter: ExamBookableFilter,
    onBookableFilterChange: (ExamBookableFilter) -> Unit,
    onToggleBooking: (String) -> Unit,
    onAddToCalendar: (String) -> Unit,
) {
    val openNowExamsCount = uiState.exams.count { !it.isBooked && it.canBook }
    val openingSoonExamsCount = uiState.exams.count { !it.isBooked && !it.canBook }

    val displayedStudentExams = if (uiState.selectedTab == 0) {
        when (bookableFilter) {
            ExamBookableFilter.ALL -> uiState.visibleExams
            ExamBookableFilter.OPEN_NOW -> uiState.visibleExams.filter { it.canBook }
            ExamBookableFilter.OPENING_SOON -> uiState.visibleExams.filter { !it.canBook }
        }
    } else {
        uiState.visibleExams
    }

    if (uiState.selectedTab == 0 && uiState.visibleExams.isNotEmpty()) {
        item(key = "student-exam-filters") {
            val filterOptions = listOf(
                Triple(
                    ExamBookableFilter.ALL,
                    stringResource(Res.string.ui_exams_all_filter),
                    uiState.visibleExams.size,
                ),
                Triple(
                    ExamBookableFilter.OPEN_NOW,
                    stringResource(Res.string.ui_exams_open_filter),
                    openNowExamsCount,
                ),
                Triple(
                    ExamBookableFilter.OPENING_SOON,
                    stringResource(Res.string.ui_exams_opening_soon_filter),
                    openingSoonExamsCount,
                ),
            )
            UniHorizontalChipRow {
                filterOptions.forEach { (filterOption, label, count) ->
                    key(filterOption) {
                        val selected = bookableFilter == filterOption
                        LiquidChip(
                            label = "$label ($count)",
                            selected = selected,
                            leadingIcon = if (selected) LiquidIcons.Check else null,
                            onClick = { onBookableFilterChange(filterOption) },
                        )
                    }
                }
            }
        }
    }

    if (displayedStudentExams.isEmpty()) {
        item(key = "exam-empty-${uiState.selectedTab}-$bookableFilter") {
            UniExpressiveStatusState(
                title = stringResource(Res.string.ui_no_exams_in_section),
                description = stringResource(Res.string.ui_no_exams_in_section_description),
                inline = true,
                mainIcon = LiquidIcons.Calendar,
            )
        }
    } else {
        item(key = "student-exams-header-${uiState.selectedTab}") {
            LiquidSectionHeader(
                title = if (uiState.selectedTab == 1) {
                    stringResource(Res.string.ui_booked_exams)
                } else {
                    stringResource(Res.string.ui_bookable_exams)
                },
                subtitle = stringResource(
                    Res.string.ui_professor_rounds_count,
                    displayedStudentExams.size,
                ),
            )
        }
        itemsIndexed(
            items = displayedStudentExams,
            key = { index, exam -> "exam|${exam.id}|${exam.date}|${exam.time}|$index" },
        ) { _, exam ->
            ExamSessionItem(
                exam = exam,
                isMutating = uiState.mutatingExamId != null,
                onToggleBooking = { onToggleBooking(exam.id) },
                onAddToCalendar = { onAddToCalendar(exam.id) },
            )
        }
    }
}
