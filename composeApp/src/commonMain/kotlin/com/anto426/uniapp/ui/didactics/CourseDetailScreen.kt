package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.ContactData
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyCourse
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.didactics.components.CourseDataTab
import com.anto426.uniapp.ui.didactics.components.CourseProfessorTab
import com.anto426.uniapp.ui.didactics.components.CourseProgramTab
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseDetailScreen(
    course: StudyCourse,
    professorContact: ContactData? = null,
    onContactClick: ((ContactData) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val uriHandler = LocalUriHandler.current
    var selectedTab by rememberSaveable(course.id.ifBlank { course.name }) { mutableIntStateOf(0) }

    val tabs = listOf(
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_program)),
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_professor)),
        LiquidNavigationItem(stringResource(Res.string.ui_course_tab_data)),
    )

    val isCompleted = course.status == CourseStatus.COMPLETED
    val statusColor = when (course.status) {
        CourseStatus.COMPLETED -> glassColors.success
        CourseStatus.ACTIVE -> colorScheme.primary
        CourseStatus.PLANNED -> colorScheme.tertiary
    }
    val statusLabel = stringResource(
        when (course.status) {
            CourseStatus.COMPLETED -> Res.string.ui_status_completed
            CourseStatus.ACTIVE -> Res.string.ui_status_in_progress
            CourseStatus.PLANNED -> Res.string.ui_status_planned
        },
    )
    val safeCourseUrl = course.courseUrl?.trim()?.takeIf {
        it.startsWith("https://", ignoreCase = true) && it.length > 8
    }

    UniScreenColumn {
        // 1. Editorial Hero Card with subtle status-tinted Monet halo & multi-accent KPI deck
        UniAnimatedEntrance(order = 0) {
            LiquidCard(
                shape = RoundedRectangle(26.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Subtle optical corner glow harmonizing with course status
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.14f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.92f, size.height * 0.12f),
                                radius = size.maxDimension * 0.58f,
                            ),
                            radius = size.maxDimension * 0.58f,
                            center = Offset(size.width * 0.92f, size.height * 0.12f),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Top status & metric badge strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LiquidBadge(
                                text = statusLabel,
                                containerColor = statusColor.copy(alpha = 0.15f),
                                contentColor = statusColor,
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (!course.grade.isNullOrBlank()) {
                                    LiquidBadge(
                                        text = course.grade,
                                        containerColor = (if (isCompleted) glassColors.success else colorScheme.primary).copy(alpha = 0.16f),
                                        contentColor = if (isCompleted) glassColors.success else colorScheme.primary,
                                    )
                                }
                                if (course.cfu.isNotBlank()) {
                                    LiquidBadge(
                                        text = course.cfu,
                                        containerColor = colorScheme.tertiary.copy(alpha = 0.15f),
                                        contentColor = colorScheme.tertiary,
                                    )
                                }
                            }
                        }

                        // Course Title + Metadata Chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                letterSpacing = (-0.4).sp,
                                lineHeight = 30.sp,
                            )

                            if (course.professor.isNotBlank() || course.ssd.isNotBlank() || safeCourseUrl != null) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (course.professor.isNotBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .clip(Capsule())
                                                .background(colorScheme.primary.copy(alpha = 0.10f))
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        ) {
                                            Icon(
                                                imageVector = LiquidIcons.AccountCircle,
                                                contentDescription = null,
                                                tint = colorScheme.primary,
                                                modifier = Modifier.size(14.dp),
                                            )
                                            Text(
                                                text = course.professor,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = colorScheme.onSurface.copy(alpha = 0.88f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }

                                    if (course.ssd.isNotBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .clip(Capsule())
                                                .background(colorScheme.secondary.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        ) {
                                            Text(
                                                text = course.ssd,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorScheme.secondary,
                                                maxLines = 1,
                                            )
                                        }
                                    }

                                    if (safeCourseUrl != null) {
                                        Row(
                                            modifier = Modifier
                                                .clip(Capsule())
                                                .background(colorScheme.tertiary.copy(alpha = 0.14f))
                                                .clickable { runCatching { uriHandler.openUri(safeCourseUrl) } }
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        ) {
                                            Icon(
                                                imageVector = LiquidIcons.Share,
                                                contentDescription = null,
                                                tint = colorScheme.tertiary,
                                                modifier = Modifier.size(13.dp),
                                            )
                                            Text(
                                                text = stringResource(Res.string.ui_course_open_catalogue_badge),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorScheme.tertiary,
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Frosted 3-Metric KPI Deck (multi-accent)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedRectangle(16.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.28f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            GradesMetricTile(
                                label = stringResource(Res.string.ui_course_credits),
                                value = course.cfu.ifBlank { "—" },
                                valueColor = colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            GradesMetricTile(
                                label = stringResource(Res.string.ui_course_year_label),
                                value = when {
                                    course.year > 0 -> stringResource(Res.string.ui_year_nth, course.year)
                                    course.semester.isNotBlank() -> course.semester
                                    else -> "—"
                                },
                                valueColor = colorScheme.tertiary,
                                modifier = Modifier.weight(1f),
                            )
                            GradesMetricTile(
                                label = stringResource(Res.string.ui_course_code_header),
                                value = course.ssd.ifBlank { course.id.ifBlank { "—" } },
                                valueColor = colorScheme.secondary,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        // Verbalization success ribbon
                        if (isCompleted && !course.examDate.isNullOrBlank()) {
                            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.22f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedRectangle(14.dp))
                                    .background(glassColors.success.copy(alpha = 0.11f))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = LiquidIcons.Check,
                                    contentDescription = null,
                                    tint = glassColors.success,
                                    modifier = Modifier.liquidIconContainer(
                                        containerSize = 26.dp,
                                        iconSize = 14.dp,
                                        containerColor = glassColors.success.copy(alpha = 0.18f),
                                        shape = Capsule(),
                                    ),
                                )
                                Text(
                                    text = if (!course.grade.isNullOrBlank()) {
                                        stringResource(Res.string.ui_course_verbalized_on_grade, course.examDate, course.grade)
                                    } else {
                                        stringResource(Res.string.ui_course_verbalized_on, course.examDate)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = glassColors.success,
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Tab Bar
        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        // 3. Tab Content
        UniTabContentHost(
            selectedTab = selectedTab,
            tabCount = tabs.size,
            onTabSelected = { selectedTab = it },
            label = "CourseDetailTabs",
        ) { tab ->
            when (tab) {
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
