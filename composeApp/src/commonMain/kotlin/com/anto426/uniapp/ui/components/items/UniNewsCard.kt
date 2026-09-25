package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.news.NewsItem
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private data class NewsCategoryConfig(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
fun UniNewsCard(
    news: NewsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fixedHeight: Dp? = null,
    categoryLabel: String? = null,
    homeCard: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val sourceLabel = categoryLabel ?: news.category.takeIf { value ->
        value.isNotBlank() && !value.all(Char::isDigit)
    }
    val dateLabel = news.publishedAt.ifBlank { stringResource(Res.string.ui_news_official_notice) }

    val categoryConfig = when {
        sourceLabel != null -> NewsCategoryConfig(
            label = sourceLabel,
            subtitle = dateLabel,
            icon = when {
                sourceLabel.contains("dip", ignoreCase = true) -> LiquidIcons.Star
                sourceLabel.contains("ateneo", ignoreCase = true) -> LiquidIcons.Home
                else -> LiquidIcons.MenuBook
            },
            color = colorScheme.primary,
        )
        news.type == LiquidStatusType.Success -> NewsCategoryConfig(
            label = stringResource(Res.string.ui_news_generic),
            subtitle = dateLabel,
            icon = LiquidIcons.Calendar,
            color = glassColors.success,
        )
        news.type == LiquidStatusType.Warning -> NewsCategoryConfig(
            label = "Avviso",
            subtitle = dateLabel,
            icon = LiquidIcons.Notifications,
            color = glassColors.warning,
        )
        news.type == LiquidStatusType.Error -> NewsCategoryConfig(
            label = "Importante",
            subtitle = dateLabel,
            icon = LiquidIcons.Warning,
            color = glassColors.error,
        )
        else -> NewsCategoryConfig(
            label = stringResource(Res.string.ui_news_generic),
            subtitle = dateLabel,
            icon = LiquidIcons.MenuBook,
            color = colorScheme.primary,
        )
    }

    val hasDescription = news.description.isNotBlank()

    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (fixedHeight != null) Modifier.height(fixedHeight) else Modifier),
        shape = RoundedRectangle(if (homeCard) 22.dp else 20.dp),
        contentPadding = 16.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (homeCard) 12.dp else 10.dp),
        ) {
            // Header: Optical icon pod + Category info + Trailing CTA pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (homeCard) 40.dp else 32.dp)
                            .clip(RoundedRectangle(if (homeCard) 12.dp else 10.dp))
                            .background(categoryConfig.color.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = categoryConfig.icon,
                            contentDescription = null,
                            tint = categoryConfig.color,
                            modifier = Modifier.size(if (homeCard) 20.dp else 16.dp),
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = categoryConfig.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = categoryConfig.color,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = categoryConfig.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(Capsule())
                        .background(colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_details),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 11.sp,
                    )
                    Icon(
                        imageVector = LiquidIcons.ArrowForward,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }

            // Headline & Description with clear, compact typography
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                )

                if (hasDescription) {
                    Text(
                        text = news.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}
