package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.services.presentation.TaxesFilter
import com.anto426.uniapp.services.presentation.TaxesUiState
import com.anto426.uniapp.ui.components.items.TaxPaymentList
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TaxesScreen(
    uiState: TaxesUiState,
    onFilterSelected: (TaxesFilter) -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val hasPending = uiState.pendingPayments.isNotEmpty()
    val statusColor = if (hasPending) glassColors.warning else glassColors.success

    val showPending = uiState.filter != TaxesFilter.PAID && uiState.pendingPayments.isNotEmpty()
    val showPaid = uiState.filter != TaxesFilter.PENDING && uiState.paidPayments.isNotEmpty()

    UniScreenColumn {
        // 1. Situazione Contribuzione Hero Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (hasPending) LiquidIcons.Warning else LiquidIcons.Check,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 36.dp,
                            iconSize = 18.dp,
                            containerColor = statusColor.copy(alpha = 0.14f),
                            shape = RoundedRectangle(11.dp),
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.ui_taxes_status_header),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_taxes_amount_due_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = uiState.dueAmountLabel,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        maxLines = 1,
                    )
                    Text(
                        text = if (hasPending && !uiState.nextDueDate.isNullOrBlank()) {
                            stringResource(Res.string.ui_taxes_next_deadline, uiState.nextDueDate)
                        } else if (hasPending) {
                            stringResource(Res.string.ui_pending_section_subtitle)
                        } else {
                            stringResource(Res.string.ui_taxes_all_paid_desc)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TaxesHeroMetric(
                        label = stringResource(Res.string.ui_pending_section),
                        value = "${uiState.pendingPayments.size}",
                        valueColor = if (hasPending) glassColors.warning else colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    TaxesHeroMetric(
                        label = stringResource(Res.string.ui_paid_section),
                        value = "${uiState.paidPayments.size}",
                        valueColor = glassColors.success,
                        modifier = Modifier.weight(1f),
                    )
                    TaxesHeroMetric(
                        label = stringResource(Res.string.ui_taxes_amount_paid_label),
                        value = uiState.paidTotalLabel,
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1.35f),
                    )
                }
            }
        }

        // 2. Filter Chips (unclipped)
        val filterOptions = listOf(
            TaxesFilter.ALL to stringResource(Res.string.ui_taxes_all_filter),
            TaxesFilter.PENDING to stringResource(Res.string.ui_pending_section),
            TaxesFilter.PAID to stringResource(Res.string.ui_paid_section),
        )
        UniHorizontalChipRow {
            filterOptions.forEach { (filterOption, label) ->
                key(filterOption) {
                    val selected = uiState.filter == filterOption
                    LiquidChip(
                        label = label,
                        selected = selected,
                        leadingIcon = if (selected) LiquidIcons.Check else null,
                        onClick = { onFilterSelected(filterOption) },
                    )
                }
            }
        }

        // 3. Installments Lists
        if (showPending) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_pending_section),
                subtitle = stringResource(Res.string.ui_pending_section_subtitle),
            )
            TaxPaymentList(uiState.pendingPayments)
        }

        if (showPaid) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_paid_section),
                subtitle = "${stringResource(Res.string.ui_taxes_amount_paid_label)}: ${uiState.paidTotalLabel}",
            )
            TaxPaymentList(uiState.paidPayments)
        }

        if (!showPending && !showPaid) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_taxes_filter_empty),
                description = stringResource(Res.string.ui_taxes_filter_empty_desc),
            )
        }

        // 4. Footer Note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Icon(
                imageVector = LiquidIcons.Info,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = stringResource(Res.string.ui_taxes_esse3_footer),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun TaxesHeroMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
