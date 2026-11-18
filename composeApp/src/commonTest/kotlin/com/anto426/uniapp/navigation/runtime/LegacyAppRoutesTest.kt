package com.anto426.uniapp.navigation.runtime

import androidx.navigation3.runtime.NavKey
import com.anto426.uniapp.navigation.model.AppRoute
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class LegacyAppRoutesTest {
    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) { defaultDeserializer { legacyAppRoute(it) } }
        }
    }

    @Test
    fun savedDestinationsOpenTheirConsolidatedScreen() {
        val destinations = mapOf(
            "TransportCatalog" to AppRoute.Transport,
            "Statistics" to AppRoute.Grades,
            "StudyPlan" to AppRoute.Transcripts(initialTab = 1),
            "ExamsHistory" to AppRoute.Exams(initialTab = 2),
        )
        destinations.forEach { (oldName, destination) ->
            val saved = """{"type":"com.anto426.uniapp.navigation.model.AppRoute.$oldName"}"""
            assertEquals(destination, json.decodeFromString(PolymorphicSerializer(NavKey::class), saved))
        }
    }

    @Test
    fun retiredTicketIdentifiersAreConsumedWithoutRecreatingSyntheticProducts() {
        val saved = """{"type":"com.anto426.uniapp.navigation.model.AppRoute.TicketDetail","ticketId":"old-product","title":"Old title"}"""
        assertEquals(AppRoute.Transport, json.decodeFromString(PolymorphicSerializer(NavKey::class), saved))
    }

    @Test
    fun unknownDestinationsAreNotSilentlyAccepted() {
        assertFails {
            json.decodeFromString(PolymorphicSerializer(NavKey::class), """{"type":"unknown.screen"}""")
        }
    }
}
