package com.anto426.uniapp.ui.didactics.components

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
import com.anto426.uniapp.model.didactics.ThesisData
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.backend.model.ProfessorContentItem
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ThesisDetailContent(
    item: ProfessorContentItem,
    thesis: ThesisData,
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // ==========================================
        // 1. HERO THESIS CARD
        // ==========================================
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Top Badge Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidBadge(
                        text = stringResource(Res.string.ui_academic_tag_thesis),
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = colorScheme.primary,
                    )

                    thesis.matricola?.takeIf(String::isNotBlank)?.let { matricola ->
                        LiquidBadge(
                            text = if (matricola.startsWith("matr", ignoreCase = true)) matricola else "matr. $matricola",
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = colorScheme.primary,
                        )
                    }
                }

                // Candidate Name & Course of Study
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = LiquidIcons.AccountCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 48.dp,
                            iconSize = 24.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedRectangle(14.dp),
                        ),
                    )

                    Spacer(modifier = Modifier.size(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = thesis.candidateName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            lineHeight = 26.sp,
                        )
                        Text(
                            text = thesis.cds ?: item.subtitle ?: stringResource(Res.string.ui_degree_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                // Stat Tiles Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    thesis.date?.takeIf(String::isNotBlank)?.let { date ->
                        ThesisHeroStatTile(
                            label = stringResource(Res.string.ui_thesis_defense),
                            value = date,
                            icon = LiquidIcons.Calendar,
                        )
                    }

                    thesis.voto?.takeIf(String::isNotBlank)?.let { voto ->
                        ThesisHeroStatTile(
                            label = stringResource(Res.string.ui_thesis_final_grade),
                            value = voto,
                            icon = LiquidIcons.Star,
                        )
                    }

                    if (thesis.relatore != null) {
                        ThesisHeroStatTile(
                            label = stringResource(Res.string.ui_thesis_supervisor),
                            value = thesis.relatore,
                            icon = LiquidIcons.AccountCircle,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ==========================================
        // 2. TITOLO & ARGOMENTO ELABORATO
        // ==========================================
        thesis.thesisTitle?.let { title ->
            LiquidCard(
                shape = RoundedRectangle(20.dp),
                contentPadding = 18.dp,
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

        // ==========================================
        // 3. RELATORI & COMMISSIONE
        // ==========================================
        if (thesis.relatore != null || thesis.correlatore != null) {
            LiquidPreferenceGroup(
                title = stringResource(Res.string.ui_thesis_supervisors_group),
            ) {
                thesis.relatore?.let { rel ->
                    LiquidPreferenceItem(
                        title = rel,
                        subtitle = stringResource(Res.string.ui_thesis_supervisor_role),
                        icon = LiquidIcons.AccountCircle,
                    )
                }
                thesis.correlatore?.let { correl ->
                    LiquidPreferenceItem(
                        title = correl,
                        subtitle = stringResource(Res.string.ui_thesis_cosupervisor_role),
                        icon = LiquidIcons.AccountCircle,
                    )
                }
            }
        }

        // ==========================================
        // 4. DATI CANDIDATO E SESSIONE
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_thesis_session_group),
        ) {
            thesis.matricola?.let { matr ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_student_matricola),
                    subtitle = matr,
                    icon = LiquidIcons.Badge,
                )
            }

            thesis.cds?.let { cds ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_degree_course),
                    subtitle = cds,
                    icon = LiquidIcons.MenuBook,
                )
            }

            thesis.sessione?.let { sess ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_degree_session),
                    subtitle = sess,
                    icon = LiquidIcons.Calendar,
                )
            }

            thesis.date?.let { date ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_defense_date_time),
                    subtitle = date,
                    icon = LiquidIcons.Time,
                )
            }

            thesis.sede?.let { sede ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_defense_location),
                    subtitle = sede,
                    icon = LiquidIcons.Info,
                )
            }

            thesis.voto?.let { voto ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_outcome_evaluation),
                    subtitle = voto,
                    icon = LiquidIcons.Star,
                )
            }

            thesis.extraFields.forEach { (label, value) ->
                LiquidPreferenceItem(
                    title = label,
                    subtitle = value,
                    icon = LiquidIcons.Info,
                )
            }
        }
    }
}

@Composable
fun ThesisHeroStatTile(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}
