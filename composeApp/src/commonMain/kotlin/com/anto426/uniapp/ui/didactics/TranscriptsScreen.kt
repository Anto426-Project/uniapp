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
import com.anto426.uniapp.didactics.presentation.TranscriptsUiState
import com.anto426.uniapp.data.contract.ExamRecord
import com.anto426.uniapp.data.contract.firstAcademicIntegerOrNull
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.items.ExamRecordItem
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.anto426.uniapp.ui.didactics.components.CareerYearChipRow
import com.anto426.uniapp.ui.didactics.components.careerYearLabel
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TranscriptsContent(
    uiState: TranscriptsUiState,
    onYearSelected: (Int) -> Unit,
    onExamClick: (ExamRecord) -> Unit = {},
    @Suppress("UNUSED_PARAMETER")
    onOpenGrades: () -> Unit = {},
) {
    val years = uiState.availableYears

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (years.isNotEmpty()) {
            CareerYearChipRow(
                years = years,
                selectedYear = uiState.selectedYear,
                onYearSelected = onYearSelected,
                includeAllChip = years.size > 1,
            )
        }

        UniContentHost(
            targetState = uiState.selectedYear,
            modifier = Modifier.fillMaxWidth(),
            isForward = { from, to -> to >= from },
            label = "TranscriptsYearSwitcher",
        ) { targetYear ->
            val targetDisplayedYears =
                if (targetYear == ALL_ACADEMIC_YEARS) years else listOf(targetYear)
            val showSingleEmptyYear = targetDisplayedYears.size == 1 ||
                targetDisplayedYears.none { uiState.examsByYear[it].orEmpty().isNotEmpty() }

            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                targetDisplayedYears.forEachIndexed { yearIndex, year ->
                    val examsForYear = uiState.examsByYear[year].orEmpty()
                    val yearTitle = careerYearLabel(year)

                    if (examsForYear.isNotEmpty()) {
                        val yearCfu = examsForYear.sumOf { exam -> exam.cfu.firstAcademicIntegerOrNull() ?: 0 }
                        LiquidSectionHeader(
                            title = yearTitle,
                            subtitle = stringResource(Res.string.ui_transcripts_year_summary, yearCfu, examsForYear.size),
                        )

                        UniInsetGroup(entranceOrder = uniListEntranceOrder(yearIndex)) {
                            examsForYear.forEachIndexed { index, exam ->
                                ExamRecordItem(
                                    exam = exam,
                                    showDivider = index < examsForYear.lastIndex,
                                    onClick = { if (exam.code.isNotBlank()) onExamClick(exam) },
                                )
                            }
                        }
                    } else if (showSingleEmptyYear) {
                        LiquidSectionHeader(
                            title = yearTitle,
                            subtitle = stringResource(Res.string.ui_transcripts_year_summary, 0, 0),
                        )
                        UniExpressiveStatusState(
                            title = stringResource(Res.string.ui_grades_verbalized_empty),
                            description = stringResource(Res.string.ui_history_empty_desc),
                            inline = true,
                        )
                    }
                }
            }
        }
    }
}
