package com.anto426.uniapp.presentation

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UniSdkErrorMessagesTest {
    @Test
    fun knownCodesHaveApplicationResources() {
        assertNotNull(sdkErrorResource("UNI_AUTH_INVALID_CREDENTIALS"))
        assertNotNull(sdkErrorResource("UNI_TRANSPORT_BOOKING_UNVERIFIED"))
        assertNull(sdkErrorResource("UNI_FUTURE_UNKNOWN_ERROR"))
        assertNull(sdkErrorResource("Server provided text"))
    }

    @Test
    fun unknownCodesUseTheFeatureFallbackAndLegacyMessagesRemainReadable() = runTest {
        assertEquals("Riprova", sdkFailureMessage("UNI_FUTURE_UNKNOWN_ERROR", "Riprova"))
        assertEquals("Riprova", sdkFailureMessage(null, "Riprova"))
        assertEquals("Messaggio precedente", sdkFailureMessage("Messaggio precedente", "Riprova"))
    }

    @Test
    fun ordinaryMessagesDoNotReadLocalizedFallbackResources() = runTest {
        assertEquals("Messaggio precedente", sdkFailureMessage("Messaggio precedente") {
            error("The fallback must remain lazy")
        })
    }
}
