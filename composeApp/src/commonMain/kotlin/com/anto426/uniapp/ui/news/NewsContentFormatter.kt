package com.anto426.uniapp.ui.news

import io.ktor.http.Url
import kotlin.math.ceil

internal data class FormattedNewsAttachment(
    val label: String,
    val url: String,
    val isDocument: Boolean,
)

internal data class FormattedNewsContent(
    val summaryText: String,
    val distinctSummary: String?,
    val articleMarkdown: String,
    val attachments: List<FormattedNewsAttachment>,
    val readingMinutes: Int,
)

/**
 * Normalizza e formatta notizie provenienti da portali UniMol / Strapi (HTML, Markdown o testo piatto).
 * Gestisce entità HTML italiane e numeriche, URL relativi `/uploads/`,
 * allegati PDF/documenti e spezzamento leggibile dei muri di testo piatti.
 */
internal object NewsContentFormatter {
    private const val UPLOADS_BASE_URL = "https://apistrapi.unimol.it/uploads/"
    private const val SITE_BASE_URL = "https://www3.unimol.it/"

    private val scriptOrStyleRegex = Regex("""<(script|style)\b[^>]*>[\s\S]*?</\1>""", RegexOption.IGNORE_CASE)
    private val imgTagRegex = Regex("""<img\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val anchorTagRegex = Regex("""<a\b[^>]*\bhref\s*=\s*["']([^"']+)["'][^>]*>([\s\S]*?)</a>""", RegexOption.IGNORE_CASE)
    private val headingTagRegex = Regex("""<h([1-4])\b[^>]*>([\s\S]*?)</h\1>""", RegexOption.IGNORE_CASE)
    private val strongTagRegex = Regex("""<(strong|b)\b[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
    private val emTagRegex = Regex("""<(em|i)\b[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
    private val blockquoteTagRegex = Regex("""<blockquote\b[^>]*>([\s\S]*?)</blockquote>""", RegexOption.IGNORE_CASE)
    private val hrTagRegex = Regex("""<hr\b[^>]*/?>""", RegexOption.IGNORE_CASE)
    private val liOpenRegex = Regex("""<li\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val liCloseRegex = Regex("""</li>""", RegexOption.IGNORE_CASE)
    private val brTagRegex = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
    private val blockBreakTagRegex = Regex("""</?(?:p|div|section|article|ul|ol|tr|table|thead|tbody|figure|figcaption)\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val cellTagRegex = Regex("""</?(?:td|th)\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val anyHtmlTagRegex = Regex("""<[^>]+>""")
    private val htmlEntityRegex = Regex("""&(#x[0-9a-fA-F]+|#\d+|[a-zA-Z]+);""")

    private val mdImageRegex = Regex("""!\[([^\]]*)]\(([^)\n]+)\)""")
    private val mdLinkRegex = Regex("""(?<!!)\[([^\]]+)]\(([^)\s]+)\)""")
    private val bareUrlRegex = Regex("""https?://[^\s<>"{}|\\^`\[\]()]+""")
    private val relativeUploadRegex = Regex("""(?<![a-zA-Z0-9_])(/uploads/[^\s"'<>)]+)""", RegexOption.IGNORE_CASE)
    private val inlineBulletRegex = Regex("""\s+[•▪◦]\s+""")
    private val sentenceBoundaryRegex = Regex("""(?<=[.!?])\s+(?=[A-ZÀÈÉÌÒÙ"«(])""")
    private val trailingHashSuffixRegex = Regex("""_[0-9a-f]{8,}$""", RegexOption.IGNORE_CASE)

    private val namedEntities = mapOf(
        "nbsp" to " ",
        "amp" to "&",
        "quot" to "\"",
        "apos" to "'",
        "lt" to "<",
        "gt" to ">",
        "agrave" to "à", "egrave" to "è", "eacute" to "é", "igrave" to "ì", "ograve" to "ò", "ugrave" to "ù",
        "Agrave" to "À", "Egrave" to "È", "Eacute" to "É", "Igrave" to "Ì", "Ograve" to "Ò", "Ugrave" to "Ù",
        "rsquo" to "’", "lsquo" to "‘", "rdquo" to "”", "ldquo" to "“", "laquo" to "«", "raquo" to "»",
        "ndash" to "–", "mdash" to "—", "hellip" to "…", "bull" to "•", "euro" to "€", "deg" to "°",
        "copy" to "©", "reg" to "®", "trade" to "™",
    )

    private val abbreviations = setOf(
        "prof.", "dott.", "dr.", "ing.", "avv.", "sig.", "art.", "pag.", "pagg.",
        "n.", "es.", "d.lgs.", "d.p.r.", "c.d.", "s.m.i.", "ecc.", "tel.", "prot.",
    )

    private val imageExtensions = setOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".svg", ".avif", ".jfif")
    private val documentExtensions = setOf(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".zip", ".rar")

    fun formatTitle(rawTitle: String): String {
        val decoded = toPlainText(rawTitle)
        return decoded.ifBlank { rawTitle.trim() }
    }

    fun resolveAssetUrl(raw: String?): String? {
        val decoded = raw?.let(::decodeHtmlEntities)?.trim()?.takeIf(String::isNotBlank) ?: return null
        if (decoded.startsWith("data:", ignoreCase = true) || decoded.startsWith("javascript:", ignoreCase = true)) {
            return null
        }
        val cleanTarget = stripMarkdownTargetTitle(decoded).replace(" ", "%20")
        val resolved = when {
            cleanTarget.startsWith("//") ->
                "https:$cleanTarget"
            cleanTarget.startsWith("/uploads/", ignoreCase = true) ->
                UPLOADS_BASE_URL + cleanTarget.removePrefix("/uploads/").removePrefix("/UPLOADS/")
            cleanTarget.startsWith("uploads/", ignoreCase = true) ->
                UPLOADS_BASE_URL + cleanTarget.removePrefix("uploads/").removePrefix("UPLOADS/")
            cleanTarget.startsWith("/") ->
                SITE_BASE_URL + cleanTarget.removePrefix("/")
            cleanTarget.startsWith("www.", ignoreCase = true) ->
                "https://$cleanTarget"
            cleanTarget.startsWith("http://", ignoreCase = true) -> {
                val host = cleanTarget.substring(7).substringBefore('/').lowercase()
                if (host == "unimol.it" || host.endsWith(".unimol.it")) {
                    "https://" + cleanTarget.substring(7)
                } else {
                    cleanTarget
                }
            }
            else -> cleanTarget
        }
        return resolved.newsSourceUrlOrNull()
    }

    private fun stripMarkdownTargetTitle(target: String): String {
        val trimmed = target.trim().removePrefix("<").removeSuffix(">").trim()
        val quoteIdx = trimmed.indexOfFirst { it == '"' || it == '\'' }
        return if (quoteIdx > 0 && trimmed[quoteIdx - 1].isWhitespace()) {
            trimmed.substring(0, quoteIdx).trim()
        } else {
            trimmed
        }
    }

    private fun isImageUrl(url: String?): Boolean {
        val clean = url?.substringBefore('?')?.substringBefore('#')?.lowercase() ?: return false
        return imageExtensions.any { clean.endsWith(it) }
    }

    fun isDocumentUrl(url: String?): Boolean {
        val clean = url?.substringBefore('?')?.substringBefore('#')?.lowercase() ?: return false
        return documentExtensions.any { clean.endsWith(it) } ||
            (!isImageUrl(url) && clean.contains("/uploads/"))
    }

    fun formatArticle(
        title: String,
        rawDescription: String,
        rawContent: String,
        sourceUrl: String?,
        attachmentFallbackLabel: String? = null,
    ): FormattedNewsContent {
        val convertedContent = htmlOrTextToMarkdown(rawContent, attachmentFallbackLabel)
        val convertedDescription = htmlOrTextToMarkdown(rawDescription, attachmentFallbackLabel)
        val resolvedSource = resolveAssetUrl(sourceUrl)

        val rawArticle = if (convertedContent.isNotBlank()) convertedContent else convertedDescription
        val reflowedArticle = reflowPlainTextIfNeeded(rawArticle)

        val summaryPlain = buildPlainSummary(
            title = formatTitle(title),
            summaryCandidate = convertedDescription,
            articleCandidate = reflowedArticle,
            maxLength = 190,
        )

        val distinctSummaryText = convertedDescription
            .takeIf { desc ->
                desc.isNotBlank() &&
                    reflowedArticle.isNotBlank() &&
                    !toPlainText(reflowedArticle).startsWith(toPlainText(desc).take(48), ignoreCase = true) &&
                    !toPlainText(desc).equals(formatTitle(title), ignoreCase = true)
            }
            ?.let(::toPlainText)

        val attachments = extractAttachments(
            sources = listOf(convertedContent, convertedDescription, resolvedSource.orEmpty()),
            fallbackLabel = attachmentFallbackLabel,
        )

        val wordCount = toPlainText(reflowedArticle.ifBlank { convertedDescription })
            .split(Regex("""\s+"""))
            .count { it.isNotBlank() }
        val readingMinutes = if (wordCount == 0) 1 else ceil(wordCount / 180.0).toInt().coerceAtLeast(1)

        return FormattedNewsContent(
            summaryText = summaryPlain,
            distinctSummary = distinctSummaryText,
            articleMarkdown = reflowedArticle,
            attachments = attachments,
            readingMinutes = readingMinutes,
        )
    }

    fun toPlainText(raw: String): String {
        if (raw.isBlank()) return ""
        val md = htmlOrTextToMarkdown(raw)
        return md
            .replace(mdImageRegex, " ")
            .replace(mdLinkRegex, "$1")
            .replace(Regex("""^#{1,6}\s+""", RegexOption.MULTILINE), "")
            .replace(Regex("""^>\s?""", RegexOption.MULTILINE), "")
            .replace(Regex("""[*_`~]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    fun htmlOrTextToMarkdown(raw: String, attachmentFallbackLabel: String? = null): String {
        val input = raw.trim()
        if (input.isBlank()) return ""

        var text = input
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .replace("\r\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\'", "'")

        text = scriptOrStyleRegex.replace(text, " ")
        text = imgTagRegex.replace(text, " ")
        text = mdImageRegex.replace(text, " ")

        text = anchorTagRegex.replace(text) { match ->
            val url = resolveAssetUrl(match.groupValues[1]) ?: return@replace match.groupValues[2]
            val innerText = decodeHtmlEntities(anyHtmlTagRegex.replace(match.groupValues[2], " "))
                .replace(Regex("""\s+"""), " ")
                .trim()
            val label = innerText.ifBlank { cleanFilenameFromUrl(url, attachmentFallbackLabel) }
            "[$label]($url)"
        }

        text = headingTagRegex.replace(text) { match ->
            val level = match.groupValues[1].toIntOrNull()?.coerceIn(1, 3) ?: 2
            val heading = decodeHtmlEntities(anyHtmlTagRegex.replace(match.groupValues[2], " "))
                .replace(Regex("""\s+"""), " ")
                .trim()
            if (heading.isBlank()) "\n\n" else "\n\n${"#".repeat(level)} $heading\n\n"
        }

        text = strongTagRegex.replace(text) { match ->
            val inner = match.groupValues[2].trim()
            if (inner.isBlank()) "" else "**$inner**"
        }
        text = emTagRegex.replace(text) { match ->
            val inner = match.groupValues[2].trim()
            if (inner.isBlank()) "" else "*$inner*"
        }
        text = blockquoteTagRegex.replace(text) { match ->
            val quote = decodeHtmlEntities(anyHtmlTagRegex.replace(match.groupValues[1], " "))
                .replace(Regex("""\s+"""), " ")
                .trim()
            if (quote.isBlank()) "\n\n" else "\n\n> $quote\n\n"
        }
        text = hrTagRegex.replace(text, "\n\n---\n\n")
        text = liOpenRegex.replace(text, "\n- ")
        text = liCloseRegex.replace(text, "\n")
        text = brTagRegex.replace(text, "\n")
        text = blockBreakTagRegex.replace(text, "\n\n")
        text = cellTagRegex.replace(text, " ")
        text = anyHtmlTagRegex.replace(text, " ")
        text = decodeHtmlEntities(text)

        // Resolve bare /uploads/... references inside markdown or plain text safely.
        text = relativeUploadRegex.replace(text) { match ->
            resolveAssetUrl(match.groupValues[1]) ?: match.value
        }

        // Convert plain-text image URLs standing on their own line or after spaces when obvious.
        text = text.lines()
            .joinToString("\n") { line -> line.replace(Regex("""[\t ]+"""), " ").trim() }
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()

        return text
    }

    private fun reflowPlainTextIfNeeded(markdown: String): String {
        if (markdown.isBlank()) return ""
        var text = markdown

        // Expand inline bullet chains (" • A • B • C ") into readable Markdown list lines.
        if (inlineBulletRegex.findAll(text).count() >= 2) {
            text = inlineBulletRegex.replace(text, "\n- ")
        }

        // Only reflow single giant walls of prose without existing paragraph breaks.
        if (text.contains("\n\n") || text.length < 220) return text

        return splitProseIntoReadableParagraphs(text, minLength = 220).joinToString("\n\n")
    }

    fun splitProseIntoReadableParagraphs(
        text: String,
        minLength: Int = 220,
    ): List<String> {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return emptyList()
        if (trimmed.length < minLength) return listOf(trimmed)

        val sentences = splitIntoCleanSentences(trimmed)
        if (sentences.size < 4) return listOf(trimmed)

        val paragraphs = mutableListOf<String>()
        val bucket = mutableListOf<String>()
        var charCount = 0
        for (sentence in sentences) {
            bucket += sentence
            charCount += sentence.length
            if (bucket.size >= 3 || charCount >= minLength) {
                paragraphs += bucket.joinToString(" ")
                bucket.clear()
                charCount = 0
            }
        }
        if (bucket.isNotEmpty()) {
            paragraphs += bucket.joinToString(" ")
        }
        return paragraphs.ifEmpty { listOf(trimmed) }
    }

    private fun splitIntoCleanSentences(text: String): List<String> {
        val parts = mutableListOf<String>()
        var start = 0
        for (match in sentenceBoundaryRegex.findAll(text)) {
            val end = match.range.first
            val candidate = text.substring(start, end + 1).trim()
            val lastToken = candidate.substringAfterLast(' ').lowercase()
            if (lastToken in abbreviations || (lastToken.length <= 2 && lastToken.endsWith('.'))) {
                continue
            }
            if (candidate.isNotBlank()) {
                parts += candidate
                start = match.range.last + 1
            }
        }
        val tail = text.substring(start).trim()
        if (tail.isNotBlank()) parts += tail
        return parts
    }

    private fun buildPlainSummary(
        title: String,
        summaryCandidate: String,
        articleCandidate: String,
        maxLength: Int,
    ): String {
        val raw = toPlainText(summaryCandidate).ifBlank { toPlainText(articleCandidate) }
        if (raw.isBlank()) return ""
        val trimmed = if (title.length >= 12 && raw.startsWith(title, ignoreCase = true)) {
            raw.drop(title.length).trimStart(' ', ':', '-', '•', '–', '\n')
        } else {
            raw
        }.ifBlank { raw }

        if (trimmed.length <= maxLength) return trimmed
        val cut = trimmed.substring(0, maxLength)
        val lastSpace = cut.lastIndexOf(' ')
        return (if (lastSpace > maxLength / 2) cut.substring(0, lastSpace) else cut).trimEnd('.', ',', ';', ':', ' ') + "…"
    }

    private fun extractAttachments(
        sources: List<String>,
        fallbackLabel: String?,
    ): List<FormattedNewsAttachment> {
        val result = linkedMapOf<String, FormattedNewsAttachment>()
        for (source in sources) {
            if (source.isBlank()) continue
            for (match in mdLinkRegex.findAll(source)) {
                val url = resolveAssetUrl(match.groupValues[2]) ?: continue
                if (isDocumentUrl(url)) {
                    val rawLabel = match.groupValues[1].trim()
                    val label = if (rawLabel.startsWith("http", ignoreCase = true) || rawLabel.length < 3) {
                        cleanFilenameFromUrl(url, fallbackLabel)
                    } else {
                        rawLabel
                    }
                    result.getOrPut(url) { FormattedNewsAttachment(label = label, url = url, isDocument = true) }
                }
            }
            for (match in bareUrlRegex.findAll(source)) {
                val url = resolveAssetUrl(match.value.trimEnd('.', ',', ';', ')')) ?: continue
                if (isDocumentUrl(url)) {
                    result.getOrPut(url) {
                        FormattedNewsAttachment(label = cleanFilenameFromUrl(url, fallbackLabel), url = url, isDocument = true)
                    }
                }
            }
        }
        return result.values.toList()
    }

    private fun cleanFilenameFromUrl(url: String, fallbackLabel: String?): String {
        val segment = url.substringBefore('?').substringBefore('#').substringAfterLast('/').trim()
        if (segment.isBlank()) return url
        val dotIdx = segment.lastIndexOf('.')
        return if (dotIdx > 0) {
            val base = segment.substring(0, dotIdx)
                .replace(trailingHashSuffixRegex, "")
                .replace('_', ' ')
                .replace('-', ' ')
                .replace(Regex("""\s+"""), " ")
                .trim()
            val ext = segment.substring(dotIdx).lowercase()
            val label = base.ifBlank { fallbackLabel ?: return segment }
            "$label$ext"
        } else {
            segment.replace('_', ' ')
        }
    }

    fun decodeHtmlEntities(input: String): String {
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

internal fun String?.newsSourceUrlOrNull(): String? {
    val raw = this?.trim()?.takeIf(String::isNotBlank) ?: return null
    val candidate = if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw
    if (candidate.any(Char::isWhitespace) ||
        !(candidate.startsWith("https://", ignoreCase = true) || candidate.startsWith("http://", ignoreCase = true))
    ) return null
    val parsed = runCatching { Url(candidate) }.getOrNull() ?: return null
    return candidate.takeIf { parsed.host.isNotBlank() && parsed.protocol.name in setOf("http", "https") }
}
