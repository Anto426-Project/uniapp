package com.anto426.uniapp.navigation.ui

import com.anto426.uniapp.navigation.model.AppRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import uniapp.composeapp.generated.resources.*

class AppRoutePresentationTest {
    @Test
    fun newsDetailUsesStandardTitleResource() {
        val route =
            AppRoute.NewsDetail(
                title = "Bando borse di studio",
                description = "Descrizione",
                fullContent = "Contenuto",
            )

        assertEquals(null, route.presentation().titleString)
        assertEquals(Res.string.nav_route_news_detail_default_title, route.presentation().titleRes)
    }

    @Test
    fun ticketDetailUsesTheSelectedTicketTitle() {
        val route = AppRoute.TicketDetail(ticketId = "ticket-1", title = "Abbonamento mensile")

        assertEquals("Abbonamento mensile", route.presentation().titleString)
    }
}
