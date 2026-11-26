package com.anto426.uniapp.ui.components.items

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.emptystate.LiquidExpressiveHeroFrame
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.DeviceInfo
import com.anto426.uniapp.data.contract.DeviceType
import com.anto426.uniapp.ui.components.display.UniInsetRow

private fun DeviceType.iconVector(): ImageVector = when (this) {
    DeviceType.PHONE -> LiquidIcons.Smartphone
    DeviceType.PC -> LiquidIcons.Computer
    DeviceType.TABLET -> LiquidIcons.Tablet
}

@Composable
fun ConnectedDevicesSecurityHero(
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val iconTint = colorScheme.primary

    LiquidExpressiveHeroFrame(
        modifier = modifier,
        mainContent = {
            Box(
                modifier = Modifier
                    .offset(x = (-18).dp, y = (-16).dp)
                    .size(34.dp)
                    .background(iconTint.copy(alpha = 0.20f), RoundedRectangle(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LiquidIcons.Smartphone,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Icon(
                imageVector = LiquidIcons.Computer,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.90f),
                modifier = Modifier
                    .offset(x = 20.dp, y = (-3).dp)
                    .size(27.dp),
            )

            Icon(
                imageVector = LiquidIcons.Tablet,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.78f),
                modifier = Modifier
                    .offset(x = (-10).dp, y = 22.dp)
                    .size(23.dp),
            )
        },
        badgeContent = {
            Canvas(modifier = Modifier.size(24.dp)) {
                val sw = 20.dp.toPx()
                val sh = 23.dp.toPx()
                val left = center.x - sw / 2f
                val top = center.y - sh / 2f
                val midX = center.x
                val right = left + sw
                val shoulderY = top + sh * 0.20f
                val curveY = top + sh * 0.56f
                val bottomY = top + sh

                val leftHalf = Path().apply {
                    moveTo(midX, top)
                    lineTo(left, shoulderY)
                    lineTo(left, curveY)
                    cubicTo(left, top + sh * 0.82f, midX - sw * 0.14f, top + sh * 0.94f, midX, bottomY)
                    close()
                }
                val rightHalf = Path().apply {
                    moveTo(midX, top)
                    lineTo(right, shoulderY)
                    lineTo(right, curveY)
                    cubicTo(right, top + sh * 0.82f, midX + sw * 0.14f, top + sh * 0.94f, midX, bottomY)
                    close()
                }
                drawPath(path = leftHalf, color = iconTint)
                drawPath(path = rightHalf, color = iconTint.copy(alpha = 0.48f))
            }
        },
    )
}

@Composable
fun DevicePreferenceItem(
    device: DeviceInfo,
    onRevoke: (() -> Unit)?,
    showDivider: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val deviceIcon = device.type.iconVector()

    UniInsetRow(
        title = device.name,
        subtitle = "${device.location} • ${device.lastSeen}",
        icon = deviceIcon,
        iconTint = if (device.isCurrent) glassColors.success else colorScheme.primary,
        showDivider = showDivider,
        titleMaxLines = 2,
        subtitleMaxLines = 2,
        trailingContent = when {
            device.isCurrent -> ({
                Text(
                    text = stringResource(Res.string.ui_online),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.success,
                    fontWeight = FontWeight.Bold,
                )
            })
            onRevoke != null -> ({
                LiquidIconButton(
                    icon = LiquidIcons.Close,
                    onClick = onRevoke,
                    variant = LiquidIconButtonVariant.Ghost,
                    size = 48.dp,
                    iconSize = 18.dp,
                    contentDescription = stringResource(Res.string.ui_revoke_access),
                )
            })
            else -> null
        },
    )
}

@Composable
fun CurrentDeviceHero(device: DeviceInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val icon = device.type.iconVector()

    LiquidCard(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false),
        shape = RoundedRectangle(22.dp),
        border = BorderStroke(1.dp, glassColors.success.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = glassColors.success,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 40.dp,
                    iconSize = 20.dp,
                    containerColor = glassColors.success.copy(alpha = 0.14f),
                    shape = RoundedRectangle(12.dp),
                ),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = device.location,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                device.appVersion?.takeIf(String::isNotBlank)?.let { version ->
                    Text(
                        text = "UniApp $version",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(glassColors.success, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.ui_online),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.success,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
