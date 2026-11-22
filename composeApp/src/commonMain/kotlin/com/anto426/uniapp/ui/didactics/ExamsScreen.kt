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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.AcademicContentItem
import com.anto426.uniapp.didactics.presentation.ExamsUiState
import com.anto426.uniapp.ui.components.items.ExamSessionItem
import com.anto426.uniapp.ui.components.items.ProfessorExamItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
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

@Composable
fun ProfessorExamsTabContent(
    rounds: List<AcademicContentItem>,
    onProfessorExamClick: (AcademicContentItem) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (rounds.isEmpty()) {
            UniExpressiveStatusState(
                title = stringResource(Res.string.ui_no_exams_in_section),
                description = stringResource(Res.string.ui_no_exams_in_section_description),
                inline = true,
                mainIcon = LiquidIcons.Calendar,
            )
        } else {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_professor_exam_rounds),
                subtitle = stringResource(
                    Res.string.ui_professor_rounds_count,
                    rounds.size,
                ),
            )
            rounds.forEachIndexed { index, exam ->
                key("professor-exam|${exam.id}|${exam.title}|$index") {
                    UniAnimatedEntrance(order = uniListEntranceOrder(index)) {
                        ProfessorExamItem(
                            exam = exam,
                            onClickDetail = { onProfessorExamClick(exam) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentExamsTabContent(
    uiState: ExamsUiState,
    tabIndex: Int,
    bookableFilter: ExamBookableFilter,
    onBookableFilterChange: (ExamBookableFilter) -> Unit,
    onToggleBooking: (String) -> Unit,
    onAddToCalendar: (String) -> Unit,
) {
    val tabExams = uiState.exams.filter { it.isBooked == (tabIndex == 1) }
    val openNowExamsCount = uiState.exams.count { !it.isBooked && it.canBook }
    val openingSoonExamsCount = uiState.exams.count { !it.isBooked && !it.canBook }

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (tabIndex == 0 && tabExams.isNotEmpty()) {
            val filterOptions = listOf(
                Triple(
                    ExamBookableFilter.ALL,
                    stringResource(Res.string.ui_exams_all_filter),
                    tabExams.size,
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

        val effectiveFilter = if (tabIndex == 0) bookableFilter else ExamBookableFilter.ALL
        UniContentHost(
            targetState = effectiveFilter,
            modifier = Modifier.fillMaxWidth(),
            isForward = { from, to -> to.ordinal >= from.ordinal },
            label = "StudentExamsFilter",
        ) { activeFilter ->
            val displayedStudentExams = if (tabIndex == 0) {
                when (activeFilter) {
                    ExamBookableFilter.ALL -> tabExams
                    ExamBookableFilter.OPEN_NOW -> tabExams.filter { it.canBook }
                    ExamBookableFilter.OPENING_SOON -> tabExams.filter { !it.canBook }
                }
            } else {
                tabExams
            }

            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (displayedStudentExams.isEmpty()) {
                    UniExpressiveStatusState(
                        title = stringResource(Res.string.ui_no_exams_in_section),
                        description = stringResource(Res.string.ui_no_exams_in_section_description),
                        inline = true,
                        mainIcon = LiquidIcons.Calendar,
                    )
                } else {
                    LiquidSectionHeader(
                        title = if (tabIndex == 1) {
                            stringResource(Res.string.ui_booked_exams)
                        } else {
                            stringResource(Res.string.ui_bookable_exams)
                        },
                        subtitle = stringResource(
                            Res.string.ui_professor_rounds_count,
                            displayedStudentExams.size,
                        ),
                    )
                    displayedStudentExams.forEachIndexed { index, exam ->
                        key("exam|${exam.id}|${exam.date}|${exam.time}|$index") {
                            UniAnimatedEntrance(order = uniListEntranceOrder(index)) {
                                ExamSessionItem(
                                    exam = exam,
                                    isMutating = uiState.mutatingExamId != null,
                                    onToggleBooking = { onToggleBooking(exam.id) },
                                    onAddToCalendar = { onAddToCalendar(exam.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
