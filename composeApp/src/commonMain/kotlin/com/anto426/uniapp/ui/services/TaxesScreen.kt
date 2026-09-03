package com.anto426.uniapp.ui.services

import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.services.presentation.TaxesUiState
import com.anto426.uniapp.ui.components.items.TaxPaymentList
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TaxesScreen(backdropState: Backdrop, uiState: TaxesUiState) {
    UniScreenColumn {
        if (uiState.pendingPayments.isNotEmpty()) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_pending_section),
                subtitle = stringResource(Res.string.ui_pending_section_subtitle),
            )
            TaxPaymentList(uiState.pendingPayments, backdropState)
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
