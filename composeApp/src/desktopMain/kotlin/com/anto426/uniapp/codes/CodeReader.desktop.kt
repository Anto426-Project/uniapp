package com.anto426.uniapp.codes
import com.google.zxing.*
import com.google.zxing.BarcodeFormat as ZxingFormat
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
actual fun createCodeReader(): CodeReader = CodeReader { bytes ->
    withContext(Dispatchers.Default) {
        val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: throw IllegalArgumentException("Unsupported image")
        val bitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(image)))
        val reader = MultiFormatReader()
        val hints = mapOf(DecodeHintType.TRY_HARDER to true, DecodeHintType.CHARACTER_SET to "UTF-8")
        val results = try { GenericMultipleBarcodeReader(reader).decodeMultiple(bitmap, hints).toList() }
            catch (_: NotFoundException) { emptyList() }
        results.map { result ->
            val format = when (result.barcodeFormat) {
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
            val symbology = result.resultMetadata?.get(ResultMetadataType.SYMBOLOGY_IDENTIFIER) as? String
            val segments = result.resultMetadata?.get(ResultMetadataType.BYTE_SEGMENTS) as? List<*>
            val utf8 = segments.orEmpty().all { segment -> (segment as? ByteArray)?.let {
                runCatching { it.decodeToString(throwOnInvalidSequence = true) }.isSuccess
            } == true }
            DecodedCode(result.text, format, format != CodeFormat.Unsupported && utf8 &&
                symbology !in setOf("]C1", "]Q3", "]Q4", "]Q5", "]Q6") && '\uFFFD' !in result.text)
        }.distinctBy { it.value to it.format }
    }
}
