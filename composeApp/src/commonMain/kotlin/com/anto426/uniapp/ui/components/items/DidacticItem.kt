package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidBadgedBox
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop

@Composable
fun DidacticRow(item1: @Composable () -> Unit, item2: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().graphicsLayer(clip = false), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f).graphicsLayer(clip = false)) { item1() }
        Box(Modifier.weight(1f).graphicsLayer(clip = false)) { item2() }
    }
}

@Composable
fun DidacticItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backdropState: Backdrop,
    badgeCount: Int? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(modifier = Modifier.graphicsLayer(clip = false), backdropState = backdropState, shape = RoundedCornerShape(22.dp), contentPadding = 16.dp, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidBadgedBox(badge = { badgeCount?.let { LiquidBadge(count = it, backdropState = backdropState) } }) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 40.dp,
                        iconSize = 20.dp,
                        containerColor = iconColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                )
            }
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, letterSpacing = (-0.3).sp)
                Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant, lineHeight = 14.sp)
            }
        }
    }
}
