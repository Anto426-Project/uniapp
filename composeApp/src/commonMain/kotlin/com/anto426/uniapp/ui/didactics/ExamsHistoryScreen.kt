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
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
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
    val earnedCfu = uiState.exams.mapNotNull { it.cfu }.sum()

    val displayedExams = when (filter) {
        ExamHistoryFilter.ALL -> uiState.exams
        ExamHistoryFilter.VERBALIZED -> uiState.exams.filter { it.status == PastExamStatus.VERBALIZED }
        ExamHistoryFilter.BOOKED_PAST -> uiState.exams.filter { it.status == PastExamStatus.BOOKED_PAST }
    }

    if (uiState.exams.isEmpty()) {
        item(key = "history-empty") {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_history_empty_title),
                description = stringResource(Res.string.ui_history_empty_desc),
            )
        }
    } else {
        item(key = "history-hero") {
            ExamsHistoryHeroCard(
                totalCount = uiState.exams.size,
                verbalizedCount = verbalizedCount,
                pastBookedCount = pastBookedCount,
                earnedCfu = earnedCfu,
            )
        }

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

@Composable
private fun ExamsHistoryHeroCard(
    totalCount: Int,
    verbalizedCount: Int,
    pastBookedCount: Int,
    earnedCfu: Int,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
        contentPadding = PaddingValues(18.dp),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 36.dp,
                            iconSize = 18.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedRectangle(11.dp),
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.ui_exams_history_kicker).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                LiquidBadge(
                    text = stringResource(Res.string.ui_professor_rounds_count, totalCount),
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    contentColor = colorScheme.primary,
                )
            }

            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HistoryHeroMetric(
                    label = stringResource(Res.string.ui_exams_history_verbalized_count),
                    value = "$verbalizedCount",
                    valueColor = glassColors.success,
                    modifier = Modifier.weight(1f),
                )
                HistoryHeroMetric(
                    label = stringResource(Res.string.ui_exams_history_past_bookings_count),
                    value = "$pastBookedCount",
                    valueColor = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                HistoryHeroMetric(
                    label = stringResource(Res.string.ui_exams_history_cfu_total),
                    value = "$earnedCfu",
                    valueColor = colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HistoryHeroMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
