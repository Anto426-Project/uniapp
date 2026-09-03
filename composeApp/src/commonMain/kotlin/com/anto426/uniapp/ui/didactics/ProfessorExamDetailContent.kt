package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.backend.model.ProfessorContentItem
import com.anto426.unisdk.backend.model.ProfessorExamBooking
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun ProfessorExamDetailContent(
    backdropState: Backdrop,
    item: ProfessorContentItem,
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
            icon = LiquidIcons.AccountCircle,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_professor_commission),
            badge = if (item.commission.isNotEmpty()) item.commission.size.toString() else null,
            icon = LiquidIcons.AccountCircle,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_professor_exam_round_tab),
            icon = LiquidIcons.Info,
        ),
    )

    UniScreenColumn {
        // ==========================================
        // 1. HERO HEADER CARD
        // ==========================================
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 20.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Top Tag Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidBadge(
                        text = stringResource(Res.string.ui_professor_exam_round_tag),
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = colorScheme.primary,
                        backdropState = backdropState,
                    )

                    item.code?.takeIf(String::isNotBlank)?.let { code ->
                        LiquidBadge(
                            text = code,
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = colorScheme.primary,
                            backdropState = backdropState,
                        )
                    }
                }

                // Title & Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                    )
                    item.subtitle?.takeIf(String::isNotBlank)?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                // Hero Quick Stat Tiles Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfessorHeroStatTile(
                        label = stringResource(Res.string.ui_professor_registered_students),
                        value = if (bookingsCount > 0) "$bookingsCount" else "0",
                        icon = LiquidIcons.AccountCircle,
                    )

                    item.date?.takeIf(String::isNotBlank)?.let { date ->
                        ProfessorHeroStatTile(
                            label = stringResource(Res.string.ui_date_time_label),
                            value = date,
                            icon = LiquidIcons.Calendar,
                        )
                    }

                    if (item.commission.isNotEmpty()) {
                        ProfessorHeroStatTile(
                            label = stringResource(Res.string.ui_professor_commission),
                            value = stringResource(Res.string.ui_professor_members_count, item.commission.size),
                            icon = LiquidIcons.AccountCircle,
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. TAB BAR
        // ==========================================
        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedTab.coerceIn(0, tabs.lastIndex),
            onTabSelected = onTabSelected,
            backdropState = backdropState,
        )

        // ==========================================
        // 3. TAB CONTENT
        // ==========================================
        when (selectedTab.coerceIn(0, tabs.lastIndex)) {
            0 -> ProfessorBookingsTabContent(backdropState = backdropState, item = item)
            1 -> ProfessorCommissionTabContent(backdropState = backdropState, item = item)
            else -> ProfessorSessionInfoTabContent(backdropState = backdropState, item = item)
        }
    }
}

@Composable
private fun ProfessorHeroStatTile(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ProfessorBookingsTabContent(
    backdropState: Backdrop,
    item: ProfessorContentItem,
) {
    if (item.bookings.isEmpty()) {
        LiquidEmptyState(
            title = stringResource(Res.string.ui_professor_no_bookings),
            description = stringResource(Res.string.ui_professor_no_bookings_description),
            icon = LiquidIcons.AccountCircle,
            backdropState = backdropState,
        )
    } else {
        val colorScheme = MaterialTheme.colorScheme
        LiquidPreferenceGroup(
            title = "${stringResource(Res.string.ui_professor_bookings)} (${item.bookings.size})",
            backdropState = backdropState,
        ) {
            item.bookings.forEach { booking ->
                LiquidPreferenceItem(
                    title = booking.studentName,
                    subtitle = booking.matricola?.takeIf(String::isNotBlank)?.let {
                        stringResource(Res.string.ui_professor_student_matricola_format, it)
                    } ?: stringResource(Res.string.ui_professor_student_registered_fallback),
                    icon = LiquidIcons.AccountCircle,
                    backdropState = backdropState,
                    trailingContent = {
                        booking.grade?.takeIf(String::isNotBlank)?.let { grade ->
                            LiquidBadge(
                                text = grade,
                                containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = colorScheme.primary,
                                backdropState = backdropState,
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
    backdropState: Backdrop,
    item: ProfessorContentItem,
) {
    if (item.commission.isEmpty()) {
        LiquidEmptyState(
            title = stringResource(Res.string.ui_professor_no_commission),
            description = stringResource(Res.string.ui_professor_no_commission_description),
            icon = LiquidIcons.AccountCircle,
            backdropState = backdropState,
        )
    } else {
        LiquidPreferenceGroup(
            title = "${stringResource(Res.string.ui_professor_commission)} (${item.commission.size})",
            backdropState = backdropState,
        ) {
            item.commission.forEach { member ->
                LiquidPreferenceItem(
                    title = member.displayName,
                    subtitle = member.role?.takeIf(String::isNotBlank) ?: stringResource(Res.string.ui_professor_examiner_role_fallback),
                    icon = LiquidIcons.AccountCircle,
                    backdropState = backdropState,
                )
            }
        }
    }
}

@Composable
private fun ProfessorSessionInfoTabContent(
    backdropState: Backdrop,
    item: ProfessorContentItem,
) {
    val rawFields =
        item.fields
            .map { it.label to it.value }
            .ifEmpty { item.detail.toAcademicDetailFields() }
            .filterNot { (label, _) ->
                label.equals("Commissione", ignoreCase = true) ||
                    label.equals("Prenotazione", ignoreCase = true)
            }

    val fields = rawFields.ifEmpty {
        listOfNotNull(
            item.date?.takeIf(String::isNotBlank)?.let { "Data e Ora" to it },
            item.code?.takeIf(String::isNotBlank)?.let { "Codice Insegnamento" to it },
            item.subtitle?.takeIf(String::isNotBlank)?.let { "Corso / Dipartimento" to it },
            item.detail?.takeIf(String::isNotBlank)?.let { "Dettagli Aggiuntivi" to it },
        )
    }

    if (fields.isEmpty()) {
        LiquidEmptyState(
            title = stringResource(Res.string.ui_details_none_title),
            description = stringResource(Res.string.ui_professor_exam_details_none_desc),
            icon = LiquidIcons.Info,
            backdropState = backdropState,
        )
    } else {
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_professor_exam_round_tab),
            backdropState = backdropState,
        ) {
            fields.forEach { (label, value) ->
                LiquidPreferenceItem(
                    title = label,
                    subtitle = value,
                    icon = LiquidIcons.Info,
                    backdropState = backdropState,
                )
            }
        }
    }
}
