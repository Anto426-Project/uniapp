package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.accordion.LiquidExpandableCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.data.contract.contactDialUri
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.document.components.RichMarkdownDocument
import io.ktor.http.encodeURLParameter
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private data class SyllabusSectionSpec(
    val title: String,
    val content: String,
    val icon: ImageVector,
    val accent: Color,
    val preserveLineBreaksAsList: Boolean = false,
)

private data class CourseRowSpec(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val badgeText: String? = null,
    val onClick: (() -> Unit)? = null,
)

/**
 * Sezione Docente integrata nella scheda unica del corso (stile UniInsetGroup come Info/Contatti).
 */
@Composable
fun CourseFacultySection(
    course: StudyCourse,
    professorContact: ContactData? = null,
    onContactClick: ((ContactData) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val uriHandler = LocalUriHandler.current
    val professorFallback = stringResource(Res.string.ui_professor)

    val professorNames = remember(course.professor) {
        course.professor.split(",", ";", "/").map(String::trim).filter(String::isNotBlank)
    }
    val primaryProfName = professorContact?.name?.takeIf(String::isNotBlank)
        ?: professorNames.firstOrNull()
        ?: ""
    if (primaryProfName.isBlank() || primaryProfName.equals(professorFallback, ignoreCase = true)) {
        return
    }

    val additionalProfessors = remember(professorNames, primaryProfName) {
        professorNames.filterNot { it.equals(primaryProfName, ignoreCase = true) }
    }
    val primarySubtitle = listOfNotNull(
        professorContact?.role?.takeIf(String::isNotBlank),
        professorContact?.department?.takeIf(String::isNotBlank),
    ).distinct().joinToString(" • ").ifBlank {
        if (professorContact != null && onContactClick != null) {
            stringResource(Res.string.ui_course_professor_open_directory)
        } else {
            stringResource(Res.string.ui_professor)
        }
    }

    val email = professorContact?.email?.ifBlank { null }
    val emailUri = email?.let { "mailto:${it.encodeURLParameter(spaceToPlus = false)}" }
    val phone = professorContact?.phone?.ifBlank { null }
    val phoneUri = professorContact?.phoneNumbers?.firstNotNullOfOrNull(String::contactDialUri)
        ?: phone?.contactDialUri()
    val office = professorContact?.office?.ifBlank { null }
    val officeHours = professorContact?.officeHours?.ifBlank { null }

    val emailLabel = stringResource(Res.string.ui_email)
    val phoneLabel = stringResource(Res.string.ui_phone)
    val officeLabel = stringResource(Res.string.ui_course_office_location)
    val officeHoursLabel = stringResource(Res.string.ui_course_office_hours)

    val rows = buildList {
        add(
            CourseRowSpec(
                title = primaryProfName,
                subtitle = primarySubtitle,
                icon = LiquidIcons.AccountCircle,
                tint = colorScheme.primary,
                onClick = if (professorContact != null && onContactClick != null) {
                    { onContactClick(professorContact) }
                } else null,
            ),
        )
        additionalProfessors.forEach { extraProf ->
            add(
                CourseRowSpec(
                    title = extraProf,
                    subtitle = professorFallback,
                    icon = LiquidIcons.AccountCircle,
                    tint = colorScheme.tertiary,
                ),
            )
        }
        if (email != null && emailUri != null) {
            add(
                CourseRowSpec(
                    title = emailLabel,
                    subtitle = email,
                    icon = LiquidIcons.Feedback,
                    tint = colorScheme.primary,
                    onClick = { runCatching { uriHandler.openUri(emailUri) } },
                ),
            )
        }
        if (phone != null && phoneUri != null) {
            add(
                CourseRowSpec(
                    title = phoneLabel,
                    subtitle = phone,
                    icon = LiquidIcons.Phone,
                    tint = glassColors.success,
                    onClick = { runCatching { uriHandler.openUri(phoneUri) } },
                ),
            )
        }
        if (office != null) {
            add(
                CourseRowSpec(
                    title = officeLabel,
                    subtitle = office,
                    icon = LiquidIcons.Home,
                    tint = colorScheme.tertiary,
                ),
            )
        }
        if (officeHours != null) {
            add(
                CourseRowSpec(
                    title = officeHoursLabel,
                    subtitle = officeHours,
                    icon = LiquidIcons.Calendar,
                    tint = colorScheme.secondary,
                ),
            )
        }
    }

    UniInsetGroup(
        title = stringResource(Res.string.ui_course_faculty_title),
        entranceOrder = 1,
    ) {
        rows.forEachIndexed { index, row ->
            UniInsetRow(
                title = row.title,
                subtitle = row.subtitle,
                icon = row.icon,
                iconTint = row.tint,
                onClick = row.onClick,
                showDivider = index < rows.lastIndex,
                titleMaxLines = 1,
                subtitleMaxLines = 1,
                minHeight = 64.dp,
            )
        }
    }
}

/**
 * Dati accademici essenziali e link al Course Catalogue in un unico blocco compatto (stile AppInfoScreen).
 */
@Composable
fun CourseAcademicInfoSection(course: StudyCourse) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val uriHandler = LocalUriHandler.current

    val safeCourseUrl = course.courseUrl?.trim()?.takeIf {
        it.startsWith("https://", ignoreCase = true) && it.length > 8
    }

    val ssdTitle = stringResource(Res.string.ui_course_scientific_sector)
    val ssdSub = stringResource(Res.string.ui_course_ssd_subtitle)
    val tafTitle = stringResource(Res.string.ui_course_activity_type)
    val tafSub = stringResource(Res.string.ui_course_taf_subtitle)
    val tafFallback = stringResource(Res.string.ui_course_activity_characterizing)
    val langTitle = stringResource(Res.string.ui_course_language)
    val langSub = stringResource(Res.string.ui_course_lang_material_note)
    val langFallback = stringResource(Res.string.ui_course_lang_italian)
    val attendanceTitle = stringResource(Res.string.ui_course_attendance)
    val attendanceSub = stringResource(Res.string.ui_course_attendance_no_obligation)
    val attendanceValue = stringResource(Res.string.ui_course_attendance_recommended)
    val portalTitle = stringResource(Res.string.ui_course_open_catalogue_title)
    val portalSub = stringResource(Res.string.ui_course_open_catalogue_sub)
    val portalBadge = stringResource(Res.string.ui_course_open_catalogue_badge)

    val rows = buildList {
        if (course.ssd.isNotBlank()) {
            add(
                CourseRowSpec(
                    title = ssdTitle,
                    subtitle = ssdSub,
                    icon = LiquidIcons.Assignment,
                    tint = colorScheme.primary,
                    badgeText = course.ssd,
                ),
            )
        }
        add(
            CourseRowSpec(
                title = tafTitle,
                subtitle = tafSub,
                icon = LiquidIcons.Info,
                tint = colorScheme.tertiary,
                badgeText = course.taf.ifBlank { tafFallback },
            ),
        )
        add(
            CourseRowSpec(
                title = langTitle,
                subtitle = langSub,
                icon = LiquidIcons.MenuBook,
                tint = colorScheme.secondary,
                badgeText = course.lingua.ifBlank { langFallback },
            ),
        )
        add(
            CourseRowSpec(
                title = attendanceTitle,
                subtitle = attendanceSub,
                icon = LiquidIcons.Check,
                tint = glassColors.success,
                badgeText = attendanceValue,
            ),
        )
        if (safeCourseUrl != null) {
            add(
                CourseRowSpec(
                    title = portalTitle,
                    subtitle = portalSub,
                    icon = LiquidIcons.Share,
                    tint = colorScheme.tertiary,
                    badgeText = portalBadge,
                    onClick = { runCatching { uriHandler.openUri(safeCourseUrl) } },
                ),
            )
        }
    }

    UniInsetGroup(
        title = stringResource(Res.string.ui_course_academic_data_title),
        entranceOrder = 2,
    ) {
        rows.forEachIndexed { index, row ->
            UniInsetRow(
                title = row.title,
                subtitle = row.subtitle,
                icon = row.icon,
                iconTint = row.tint,
                onClick = row.onClick,
                showDivider = index < rows.lastIndex,
                titleMaxLines = 1,
                subtitleMaxLines = 1,
                minHeight = 64.dp,
                trailingContent = row.badgeText?.let { badge ->
                    {
                        LiquidBadge(
                            text = badge,
                            containerColor = row.tint.copy(alpha = 0.14f),
                            contentColor = row.tint,
                        )
                    }
                },
            )
        }
    }
}

/**
 * Programma, Obiettivi e Syllabus in formato editoriale senza card annidate (come Info / Documenti / News)
 * con colori semantici dell'app per ogni sezione.
 */
@Composable
fun CourseSyllabusSection(course: StudyCourse) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val obiettiviTitle = stringResource(Res.string.ui_course_objectives_title)
    val contenutiTitle = stringResource(Res.string.ui_course_contents_title)
    val verificaTitle = stringResource(Res.string.ui_course_exam_modes_title)
    val metodiTitle = stringResource(Res.string.ui_course_teaching_methods_title)
    val testiTitle = stringResource(Res.string.ui_course_materials_title)
    val prerequisitiTitle = stringResource(Res.string.ui_course_prerequisites_title)

    val sections = buildList {
        if (course.obiettivi.isNotBlank()) {
            add(SyllabusSectionSpec(obiettiviTitle, course.obiettivi, LiquidIcons.Star, colorScheme.primary))
        }
        val contentText = if (course.contenuti.isNotBlank()) course.contenuti else course.description
        if (contentText.isNotBlank()) {
            add(SyllabusSectionSpec(contenutiTitle, contentText, LiquidIcons.MenuBook, colorScheme.tertiary))
        }
        if (course.modalitaVerifica.isNotBlank()) {
            add(SyllabusSectionSpec(verificaTitle, course.modalitaVerifica, LiquidIcons.Check, glassColors.success))
        }
        if (course.metodiDidattici.isNotBlank()) {
            add(SyllabusSectionSpec(metodiTitle, course.metodiDidattici, LiquidIcons.Assignment, colorScheme.secondary))
        }
        if (course.testiRiferimento.isNotBlank()) {
            add(
                SyllabusSectionSpec(
                    title = testiTitle,
                    content = course.testiRiferimento,
                    icon = LiquidIcons.MenuBook,
                    accent = colorScheme.primary,
                    preserveLineBreaksAsList = true,
                ),
            )
        }
        if (course.prerequisiti.isNotBlank()) {
            add(SyllabusSectionSpec(prerequisitiTitle, course.prerequisiti, LiquidIcons.Lock, glassColors.warning))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_syllabus_title),
            subtitle = stringResource(Res.string.ui_course_syllabus_sub),
        )

        if (sections.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_course_syllabus_title),
                description = stringResource(Res.string.ui_course_syllabus_empty),
            )
        } else {
            val courseKey = course.id.ifBlank { course.name }
            sections.forEachIndexed { index, spec ->
                val formattedMarkdown = remember(spec.content, spec.preserveLineBreaksAsList) {
                    CourseSyllabusFormatter.formatSectionMarkdown(
                        raw = spec.content,
                        preserveLineBreaksAsList = spec.preserveLineBreaksAsList,
                    ).ifBlank { spec.content.trim() }
                }
                var expanded by rememberSaveable(courseKey, spec.title) { mutableStateOf(index == 0) }
                UniAnimatedEntrance(order = uniListEntranceOrder(index, offset = 2)) {
                    LiquidExpandableCard(
                        title = spec.title,
                        leadingIcon = spec.icon,
                        isExpanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        RichMarkdownDocument(
                            markdown = formattedMarkdown,
                            accentColor = spec.accent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
