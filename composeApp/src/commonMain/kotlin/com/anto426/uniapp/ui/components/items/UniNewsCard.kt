package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.NewsContentFormatter
import com.anto426.uniapp.data.contract.NewsItem
import com.kyant.shapes.RoundedRectangle
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

internal val ItalianNewsLocaleList = LocaleList(Locale("it"))

internal data class NewsCategoryVisuals(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
internal fun resolveNewsCategoryVisuals(
    type: LiquidStatusType,
    category: String,
    publishedAt: String,
    categoryLabel: String? = null,
): NewsCategoryVisuals {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val sourceLabel = categoryLabel ?: category.trim().takeIf { value ->
        value.isNotBlank() && !value.all(Char::isDigit)
    }
    val dateLabel = formatCompactNewsDate(publishedAt, includeTime = true)
        .ifBlank { stringResource(Res.string.ui_news_official_notice) }

    return when {
        type == LiquidStatusType.Warning -> NewsCategoryVisuals(
            label = sourceLabel ?: "Avviso",
            subtitle = dateLabel,
            icon = LiquidIcons.Notifications,
            color = glassColors.warning,
        )
        type == LiquidStatusType.Error -> NewsCategoryVisuals(
            label = sourceLabel ?: "Importante",
            subtitle = dateLabel,
            icon = LiquidIcons.Warning,
            color = glassColors.error,
        )
        type == LiquidStatusType.Success -> NewsCategoryVisuals(
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
    val visuals = resolveNewsCategoryVisuals(
        type = news.type,
        category = news.category,
        publishedAt = news.publishedAt,
        categoryLabel = categoryLabel,
    )
    val displayTitle = remember(news.title) { NewsContentFormatter.formatTitle(news.title) }
    val plainSummary = remember(displayTitle, news.description, news.fullContent) {
        NewsContentFormatter.toPlainText(news.description)
            .takeIf { desc ->
                desc.isNotBlank() && !desc.startsWith(displayTitle.take(24), ignoreCase = true)
            }
            .orEmpty()
    }
    val showSummary = plainSummary.isNotBlank()
    val sourceHost = remember(news.sourceUrl) {
        news.sourceUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { raw -> runCatching { Url(raw).host.removePrefix("www.") }.getOrNull() }
            ?.takeIf { it.isNotBlank() }
    }

    // 1. Apple News Editorial Home Carousel Card — uniform height, clear footer affordance
    if (homeCard) {
        val shortDate = remember(news.publishedAt) {
            formatCompactNewsDate(news.publishedAt, includeTime = false)
                .ifBlank { visuals.subtitle }
        }
        LiquidCard(
            modifier = modifier
                .fillMaxWidth()
                .height(fixedHeight ?: 168.dp)
                .graphicsLayer(clip = false)
                .semantics(mergeDescendants = true) {},
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            onClick = onClick,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 140.dp),
                verticalArrangement = Arrangement.SpaceBetween,
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
                        LiquidBadge(
                            text = visuals.label,
                            containerColor = visuals.color.copy(alpha = 0.14f),
                            contentColor = visuals.color,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = shortDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                localeList = ItalianNewsLocaleList,
                                lineBreak = LineBreak.Simple,
                                hyphens = Hyphens.None,
                            ),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            color = colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (showSummary) {
                            Text(
                                text = plainSummary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    localeList = ItalianNewsLocaleList,
                                    lineBreak = LineBreak.Simple,
                                    hyphens = Hyphens.None,
                                ),
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = sourceHost ?: stringResource(Res.string.ui_news_read_more),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = visuals.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = stringResource(Res.string.ui_details),
                        tint = visuals.color,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        return
    }

    // 2. Featured Hero Editorial News Card (NewsScreen lead)
    if (featured) {
        LiquidCard(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false)
                .semantics(mergeDescendants = true) {},
            shape = RoundedRectangle(28.dp),
            contentPadding = PaddingValues(18.dp),
            onClick = onClick,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = visuals.icon,
                            contentDescription = null,
                            tint = visuals.color,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 36.dp,
                                iconSize = 18.dp,
                                containerColor = visuals.color.copy(alpha = 0.14f),
                                shape = RoundedRectangle(11.dp),
                            ),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = visuals.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = visuals.color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = visuals.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    LiquidBadge(
                        text = stringResource(Res.string.ui_news_featured_badge),
                        containerColor = visuals.color.copy(alpha = 0.14f),
                        contentColor = visuals.color,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            localeList = ItalianNewsLocaleList,
                            lineBreak = LineBreak.Simple,
                            hyphens = Hyphens.None,
                        ),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (showSummary) {
                        Text(
                            text = plainSummary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                localeList = ItalianNewsLocaleList,
                                lineBreak = LineBreak.Simple,
                                hyphens = Hyphens.None,
                            ),
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = sourceHost ?: stringResource(Res.string.ui_news_read_more),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = visuals.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = stringResource(Res.string.ui_details),
                        tint = visuals.color,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        return
    }

    // 3. Standard News List Card (clean single-line metadata header, crisp hierarchy)
    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false)
            .semantics(mergeDescendants = true) {}
            .then(if (fixedHeight != null) Modifier.height(fixedHeight) else Modifier),
        shape = RoundedRectangle(24.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 108.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidBadge(
                        text = visuals.label,
                        containerColor = visuals.color.copy(alpha = 0.14f),
                        contentColor = visuals.color,
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
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.size(16.dp),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        localeList = ItalianNewsLocaleList,
                        lineBreak = LineBreak.Simple,
                        hyphens = Hyphens.None,
                    ),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (showSummary) {
                    Text(
                        text = plainSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            localeList = ItalianNewsLocaleList,
                            lineBreak = LineBreak.Simple,
                            hyphens = Hyphens.None,
                        ),
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (sourceHost != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(visuals.color),
                    )
                    Text(
                        text = sourceHost,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = visuals.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private val isoDateTimeRegex = Regex("""^(\d{4})-(\d{2})-(\d{2})(?:[T\s](\d{2}:\d{2})(?::\d{2})?)?.*$""")

internal fun formatCompactNewsDate(raw: String, includeTime: Boolean): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return ""
    val match = isoDateTimeRegex.matchEntire(trimmed) ?: return trimmed
    val year = match.groupValues[1]
    val month = match.groupValues[2]
    val day = match.groupValues[3]
    val time = match.groupValues[4]
    return if (includeTime && time.isNotBlank()) "$day/$month/$year • $time" else "$day/$month/$year"
}
