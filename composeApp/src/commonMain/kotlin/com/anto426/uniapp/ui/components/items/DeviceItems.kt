package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.model.settings.DeviceType
import com.kyant.backdrop.Backdrop

@Composable
fun DevicePreferenceItem(
    device: DeviceInfo,
    backdropState: Backdrop,
    onRevoke: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val deviceIcon = when(device.type) {
        DeviceType.PHONE -> LiquidIcons.Phone
        DeviceType.PC -> LiquidIcons.Home
        DeviceType.TABLET -> LiquidIcons.Star
    }

    LiquidPreferenceItem(
        title = device.name,
        subtitle = "${device.location}\n${device.lastSeen}",
        icon = deviceIcon,
        backdropState = backdropState,
        onClick = { /* Device detail logic */ },
        trailingContent = {
            if (!device.isCurrent) {
                LiquidButton(
                    text = "Revoca",
                    onClick = onRevoke,
                    variant = LiquidButtonVariant.Text,
                    backdropState = backdropState
                )
            } else {
                Text(
                    text = "Online",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun CurrentDeviceHero(device: DeviceInfo, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(24.dp),
        contentPadding = 16.dp,
        interactiveGelatin = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(device.type) {
                    DeviceType.PHONE -> LiquidIcons.Phone
                    DeviceType.PC -> LiquidIcons.Home
                    DeviceType.TABLET -> LiquidIcons.Star
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = device.location,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(Color(0xFF00C853), CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Online",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00C853),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
