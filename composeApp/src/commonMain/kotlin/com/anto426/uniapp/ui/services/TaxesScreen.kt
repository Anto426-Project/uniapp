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
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.services.presentation.TaxesFilter
import com.anto426.uniapp.services.presentation.TaxesUiState
import com.anto426.uniapp.ui.components.display.UniCircularProgressRing
import com.anto426.uniapp.ui.components.items.TaxPaymentList
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.state.UniExpressiveStatusState
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt
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

    val totalInstallments = uiState.pendingPayments.size + uiState.paidPayments.size

    UniScreenColumn {
        // 1. Situazione Contribuzione Hero Card
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
                        modifier = Modifier.weight(1f),
                    )
                    LiquidBadge(
                        text = if (hasPending) {
                            stringResource(Res.string.ui_taxes_status_pending, uiState.pendingPayments.size)
                        } else {
                            stringResource(Res.string.ui_taxes_status_regular)
                        },
                        containerColor = statusColor.copy(alpha = 0.14f),
                        contentColor = statusColor,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (hasPending) {
                                    Res.string.ui_taxes_amount_due_label
                                } else {
                                    Res.string.ui_taxes_amount_paid_label
                                },
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = if (hasPending) uiState.dueAmountLabel else uiState.paidTotalLabel,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = if (hasPending) statusColor else colorScheme.onSurface,
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

                    UniCircularProgressRing(
                        progress = uiState.paidProgress,
                        centerText = "${(uiState.paidProgress * 100f).roundToInt()}%",
                        subtitleText = "${uiState.paidPayments.size}/$totalInstallments",
                        size = 74.dp,
                        strokeWidth = 7.dp,
                        primaryColor = statusColor,
                        secondaryColor = statusColor.copy(alpha = 0.65f),
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
                        label = stringResource(
                            if (hasPending) {
                                Res.string.ui_taxes_amount_paid_label
                            } else {
                                Res.string.ui_taxes_amount_due_label
                            },
                        ),
                        value = if (hasPending) uiState.paidTotalLabel else uiState.dueAmountLabel,
                        valueColor = if (hasPending) colorScheme.onSurface else glassColors.success,
                        modifier = Modifier.weight(1.35f),
                    )
                }
            }
        }
        }

        // 2. Filter Chips (unclipped)
        val filterOptions = listOf(
            Triple(TaxesFilter.ALL, stringResource(Res.string.ui_taxes_all_filter), totalInstallments),
            Triple(TaxesFilter.PENDING, stringResource(Res.string.ui_pending_section), uiState.pendingPayments.size),
            Triple(TaxesFilter.PAID, stringResource(Res.string.ui_paid_section), uiState.paidPayments.size),
        )
        UniHorizontalChipRow {
            filterOptions.forEach { (filterOption, label, count) ->
                key(filterOption) {
                    val selected = uiState.filter == filterOption
                    LiquidChip(
                        label = label,
                        selected = selected,
                        badge = count.toString(),
                        leadingIcon = if (selected) LiquidIcons.Check else null,
                        onClick = { onFilterSelected(filterOption) },
                    )
                }
            }
        }

        // 3. Installments Lists
        UniContentHost(
            targetState = uiState.filter,
            modifier = Modifier.fillMaxWidth(),
            isForward = { from, to -> to.ordinal >= from.ordinal },
            label = "TaxesFilterSwitcher",
        ) { activeFilter ->
            val filterShowsPending = activeFilter != TaxesFilter.PAID && uiState.pendingPayments.isNotEmpty()
            val filterShowsPaid = activeFilter != TaxesFilter.PENDING && uiState.paidPayments.isNotEmpty()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (filterShowsPending) {
                    UniAnimatedEntrance(order = 1) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LiquidSectionHeader(
                                title = stringResource(Res.string.ui_pending_section),
                                subtitle = stringResource(Res.string.ui_pending_section_subtitle),
                            )
                            TaxPaymentList(uiState.pendingPayments)
                        }
                    }
                }

                if (filterShowsPaid) {
                    UniAnimatedEntrance(order = if (filterShowsPending) 2 else 1) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LiquidSectionHeader(
                                title = stringResource(Res.string.ui_paid_section),
                                subtitle = "${stringResource(Res.string.ui_taxes_amount_paid_label)}: ${uiState.paidTotalLabel}",
                            )
                            TaxPaymentList(uiState.paidPayments)
                        }
                    }
                }

                if (!filterShowsPending && !filterShowsPaid) {
                    UniExpressiveStatusState(
                        title = stringResource(Res.string.ui_taxes_filter_empty),
                        description = stringResource(Res.string.ui_taxes_filter_empty_desc),
                        inline = true,
                        mainIcon = LiquidIcons.CreditCard,
                    )
                }
            }
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
