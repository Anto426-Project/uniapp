package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.ExamsHistoryUiState
import com.anto426.uniapp.model.didactics.PastExamStatus
import com.anto426.uniapp.ui.components.items.PastExamItem
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

enum class ExamHistoryFilter {
    ALL,
    VERBALIZED,
    BOOKED_PAST,
}

@Composable
fun ExamsHistoryScreen(uiState: ExamsHistoryUiState) {
    var filter by rememberSaveable { mutableStateOf(ExamHistoryFilter.ALL) }
    UniScreenLazyColumn {
        examsHistoryContentItems(
            uiState = uiState,
            filter = filter,
            onFilterSelected = { filter = it },
        )
    }
}

fun LazyListScope.examsHistoryContentItems(
    uiState: ExamsHistoryUiState,
    filter: ExamHistoryFilter,
    onFilterSelected: (ExamHistoryFilter) -> Unit,
) {
    val verbalizedCount = uiState.exams.count { it.status == PastExamStatus.VERBALIZED }
    val pastBookedCount = uiState.exams.count { it.status == PastExamStatus.BOOKED_PAST }

    val displayedExams = when (filter) {
        ExamHistoryFilter.ALL -> uiState.exams
        ExamHistoryFilter.VERBALIZED -> uiState.exams.filter { it.status == PastExamStatus.VERBALIZED }
        ExamHistoryFilter.BOOKED_PAST -> uiState.exams.filter { it.status == PastExamStatus.BOOKED_PAST }
    }

    if (uiState.exams.isEmpty()) {
        item(key = "history-empty") {
            UniExpressiveStatusState(
                title = stringResource(Res.string.ui_history_empty_title),
                description = stringResource(Res.string.ui_history_empty_desc),
                inline = true,
                mainIcon = LiquidIcons.Time,
            )
        }
    } else {
        if (verbalizedCount > 0 && pastBookedCount > 0) {
            item(key = "history-filters") {
                val options = listOf(
                    Triple(ExamHistoryFilter.ALL, stringResource(Res.string.ui_exams_all_filter), uiState.exams.size),
                    Triple(ExamHistoryFilter.VERBALIZED, stringResource(Res.string.ui_exams_history_verbalized_count), verbalizedCount),
                    Triple(ExamHistoryFilter.BOOKED_PAST, stringResource(Res.string.ui_exams_history_past_bookings_count), pastBookedCount),
                )
                UniHorizontalChipRow {
                    options.forEach { (option, label, count) ->
                        key(option) {
                            val selected = filter == option
                            LiquidChip(
                                label = "$label ($count)",
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { onFilterSelected(option) },
                            )
                        }
                    }
                }
            }
        }

        item(key = "history-header") {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_exams_history_kicker),
                subtitle = stringResource(Res.string.ui_professor_rounds_count, displayedExams.size),
            )
        }

        itemsIndexed(
            items = displayedExams,
            key = { index, exam ->
                "history|${exam.id}|${exam.date}|${exam.time}|$index"
            },
        ) { _, exam ->
            PastExamItem(exam)
        }
    }
}
