package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardUiState
import com.anto426.uniapp.ui.components.display.UniBentoStatTile
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
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
    onOpenTranscripts: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenQuestionnaires: () -> Unit = {},
    onOpenBadge: () -> Unit = {},
    onOpenAttendance: () -> Unit = {},
    onOpenTeachings: () -> Unit = {},
    onOpenTheses: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

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
        // 1. Academic Degree Bento Hero Card (compatto, zero IntrinsicSize.Min)
        LiquidCard(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .graphicsLayer(clip = false),
            shape = RoundedRectangle(24.dp),
            contentPadding = 18.dp,
            onClick = onOpenGrades,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = uiState.degreeName.ifBlank { stringResource(Res.string.ui_didactics_degree_fallback) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = uiState.degreeDetails.ifBlank { stringResource(Res.string.ui_didactics_career_data_fallback) },
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    LiquidBadge(
                        text = if (uiState.plannedActivities > 0) {
                            stringResource(Res.string.ui_didactics_activities_badge, uiState.completedExams, uiState.plannedActivities)
                        } else {
                            stringResource(Res.string.ui_didactics_exams_badge, uiState.completedExams)
                        },
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.55f),
                        contentColor = colorScheme.primary,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        UniBentoStatTile(
                            label = stringResource(Res.string.ui_weighted_average),
                            value = uiState.average,
                            icon = LiquidIcons.Analytics,
                            accentColor = colorScheme.primary,
                            valueColor = colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        val yearsShape = RoundedRectangle(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(yearsShape)
                                .background(colorScheme.surfaceContainerHigh.copy(alpha = 0.55f))
                                .border(
                                    width = 1.dp,
                                    color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                                    shape = yearsShape,
                                )
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            (1..uiState.totalYears.coerceIn(1, 4)).forEach { year ->
                                YearProgress("$year°", year <= uiState.currentYear, colorScheme)
                            }
                        }
                    }

                    val cfuTileShape = RoundedRectangle(18.dp)
                    Column(
                        modifier = Modifier
                            .width(114.dp)
                            .defaultMinSize(minHeight = 122.dp)
                            .clip(cfuTileShape)
                            .background(colorScheme.surfaceContainerHigh.copy(alpha = 0.55f))
                            .border(
                                width = 1.dp,
                                color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                                shape = cfuTileShape,
                            )
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    ) {
                        UniCircularProgressRing(
                            progress = uiState.progress,
                            centerText = "${(uiState.progress * 100).toInt()}%",
                            subtitleText = "CFU",
                            size = 70.dp,
                            strokeWidth = 6.5.dp,
                            primaryColor = colorScheme.primary,
                            secondaryColor = colorScheme.tertiary,
                        )
                        Text(
                            text = "${uiState.acquiredCfu} / ${uiState.targetCfu.takeIf { it > 0 } ?: "—"} CFU",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_didactics_career_progress_percent, (uiState.progress * 100).toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_details),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                        Icon(
                            imageVector = LiquidIcons.ChevronRight,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }

        // 2. Carriera e valutazioni in Apple Inset Grouped List (zero troncamenti)
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_didactics_section_career_evaluations),
            subtitle = stringResource(Res.string.ui_didactics_section_career_evaluations_sub),
        )

        UniInsetGroup(cornerRadius = 22.dp) {
            UniInsetRow(
                title = stringResource(Res.string.nav_route_transcripts_title),
                subtitle = stringResource(Res.string.nav_route_transcripts_subtitle),
                icon = LiquidIcons.MenuBook,
                iconTint = colorScheme.primary,
                onClick = onOpenTranscripts,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.nav_route_grades_title),
                subtitle = stringResource(Res.string.ui_didactics_grades_sub),
                icon = LiquidIcons.Assignment,
                iconTint = colorScheme.tertiary,
                onClick = onOpenGrades,
                showDivider = true,
            )
            UniInsetRow(
                title = stringResource(Res.string.ui_didactics_taxes_title),
                subtitle = stringResource(Res.string.ui_didactics_taxes_sub),
                icon = LiquidIcons.CreditCard,
                iconTint = glassColors.warning,
                onClick = onOpenTaxes,
                showDivider = false,
            )
        }

        // 3. Esami e aula in griglia Bento compatta 2×2
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_didactics_section_exams_classroom),
            subtitle = stringResource(Res.string.ui_didactics_section_exams_classroom_sub),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_exams_title),
                        subtitle = stringResource(Res.string.ui_didactics_exams_sub),
                        icon = LiquidIcons.Calendar,
                        badgeCount = uiState.openExamRounds.takeIf { it > 0 },
                        iconColor = colorScheme.primary,
                        onClick = onOpenExams,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_attendance_title),
                        subtitle = stringResource(Res.string.ui_didactics_attendance_sub),
                        icon = LiquidIcons.QrCode,
                        iconColor = colorScheme.tertiary,
                        onClick = onOpenAttendance,
                    )
                },
            )

            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_questionnaires_title),
                        subtitle = stringResource(Res.string.ui_didactics_questionnaires_sub),
                        icon = LiquidIcons.Feedback,
                        badgeCount = uiState.pendingQuestionnaires.takeIf { it > 0 },
                        iconColor = glassColors.warning,
                        onClick = onOpenQuestionnaires,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_didactics_badge_title),
                        subtitle = stringResource(Res.string.ui_didactics_badge_sub),
                        icon = LiquidIcons.Badge,
                        iconColor = colorScheme.secondary,
                        onClick = onOpenBadge,
                    )
                },
            )
        }
    }
}
