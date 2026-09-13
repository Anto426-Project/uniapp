package com.anto426.uniapp.codes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Color
import com.google.zxing.BarcodeFormat as ZxingFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.EncodeHintType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assume
import java.io.File
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import kotlin.test.*

/** Input pixels come from an independent encoder, placed inside a photographed-ticket layout. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CodeReaderTest {
    @Test fun readsBadgeAndBarcodeFromAnImageWithoutChangingTheirContents() = runTest {
        for ((value, format, expected) in listOf(
            Triple("001234|DE ROSSI|Anna Maria|INFORMATICA", ZxingFormat.QR_CODE, CodeFormat.Qr),
            Triple("00000123-AbC", ZxingFormat.CODE_128, CodeFormat.Code128),
        )) {
            val decoded = createCodeReader().read(image(value, format))
            assertEquals(listOf(DecodedCode(value, expected)), decoded)
        }
    }

    @Test fun readsRotatedTicketBarcode() = runTest {
        val decoded = createCodeReader().read(image("000123", ZxingFormat.CODE_128, rotated = true))
        assertEquals(listOf(DecodedCode("000123", CodeFormat.Code128)), decoded)
    }

    @Test fun gs1AndUnsupportedFormatsRemainOriginalOnly() = runTest {
        val gs1 = createCodeReader().read(image("0101234567890128", ZxingFormat.CODE_128, gs1 = true)).single()
        assertFalse(gs1.canRegenerate)
        val aztec = createCodeReader().read(image("original-only", ZxingFormat.AZTEC)).single()
        assertEquals(CodeFormat.Unsupported, aztec.format)
        assertFailsWith<IllegalArgumentException> { CodeGenerator().generate(aztec) }
    }

    @Test fun blankImageReturnsNoCodeAndInvalidBytesAreRejected() = runTest {
        val blank = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        assertTrue(createCodeReader().read(png(blank)).isEmpty())
        assertFailsWith<IllegalArgumentException> { createCodeReader().read(byteArrayOf(1, 2, 3)) }
    }

    /** Optional local reference; never copy personal badge/ticket images into test fixtures. */
    @Test fun suppliedReferenceImageCanBeReadAndRecreated() = runTest {
        val reference = System.getenv("UNIAPP_CODE_REFERENCE_IMAGE")
        Assume.assumeTrue("Set UNIAPP_CODE_REFERENCE_IMAGE to verify a private reference", reference != null)
        val codes = createCodeReader().read(File(reference!!).readBytes())
        assertTrue(codes.isNotEmpty(), "No code found in reference image")
        for (code in codes) {
            assertTrue(code.canRegenerate, "Reference format cannot be recreated")
            val painter = CodeGenerator().generate(code)
            val width = 1000
            val height = if (code.format == CodeFormat.Qr) 1000 else 240
            val bitmap = ImageBitmap(width, height)
            CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, ComposeCanvas(bitmap), Size(width.toFloat(), height.toFloat())) {
                with(painter) { draw(size) }
            }
            val reread = createCodeReader().read(png(bitmap.asAndroidBitmap()))
            assertTrue(reread.singleOrNull() == code, "Recreated code differs from reference")
        }
    }

    private fun image(value: String, format: ZxingFormat, rotated: Boolean = false, gs1: Boolean = false): ByteArray {
        val contents = if (gs1 && format == ZxingFormat.CODE_128) "\u00f1$value" else value
        val matrix = MultiFormatWriter().encode(contents, format, 600, if (format == ZxingFormat.CODE_128) 160 else 600,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.GS1_FORMAT to gs1))
        val code = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) code.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        val ticket = Bitmap.createBitmap(900, 1200, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        Canvas(ticket).drawBitmap(code, 150f, 330f, null)
        code.recycle()
        if (!rotated) return png(ticket)
        val turned = Bitmap.createBitmap(ticket, 0, 0, ticket.width, ticket.height, Matrix().apply { postRotate(90f) }, false)
        ticket.recycle()
        return png(turned)
    }

    private fun png(bitmap: Bitmap): ByteArray = ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        bitmap.recycle()
        stream.toByteArray()
    }
}
