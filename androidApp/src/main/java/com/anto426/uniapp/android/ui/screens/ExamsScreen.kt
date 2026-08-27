package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.components.items.ExamSessionItem
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun ExamsScreen(backdropState: Backdrop) {
    val exams = remember { UiInitialData.examSessions }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        LiquidNavigationItem(stringResource(R.string.ui_bookable_exams), badge = exams.count { !it.isBooked }.toString(), icon = LiquidIcons.Calendar),
        LiquidNavigationItem(stringResource(R.string.ui_booked_exams), badge = exams.count { it.isBooked }.toString(), icon = LiquidIcons.Check)
    )
    val filteredExams = exams.filter { it.isBooked == (selectedTabIndex == 1) }
    UniScreenColumn {
        LiquidTabBar(items = tabs, selectedIndex = selectedTabIndex, onTabSelected = { selectedTabIndex = it }, backdropState = backdropState, modifier = Modifier.padding(bottom = 8.dp))
        Column(Modifier.fillMaxWidth().graphicsLayer(clip = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            filteredExams.forEach { ExamSessionItem(it, backdropState) }
        }
        Spacer(Modifier.height(80.dp))
    }
}
