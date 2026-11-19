package com.anto426.uniapp.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.items.NewsCategoryPill
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.document.RichMarkdownDocument
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun NewsDetailScreen(
    title: String,
    description: String,
    fullContent: String,
    onBack: () -> Unit = {},
    category: String = "",
    publishedAt: String = "",
    sourceUrl: String? = null,
    onOpenSource: ((String) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val summaryHeading = stringResource(Res.string.ui_news_summary_heading)
    val articleHeading = stringResource(Res.string.ui_news_article_heading)

    val summary = remember(description) { description.trim() }
    val article = remember(fullContent) { fullContent.trim() }
    val distinctSummary = remember(summary, article) {
        summary.isNotBlank() && article.isNotBlank() && !article.startsWith(summary, ignoreCase = true)
    }
    val articleMarkdown = remember(summary, article, distinctSummary, summaryHeading, articleHeading) {
        if (distinctSummary) {
            "## $articleHeading\n\n$article"
        } else {
            formatNewsBody(description, fullContent, summaryHeading, articleHeading)
        }
    }
    val originalUrl = remember(sourceUrl) { sourceUrl.newsSourceUrlOrNull() }
    val originalHost = remember(originalUrl) {
        originalUrl?.let { runCatching { Url(it).host.removePrefix("www.") }.getOrNull() }
    }

    val categoryLabel = category.trim().takeIf { it.isNotBlank() && !it.all(Char::isDigit) }
    val categoryColor = when {
        categoryLabel?.contains("dip", ignoreCase = true) == true -> colorScheme.tertiary
        categoryLabel?.contains("cors", ignoreCase = true) == true -> colorScheme.secondary
        else -> colorScheme.primary
    }
    val categoryIcon = when {
        categoryLabel?.contains("dip", ignoreCase = true) == true -> LiquidIcons.Star
        categoryLabel?.contains("ateneo", ignoreCase = true) == true -> LiquidIcons.Home
        else -> LiquidIcons.Notifications
    }
    val dateLabel = publishedAt.trim().ifBlank { stringResource(Res.string.ui_news_official_notice) }

    UniScreenColumn {
        // 1. Editorial Hero Header Card
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(26.dp),
            contentPadding = 20.dp,
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
                        NewsCategoryPill(
                            label = categoryLabel ?: stringResource(Res.string.ui_news_generic),
                            icon = categoryIcon,
                            accentColor = categoryColor,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Calendar,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface,
                            fontSize = 21.sp,
                            lineHeight = 27.sp,
                        )
                    }

                    if (distinctSummary) {
                        val calloutShape = RoundedRectangle(16.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(calloutShape)
                                .border(
                                    width = 1.dp,
                                    color = colorScheme.onSurface.copy(alpha = 0.09f),
                                    shape = calloutShape,
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                                Text(
                                    text = summaryHeading.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor,
                                    letterSpacing = 0.7.sp,
                                    fontSize = 10.sp,
                                )
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurface.copy(alpha = 0.90f),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                )
                        }
                    }
                }
            }

        // 2. Article Body Card
        if (articleMarkdown.isNotBlank()) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(24.dp),
                contentPadding = 20.dp,
            ) {
                RichMarkdownDocument(
                    markdown = articleMarkdown,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 3. Original Source CTA
        if (originalUrl != null && onOpenSource != null) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(22.dp),
                contentPadding = 16.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!originalHost.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Info,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = originalHost,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    LiquidButton(
                        onClick = { onOpenSource(originalUrl) },
                        variant = LiquidButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_news_open_original),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Icon(
                                imageVector = LiquidIcons.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun formatNewsBody(
    description: String,
    fullContent: String,
    summaryHeading: String,
    articleHeading: String,
): String {
    val summary = description.trim()
    val article = fullContent.trim()
    val distinctSummary = summary.isNotBlank() && article.isNotBlank() &&
        !article.startsWith(summary, ignoreCase = true)
    return buildString {
        if (distinctSummary) {
            append("## ").append(summaryHeading).append("\n\n")
            append(summary).append("\n\n---\n\n")
        }
        if (article.isNotBlank() || summary.isNotBlank()) {
            append("## ").append(articleHeading).append("\n\n")
            append(if (article.isNotBlank()) article else summary)
        }
    }
}

internal fun String?.newsSourceUrlOrNull(): String? {
    val raw = this?.trim()?.takeIf(String::isNotBlank) ?: return null
    val candidate = if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw
    if (candidate.any(Char::isWhitespace) ||
        !(candidate.startsWith("https://", ignoreCase = true) || candidate.startsWith("http://", ignoreCase = true))
    ) return null
    val parsed = runCatching { Url(candidate) }.getOrNull() ?: return null
    return candidate.takeIf { parsed.host.isNotBlank() && parsed.protocol.name in setOf("http", "https") }
}
