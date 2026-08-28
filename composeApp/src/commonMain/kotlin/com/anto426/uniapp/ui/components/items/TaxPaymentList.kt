package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.ui.models.TaxPaymentData
import com.kyant.backdrop.Backdrop

@Composable
fun TaxPaymentList(payments: List<TaxPaymentData>, backdropState: Backdrop) {
    Column(Modifier.fillMaxWidth().graphicsLayer(clip = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        payments.forEach { TaxPaymentItem(it, backdropState) }
    }
}
