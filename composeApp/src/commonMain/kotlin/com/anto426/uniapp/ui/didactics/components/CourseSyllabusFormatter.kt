package com.anto426.uniapp.ui.didactics.components

import com.anto426.uniapp.ui.document.DocumentMarkdownParser

/**
 * Normalizes raw Esse3 syllabus fields (HTML, entities, Dublin descriptors, bullet glyphs,
 * multi-line bibliographies, and dense prose) into clean Markdown for [com.anto426.uniapp.ui.document.components.RichMarkdownDocument].
 */
internal object CourseSyllabusFormatter {
    private val structuralPrefixRegex = Regex("""^(?:#{1,4}\s+|[-*•▪◦·–—➢►]\s+|(?:\d+|[a-zA-Z])[.)]\s+|>\s*|```|\||---|===|\*\*\*|___)""")
    private val dublinOrSyllabusLabelRegex = Regex(
        """^(Conoscenza e capacit[aà] di comprensione|Capacit[aà] di applicare conoscenza e comprensione|Autonomia di giudizio|Abilit[aà] comunicative|Capacit[aà] di apprendimento|Risultati di apprendimento attesi|Obiettivi formativi|Obiettivi specifici|Contenuti del corso|Programma esteso|Programma del corso|Metodi didattici|Modalit[aà] didattiche|Modalit[aà] di verifica(?: dell['’]apprendimento)?|Modalit[aà] d['’]esame|Criteri di valutazione|Prova scritta|Prova orale|Testi di riferimento|Testi consigliati|Bibliografia|Materiale didattico|Prerequisiti|Propedeuticit[aà]|Modulo\s+[0-9IVX]+|Parte\s+[0-9IVX]+|Unit[aà]\s+[0-9IVX]+)\s*:\s*(.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private val standaloneHeadingRegex = Regex("""^[^.!?•\-*#>`|]{3,80}:$""")
    private val excessBlankLinesRegex = Regex("""\n{3,}""")

    fun formatSectionMarkdown(
        raw: String,
        preserveLineBreaksAsList: Boolean = false,
    ): String {
        val normalized = DocumentMarkdownParser.normalizeDocumentMarkdown(raw)
        if (normalized.isBlank()) return ""

        val lines = normalized.lines()
        val nonBlankLines = lines.map(String::trim).filter(String::isNotBlank)
        val shouldListifyLines = preserveLineBreaksAsList &&
            nonBlankLines.size >= 2 &&
            nonBlankLines.none { structuralPrefixRegex.containsMatchIn(it) }

        val output = mutableListOf<String>()
        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isBlank()) {
                if (output.isNotEmpty() && output.last().isNotEmpty()) {
                    output += ""
                }
                continue
            }

            if (structuralPrefixRegex.containsMatchIn(trimmed)) {
                output += rawLine
                continue
            }

            val inlineLabelMatch = dublinOrSyllabusLabelRegex.matchEntire(trimmed)
            if (inlineLabelMatch != null) {
                val label = inlineLabelMatch.groupValues[1].trim()
                val body = inlineLabelMatch.groupValues[2].trim()
                if (output.isNotEmpty() && output.last().isNotEmpty()) {
                    output += ""
                }
                output += "**$label:** $body"
                output += ""
                continue
            }

            if (!trimmed.startsWith("http://", ignoreCase = true) &&
                !trimmed.startsWith("https://", ignoreCase = true) &&
                standaloneHeadingRegex.matches(trimmed) &&
                !trimmed.contains("://")
            ) {
                val heading = trimmed.removeSuffix(":").trim()
                if (output.isNotEmpty() && output.last().isNotEmpty()) {
                    output += ""
                }
                output += "#### $heading"
                output += ""
                continue
            }

            if (shouldListifyLines) {
                output += "- $trimmed"
            } else {
                output += trimmed
            }
        }

        return output
            .joinToString("\n")
            .replace(excessBlankLinesRegex, "\n\n")
            .trim()
    }
}
