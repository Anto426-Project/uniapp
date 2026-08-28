package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.uniapp.ui.models.TransportRoute
import com.kyant.backdrop.Backdrop

@Composable
fun TransportItem(route: TransportRoute, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(backdropState = backdropState, shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(route.route, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, fontSize = 15.sp)
                Text(route.time, color = colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            LiquidBadge(text = route.countdown, backdropState = backdropState)
        }
    }
}
