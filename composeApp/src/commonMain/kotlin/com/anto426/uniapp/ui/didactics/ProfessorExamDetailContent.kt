package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.AcademicContentItem
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun ProfessorExamDetailContent(
    item: AcademicContentItem,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val bookingsCount = item.bookings.size.takeIf { it > 0 }
        ?: item.detail?.let { d ->
            val match = Regex("""(\d+)\s*(?:prenotat|iscritt)""", RegexOption.IGNORE_CASE).find(d)
            match?.groupValues?.get(1)?.toIntOrNull()
        } ?: 0

    val tabs = listOf(
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_professor_registered_students),
            badge = if (bookingsCount > 0) bookingsCount.toString() else null,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_professor_commission),
            badge = if (item.commission.isNotEmpty()) item.commission.size.toString() else null,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_professor_exam_round_tab),
        ),
    )

    UniScreenColumn {
        UniAnimatedEntrance(order = 0) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Calendar,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 36.dp,
                                iconSize = 18.dp,
                                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                shape = RoundedRectangle(11.dp),
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.ui_professor_exam_round_tag),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    item.code?.takeIf(String::isNotBlank)?.let { code ->
                        LiquidBadge(
                            text = code,
                            containerColor = colorScheme.onSurface.copy(alpha = 0.06f),
                            contentColor = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.subtitle?.takeIf(String::isNotBlank)?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_professor_registered_students),
                        value = "$bookingsCount",
                        valueColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )

                    item.date?.takeIf(String::isNotBlank)?.let { date ->
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_date_time_label),
                            value = date,
                            valueColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (item.commission.isNotEmpty()) {
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_professor_commission),
                            value = stringResource(Res.string.ui_professor_members_count, item.commission.size),
                            valueColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        }

        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedTab.coerceIn(0, tabs.lastIndex),
            onTabSelected = onTabSelected,
        )

        UniTabContentHost(
            selectedTab = selectedTab,
            tabCount = tabs.size,
            onTabSelected = onTabSelected,
            label = "ProfessorExamTabs",
        ) { tab ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (tab) {
                    0 -> ProfessorBookingsTabContent(item = item)
                    1 -> ProfessorCommissionTabContent(item = item)
                    else -> ProfessorSessionInfoTabContent(item = item)
                }
            }
        }
    }
}

@Composable
private fun ProfessorBookingsTabContent(
    item: AcademicContentItem,
) {
    val colorScheme = MaterialTheme.colorScheme
    if (item.bookings.isEmpty()) {
        UniInsetGroup {
            UniInsetRow(
                title = stringResource(Res.string.ui_professor_no_bookings),
                subtitle = stringResource(Res.string.ui_professor_no_bookings_description),
                icon = LiquidIcons.AccountCircle,
                showDivider = false,
            )
        }
    } else {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_professor_bookings),
            subtitle = stringResource(Res.string.ui_professor_registered_count, item.bookings.size),
        )
        UniInsetGroup {
            item.bookings.forEachIndexed { index, booking ->
                UniInsetRow(
                    title = booking.studentName,
                    subtitle = booking.matricola?.takeIf(String::isNotBlank)?.let {
                        stringResource(Res.string.ui_professor_student_matricola_format, it)
                    } ?: stringResource(Res.string.ui_professor_student_registered_fallback),
                    icon = LiquidIcons.AccountCircle,
                    showDivider = index < item.bookings.lastIndex,
                    trailingContent = booking.grade?.takeIf(String::isNotBlank)?.let { grade ->
                        {
                            LiquidBadge(
                                text = grade,
                                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                contentColor = colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ProfessorCommissionTabContent(
    item: AcademicContentItem,
) {
    if (item.commission.isEmpty()) {
        UniInsetGroup {
            UniInsetRow(
                title = stringResource(Res.string.ui_professor_no_commission),
                subtitle = stringResource(Res.string.ui_professor_no_commission_description),
                icon = LiquidIcons.AccountCircle,
                showDivider = false,
            )
        }
    } else {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_professor_commission),
            subtitle = stringResource(Res.string.ui_professor_members_count, item.commission.size),
        )
        UniInsetGroup {
            item.commission.forEachIndexed { index, member ->
                UniInsetRow(
                    title = member.displayName,
                    subtitle = member.role?.takeIf(String::isNotBlank)
                        ?: stringResource(Res.string.ui_professor_examiner_role_fallback),
                    icon = LiquidIcons.AccountCircle,
                    showDivider = index < item.commission.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun ProfessorSessionInfoTabContent(
    item: AcademicContentItem,
) {
    val rawFields =
        item.fields
            .map { it.label to it.value }
            .filterNot { (label, _) ->
                label.equals("Commissione", ignoreCase = true) ||
                    label.equals("Prenotazione", ignoreCase = true)
            }

    val teachingCodeLabel = stringResource(Res.string.ui_professor_teaching_code)
    val courseDepartmentLabel = stringResource(Res.string.ui_professor_course_department)
    val extraDetailsLabel = stringResource(Res.string.ui_professor_extra_details)
    val dateTimeLabel = stringResource(Res.string.ui_date_time_label)

    val fields = rawFields.ifEmpty {
        listOfNotNull(
            item.date?.takeIf(String::isNotBlank)?.let { dateTimeLabel to it },
            item.code?.takeIf(String::isNotBlank)?.let { teachingCodeLabel to it },
            item.subtitle?.takeIf(String::isNotBlank)?.let { courseDepartmentLabel to it },
            item.detail?.takeIf(String::isNotBlank)?.let { extraDetailsLabel to it },
        )
    }

    if (fields.isEmpty()) {
        UniInsetGroup {
            UniInsetRow(
                title = stringResource(Res.string.ui_details_none_title),
                subtitle = stringResource(Res.string.ui_professor_exam_details_none_desc),
                icon = LiquidIcons.Info,
                showDivider = false,
            )
        }
    } else {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_professor_exam_round_tab),
        )
        UniInsetGroup {
            fields.forEachIndexed { index, (label, value) ->
                UniInsetRow(
                    title = label,
                    subtitle = value,
                    icon = LiquidIcons.Info,
                    showDivider = index < fields.lastIndex,
                )
            }
        }
    }
}
