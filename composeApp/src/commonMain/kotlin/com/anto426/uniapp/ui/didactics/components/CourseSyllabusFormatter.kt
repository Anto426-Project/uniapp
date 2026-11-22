package com.anto426.uniapp.ui.didactics.components

import com.anto426.uniapp.ui.document.DocumentMarkdownParser

/**
 * Normalizes raw Esse3 / Cineca Course Catalogue syllabus fields (HTML, entities, standalone and
 * inline Dublin descriptors, module/CFU headings, tab-separated assessment tables, wrapped
 * bibliographies, bare emails, and dense prose) into structured Markdown for
 * [com.anto426.uniapp.ui.document.components.RichMarkdownDocument].
 */
internal object CourseSyllabusFormatter {
    private val structuralPrefixRegex = Regex("""^(?:#{1,4}\s+|[-*•▪◦·–—➢►]\s+|(?:\d+|[a-zA-Z])[.)]\s+|>\s*|```|\||---|===|\*\*\*|___)""")

    private const val KNOWN_LABELS_PATTERN =
        """Conoscenza e capacit[aà] di comprensione|Capacit[aà] di applicare conoscenza e comprensione|Autonomia di giudizio|Abilit[aà] comunicative|Capacit[aà] di apprendimento|Knowledge and understanding|Applied knowledge and understanding|Making judgments|Communication skills|Ability to learn|Risultati di apprendimento attesi|Obiettivi formativi|Obiettivi specifici|Contenuti del corso|Programma esteso|Programma del corso|Metodi didattici|Modalit[aà] didattiche|Modalit[aà] di verifica(?: dell['’]apprendimento)?|Modalit[aà] d['’]esame|Criteri di valutazione|Prova scritta|Prova orale|Testi di riferimento|Testi consigliati|Bibliografia|Materiale didattico|Prerequisiti|Propedeuticit[aà]|Altre informazioni|Studenti con disabilit[aà] o studenti con DSA|Modulo\s+[0-9IVX]+|Parte\s+[0-9IVX]+|Unit[aà]\s+[0-9IVX]+"""

    private val inlineLabelRegex = Regex(
        """^($KNOWN_LABELS_PATTERN)\s*:\s*(.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private val standaloneKnownLabelRegex = Regex(
        """^($KNOWN_LABELS_PATTERN)\s*:?$""",
        RegexOption.IGNORE_CASE,
    )
    private val moduleHeadingRegex = Regex(
        """^(?:[IVX]+\s+Modulo|Modulo\s+[0-9IVX]+|[IVX]+\s+Parte|Parte\s+[0-9IVX]+|Unit[aà]\s+[0-9IVX]+)(?:\s*[:\-–—]\s*(.+))?$""",
        RegexOption.IGNORE_CASE,
    )
    private val cfuHeadingRegex = Regex(
        """^CFU\s*(\d+)(?:\s*[:\-–—]\s*(.+))?$""",
        RegexOption.IGNORE_CASE,
    )
    private val standaloneHeadingRegex = Regex("""^[^.!?•\-*#>`|\t]{3,80}:$""")
    private val dsaHeadingRegex = Regex("""^Studenti con disabilit[aà].*DSA$""", RegexOption.IGNORE_CASE)
    private val bibliographyIntroRegex = Regex(
        """^(?:Nel corso della prima lezione|Durante il corso|Il docente|I docenti|Per la preparazione|Verranno forniti|Si consiglia).*$""",
        RegexOption.IGNORE_CASE,
    )
    private val bareEmailRegex = Regex("""(?<![\w\[(\/:])([A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,})(?![\w\])])""")
    private val excessBlankLinesRegex = Regex("""\n{3,}""")

    fun formatSectionMarkdown(
        raw: String,
        preserveLineBreaksAsList: Boolean = false,
    ): String {
        val preNormalized = DocumentMarkdownParser.normalizeDocumentMarkdown(raw)
        if (preNormalized.isBlank()) return ""

        val lines = convertTabSeparatedTables(preNormalized.lines())
        val mergedLines = if (preserveLineBreaksAsList) mergeWrappedBibliographyLines(lines) else lines
        val nonBlankLines = mergedLines.map(String::trim).filter(String::isNotBlank)
        val shouldListifyLines = preserveLineBreaksAsList &&
            nonBlankLines.size >= 2 &&
            nonBlankLines.none { structuralPrefixRegex.containsMatchIn(it) }

        val output = mutableListOf<String>()
        var inDsaCallout = false

        fun ensureBlankSeparator() {
            if (output.isNotEmpty() && output.last().isNotEmpty()) {
                output += ""
            }
        }

        for (rawLine in mergedLines) {
            val trimmed = rawLine.trim()
            if (trimmed.isBlank()) {
                inDsaCallout = false
                ensureBlankSeparator()
                continue
            }

            if (structuralPrefixRegex.containsMatchIn(trimmed)) {
                inDsaCallout = false
                output += autoLinkEmails(rawLine)
                continue
            }

            val moduleMatch = moduleHeadingRegex.matchEntire(trimmed)
            if (moduleMatch != null && !trimmed.contains(": ")) {
                inDsaCallout = false
                ensureBlankSeparator()
                output += "## $trimmed"
                output += ""
                continue
            }

            val cfuMatch = cfuHeadingRegex.matchEntire(trimmed)
            if (cfuMatch != null) {
                inDsaCallout = false
                val cfuNum = cfuMatch.groupValues[1]
                val subtitle = cfuMatch.groupValues[2].trim()
                ensureBlankSeparator()
                output += if (subtitle.isNotBlank()) "#### CFU $cfuNum · $subtitle" else "#### CFU $cfuNum"
                output += ""
                continue
            }

            val inlineLabelMatch = inlineLabelRegex.matchEntire(trimmed)
            if (inlineLabelMatch != null) {
                inDsaCallout = false
                val label = inlineLabelMatch.groupValues[1].trim()
                val body = autoLinkEmails(inlineLabelMatch.groupValues[2].trim())
                ensureBlankSeparator()
                output += "**$label:** $body"
                output += ""
                continue
            }

            val standaloneKnownMatch = standaloneKnownLabelRegex.matchEntire(trimmed)
            if (standaloneKnownMatch != null) {
                val heading = standaloneKnownMatch.groupValues[1].trim()
                ensureBlankSeparator()
                if (dsaHeadingRegex.matches(heading)) {
                    inDsaCallout = true
                    output += "> [!NOTE]"
                    output += "> **$heading**"
                } else {
                    inDsaCallout = false
                    output += "#### $heading"
                    output += ""
                }
                continue
            }

            if (!trimmed.startsWith("http://", ignoreCase = true) &&
                !trimmed.startsWith("https://", ignoreCase = true) &&
                standaloneHeadingRegex.matches(trimmed) &&
                !trimmed.contains("://")
            ) {
                inDsaCallout = false
                val heading = trimmed.removeSuffix(":").trim()
                ensureBlankSeparator()
                output += "#### $heading"
                output += ""
                continue
            }

            val linked = autoLinkEmails(trimmed)
            if (inDsaCallout) {
                output += "> $linked"
                continue
            }

            if (shouldListifyLines) {
                if (output.isEmpty() && bibliographyIntroRegex.matches(trimmed)) {
                    output += linked
                    output += ""
                } else {
                    output += "- $linked"
                }
            } else {
                output += linked
            }
        }

        return output
            .joinToString("\n")
            .replace(excessBlankLinesRegex, "\n\n")
            .trim()
    }

    /**
     * Converts Cineca TSV tables (e.g. Dublin descriptor verification grids with `\t` delimiters,
     * including wrapped multi-line cells) into standard Markdown pipe tables so
     * [com.anto426.uniapp.ui.document.components.RichMarkdownDocument] renders a styled TableBlock.
     */
    private fun convertTabSeparatedTables(lines: List<String>): List<String> {
        if (lines.none { it.contains('\t') }) return lines
        val result = mutableListOf<String>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (!line.contains('\t')) {
                result += line
                i++
                continue
            }

            val headerCells = line.split('\t').map { cleanTableCell(it) }
            val columnCount = headerCells.size
            if (columnCount < 2) {
                result += line.replace('\t', ' ')
                i++
                continue
            }

            val rows = mutableListOf<List<String>>()
            var j = i + 1
            var pendingCells: MutableList<String>? = null

            while (j < lines.size) {
                val rawRow = lines[j]
                val trimmed = rawRow.trim()
                if (trimmed.isBlank()) {
                    if (pendingCells != null && pendingCells.size >= columnCount) {
                        rows += pendingCells.take(columnCount)
                        pendingCells = null
                    }
                    // Look ahead: if the next non-blank line still has tabs, continue this table.
                    var look = j + 1
                    while (look < lines.size && lines[look].isBlank()) look++
                    if (look < lines.size && lines[look].contains('\t')) {
                        j++
                        continue
                    }
                    break
                }

                if (rawRow.contains('\t')) {
                    val parts = rawRow.split('\t').map { cleanTableCell(it) }
                    if (pendingCells != null && pendingCells.size < columnCount && parts.size < columnCount) {
                        // Continuation of a cell followed by remaining columns on this line.
                        val lastIdx = pendingCells.lastIndex
                        pendingCells[lastIdx] = joinCellText(pendingCells[lastIdx], parts.first())
                        for (k in 1 until parts.size) {
                            pendingCells += parts[k]
                        }
                        if (pendingCells.size >= columnCount) {
                            rows += pendingCells.take(columnCount)
                            pendingCells = null
                        }
                    } else {
                        if (pendingCells != null) {
                            rows += padCells(pendingCells, columnCount)
                            pendingCells = null
                        }
                        if (parts.size >= columnCount) {
                            rows += parts.take(columnCount)
                        } else {
                            pendingCells = parts.toMutableList()
                        }
                    }
                } else if (pendingCells != null) {
                    // Wrapped continuation line inside the current open TSV cell.
                    val lastIdx = pendingCells.lastIndex
                    pendingCells[lastIdx] = joinCellText(pendingCells[lastIdx], cleanTableCell(trimmed))
                } else {
                    break
                }
                j++
            }

            if (pendingCells != null) {
                rows += padCells(pendingCells, columnCount)
            }

            if (rows.isNotEmpty()) {
                if (result.isNotEmpty() && result.last().isNotBlank()) result += ""
                result += headerCells.joinToString(prefix = "| ", postfix = " |", separator = " | ")
                result += List(columnCount) { "---" }.joinToString(prefix = "| ", postfix = " |", separator = " | ")
                rows.forEach { row ->
                    result += row.joinToString(prefix = "| ", postfix = " |", separator = " | ")
                }
                result += ""
                i = j
            } else {
                result += line.replace('\t', ' ')
                i++
            }
        }
        return result
    }

    private fun mergeWrappedBibliographyLines(lines: List<String>): List<String> {
        val merged = mutableListOf<String>()
        for (raw in lines) {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) {
                merged += ""
                continue
            }
            val prev = merged.lastOrNull()?.trim()
            val shouldJoinWithPrevious = prev != null &&
                prev.isNotBlank() &&
                !structuralPrefixRegex.containsMatchIn(prev) &&
                !structuralPrefixRegex.containsMatchIn(trimmed) &&
                prev.endsWith(",") &&
                !prev.endsWith(".")
            if (shouldJoinWithPrevious) {
                merged[merged.lastIndex] = "$prev $trimmed"
            } else {
                merged += raw
            }
        }
        return merged
    }

    private fun cleanTableCell(raw: String): String =
        raw.trim().replace("|", "\\|").replace(Regex("""\s+"""), " ")

    private fun joinCellText(left: String, right: String): String =
        when {
            left.isBlank() -> right
            right.isBlank() -> left
            else -> "$left $right"
        }

    private fun padCells(cells: List<String>, count: Int): List<String> =
        if (cells.size >= count) cells.take(count) else cells + List(count - cells.size) { "" }

    private fun autoLinkEmails(text: String): String =
        bareEmailRegex.replace(text) { match ->
            val email = match.groupValues[1]
            "[$email](mailto:$email)"
        }
}
