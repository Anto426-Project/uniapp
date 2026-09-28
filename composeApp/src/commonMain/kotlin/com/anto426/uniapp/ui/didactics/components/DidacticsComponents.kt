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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header row with Role Badge
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
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = uiState.degreeDetails.ifBlank { stringResource(Res.string.ui_university) },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LiquidBadge(
                        text = stringResource(Res.string.ui_professor_area_title),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                }

                LiquidHorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Metric Badges row - perfectly distributed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProfessorMetricTile(
                        label = stringResource(Res.string.ui_professor_teachings),
                        value = "${uiState.teachingCount}",
                        icon = LiquidIcons.MenuBook,
                        modifier = Modifier.weight(1f),
                    )
                    ProfessorMetricTile(
                        label = stringResource(Res.string.ui_exams),
                        value = "${uiState.openExamRounds}",
                        icon = LiquidIcons.Calendar,
                        modifier = Modifier.weight(1f),
                    )
                    ProfessorMetricTile(
                        label = stringResource(Res.string.ui_professor_theses),
                        value = "${uiState.thesisCount}",
                        icon = LiquidIcons.Assignment,
                        modifier = Modifier.weight(1f),
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
private fun ProfessorMetricTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = colorScheme.onSurface,
        )
    }
}

@Composable
fun ProfessorMetricBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    LiquidBadge(
        text = text,
        modifier = modifier,
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
