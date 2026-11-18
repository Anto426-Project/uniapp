package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.badge.LiquidBadgedBox
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.services.ServiceData
import com.kyant.shapes.RoundedRectangle

@Composable
fun ServiceItem(
    data: ServiceData,
    modifier: Modifier = Modifier,
    accentColor: Color = serviceAccentColor(data.id),
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusBadge = data.badgeLabel
    LiquidCard(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .graphicsLayer(clip = false),
        shape = RoundedRectangle(20.dp),
        contentPadding = 13.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 82.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ServiceTileIcon(data = data, accentColor = accentColor)
                if (!statusBadge.isNullOrBlank()) {
                    LiquidBadge(
                        text = statusBadge,
                        containerColor = accentColor.copy(alpha = 0.16f),
                        contentColor = accentColor,
                    )
                } else {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            ServiceTileLabels(title = data.title, subtitle = data.subtitle)
        }
    }
}

@Composable
fun ServiceWideItem(
    data: ServiceData,
    modifier: Modifier = Modifier,
    accentColor: Color = serviceAccentColor(data.id),
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusBadge = data.badgeLabel
    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .graphicsLayer(clip = false),
        shape = RoundedRectangle(20.dp),
        contentPadding = 13.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ServiceTileIcon(data = data, accentColor = accentColor)
            ServiceTileLabels(
                title = data.title,
                subtitle = data.subtitle,
                modifier = Modifier.weight(1f),
            )
            if (!statusBadge.isNullOrBlank()) {
                LiquidBadge(
                    text = statusBadge,
                    containerColor = accentColor.copy(alpha = 0.16f),
                    contentColor = accentColor,
                )
            }
            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ServiceTileIcon(data: ServiceData, accentColor: Color) {
    LiquidBadgedBox(
        badge = { data.badgeCount?.let { LiquidBadge(count = it) } },
    ) {
        Icon(
            imageVector = data.icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.liquidIconContainer(
                containerSize = 36.dp,
                iconSize = 18.dp,
                containerColor = accentColor.copy(alpha = 0.14f),
                shape = RoundedRectangle(11.dp),
            ),
        )
    }
}

@Composable
private fun ServiceTileLabels(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            letterSpacing = (-0.2).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            lineHeight = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun serviceAccentColor(serviceId: String): Color {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    return when (serviceId) {
        "transport", "university-web" -> colorScheme.primary
        "taxes", "esse3" -> glassColors.warning
        "contacts", "library", "moodle" -> colorScheme.tertiary
        "news", "student-office", "email" -> colorScheme.secondary
        else -> colorScheme.primary
    }
}
