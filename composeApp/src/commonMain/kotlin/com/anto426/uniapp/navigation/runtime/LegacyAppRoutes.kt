package com.anto426.uniapp.navigation.runtime

import androidx.navigation3.runtime.NavKey
import com.anto426.uniapp.navigation.model.AppRoute
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder

/** Decode-only compatibility for back stacks written before screen consolidation. */
internal fun legacyAppRoute(serialName: String?): DeserializationStrategy<NavKey>? {
    val prefix = "com.anto426.uniapp.navigation.model.AppRoute."
    val destination = when (serialName) {
        prefix + "TransportCatalog", prefix + "TicketDetail" -> AppRoute.Transport
        prefix + "Statistics" -> AppRoute.Grades
        prefix + "StudyPlan" -> AppRoute.Transcripts(initialTab = 1)
        prefix + "ExamsHistory" -> AppRoute.Exams(initialTab = 2)
        else -> return null
    }
    return object : DeserializationStrategy<NavKey> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName) {
            if (serialName == prefix + "TicketDetail") {
                element<String>("ticketId", isOptional = true)
                element<String>("title", isOptional = true)
            }
        }
        override fun deserialize(decoder: Decoder): NavKey {
            val input = decoder.beginStructure(descriptor)
            while (true) {
                val index = input.decodeElementIndex(descriptor)
                if (index == CompositeDecoder.DECODE_DONE) break
                input.decodeStringElement(descriptor, index)
            }
            input.endStructure(descriptor)
            return destination
        }
    }
}
