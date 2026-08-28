package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.ui.components.items.PastExamItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun ExamsHistoryScreen(backdropState: Backdrop) {
    UniScreenColumn {
        Column(Modifier.fillMaxWidth().graphicsLayer(clip = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            UiInitialData.pastExams.forEach { PastExamItem(it, backdropState) }
        }
    }
}
