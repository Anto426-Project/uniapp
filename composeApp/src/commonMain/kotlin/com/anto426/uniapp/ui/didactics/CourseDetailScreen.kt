package com.anto426.uniapp.ui.didactics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.CourseDataTab
import com.anto426.uniapp.ui.didactics.components.CourseHeroStatTile
import com.anto426.uniapp.ui.didactics.components.CourseProfessorTab
import com.anto426.uniapp.ui.didactics.components.CourseProgramTab
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun CourseDetailScreen(course: StudyCourse, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_program), icon = LiquidIcons.Edit),
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_professor), icon = LiquidIcons.AccountCircle),
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_data), icon = LiquidIcons.Info),
    )

    val isCompleted = course.status == CourseStatus.COMPLETED
    val statusLabel = when (course.status) {
        CourseStatus.COMPLETED -> stringResource(Res.string.ui_exam_status_verbalized)
        CourseStatus.ACTIVE -> stringResource(Res.string.ui_student_status)
        CourseStatus.PLANNED -> stringResource(Res.string.ui_bookable_exams)
    }

    UniScreenColumn {
        // ==========================================
        // 1. HERO COURSE CARD (VETRO LIQUID MONET)
        // ==========================================
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 18.dp,
            interactiveGelatin = false,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Status Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCompleted) colorScheme.primary
                                    else colorScheme.outlineVariant
                                ),
                        )
                        Text(
                            text = statusLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) colorScheme.primary else colorScheme.onSurfaceVariant,
                        )
                    }

                    LiquidBadge(
                        text = course.cfu,
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )
                }

                // Course Name & Professor Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = course.professor.ifBlank { stringResource(Res.string.ui_professor) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Hero Stat Grid (2 a 2)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CourseHeroStatTile(
                            label = stringResource(Res.string.ui_course_period),
                            value = course.semester.ifBlank { "—" },
                            icon = LiquidIcons.Calendar,
                            backdropState = backdropState,
                            modifier = Modifier.weight(1f),
                        )
                        CourseHeroStatTile(
                            label = stringResource(Res.string.ui_course_credits),
                            value = course.cfu,
                            icon = LiquidIcons.Star,
                            backdropState = backdropState,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CourseHeroStatTile(
                            label = stringResource(Res.string.ui_course_status_header),
                            value = statusLabel,
                            icon = if (isCompleted) LiquidIcons.Check else LiquidIcons.Time,
                            backdropState = backdropState,
                            modifier = Modifier.weight(1f),
                        )
                        CourseHeroStatTile(
                            label = stringResource(Res.string.ui_course_code_header),
                            value = if (course.id.isNotBlank()) course.id else "—",
                            icon = LiquidIcons.Lock,
                            backdropState = backdropState,
                            modifier = Modifier.weight(1f),
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
            backdropState = backdropState,
        )

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "courseDetailTabTransition",
        ) { tabIndex ->
            when (tabIndex) {
                0 -> CourseProgramTab(course = course, backdropState = backdropState)
                1 -> CourseProfessorTab(course = course, backdropState = backdropState)
                else -> CourseDataTab(course = course, backdropState = backdropState)
            }
        }
    }
}
