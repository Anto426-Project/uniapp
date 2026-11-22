package com.anto426.uniapp.ui.didactics.components

import com.anto426.uniapp.ui.document.DocumentMarkdownParser
import com.anto426.uniapp.ui.document.MarkdownBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CourseSyllabusFormatterTest {
    @Test
    fun formatsHtmlSyllabusListsEntitiesAndIgnoresEmptyBoldTags() {
        val raw = """
            <p><strong>&nbsp;</strong></p>
            <p>Il corso introduce l&rsquo;architettura dei calcolatori e i sistemi operativi.</p>
            <ol>
              <li>Rappresentazione dell&rsquo;informazione</li>
              <li>Gestione della memoria &amp; processi</li>
            </ol>
            <ul>
              <li><strong>Laboratorio:</strong> esercitazioni in C</li>
              <li>Vedi <a href="https://learn.unimol.it">Moodle</a></li>
            </ul>
        """.trimIndent()

        val formatted = CourseSyllabusFormatter.formatSectionMarkdown(raw)
        assertFalse(formatted.contains("****"))
        assertFalse(formatted.contains("<p>"))
        assertTrue(formatted.contains("l’architettura"))
        assertTrue(formatted.contains("1. Rappresentazione dell’informazione"))
        assertTrue(formatted.contains("2. Gestione della memoria & processi"))
        assertTrue(formatted.contains("[Moodle](https://learn.unimol.it)"))

        val blocks = DocumentMarkdownParser.parseMarkdownBlocks(formatted)
        assertTrue(blocks.any { it is MarkdownBlock.ListItem && it.marker == "1." })
        assertTrue(blocks.any { it is MarkdownBlock.ListItem && it.marker == "2." })
        assertTrue(blocks.any { it is MarkdownBlock.ListItem && it.marker == "•" })
    }

    @Test
    fun promotesDublinDescriptorsAndStandaloneSyllabusSubheadings() {
        val raw = """
            Obiettivi specifici:
            Conoscenza e capacità di comprensione: Lo studente acquisirà le basi teoriche.
            Autonomia di giudizio: Capacità di valutare soluzioni progettuali.
            – Primo punto con trattino lungo
            ▪ Secondo punto con quadratino
        """.trimIndent()

        val formatted = CourseSyllabusFormatter.formatSectionMarkdown(raw)
        val blocks = DocumentMarkdownParser.parseMarkdownBlocks(formatted)

        assertEquals(MarkdownBlock.Heading(4, "Obiettivi specifici"), blocks[0])
        assertTrue(
            blocks.any {
                it is MarkdownBlock.Paragraph &&
                    it.text == "**Conoscenza e capacità di comprensione:** Lo studente acquisirà le basi teoriche."
            },
        )
        assertTrue(
            blocks.any {
                it is MarkdownBlock.Paragraph &&
                    it.text == "**Autonomia di giudizio:** Capacità di valutare soluzioni progettuali."
            },
        )
        assertTrue(blocks.any { it is MarkdownBlock.ListItem && it.text == "Primo punto con trattino lungo" })
        assertTrue(blocks.any { it is MarkdownBlock.ListItem && it.text == "Secondo punto con quadratino" })
    }

    @Test
    fun preservesSeparateBibliographyLinesAsListItems() {
        val raw = """
            Cormen, Leiserson, Rivest, Stein - Introduzione agli algoritmi, McGraw-Hill
            Tanenbaum, Bos - I moderni sistemi operativi, Pearson
            Slide e dispense disponibili su https://learn.unimol.it
        """.trimIndent()

        val formatted = CourseSyllabusFormatter.formatSectionMarkdown(raw, preserveLineBreaksAsList = true)
        val blocks = DocumentMarkdownParser.parseMarkdownBlocks(formatted)

        assertEquals(3, blocks.size)
        assertTrue(blocks.all { it is MarkdownBlock.ListItem })
    }
}
