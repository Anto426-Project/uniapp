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
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.uniapp.didactics.presentation.TranscriptsUiState
import com.anto426.uniapp.ui.components.items.ExamRecordItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun TranscriptsScreen(
    backdropState: Backdrop,
    uiState: TranscriptsUiState,
    onYearSelected: (Int) -> Unit,
) {
    val tabs = listOf(
        LiquidNavigationItem(label = "Tutti"),
        LiquidNavigationItem(label = "1° Anno"),
        LiquidNavigationItem(label = "2° Anno"),
        LiquidNavigationItem(label = "3° Anno")
    )

    UniScreenColumn {
        // 1. Year Tab Selector
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedYear,
            onTabSelected = onYearSelected,
            backdropState = backdropState,
        )

        // 2. Exams Grouped by Year
        uiState.displayedYears.forEach { year ->
            val examsForYear = uiState.examsByYear[year].orEmpty()
            if (examsForYear.isNotEmpty()) {
                val yearCfu = examsForYear.sumOf { exam ->
                    exam.cfu.filter { it.isDigit() }.toIntOrNull() ?: 0
                }

                LiquidSectionHeader(
                    title = when (year) {
                        1 -> "Primo Anno"
                        2 -> "Secondo Anno"
                        3 -> "Terzo Anno"
                        else -> "$year° Anno"
                    },
                    subtitle = "$yearCfu CFU conseguiti • ${examsForYear.size} esami verbalizzati",
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
            }
        }
    }
}
