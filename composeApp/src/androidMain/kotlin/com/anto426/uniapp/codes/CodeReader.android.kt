package com.anto426.uniapp.codes

import android.graphics.BitmapFactory
import com.google.zxing.*
import com.google.zxing.BarcodeFormat as ZxingFormat
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

actual fun createCodeReader(): CodeReader = CodeReader { image ->
    withContext(Dispatchers.Default) {
        require(image.isNotEmpty()) { "Image is empty" }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(image, 0, image.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Invalid image" }
        val options = BitmapFactory.Options().apply {
            inSampleSize = 1
            while (bounds.outWidth / inSampleSize > 2048 || bounds.outHeight / inSampleSize > 2048) inSampleSize *= 2
        }
        val bitmap = requireNotNull(BitmapFactory.decodeByteArray(image, 0, image.size, options)) { "Invalid image" }
        try {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            var source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
            val results = mutableListOf<DecodedCode>()
            val hints = mapOf(DecodeHintType.TRY_HARDER to true, DecodeHintType.RETURN_CODABAR_START_END to true)
            // RGBLuminanceSource cannot rotate; use transposed pixels for the second pass.
            repeat(2) { orientation ->
                ensureActive()
                for (candidate in listOf(source, source.invert())) {
                    try {
                        GenericMultipleBarcodeReader(MultiFormatReader())
                            .decodeMultiple(BinaryBitmap(HybridBinarizer(candidate)), hints)
                            .mapTo(results) { it.toDecodedCode() }
                    } catch (_: NotFoundException) { /* No readable code in this orientation. */ }
                }
                if (orientation == 0) {
                    val rotated = IntArray(pixels.size)
                    for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                        rotated[x * bitmap.height + bitmap.height - 1 - y] = pixels[y * bitmap.width + x]
                    }
                    source = RGBLuminanceSource(bitmap.height, bitmap.width, rotated)
                }
            }
            results.distinct()
        } finally { bitmap.recycle() }
    }
}

private fun Result.toDecodedCode(): DecodedCode {
    val format = when (barcodeFormat) {
        ZxingFormat.QR_CODE -> CodeFormat.Qr
        ZxingFormat.CODE_128 -> CodeFormat.Code128
        ZxingFormat.CODE_39 -> CodeFormat.Code39
        ZxingFormat.CODE_93 -> CodeFormat.Code93
        ZxingFormat.EAN_13 -> CodeFormat.Ean13
        ZxingFormat.EAN_8 -> CodeFormat.Ean8
        ZxingFormat.UPC_A -> CodeFormat.UpcA
        ZxingFormat.UPC_E -> CodeFormat.UpcE
        ZxingFormat.ITF -> CodeFormat.Itf
        ZxingFormat.CODABAR -> CodeFormat.Codabar
        else -> CodeFormat.Unsupported
    }
    val symbology = resultMetadata?.get(ResultMetadataType.SYMBOLOGY_IDENTIFIER) as? String
    val segments = resultMetadata?.get(ResultMetadataType.BYTE_SEGMENTS) as? List<*>
    val utf8 = segments.orEmpty().all { segment ->
        (segment as? ByteArray)?.let { runCatching { it.decodeToString(throwOnInvalidSequence = true) }.isSuccess } == true
    }
    val ordinary = symbology !in setOf("]C1", "]Q3", "]Q4", "]Q5", "]Q6")
    return DecodedCode(text, format, format != CodeFormat.Unsupported && ordinary && utf8 && '\uFFFD' !in text)
}
