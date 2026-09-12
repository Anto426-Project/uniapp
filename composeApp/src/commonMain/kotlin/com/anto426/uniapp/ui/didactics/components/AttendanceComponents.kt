package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.kyant.shapes.RoundedRectangle

@Composable
fun AttendanceKpiCard(
    label: String,
    value: String,
    subvalue: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        shape = RoundedRectangle(20.dp),
        contentPadding = 12.dp,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.primary,
            )
            Text(
                text = subvalue,
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
