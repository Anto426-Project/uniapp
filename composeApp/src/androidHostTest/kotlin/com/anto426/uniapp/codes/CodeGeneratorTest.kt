package com.anto426.uniapp.codes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Independently decodes rendered pixels rather than checking the encoder against itself. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CodeGeneratorTest {
    private val generator = CodeGenerator()

    @Test
    fun qrCodePreservesExactPayloadIncludingWhitespaceAndUnicode() {
        for (payload in listOf("001234|DE ROSSI|Anna Maria|Scienze e Tecnologie", "  badge:001234\n", "https://example.org/ticket?id=00123&token=a%2Fb", "Università 🔐 日本語")) {
            val result = decode(generator.qrCode(payload), 660, 660)
            assertEquals(com.google.zxing.BarcodeFormat.QR_CODE, result.barcodeFormat)
            assertEquals(payload, result.text)
        }
    }

    @Test
    fun artisticQrPreservesPayloadAndScannabilityAtCardSizes() {
        for (payload in listOf("001234|DE ROSSI|Anna Maria|Scienze e Tecnologie", "Università 🔐 日本語", "https://example.org/" + "x".repeat(160))) {
            for (pixels in listOf(240, 480, 720)) {
                val result = decode(generator.qrCode(payload, QrCodeStyle.Artistic), pixels, pixels)
                assertEquals(payload, result.text)
            }
        }
    }

    @Test
    fun code128PreservesLeadingZeroesAndCase() {
        val payload = "00123-Abc-xyz"
        val result = decode(generator.barcode(payload), 1200, 240)
        assertEquals(com.google.zxing.BarcodeFormat.CODE_128, result.barcodeFormat)
        assertEquals(payload, result.text)
    }

    @Test
    fun ean13PreservesTheExplicitValidCheckDigit() {
        val payload = "9780201379624"
        val result = decode(generator.barcode(payload, BarcodeFormat.Ean13), 960, 240)
        assertEquals(com.google.zxing.BarcodeFormat.EAN_13, result.barcodeFormat)
        assertEquals(payload, result.text)
    }

    @Test
    fun invalidCodesAreRejectedWithoutInventingOrRepairingPayloads() {
        assertFailsWith<IllegalArgumentException> { generator.qrCode("") }
        assertFailsWith<IllegalArgumentException> { generator.qrCode("x".repeat(10_000)) }
        assertFailsWith<IllegalArgumentException> { generator.barcode("") }
        assertFailsWith<IllegalArgumentException> { generator.barcode("è") }
        assertFailsWith<IllegalArgumentException> { generator.barcode("978020137962", BarcodeFormat.Ean13) }
        assertFailsWith<IllegalArgumentException> { generator.barcode("9780201379625", BarcodeFormat.Ean13) }
        assertFailsWith<IllegalArgumentException> { generator.barcode("978020137962X", BarcodeFormat.Ean13) }
    }

    private fun decode(painter: Painter, width: Int, height: Int): com.google.zxing.Result {
        val image = ImageBitmap(width, height)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(width.toFloat(), height.toFloat())) {
            with(painter) { draw(size) }
        }
        val pixels = IntArray(width * height)
        image.asAndroidBitmap().getPixels(pixels, 0, width, 0, 0, width, height)
        // Every generator supplies an opaque, white quiet zone even on a dark UI.
        assertEquals(-1, pixels.first())
        assertEquals(-1, pixels.last())
        return MultiFormatReader().decode(
            BinaryBitmap(HybridBinarizer(RGBLuminanceSource(width, height, pixels))),
            mapOf(DecodeHintType.CHARACTER_SET to "UTF-8"),
        )
    }
}
