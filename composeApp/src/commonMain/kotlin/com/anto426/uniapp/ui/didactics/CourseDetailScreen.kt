package com.anto426.uniapp.ui.didactics

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.CourseAcademicInfoSection
import com.anto426.uniapp.ui.didactics.components.CourseFacultySection
import com.anto426.uniapp.ui.didactics.components.CourseSyllabusSection
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun CourseDetailScreen(
    course: StudyCourse,
    professorContact: ContactData? = null,
    onContactClick: ((ContactData) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val isCompleted = course.status == CourseStatus.COMPLETED
    val statusColor = when (course.status) {
        CourseStatus.COMPLETED -> glassColors.success
        CourseStatus.ACTIVE -> colorScheme.primary
        CourseStatus.PLANNED -> colorScheme.onSurfaceVariant
    }
    val statusLabel = stringResource(
        when (course.status) {
            CourseStatus.COMPLETED -> Res.string.ui_status_completed
            CourseStatus.ACTIVE -> Res.string.ui_status_in_progress
            CourseStatus.PLANNED -> Res.string.ui_status_planned
        },
    )

    UniScreenColumn {
        // 1. Hero Card del Corso (stile nativo delle detail screen di UniApp)
        UniAnimatedEntrance(order = 0) {
            LiquidCard(
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
                            text = statusLabel,
                            containerColor = statusColor.copy(alpha = 0.14f),
                            contentColor = statusColor,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (!course.grade.isNullOrBlank()) {
                                LiquidBadge(
                                    text = course.grade,
                                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                    contentColor = colorScheme.primary,
                                )
                            }
                            if (course.cfu.isNotBlank()) {
                                LiquidBadge(
                                    text = course.cfu,
                                    containerColor = colorScheme.secondary.copy(alpha = 0.14f),
                                    contentColor = colorScheme.secondary,
                                )
                            }
                        }
                    }

                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.3).sp,
                    )

                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_course_credits),
                            value = course.cfu.ifBlank { "—" },
                            valueColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_course_year_label),
                            value = when {
                                course.year > 0 -> stringResource(Res.string.ui_year_nth, course.year)
                                course.semester.isNotBlank() -> course.semester
                                else -> "—"
                            },
                            valueColor = colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_course_code_header),
                            value = course.ssd.ifBlank { course.id.ifBlank { "—" } },
                            valueColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (isCompleted && !course.examDate.isNullOrBlank()) {
                        LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Check,
                                contentDescription = null,
                                tint = glassColors.success,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = if (!course.grade.isNullOrBlank()) {
                                    stringResource(Res.string.ui_course_verbalized_on_grade, course.examDate, course.grade)
                                } else {
                                    stringResource(Res.string.ui_course_verbalized_on, course.examDate)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = glassColors.success,
                            )
                        }
                    }
                }
            }
        }

        // 2. Docente & Contatti integrati
        CourseFacultySection(
            course = course,
            professorContact = professorContact,
            onContactClick = onContactClick,
        )

        // 3. Dati essenziali del corso & Link ufficiale Course Catalogue
        CourseAcademicInfoSection(course = course)

        // 4. Programma & Syllabus editoriale (stile Info / Documenti con colori semantici dell'app)
        CourseSyllabusSection(course = course)
    }
}
