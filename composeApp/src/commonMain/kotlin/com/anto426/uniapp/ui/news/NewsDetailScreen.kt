package com.anto426.uniapp.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.navigation.ui.NewsDetailActionHost
import com.anto426.uniapp.navigation.ui.NewsDetailActions
import com.anto426.uniapp.ui.components.items.resolveNewsCategoryVisuals
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.document.RichMarkdownDocument
import com.anto426.uniapp.ui.share.rememberTextSharer
import com.kyant.shapes.RoundedRectangle
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.Res
import uniapp.composeapp.generated.resources.ui_news_attachment_document

@Composable
internal fun NewsDetailScreen(
    title: String,
    description: String,
    fullContent: String,
    onBack: () -> Unit = {},
    category: String = "",
    publishedAt: String = "",
    sourceUrl: String? = null,
    type: LiquidStatusType = LiquidStatusType.Info,
    onOpenSource: ((String) -> Unit)? = null,
    newsDetailActionHost: NewsDetailActionHost? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    val attachmentFallbackLabel = stringResource(Res.string.ui_news_attachment_document)
    val formatted = remember(title, description, fullContent, sourceUrl, attachmentFallbackLabel) {
        NewsContentFormatter.formatArticle(
            title = title,
            rawDescription = description,
            rawContent = fullContent,
            sourceUrl = sourceUrl,
            attachmentFallbackLabel = attachmentFallbackLabel,
        )
    }

    val originalUrl = remember(sourceUrl) { sourceUrl.newsSourceUrlOrNull() }
    val visuals = resolveNewsCategoryVisuals(
        type = type,
        category = category,
        publishedAt = publishedAt,
    )

    val shareText = rememberTextSharer()
    val onShare = remember(title, description, originalUrl, shareText) {
        {
            val payload = buildString {
                if (title.isNotBlank()) append(NewsContentFormatter.formatTitle(title)).append("\n\n")
                if (description.isNotBlank()) append(description.trim()).append("\n\n")
                if (!originalUrl.isNullOrBlank()) append(originalUrl)
            }.trim()
            shareText(payload)
        }
    }

    val currentOnOpenSource = onOpenSource
    val onOpenOriginal = remember(originalUrl, currentOnOpenSource) {
        if (originalUrl != null && currentOnOpenSource != null) {
            { currentOnOpenSource(originalUrl) }
        } else null
    }

    DisposableEffect(newsDetailActionHost, onOpenOriginal, onShare) {
        if (newsDetailActionHost != null) {
            val actions = NewsDetailActions(
                onOpenOriginal = onOpenOriginal,
                onShare = onShare,
            )
            newsDetailActionHost.register(actions)
            onDispose { newsDetailActionHost.unregister(actions) }
        } else {
            onDispose {}
        }
    }

    UniScreenColumn {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Testata essenziale e titolo di forte rilievo tipografico (senza contenitore card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = visuals.label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = visuals.color,
                        maxLines = 1,
                    )
                    if (visuals.subtitle.isNotBlank()) {
                        Text(
                            text = "• ${visuals.subtitle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (title.isNotBlank()) {
                    Text(
                        text = NewsContentFormatter.formatTitle(title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        lineHeight = 33.sp,
                        color = colorScheme.onSurface,
                    )
                }
            }

            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

            // 2. Corpo dell'articolo formattato direttamente su pagina
            val bodyMarkdown = formatted.articleMarkdown.ifBlank { formatted.summaryText }
            if (bodyMarkdown.isNotBlank()) {
                RichMarkdownDocument(
                    markdown = bodyMarkdown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                )
            }

            // 3. Allegati / documenti estratti
            if (formatted.attachments.isNotEmpty() && onOpenSource != null) {
                Spacer(Modifier.height(2.dp))
                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    formatted.attachments.forEach { attachment ->
                        val host = remember(attachment.url) {
                            runCatching { Url(attachment.url).host.removePrefix("www.") }.getOrNull().orEmpty()
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedRectangle(14.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable { onOpenSource(attachment.url) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Assignment,
                                contentDescription = null,
                                tint = visuals.color,
                                modifier = Modifier.size(18.dp),
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                Text(
                                    text = attachment.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (host.isNotBlank()) {
                                    Text(
                                        text = host,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Icon(
                                imageVector = LiquidIcons.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
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
