package com.anto426.uniapp.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.items.resolveNewsCategoryVisuals
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.document.RichMarkdownDocument
import com.kyant.shapes.RoundedRectangle
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsDetailScreen(
    title: String,
    description: String,
    fullContent: String,
    onBack: () -> Unit = {},
    category: String = "",
    publishedAt: String = "",
    sourceUrl: String? = null,
    type: LiquidStatusType = LiquidStatusType.Info,
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
    val bodyMarkdown = remember(summary, article, distinctSummary, articleHeading) {
        when {
            distinctSummary -> "## $articleHeading\n\n$article"
            article.isNotBlank() -> "## $articleHeading\n\n$article"
            summary.isNotBlank() -> "## $articleHeading\n\n$summary"
            else -> ""
        }
    }
    val originalUrl = remember(sourceUrl) { sourceUrl.newsSourceUrlOrNull() }
    val originalHost = remember(originalUrl) {
        originalUrl?.let { runCatching { Url(it).host.removePrefix("www.") }.getOrNull() }
    }
    val visuals = resolveNewsCategoryVisuals(
        type = type,
        category = category,
        publishedAt = publishedAt,
    )

    UniScreenColumn {
        // 1. Editorial Hero Header Card
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
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

                    if (!originalHost.isNullOrBlank()) {
                        LiquidBadge(
                            text = originalHost,
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            contentColor = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                    )
                }

                if (distinctSummary) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = summaryHeading.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = visuals.color,
                        )
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // 2. Article Body Card
        if (bodyMarkdown.isNotBlank()) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(22.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                RichMarkdownDocument(
                    markdown = bodyMarkdown,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 3. Original Source CTA
        if (originalUrl != null && onOpenSource != null) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedRectangle(20.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Info,
                            contentDescription = null,
                            tint = visuals.color,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 36.dp,
                                iconSize = 18.dp,
                                containerColor = visuals.color.copy(alpha = 0.12f),
                                shape = RoundedRectangle(10.dp),
                            ),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.ui_news_source_official_label),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = originalHost ?: originalUrl,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    LiquidButton(
                        text = stringResource(Res.string.ui_news_open_original),
                        onClick = { onOpenSource(originalUrl) },
                        variant = LiquidButtonVariant.Primary,
                        size = LiquidButtonSize.Medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
