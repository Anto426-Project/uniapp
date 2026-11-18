package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.news.NewsItem
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

internal data class NewsCategoryVisuals(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
internal fun resolveNewsCategoryVisuals(
    news: NewsItem,
    categoryLabel: String? = null,
): NewsCategoryVisuals {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val sourceLabel = categoryLabel ?: news.category.takeIf { value ->
        value.isNotBlank() && !value.all(Char::isDigit)
    }
    val dateLabel = formatCompactNewsDate(news.publishedAt, includeTime = true)
        .ifBlank { stringResource(Res.string.ui_news_official_notice) }

    return when {
        news.type == LiquidStatusType.Warning -> NewsCategoryVisuals(
            label = sourceLabel ?: "Avviso",
            subtitle = dateLabel,
            icon = LiquidIcons.Notifications,
            color = glassColors.warning,
        )
        news.type == LiquidStatusType.Error -> NewsCategoryVisuals(
            label = sourceLabel ?: "Importante",
            subtitle = dateLabel,
            icon = LiquidIcons.Warning,
            color = glassColors.error,
        )
        news.type == LiquidStatusType.Success -> NewsCategoryVisuals(
            label = sourceLabel ?: stringResource(Res.string.ui_news_generic),
            subtitle = dateLabel,
            icon = LiquidIcons.Calendar,
            color = glassColors.success,
        )
        sourceLabel != null -> {
            val isDepartment = sourceLabel.contains("dip", ignoreCase = true)
            val isCourse = sourceLabel.contains("cors", ignoreCase = true)
            val isUniversity = sourceLabel.contains("ateneo", ignoreCase = true)
            NewsCategoryVisuals(
                label = sourceLabel,
                subtitle = dateLabel,
                icon = when {
                    isDepartment -> LiquidIcons.Star
                    isUniversity -> LiquidIcons.Home
                    else -> LiquidIcons.MenuBook
                },
                color = when {
                    isDepartment -> colorScheme.tertiary
                    isCourse -> colorScheme.secondary
                    else -> colorScheme.primary
                },
            )
        }
        else -> NewsCategoryVisuals(
            label = stringResource(Res.string.ui_news_generic),
            subtitle = dateLabel,
            icon = LiquidIcons.MenuBook,
            color = colorScheme.primary,
        )
    }
}

@Composable
fun UniNewsCard(
    news: NewsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fixedHeight: Dp? = null,
    categoryLabel: String? = null,
    homeCard: Boolean = false,
    featured: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val visuals = resolveNewsCategoryVisuals(news, categoryLabel)
    val showSummary = remember(news.title, news.description) {
        val desc = news.description.trim()
        val title = news.title.trim()
        desc.isNotBlank() && !desc.startsWith(title.take(24), ignoreCase = true)
    }

    if (homeCard) {
        val shortDate = remember(news.publishedAt) {
            formatCompactNewsDate(news.publishedAt, includeTime = false)
                .ifBlank { visuals.subtitle }
        }
        // Light, airy editorial card for the Home carousel (concise, zero duplicate-text clutter)
        LiquidCard(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .then(if (fixedHeight != null) Modifier.height(fixedHeight) else Modifier),
            shape = RoundedRectangle(22.dp),
            contentPadding = 16.dp,
            onClick = onClick,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 92.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(visuals.color),
                            )
                            Text(
                                text = visuals.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = visuals.color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = shortDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }

                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (showSummary) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = news.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        return
    }

    val cornerRadius = if (featured) 24.dp else 20.dp
    val sourceHost = remember(news.sourceUrl) {
        news.sourceUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { raw -> runCatching { Url(raw).host.removePrefix("www.") }.getOrNull() }
            ?.takeIf { it.isNotBlank() }
    }

    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .then(if (fixedHeight != null) Modifier.height(fixedHeight) else Modifier),
        shape = RoundedRectangle(cornerRadius),
        contentPadding = if (featured) 18.dp else 16.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NewsCategoryPill(
                        label = visuals.label,
                        icon = visuals.icon,
                        accentColor = visuals.color,
                    )

                    Text(
                        text = visuals.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = LiquidIcons.ChevronRight,
                    contentDescription = stringResource(Res.string.ui_details),
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = news.title,
                    style = if (featured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = if (featured) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (showSummary) {
                    Text(
                        text = news.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = if (featured) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (sourceHost != null) {
                Text(
                    text = sourceHost,
                    style = MaterialTheme.typography.labelSmall,
                    color = visuals.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun NewsCategoryPill(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(Capsule())
            .background(accentColor.copy(alpha = 0.14f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.22f),
                shape = Capsule(),
            )
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val isoDateTimeRegex = Regex("""^(\d{4})-(\d{2})-(\d{2})(?:[T\s](\d{2}:\d{2})(?::\d{2})?)?.*$""")

private fun formatCompactNewsDate(raw: String, includeTime: Boolean): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return ""
    val match = isoDateTimeRegex.matchEntire(trimmed) ?: return trimmed
    val year = match.groupValues[1]
    val month = match.groupValues[2]
    val day = match.groupValues[3]
    val time = match.groupValues[4]
    return if (includeTime && time.isNotBlank()) "$day/$month/$year • $time" else "$day/$month/$year"
}
