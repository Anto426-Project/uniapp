package com.anto426.uniapp.model.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContactPhoneTest {
    @Test
    fun completeNumbersKeepTheirCountryCodeAndDialWithoutFormatting() {
        assertEquals("tel:+390874404281", "+39 0874 404 281".contactDialUri())
        assertEquals("tel:0874404281", "0874/404281".contactDialUri())
    }

    @Test
    fun extensionsAndAmbiguousCombinedValuesCannotBeCalledAsFullNumbers() {
        assertNull("2281".contactDialUri())
        assertNull("+39 0874 404 281 / 493".contactDialUri())
    }
}
