package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardUiState
import com.anto426.uniapp.ui.components.items.DidacticItem
import com.anto426.uniapp.ui.components.items.DidacticRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.ProfessorDidacticsContent
import com.anto426.uniapp.ui.didactics.components.YearProgress
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun DidacticsScreen(
    uiState: DidacticsDashboardUiState,
    onOpenTaxes: () -> Unit = {},
    onOpenGrades: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenTranscripts: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenQuestionnaires: () -> Unit = {},
    onOpenBadge: () -> Unit = {},
    onOpenAttendance: () -> Unit = {},
    onOpenStudyPlan: () -> Unit = {},
    onOpenTeachings: () -> Unit = {},
    onOpenTheses: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    if (uiState.isProfessor) {
        ProfessorDidacticsContent(
            uiState = uiState,
            onOpenTeachings = onOpenTeachings,
            onOpenExams = onOpenExams,
            onOpenTheses = onOpenTheses,
            onOpenReports = onOpenReports,
            onOpenNews = onOpenNews,
            onOpenSettings = onOpenSettings,
        )
        return
    }

    UniScreenColumn {
        // 1. Academic Degree Header Card - Semplificata e Pulita
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 20.dp,
            onClick = onOpenStatistics,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Titolo e CFU
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.degreeName.ifBlank { stringResource(Res.string.ui_didactics_degree_fallback) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = uiState.degreeDetails.ifBlank { stringResource(Res.string.ui_didactics_career_data_fallback) },
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    LiquidBadge(
                        text = if (uiState.plannedActivities > 0) {
                            stringResource(Res.string.ui_didactics_activities_badge, uiState.completedExams, uiState.plannedActivities)
                        } else {
                            stringResource(Res.string.ui_didactics_exams_badge, uiState.completedExams)
                        },
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                    )
                }

                // Progress Info: Visualizzazione snella degli anni
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        (1..uiState.totalYears.coerceIn(1, 4)).forEach { year ->
                            YearProgress("$year°", year <= uiState.currentYear, colorScheme)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = uiState.average,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.primary
                        )
                        Text(
                            text = stringResource(Res.string.ui_weighted_average),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Progress Bar & Footer
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidLinearProgressIndicator(
                        progress = uiState.progress,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_didactics_career_progress_percent, (uiState.progress * 100).toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.acquiredCfu} / ${uiState.targetCfu.takeIf { it > 0 } ?: "—"} CFU",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Section: Carriera e Piano di Studi
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_didactics_section_career_evaluations),
            subtitle = stringResource(Res.string.ui_didactics_section_career_evaluations_sub)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_transcript),
                        subtitle = stringResource(Res.string.ui_didactics_transcript_sub),
                        icon = LiquidIcons.MenuBook,
                        onClick = onOpenTranscripts
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_study_plan),
                        subtitle = stringResource(Res.string.ui_didactics_study_plan_sub),
                        icon = LiquidIcons.Assignment,
                        onClick = onOpenStudyPlan
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_taxes_title),
                        subtitle = stringResource(Res.string.ui_didactics_taxes_sub),
                        icon = LiquidIcons.CreditCard,
                        onClick = onOpenTaxes
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_grades_title),
                        subtitle = stringResource(Res.string.ui_didactics_grades_sub),
                        icon = LiquidIcons.Analytics,
                        onClick = onOpenGrades
                    )
                }
            )
        }

        // 3. Section: Esami e Attività Didattica
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_didactics_section_exams_classroom),
            subtitle = stringResource(Res.string.ui_didactics_section_exams_classroom_sub)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_exams_title),
                        subtitle = stringResource(Res.string.ui_didactics_exams_sub),
                        icon = LiquidIcons.Calendar,
                        badgeCount = uiState.openExamRounds.takeIf { it > 0 },
                        onClick = onOpenExams
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_attendance_title),
                        subtitle = stringResource(Res.string.ui_didactics_attendance_sub),
                        icon = LiquidIcons.QrCode,
                        onClick = onOpenAttendance
                    )
                }
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_questionnaires_title),
                        subtitle = stringResource(Res.string.ui_didactics_questionnaires_sub),
                        icon = LiquidIcons.Feedback,
                        badgeCount = uiState.pendingQuestionnaires.takeIf { it > 0 },
                        onClick = onOpenQuestionnaires
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_badge_title),
                        subtitle = stringResource(Res.string.ui_didactics_badge_sub),
                        icon = LiquidIcons.Badge,
                        onClick = onOpenBadge
                    )
                }
            )
        }
    }
}
