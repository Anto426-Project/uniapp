@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.anto426.uniapp.codes

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Vision.*

actual fun createCodeReader(): CodeReader = CodeReader { image ->
    withContext(Dispatchers.Default) {
        require(image.isNotEmpty()) { "Image is empty" }
        val data = image.usePinned { NSData.create(bytes = it.addressOf(0), length = image.size.toULong()) }
        val request = VNDetectBarcodesRequest()
        val handler = VNImageRequestHandler(data = data, options = emptyMap<Any?, Any>())
        check(handler.performRequests(listOf(request), error = null)) { "Unable to read ticket image" }
        request.results.orEmpty().filterIsInstance<VNBarcodeObservation>().map { observation ->
            val value = observation.payloadStringValue.orEmpty()
            val format = when (observation.symbology) {
                VNBarcodeSymbologyQR -> CodeFormat.Qr
                VNBarcodeSymbologyCode128 -> CodeFormat.Code128
                VNBarcodeSymbologyCode39 -> CodeFormat.Code39
                VNBarcodeSymbologyCode93 -> CodeFormat.Code93
                VNBarcodeSymbologyEAN13 -> CodeFormat.Ean13
                VNBarcodeSymbologyEAN8 -> CodeFormat.Ean8
                VNBarcodeSymbologyUPCE -> CodeFormat.UpcE
                VNBarcodeSymbologyI2of5 -> CodeFormat.Itf
                VNBarcodeSymbologyCodabar -> CodeFormat.Codabar
                else -> CodeFormat.Unsupported
            }
            // Vision's string-only API cannot preserve binary or GS1 metadata. These use the original.
            DecodedCode(value, format, value.isNotEmpty() && format != CodeFormat.Unsupported &&
                value.all { it.code in 32..126 } && !observation.isGS1DataCarrier)
        }.distinct()
    }
}
