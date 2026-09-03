package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.uniapp.didactics.presentation.TranscriptsUiState
import com.anto426.uniapp.ui.components.items.ExamRecordItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TranscriptsScreen(
    backdropState: Backdrop,
    uiState: TranscriptsUiState,
    onYearSelected: (Int) -> Unit,
) {
    val years = uiState.availableYears
    val tabs = years.map { year ->
        LiquidNavigationItem(label = "${year}° Anno")
    }
    val selectedTabIndex = years.indexOf(uiState.selectedYear).coerceAtLeast(0)

    UniScreenColumn {
        // 1. Year Tab Selector
        if (tabs.isNotEmpty()) {
            LiquidTabBar(
                items = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = { index ->
                    val year = years.getOrElse(index) { index + 1 }
                    onYearSelected(year)
                },
                backdropState = backdropState,
            )
        }

        // 2. Exams Grouped by Year
        uiState.displayedYears.forEach { year ->
            val examsForYear = uiState.examsByYear[year].orEmpty()
            if (examsForYear.isNotEmpty()) {
                val yearCfu = examsForYear.sumOf { exam ->
                    exam.cfu.filter { it.isDigit() }.toIntOrNull() ?: 0
                }

                LiquidSectionHeader(
                    title = when (year) {
                        1 -> stringResource(Res.string.ui_year_first)
                        2 -> stringResource(Res.string.ui_year_second)
                        3 -> stringResource(Res.string.ui_year_third)
                        else -> stringResource(Res.string.ui_year_nth, year)
                    },
                    subtitle = stringResource(Res.string.ui_transcripts_year_summary, yearCfu, examsForYear.size),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    examsForYear.forEach { exam ->
                        ExamRecordItem(exam = exam, backdropState = backdropState)
                    }
                }
            } else {
                LiquidSectionHeader(
                    title = when (year) {
                        1 -> stringResource(Res.string.ui_year_first)
                        2 -> stringResource(Res.string.ui_year_second)
                        3 -> stringResource(Res.string.ui_year_third)
                        else -> stringResource(Res.string.ui_year_nth, year)
                    },
                    subtitle = stringResource(Res.string.ui_transcripts_year_summary, 0, 0),
                )
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_grades_verbalized_empty),
                    description = stringResource(Res.string.ui_history_empty_desc),
                    backdropState = backdropState,
                )
            }
        }
    }
}
