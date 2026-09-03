package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.StudyCourse
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Tab 0: Programma, Obiettivi e Syllabus dettagliato del corso.
 */
@Composable
fun CourseProgramTab(course: StudyCourse, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_syllabus_title),
            subtitle = stringResource(Res.string.ui_course_syllabus_sub),
        )

        // Card Obiettivi e Descrizione
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(20.dp),
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
                        imageVector = LiquidIcons.Edit,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_course_objectives_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }

                Text(
                    text = course.description.ifBlank {
                        "Il corso approfondisce i principi cardine, le architetture di riferimento e le metodologie applicative standard richieste dal piano di studio."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 22.sp,
                )
            }
        }

        // Card Modalità d'Esame
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(20.dp),
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
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_course_exam_modes_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(Res.string.ui_course_exam_mode_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 22.sp,
                )
            }
        }

        // Card Materiale e Bibliografia
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(20.dp),
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
                        imageVector = LiquidIcons.Star,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_course_materials_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(Res.string.ui_course_materials_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

/**
 * Tab 1: Docente, Contatti e Ricevimento studenti.
 */
@Composable
fun CourseProfessorTab(course: StudyCourse, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    val profName = course.professor.ifBlank { stringResource(Res.string.ui_professor) }
    val initials = profName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

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
            backdropState = backdropState,
            shape = RoundedCornerShape(22.dp),
            contentPadding = 16.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LiquidAvatar(
                    initials = if (initials.isNotBlank()) initials else "DC",
                    size = 52.dp,
                    backdropState = backdropState,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_university),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Dettagli Ricevimento (2 a 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_course_office_hours),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Icon(
                            imageVector = LiquidIcons.Calendar,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        text = stringResource(Res.string.ui_course_by_appointment),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_course_in_person_or_online),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_course_office_location),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Icon(
                            imageVector = LiquidIcons.Home,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        text = stringResource(Res.string.ui_course_dept_office),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_course_dept_building),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

/**
 * Tab 2: Dati Accademici, Tipologia Crediti e Vincoli del Corso.
 */
@Composable
fun CourseDataTab(course: StudyCourse, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_course_academic_data_title),
            subtitle = stringResource(Res.string.ui_course_academic_data_sub),
        )

        // Academic details in 2 a 2 tiles
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_activity_type),
                    value = stringResource(Res.string.ui_course_activity_characterizing),
                    subvalue = stringResource(Res.string.ui_course_activity_base),
                    backdropState = backdropState,
                    modifier = Modifier.weight(1f),
                )
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_attendance),
                    value = stringResource(Res.string.ui_course_attendance_recommended),
                    subvalue = stringResource(Res.string.ui_course_attendance_no_obligation),
                    backdropState = backdropState,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_scientific_sector),
                    value = "ING-INF / INF",
                    subvalue = stringResource(Res.string.ui_course_sector_label),
                    backdropState = backdropState,
                    modifier = Modifier.weight(1f),
                )
                AcademicInfoTile(
                    label = stringResource(Res.string.ui_course_language),
                    value = stringResource(Res.string.ui_course_lang_italian),
                    subvalue = stringResource(Res.string.ui_course_lang_material_note),
                    backdropState = backdropState,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Propedeuticità Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(20.dp),
            contentPadding = 16.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_course_prerequisites_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = stringResource(Res.string.ui_course_no_prerequisites),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

/**
 * Reusable Hero Stat Tile for the Top Course Header Card.
 */
@Composable
fun CourseHeroStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
            )
            Text(
                text = subvalue,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            )
        }
    }
}
