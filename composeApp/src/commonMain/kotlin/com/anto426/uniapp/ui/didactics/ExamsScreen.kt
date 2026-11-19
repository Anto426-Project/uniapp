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
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.ExamsUiState
import com.anto426.uniapp.model.didactics.ExamSession
import com.anto426.uniapp.ui.components.items.ExamSessionItem
import com.anto426.uniapp.ui.components.items.ProfessorExamItem
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.anto426.unisdk.model.ProfessorContentItem
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

enum class ExamBookableFilter {
    ALL,
    OPEN_NOW,
    OPENING_SOON,
}

@Composable
fun ExamsScreen(
    uiState: ExamsUiState,
    onTabSelected: (Int) -> Unit,
    showTabs: Boolean = true,
    onToggleBooking: (String) -> Unit,
    onProfessorExamClick: (ProfessorContentItem) -> Unit = {},
    onAddToCalendar: (String) -> Unit,
) {
    var bookableFilter by rememberSaveable { mutableStateOf(ExamBookableFilter.ALL) }

    val studentTabs = listOf(
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_bookable_exams),
            badge = uiState.bookableCount.takeIf { it > 0 }?.toString(),
            icon = LiquidIcons.Calendar,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_booked_exams),
            badge = uiState.bookedCount.takeIf { it > 0 }?.toString(),
            icon = LiquidIcons.Check,
        ),
    )

    UniScreenLazyColumn {
        if (!uiState.isProfessor && showTabs) {
            item(key = "exam-tabs") {
                LiquidTabBar(
                    items = studentTabs,
                    selectedIndex = uiState.selectedTab,
                    onTabSelected = onTabSelected,
                )
            }
        }

        if (uiState.isProfessor) {
            professorExamsContentItems(
                rounds = uiState.visibleProfessorExamRounds,
                onProfessorExamClick = onProfessorExamClick,
            )
        } else {
            studentExamsContentItems(
                uiState = uiState,
                bookableFilter = bookableFilter,
                onBookableFilterChange = { bookableFilter = it },
                onToggleBooking = onToggleBooking,
                onAddToCalendar = onAddToCalendar,
            )
        }
    }
}

fun LazyListScope.professorExamsContentItems(
    rounds: List<ProfessorContentItem>,
    onProfessorExamClick: (ProfessorContentItem) -> Unit,
) {
    item(key = "professor-exams-hero") {
        ProfessorExamsHeroCard(rounds = rounds)
    }

    if (rounds.isEmpty()) {
        item(key = "professor-exam-empty") {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_no_exams_in_section),
                description = stringResource(Res.string.ui_no_exams_in_section_description),
            )
        }
    } else {
        item(key = "professor-exams-header") {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_professor_exam_rounds),
                subtitle = stringResource(
                    Res.string.ui_professor_rounds_count,
                    rounds.size,
                ),
            )
        }
        itemsIndexed(
            items = rounds,
            key = { index, exam -> "professor-exam|${exam.id}|${exam.title}|$index" },
        ) { _, exam ->
            ProfessorExamItem(
                exam = exam,
                onClickDetail = { onProfessorExamClick(exam) },
            )
        }
    }
}

fun LazyListScope.studentExamsContentItems(
    uiState: ExamsUiState,
    bookableFilter: ExamBookableFilter,
    onBookableFilterChange: (ExamBookableFilter) -> Unit,
    onToggleBooking: (String) -> Unit,
    onAddToCalendar: (String) -> Unit,
) {
    val openNowExamsCount = uiState.exams.count { !it.isBooked && it.canBook }
    val openingSoonExamsCount = uiState.exams.count { !it.isBooked && !it.canBook }
    val nextBookedExam = uiState.exams.firstOrNull { it.isBooked }

    val displayedStudentExams = if (uiState.selectedTab == 0) {
        when (bookableFilter) {
            ExamBookableFilter.ALL -> uiState.visibleExams
            ExamBookableFilter.OPEN_NOW -> uiState.visibleExams.filter { it.canBook }
            ExamBookableFilter.OPENING_SOON -> uiState.visibleExams.filter { !it.canBook }
        }
    } else {
        uiState.visibleExams
    }

    item(key = "student-exams-hero") {
        StudentExamsHeroCard(
            totalCount = uiState.exams.size,
            openNowCount = openNowExamsCount,
            bookedCount = uiState.bookedCount,
            nextBookedExam = nextBookedExam,
        )
    }

    if (uiState.selectedTab == 0 && uiState.visibleExams.isNotEmpty()) {
        item(key = "student-exam-filters") {
            val filterOptions = listOf(
                Triple(
                    ExamBookableFilter.ALL,
                    stringResource(Res.string.ui_exams_all_filter),
                    uiState.visibleExams.size,
                ),
                Triple(
                    ExamBookableFilter.OPEN_NOW,
                    stringResource(Res.string.ui_exams_open_filter),
                    openNowExamsCount,
                ),
                Triple(
                    ExamBookableFilter.OPENING_SOON,
                    stringResource(Res.string.ui_exams_opening_soon_filter),
                    openingSoonExamsCount,
                ),
            )
            UniHorizontalChipRow {
                filterOptions.forEach { (filterOption, label, count) ->
                    key(filterOption) {
                        val selected = bookableFilter == filterOption
                        LiquidChip(
                            label = "$label ($count)",
                            selected = selected,
                            leadingIcon = if (selected) LiquidIcons.Check else null,
                            onClick = { onBookableFilterChange(filterOption) },
                        )
                    }
                }
            }
        }
    }

    if (displayedStudentExams.isEmpty()) {
        item(key = "exam-empty-${uiState.selectedTab}-$bookableFilter") {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_no_exams_in_section),
                description = stringResource(Res.string.ui_no_exams_in_section_description),
            )
        }
    } else {
        item(key = "student-exams-header-${uiState.selectedTab}") {
            LiquidSectionHeader(
                title = if (uiState.selectedTab == 1) {
                    stringResource(Res.string.ui_booked_exams)
                } else {
                    stringResource(Res.string.ui_bookable_exams)
                },
                subtitle = stringResource(
                    Res.string.ui_professor_rounds_count,
                    displayedStudentExams.size,
                ),
            )
        }
        itemsIndexed(
            items = displayedStudentExams,
            key = { index, exam -> "exam|${exam.id}|${exam.date}|${exam.time}|$index" },
        ) { _, exam ->
            ExamSessionItem(
                exam = exam,
                isMutating = uiState.mutatingExamId != null,
                onToggleBooking = { onToggleBooking(exam.id) },
                onAddToCalendar = { onAddToCalendar(exam.id) },
            )
        }
    }
}

@Composable
private fun StudentExamsHeroCard(
    totalCount: Int,
    openNowCount: Int,
    bookedCount: Int,
    nextBookedExam: ExamSession?,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val accentColor = when {
        bookedCount > 0 -> glassColors.success
        openNowCount > 0 -> colorScheme.primary
        else -> colorScheme.onSurfaceVariant
    }

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
        contentPadding = PaddingValues(18.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        imageVector = if (bookedCount > 0) LiquidIcons.Check else LiquidIcons.Calendar,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 36.dp,
                            iconSize = 18.dp,
                            containerColor = accentColor.copy(alpha = 0.14f),
                            shape = RoundedRectangle(11.dp),
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.ui_exams_hero_kicker).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                LiquidBadge(
                    text = if (bookedCount > 0) {
                        "$bookedCount ${stringResource(Res.string.ui_booked_exams).lowercase()}"
                    } else {
                        "$openNowCount ${stringResource(Res.string.ui_exams_status_open).lowercase()}"
                    },
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (nextBookedExam != null) {
                    Text(
                        text = stringResource(Res.string.ui_exams_next_booked_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = nextBookedExam.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val scheduleText = buildList {
                        add(nextBookedExam.date)
                        nextBookedExam.time.takeIf(String::isNotBlank)?.let(::add)
                        nextBookedExam.room.takeIf(String::isNotBlank)?.let(::add)
                    }.joinToString(" • ")
                    Text(
                        text = scheduleText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.ui_exams_open_count_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "$openNowCount",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Text(
                            text = stringResource(Res.string.ui_bookable_exams).lowercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExamsHeroMetric(
                    label = stringResource(Res.string.ui_exams_open_count_label),
                    value = "$openNowCount",
                    valueColor = if (openNowCount > 0) colorScheme.primary else colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                ExamsHeroMetric(
                    label = stringResource(Res.string.ui_booked_exams),
                    value = "$bookedCount",
                    valueColor = if (bookedCount > 0) glassColors.success else colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                ExamsHeroMetric(
                    label = stringResource(Res.string.ui_exams_total_count_label),
                    value = "$totalCount",
                    valueColor = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ProfessorExamsHeroCard(rounds: List<ProfessorContentItem>) {
    val colorScheme = MaterialTheme.colorScheme
    val totalRegistrations = rounds.sumOf { exam ->
        exam.bookings.size.takeIf { it > 0 }
            ?: exam.detail?.let { d ->
                Regex("""(\d+)\s*(?:prenotat|iscritt)""", RegexOption.IGNORE_CASE)
                    .find(d)?.groupValues?.get(1)?.toIntOrNull()
            }
            ?: 0
    }

    LiquidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
        contentPadding = PaddingValues(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Calendar,
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
                        text = stringResource(Res.string.ui_exams_professor_hero_kicker).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                LiquidBadge(
                    text = stringResource(Res.string.ui_professor_rounds_count, rounds.size),
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    contentColor = colorScheme.primary,
                )
            }

            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                GradesMetricTile(
                    label = stringResource(Res.string.ui_professor_exam_rounds),
                    value = "${rounds.size}",
                    valueColor = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                GradesMetricTile(
                    label = stringResource(Res.string.ui_professor_bookings),
                    value = "$totalRegistrations",
                    valueColor = colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ExamsHeroMetric(
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
