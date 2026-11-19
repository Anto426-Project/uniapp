package com.anto426.uniapp.ui.didactics.components

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
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.ThesisData
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.model.ProfessorContentItem
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ThesisDetailContent(
    item: ProfessorContentItem,
    thesis: ThesisData,
) {
    val colorScheme = MaterialTheme.colorScheme

    val supervisorRows = buildList {
        thesis.relatore?.takeIf(String::isNotBlank)?.let {
            add(Triple(it, stringResource(Res.string.ui_thesis_supervisor_role), LiquidIcons.AccountCircle))
        }
        thesis.correlatore?.takeIf(String::isNotBlank)?.let {
            add(Triple(it, stringResource(Res.string.ui_thesis_cosupervisor_role), LiquidIcons.AccountCircle))
        }
    }

    val sessionRows = buildList {
        thesis.matricola?.takeIf(String::isNotBlank)?.let {
            add(Triple(stringResource(Res.string.ui_thesis_student_matricola), it, LiquidIcons.Badge))
        }
        thesis.cds?.takeIf(String::isNotBlank)?.let {
            add(Triple(stringResource(Res.string.ui_thesis_degree_course), it, LiquidIcons.MenuBook))
        }
        thesis.sessione?.takeIf(String::isNotBlank)?.let {
            add(Triple(stringResource(Res.string.ui_thesis_degree_session), it, LiquidIcons.Calendar))
        }
        thesis.date?.takeIf(String::isNotBlank)?.let {
            add(Triple(stringResource(Res.string.ui_thesis_defense_date_time), it, LiquidIcons.Time))
        }
        thesis.sede?.takeIf(String::isNotBlank)?.let {
            add(Triple(stringResource(Res.string.ui_thesis_defense_location), it, LiquidIcons.Info))
        }
        thesis.voto?.takeIf(String::isNotBlank)?.let {
            add(Triple(stringResource(Res.string.ui_thesis_outcome_evaluation), it, LiquidIcons.Star))
        }
        thesis.extraFields.forEach { (label, value) ->
            if (value.isNotBlank()) add(Triple(label, value, LiquidIcons.Info))
        }
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
                        text = stringResource(Res.string.ui_academic_tag_thesis),
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )

                    thesis.matricola?.takeIf(String::isNotBlank)?.let { matricola ->
                        LiquidBadge(
                            text = if (matricola.startsWith("matr", ignoreCase = true)) matricola else "matr. $matricola",
                            containerColor = colorScheme.onSurface.copy(alpha = 0.06f),
                            contentColor = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.AccountCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 48.dp,
                            iconSize = 24.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedRectangle(14.dp),
                        ),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = thesis.candidateName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            lineHeight = 26.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = thesis.cds ?: item.subtitle ?: stringResource(Res.string.ui_degree_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (!thesis.date.isNullOrBlank() || !thesis.voto.isNullOrBlank() || !thesis.relatore.isNullOrBlank()) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        thesis.date?.takeIf(String::isNotBlank)?.let { date ->
                            GradesMetricTile(
                                label = stringResource(Res.string.ui_thesis_defense),
                                value = date,
                                valueColor = colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        thesis.voto?.takeIf(String::isNotBlank)?.let { voto ->
                            GradesMetricTile(
                                label = stringResource(Res.string.ui_thesis_final_grade),
                                value = voto,
                                valueColor = colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        thesis.relatore?.takeIf(String::isNotBlank)?.let { rel ->
                            GradesMetricTile(
                                label = stringResource(Res.string.ui_thesis_supervisor),
                                value = rel,
                                valueColor = colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        thesis.thesisTitle?.takeIf(String::isNotBlank)?.let { title ->
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(22.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Assignment,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(Res.string.ui_thesis_title_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }
            }
        }

        if (supervisorRows.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_thesis_supervisors_group),
            )
            UniInsetGroup {
                supervisorRows.forEachIndexed { index, (name, role, icon) ->
                    UniInsetRow(
                        title = name,
                        subtitle = role,
                        icon = icon,
                        showDivider = index < supervisorRows.lastIndex,
                    )
                }
            }
        }

        if (sessionRows.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_thesis_session_group),
            )
            UniInsetGroup {
                sessionRows.forEachIndexed { index, (label, value, icon) ->
                    UniInsetRow(
                        title = label,
                        subtitle = value,
                        icon = icon,
                        showDivider = index < sessionRows.lastIndex,
                    )
                }
            }
        }
    }
}
