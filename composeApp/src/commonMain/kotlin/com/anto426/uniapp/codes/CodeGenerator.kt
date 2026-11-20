package com.anto426.uniapp.codes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import io.github.alexzhirkevich.qrose.QrCodePainter
import io.github.alexzhirkevich.qrose.oned.BarcodePainter
import io.github.alexzhirkevich.qrose.oned.BarcodeType
import io.github.alexzhirkevich.qrose.options.QrBackground
import io.github.alexzhirkevich.qrose.options.QrCodeMatrix
import io.github.alexzhirkevich.qrose.options.QrCodeShape
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.QrShapes
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid

/**
 * Generates scannable vector codes on Android and iOS, with a white quiet zone.
 * Payloads are encoded verbatim: no trimming, casing changes or display-value fallback.
 * Invalid/oversized data throws IllegalArgumentException. Remember the returned painter in UI.
 */
class CodeGenerator {
    fun generate(
        code: DecodedCode,
        moduleColor: Color? = null,
        accentColor: Color? = null,
        backgroundColor: Color = Color.White,
    ): Painter {
        require(code.canRegenerate) { "Use the original image for this code" }
        return when (code.format) {
            CodeFormat.Qr -> qrCode(
                value = code.value,
                moduleColor = moduleColor,
                accentColor = accentColor,
                backgroundColor = backgroundColor,
            )
            CodeFormat.Unsupported -> throw IllegalArgumentException("Unsupported code format")
            else -> barcode(
                value = code.value,
                format = BarcodeFormat.valueOf(code.format.name),
                barColor = moduleColor,
                backgroundColor = backgroundColor,
            )
        }
    }

    fun qrCode(
        value: String,
        style: QrCodeStyle = QrCodeStyle.Classic,
        moduleColor: Color? = null,
        accentColor: Color? = null,
        backgroundColor: Color = Color.White,
    ): Painter {
        require(value.isNotEmpty()) { "QR code data cannot be empty" }
        val resolvedModule = moduleColor ?: when (style) {
            QrCodeStyle.Classic -> Color.Black
            QrCodeStyle.Artistic -> Color(0xFF142B3A)
        }
        val resolvedAccent = accentColor ?: when (style) {
            QrCodeStyle.Classic -> resolvedModule
            QrCodeStyle.Artistic -> if (moduleColor != null) resolvedModule else Color(0xFF164C50)
        }
        return QrCodePainter(
            data = value,
            options = QrOptions(
                shapes = when (style) {
                    QrCodeStyle.Classic -> QrShapes(code = QrQuietZone())
                    QrCodeStyle.Artistic -> QrShapes(
                        code = QrQuietZone(),
                        darkPixel = QrPixelShape.roundCorners(.42f),
                        frame = QrFrameShape.roundCorners(.26f, bottomRight = false),
                        ball = QrBallShape.roundCorners(.30f, bottomRight = false),
                    )
                },
                colors = when (style) {
                    QrCodeStyle.Classic -> if (moduleColor == null && accentColor == null) {
                        QrColors()
                    } else {
                        QrColors(
                            dark = QrBrush.solid(resolvedModule),
                            frame = QrBrush.solid(resolvedAccent),
                            ball = QrBrush.solid(resolvedModule),
                        )
                    }
                    QrCodeStyle.Artistic -> QrColors(
                        dark = QrBrush.solid(resolvedModule),
                        frame = QrBrush.solid(resolvedAccent),
                        ball = QrBrush.solid(resolvedModule),
                    )
                },
                background = QrBackground(fill = SolidColor(backgroundColor)),
                errorCorrectionLevel = QrErrorCorrectionLevel.Medium,
            ),
        )
    }

    fun barcode(
        value: String,
        format: BarcodeFormat = BarcodeFormat.Code128,
        barColor: Color? = null,
        backgroundColor: Color = Color.White,
    ): Painter {
        require(value.isNotEmpty()) { "Barcode data cannot be empty" }
        val type = when (format) {
            BarcodeFormat.Code128 -> {
                require(value.all { it.code in 0..127 }) { "Code 128 requires ASCII data" }
                BarcodeType.Code128
            }
            BarcodeFormat.Code39 -> BarcodeType.Code39
            BarcodeFormat.Code93 -> BarcodeType.Code93
            BarcodeFormat.Itf -> BarcodeType.ITF
            BarcodeFormat.Codabar -> {
                require(value.first() in "ABCD" && value.last() in "ABCD") { "Codabar requires explicit start and end guards" }
                BarcodeType.Codabar
            }
            BarcodeFormat.Ean8 -> { require(value.length == 8); BarcodeType.EAN8 }
            BarcodeFormat.UpcA -> { require(value.length == 12); BarcodeType.UPCA }
            BarcodeFormat.UpcE -> { require(value.length == 8); BarcodeType.UPCE }
            BarcodeFormat.Ean13 -> {
                require(value.length == 13 && value.all { it in '0'..'9' }) {
                    "EAN-13 requires 13 digits including the check digit"
                }
                BarcodeType.EAN13
            }
        }
        val bars = type.encoder.encode(value)
        // Eleven blank modules on each side cover both Code 128 and EAN-13 quiet zones.
        val padded = BooleanArray(bars.size + 22)
        bars.copyInto(padded, destinationOffset = 11)
        return WhiteBackgroundPainter(
            content = BarcodePainter(padded),
            backgroundColor = backgroundColor,
            barColor = barColor,
        )
    }
}

enum class BarcodeFormat { Code128, Code39, Code93, Ean13, Ean8, UpcA, UpcE, Itf, Codabar }
enum class QrCodeStyle { Classic, Artistic }

private class QrQuietZone : QrCodeShape {
    override var shapeSizeIncrease: Float = 1f
        private set

    override fun QrCodeMatrix.transform(): QrCodeMatrix {
        shapeSizeIncrease = (size + 8).toFloat() / size
        val padded = QrCodeMatrix(size + 8)
        for (y in 0 until size) {
            for (x in 0 until size) padded[x + 4, y + 4] = this[x, y]
        }
        return padded
    }
}

private class WhiteBackgroundPainter(
    private val content: Painter,
    private val backgroundColor: Color = Color.White,
    private val barColor: Color? = null,
) : Painter() {
    override val intrinsicSize: Size get() = content.intrinsicSize

    override fun DrawScope.onDraw() {
        drawRect(backgroundColor)
        if (barColor == null) {
            with(content) { draw(size) }
        } else {
            val bounds = Rect(Offset.Zero, size)
            val paint = Paint().apply {
                colorFilter = ColorFilter.tint(barColor, BlendMode.SrcIn)
            }
            drawIntoCanvas { canvas ->
                canvas.saveLayer(bounds, paint)
                with(content) { draw(size) }
                canvas.restore()
            }
        }
    }
}
