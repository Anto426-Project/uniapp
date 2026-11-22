package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.document.components.RichMarkdownDocument
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private data class SyllabusSectionSpec(
    val title: String,
    val shortChip: String,
    val content: String,
    val icon: ImageVector,
    val accent: Color,
    val preserveLineBreaksAsList: Boolean = false,
)

/**
 * Tab 0: Programma, Obiettivi e Syllabus dettagliato del corso con palette semantica coerente con Info.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseProgramTab(course: StudyCourse) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val uriHandler = LocalUriHandler.current

    val obiettiviTitle = stringResource(Res.string.ui_course_objectives_title)
    val contenutiTitle = stringResource(Res.string.ui_course_contents_title)
    val verificaTitle = stringResource(Res.string.ui_course_exam_modes_title)
    val metodiTitle = stringResource(Res.string.ui_course_teaching_methods_title)
    val testiTitle = stringResource(Res.string.ui_course_materials_title)
    val prerequisitiTitle = stringResource(Res.string.ui_course_prerequisites_title)

    val sections = buildList {
        if (course.obiettivi.isNotBlank()) {
            add(
                SyllabusSectionSpec(
                    title = obiettiviTitle,
                    shortChip = obiettiviTitle.substringBefore('&').trim(),
                    content = course.obiettivi,
                    icon = LiquidIcons.Star,
                    accent = colorScheme.primary,
                ),
            )
        }
        val contentText = if (course.contenuti.isNotBlank()) course.contenuti else course.description
        if (contentText.isNotBlank()) {
            add(
                SyllabusSectionSpec(
                    title = contenutiTitle,
                    shortChip = contenutiTitle,
                    content = contentText,
                    icon = LiquidIcons.MenuBook,
                    accent = colorScheme.tertiary,
                ),
            )
        }
        if (course.modalitaVerifica.isNotBlank()) {
            add(
                SyllabusSectionSpec(
                    title = verificaTitle,
                    shortChip = verificaTitle.substringBefore('&').trim(),
                    content = course.modalitaVerifica,
                    icon = LiquidIcons.Check,
                    accent = glassColors.success,
                ),
            )
        }
        if (course.metodiDidattici.isNotBlank()) {
            add(
                SyllabusSectionSpec(
                    title = metodiTitle,
                    shortChip = metodiTitle,
                    content = course.metodiDidattici,
                    icon = LiquidIcons.Assignment,
                    accent = colorScheme.secondary,
                ),
            )
        }
        if (course.testiRiferimento.isNotBlank()) {
            add(
                SyllabusSectionSpec(
                    title = testiTitle,
                    shortChip = testiTitle.substringBefore('&').trim(),
                    content = course.testiRiferimento,
                    icon = LiquidIcons.MenuBook,
                    accent = colorScheme.primary,
                    preserveLineBreaksAsList = true,
                ),
            )
        }
        if (course.prerequisiti.isNotBlank()) {
            add(
                SyllabusSectionSpec(
                    title = prerequisitiTitle,
                    shortChip = prerequisitiTitle,
                    content = course.prerequisiti,
                    icon = LiquidIcons.Lock,
                    accent = glassColors.warning,
                ),
            )
        }
    }

    val safeCourseUrl = course.courseUrl?.trim()?.takeIf {
        it.startsWith("https://", ignoreCase = true) && it.length > 8
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
            // Compact color-coded section pills overview (matching AppInfo palette)
            if (sections.size >= 2) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    sections.forEachIndexed { index, spec ->
                        Row(
                            modifier = Modifier
                                .clip(Capsule())
                                .background(spec.accent.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = spec.accent,
                            )
                            Text(
                                text = spec.shortChip,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface.copy(alpha = 0.86f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            sections.forEachIndexed { index, spec ->
                UniAnimatedEntrance(order = uniListEntranceOrder(index)) {
                    SyllabusSectionCard(
                        index = index + 1,
                        title = spec.title,
                        content = spec.content,
                        icon = spec.icon,
                        accentColor = spec.accent,
                        preserveLineBreaksAsList = spec.preserveLineBreaksAsList,
                    )
                }
            }
        }

        if (safeCourseUrl != null) {
            CourseCataloguePortalCard(
                accentColor = colorScheme.tertiary,
                onClick = { runCatching { uriHandler.openUri(safeCourseUrl) } },
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
    val glassColors = LiquidGlassTheme.colors
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_faculty_title),
            subtitle = stringResource(Res.string.ui_course_faculty_sub),
        )

        // Elevated Professor Hero Card
        UniAnimatedEntrance(order = 0) {
            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = PaddingValues(18.dp),
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
                        size = 56.dp,
                    )

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (professorContact != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                LiquidBadge(
                                    text = professorContact.role,
                                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                    contentColor = colorScheme.primary,
                                )
                                if (onContactClick != null) {
                                    Text(
                                        text = stringResource(Res.string.ui_course_professor_open_directory),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    if (professorContact != null && onContactClick != null) {
                        Icon(
                            imageVector = LiquidIcons.ChevronRight,
                            contentDescription = null,
                            tint = colorScheme.primary.copy(alpha = 0.72f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // Semantic Multi-Accent Contact Rows (matching AppInfoScreen)
        val emailLabel = stringResource(Res.string.ui_email)
        val phoneLabel = stringResource(Res.string.ui_phone)
        val officeLabel = stringResource(Res.string.ui_course_office_location)
        data class ContactRowSpec(
            val title: String,
            val subtitle: String,
            val icon: ImageVector,
            val tint: Color,
            val action: (() -> Unit)?,
        )
        val contactRows = buildList {
            if (email != null) {
                add(ContactRowSpec(emailLabel, email, LiquidIcons.Feedback, colorScheme.primary) {
                    runCatching { uriHandler.openUri("mailto:$email") }
                })
            }
            if (phone != null) {
                add(ContactRowSpec(phoneLabel, phone, LiquidIcons.Phone, glassColors.success) {
                    runCatching { uriHandler.openUri("tel:$phone") }
                })
            }
            if (office != null) {
                add(ContactRowSpec(officeLabel, office, LiquidIcons.Home, colorScheme.tertiary, null))
            }
        }
        if (contactRows.isNotEmpty()) {
            UniInsetGroup(entranceOrder = 1) {
                contactRows.forEachIndexed { index, row ->
                    UniInsetRow(
                        title = row.title,
                        subtitle = row.subtitle,
                        icon = row.icon,
                        iconTint = row.tint,
                        onClick = row.action,
                        showDivider = index < contactRows.lastIndex,
                        titleMaxLines = 1,
                        subtitleMaxLines = 1,
                        minHeight = 64.dp,
                    )
                }
            }
        }

        // Ricevimento Studenti Card with secondary accent
        UniAnimatedEntrance(order = 2) {
            LiquidCard(
                shape = RoundedRectangle(22.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Calendar,
                            contentDescription = null,
                            tint = colorScheme.secondary,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 34.dp,
                                iconSize = 17.dp,
                                containerColor = colorScheme.secondary.copy(alpha = 0.14f),
                                shape = RoundedRectangle(10.dp),
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.ui_course_office_hours),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                    }
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.20f))
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
}

/**
 * Tab 2: Dati Accademici, Tipologia Crediti, Settore Scientifico e Vincoli Reali (stile AppInfo).
 */
@Composable
fun CourseDataTab(course: StudyCourse) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val uriHandler = LocalUriHandler.current
    val isCompleted = course.status == CourseStatus.COMPLETED
    val statusColor = if (isCompleted) glassColors.success else colorScheme.primary
    val safeCourseUrl = course.courseUrl?.trim()?.takeIf {
        it.startsWith("https://", ignoreCase = true) && it.length > 8
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_academic_data_title),
            subtitle = stringResource(Res.string.ui_course_academic_data_sub),
        )

        // Stato Carriera & Esito
        UniAnimatedEntrance(order = 0) {
            LiquidCard(
                shape = RoundedRectangle(22.dp),
                contentPadding = PaddingValues(18.dp),
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
                                containerSize = 42.dp,
                                iconSize = 20.dp,
                                containerColor = statusColor.copy(alpha = 0.14f),
                                shape = RoundedRectangle(13.dp),
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
                            containerColor = glassColors.success.copy(alpha = 0.16f),
                            contentColor = glassColors.success,
                        )
                    }
                }
            }
        }

        // Multi-accent Data Rows with tinted badges (AppInfoScreen visual grammar)
        data class AcademicDataRowSpec(
            val label: String,
            val subtitle: String,
            val value: String,
            val icon: ImageVector,
            val accent: Color,
        )
        val dataRows = listOf(
            AcademicDataRowSpec(
                label = stringResource(Res.string.ui_course_scientific_sector),
                subtitle = stringResource(Res.string.ui_course_ssd_subtitle),
                value = course.ssd.ifBlank { "—" },
                icon = LiquidIcons.Assignment,
                accent = colorScheme.primary,
            ),
            AcademicDataRowSpec(
                label = stringResource(Res.string.ui_course_activity_type),
                subtitle = stringResource(Res.string.ui_course_taf_subtitle),
                value = course.taf.ifBlank { stringResource(Res.string.ui_course_activity_characterizing) },
                icon = LiquidIcons.Info,
                accent = colorScheme.tertiary,
            ),
            AcademicDataRowSpec(
                label = stringResource(Res.string.ui_course_credits),
                subtitle = stringResource(Res.string.ui_course_credits_subtitle),
                value = course.cfu.ifBlank { "—" },
                icon = LiquidIcons.Star,
                accent = glassColors.warning,
            ),
            AcademicDataRowSpec(
                label = stringResource(Res.string.ui_course_year_label),
                subtitle = stringResource(Res.string.ui_course_plan_placement),
                value = when {
                    course.year > 0 -> stringResource(Res.string.ui_year_nth, course.year)
                    course.semester.isNotBlank() -> course.semester
                    else -> "—"
                },
                icon = LiquidIcons.Calendar,
                accent = colorScheme.secondary,
            ),
            AcademicDataRowSpec(
                label = stringResource(Res.string.ui_course_language),
                subtitle = stringResource(Res.string.ui_course_lang_material_note),
                value = course.lingua.ifBlank { stringResource(Res.string.ui_course_lang_italian) },
                icon = LiquidIcons.MenuBook,
                accent = colorScheme.primary,
            ),
            AcademicDataRowSpec(
                label = stringResource(Res.string.ui_course_attendance),
                subtitle = stringResource(Res.string.ui_course_attendance_no_obligation),
                value = stringResource(Res.string.ui_course_attendance_recommended),
                icon = LiquidIcons.Check,
                accent = glassColors.success,
            ),
        )

        UniInsetGroup(entranceOrder = 1) {
            dataRows.forEachIndexed { index, row ->
                UniInsetRow(
                    title = row.label,
                    subtitle = row.subtitle,
                    icon = row.icon,
                    iconTint = row.accent,
                    showDivider = index < dataRows.lastIndex,
                    titleMaxLines = 1,
                    subtitleMaxLines = 1,
                    minHeight = 64.dp,
                    trailingContent = {
                        LiquidBadge(
                            text = row.value,
                            containerColor = row.accent.copy(alpha = 0.14f),
                            contentColor = row.accent,
                        )
                    },
                )
            }
        }

        if (safeCourseUrl != null) {
            CourseCataloguePortalCard(
                accentColor = colorScheme.tertiary,
                onClick = { runCatching { uriHandler.openUri(safeCourseUrl) } },
            )
        }

        // Propedeuticità Card with Warning Accent
        UniAnimatedEntrance(order = 2) {
            val prereqAccent = glassColors.warning
            LiquidCard(
                shape = RoundedRectangle(22.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Lock,
                            contentDescription = null,
                            tint = prereqAccent,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 34.dp,
                                iconSize = 17.dp,
                                containerColor = prereqAccent.copy(alpha = 0.14f),
                                shape = RoundedRectangle(10.dp),
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.ui_course_prerequisites_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                    }
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.20f))
                    if (course.prerequisiti.isNotBlank()) {
                        val formattedPrereq = remember(course.prerequisiti) {
                            CourseSyllabusFormatter.formatSectionMarkdown(course.prerequisiti)
                        }
                        RichMarkdownDocument(
                            markdown = formattedPrereq,
                            accentColor = prereqAccent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.ui_course_no_prerequisites),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCataloguePortalCard(
    accentColor: Color,
    onClick: () -> Unit,
) {
    UniInsetGroup {
        UniInsetRow(
            title = stringResource(Res.string.ui_course_open_catalogue_title),
            subtitle = stringResource(Res.string.ui_course_open_catalogue_sub),
            icon = LiquidIcons.Share,
            iconTint = accentColor,
            onClick = onClick,
            showDivider = false,
            titleMaxLines = 1,
            subtitleMaxLines = 1,
            minHeight = 64.dp,
            trailingContent = {
                LiquidBadge(
                    text = stringResource(Res.string.ui_course_open_catalogue_badge),
                    containerColor = accentColor.copy(alpha = 0.14f),
                    contentColor = accentColor,
                )
            },
        )
    }
}

/**
 * Editorial Syllabus Section Card with semantic Monet accent coloring (matching AppInfo & Document screens).
 * Propagates [accentColor] into the icon container, numbered eyebrow, divider tint, and [RichMarkdownDocument].
 */
@Composable
private fun SyllabusSectionCard(
    index: Int,
    title: String,
    content: String,
    icon: ImageVector,
    accentColor: Color,
    preserveLineBreaksAsList: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val formattedMarkdown = remember(content, preserveLineBreaksAsList) {
        CourseSyllabusFormatter.formatSectionMarkdown(
            raw = content,
            preserveLineBreaksAsList = preserveLineBreaksAsList,
        )
    }
    val indexBadge = index.toString().padStart(2, '0')

    LiquidCard(
        shape = RoundedRectangle(22.dp),
        contentPadding = PaddingValues(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Vertical semantic accent pill + tinted icon container
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(30.dp)
                        .clip(Capsule())
                        .background(accentColor),
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 36.dp,
                        iconSize = 18.dp,
                        containerColor = accentColor.copy(alpha = 0.14f),
                        shape = RoundedRectangle(11.dp),
                    ),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = indexBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 0.8.sp,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        fontSize = 16.5.sp,
                        lineHeight = 22.sp,
                    )
                }
            }

            LiquidHorizontalDivider(color = accentColor.copy(alpha = 0.16f))

            RichMarkdownDocument(
                markdown = formattedMarkdown.ifBlank { content.trim() },
                accentColor = accentColor,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
