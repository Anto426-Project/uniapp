package com.anto426.uniapp.didactics.presentation

import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.parseExamRoundDateOrNull
import com.anto426.unisdk.backend.model.parseExamRoundDateTimeOrNull
import io.ktor.http.URLBuilder
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** Opens an editable calendar draft; the student chooses whether to save it. */
internal fun ExamRoundData.calendarUrlOrNull(): String? {
    if (!booked) return null
    val date = parseExamRoundDateOrNull(dateTime) ?: return null
    fun LocalDate.compact() = toString().replace("-", "")
    val dates = if (Regex("""(?:T|\s)\d{1,2}:\d{2}""").containsMatchIn(dateTime)) {
        val start = runCatching { Instant.parse(dateTime) }.getOrNull()
            ?: parseExamRoundDateTimeOrNull(dateTime)?.toInstant(TimeZone.of("Europe/Rome")) ?: return null
        fun Instant.compact(): String {
            val utc = toLocalDateTime(TimeZone.UTC)
            return utc.date.compact() + "T" + utc.hour.toString().padStart(2, '0') +
                utc.minute.toString().padStart(2, '0') + utc.second.toString().padStart(2, '0') + "Z"
        }
        "${start.compact()}/${(start + 1.hours).compact()}"
    } else {
        // Missing exam time is represented as an all-day draft, not an invented start time.
        "${date.compact()}/${date.plus(1, DateTimeUnit.DAY).compact()}"
    }
    return URLBuilder("https://calendar.google.com/calendar/render").apply {
        parameters.append("action", "TEMPLATE")
        parameters.append("text", courseName)
        parameters.append("dates", dates)
        parameters.append("ctz", "Europe/Rome")
        room.takeIf { it.isNotBlank() && it != "-" }?.let { parameters.append("location", it) }
        listOfNotNull(presidentFullName, notes).filter(String::isNotBlank).joinToString("\n")
            .takeIf(String::isNotBlank)?.let { parameters.append("details", it) }
    }.buildString()
}
