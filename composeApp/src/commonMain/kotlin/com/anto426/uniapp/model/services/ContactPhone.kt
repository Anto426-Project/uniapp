package com.anto426.uniapp.model.services

/** Only complete, unambiguous numbers can become a call action. */
internal fun String.contactDialUri(): String? {
    val value = trim()
    if (value.count(Char::isDigit) < 7 || " / " in value) return null
    if (!Regex("^\\+?[0-9() ./-]+$").matches(value)) return null
    val normalized = value.filter(Char::isDigit)
    return "tel:${if (value.startsWith('+')) "+" else ""}$normalized"
}
