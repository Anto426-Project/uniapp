package com.anto426.uniapp.library.model

import com.anto426.uniapp.data.contract.LibraryBookCopyAction
import com.anto426.uniapp.data.contract.LibraryTextParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibraryTextParserTest {
    @Test
    fun parseCatalogBookExtractsAuthorYearAndBranches() {
        val parsed = LibraryTextParser.parseCatalogBook(
            id = "MOL00065117",
            title = "Informatica : arte e mestiere",
            rawDescription = "Informatica : arte e mestiere Ceri, Stefano 1999 Lo trovi in Biblioteca di Ateneo Sede di Campobasso Biblioteca di Ateneo Sede di Pesche Biblioteca di Ateneo di Termoli",
        )

        assertEquals("MOL00065117", parsed.id)
        assertEquals("Informatica : arte e mestiere", parsed.title)
        assertEquals("Ceri, Stefano", parsed.subtitle)
        assertEquals("1999", parsed.metadata)
        assertEquals(listOf("Campobasso", "Pesche", "Termoli"), parsed.branches)
    }

    @Test
    fun branchDisplayNameResolvesMoliseCodesAndFallsBackCleanly() {
        assertEquals("Biblioteca di Ateneo · Campobasso", LibraryTextParser.branchDisplayName("MOLBU"))
        assertEquals("Biblioteca di Ateneo · Pesche", LibraryTextParser.branchDisplayName("molba"))
        assertEquals("Biblioteca di Ateneo · Termoli", LibraryTextParser.branchDisplayName("MOLBT"))
        assertEquals("OTHER", LibraryTextParser.branchDisplayName("OTHER"))
    }

    @Test
    fun parseMovementSetsBranchAndActiveBadges() {
        val cancelable = LibraryTextParser.parseMovement(
            id = "mov-1",
            rawDescription = "Biblioteca Pesche Libro richiesto Da ritirare entro il 22/10/2026 Cancella",
            canCancel = true,
            canRenew = false,
            actionToken = "tok-1",
        )
        assertEquals("Pesche", cancelable.branchName)
        assertEquals("Libro richiesto Da ritirare entro il 22/10/2026", cancelable.title)

        val renewable = LibraryTextParser.parseMovement(
            id = "mov-2",
            rawDescription = "Campobasso Manuale Kotlin Proroga",
            canCancel = false,
            canRenew = true,
            actionToken = "tok-2",
        )
        assertEquals("Campobasso", renewable.branchName)
    }

    @Test
    fun parseSlotExtractsDateTimeAndSeatsWhenPresent() {
        val slot = LibraryTextParser.parseSlot(
            slotId = "s0",
            label = "1",
            rawDescription = "19/10/2026 08:30 - 13:00 8 8 1",
            requiresDeviceAddress = false,
        )
        assertEquals("19/10/2026", slot.dateLabel)
        assertEquals("08:30 – 13:00", slot.timeLabel)
        assertEquals("8 / 8 posti", slot.seatsLabel)
        assertTrue(slot.description.contains("08:30"))
    }

    @Test
    fun parseSpaceBookingExtractsTitleDateAndTimeRange() {
        val booking = LibraryTextParser.parseSpaceBooking(
            id = "b1",
            rawDescription = "Sala Studio Pesche 19/10/2026 08:30 - 13:00 Annulla",
            canCancel = true,
            bookingToken = "tok-b1",
        )
        assertEquals("Sala Studio Pesche", booking.title)
        assertEquals("19/10/2026", booking.dateLabel)
        assertEquals("08:30 – 13:00", booking.timeLabel)
    }

    @Test
    fun parseBookDetailExtractsInventoryShelfmarkAndNotes() {
        val detail = LibraryTextParser.parseBookDetail(
            id = "MOL00065117",
            title = "Informatica : arte e mestiere",
            rawDescription = "Informatica : arte e mestiere Ceri, Stefano 1999 Documento disponibile Inventario D1 1225 Collocazione PE SL004 CERIS Note 1 v.",
            actions = listOf(LibraryBookCopyAction("a1", "Richiesta", "MOLBA", "Pesche", "RPE")),
        )
        assertTrue(detail.hasAvailableDocument)
        assertEquals("D1 1225", detail.inventory)
        assertEquals("PE SL004 CERIS", detail.shelfmark)
        assertEquals("1 v.", detail.notes)
    }
}
