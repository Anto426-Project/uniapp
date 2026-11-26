package com.anto426.uniapp.ui.components.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.anto426.uniapp.ui.components.display.UniBentoTile
import com.anto426.uniapp.ui.components.display.UniBentoTileRow

@Composable
fun DidacticRow(
    item1: @Composable () -> Unit,
    item2: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    UniBentoTileRow(
        item1 = item1,
        item2 = item2,
        modifier = modifier,
    )
}

@Composable
fun DidacticItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeCount: Int? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    UniBentoTile(
        title = title,
        subtitle = subtitle,
        icon = icon,
        modifier = modifier,
        iconTint = iconColor,
        badgeCount = badgeCount,
        onClick = onClick,
    )
}
