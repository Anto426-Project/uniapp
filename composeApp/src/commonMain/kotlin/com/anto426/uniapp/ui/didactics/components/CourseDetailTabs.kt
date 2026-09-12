package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.model.services.ContactData
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
                description = "I contenuti e gli obiettivi didattici per questo insegnamento non sono ancora stati registrati nel portale d'Ateneo.",
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
                title = "Programma & Contenuti",
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
                title = "Metodi Didattici",
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
                title = "Docente non specificato",
                description = "Nessun docente associato a questo insegnamento nei registri Esse3.",
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

        // Quick Contact Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Email Button
            if (email != null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedRectangle(18.dp))
                        .background(colorScheme.onSurface.copy(alpha = 0.04f))
                        .border(BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)), RoundedRectangle(18.dp))
                        .clickable { uriHandler.openUri("mailto:$email") }
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Email",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Icon(
                                imageVector = LiquidIcons.Feedback,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Text(
                            text = "Tocca per scrivere",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary,
                        )
                    }
                }
            }

            // Phone Button (if present)
            if (phone != null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedRectangle(18.dp))
                        .background(colorScheme.onSurface.copy(alpha = 0.04f))
                        .border(BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)), RoundedRectangle(18.dp))
                        .clickable { uriHandler.openUri("tel:$phone") }
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Telefono",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Icon(
                                imageVector = LiquidIcons.Phone,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Text(
                            text = "Chiama interno",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary,
                        )
                    }
                }
            }
        }

        // Office / Location (if present)
        if (office != null) {
            LiquidCard(
                shape = RoundedRectangle(20.dp),
                contentPadding = 16.dp,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Home,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 36.dp,
                            iconSize = 18.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedRectangle(10.dp),
                        ),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(Res.string.ui_course_office_location),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = office,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                        )
                    }
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
                    text = "Il ricevimento studenti si tiene previo accordo via email istituzionale con il docente, in presenza presso lo studio del dipartimento oppure online tramite Microsoft Teams o Google Meet.",
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
    val isCompleted = course.status == CourseStatus.COMPLETED

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
                        tint = if (isCompleted) colorScheme.primary else colorScheme.secondary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 40.dp,
                            iconSize = 20.dp,
                            containerColor = if (isCompleted) colorScheme.primary.copy(alpha = 0.12f)
                            else colorScheme.secondary.copy(alpha = 0.12f),
                            shape = RoundedRectangle(12.dp),
                        ),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isCompleted) "Attività Verbalizzata" else "Attività da Sostenere",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = buildString {
                                if (isCompleted) {
                                    append("Superato con successo")
                                    if (!course.examDate.isNullOrBlank()) append(" il ${course.examDate}")
                                } else {
                                    append("Pianificato nel piano di studi")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isCompleted && !course.grade.isNullOrBlank()) {
                    LiquidBadge(
                        text = course.grade,
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.primary,
                    )
                }
            }
        }

        // Academic details 2 a 2
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_scientific_sector),
                    value = course.ssd.ifBlank { "—" },
                    subvalue = "Settore Disciplinare (SSD)",
                    modifier = Modifier.weight(1f),
                )
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_activity_type),
                    value = course.taf.ifBlank { "Caratterizzante" },
                    subvalue = "Ambito formativo (TAF)",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_credits),
                    value = course.cfu.ifBlank { "—" },
                    subvalue = "Crediti Universitari",
                    modifier = Modifier.weight(1f),
                )
                AcademicInfoTile(
                    label = "Anno di Corso",
                    value = if (course.year > 0) "${course.year}° Anno" else if (course.semester.isNotBlank()) course.semester else "—",
                    subvalue = "Collocazione piano di studio",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_language),
                    value = course.lingua.ifBlank { stringResource(Res.string.ui_course_lang_italian) },
                    subvalue = stringResource(Res.string.ui_course_lang_material_note),
                    modifier = Modifier.weight(1f),
                )
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_attendance),
                    value = stringResource(Res.string.ui_course_attendance_recommended),
                    subvalue = stringResource(Res.string.ui_course_attendance_no_obligation),
                    modifier = Modifier.weight(1f),
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

/**
 * Reusable Hero Stat Tile for the Top Course Header Card with translucent styling.
 */
@Composable
fun CourseHeroStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedRectangle(16.dp))
            .background(colorScheme.onSurface.copy(alpha = 0.04f))
            .border(BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)), RoundedRectangle(16.dp))
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

/**
 * Reusable Academic Info Tile for 2 a 2 Course Data Grid.
 */
@Composable
fun AcademicInfoTile(
    label: String,
    value: String,
    subvalue: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedRectangle(18.dp))
            .background(colorScheme.onSurface.copy(alpha = 0.04f))
            .border(BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.25f)), RoundedRectangle(18.dp))
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = subvalue,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                maxLines = 1,
            )
        }
    }
}
