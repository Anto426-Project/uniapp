package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.uniapp.didactics.presentation.StudyPlanUiState
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.ui.components.items.StudyCourseItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun StudyPlanScreen(
    backdropState: Backdrop,
    uiState: StudyPlanUiState,
    onYearSelected: (Int) -> Unit,
    onCourseClick: (StudyCourse) -> Unit = {}
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

        // 2. Courses Grouped by Year
        uiState.displayedYears.forEach { yearGroup ->
            val totalYearCfu = yearGroup.courses.sumOf { course ->
                course.cfu.filter { it.isDigit() }.toIntOrNull() ?: 0
            }
            val completedCount = yearGroup.courses.count { it.status == CourseStatus.COMPLETED }

            LiquidSectionHeader(
                title = yearGroup.yearName,
                subtitle = "$totalYearCfu CFU totali • $completedCount/${yearGroup.courses.size} superati",
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                yearGroup.courses.forEach { course ->
                    StudyCourseItem(
                        course = course,
                        backdropState = backdropState,
                        onClick = { onCourseClick(course) },
                    )
                }
            }
        }
    }
}
