package com.anto426.uniapp.ui.document

import com.anto426.unisdk.model.NewsContentFormatter

internal data class ParsedMarkdownSection(
    val title: String?,
    val body: String,
)

internal enum class MarkdownAlertKind {
    Note,
    Tip,
    Important,
    Warning,
    Caution,
}

internal sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class ListItem(val marker: String, val text: String, val indentLevel: Int) : MarkdownBlock
    data class TaskItem(val checked: Boolean, val text: String, val indentLevel: Int) : MarkdownBlock
    data class BlockQuote(val text: String, val alertKind: MarkdownAlertKind? = null) : MarkdownBlock
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock
    data object Divider : MarkdownBlock
}

internal object DocumentMarkdownParser {
    private val htmlCommentRegex = Regex("""<!--[\s\S]*?-->""")
    private val detailsSummaryRegex = Regex("""<summary\b[^>]*>([\s\S]*?)</summary>""", RegexOption.IGNORE_CASE)
    private val detailsTagRegex = Regex("""</?details\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val htmlAnchorRegex = Regex("""<a\b[^>]*\bhref\s*=\s*["']([^"']+)["'][^>]*>([\s\S]*?)</a>""", RegexOption.IGNORE_CASE)
    private val htmlHeadingRegex = Regex("""<h([1-4])\b[^>]*>([\s\S]*?)</h\1>""", RegexOption.IGNORE_CASE)
    private val htmlStrongRegex = Regex("""<(strong|b)\b[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
    private val htmlEmRegex = Regex("""<(em|i)\b[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
    private val htmlCodeRegex = Regex("""<code\b[^>]*>([\s\S]*?)</code>""", RegexOption.IGNORE_CASE)
    private val htmlLiOpenRegex = Regex("""<li\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val htmlLiCloseRegex = Regex("""</li>""", RegexOption.IGNORE_CASE)
    private val htmlBrRegex = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
    private val htmlBlockBreakRegex = Regex("""</?(?:p|div|ul|ol|blockquote|section|article)\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val htmlImgRegex = Regex("""<img\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val htmlTagRegex = Regex("""<(?!(?:https?|mailto):)[^>\s][^>]*>""")

    private val mdHeadingRegex = Regex("""^(#{1,4})\s+(.+)$""")
    private val dividerRegex = Regex("""^(?:---+|===+|\*\*\*+|___+)\s*$""")
    private val fenceOpenRegex = Regex("""^```([A-Za-z0-9_+-]*)\s*$""")
    private val taskRegex = Regex("""^(?:[-*•]\s+)?\[([ xX])]\s+(.+)$""")
    private val bulletRegex = Regex("""^[-*•]\s+(.+)$""")
    private val orderedRegex = Regex("""^(\d+)[.)]\s+(.+)$""")
    private val quoteAlertRegex = Regex("""^\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val tableSeparatorCellRegex = Regex("""^:?-{2,}:?$""")
    private val markdownImageLineRegex = Regex("""^!\[[^\]]*]\([^)\n]+\)$""")
    private val inlineBulletChainRegex = Regex("""\s+[•▪◦]\s+""")
    private val bareGitHubPullRegex = Regex("""(?<![(\[])https://github\.com/[^\s/]+/[^\s/]+/pull/(\d+)""")
    private val bareGitHubCompareRegex = Regex("""(?<![(\[])https://github\.com/[^\s/]+/[^\s/]+/compare/([^\s)]+)""")

    /**
     * Normalizes GitHub Release notes, changelog entries, and legal Markdown strings by cleaning
     * line endings, HTML comments/tags, and HTML entities while preserving Markdown semantics.
     */
    fun normalizeDocumentMarkdown(raw: String): String {
        if (raw.isBlank()) return ""
        var text = raw
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .replace("\r\n", "\n")

        text = htmlCommentRegex.replace(text, "")
        text = htmlImgRegex.replace(text, "")
        text = detailsSummaryRegex.replace(text) { m ->
            val summary = htmlTagRegex.replace(m.groupValues[1], " ").trim()
            if (summary.isBlank()) "" else "\n\n### $summary\n\n"
        }
        text = detailsTagRegex.replace(text, "\n")

        if (htmlTagRegex.containsMatchIn(text)) {
            text = htmlAnchorRegex.replace(text) { m ->
                val url = m.groupValues[1].trim()
                val label = htmlTagRegex.replace(m.groupValues[2], " ").replace(Regex("""\s+"""), " ").trim()
                if (url.startsWith("http://", true) || url.startsWith("https://", true) || url.startsWith("mailto:", true)) {
                    "[$label]($url)"
                } else {
                    label
                }
            }
            text = htmlHeadingRegex.replace(text) { m ->
                val level = m.groupValues[1].toIntOrNull()?.coerceIn(1, 4) ?: 2
                val heading = htmlTagRegex.replace(m.groupValues[2], " ").replace(Regex("""\s+"""), " ").trim()
                if (heading.isBlank()) "\n\n" else "\n\n${"#".repeat(level)} $heading\n\n"
            }
            text = htmlStrongRegex.replace(text) { m -> "**${m.groupValues[2].trim()}**" }
            text = htmlEmRegex.replace(text) { m -> "*${m.groupValues[2].trim()}*" }
            text = htmlCodeRegex.replace(text) { m -> "`${m.groupValues[1].trim()}`" }
            text = htmlLiOpenRegex.replace(text, "\n- ")
            text = htmlLiCloseRegex.replace(text, "\n")
            text = htmlBrRegex.replace(text, "\n")
            text = htmlBlockBreakRegex.replace(text, "\n\n")
            text = htmlTagRegex.replace(text, "")
        }

        text = NewsContentFormatter.decodeHtmlEntities(text)
        text = bareGitHubPullRegex.replace(text) { m -> "[#${m.groupValues[1]}](${m.value})" }
        text = bareGitHubCompareRegex.replace(text) { m -> "[${m.groupValues[1]}](${m.value})" }
        if (inlineBulletChainRegex.findAll(text).count() >= 2) {
            text = inlineBulletChainRegex.replace(text, "\n- ")
        }
        return text.lines()
            .joinToString("\n") { line -> line.trimEnd() }
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    /** Splits top-level document sections by `## ` headings (falling back to `# ` if no `## ` exists), ignoring fenced code. */
    fun splitMarkdownSections(markdown: String): List<ParsedMarkdownSection> {
        val normalized = normalizeDocumentMarkdown(markdown)
        if (normalized.isBlank()) return emptyList()

        val rawLines = normalized.lines()
        val hasH2 = hasHeadingOutsideFence(rawLines, "## ")
        val splitPrefix = if (hasH2) "## " else if (hasHeadingOutsideFence(rawLines, "# ")) "# " else null

        if (splitPrefix == null) {
            return listOf(ParsedMarkdownSection(null, normalized))
        }

        val sections = mutableListOf<ParsedMarkdownSection>()
        var currentTitle: String? = null
        val currentLines = mutableListOf<String>()
        var insideFence = false

        fun flush() {
            val cleaned = currentLines
                .dropWhile { it.isBlank() || dividerRegex.matches(it.trim()) }
                .dropLastWhile { it.isBlank() || dividerRegex.matches(it.trim()) }
                .joinToString("\n")
            if (!currentTitle.isNullOrBlank() || cleaned.isNotBlank()) {
                sections += ParsedMarkdownSection(currentTitle, cleaned)
            }
            currentLines.clear()
        }

        for (line in rawLines) {
            val trimmed = line.trim()
            if (fenceOpenRegex.matches(trimmed) || (insideFence && trimmed == "```")) {
                insideFence = !insideFence
                currentLines += line
                continue
            }
            if (!insideFence && trimmed.startsWith(splitPrefix)) {
                flush()
                currentTitle = cleanHeadingText(trimmed.removePrefix(splitPrefix))
            } else {
                currentLines += line
            }
        }
        flush()
        return sections
    }

    fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
        val normalized = normalizeDocumentMarkdown(markdown)
        if (normalized.isBlank()) return emptyList()

        val lines = normalized.lines()
        val blocks = mutableListOf<MarkdownBlock>()
        val paragraphLines = mutableListOf<String>()
        var i = 0

        fun flushParagraph() {
            if (paragraphLines.isNotEmpty()) {
                val joined = paragraphLines.joinToString(" ").replace(Regex("""\s+"""), " ").trim()
                if (joined.isNotBlank()) {
                    for (paragraph in NewsContentFormatter.splitProseIntoReadableParagraphs(joined, minLength = 260)) {
                        blocks += MarkdownBlock.Paragraph(paragraph)
                    }
                }
                paragraphLines.clear()
            }
        }

        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trim()

            if (trimmed.isBlank()) {
                flushParagraph()
                i += 1
                continue
            }

            if (markdownImageLineRegex.matches(trimmed)) {
                flushParagraph()
                i += 1
                continue
            }

            // 1. Fenced code block
            val fenceMatch = fenceOpenRegex.matchEntire(trimmed)
            if (fenceMatch != null) {
                flushParagraph()
                val language = fenceMatch.groupValues[1].trim().takeIf { it.isNotBlank() }
                val codeLines = mutableListOf<String>()
                i += 1
                while (i < lines.size && lines[i].trim() != "```") {
                    codeLines += lines[i]
                    i += 1
                }
                if (i < lines.size && lines[i].trim() == "```") i += 1
                blocks += MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n").trimEnd())
                continue
            }

            // 2. Markdown table (`| a | b |` + `| --- | --- |`)
            if (trimmed.startsWith("|") && i + 1 < lines.size && isTableSeparatorLine(lines[i + 1].trim())) {
                flushParagraph()
                val headers = parseTableRow(trimmed)
                i += 2
                val rows = mutableListOf<List<String>>()
                while (i < lines.size && lines[i].trim().startsWith("|")) {
                    rows += parseTableRow(lines[i].trim())
                    i += 1
                }
                if (headers.isNotEmpty()) {
                    blocks += MarkdownBlock.TableBlock(headers, rows)
                }
                continue
            }

            // 3. Horizontal rule
            if (dividerRegex.matches(trimmed)) {
                flushParagraph()
                blocks += MarkdownBlock.Divider
                i += 1
                continue
            }

            // 4. Headings `#` .. `####`
            val headingMatch = mdHeadingRegex.matchEntire(trimmed)
            if (headingMatch != null) {
                flushParagraph()
                val level = headingMatch.groupValues[1].length.coerceIn(1, 4)
                val title = cleanHeadingText(headingMatch.groupValues[2])
                if (title.isNotBlank()) {
                    blocks += MarkdownBlock.Heading(level, title)
                }
                i += 1
                continue
            }

            // 5. Multi-line blockquote / GitHub Alert (`> ...`)
            if (trimmed.startsWith(">")) {
                flushParagraph()
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    val content = lines[i].trim().removePrefix(">").trim()
                    quoteLines += content
                    i += 1
                }
                val firstLine = quoteLines.firstOrNull().orEmpty()
                val alertMatch = quoteAlertRegex.matchEntire(firstLine)
                val alertKind = alertMatch?.groupValues?.getOrNull(1)?.uppercase()?.let { tag ->
                    when (tag) {
                        "NOTE" -> MarkdownAlertKind.Note
                        "TIP" -> MarkdownAlertKind.Tip
                        "IMPORTANT" -> MarkdownAlertKind.Important
                        "WARNING" -> MarkdownAlertKind.Warning
                        "CAUTION" -> MarkdownAlertKind.Caution
                        else -> null
                    }
                }
                val cleanQuoteLines = if (alertKind != null) {
                    val restOfFirst = alertMatch.groupValues[2].trim()
                    listOfNotNull(restOfFirst.takeIf { it.isNotBlank() }) + quoteLines.drop(1)
                } else {
                    quoteLines
                }
                val quoteBody = cleanQuoteLines.filter { it.isNotBlank() }.joinToString("\n")
                if (quoteBody.isNotBlank()) {
                    blocks += MarkdownBlock.BlockQuote(quoteBody, alertKind)
                }
                continue
            }

            // 6. Task item (`- [x] ...`, `- [ ] ...`)
            val indentLevel = countIndentLevel(rawLine)
            val taskMatch = taskRegex.matchEntire(trimmed)
            if (taskMatch != null) {
                flushParagraph()
                val checked = taskMatch.groupValues[1].equals("x", ignoreCase = true)
                blocks += MarkdownBlock.TaskItem(checked, taskMatch.groupValues[2].trim(), indentLevel)
                i += 1
                continue
            }

            // 7. Unordered list item (`- `, `* `, `• `)
            val bulletMatch = bulletRegex.matchEntire(trimmed)
            if (bulletMatch != null) {
                flushParagraph()
                val marker = if (indentLevel > 0) "◦" else "•"
                blocks += MarkdownBlock.ListItem(marker, bulletMatch.groupValues[1].trim(), indentLevel)
                i += 1
                continue
            }

            // 8. Ordered list item (`1. `, `1) `)
            val orderedMatch = orderedRegex.matchEntire(trimmed)
            if (orderedMatch != null) {
                flushParagraph()
                blocks += MarkdownBlock.ListItem("${orderedMatch.groupValues[1]}.", orderedMatch.groupValues[2].trim(), indentLevel)
                i += 1
                continue
            }

            paragraphLines += trimmed
            i += 1
        }
        flushParagraph()
        return blocks
    }

    private fun hasHeadingOutsideFence(lines: List<String>, prefix: String): Boolean {
        var insideFence = false
        for (line in lines) {
            val trimmed = line.trim()
            if (fenceOpenRegex.matches(trimmed) || (insideFence && trimmed == "```")) {
                insideFence = !insideFence
                continue
            }
            if (!insideFence && trimmed.startsWith(prefix)) return true
        }
        return false
    }

    private fun cleanHeadingText(raw: String): String {
        val trimmed = raw.trim().removeSuffix("#").trim()
        return if (trimmed.length >= 4 && ((trimmed.startsWith("**") && trimmed.endsWith("**")) || (trimmed.startsWith("__") && trimmed.endsWith("__")))) {
            trimmed.drop(2).dropLast(2).trim()
        } else {
            trimmed
        }
    }

    private fun countIndentLevel(rawLine: String): Int {
        var spaces = 0
        for (ch in rawLine) {
            when (ch) {
                ' ' -> spaces += 1
                '\t' -> spaces += 2
                else -> break
            }
        }
        return (spaces / 2).coerceIn(0, 3)
    }

    private fun isTableSeparatorLine(trimmed: String): Boolean {
        if (!trimmed.startsWith("|")) return false
        val cells = parseTableRow(trimmed)
        return cells.isNotEmpty() && cells.all { tableSeparatorCellRegex.matches(it) }
    }

    private fun parseTableRow(trimmed: String): List<String> {
        return trimmed
            .removePrefix("|")
            .removeSuffix("|")
            .split('|')
            .map { it.trim() }
    }
}
