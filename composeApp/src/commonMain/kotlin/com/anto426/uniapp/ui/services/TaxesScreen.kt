package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.uniapp.ui.components.items.TaxPaymentList
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.services.presentation.TaxesUiState
import com.kyant.backdrop.Backdrop

@Composable
fun TaxesScreen(backdropState: Backdrop, uiState: TaxesUiState) {
    UniScreenColumn {
        LiquidStatusCard(
            title = if (uiState.pendingPayments.isEmpty()) stringResource(Res.string.ui_pay_all) else stringResource(Res.string.ui_pending_payments),
            description = if (uiState.pendingPayments.isEmpty()) stringResource(Res.string.ui_pending_payment_description) else stringResource(Res.string.ui_pending_count, uiState.pendingPayments.size.toString()),
            statusType = if (uiState.pendingPayments.isEmpty()) LiquidStatusType.Success else LiquidStatusType.Warning,
            backdropState = backdropState
        )
        if (uiState.pendingPayments.isNotEmpty()) {
            LiquidSectionHeader(title = stringResource(Res.string.ui_pending_section), subtitle = stringResource(Res.string.ui_pending_section_subtitle))
            TaxPaymentList(uiState.pendingPayments, backdropState)
        }
        Spacer(Modifier.height(8.dp))
        if (uiState.paidPayments.isNotEmpty()) {
            LiquidSectionHeader(title = stringResource(Res.string.ui_paid_section), subtitle = stringResource(Res.string.ui_paid_section_subtitle))
            TaxPaymentList(uiState.paidPayments, backdropState)
        }
    }
}
