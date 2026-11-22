package com.anto426.uniapp.data.contract

import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.uniapp.data.datasetCacheKey
import com.anto426.unisdk.model.UniversityNews
import io.ktor.http.Url
import kotlin.math.ceil
import kotlinx.datetime.plus

internal fun List<UniversityNews>.toNewsItems(): List<NewsItem> {
    val occurrences = mutableMapOf<String, Int>()
    return map { news ->
        val cleanTitle = NewsContentFormatter.formatTitle(news.title)
        val category = news.category?.trim().orEmpty()
        val publishedAt = news.publishedAt?.trim().orEmpty()
        val rawId = news.id?.trim()
        val identityParts = if (rawId.isNullOrBlank()) listOf(category.lowercase(), publishedAt, news.title)
        else listOf(category.lowercase(), rawId)
        val identity = identityParts.joinToString("|") { "${it.length}:$it" }
        val occurrence = occurrences.getOrElse(identity) { 0 }
        occurrences[identity] = occurrence + 1

        val formatted = NewsContentFormatter.formatArticle(
            title = cleanTitle,
            rawDescription = news.summary.orEmpty(),
            rawContent = news.content.orEmpty(),
            sourceUrl = news.sourceUrl,
        )
        val rawSummary = news.summary.orEmpty()
        val rawBody = news.content.orEmpty()
        val resolvedSourceUrl = NewsContentFormatter.resolveAssetUrl(news.sourceUrl)
            ?: news.sourceUrl?.trim()?.takeIf(String::isNotBlank)

        val item = NewsItem(
            title = cleanTitle,
            description = formatted.summaryText,
            fullContent = rawBody.ifBlank { rawSummary },
            type = news.category.toNewsType(),
            category = category,
            publishedAt = publishedAt,
            key = "${datasetCacheKey("news", identity)}#$occurrence",
            sourceUrl = resolvedSourceUrl,
        )
        item to publishedAt.newsDateSortKey()
    }.sortedWith(compareByDescending<Pair<NewsItem, Long>> { it.second }.thenBy { it.first.title })
        .map { it.first }
}

private fun String?.toNewsType(): LiquidStatusType =
    when {
        this?.contains("scad", ignoreCase = true) == true -> LiquidStatusType.Warning
        this?.contains("success", ignoreCase = true) == true -> LiquidStatusType.Success
        else -> LiquidStatusType.Info
    }

data class FormattedNewsAttachment(
    val label: String,
    val url: String,
    val isDocument: Boolean,
)

data class FormattedNewsContent(
    val summaryText: String,
    val distinctSummary: String?,
    val articleMarkdown: String,
    val attachments: List<FormattedNewsAttachment>,
    val readingMinutes: Int,
)

private val NewsDatePattern = Regex(
    """(?:(\d{4})-(\d{1,2})-(\d{1,2})|(\d{1,2})/(\d{1,2})/(\d{4}))(?:\s*[T\s•\-]\s*(\d{1,2}):(\d{2}))?""",
)

internal fun String.newsDateSortKey(): Long {
    val match = NewsDatePattern.find(this) ?: return Long.MIN_VALUE
    val (year, month, day) = if (match.groupValues[1].isNotEmpty()) {
        Triple(match.groupValues[1], match.groupValues[2], match.groupValues[3])
    } else {
        Triple(match.groupValues[6], match.groupValues[5], match.groupValues[4])
    }
    val dateKey = year.toLongOrNull()?.times(10_000L)
        ?.plus((month.toLongOrNull() ?: 0L) * 100L)
        ?.plus(day.toLongOrNull() ?: 0L) ?: return Long.MIN_VALUE
    val hour = match.groupValues[7].toLongOrNull()?.coerceIn(0L, 23L) ?: 0L
    val minute = match.groupValues[8].toLongOrNull()?.coerceIn(0L, 59L) ?: 0L
    return dateKey * 10_000L + hour * 100L + minute
}

internal fun String?.newsSourceUrlOrNull(): String? {
    val raw = this?.trim()?.takeIf(String::isNotBlank) ?: return null
    val candidate = if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw
    if (candidate.any(Char::isWhitespace)) return null
    val parsed = runCatching { Url(candidate) }.getOrNull() ?: return null
    val scheme = parsed.protocol.name.lowercase()
    if ((scheme != "https" && scheme != "http") || parsed.host.isBlank()) return null
    return candidate
}

object NewsContentFormatter {
    private const val DEFAULT_NEWS_ORIGIN = "https://apistrapi.unimol.it"
    private const val WORDS_PER_MINUTE = 190.0

    private val scriptOrStyleRegex =
        Regex("<(script|style)\\b[^>]*>[\\s\\S]*?</\\1>", setOf(RegexOption.IGNORE_CASE))
    private val imageTagRegex =
        Regex("<img\\b[^>]*>", setOf(RegexOption.IGNORE_CASE))
    private val anchorTagRegex =
        Regex("<a\\b([^>]*)>([\\s\\S]*?)</a>", setOf(RegexOption.IGNORE_CASE))
    private val hrefAttrRegex =
        Regex("""href\s*=\s*["']([^"']+)["']""", setOf(RegexOption.IGNORE_CASE))
    private val headingTagRegex =
        Regex("<h([1-6])\\b[^>]*>([\\s\\S]*?)</h\\1>", setOf(RegexOption.IGNORE_CASE))
    private val orderedListRegex =
        Regex("<ol\\b[^>]*>([\\s\\S]*?)</ol>", setOf(RegexOption.IGNORE_CASE))
    private val unorderedListRegex =
        Regex("<ul\\b[^>]*>([\\s\\S]*?)</ul>", setOf(RegexOption.IGNORE_CASE))
    private val listItemRegex =
        Regex("<li\\b[^>]*>([\\s\\S]*?)</li>", setOf(RegexOption.IGNORE_CASE))
    private val lineBreakRegex =
        Regex("<br\\s*/?>", setOf(RegexOption.IGNORE_CASE))
    private val blockCloseRegex =
        Regex("</(p|div|section|article|blockquote|tr|table)\\s*>", setOf(RegexOption.IGNORE_CASE))
    private val blockOpenRegex =
        Regex("<(p|div|section|article|blockquote|tr|table)\\b[^>]*>", setOf(RegexOption.IGNORE_CASE))
    private val strongTagRegex =
        Regex("<(strong|b)\\b[^>]*>([\\s\\S]*?)</\\1>", setOf(RegexOption.IGNORE_CASE))
    private val emphasisTagRegex =
        Regex("<(em|i)\\b[^>]*>([\\s\\S]*?)</\\1>", setOf(RegexOption.IGNORE_CASE))
    private val remainingHtmlTagRegex =
        Regex("</?[a-zA-Z][^>]*>")
    private val markdownImageRegex =
        Regex("""!\[[^\]]*]\([^)]*\)""")
    private val markdownLinkRegex =
        Regex("""\[([^\]]+)]\(([^)]+)\)""")
    private val rawUrlRegex =
        Regex("""https?://[^\s<>()"\]]+""", setOf(RegexOption.IGNORE_CASE))
    private val htmlEntityRegex =
        Regex("""&(#x[0-9a-fA-F]+|#\d+|[a-zA-Z]+);""")
    private val excessBlankLinesRegex =
        Regex("""\n{3,}""")
    private val horizontalSpacesRegex =
        Regex("""[ \t\u00A0]{2,}""")
    private val trailingHashClusterRegex =
        Regex("""(?:_[0-9a-fA-F]{6,16})+$""")
    private val sentenceSplitBoundaryRegex =
        Regex("""(?<=[.!?])\s+(?=[A-ZÀÈÉÌÒÙ"«“])""")

    private val documentExtensions = listOf(
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".ods", ".zip", ".rar",
    )
    private val imageExtensions = listOf(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp", ".avif",
    )

    fun formatTitle(rawTitle: String): String =
        toPlainText(rawTitle).lineSequence().joinToString(" ").trim()

    fun toPlainText(raw: String): String {
        if (raw.isBlank()) return ""
        val withoutNoise = raw
            .replace(scriptOrStyleRegex, " ")
            .replace(imageTagRegex, " ")
            .replace(markdownImageRegex, " ")
            .replace(anchorTagRegex) { match -> match.groupValues[2] }
            .replace(lineBreakRegex, "\n")
            .replace(blockCloseRegex, "\n")
            .replace(blockOpenRegex, " ")
            .replace(remainingHtmlTagRegex, " ")

        val decoded = decodeHtmlEntities(withoutNoise)
            .replace(markdownLinkRegex) { match -> match.groupValues[1] }

        return decoded
            .lineSequence()
            .map { line -> line.replace(horizontalSpacesRegex, " ").trim() }
            .filter(String::isNotBlank)
            .joinToString("\n")
    }

    fun formatMarkdownBody(
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

    fun formatArticle(
        title: String,
        rawDescription: String,
        rawContent: String,
        sourceUrl: String?,
        attachmentFallbackLabel: String? = null,
    ): FormattedNewsContent {
        val attachments = LinkedHashMap<String, FormattedNewsAttachment>()
        val cleanSummary = cleanBodyToMarkdown(rawDescription, sourceUrl, attachments, attachmentFallbackLabel)
        val cleanArticle = cleanBodyToMarkdown(rawContent, sourceUrl, attachments, attachmentFallbackLabel)

        val summaryPlain = toPlainText(rawDescription)
        val primaryMarkdown = cleanArticle.ifBlank { cleanSummary }
        val normalizedSummary = cleanSummary.trim()
        val normalizedPrimary = primaryMarkdown.trim()

        val distinctSummary = normalizedSummary.takeIf { summary ->
            summary.isNotBlank() &&
                normalizedPrimary.isNotBlank() &&
                !normalizedPrimary.equals(summary, ignoreCase = true) &&
                !normalizedPrimary.startsWith(summary.take(80), ignoreCase = true) &&
                !summary.equals(title.trim(), ignoreCase = true)
        }

        val wordCount = (distinctSummary.orEmpty() + " " + normalizedPrimary)
            .split(Regex("\\s+"))
            .count(String::isNotBlank)
        val readingMinutes = ceil(wordCount / WORDS_PER_MINUTE).toInt().coerceAtLeast(1)

        return FormattedNewsContent(
            summaryText = summaryPlain.ifBlank { toPlainText(rawContent) },
            distinctSummary = distinctSummary,
            articleMarkdown = normalizedPrimary,
            attachments = attachments.values.toList(),
            readingMinutes = readingMinutes,
        )
    }

    fun resolveAssetUrl(rawUrl: String?, baseOriginUrl: String? = null): String? {
        val trimmed = rawUrl?.trim()?.takeIf(String::isNotBlank) ?: return null
        val decoded = decodeHtmlEntities(trimmed).replace(" ", "%20")
        val normalizedCandidate = when {
            decoded.startsWith("https://", ignoreCase = true) -> decoded
            decoded.startsWith("http://", ignoreCase = true) ->
                if (decoded.contains("unimol.it", ignoreCase = true)) {
                    "https://" + decoded.removePrefix("http://").removePrefix("HTTP://")
                } else {
                    decoded
                }
            decoded.startsWith("//") -> "https:$decoded"
            decoded.startsWith("www.", ignoreCase = true) -> "https://$decoded"
            decoded.startsWith("/") -> "${extractOrigin(baseOriginUrl)}$decoded"
            decoded.startsWith("uploads/", ignoreCase = true) -> "${extractOrigin(baseOriginUrl)}/$decoded"
            else -> return null
        }
        return normalizedCandidate.newsSourceUrlOrNull()
    }

    private fun extractOrigin(sourceUrl: String?): String {
        val valid = sourceUrl.newsSourceUrlOrNull() ?: return DEFAULT_NEWS_ORIGIN
        val parsed = runCatching { Url(valid) }.getOrNull() ?: return DEFAULT_NEWS_ORIGIN
        return "${parsed.protocol.name.lowercase()}://${parsed.host}"
    }

    private fun cleanBodyToMarkdown(
        raw: String,
        sourceUrl: String?,
        attachments: LinkedHashMap<String, FormattedNewsAttachment>,
        attachmentFallbackLabel: String?,
    ): String {
        if (raw.isBlank()) return ""

        var text = raw
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace(scriptOrStyleRegex, "")
            .replace(imageTagRegex, "")
            .replace(markdownImageRegex, "")

        text = text.replace(anchorTagRegex) { match ->
            val attrs = match.groupValues[1]
            val innerHtml = match.groupValues[2]
            val rawHref = hrefAttrRegex.find(attrs)?.groupValues?.getOrNull(1)
            val resolvedHref = resolveAssetUrl(rawHref, sourceUrl)
            val label = toPlainText(innerHtml).lineSequence().joinToString(" ").trim()

            if (resolvedHref == null) {
                label
            } else if (isImageUrl(resolvedHref)) {
                ""
            } else {
                val isDoc = isDocumentUrl(resolvedHref)
                val attachmentLabel = buildAttachmentLabel(label, resolvedHref, attachmentFallbackLabel)
                attachments.getOrPut(resolvedHref) {
                    FormattedNewsAttachment(
                        label = attachmentLabel,
                        url = resolvedHref,
                        isDocument = isDoc,
                    )
                }
                if (label.isBlank() || label.equals(rawHref?.trim(), ignoreCase = true) || looksLikeRawFilename(label)) {
                    attachmentLabel
                } else {
                    "[$label]($resolvedHref)"
                }
            }
        }

        text = text.replace(markdownLinkRegex) { match ->
            val label = decodeHtmlEntities(match.groupValues[1]).trim()
            val target = match.groupValues[2].trim()
            val resolved = resolveAssetUrl(target, sourceUrl)
            if (resolved == null) {
                label
            } else if (isImageUrl(resolved)) {
                ""
            } else {
                val isDoc = isDocumentUrl(resolved)
                val attachmentLabel = buildAttachmentLabel(label, resolved, attachmentFallbackLabel)
                attachments.getOrPut(resolved) {
                    FormattedNewsAttachment(
                        label = attachmentLabel,
                        url = resolved,
                        isDocument = isDoc,
                    )
                }
                if (label.isBlank() || looksLikeRawFilename(label)) {
                    attachmentLabel
                } else {
                    "[$label]($resolved)"
                }
            }
        }

        text = text.replace(headingTagRegex) { match ->
            val level = (match.groupValues[1].toIntOrNull() ?: 2).coerceIn(2, 4)
            val headingText = toPlainText(match.groupValues[2]).lineSequence().joinToString(" ").trim()
            if (headingText.isBlank()) "" else "\n\n${"#".repeat(level)} $headingText\n\n"
        }

        text = text.replace(orderedListRegex) { match ->
            val items = listItemRegex.findAll(match.groupValues[1])
                .mapIndexedNotNull { index, item ->
                    val itemText = cleanInlineHtml(item.groupValues[1])
                    itemText.takeIf(String::isNotBlank)?.let { "${index + 1}. $it" }
                }
                .toList()
            if (items.isEmpty()) "" else "\n\n" + items.joinToString("\n") + "\n\n"
        }

        text = text.replace(unorderedListRegex) { match ->
            val items = listItemRegex.findAll(match.groupValues[1])
                .mapNotNull { item ->
                    val itemText = cleanInlineHtml(item.groupValues[1])
                    itemText.takeIf(String::isNotBlank)?.let { "- $it" }
                }
                .toList()
            if (items.isEmpty()) "" else "\n\n" + items.joinToString("\n") + "\n\n"
        }

        text = text.replace(listItemRegex) { match ->
            val itemText = cleanInlineHtml(match.groupValues[1])
            if (itemText.isBlank()) "" else "\n- $itemText\n"
        }

        text = text
            .replace(strongTagRegex) { match ->
                val inner = cleanInlineHtml(match.groupValues[2])
                if (inner.isBlank()) "" else "**$inner**"
            }
            .replace(emphasisTagRegex) { match ->
                val inner = cleanInlineHtml(match.groupValues[2])
                if (inner.isBlank()) "" else "*$inner*"
            }
            .replace(lineBreakRegex, "\n")
            .replace(blockCloseRegex, "\n\n")
            .replace(blockOpenRegex, "\n\n")
            .replace(remainingHtmlTagRegex, "")

        text = decodeHtmlEntities(text)

        rawUrlRegex.findAll(text).forEach { match ->
            val start = match.range.first
            if (start >= 2 && text[start - 1] == '(' && text[start - 2] == ']') return@forEach
            val resolved = resolveAssetUrl(match.value, sourceUrl) ?: return@forEach
            if (!isImageUrl(resolved)) {
                attachments.getOrPut(resolved) {
                    FormattedNewsAttachment(
                        label = buildAttachmentLabel("", resolved, attachmentFallbackLabel),
                        url = resolved,
                        isDocument = isDocumentUrl(resolved),
                    )
                }
            }
        }

        val normalizedLines = text
            .lineSequence()
            .map { line -> line.replace(horizontalSpacesRegex, " ").trim() }
            .toList()

        val blocks = mutableListOf<String>()
        val paragraphBuffer = mutableListOf<String>()

        fun flushParagraph() {
            if (paragraphBuffer.isEmpty()) return
            val joined = paragraphBuffer.joinToString(" ").trim()
            paragraphBuffer.clear()
            if (joined.isNotBlank()) {
                blocks += splitProseIntoReadableParagraphs(joined)
            }
        }

        for (line in normalizedLines) {
            when {
                line.isBlank() -> flushParagraph()
                isStructuralMarkdownLine(line) -> {
                    flushParagraph()
                    blocks += line
                }
                else -> paragraphBuffer += line
            }
        }
        flushParagraph()

        return blocks
            .joinToString("\n\n")
            .replace(excessBlankLinesRegex, "\n\n")
            .trim()
    }

    private fun isStructuralMarkdownLine(line: String): Boolean =
        line.startsWith("#") ||
            line.startsWith("- ") ||
            line.startsWith("* ") ||
            line.startsWith("> ") ||
            line.startsWith("```") ||
            line.startsWith("|") ||
            line == "---" ||
            Regex("^\\d+\\.\\s+.*").matches(line)

    internal fun splitProseIntoReadableParagraphs(
        paragraph: String,
        minLength: Int = 220,
    ): List<String> {
        if (paragraph.length < minLength) return listOf(paragraph)
        val sentences = sentenceSplitBoundaryRegex.split(paragraph)
            .map(String::trim)
            .filter(String::isNotBlank)
        if (sentences.size < 3) return listOf(paragraph)

        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        var sentenceCount = 0

        for (sentence in sentences) {
            if (current.isNotEmpty()) current.append(' ')
            current.append(sentence)
            sentenceCount++
            if (current.length >= 150 && sentenceCount >= 2) {
                chunks += current.toString().trim()
                current.clear()
                sentenceCount = 0
            }
        }
        if (current.isNotEmpty()) {
            chunks += current.toString().trim()
        }
        return chunks.ifEmpty { listOf(paragraph) }
    }

    private fun cleanInlineHtml(raw: String): String =
        decodeHtmlEntities(
            raw.replace(lineBreakRegex, " ")
                .replace(remainingHtmlTagRegex, " "),
        ).replace(horizontalSpacesRegex, " ").trim()

    private fun isImageUrl(url: String): Boolean {
        val cleanPath = url.substringBefore('?').substringBefore('#').lowercase()
        return imageExtensions.any(cleanPath::endsWith)
    }

    private fun isDocumentUrl(url: String): Boolean {
        val cleanPath = url.substringBefore('?').substringBefore('#').lowercase()
        return documentExtensions.any(cleanPath::endsWith) || cleanPath.contains("/uploads/")
    }

    private fun looksLikeRawFilename(label: String): Boolean {
        val trimmed = label.trim()
        val lower = trimmed.lowercase()
        return documentExtensions.any(lower::endsWith) ||
            imageExtensions.any(lower::endsWith) ||
            trimmed.contains('_') && trailingHashClusterRegex.containsMatchIn(trimmed.substringBeforeLast('.'))
    }

    private fun buildAttachmentLabel(
        rawLabel: String,
        url: String,
        attachmentFallbackLabel: String?,
    ): String {
        val cleanRaw = rawLabel.trim()
        if (cleanRaw.isNotBlank() &&
            !cleanRaw.startsWith("http://", ignoreCase = true) &&
            !cleanRaw.startsWith("https://", ignoreCase = true) &&
            !looksLikeRawFilename(cleanRaw)
        ) {
            return cleanRaw
        }

        val segment = url.substringBefore('?').substringBefore('#')
            .substringAfterLast('/')
            .trim()
        if (segment.isBlank()) {
            return runCatching { Url(url).host.removePrefix("www.") }.getOrNull()
                ?.takeIf(String::isNotBlank)
                ?: attachmentFallbackLabel.orEmpty()
        }

        val ext = segment.substringAfterLast('.', missingDelimiterValue = "").uppercase()
        val baseName = segment.substringBeforeLast('.', missingDelimiterValue = segment)
            .replace(trailingHashClusterRegex, "")
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(horizontalSpacesRegex, " ")
            .trim()

        if (baseName.isBlank()) {
            return if (ext.isNotBlank()) {
                val baseLabel = attachmentFallbackLabel?.takeIf(String::isNotBlank)
                if (baseLabel != null) "$baseLabel ($ext)" else ext
            } else {
                attachmentFallbackLabel.orEmpty()
            }
        }
        return if (ext.isNotBlank() && ext.length <= 5) "$baseName ($ext)" else baseName
    }

    internal fun decodeHtmlEntities(input: String): String {
        if (!input.contains('&')) return input
        val namedEntities = mapOf(
            "nbsp" to " ",
            "amp" to "&",
            "lt" to "<",
            "gt" to ">",
            "quot" to "\"",
            "apos" to "'",
            "rsquo" to "’",
            "lsquo" to "‘",
            "rdquo" to "”",
            "ldquo" to "“",
            "ndash" to "–",
            "mdash" to "—",
            "hellip" to "…",
            "bull" to "•",
            "middot" to "·",
            "laquo" to "«",
            "raquo" to "»",
            "euro" to "€",
            "deg" to "°",
            "ordm" to "º",
            "ordf" to "ª",
            "agrave" to "à",
            "egrave" to "è",
            "eacute" to "é",
            "igrave" to "ì",
            "ograve" to "ò",
            "ugrave" to "ù",
            "Agrave" to "À",
            "Egrave" to "È",
            "Eacute" to "É",
            "Igrave" to "Ì",
            "Ograve" to "Ò",
            "Ugrave" to "Ù",
        )
        return htmlEntityRegex.replace(input) { match ->
            val entity = match.groupValues[1]
            when {
                entity.startsWith("#x", ignoreCase = true) ->
                    entity.drop(2).toIntOrNull(16)?.takeIf { it in 1..0x10FFFF }?.let { Char(it).toString() }
                        ?: match.value
                entity.startsWith("#") ->
                    entity.drop(1).toIntOrNull()?.takeIf { it in 1..0x10FFFF }?.let { Char(it).toString() }
                        ?: match.value
                else -> namedEntities[entity] ?: namedEntities[entity.lowercase()] ?: match.value
            }
        }
    }
}
