package com.anto426.uniapp.android.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidBadgedBox
import com.anto426.uniapp.android.ui.models.ServiceData
import com.kyant.backdrop.Backdrop

@Composable
fun ServiceItem(data: ServiceData, backdropState: Backdrop, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(modifier = modifier.graphicsLayer(clip = false), backdropState = backdropState, shape = RoundedCornerShape(22.dp), contentPadding = 14.dp, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LiquidBadgedBox(badge = { data.badgeCount?.let { LiquidBadge(count = it, backdropState = backdropState) } }) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.primaryContainer.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                    Icon(data.icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(data.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, letterSpacing = (-0.3).sp)
                Text(data.subtitle, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant, lineHeight = 14.sp)
            }
        }
    }
}
