package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.ui.components.items.StudyCourseItem
import com.anto426.uniapp.ui.data.UiInitialData
import com.anto426.uniapp.ui.models.StudyCourse
import com.kyant.backdrop.Backdrop

@Composable
fun StudyPlanScreen(
    backdropState: Backdrop,
    onCourseClick: (StudyCourse) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val planItems = UiInitialData.studyPlan

    val tabs = planItems.map { LiquidNavigationItem(label = it.yearName, icon = it.icon) }

    UniScreenColumn {
        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            backdropState = backdropState
        )

        Spacer(modifier = Modifier.height(8.dp))

        val currentYear = planItems[selectedTab]
        LiquidSectionTitle(
            title = currentYear.yearName,
            subtitle = stringResource(Res.string.ui_exams_planned, currentYear.courses.size.toString())
        )

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            currentYear.courses.forEach { course ->
                StudyCourseItem(
                    course = course,
                    backdropState = backdropState,
                    onClick = { onCourseClick(course) }
                )
            }
        }
    }
}
