package com.anto426.uniapp.ui.components.cards

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class UniHeroCardPaletteTest {
    @Test
    fun artworkKeepsItsHueWithoutWashingOutWhiteText() {
        val seeds = listOf(
            Color.White,
            Color.Black,
            Color(0xFFFFEB3B),
            Color(0xFF80FF00),
            Color(0xFF03A9F4),
            Color(0xFFB71C1C),
        )

        seeds.forEach { seed ->
            val palette = generateUniHeroCardPalette(seed)
            assertTrue(palette.cool.luminance() <= 0.141f, "cool pigment for $seed")
            assertTrue(palette.violet.luminance() <= 0.101f, "violet pigment for $seed")
            assertTrue(palette.warm.luminance() <= 0.171f, "warm pigment for $seed")
            assertTrue(palette.glow.luminance() <= 0.221f, "glow pigment for $seed")
            assertTrue(palette.warm.heroTextAccent().luminance() >= 0.719f, "text accent for $seed")

            if (seed.toHsv()[1] >= 0.08f) {
                val hueDifference = abs(palette.cool.toHsv()[0] - seed.toHsv()[0])
                assertTrue(minOf(hueDifference, 360f - hueDifference) < 1f, "seed hue for $seed")
            }
        }
    }
}
