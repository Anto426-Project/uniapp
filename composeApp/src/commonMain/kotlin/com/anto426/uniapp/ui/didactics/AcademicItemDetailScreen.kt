package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.anto426.unisdk.backend.model.ProfessorContentItem
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AcademicItemDetailScreen(
    backdropState: Backdrop,
    uiState: AcademicItemDetailUiState,
    section: AcademicSection,
    onTabSelected: (Int) -> Unit,
) {
    val item = uiState.item ?: return
    if (section == AcademicSection.ExamRounds) {
        ProfessorExamDetailContent(
            backdropState = backdropState,
            item = item,
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected,
        )
        return
    }

    if (section == AcademicSection.Theses) {
        ThesisDetailContent(
            backdropState = backdropState,
            item = item,
        )
        return
    }

    val colorScheme = MaterialTheme.colorScheme
    val fields = item.orderedAcademicDetailFields(section)

    val sectionTag = when (section) {
        AcademicSection.Theses -> "TESISTA • LAUREANDO"
        AcademicSection.Teachings -> "INSEGNAMENTO"
        AcademicSection.Reports -> "VERBALE DI COMMISSIONE"
        AcademicSection.ExamRounds -> "APPELLO D'ESAME"
    }

    UniScreenColumn {
        // Hero Header Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
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
                        backdropState = backdropState,
                    )

                    item.code?.takeIf(String::isNotBlank)?.let { code ->
                        LiquidBadge(
                            text = code,
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = colorScheme.primary,
                            backdropState = backdropState,
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
                backdropState = backdropState,
            ) {
                fields.forEach { (label, value) ->
                    LiquidPreferenceItem(
                        title = label,
                        subtitle = value,
                        icon = LiquidIcons.Info,
                        backdropState = backdropState,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThesisDetailContent(
    backdropState: Backdrop,
    item: ProfessorContentItem,
) {
    val colorScheme = MaterialTheme.colorScheme
    val thesis = extractThesisData(item)

    UniScreenColumn {
        // ==========================================
        // 1. HERO THESIS CARD
        // ==========================================
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
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
                        backdropState = backdropState,
                    )

                    thesis.matricola?.takeIf(String::isNotBlank)?.let { matricola ->
                        LiquidBadge(
                            text = if (matricola.startsWith("matr", ignoreCase = true)) matricola else "matr. $matricola",
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = colorScheme.primary,
                            backdropState = backdropState,
                        )
                    }
                }

                // Candidate Name & Degree
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = thesis.candidateName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                    )
                    thesis.cds?.takeIf(String::isNotBlank)?.let { cds ->
                        Text(
                            text = cds,
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

        // ==========================================
        // 2. THESIS TITLE CARD (HIGHLIGHTED)
        // ==========================================
        thesis.thesisTitle?.takeIf(String::isNotBlank)?.let { title ->
            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(20.dp),
                contentPadding = 18.dp,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Assignment,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 32.dp,
                                iconSize = 16.dp,
                                containerColor = colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                            ),
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
                backdropState = backdropState,
            ) {
                thesis.relatore?.let { rel ->
                    LiquidPreferenceItem(
                        title = rel,
                        subtitle = stringResource(Res.string.ui_thesis_supervisor_role),
                        icon = LiquidIcons.AccountCircle,
                        backdropState = backdropState,
                    )
                }
                thesis.correlatore?.let { correl ->
                    LiquidPreferenceItem(
                        title = correl,
                        subtitle = stringResource(Res.string.ui_thesis_cosupervisor_role),
                        icon = LiquidIcons.AccountCircle,
                        backdropState = backdropState,
                    )
                }
            }
        }

        // ==========================================
        // 4. DATI CANDIDATO E SESSIONE
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_thesis_session_group),
            backdropState = backdropState,
        ) {
            thesis.matricola?.let { matr ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_student_matricola),
                    subtitle = matr,
                    icon = LiquidIcons.Badge,
                    backdropState = backdropState,
                )
            }

            thesis.cds?.let { cds ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_degree_course),
                    subtitle = cds,
                    icon = LiquidIcons.MenuBook,
                    backdropState = backdropState,
                )
            }

            thesis.sessione?.let { sess ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_degree_session),
                    subtitle = sess,
                    icon = LiquidIcons.Calendar,
                    backdropState = backdropState,
                )
            }

            thesis.date?.let { date ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_defense_date_time),
                    subtitle = date,
                    icon = LiquidIcons.Time,
                    backdropState = backdropState,
                )
            }

            thesis.sede?.let { sede ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_defense_location),
                    subtitle = sede,
                    icon = LiquidIcons.Info,
                    backdropState = backdropState,
                )
            }

            thesis.voto?.let { voto ->
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_thesis_outcome_evaluation),
                    subtitle = voto,
                    icon = LiquidIcons.Star,
                    backdropState = backdropState,
                )
            }

            thesis.extraFields.forEach { (label, value) ->
                LiquidPreferenceItem(
                    title = label,
                    subtitle = value,
                    icon = LiquidIcons.Info,
                    backdropState = backdropState,
                )
            }
        }
    }
}

@Composable
private fun ThesisHeroStatTile(
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

private data class ThesisData(
    val candidateName: String,
    val thesisTitle: String? = null,
    val relatore: String? = null,
    val correlatore: String? = null,
    val matricola: String? = null,
    val cds: String? = null,
    val date: String? = null,
    val sede: String? = null,
    val voto: String? = null,
    val sessione: String? = null,
    val extraFields: List<Pair<String, String>> = emptyList(),
)

private fun extractThesisData(item: ProfessorContentItem): ThesisData {
    val fields =
        item.fields.map { it.label to it.value }.ifEmpty { item.detail.toAcademicDetailFields() }
    val map = fields.associate { (k, v) -> k.lowercase().trim() to v.trim() }

    val thesisTitle =
        map["titolo tesi"]
            ?: map["titolo"]
            ?: map["argomento"]
            ?: map["elaborato"]
            ?: map["titolo elaborato"]

    val relatore =
        map["relatore"]
            ?: map["docente relatore"]
            ?: map["professore relatore"]
            ?: map["relatore tesi"]

    val correlatore =
        map["correlatore"]
            ?: map["docente correlatore"]
            ?: map["secondo relatore"]

    val matricola =
        map["matricola"]
            ?: map["matr."]
            ?: map["matr"]
            ?: item.code

    val cds =
        map["corso di laurea"]
            ?: map["cds"]
            ?: map["corso"]
            ?: map["percorso"]
            ?: item.subtitle

    val date =
        map["data discussione"]
            ?: map["discussione"]
            ?: map["data tesi"]
            ?: map["data"]
            ?: item.date

    val sede =
        map["sede"]
            ?: map["aula"]
            ?: map["luogo"]

    val voto =
        map["voto"]
            ?: map["voto finale"]
            ?: map["punteggio"]
            ?: map["esito"]

    val sessione =
        map["sessione"]
            ?: map["sessione di laurea"]
            ?: map["appello"]

    val standardKeys =
        setOf(
            "titolo tesi", "titolo", "argomento", "elaborato", "titolo elaborato",
            "relatore", "docente relatore", "professore relatore", "relatore tesi",
            "correlatore", "docente correlatore", "secondo relatore",
            "matricola", "matr.", "matr",
            "corso di laurea", "cds", "corso", "percorso",
            "data discussione", "discussione", "data tesi", "data",
            "sede", "aula", "luogo",
            "voto", "voto finale", "punteggio", "esito",
            "sessione", "sessione di laurea", "appello",
            "cognome", "nome", "studente", "candidato",
        )

    val extraFields = fields.filterNot { (k, _) -> standardKeys.contains(k.lowercase().trim()) }

    return ThesisData(
        candidateName = item.title,
        thesisTitle = thesisTitle,
        relatore = relatore,
        correlatore = correlatore,
        matricola = matricola,
        cds = cds,
        date = date,
        sede = sede,
        voto = voto,
        sessione = sessione,
        extraFields = extraFields,
    )
}

private fun ProfessorContentItem.orderedAcademicDetailFields(
    section: AcademicSection,
): List<Pair<String, String>> {
    val parsed =
        fields.map { it.label to it.value }.ifEmpty { detail.toAcademicDetailFields() }
    if (section != AcademicSection.Theses) return parsed
    val priorities =
        listOf("data", "discussione", "cognome", "nome", "matricola", "percorso", "cds", "argomento", "titolo tesi", "titolo", "voto")
    return parsed
        .map { (label, value) ->
            val normalizedLabel =
                when (label.lowercase()) {
                    "discussione", "data tesi" -> "Data Discussione"
                    "titolo tesi" -> "Titolo Tesi"
                    "cds" -> "Corso di Laurea"
                    else -> label
                }
            normalizedLabel to value
        }
        .sortedBy { (label, _) ->
            priorities.indexOf(label.lowercase()).takeIf { it >= 0 } ?: Int.MAX_VALUE
        }
}

internal fun String?.toAcademicDetailFields(): List<Pair<String, String>> =
    orEmpty()
        .lineSequence()
        .mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0 || separator == line.lastIndex) return@mapNotNull null
            val label = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim()
            if (label.isBlank() || value.isBlank()) null else label to value
        }
        .toList()
