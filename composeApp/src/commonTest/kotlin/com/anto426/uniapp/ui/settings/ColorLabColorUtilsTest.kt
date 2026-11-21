package com.anto426.uniapp.ui.settings

import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ColorLabColorUtilsTest {

    @Test
    fun colorToHexAndParseRoundTrip() {
        val sample = Color(0xFF2979FF)
        assertEquals("#2979FF", colorToHex(sample))
        val parsed = parseHexColor("#2979FF")
        assertNotNull(parsed)
        assertTrue(parsed.sameRgbAs(sample))
    }

    @Test
    fun parseHexSupportsShortAndPrefixlessForms() {
        val shortHex = parseHexColor("0F8")
        assertNotNull(shortHex)
        assertEquals("#00FF88", colorToHex(shortHex))

        val plainHex = parseHexColor("0b57d0")
        assertNotNull(plainHex)
        assertEquals("#0B57D0", colorToHex(plainHex))

        assertNull(parseHexColor("#ZZZZZZ"))
        assertNull(parseHexColor("#12345"))
    }

    @Test
    fun hsvRoundTripAndFallbackHueOnAchromaticColors() {
        val original = Color(0xFFE91E63)
        val hsv = colorToHsv(original)
        val rebuilt = hsvToColor(hsv.hue, hsv.saturation, hsv.value)
        assertTrue(rebuilt.sameRgbAs(original))

        val whiteHsv = colorToHsv(Color.White, fallbackHue = 145f)
        assertEquals(145f, whiteHsv.hue, 0.01f)
        assertEquals(0f, whiteHsv.saturation, 0.01f)
        assertEquals(1f, whiteHsv.value, 0.01f)
    }

    @Test
    fun randomHarmoniousColorProducesOpaqueTastefulSeed() {
        val color = randomHarmoniousColor(Random(42))
        assertEquals(1f, color.alpha)
        val hsv = colorToHsv(color)
        assertTrue(hsv.saturation in 0.55f..0.91f)
        assertTrue(hsv.value in 0.61f..0.93f)
    }
}
