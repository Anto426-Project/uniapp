package com.anto426.uniapp.ui.components.items

import androidx.compose.runtime.Composable
import com.anto426.uniapp.model.services.TaxPaymentData
import com.anto426.uniapp.ui.components.display.UniInsetGroup

@Composable
fun TaxPaymentList(payments: List<TaxPaymentData>) {
    UniInsetGroup {
        payments.forEachIndexed { index, payment ->
            TaxPaymentItem(
                data = payment,
                showDivider = index < payments.lastIndex,
            )
        }
    }
}
