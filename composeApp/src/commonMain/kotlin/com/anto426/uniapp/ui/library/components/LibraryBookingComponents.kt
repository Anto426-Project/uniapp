package com.anto426.uniapp.ui.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.kyant.shapes.RoundedRectangle

@Composable
internal fun StepCardHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.liquidIconContainer(
                containerSize = 34.dp,
                iconSize = 18.dp,
                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                shape = RoundedRectangle(10.dp),
            ),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
        )
    }
}
