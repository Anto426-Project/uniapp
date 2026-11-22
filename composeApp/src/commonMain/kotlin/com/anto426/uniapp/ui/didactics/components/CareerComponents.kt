package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.ALL_ACADEMIC_YEARS
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CareerSummaryHeroCard(
    kicker: String,
    headlineValue: String,
    headlineUnit: String,
    metaPills: List<Pair<String, Color?>>,
    acquiredCfu: Int,
    targetCfu: Int,
    footerLabel: String? = null,
    onFooterClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val safeTarget = targetCfu.coerceAtLeast(1)
    val progress = (acquiredCfu.toFloat() / safeTarget).coerceIn(0f, 1f)
    val percentage = (progress * 100f).roundToInt()

    LiquidCard(
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
        shape = RoundedRectangle(24.dp),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.22f)),
        clipContent = false,
    ) {
        Column(modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = kicker.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = headlineValue,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = headlineUnit,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 5.dp),
                        )
                    }

                    if (metaPills.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            metaPills.forEach { (label, accent) ->
                                val pillTint = accent ?: colorScheme.primary
                                LiquidBadge(
                                    text = label,
                                    containerColor = pillTint.copy(alpha = 0.13f),
                                    contentColor = pillTint,
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    UniCircularProgressRing(
                        progress = progress,
                        centerText = "$percentage%",
                        subtitleText = "CFU",
                        size = 76.dp,
                        strokeWidth = 7.dp,
                    )
                    Text(
                        text = stringResource(Res.string.ui_career_cfu_pair, acquiredCfu, safeTarget),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            if (footerLabel != null && onFooterClick != null) {
                val footerShape = remember { RoundedRectangle(16.dp) }
                LiquidHorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = colorScheme.outlineVariant.copy(alpha = 0.28f),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .liquidClickable(shape = footerShape, role = Role.Button, onClick = onFooterClick)
                        .semantics(mergeDescendants = true) {}
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Analytics,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 30.dp,
                            iconSize = 16.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedRectangle(9.dp),
                        ),
                    )
                    Text(
                        text = footerLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.primary.copy(alpha = 0.72f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun CareerYearChipRow(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    includeAllChip: Boolean = false,
) {
    if (years.isEmpty()) return
    UniHorizontalChipRow {
        if (includeAllChip && years.size > 1) {
            key(ALL_ACADEMIC_YEARS) {
                val selected = selectedYear == ALL_ACADEMIC_YEARS
                LiquidChip(
                    label = stringResource(Res.string.ui_career_all_statuses),
                    selected = selected,
                    leadingIcon = if (selected) LiquidIcons.Check else null,
                    onClick = { onYearSelected(ALL_ACADEMIC_YEARS) },
                )
            }
        }
        years.forEach { year ->
            key(year) {
                val selected = year == selectedYear
                LiquidChip(
                    label = careerYearLabel(year),
                    selected = selected,
                    leadingIcon = if (selected) LiquidIcons.Check else null,
                    onClick = { onYearSelected(year) },
                )
            }
        }
    }
}

@Composable
internal fun CareerStatusFilterRow(
    selectedStatus: CourseStatus?,
    onStatusSelected: (CourseStatus?) -> Unit,
) {
    val options: List<Pair<CourseStatus?, String>> = listOf(
        null to stringResource(Res.string.ui_career_all_statuses),
        CourseStatus.ACTIVE to stringResource(Res.string.ui_career_to_complete),
        CourseStatus.COMPLETED to stringResource(Res.string.ui_status_completed),
    )
    UniHorizontalChipRow {
        options.forEach { (status, label) ->
            key(status) {
                val selected = selectedStatus == status
                LiquidChip(
                    label = label,
                    selected = selected,
                    leadingIcon = if (selected) LiquidIcons.Check else null,
                    onClick = { onStatusSelected(status) },
                )
            }
        }
    }
}

@Composable
internal fun careerYearLabel(year: Int): String = when (year) {
    0 -> stringResource(Res.string.ui_year_unspecified)
    1 -> stringResource(Res.string.ui_year_first)
    2 -> stringResource(Res.string.ui_year_second)
    3 -> stringResource(Res.string.ui_year_third)
    else -> stringResource(Res.string.ui_year_nth, year)
}
