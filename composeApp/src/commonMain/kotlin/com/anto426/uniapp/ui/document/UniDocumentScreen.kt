package com.anto426.uniapp.ui.document

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.updates.presentation.ChangelogUiState

/**
 * Single unified document screen for textual reading experiences:
 * - News Detail
 * - Changelog
 * - Privacy Policy
 * - Terms of Service
 * - Cookie Policy
 *
 * Supports Markdown headers (#, ##, ###), dividers (---), bullet lists (- / * / •),
 * bold (**text**), italics (*text*), code (`code`), and clickable links ([text](url) and https://...).
 */
@Composable
fun UniDocumentScreen(
    content: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    headerIcon: ImageVector? = null,
    headerBadge: String? = null,
    onBack: (() -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn(modifier = modifier) {
        if (!title.isNullOrBlank()) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (headerIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    color = colorScheme.primary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (!headerBadge.isNullOrBlank()) {
                            LiquidBadge(
                                text = headerBadge,
                                containerColor = colorScheme.primary.copy(alpha = 0.12f),
                                contentColor = colorScheme.primary,
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            fontSize = 17.sp,
                            lineHeight = 23.sp,
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp,
                            )
                        }
                    }
                }
            }
        }

        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 20.dp,
        ) {
            RichMarkdownDocument(
                markdown = content,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (bottomContent != null) {
            Spacer(Modifier.height(12.dp))
            bottomContent()
        }
    }
}

@Composable
fun RichMarkdownDocument(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val linkColor = colorScheme.primary

    val lines = remember(markdown) { markdown.lines() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                Spacer(Modifier.height(4.dp))
                continue
            }

            when {
                trimmed.startsWith("### ") -> {
                    val headingText = trimmed.removePrefix("### ")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = parseInlineMarkdown(headingText, linkColor),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        fontSize = 17.sp,
                        lineHeight = 23.sp,
                    )
                }

                trimmed.startsWith("## ") -> {
                    val headingText = trimmed.removePrefix("## ")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = parseInlineMarkdown(headingText, linkColor),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 19.5.sp,
                        lineHeight = 26.sp,
                    )
                }

                trimmed.startsWith("# ") -> {
                    val headingText = trimmed.removePrefix("# ")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = parseInlineMarkdown(headingText, linkColor),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                    )
                }

                trimmed == "---" || trimmed == "***" -> {
                    LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                }

                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    val bulletContent = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = parseInlineMarkdown(bulletContent, linkColor),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp,
                            lineHeight = 25.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.92f),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                trimmed.matches(Regex("""^\d+\.\s+.*""")) -> {
                    val dotIndex = trimmed.indexOf('.')
                    val number = trimmed.substring(0, dotIndex + 1)
                    val itemContent = trimmed.substring(dotIndex + 1).trim()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            fontSize = 16.sp,
                        )
                        Text(
                            text = parseInlineMarkdown(itemContent, linkColor),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp,
                            lineHeight = 25.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.92f),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                else -> {
                    Text(
                        text = parseInlineMarkdown(trimmed, linkColor),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.5.sp,
                        lineHeight = 26.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.92f),
                    )
                }
            }
        }
    }
}

private val inlineTokenRegex = Regex(
    """(\[(?<label>[^\]]+)\]\((?<url>https?://[^\s)]+)\))|(\*\*(?<bold>[^*]+)\*\*)|(`(?<code>[^`]+)`)|(\*(?<italic>[^*]+)\*)|(?<rawUrl>https?://[^\s<>"{}|\\^`]+)"""
)

fun parseInlineMarkdown(text: String, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        inlineTokenRegex.findAll(text).forEach { matchResult ->
            val range = matchResult.range
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            val linkLabel = matchResult.groups["label"]?.value
            val linkUrl = matchResult.groups["url"]?.value
            val boldText = matchResult.groups["bold"]?.value
            val italicText = matchResult.groups["italic"]?.value
            val codeText = matchResult.groups["code"]?.value
            val rawUrl = matchResult.groups["rawUrl"]?.value

            when {
                linkLabel != null && linkUrl != null -> {
                    withLink(
                        LinkAnnotation.Url(
                            url = linkUrl,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            ),
                        ),
                    ) {
                        append(linkLabel)
                    }
                }

                rawUrl != null -> {
                    withLink(
                        LinkAnnotation.Url(
                            url = rawUrl,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            ),
                        ),
                    ) {
                        append(rawUrl)
                    }
                }

                boldText != null -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldText)
                    }
                }

                italicText != null -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicText)
                    }
                }

                codeText != null -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = linkColor.copy(alpha = 0.08f),
                        ),
                    ) {
                        append(" $codeText ")
                    }
                }

                else -> {
                    append(matchResult.value)
                }
            }

            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

/**
 * Extension to convert a list of LegalSectionData into a clean Markdown document string.
 */
@Composable
fun List<LegalSectionData>.toMarkdownString(): String {
    val sb = StringBuilder()
    for (section in this) {
        val sectionTitle = section.title
        val sectionContent = section.content
        if (sb.isNotEmpty()) {
            sb.append("\n\n")
        }
        sb.append("## ").append(sectionTitle).append("\n\n").append(sectionContent)
    }
    return sb.toString()
}

/**
 * Extension to convert ChangelogUiState into a clean Markdown document string.
 */
@Composable
fun ChangelogUiState.toMarkdownString(): String {
    val sb = StringBuilder()
    for (i in versions.indices) {
        val version = versions[i]
        sb.append("## Versione ").append(version.version)
        val date = version.date
        if (date.isNotBlank()) {
            sb.append(" • ").append(date)
        }
        sb.append("\n\n")
        for (item in version.items) {
            val itemTitle = item.title
            val itemDescription = item.description
            sb.append("- **")
                .append(itemTitle)
                .append("**: ")
                .append(itemDescription)
                .append("\n")
        }
        if (i < versions.lastIndex) {
            sb.append("\n---\n\n")
        }
    }
    return sb.toString()
}


