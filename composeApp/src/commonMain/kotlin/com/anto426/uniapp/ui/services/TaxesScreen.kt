package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.services.presentation.TaxesUiState
import com.anto426.uniapp.ui.components.items.TaxPaymentList
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun TaxesScreen(backdropState: Backdrop, uiState: TaxesUiState) {
    val colorScheme = MaterialTheme.colorScheme
    val pendingCount = uiState.pendingPayments.size
    val isAllPaid = pendingCount == 0

    UniScreenColumn {
        // Hero Summary Card
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 20.dp,
            interactiveGelatin = false,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_taxes_status_header),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        letterSpacing = 0.8.sp,
                    )
                    LiquidBadge(
                        text = if (isAllPaid) stringResource(Res.string.ui_taxes_status_regular) else stringResource(Res.string.ui_taxes_status_pending, pendingCount),
                        containerColor = if (isAllPaid) colorScheme.primaryContainer.copy(alpha = 0.5f) else colorScheme.errorContainer.copy(alpha = 0.5f),
                        contentColor = if (isAllPaid) colorScheme.primary else colorScheme.error,
                        backdropState = backdropState,
                    )
                }

                Text(
                    text = if (isAllPaid) stringResource(Res.string.ui_taxes_all_paid_desc) else stringResource(Res.string.ui_pending_count, pendingCount.toString()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )

                Text(
                    text = stringResource(Res.string.ui_taxes_esse3_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (uiState.pendingPayments.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_pending_section),
                subtitle = stringResource(Res.string.ui_pending_section_subtitle),
            )
            TaxPaymentList(uiState.pendingPayments, backdropState)
            Spacer(Modifier.height(8.dp))
        }

        if (uiState.paidPayments.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_paid_section),
                subtitle = stringResource(Res.string.ui_paid_section_subtitle),
            )
            TaxPaymentList(uiState.paidPayments, backdropState)
        }
    }
}
