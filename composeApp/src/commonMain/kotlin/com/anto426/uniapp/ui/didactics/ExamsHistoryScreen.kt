package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.didactics.presentation.ExamsHistoryUiState
import com.anto426.uniapp.ui.components.items.PastExamItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun ExamsHistoryScreen(backdropState: Backdrop, uiState: ExamsHistoryUiState) {
    UniScreenColumn {
        Column(Modifier.fillMaxWidth().graphicsLayer(clip = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            uiState.exams.forEach { PastExamItem(it, backdropState) }
        }
    }
}
