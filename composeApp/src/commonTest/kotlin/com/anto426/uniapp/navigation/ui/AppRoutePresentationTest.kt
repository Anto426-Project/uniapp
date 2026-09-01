package com.anto426.uniapp.navigation.ui

import com.anto426.uniapp.navigation.model.AppRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class AppRoutePresentationTest {
    @Test
    fun newsDetailUsesTheSelectedNewsTitle() {
        val route =
            AppRoute.NewsDetail(
                title = "Bando borse di studio",
                description = "Descrizione",
                fullContent = "Contenuto",
            )

        assertEquals("Bando borse di studio", route.presentation().title)
    }

    @Test
    fun ticketDetailUsesTheSelectedTicketTitle() {
        val route = AppRoute.TicketDetail(ticketId = "ticket-1", title = "Abbonamento mensile")

        assertEquals("Abbonamento mensile", route.presentation().title)
    }
}
