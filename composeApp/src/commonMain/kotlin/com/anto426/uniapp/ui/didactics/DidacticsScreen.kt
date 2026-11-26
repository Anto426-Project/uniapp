package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardUiState
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.items.DidacticItem
import com.anto426.uniapp.ui.components.items.DidacticRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.ProfessorDidacticsContent
import com.anto426.uniapp.ui.didactics.components.YearProgress
import com.anto426.uniapp.ui.home.dashboard.components.HomeErrorRetryCard
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun DidacticsScreen(
    uiState: DidacticsDashboardUiState,
    onRefresh: () -> Unit = {},
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
        if (uiState.loadState == FeatureLoadState.Error && !uiState.errorMessage.isNullOrBlank()) {
            HomeErrorRetryCard(
                message = uiState.errorMessage,
                onRetry = onRefresh,
            )
        }

        // 1. Apple Fitness / Activity Ring Career Summary Card (28dp, zero nested LiquidCard)
        UniAnimatedEntrance(order = 0) {
            val cfuPercent = (uiState.progress * 100f).roundToInt().coerceIn(0, 100)
            val targetCfuLabel = uiState.targetCfu.takeIf { it > 0 }?.toString() ?: "—"

            LiquidCard(
                contentPadding = PaddingValues(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                shape = RoundedRectangle(28.dp),
                onClick = onOpenGrades,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.25).sp,
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

                        Spacer(Modifier.width(10.dp))

                        LiquidBadge(
                            text = if (uiState.plannedActivities > 0) {
                                stringResource(Res.string.ui_didactics_activities_badge, uiState.completedExams, uiState.plannedActivities)
                            } else {
                                stringResource(Res.string.ui_didactics_exams_badge, uiState.completedExams)
                            },
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.55f),
                            contentColor = colorScheme.primary,
                            modifier = Modifier
                                .clip(Capsule())
                                .clickable(role = Role.Button, onClick = onOpenTranscripts),
                        )
                    }

                    LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_weighted_average),
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = uiState.average,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1.3).sp,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                )
                                Text(
                                    text = "/ 30",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                (1..uiState.totalYears.coerceIn(1, 4)).forEach { year ->
                                    YearProgress("$year°", year <= uiState.currentYear, colorScheme)
                                }
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            UniCircularProgressRing(
                                progress = uiState.progress,
                                centerText = "$cfuPercent%",
                                subtitleText = "CFU",
                                size = 76.dp,
                                strokeWidth = 7.dp,
                                primaryColor = colorScheme.primary,
                                secondaryColor = colorScheme.tertiary,
                            )
                            Text(
                                text = "${uiState.acquiredCfu} / $targetCfuLabel CFU",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_didactics_career_progress_percent, cfuPercent),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_details),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                            )
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }
        }

        // 2. Carriera e valutazioni in Apple Inset Grouped List (24dp coerente)
        UniAnimatedEntrance(order = 1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_didactics_section_career_evaluations),
                    subtitle = stringResource(Res.string.ui_didactics_section_career_evaluations_sub),
                )

                UniInsetGroup(
                    cornerRadius = 24.dp,
                    animateEntrance = false,
                ) {
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
            }
        }

        // 3. Esami e aula in griglia Bento 2x2 simmetrica (12dp row & column gap)
        UniAnimatedEntrance(order = 2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_didactics_section_exams_classroom),
                    subtitle = stringResource(Res.string.ui_didactics_section_exams_classroom_sub),
                )

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
}
