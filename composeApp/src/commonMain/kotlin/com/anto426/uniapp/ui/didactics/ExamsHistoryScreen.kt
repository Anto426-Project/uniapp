package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.uniapp.didactics.presentation.ExamsHistoryUiState
import com.anto426.uniapp.ui.components.items.PastExamItem
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ExamsHistoryScreen(backdropState: Backdrop, uiState: ExamsHistoryUiState) {
    UniScreenLazyColumn {
        if (uiState.exams.isEmpty()) {
            item(key = "history-empty") {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_history_empty_title),
                    description = stringResource(Res.string.ui_history_empty_desc),
                    backdropState = backdropState,
                )
            }
        }

        itemsIndexed(
            items = uiState.exams,
            key = { index, exam ->
                "history|${exam.id}|${exam.date}|${exam.time}|$index"
            },
        ) { _, exam ->
            PastExamItem(exam, backdropState)
        }
    }
}
