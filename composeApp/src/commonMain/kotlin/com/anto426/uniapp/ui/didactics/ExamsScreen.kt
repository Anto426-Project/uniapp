package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.ExamsUiState
import com.anto426.uniapp.ui.components.items.ExamSessionItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun ExamsScreen(
    backdropState: Backdrop,
    uiState: ExamsUiState,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_bookable_exams), badge = uiState.bookableCount.toString(), icon = LiquidIcons.Calendar),
        LiquidNavigationItem(stringResource(Res.string.ui_booked_exams), badge = uiState.bookedCount.toString(), icon = LiquidIcons.Check)
    )
    UniScreenColumn {
        LiquidTabBar(items = tabs, selectedIndex = uiState.selectedTab, onTabSelected = onTabSelected, backdropState = backdropState, modifier = Modifier.padding(bottom = 8.dp))
        Column(Modifier.fillMaxWidth().graphicsLayer(clip = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            uiState.visibleExams.forEach { ExamSessionItem(it, backdropState) }
        }
        Spacer(Modifier.height(80.dp))
    }
}
