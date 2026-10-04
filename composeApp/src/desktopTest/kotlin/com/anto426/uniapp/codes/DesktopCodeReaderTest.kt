package com.anto426.uniapp.codes

import com.google.zxing.BarcodeFormat as ZxingFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayOutputStream
import kotlin.test.*

class DesktopCodeReaderTest {
    private fun image(value: String, format: ZxingFormat, gs1: Boolean = false): ByteArray {
        val matrix = MultiFormatWriter().encode(if (gs1) "\u00f1$value" else value, format, 600, if (format == ZxingFormat.CODE_128) 180 else 600,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.GS1_FORMAT to gs1))
        return ByteArrayOutputStream().use { output ->
            MatrixToImageWriter.writeToStream(matrix, "PNG", output)
            output.toByteArray()
        }
    }
    @Test fun badgePayloadAndBarcodeKeepTheirExactContents() = runTest {
        val reader = createCodeReader()
        val badge = "001234|DE ROSSI|Anna Maria|INFORMATICA"
        assertEquals(listOf(DecodedCode(badge, CodeFormat.Qr)), reader.read(image(badge, ZxingFormat.QR_CODE)))
        assertEquals(listOf(DecodedCode("00000123-AbC", CodeFormat.Code128)), reader.read(image("00000123-AbC", ZxingFormat.CODE_128)))
    }
    @Test fun gs1RemainsOriginalOnly() = runTest {
        val result = createCodeReader().read(image("0101234567890128", ZxingFormat.CODE_128, true)).single()
        assertFalse(result.canRegenerate)
    }
    @Test fun malformedImagesAreRejected() = runTest {
        assertFailsWith<IllegalArgumentException> { createCodeReader().read(byteArrayOf(1, 2, 3)) }
    }
}
