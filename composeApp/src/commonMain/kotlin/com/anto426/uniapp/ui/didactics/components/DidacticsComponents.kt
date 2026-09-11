package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardUiState
import com.anto426.uniapp.ui.components.items.DidacticItem
import com.anto426.uniapp.ui.components.items.DidacticRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ProfessorDidacticsContent(
    uiState: DidacticsDashboardUiState,
    onOpenTeachings: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenTheses: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    UniScreenColumn {
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = uiState.degreeName.ifBlank { stringResource(Res.string.ui_professor_role) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = uiState.degreeDetails.ifBlank { stringResource(Res.string.ui_university) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProfessorMetricBadge(
                        stringResource(Res.string.ui_professor_courses_count, uiState.teachingCount),
                    )
                    ProfessorMetricBadge(
                        stringResource(Res.string.ui_professor_rounds_count, uiState.openExamRounds),
                    )
                    ProfessorMetricBadge(
                        stringResource(Res.string.ui_professor_theses_count, uiState.thesisCount),
                    )
                }
            }
        }

        LiquidSectionHeader(
            title = stringResource(Res.string.ui_professor_didactics_tools),
            subtitle = stringResource(Res.string.ui_professor_didactics_tools_subtitle),
        )

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_teachings),
                        subtitle = stringResource(Res.string.ui_professor_teachings_subtitle),
                        icon = LiquidIcons.MenuBook,
                        badgeCount = uiState.teachingCount.takeIf { it > 0 },
                        onClick = onOpenTeachings,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_exam_rounds),
                        subtitle = stringResource(Res.string.ui_professor_exam_rounds_subtitle),
                        icon = LiquidIcons.Calendar,
                        badgeCount = uiState.openExamRounds.takeIf { it > 0 },
                        onClick = onOpenExams,
                    )
                },
            )
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_theses),
                        subtitle = stringResource(Res.string.ui_professor_theses_subtitle),
                        icon = LiquidIcons.Assignment,
                        badgeCount = uiState.thesisCount.takeIf { it > 0 },
                        onClick = onOpenTheses,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_reports),
                        subtitle = stringResource(Res.string.ui_professor_reports_subtitle),
                        icon = LiquidIcons.Edit,
                        badgeCount = uiState.reportCount.takeIf { it > 0 },
                        onClick = onOpenReports,
                    )
                },
            )
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.nav_route_news_title),
                        subtitle = stringResource(Res.string.nav_route_news_subtitle),
                        icon = LiquidIcons.Notifications,
                        onClick = onOpenNews,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_settings),
                        subtitle = stringResource(Res.string.ui_professor_settings_subtitle),
                        icon = LiquidIcons.Settings,
                        onClick = onOpenSettings,
                    )
                },
            )
        }
    }
}

@Composable
fun ProfessorMetricBadge(text: String) {
    LiquidBadge(
        text = text,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f),
        contentColor = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun YearProgress(year: String, isCompleted: Boolean, colorScheme: ColorScheme) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isCompleted) LiquidIcons.Check else LiquidIcons.Time,
            contentDescription = null,
            tint = if (isCompleted) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = year,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium,
            color = if (isCompleted) colorScheme.onSurface else colorScheme.onSurfaceVariant
        )
    }
}
