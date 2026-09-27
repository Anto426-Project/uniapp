package com.anto426.uniapp.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.document.UniDocumentScreen
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
    val summaryHeading = stringResource(Res.string.ui_news_summary_heading)
    val articleHeading = stringResource(Res.string.ui_news_article_heading)
    val body = remember(description, fullContent, summaryHeading, articleHeading) {
        formatNewsBody(description, fullContent, summaryHeading, articleHeading)
    }
    val originalUrl = remember(sourceUrl) { sourceUrl.newsSourceUrlOrNull() }

    UniDocumentScreen(
        title = title.takeIf { it.isNotBlank() },
        subtitle = publishedAt.takeIf(String::isNotBlank),
        headerBadge = category.takeIf(String::isNotBlank),
        headerIcon = LiquidIcons.Notifications,
        content = body,
        onBack = onBack,
        bottomContent = if (originalUrl != null && onOpenSource != null) {
            {
                LiquidButton(
                    text = stringResource(Res.string.ui_news_open_original),
                    onClick = { onOpenSource(originalUrl) },
                    variant = LiquidButtonVariant.Glass,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else null,
    )
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
