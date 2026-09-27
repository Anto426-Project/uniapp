package com.anto426.uniapp.ui.news

import com.anto426.uniapp.data.toNewsItems
import com.anto426.unisdk.backend.model.UniversityNews
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewsDetailContentTest {
    @Test
    fun originalLinkReachesTheDetailAndOnlyWebUrlsCanBeOpened() {
        val news = UniversityNews(
            id = "42",
            title = "Avviso",
            summary = "Riepilogo",
            content = "Testo completo",
            publishedAt = "2026-09-27",
            category = "Ateneo",
            sourceUrl = " https://www.unimol.it/notizie/42 ",
        )
        val item = listOf(news).toNewsItems().single()
        assertEquals("https://www.unimol.it/notizie/42", item.sourceUrl.newsSourceUrlOrNull())
        assertEquals("https://www.unimol.it/news", "www.unimol.it/news".newsSourceUrlOrNull())
        assertNull("javascript:alert(1)".newsSourceUrlOrNull())
        assertNull("https://example.com/path with spaces".newsSourceUrlOrNull())
    }

    @Test
    fun articleHasSectionsWithoutRepeatingATruncatedSummary() {
        val formatted = formatNewsBody("Una sintesi diversa", "Testo completo", "In breve", "Notizia")
        assertTrue(formatted.startsWith("## In breve\n\nUna sintesi diversa"))
        assertTrue(formatted.contains("## Notizia\n\nTesto completo"))

        val repeated = formatNewsBody("Testo", "Testo completo", "In breve", "Notizia")
        assertFalse(repeated.contains("In breve"))
        assertEquals("## Notizia\n\nTesto completo", repeated)
    }
}
