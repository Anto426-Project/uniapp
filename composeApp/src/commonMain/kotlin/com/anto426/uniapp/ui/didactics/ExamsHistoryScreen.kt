package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.PastExamStatus
import com.anto426.uniapp.didactics.presentation.ExamsHistoryUiState
import com.anto426.uniapp.ui.components.items.PastExamItem
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

enum class ExamHistoryFilter {
    ALL,
    VERBALIZED,
    BOOKED_PAST,
}

@Composable
fun ExamsHistoryTabContent(
    uiState: ExamsHistoryUiState,
    filter: ExamHistoryFilter,
    onFilterSelected: (ExamHistoryFilter) -> Unit,
) {
    val verbalizedCount = uiState.exams.count { it.status == PastExamStatus.VERBALIZED }
    val pastBookedCount = uiState.exams.count { it.status == PastExamStatus.BOOKED_PAST }

    Column(
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.exams.isEmpty()) {
            UniExpressiveStatusState(
                title = stringResource(Res.string.ui_history_empty_title),
                description = stringResource(Res.string.ui_history_empty_desc),
                inline = true,
                mainIcon = LiquidIcons.Time,
            )
        } else {
            if (verbalizedCount > 0 && pastBookedCount > 0) {
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

            UniContentHost(
                targetState = filter,
                modifier = Modifier.fillMaxWidth(),
                isForward = { from, to -> to.ordinal >= from.ordinal },
                label = "ExamsHistoryFilter",
            ) { activeFilter ->
                val displayedExams = when (activeFilter) {
                    ExamHistoryFilter.ALL -> uiState.exams
                    ExamHistoryFilter.VERBALIZED -> uiState.exams.filter { it.status == PastExamStatus.VERBALIZED }
                    ExamHistoryFilter.BOOKED_PAST -> uiState.exams.filter { it.status == PastExamStatus.BOOKED_PAST }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LiquidSectionHeader(
                        title = stringResource(Res.string.ui_exams_history_kicker),
                        subtitle = stringResource(Res.string.ui_professor_rounds_count, displayedExams.size),
                    )

                    displayedExams.forEachIndexed { index, exam ->
                        key("history|${exam.id}|${exam.date}|${exam.time}|$index") {
                            UniAnimatedEntrance(order = uniListEntranceOrder(index)) {
                                PastExamItem(exam)
                            }
                        }
                    }
                }
            }
        }
    }
}
