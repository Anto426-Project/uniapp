package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.uniapp.didactics.presentation.TranscriptsUiState
import com.anto426.uniapp.model.didactics.ExamRecord
import com.anto426.uniapp.model.didactics.firstAcademicIntegerOrNull
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.items.ExamRecordItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.CareerSummaryHeroCard
import com.anto426.uniapp.ui.didactics.components.CareerYearChipRow
import com.anto426.uniapp.ui.didactics.components.careerYearLabel
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TranscriptsScreen(
    uiState: TranscriptsUiState,
    onYearSelected: (Int) -> Unit,
    onExamClick: (ExamRecord) -> Unit = {},
    onOpenGrades: () -> Unit = {},
) {
    UniScreenColumn {
        TranscriptsContent(
            uiState = uiState,
            onYearSelected = onYearSelected,
            onExamClick = onExamClick,
            onOpenGrades = onOpenGrades,
        )
    }
}

@Composable
fun TranscriptsContent(
    uiState: TranscriptsUiState,
    onYearSelected: (Int) -> Unit,
    onExamClick: (ExamRecord) -> Unit = {},
    onOpenGrades: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val years = uiState.availableYears

    val metaPills = buildList {
        if (uiState.degreeBaseLabel.isNotBlank() && uiState.degreeBaseLabel != "—") {
            add(stringResource(Res.string.ui_career_degree_base_short, uiState.degreeBaseLabel) to colorScheme.primary)
        }
        add(stringResource(Res.string.ui_career_exams_count, uiState.totalExamsCount) to colorScheme.secondary)
        if (uiState.lodeCount > 0) {
            add(stringResource(Res.string.ui_career_lodi_count, uiState.lodeCount) to colorScheme.tertiary)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CareerSummaryHeroCard(
            kicker = stringResource(Res.string.ui_career_summary_libretto),
            headlineValue = uiState.averageLabel,
            headlineUnit = stringResource(Res.string.ui_career_average_unit),
            metaPills = metaPills,
            acquiredCfu = uiState.acquiredCfu,
            targetCfu = uiState.targetCfu,
            footerLabel = stringResource(Res.string.ui_career_open_stats),
            onFooterClick = onOpenGrades,
        )

        if (years.isNotEmpty()) {
            CareerYearChipRow(
                years = years,
                selectedYear = uiState.selectedYear,
                onYearSelected = onYearSelected,
            )
        }

        uiState.displayedYears.forEach { year ->
            val examsForYear = uiState.examsByYear[year].orEmpty()
            val yearTitle = careerYearLabel(year)

            if (examsForYear.isNotEmpty()) {
                val yearCfu = examsForYear.sumOf { exam -> exam.cfu.firstAcademicIntegerOrNull() ?: 0 }
                LiquidSectionHeader(
                    title = yearTitle,
                    subtitle = stringResource(Res.string.ui_transcripts_year_summary, yearCfu, examsForYear.size),
                )

                UniInsetGroup {
                    examsForYear.forEachIndexed { index, exam ->
                        ExamRecordItem(
                            exam = exam,
                            showDivider = index < examsForYear.lastIndex,
                            onClick = { if (exam.code.isNotBlank()) onExamClick(exam) },
                        )
                    }
                }
            } else {
                LiquidSectionHeader(
                    title = yearTitle,
                    subtitle = stringResource(Res.string.ui_transcripts_year_summary, 0, 0),
                )
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_grades_verbalized_empty),
                    description = stringResource(Res.string.ui_history_empty_desc),
                )
            }
        }
    }
}
