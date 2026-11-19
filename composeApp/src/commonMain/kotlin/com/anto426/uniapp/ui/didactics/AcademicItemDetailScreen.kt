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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AcademicItemDetailUiState
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.ThesisDetailContent
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AcademicItemDetailScreen(
    uiState: AcademicItemDetailUiState,
    section: AcademicSection,
    onTabSelected: (Int) -> Unit,
) {
    val item = uiState.item ?: return
    if (section == AcademicSection.ExamRounds) {
        ProfessorExamDetailContent(
            item = item,
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected,
        )
        return
    }

    if (section == AcademicSection.Theses) {
        val thesis = uiState.thesisData ?: return
        ThesisDetailContent(
            item = item,
            thesis = thesis,
        )
        return
    }

    val colorScheme = MaterialTheme.colorScheme
    val fields = uiState.detailFields

    val sectionTag = when (section) {
        AcademicSection.Theses -> stringResource(Res.string.ui_academic_tag_thesis)
        AcademicSection.Teachings -> stringResource(Res.string.ui_academic_tag_teaching)
        AcademicSection.Reports -> stringResource(Res.string.ui_academic_tag_report)
        AcademicSection.ExamRounds -> stringResource(Res.string.ui_academic_tag_exam)
    }

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
                    LiquidBadge(
                        text = sectionTag,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )

                    item.code?.takeIf(String::isNotBlank)?.let { code ->
                        LiquidBadge(
                            text = code,
                            containerColor = colorScheme.onSurface.copy(alpha = 0.06f),
                            contentColor = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.subtitle?.takeIf(String::isNotBlank)?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                item.date?.takeIf(String::isNotBlank)?.let { date ->
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Calendar,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "${stringResource(Res.string.ui_academic_data_prefix)}$date",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary,
                        )
                    }
                }
            }
        }

        if (fields.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_academic_item_details),
            )

            UniInsetGroup {
                fields.forEachIndexed { index, (label, value) ->
                    UniInsetRow(
                        title = label,
                        subtitle = value,
                        icon = LiquidIcons.Info,
                        showDivider = index < fields.lastIndex,
                    )
                }
            }
        }
    }
}
