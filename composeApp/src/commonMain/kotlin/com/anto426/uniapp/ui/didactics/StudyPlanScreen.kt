package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.uniapp.didactics.presentation.ALL_ACADEMIC_YEARS
import com.anto426.uniapp.didactics.presentation.StudyPlanUiState
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.data.contract.firstAcademicIntegerOrNull
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.items.StudyCourseItem
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.anto426.uniapp.ui.didactics.components.CareerStatusFilterRow
import com.anto426.uniapp.ui.didactics.components.CareerYearChipRow
import com.anto426.uniapp.ui.didactics.components.careerYearLabel
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun StudyPlanContent(
    uiState: StudyPlanUiState,
    onYearSelected: (Int) -> Unit,
    onCourseClick: (StudyCourse) -> Unit = {},
    onStatusFilterChanged: (CourseStatus?) -> Unit = {},
) {
    val yearNumbers = uiState.years.map { it.yearNumber }
    val selectedYearNumber = if (uiState.selectedYearIndex == ALL_ACADEMIC_YEARS) {
        ALL_ACADEMIC_YEARS
    } else {
        uiState.years.getOrNull(uiState.selectedYearIndex)?.yearNumber ?: 0
    }

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (yearNumbers.isNotEmpty()) {
            CareerYearChipRow(
                years = yearNumbers,
                selectedYear = selectedYearNumber,
                includeAllChip = yearNumbers.size > 1,
                onYearSelected = { targetYear ->
                    if (targetYear == ALL_ACADEMIC_YEARS) {
                        onYearSelected(ALL_ACADEMIC_YEARS)
                    } else {
                        val index = uiState.years.indexOfFirst { it.yearNumber == targetYear }
                        if (index >= 0) onYearSelected(index)
                    }
                },
            )
        }

        CareerStatusFilterRow(
            selectedStatus = uiState.statusFilter,
            onStatusSelected = onStatusFilterChanged,
        )

        UniContentHost(
            targetState = selectedYearNumber to uiState.statusFilter,
            modifier = Modifier.fillMaxWidth(),
            isForward = { from, to ->
                if (from.first != to.first) to.first >= from.first
                else (to.second?.ordinal ?: -1) >= (from.second?.ordinal ?: -1)
            },
            label = "StudyPlanFilterSwitcher",
        ) { (targetYear, targetStatus) ->
            val targetDisplayedYears =
                if (targetYear == ALL_ACADEMIC_YEARS) {
                    uiState.years
                } else {
                    uiState.years.filter { it.yearNumber == targetYear }
                }
            val yearResults = targetDisplayedYears.map { yearGroup ->
                val filteredCourses = when (targetStatus) {
                    null -> yearGroup.courses
                    CourseStatus.COMPLETED -> yearGroup.courses.filter { it.status == CourseStatus.COMPLETED }
                    CourseStatus.ACTIVE, CourseStatus.PLANNED -> yearGroup.courses.filter { it.status != CourseStatus.COMPLETED }
                }
                yearGroup to filteredCourses
            }
            val anyYearHasMatches = yearResults.any { it.second.isNotEmpty() }

            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                yearResults.forEachIndexed { yearIndex, (yearGroup, filteredCourses) ->
                    if (targetDisplayedYears.size > 1 && anyYearHasMatches && filteredCourses.isEmpty()) {
                        return@forEachIndexed
                    }
                    val totalYearCfu = yearGroup.courses.sumOf { course -> course.cfu.firstAcademicIntegerOrNull() ?: 0 }
                    val completedCount = yearGroup.courses.count { it.status == CourseStatus.COMPLETED }

                    LiquidSectionHeader(
                        title = careerYearLabel(yearGroup.yearNumber),
                        subtitle = stringResource(
                            Res.string.ui_study_plan_year_summary,
                            totalYearCfu,
                            completedCount,
                            yearGroup.courses.size,
                        ),
                    )

                    if (filteredCourses.isNotEmpty()) {
                        UniInsetGroup(entranceOrder = uniListEntranceOrder(yearIndex)) {
                            filteredCourses.forEachIndexed { index, course ->
                                StudyCourseItem(
                                    course = course,
                                    showDivider = index < filteredCourses.lastIndex,
                                    onClick = { onCourseClick(course) },
                                )
                            }
                        }
                    } else {
                        UniExpressiveStatusState(
                            title = stringResource(Res.string.ui_career_filter_empty),
                            description = stringResource(Res.string.ui_career_filter_empty_desc),
                            inline = true,
                        )
                    }
                }
            }
        }
    }
}
