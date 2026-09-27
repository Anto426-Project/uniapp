package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.accordion.LiquidAccordionItem
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.updates.ChangelogItemData
import com.anto426.uniapp.model.updates.ChangelogVersionData
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

@Composable
fun ChangelogVersion(version: ChangelogVersionData, isExpanded: Boolean, onExpand: (Boolean) -> Unit) {
    LiquidAccordionItem(
        title = version.version,
        subtitle = version.date,
        leadingIcon = LiquidIcons.Refresh,
        isExpanded = isExpanded,
        onExpandedChange = onExpand,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            version.items.forEach { ChangelogItem(it) }
        }
    }
}

@Composable
fun ChangelogItem(item: ChangelogItemData) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        LiquidBadge(text = item.tag, containerColor = item.tagColor.copy(alpha = 0.15f), contentColor = item.tagColor, modifier = Modifier.padding(top = 2.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 18.sp)
        }
    }
}
