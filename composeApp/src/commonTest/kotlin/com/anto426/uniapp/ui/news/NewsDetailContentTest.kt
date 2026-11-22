package com.anto426.uniapp.ui.news

import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.contract.toNewsItems
import com.anto426.uniapp.data.contract.NewsContentFormatter
import com.anto426.unisdk.model.UniversityNews
import com.anto426.uniapp.data.contract.newsSourceUrlOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
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

    @Test
    fun formatterDecodesHtmlConvertsListsStripsImagesAndExtractsAttachments() {
        val rawHtml = """
            <p>Pubblicato l&rsquo;avviso per l&agrave; partecipazione &#8217;26.</p>
            <img src="/uploads/locandina_test.jpg" alt="Locandina">
            <ul>
              <li>Primo punto</li>
              <li><a href="/uploads/Bando_Definitivo_a1b2c3d4.pdf">Scarica bando</a></li>
            </ul>
        """.trimIndent()

        val formatted = NewsContentFormatter.formatArticle(
            title = "Avviso &egrave; online",
            rawDescription = "<p>Pubblicato l&rsquo;avviso per l&agrave; partecipazione</p>",
            rawContent = rawHtml,
            sourceUrl = null,
        )

        assertFalse(formatted.articleMarkdown.contains("locandina_test.jpg"))
        assertFalse(formatted.articleMarkdown.contains("!["))
        assertNull(formatted.distinctSummary)
        assertTrue(formatted.articleMarkdown.contains("Pubblicato l’avviso per là partecipazione ’26."))
        assertTrue(formatted.articleMarkdown.contains("- Primo punto"))
        assertEquals(1, formatted.attachments.size)
        assertEquals("Scarica bando", formatted.attachments.single().label)
        assertEquals("https://apistrapi.unimol.it/uploads/Bando_Definitivo_a1b2c3d4.pdf", formatted.attachments.single().url)
    }

    @Test
    fun formatterReflowsFlattenedWallsOfTextIntoReadableParagraphs() {
        val flattened =
            "Prima frase lunga che spiega l'avviso accademico di ateneo. " +
                "Seconda frase con dettagli sulle date di apertura delle domande. " +
                "Terza frase relativa ai requisiti richiesti agli studenti iscritti. " +
                "Quarta frase con le indicazioni sugli uffici di segreteria e orari."

        val formatted = NewsContentFormatter.formatArticle(
            title = "Avviso",
            rawDescription = "",
            rawContent = flattened,
            sourceUrl = null,
        )

        assertTrue(formatted.articleMarkdown.contains("\n\n"))
    }

    @Test
    fun mapperFormatsNewsWithVersionedCacheKey() {
        assertEquals("university-news-v3", UniDatasets.News.cacheKey)

        val news = UniversityNews(
            id = "99",
            title = "Avviso &egrave; online",
            summary = "Sintesi evento.",
            content = "<p>Dettagli completi.</p><img src=\"/uploads/locandina_evento.jpg\" alt=\"Locandina\">",
            publishedAt = "2026-10-02",
            category = "Ateneo",
            sourceUrl = "https://www3.unimol.it/notizie/99",
        )

        val item = listOf(news).toNewsItems().single()
        assertEquals("Avviso è online", item.title)
        assertEquals("Sintesi evento.", item.description)
    }

    @Test
    fun cachedUniversityNewsSerializationSupportsCurrentAndLegacyPayloads() {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val serializer = ListSerializer(UniversityNews.serializer())
        val original = listOf(
            UniversityNews(
                id = "10",
                title = "Seminario",
                summary = "Riepilogo",
                content = "Corpo in markdown",
                publishedAt = "2026-10-05",
                category = "Dipartimento",
                sourceUrl = "https://www3.unimol.it/notizie/10",
            ),
        )

        val encoded = json.encodeToString(serializer, original)
        val decoded = json.decodeFromString(serializer, encoded)
        assertEquals(original, decoded)

        val legacyPayload = """
            [
              {
                "id": "11",
                "title": "Avviso precedente",
                "summary": "Testo",
                "content": "Corpo",
                "publishedAt": "2026-09-01",
                "category": "Ateneo",
                "sourceUrl": null
              }
            ]
        """.trimIndent()
        val decodedLegacy = json.decodeFromString(serializer, legacyPayload).single()
        assertNull(decodedLegacy.imageUrl)
        assertEquals("Avviso precedente", decodedLegacy.title)
    }

    @Test
    fun formatterResolvesDocumentUrlsAndStripsMarkdownOrHtmlImages() {
        assertEquals(
            "https://apistrapi.unimol.it/uploads/Bando%202026.pdf",
            NewsContentFormatter.resolveAssetUrl("http://apistrapi.unimol.it/uploads/Bando 2026.pdf"),
        )
        assertEquals(
            "https://apistrapi.unimol.it/uploads/doc_banner.pdf",
            NewsContentFormatter.resolveAssetUrl("uploads/doc_banner.pdf"),
        )

        val formatted = NewsContentFormatter.formatArticle(
            title = "Evento",
            rawDescription = "",
            rawContent = """
                <p>Dettagli evento.</p>
                <img src="/uploads/locandina.jpg" alt="Locandina">
                ![Foto](https://apistrapi.unimol.it/uploads/foto.jpg)
            """.trimIndent(),
            sourceUrl = null,
        )
        assertEquals("Dettagli evento.", formatted.articleMarkdown)
    }
}
