package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.badge.LiquidBadgedBox
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.services.ServiceData
import com.anto426.uniapp.ui.components.display.UniBentoTile
import com.anto426.uniapp.ui.components.display.UniBlockDefaults
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.kyant.shapes.RoundedRectangle

@Composable
fun ServiceItem(
    data: ServiceData,
    modifier: Modifier = Modifier,
    accentColor: Color = serviceAccentColor(data.id),
    onClick: () -> Unit = {},
) {
    UniBentoTile(
        title = data.title,
        subtitle = data.subtitle,
        icon = data.icon,
        modifier = modifier,
        iconTint = accentColor,
        badgeCount = data.badgeCount,
        badgeLabel = data.badgeLabel,
        onClick = onClick,
    )
}

@Composable
fun ServiceWideItem(
    data: ServiceData,
    modifier: Modifier = Modifier,
    accentColor: Color = serviceAccentColor(data.id),
    onClick: () -> Unit = {},
) {
    val statusBadge = data.badgeLabel
    UniInsetGroup(
        modifier = modifier,
        border = if (!statusBadge.isNullOrBlank()) BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)) else null,
        animateEntrance = false,
    ) {
        UniInsetRow(
            title = data.title,
            subtitle = data.subtitle,
            iconTint = accentColor,
            onClick = onClick,
            showDivider = false,
            showTrailingChevron = !statusBadge.isNullOrBlank(),
            subtitleMaxLines = 2,
            leadingContent = {
                LiquidBadgedBox(badge = { data.badgeCount?.let { LiquidBadge(count = it) } }) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = UniBlockDefaults.IconContainerSize,
                            iconSize = UniBlockDefaults.IconSize,
                            containerColor = accentColor.copy(alpha = 0.14f),
                            shape = RoundedRectangle(UniBlockDefaults.IconCornerRadius),
                        ),
                    )
                }
            },
            trailingContent = if (!statusBadge.isNullOrBlank()) {
                {
                    LiquidBadge(
                        text = statusBadge,
                        containerColor = accentColor.copy(alpha = 0.16f),
                        contentColor = accentColor,
                    )
                }
            } else {
                null
            },
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
