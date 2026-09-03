package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun DidacticsScreen(
    backdropState: Backdrop,
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
            backdropState = backdropState,
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
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
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
                        backdropState = backdropState
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
                        backdropState = backdropState
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
                        backdropState = backdropState,
                        onClick = onOpenTranscripts
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_study_plan),
                        subtitle = stringResource(Res.string.ui_didactics_study_plan_sub),
                        icon = LiquidIcons.Assignment,
                        backdropState = backdropState,
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
                        backdropState = backdropState,
                        onClick = onOpenTaxes
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_grades_title),
                        subtitle = stringResource(Res.string.ui_didactics_grades_sub),
                        icon = LiquidIcons.Analytics,
                        backdropState = backdropState,
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
                        backdropState = backdropState,
                        badgeCount = uiState.openExamRounds.takeIf { it > 0 },
                        onClick = onOpenExams
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_attendance_title),
                        subtitle = stringResource(Res.string.ui_didactics_attendance_sub),
                        icon = LiquidIcons.QrCode,
                        backdropState = backdropState,
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
                        backdropState = backdropState,
                        badgeCount = uiState.pendingQuestionnaires.takeIf { it > 0 },
                        onClick = onOpenQuestionnaires
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_badge_title),
                        subtitle = stringResource(Res.string.ui_didactics_badge_sub),
                        icon = LiquidIcons.Badge,
                        backdropState = backdropState,
                        onClick = onOpenBadge
                    )
                }
            )
        }
    }
}

@Composable
private fun ProfessorDidacticsContent(
    backdropState: Backdrop,
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
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
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
                        backdropState,
                    )
                    ProfessorMetricBadge(
                        stringResource(Res.string.ui_professor_rounds_count, uiState.openExamRounds),
                        backdropState,
                    )
                    ProfessorMetricBadge(
                        stringResource(Res.string.ui_professor_theses_count, uiState.thesisCount),
                        backdropState,
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
                        backdropState = backdropState,
                        onClick = onOpenTeachings,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_exam_rounds),
                        subtitle = stringResource(Res.string.ui_professor_exam_rounds_subtitle),
                        icon = LiquidIcons.Calendar,
                        badgeCount = uiState.openExamRounds.takeIf { it > 0 },
                        backdropState = backdropState,
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
                        backdropState = backdropState,
                        onClick = onOpenTheses,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_reports),
                        subtitle = stringResource(Res.string.ui_professor_reports_subtitle),
                        icon = LiquidIcons.Edit,
                        badgeCount = uiState.reportCount.takeIf { it > 0 },
                        backdropState = backdropState,
                        onClick = onOpenReports,
                    )
                },
            )
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_news),
                        subtitle = stringResource(Res.string.ui_professor_news_subtitle),
                        icon = LiquidIcons.Notifications,
                        backdropState = backdropState,
                        onClick = onOpenNews,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_settings),
                        subtitle = stringResource(Res.string.ui_professor_settings_subtitle),
                        icon = LiquidIcons.Settings,
                        backdropState = backdropState,
                        onClick = onOpenSettings,
                    )
                },
            )
        }
    }
}

@Composable
private fun ProfessorMetricBadge(text: String, backdropState: Backdrop) {
    LiquidBadge(
        text = text,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f),
        contentColor = MaterialTheme.colorScheme.primary,
        backdropState = backdropState,
    )
}

@Composable
private fun YearProgress(year: String, isCompleted: Boolean, colorScheme: androidx.compose.material3.ColorScheme) {
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
