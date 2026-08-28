package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.models.TaxPaymentData
import com.kyant.backdrop.Backdrop

@Composable
fun TaxPaymentItem(data: TaxPaymentData, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = if (data.isPaid) colorScheme.primary else colorScheme.error
    LiquidCard(backdropState = backdropState, shape = RoundedCornerShape(22.dp), contentPadding = 18.dp, modifier = Modifier.graphicsLayer(clip = false)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(data.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    Text(data.date, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text("IUV: ${data.iuv}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(data.amount, fontSize = 18.sp, fontWeight = FontWeight.Black, color = colorScheme.onSurface, letterSpacing = (-0.5).sp)
                    LiquidBadge(text = if (data.isPaid) "PAGATA" else "DA PAGARE", containerColor = statusColor.copy(alpha = 0.15f), contentColor = statusColor, backdropState = backdropState)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidButton(
                    text = if (data.isPaid) "Ricevuta PDF" else "Paga con PagoPA",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    variant = if (data.isPaid) LiquidButtonVariant.Glass else LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Small,
                    backdropState = backdropState,
                    leadingIcon = { Icon(if (data.isPaid) LiquidIcons.Info else LiquidIcons.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                LiquidButton(text = "Dettagli", onClick = {}, modifier = Modifier.weight(0.6f), variant = LiquidButtonVariant.Tonal, size = LiquidButtonSize.Small, backdropState = backdropState)
            }
        }
    }
}
