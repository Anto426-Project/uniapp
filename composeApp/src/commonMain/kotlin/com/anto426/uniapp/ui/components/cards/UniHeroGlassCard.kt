package com.anto426.uniapp.ui.components.cards

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.glass.LiquidGlass
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState
import com.anto426.uniapp.ui.components.banners.logUniAppShaderError
import com.anto426.uniapp.ui.components.banners.supportsUniAppRuntimeShader
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Palette cromatica condivisa per lo shader fluido hero delle card di UniApp.
 * Sincronizzata dinamicamente con il ColorScheme attivo (Material You, preset Monet e tinte personalizzate).
 */
data class UniHeroCardPalette(
    val cool: Color,
    val violet: Color,
    val warm: Color,
    val glow: Color,
)

/**
 * CompositionLocal per la palette visiva della card hero.
 */
val LocalUniHeroCardPalette = compositionLocalOf<UniHeroCardPalette?> { null }

/**
 * Converte un [Color] nelle coordinate HSV (Hue 0..360, Saturation 0..1, Value 0..1).
 */
internal fun Color.toHsv(): FloatArray {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = 0f
    if (delta > 0.0001f) {
        h = when (max) {
            r -> 60f * (((g - b) / delta) % 6f)
            g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        if (h < 0f) h += 360f
    }

    val s = if (max > 0.0001f) delta / max else 0f
    val v = max
    return floatArrayOf(h, s, v)
}

/**
 * Converte coordinate HSV in un [Color] Compose.
 */
internal fun hsvToColor(hue: Float, saturation: Float, value: Float, alpha: Float = 1f): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val v = value.coerceIn(0f, 1f)

    val c = v * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = v - c

    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

/**
 * Keeps each pigment's hue while limiting its perceived brightness so white copy remains legible
 * for bright seeds (especially yellow, lime and monochrome themes).
 */
fun generateUniHeroCardPalette(seed: Color): UniHeroCardPalette {
    val (h, s, v) = seed.toHsv()

    val isMonochrome = s < 0.08f
    val baseSat = if (isMonochrome) s else s.coerceIn(0.46f, 0.90f)
    val baseVal = v.coerceIn(if (isMonochrome) 0.30f else 0.55f, 0.95f)

    val cool = hsvToColor(
        hue = h,
        saturation = baseSat,
        value = baseVal,
    ).limitLuminance(0.14f)

    val violet = hsvToColor(
        hue = (h + 12f) % 360f,
        saturation = (baseSat * 1.08f).coerceIn(0f, 0.95f),
        value = baseVal * 0.90f,
    ).limitLuminance(0.10f)

    val warm = hsvToColor(
        hue = ((h - 12f) % 360f + 360f) % 360f,
        saturation = if (isMonochrome) baseSat else (baseSat * 0.88f).coerceIn(0.35f, 0.85f),
        value = baseVal * 1.08f,
    ).limitLuminance(0.17f)

    val glow = hsvToColor(
        hue = (h + 3f) % 360f,
        saturation = if (isMonochrome) baseSat else (baseSat * 0.45f).coerceIn(0.18f, 0.50f),
        value = 1.0f,
    ).limitLuminance(0.22f)

    return UniHeroCardPalette(
        cool = cool,
        violet = violet,
        warm = warm,
        glow = glow,
    )
}

private fun Color.limitLuminance(maxLuminance: Float): Color {
    if (luminance() <= maxLuminance) return this
    val (hue, saturation, value) = toHsv()
    var low = 0f
    var high = value
    repeat(10) {
        val mid = (low + high) * 0.5f
        if (hsvToColor(hue, saturation, mid).luminance() > maxLuminance) high = mid else low = mid
    }
    return hsvToColor(hue, saturation, low)
}

/** A pale accent for text and controls, separate from the darker artwork pigment. */
internal fun Color.heroTextAccent(): Color {
    if (luminance() >= 0.72f) return this
    var low = 0f
    var high = 1f
    repeat(10) {
        val mid = (low + high) * 0.5f
        if (lerp(this, Color.White, mid).luminance() < 0.72f) low = mid else high = mid
    }
    return lerp(this, Color.White, high)
}

@Composable
fun rememberUniHeroCardPalette(
    seed: Color = MaterialTheme.colorScheme.primary,
): UniHeroCardPalette {
    val local = LocalUniHeroCardPalette.current
    if (local != null) return local

    return remember(seed) {
        generateUniHeroCardPalette(seed)
    }
}

/**
 * Formula procedurale AGSL dello shader fluido per le card hero (UniPass Badge, Update Banner, ecc.).
 */
const val UniHeroCardShader = """
uniform float2 resolution;
uniform float time;
layout(color) uniform float4 primaryColor;
layout(color) uniform float4 secondaryColor;
layout(color) uniform float4 tertiaryColor;
layout(color) uniform float4 glowColor;
layout(color) uniform float4 baseColor;

float softField(float2 point, float2 center, float radius) {
    return 1.0 - smoothstep(0.0, radius, length(point - center));
}

half4 main(float2 p) {
    float2 uv = p / resolution;
    float drift = sin(time * 0.42) * 0.018;

    float3 primary = primaryColor.rgb;
    float3 secondary = secondaryColor.rgb;
    float3 tertiary = tertiaryColor.rgb;
    float3 glow = glowColor.rgb;
    float3 deepBase = baseColor.rgb;

    float3 container = mix(deepBase, mix(primary, tertiary, 0.34), 0.78);
    float3 primaryTone = mix(deepBase, primary, 0.92);
    float3 secondaryTone = mix(deepBase, secondary, 0.90);
    float3 tertiaryTone = mix(deepBase, tertiary, 0.92);
    float3 glowTone = mix(deepBase, glow, 0.86);
    float3 base = mix(deepBase, mix(tertiary, primary, 0.44), 0.36);

    base = mix(base, tertiaryTone, softField(uv, float2(-0.20 + drift, 0.52), 1.04) * 0.88);
    base = mix(base, primaryTone, softField(uv, float2(1.10 - drift, 0.16), 1.00) * 0.86);
    base = mix(base, secondaryTone, softField(uv, float2(0.70 + drift, 1.12), 0.90) * 0.82);
    base = mix(base, glowTone, softField(uv, float2(0.02, 0.88 + drift), 0.42) * 0.58);

    float2 lowerCenter = float2(0.72 + drift, 1.19);
    float lowerDistance = length(uv - lowerCenter);
    float lowerBody = 1.0 - smoothstep(0.48, 0.76, lowerDistance);
    float3 lowerColor = mix(container, primaryTone, 0.78);
    base = mix(base, lowerColor, lowerBody * 0.78);
    float lowerRim = 1.0 - smoothstep(0.0, 0.012, abs(lowerDistance - 0.55));
    base = mix(base, primaryTone, lowerRim * 0.28);

    float ribbonY = 0.67 - 0.11 * sin(uv.x * 2.75 + 0.32 + time * 0.035) - uv.x * 0.07;
    float ribbonDistance = abs(uv.y - ribbonY);
    float ribbonGlow = 1.0 - smoothstep(0.018, 0.095, ribbonDistance);
    float ribbonCore = 1.0 - smoothstep(0.002, 0.016, ribbonDistance);
    float3 ribbonColor = mix(tertiaryTone, primaryTone, 0.42);
    base = mix(base, ribbonColor, ribbonGlow * 0.52);
    base = mix(base, ribbonColor, ribbonCore * 0.45);

    float2 lensCenter = float2(0.86 + drift * 0.35, 0.52);
    float lensDistance = length(uv - lensCenter);
    float lensSignedDistance = lensDistance - 0.72;
    float lensHalo = 1.0 - smoothstep(0.006, 0.032, abs(lensSignedDistance));
    float lensCore = 1.0 - smoothstep(0.0, 0.0045, abs(lensSignedDistance));
    float lensInterior = 1.0 - smoothstep(-0.035, 0.035, lensSignedDistance);
    base = mix(base, tertiaryTone, lensHalo * 0.32);
    base = mix(base, tertiaryTone, lensCore * 0.40);
    base = mix(base, container, lensInterior * 0.18);

    float2 centered = (uv - 0.5) * float2(resolution.x / resolution.y, 1.0);
    float vignette = smoothstep(0.35, 0.86, length(centered));
    base = mix(base, deepBase, vignette * 0.16);

    float header = 1.0 - smoothstep(0.10, 0.48, uv.y);
    base = mix(base, deepBase, header * 0.42);

    return half4(clamp(base, 0.0, 1.0), 0.88);
}
"""

/**
 * Sfondo fluido comune con rendering AGSL o fallback a gradiente radiale.
 */
@Composable
fun UniHeroFluidBackground(
    alpha: Float = 1f,
    palette: UniHeroCardPalette = rememberUniHeroCardPalette(),
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val baseColor = if (scheme.surfaceContainerLowest.luminance() <= scheme.inverseSurface.luminance()) {
        scheme.surfaceContainerLowest
    } else {
        scheme.inverseSurface
    }
    val movement = 0f
    val shader = remember {
        if (supportsUniAppRuntimeShader()) {
            try {
                RuntimeShader(UniHeroCardShader)
            } catch (error: Throwable) {
                logUniAppShaderError("Unable to create hero card AGSL shader", error)
                null
            }
        } else null
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha),
    ) {
        val shaderDrawn = shader?.let { runtimeShader ->
            try {
                runtimeShader.setFloatUniform("resolution", size.width, size.height)
                runtimeShader.setFloatUniform("time", movement * 6.28318f)
                runtimeShader.setColorUniform("primaryColor", palette.cool)
                runtimeShader.setColorUniform("secondaryColor", palette.violet)
                runtimeShader.setColorUniform("tertiaryColor", palette.warm)
                runtimeShader.setColorUniform("glowColor", palette.glow)
                runtimeShader.setColorUniform("baseColor", baseColor)
                drawRect(brush = ShaderBrush(runtimeShader.asComposeShader()))
                true
            } catch (error: Throwable) {
                logUniAppShaderError("Unable to draw hero card AGSL shader", error)
                false
            }
        } ?: false

        if (!shaderDrawn) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(lerp(palette.cool, baseColor, 0.42f), palette.violet, baseColor),
                    center = Offset(size.width * 0.3f, size.height * 0.2f),
                    radius = size.maxDimension * 0.9f,
                ),
            )
        }
    }
}

/**
 * Lenti di vetro ottico (Bubbles) per rifrazione fluida ad alta fedeltà.
 */
@Composable
fun BoxScope.UniHeroGlassLenses(
    artworkBackdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    UniHeroPureGlassLens(
        artworkBackdrop = artworkBackdrop,
        modifier = modifier
            .size(390.dp)
            .align(Alignment.TopStart)
            .offset(x = (-146).dp, y = (-76).dp),
    )
    UniHeroPureGlassLens(
        artworkBackdrop = artworkBackdrop,
        modifier = modifier
            .size(470.dp)
            .align(Alignment.BottomCenter)
            .offset(x = 112.dp, y = 228.dp),
    )
}

@Composable
fun UniHeroPureGlassLens(
    artworkBackdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    LiquidGlass(
        modifier = modifier,
        backdropState = artworkBackdrop,
        backdropPolicy = LiquidGlassBackdropPolicy.ExplicitFirst,
        shape = CircleShape,
        blurRadius = 3.dp,
        refractionHeight = 50.dp,
        refractionAmount = 44.dp,
        containerColor = Color.Transparent,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.28f, size.height * 0.20f),
                    radius = size.maxDimension * 0.94f,
                ),
            )
        }
    }
}

/**
 * Modalità di attivazione della rotazione 3D della card.
 */
enum class UniHeroFlipTrigger {
    /** Rotazione su semplice tap/click (utilizzata nel badge). */
    CLICK,
    /** Rotazione con pressione prolungata / long-press (utilizzata nell'update banner). */
    LONG_PRESS,
    /** Rotazione controllata esclusivamente dallo stato esterno. */
    MANUAL,
}

/**
 * Base unificata per card scenografiche con shader AGSL, lenti LiquidGlass e flip 3D.
 */
@Composable
fun UniHeroGlassCard(
    modifier: Modifier = Modifier,
    height: Dp = 480.dp,
    cornerRadius: Dp = 28.dp,
    shape: Shape = RoundedRectangle(cornerRadius),
    flipTrigger: UniHeroFlipTrigger = UniHeroFlipTrigger.LONG_PRESS,
    isFlipped: Boolean = false,
    onFlippedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    palette: UniHeroCardPalette = rememberUniHeroCardPalette(),
    backgroundContent: (@Composable BoxScope.(Backdrop) -> Unit)? = null,
    backContent: (@Composable BoxScope.() -> Unit)? = null,
    frontContent: @Composable BoxScope.() -> Unit,
) {
    var internalFlipped by remember { mutableStateOf(false) }
    val flipped = if (onFlippedChange != null) isFlipped else internalFlipped

    val setFlipped: (Boolean) -> Unit = { target ->
        if (onFlippedChange != null) {
            onFlippedChange(target)
        } else {
            internalFlipped = target
        }
    }

    val performance = LocalLiquidGlassPerformance.current
    val rotation by animateLiquidFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = remember(performance) { LiquidMotion.spatialSpring<Float>(performance) },
        label = "heroCardFlipRotation",
    )

    val cardArtworkBackdrop = rememberLayerBackdrop()

    val currentScheme = MaterialTheme.colorScheme
    val heroColorScheme = remember(currentScheme, palette) {
        currentScheme.copy(
            onSurface = Color.White,
            onSurfaceVariant = Color.White.copy(alpha = 0.78f),
            primary = palette.cool.heroTextAccent(),
            onPrimary = Color.Black,
            primaryContainer = palette.cool.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = palette.violet.heroTextAccent(),
            onSecondary = Color.Black,
            secondaryContainer = palette.violet.copy(alpha = 0.25f),
            onSecondaryContainer = Color.White,
            tertiary = palette.warm.heroTextAccent(),
            onTertiary = Color.Black,
            tertiaryContainer = palette.warm.copy(alpha = 0.25f),
            onTertiaryContainer = Color.White,
            surface = Color.White.copy(alpha = 0.12f),
            surfaceContainerHigh = Color.White.copy(alpha = 0.15f),
            surfaceContainerHighest = Color.White.copy(alpha = 0.22f),
            outline = Color.White.copy(alpha = 0.30f),
            outlineVariant = Color.White.copy(alpha = 0.18f),
        )
    }

    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        shape = shape,
        contentPadding = 0.dp,
        colors = LiquidCardDefaults.colors(containerColor = Color.Transparent),
    ) {
        MaterialTheme(colorScheme = heroColorScheme) {
            CompositionLocalProvider(
                LocalUniHeroCardPalette provides palette,
                LocalContentColor provides Color.White,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                .pointerInput(flipTrigger, flipped) {
                    detectTapGestures(
                        onLongPress = {
                            if (flipTrigger == UniHeroFlipTrigger.LONG_PRESS) {
                                setFlipped(!flipped)
                            }
                            onLongPress?.invoke()
                        },
                        onTap = {
                            if (flipped) {
                                // Toccando il retro si torna sempre al fronte
                                setFlipped(false)
                            } else {
                                if (flipTrigger == UniHeroFlipTrigger.CLICK) {
                                    setFlipped(true)
                                }
                                onClick?.invoke()
                            }
                        },
                    )
                },
        ) {
            if (backgroundContent != null) {
                backgroundContent(cardArtworkBackdrop)
            } else {
                // 1. Shader Fluido con LayerBackdrop
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .layerBackdrop(cardArtworkBackdrop),
                ) {
                    UniHeroFluidBackground(alpha = 0.98f, palette = palette)
                }

                // 2. Lenti ottiche in vetro
                UniHeroGlassLenses(cardArtworkBackdrop)
            }

            // 3. Facce della card in base alla rotazione
            if (rotation <= 90f) {
                Box(modifier = Modifier.fillMaxSize()) {
                    frontContent()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                ) {
                    backContent?.invoke(this)
                }
            }

            // 4. Bordo perimetrale di cristallo speculare
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.42f),
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.03f),
                            Color.White.copy(alpha = 0.28f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                    style = Stroke(width = 1.2.dp.toPx()),
                )
            }
        }
    }
}
}
}
