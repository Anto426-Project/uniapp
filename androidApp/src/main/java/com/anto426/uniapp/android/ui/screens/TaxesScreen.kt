package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anto426.uniapp.android.R
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.uniapp.android.ui.components.items.TaxPaymentItem
import com.anto426.uniapp.android.ui.components.items.TaxPaymentList
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun TaxesScreen(backdropState: Backdrop) {
    val payments = UiInitialData.taxPayments
    val toPay = payments.filterNot { it.isPaid }
    val paid = payments.filter { it.isPaid }
    UniScreenColumn {
        LiquidStatusCard(
            title = if (toPay.isEmpty()) stringResource(R.string.ui_pay_all) else stringResource(R.string.ui_pending_payments),
            description = if (toPay.isEmpty()) stringResource(R.string.ui_pending_payment_description) else stringResource(R.string.ui_pending_count, toPay.size.toString()),
            statusType = if (toPay.isEmpty()) LiquidStatusType.Success else LiquidStatusType.Warning,
            backdropState = backdropState
        )
        if (toPay.isNotEmpty()) {
            LiquidSectionTitle(title = stringResource(R.string.ui_pending_section), subtitle = stringResource(R.string.ui_pending_section_subtitle))
            TaxPaymentList(toPay, backdropState)
        }
        Spacer(Modifier.height(8.dp))
        if (paid.isNotEmpty()) {
            LiquidSectionTitle(title = stringResource(R.string.ui_paid_section), subtitle = stringResource(R.string.ui_paid_section_subtitle))
            TaxPaymentList(paid, backdropState)
        }
    }
}
