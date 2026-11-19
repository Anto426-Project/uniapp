package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.didactics.presentation.AcademicSectionUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.anto426.unisdk.model.ProfessorContentItem
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AcademicSectionScreen(
    uiState: AcademicSectionUiState,
    section: AcademicSection = AcademicSection.Teachings,
    onItemClick: (ProfessorContentItem) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val (sectionIcon, sectionTitle, sectionSubtitle) = when (section) {
        AcademicSection.Teachings -> Triple(
            LiquidIcons.MenuBook,
            stringResource(Res.string.ui_professor_teachings),
            stringResource(Res.string.ui_professor_teachings_subtitle),
        )
        AcademicSection.Theses -> Triple(
            LiquidIcons.Assignment,
            stringResource(Res.string.ui_professor_theses),
            stringResource(Res.string.ui_professor_theses_subtitle),
        )
        AcademicSection.Reports -> Triple(
            LiquidIcons.Edit,
            stringResource(Res.string.ui_professor_reports),
            stringResource(Res.string.ui_professor_reports_subtitle),
        )
        AcademicSection.ExamRounds -> Triple(
            LiquidIcons.Calendar,
            stringResource(Res.string.ui_professor_exam_rounds),
            stringResource(Res.string.ui_professor_exam_rounds_subtitle),
        )
    }

    val countBadge = when (section) {
        AcademicSection.Teachings -> stringResource(Res.string.ui_professor_courses_count, uiState.items.size)
        AcademicSection.Theses -> stringResource(Res.string.ui_professor_theses_count, uiState.items.size)
        AcademicSection.ExamRounds -> stringResource(Res.string.ui_professor_rounds_count, uiState.items.size)
        AcademicSection.Reports -> stringResource(Res.string.ui_professor_items_count, uiState.items.size)
    }
    val datedCount = uiState.items.count { !it.date.isNullOrBlank() }
    val codedCount = uiState.items.count { !it.code.isNullOrBlank() }
    val visibleItems = uiState.visibleItems

    UniScreenColumn {
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
                            imageVector = sectionIcon,
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
                            text = sectionTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    LiquidBadge(
                        text = countBadge,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${uiState.items.size}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = sectionSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    GradesMetricTile(
                        label = sectionTitle,
                        value = "${uiState.items.size}",
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_course_code_header),
                        value = "$codedCount",
                        valueColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_date_time_label),
                        value = "$datedCount",
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        LiquidSectionHeader(
            title = sectionTitle,
            subtitle = stringResource(Res.string.ui_professor_items_count, visibleItems.size),
        )

        if (visibleItems.isEmpty()) {
            UniInsetGroup {
                UniInsetRow(
                    title = stringResource(Res.string.ui_professor_empty_title),
                    subtitle = stringResource(Res.string.ui_state_empty_academic_section),
                    icon = sectionIcon,
                    showDivider = false,
                )
            }
        } else {
            UniInsetGroup {
                visibleItems.forEachIndexed { index, item ->
                    key("${item.id}|${item.code}|${item.title}|${item.date}|$index") {
                        val rowSubtitle = buildList {
                            item.subtitle?.takeIf(String::isNotBlank)?.let(::add)
                            item.date?.takeIf(String::isNotBlank)?.let(::add)
                        }.joinToString(" • ").ifBlank { null }

                        UniInsetRow(
                            title = item.title,
                            subtitle = rowSubtitle,
                            icon = sectionIcon,
                            onClick = { onItemClick(item) },
                            showDivider = index < visibleItems.lastIndex,
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    item.code?.takeIf(String::isNotBlank)?.let { code ->
                                        LiquidBadge(
                                            text = code,
                                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                            contentColor = colorScheme.primary,
                                        )
                                    }
                                    Icon(
                                        imageVector = LiquidIcons.ChevronRight,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

