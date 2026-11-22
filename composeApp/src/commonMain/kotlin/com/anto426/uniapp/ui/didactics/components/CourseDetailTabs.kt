package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Tab 0: Programma, Obiettivi e Syllabus dettagliato del corso con campi reali Esse3.
 */
@Composable
fun CourseProgramTab(course: StudyCourse) {
    val hasObiettivi = course.obiettivi.isNotBlank()
    val hasContenuti = course.contenuti.isNotBlank() || course.description.isNotBlank()
    val hasMetodi = course.metodiDidattici.isNotBlank()
    val hasVerifica = course.modalitaVerifica.isNotBlank()
    val hasTesti = course.testiRiferimento.isNotBlank()
    val hasPrerequisiti = course.prerequisiti.isNotBlank()

    val hasAnyContent = hasObiettivi || hasContenuti || hasMetodi || hasVerifica || hasTesti || hasPrerequisiti

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_syllabus_title),
            subtitle = stringResource(Res.string.ui_course_syllabus_sub),
        )

        if (!hasAnyContent) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_course_syllabus_title),
                description = stringResource(Res.string.ui_course_syllabus_empty),
            )
            return@Column
        }

        // 1. Obiettivi Formativi
        if (hasObiettivi) {
            SyllabusSectionCard(
                title = stringResource(Res.string.ui_course_objectives_title),
                content = course.obiettivi,
                icon = LiquidIcons.Star,
            )
        }

        // 2. Programma e Contenuti
        if (hasContenuti) {
            val contentText = if (course.contenuti.isNotBlank()) course.contenuti else course.description
            SyllabusSectionCard(
                title = stringResource(Res.string.ui_course_contents_title),
                content = contentText,
                icon = LiquidIcons.MenuBook,
            )
        }

        // 3. Modalità d'Esame / Verifica dell'Apprendimento
        if (hasVerifica) {
            SyllabusSectionCard(
                title = stringResource(Res.string.ui_course_exam_modes_title),
                content = course.modalitaVerifica,
                icon = LiquidIcons.Check,
            )
        }

        // 4. Metodi Didattici
        if (hasMetodi) {
            SyllabusSectionCard(
                title = stringResource(Res.string.ui_course_teaching_methods_title),
                content = course.metodiDidattici,
                icon = LiquidIcons.Assignment,
            )
        }

        // 5. Testi di Riferimento & Bibliografia
        if (hasTesti) {
            SyllabusSectionCard(
                title = stringResource(Res.string.ui_course_materials_title),
                content = course.testiRiferimento,
                icon = LiquidIcons.MenuBook,
            )
        }

        // 6. Prerequisiti & Propedeuticità
        if (hasPrerequisiti) {
            SyllabusSectionCard(
                title = stringResource(Res.string.ui_course_prerequisites_title),
                content = course.prerequisiti,
                icon = LiquidIcons.Lock,
            )
        }
    }
}

/**
 * Tab 1: Docente, Contatti reali della Rubrica d'Ateneo e Ricevimento studenti.
 */
@Composable
fun CourseProfessorTab(
    course: StudyCourse,
    professorContact: ContactData? = null,
    onContactClick: ((ContactData) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current

    val profName = course.professor.ifBlank {
        professorContact?.name ?: stringResource(Res.string.ui_professor)
    }

    if (profName.isBlank() || profName.equals(stringResource(Res.string.ui_professor), ignoreCase = true)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_course_faculty_title),
                subtitle = stringResource(Res.string.ui_course_faculty_sub),
            )
            LiquidEmptyState(
                title = stringResource(Res.string.ui_course_professor_empty_title),
                description = stringResource(Res.string.ui_course_professor_empty_desc),
            )
        }
        return
    }

    val initials = profName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
    val email = professorContact?.email?.ifBlank { null }
        ?: run {
            val parts = profName.lowercase().split(" ").filter { it.length > 1 }
            if (parts.size >= 2) "${parts.first()}.${parts.last()}@unimol.it" else null
        }
    val phone = professorContact?.phone?.ifBlank { null }
    val office = professorContact?.office?.ifBlank { null }
    val department = professorContact?.department?.ifBlank { null } ?: stringResource(Res.string.ui_university)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_faculty_title),
            subtitle = stringResource(Res.string.ui_course_faculty_sub),
        )

        // Card Profilo Docente
        LiquidCard(
            shape = RoundedRectangle(22.dp),
            contentPadding = 18.dp,
            onClick = {
                if (professorContact != null && onContactClick != null) {
                    onContactClick(professorContact)
                }
            },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LiquidAvatar(
                    initials = if (initials.isNotBlank()) initials else "DC",
                    size = 54.dp,
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = profName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = department,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                    if (professorContact != null) {
                        Text(
                            text = professorContact.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                        if (onContactClick != null) {
                            Text(
                                text = stringResource(Res.string.ui_course_professor_open_directory),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                if (professorContact != null && onContactClick != null) {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        val emailLabel = stringResource(Res.string.ui_email)
        val phoneLabel = stringResource(Res.string.ui_phone)
        val officeLabel = stringResource(Res.string.ui_course_office_location)
        val contactRows = buildList {
            if (email != null) add(Triple(emailLabel, email) { uriHandler.openUri("mailto:$email") } to LiquidIcons.Feedback)
            if (phone != null) add(Triple(phoneLabel, phone) { uriHandler.openUri("tel:$phone") } to LiquidIcons.Phone)
            if (office != null) add(Triple(officeLabel, office, null) to LiquidIcons.Home)
        }
        if (contactRows.isNotEmpty()) {
            UniInsetGroup {
                contactRows.forEachIndexed { index, (info, rowIcon) ->
                    val (rowTitle, rowSubtitle, action) = info
                    UniInsetRow(
                        title = rowTitle,
                        subtitle = rowSubtitle,
                        icon = rowIcon,
                        onClick = action,
                        showDivider = index < contactRows.lastIndex,
                    )
                }
            }
        }

        // Ricevimento Studenti Guide
        LiquidCard(
            shape = RoundedRectangle(20.dp),
            contentPadding = 16.dp,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Calendar,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_course_office_hours),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }
                Text(
                    text = stringResource(Res.string.ui_course_office_hours_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

/**
 * Tab 2: Dati Accademici, Tipologia Crediti, Settore Scientifico e Vincoli Reali.
 */
@Composable
fun CourseDataTab(course: StudyCourse) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val isCompleted = course.status == CourseStatus.COMPLETED
    val statusColor = if (isCompleted) glassColors.success else colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_academic_data_title),
            subtitle = stringResource(Res.string.ui_course_academic_data_sub),
        )

        // Stato Carriera & Esito
        LiquidCard(
            shape = RoundedRectangle(20.dp),
            contentPadding = 16.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = if (isCompleted) LiquidIcons.Check else LiquidIcons.Calendar,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 40.dp,
                            iconSize = 20.dp,
                            containerColor = statusColor.copy(alpha = 0.12f),
                            shape = RoundedRectangle(12.dp),
                        ),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(
                                if (isCompleted) Res.string.ui_course_activity_verbalized
                                else Res.string.ui_course_activity_pending,
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = when {
                                isCompleted && !course.examDate.isNullOrBlank() ->
                                    stringResource(Res.string.ui_course_passed_on_date, course.examDate)
                                isCompleted -> stringResource(Res.string.ui_course_passed_success)
                                else -> stringResource(Res.string.ui_course_planned_in_plan)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isCompleted && !course.grade.isNullOrBlank()) {
                    LiquidBadge(
                        text = course.grade,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                }
            }
        }

        val dataRows = listOf(
            Triple(
                stringResource(Res.string.ui_course_scientific_sector),
                stringResource(Res.string.ui_course_ssd_subtitle),
                course.ssd.ifBlank { "—" } to LiquidIcons.Assignment,
            ),
            Triple(
                stringResource(Res.string.ui_course_activity_type),
                stringResource(Res.string.ui_course_taf_subtitle),
                course.taf.ifBlank { stringResource(Res.string.ui_course_activity_characterizing) } to LiquidIcons.Info,
            ),
            Triple(
                stringResource(Res.string.ui_course_credits),
                stringResource(Res.string.ui_course_credits_subtitle),
                course.cfu.ifBlank { "—" } to LiquidIcons.Star,
            ),
            Triple(
                stringResource(Res.string.ui_course_year_label),
                stringResource(Res.string.ui_course_plan_placement),
                when {
                    course.year > 0 -> stringResource(Res.string.ui_year_nth, course.year)
                    course.semester.isNotBlank() -> course.semester
                    else -> "—"
                } to LiquidIcons.Calendar,
            ),
            Triple(
                stringResource(Res.string.ui_course_language),
                stringResource(Res.string.ui_course_lang_material_note),
                course.lingua.ifBlank { stringResource(Res.string.ui_course_lang_italian) } to LiquidIcons.MenuBook,
            ),
            Triple(
                stringResource(Res.string.ui_course_attendance),
                stringResource(Res.string.ui_course_attendance_no_obligation),
                stringResource(Res.string.ui_course_attendance_recommended) to LiquidIcons.Check,
            ),
        )

        UniInsetGroup {
            dataRows.forEachIndexed { index, (label, note, valueAndIcon) ->
                val (valueText, icon) = valueAndIcon
                UniInsetRow(
                    title = label,
                    subtitle = note,
                    icon = icon,
                    showDivider = index < dataRows.lastIndex,
                    trailingContent = {
                        Text(
                            text = valueText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }

        // Propedeuticità Card
        LiquidCard(
            shape = RoundedRectangle(20.dp),
            contentPadding = 16.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Lock,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_course_prerequisites_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }
                Text(
                    text = course.prerequisiti.ifBlank {
                        stringResource(Res.string.ui_course_no_prerequisites)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

/**
 * Reusable Card for Syllabus Sections with rich text and clean styling.
 */
@Composable
private fun SyllabusSectionCard(
    title: String,
    content: String,
    icon: ImageVector,
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        shape = RoundedRectangle(20.dp),
        contentPadding = 16.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.88f),
                lineHeight = 22.sp,
            )
        }
    }
}
