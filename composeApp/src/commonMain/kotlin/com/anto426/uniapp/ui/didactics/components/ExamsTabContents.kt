package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.AcademicContentItem
import com.anto426.uniapp.didactics.presentation.ExamsUiState
import com.anto426.uniapp.ui.components.items.ExamSessionItem
import com.anto426.uniapp.ui.components.items.ProfessorExamItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.anto426.uniapp.ui.didactics.ExamBookableFilter
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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
    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (tabIndex == 0 && tabExams.isNotEmpty()) {
            val filterOptions = listOf(
                ExamBookableFilter.ALL to stringResource(Res.string.ui_exams_all_filter),
                ExamBookableFilter.OPEN_NOW to stringResource(Res.string.ui_exams_open_filter),
                ExamBookableFilter.OPENING_SOON to stringResource(Res.string.ui_exams_opening_soon_filter),
            )
            UniHorizontalChipRow {
                filterOptions.forEach { (filterOption, label) ->
                    key(filterOption) {
                        val selected = bookableFilter == filterOption
                        LiquidChip(
                            label = label,
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
