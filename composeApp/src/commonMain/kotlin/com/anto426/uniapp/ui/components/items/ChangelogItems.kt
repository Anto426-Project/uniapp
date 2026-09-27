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

/**
 * Singola voce del Changelog con badge di stato e formattazione chiara.
 */
@Composable
fun ChangelogItem(
    item: ChangelogItemData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        LiquidBadge(
            text = item.tag,
            containerColor = item.tagColor.copy(alpha = 0.16f),
            contentColor = item.tagColor,
            modifier = Modifier.padding(top = 2.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            val titleText = item.title
            if (titleText.isNotBlank()) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
            )
        }
    }
}

/**
 * Card tematica di categoria (es. Design, Prestazioni, Sicurezza) in perfetto stile Liquid Monet / ColorOS 17.
 */
@Composable
fun ChangelogCategoryCard(
    categoryName: String,
    items: List<ChangelogItemData>,
    modifier: Modifier = Modifier,
) {
    val icon = resolveCategoryIcon(categoryName)

    LiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedRectangle(22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Intestazione della Categoria
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Conteggio modifiche
                Box(
                    modifier = Modifier
                        .clip(Capsule())
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${items.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 2.dp))

            // Elenco voci
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEachIndexed { index, item ->
                    ChangelogItem(item = item)
                    if (index < items.lastIndex) {
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

/**
 * Versione storica espandibile con LiquidAccordionItem.
 */
@Composable
fun ChangelogVersion(
    version: ChangelogVersionData,
    isExpanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidAccordionItem(
        title = "UniApp ${version.version}",
        subtitle = version.date,
        leadingIcon = LiquidIcons.Refresh,
        isExpanded = isExpanded,
        onExpandedChange = onExpand,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            version.items.forEach { item ->
                ChangelogItem(item)
            }
        }
    }
}

private fun resolveCategoryIcon(category: String): ImageVector {
    val lower = category.lowercase()
    return when {
        lower.contains("design") || lower.contains("interfaccia") || lower.contains("grafic") || lower.contains("stile") ->
            LiquidIcons.Palette
        lower.contains("prestazion") || lower.contains("fluid") || lower.contains("veloc") || lower.contains("ottimizz") ->
            LiquidIcons.Analytics
        lower.contains("sicur") || lower.contains("keystore") || lower.contains("firma") || lower.contains("privac") ->
            LiquidIcons.Lock
        lower.contains("serviz") || lower.contains("didattic") || lower.contains("carrier") || lower.contains("esami") ->
            LiquidIcons.MenuBook
        lower.contains("correz") || lower.contains("fix") || lower.contains("bug") || lower.contains("risolt") ->
            LiquidIcons.Check
        else ->
            LiquidIcons.Star
    }
}
