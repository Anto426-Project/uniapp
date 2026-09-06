package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AcademicItemDetailUiState
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.ThesisDetailContent
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
        AcademicSection.Theses -> "TESISTA • LAUREANDO"
        AcademicSection.Teachings -> "INSEGNAMENTO"
        AcademicSection.Reports -> "VERBALE DI COMMISSIONE"
        AcademicSection.ExamRounds -> "APPELLO D'ESAME"
    }

    UniScreenColumn {
        // Hero Header Card
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Top Tag Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidBadge(
                        text = sectionTag,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = colorScheme.primary,
                    )

                    item.code?.takeIf(String::isNotBlank)?.let { code ->
                        LiquidBadge(
                            text = code,
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = colorScheme.primary,
                        )
                    }
                }

                // Title & Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                    )
                    item.subtitle?.takeIf(String::isNotBlank)?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                item.date?.takeIf(String::isNotBlank)?.let { date ->
                    LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_academic_data_prefix),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                }
            }
        }

        // Details Preference Group
        if (fields.isNotEmpty()) {
            LiquidPreferenceGroup(
                title = stringResource(Res.string.ui_academic_item_details),
            ) {
                fields.forEach { (label, value) ->
                    LiquidPreferenceItem(
                        title = label,
                        subtitle = value,
                        icon = LiquidIcons.Info,
                    )
                }
            }
        }
    }
}
