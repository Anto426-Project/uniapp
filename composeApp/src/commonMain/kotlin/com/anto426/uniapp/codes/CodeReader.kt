package com.anto426.uniapp.codes

/** A decoded value retains its symbology; an unknown/binary/GS1 code must use the original image. */
data class DecodedCode(val value: String, val format: CodeFormat, val canRegenerate: Boolean = true)

enum class CodeFormat { Qr, Code128, Code39, Code93, Ean13, Ean8, UpcA, UpcE, Itf, Codabar, Unsupported }

/** Reads all distinct QR/barcodes from an encoded PNG/JPEG image, entirely on the device. */
fun interface CodeReader {
    suspend fun read(image: ByteArray): List<DecodedCode>
}

expect fun createCodeReader(): CodeReader
