package com.anto426.uniapp.model.didactics

/** Reads the first whole number without joining separate values such as `120 / 180`. */
internal fun String.firstAcademicIntegerOrNull(): Int? =
    Regex("""\d+""").find(this)?.value?.toIntOrNull()
