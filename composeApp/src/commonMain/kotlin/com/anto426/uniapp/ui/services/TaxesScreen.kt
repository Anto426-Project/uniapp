package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.services.presentation.TaxesUiState
import com.anto426.uniapp.ui.components.display.UniBentoStatTile
import com.anto426.uniapp.ui.components.items.TaxPaymentList
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TaxesScreen(uiState: TaxesUiState) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val hasPending = uiState.pendingPayments.isNotEmpty()
    val statusColor = if (hasPending) colorScheme.error else glassColors.success

    UniScreenColumn {
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(26.dp),
            contentPadding = 20.dp,
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = if (hasPending) LiquidIcons.Warning else LiquidIcons.Check,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.liquidIconContainer(
                                    containerSize = 40.dp,
                                    iconSize = 20.dp,
                                    containerColor = statusColor.copy(alpha = 0.14f),
                                    shape = RoundedRectangle(13.dp),
                                ),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(Res.string.ui_taxes_status_header),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                )
                                Text(
                                    text = if (hasPending) {
                                        stringResource(Res.string.ui_pending_section_subtitle)
                                    } else {
                                        stringResource(Res.string.ui_taxes_all_paid_desc)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface,
                                )
                            }
                        }

                        LiquidBadge(
                            text = if (hasPending) {
                                stringResource(Res.string.ui_taxes_status_pending, uiState.pendingPayments.size)
                            } else {
                                stringResource(Res.string.ui_taxes_status_regular)
                            },
                            containerColor = statusColor.copy(alpha = 0.15f),
                            contentColor = statusColor,
                        )
                    }

                    LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        UniBentoStatTile(
                            label = stringResource(Res.string.ui_pending_section),
                            value = "${uiState.pendingPayments.size}",
                            icon = LiquidIcons.Time,
                            accentColor = if (hasPending) colorScheme.error else colorScheme.primary,
                            valueColor = if (hasPending) colorScheme.error else colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        UniBentoStatTile(
                            label = stringResource(Res.string.ui_paid_section),
                            value = "${uiState.paidPayments.size}",
                            icon = LiquidIcons.Check,
                            accentColor = glassColors.success,
                            valueColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

        if (uiState.pendingPayments.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_pending_section),
                subtitle = stringResource(Res.string.ui_pending_section_subtitle),
            )
            TaxPaymentList(uiState.pendingPayments)
        }

        if (uiState.paidPayments.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_paid_section),
                subtitle = stringResource(Res.string.ui_paid_section_subtitle),
            )
            TaxPaymentList(uiState.paidPayments)
        }
    }
}
