package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.DidacticsDashboardUiState
import com.anto426.uniapp.ui.components.display.UniBentoStatTile
import com.anto426.uniapp.ui.components.items.DidacticItem
import com.anto426.uniapp.ui.components.items.DidacticRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
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
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    UniScreenColumn {
        UniAnimatedEntrance(order = 0) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(24.dp),
                contentPadding = 18.dp,
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
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = uiState.degreeName.ifBlank { stringResource(Res.string.ui_professor_role) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = uiState.degreeDetails.ifBlank { stringResource(Res.string.ui_university) },
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LiquidBadge(
                        text = stringResource(Res.string.ui_professor_area_title),
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    UniBentoStatTile(
                        label = stringResource(Res.string.ui_professor_teachings),
                        value = "${uiState.teachingCount}",
                        icon = LiquidIcons.MenuBook,
                        accentColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    UniBentoStatTile(
                        label = stringResource(Res.string.ui_exams),
                        value = "${uiState.openExamRounds}",
                        icon = LiquidIcons.Calendar,
                        accentColor = colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                    )
                    UniBentoStatTile(
                        label = stringResource(Res.string.ui_professor_theses),
                        value = "${uiState.thesisCount}",
                        icon = LiquidIcons.Assignment,
                        accentColor = colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        }

        UniAnimatedEntrance(order = 1) {
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_professor_didactics_tools),
                    subtitle = stringResource(Res.string.ui_professor_didactics_tools_subtitle),
                )

                DidacticRow(
                    item1 = {
                        DidacticItem(
                            title = stringResource(Res.string.ui_professor_teachings),
                            subtitle = stringResource(Res.string.ui_professor_teachings_subtitle),
                            icon = LiquidIcons.MenuBook,
                            badgeCount = uiState.teachingCount.takeIf { it > 0 },
                            iconColor = colorScheme.primary,
                            onClick = onOpenTeachings,
                        )
                    },
                    item2 = {
                        DidacticItem(
                            title = stringResource(Res.string.ui_professor_exam_rounds),
                            subtitle = stringResource(Res.string.ui_professor_exam_rounds_subtitle),
                            icon = LiquidIcons.Calendar,
                            badgeCount = uiState.openExamRounds.takeIf { it > 0 },
                            iconColor = colorScheme.tertiary,
                            onClick = onOpenExams,
                        )
                    },
                )
            }
        }

        UniAnimatedEntrance(order = 2) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_theses),
                        subtitle = stringResource(Res.string.ui_professor_theses_subtitle),
                        icon = LiquidIcons.Assignment,
                        badgeCount = uiState.thesisCount.takeIf { it > 0 },
                        iconColor = glassColors.warning,
                        onClick = onOpenTheses,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_professor_reports),
                        subtitle = stringResource(Res.string.ui_professor_reports_subtitle),
                        icon = LiquidIcons.Edit,
                        badgeCount = uiState.reportCount.takeIf { it > 0 },
                        iconColor = colorScheme.secondary,
                        onClick = onOpenReports,
                    )
                },
            )
        }

        UniAnimatedEntrance(order = 3) {
            DidacticRow(
                item1 = {
                    DidacticItem(
                        title = stringResource(Res.string.nav_route_news_title),
                        subtitle = stringResource(Res.string.nav_route_news_subtitle),
                        icon = LiquidIcons.Notifications,
                        iconColor = colorScheme.primary,
                        onClick = onOpenNews,
                    )
                },
                item2 = {
                    DidacticItem(
                        title = stringResource(Res.string.ui_settings),
                        subtitle = stringResource(Res.string.ui_professor_settings_subtitle),
                        icon = LiquidIcons.Settings,
                        iconColor = colorScheme.tertiary,
                        onClick = onOpenSettings,
                    )
                },
            )
        }
    }
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
