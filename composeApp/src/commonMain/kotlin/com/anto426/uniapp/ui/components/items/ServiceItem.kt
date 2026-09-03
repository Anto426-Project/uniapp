package com.anto426.uniapp.ui.components.items

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
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.uniapp.model.services.ServiceData
import com.kyant.backdrop.Backdrop

@Composable
fun ServiceItem(data: ServiceData, backdropState: Backdrop, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        modifier = modifier.graphicsLayer(clip = false),
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidBadgedBox(
                badge = { data.badgeCount?.let { LiquidBadge(count = it, backdropState = backdropState) } }
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 40.dp,
                        iconSize = 20.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                )
            }
            Column {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
