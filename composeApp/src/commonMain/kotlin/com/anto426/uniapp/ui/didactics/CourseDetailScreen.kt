package com.anto426.uniapp.ui.didactics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.CourseDataTab
import com.anto426.uniapp.ui.didactics.components.CourseHeroStatTile
import com.anto426.uniapp.ui.didactics.components.CourseProfessorTab
import com.anto426.uniapp.ui.didactics.components.CourseProgramTab
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun CourseDetailScreen(
    course: StudyCourse,
    professorContact: ContactData? = null,
    onContactClick: ((ContactData) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_program), icon = LiquidIcons.Edit),
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_professor), icon = LiquidIcons.AccountCircle),
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_data), icon = LiquidIcons.Info),
    )

    val isCompleted = course.status == CourseStatus.COMPLETED
    val statusLabel = when (course.status) {
        CourseStatus.COMPLETED -> "Superato"
        CourseStatus.ACTIVE -> "In corso"
        CourseStatus.PLANNED -> "Pianificato"
    }

    UniScreenColumn {
        // ==========================================
        // 1. HERO COURSE CARD (VETRO LIQUID MONET)
        // ==========================================
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Status & Badges Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedRectangle(12.dp))
                            .background(
                                if (isCompleted) colorScheme.primary.copy(alpha = 0.12f)
                                else colorScheme.primary.copy(alpha = 0.08f)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isCompleted) colorScheme.primary.copy(alpha = 0.25f)
                                    else colorScheme.outlineVariant.copy(alpha = 0.25f)
                                ),
                                RoundedRectangle(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Icon(
                            imageVector = if (isCompleted) LiquidIcons.Check else LiquidIcons.Calendar,
                            contentDescription = null,
                            tint = if (isCompleted) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = statusLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) colorScheme.primary else colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp,
                        )
                    }

                    // Right Badges (Grade + CFU)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (!course.grade.isNullOrBlank()) {
                            LiquidBadge(
                                text = course.grade,
                                containerColor = colorScheme.primaryContainer,
                                contentColor = colorScheme.primary,
                            )
                        }

                        if (course.cfu.isNotBlank()) {
                            LiquidBadge(
                                text = course.cfu,
                                containerColor = colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                contentColor = colorScheme.secondary,
                            )
                        }
                    }
                }

                // Course Name & Professor Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.3).sp,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (course.professor.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = LiquidIcons.AccountCircle,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = course.professor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (course.ssd.isNotBlank()) {
                            Text(
                                text = "•",
                                color = colorScheme.outlineVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = course.ssd,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary,
                            )
                        }
                    }
                }

                LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Hero Stat Grid (2 a 2)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CourseHeroStatTile(
                            label = stringResource(Res.string.ui_course_credits),
                            value = course.cfu.ifBlank { "—" },
                            icon = LiquidIcons.Star,
                            modifier = Modifier.weight(1f),
                        )
                        CourseHeroStatTile(
                            label = "Anno di Corso",
                            value = if (course.year > 0) "${course.year}° Anno"
                            else if (course.semester.isNotBlank()) course.semester else "—",
                            icon = LiquidIcons.Calendar,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CourseHeroStatTile(
                            label = stringResource(Res.string.ui_course_code_header),
                            value = course.ssd.ifBlank { if (course.id.isNotBlank()) course.id else "—" },
                            icon = LiquidIcons.Assignment,
                            modifier = Modifier.weight(1f),
                        )
                        CourseHeroStatTile(
                            label = stringResource(Res.string.ui_course_activity_type),
                            value = course.taf.ifBlank { "Caratterizzante" },
                            icon = LiquidIcons.Info,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Verbalization banner (if completed with date)
                if (isCompleted && !course.examDate.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedRectangle(14.dp))
                            .background(colorScheme.primary.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.2f)), RoundedRectangle(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Check,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = buildString {
                                append("Verbalizzato il ${course.examDate}")
                                if (!course.grade.isNullOrBlank()) append(" con esito ${course.grade}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.primary,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ==========================================
        // 2. TAB SELECTOR & SUB-PAGE SWITCHER
        // ==========================================
        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "courseDetailTabTransition",
        ) { tabIndex ->
            when (tabIndex) {
                0 -> CourseProgramTab(course = course)
                1 -> CourseProfessorTab(
                    course = course,
                    professorContact = professorContact,
                    onContactClick = onContactClick,
                )
                else -> CourseDataTab(course = course)
            }
        }
    }
}
