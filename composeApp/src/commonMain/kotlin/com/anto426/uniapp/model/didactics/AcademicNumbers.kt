package com.anto426.uniapp.model.didactics

import kotlin.math.abs
import kotlin.math.round

/** Reads the first whole number without joining separate values such as `120 / 180`. */
internal fun String.firstAcademicIntegerOrNull(): Int? =
    Regex("""\d+""").find(this)?.value?.toIntOrNull()

fun Double.toFixedTwoDecimals(): String {
    val scaled = round(this * 100.0).toLong()
    val magnitude = abs(scaled)
    val sign = if (scaled < 0) "-" else ""
    val fraction = (magnitude % 100).toString().padStart(2, '0')
    return "$sign${magnitude / 100}.$fraction"
}

