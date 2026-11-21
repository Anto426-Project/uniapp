package com.anto426.uniapp.ui.components.cards

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
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
    val accent: Color = warm.heroStrongAccent(),
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
        accent = seed.heroStrongAccent(),
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
internal fun Color.heroTextAccent(): Color = ensureMinLuminance(0.72f)

/** Rich, saturated theme accent for focal display digits while remaining legible on dark hero surfaces. */
internal fun Color.heroStrongAccent(): Color {
    val (h, s, _) = toHsv()
    if (s < 0.08f) return Color(0xFFB4C5E4)
    return hsvToColor(
        hue = h,
        saturation = maxOf(s, 0.78f).coerceAtMost(0.95f),
        value = 1.0f,
    ).ensureMinLuminance(0.24f)
}

private fun Color.ensureMinLuminance(minLuminance: Float): Color {
    if (luminance() >= minLuminance) return this
    var low = 0f
    var high = 1f
    repeat(10) {
        val mid = (low + high) * 0.5f
        if (lerp(this, Color.White, mid).luminance() < minLuminance) low = mid else high = mid
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

half4 main(float2 p) {
    float2 uv = p / max(resolution, float2(1.0));
    float aspect = resolution.x / max(resolution.y, 1.0);
    float drift = sin(time * 0.42) * 0.012;

    float3 primary = primaryColor.rgb;
    float3 secondary = secondaryColor.rgb;
    float3 tertiary = tertiaryColor.rgb;
    float3 glow = glowColor.rgb;
    float3 deepBase = baseColor.rgb;

    float3 upperTop = mix(deepBase, secondary, 0.28);
    float3 upperMid = mix(deepBase, primary, 0.72);
    float3 upperEdge = mix(mix(primary, glow, 0.72), float3(1.0), 0.14);
    float3 lowerDeep = mix(deepBase, secondary, 0.42);

    // 1. Piano diagonale bicolore: parte alta calma verso taglio netto luminoso in tinta tema
    float diagY = 0.56 + 0.34 * uv.x + drift * 0.5;
    float gradT = clamp(uv.y / max(diagY, 0.05), 0.0, 1.0);
    float3 upperSky = mix(upperTop, upperMid, smoothstep(0.0, 0.58, gradT));
    upperSky = mix(upperSky, upperEdge, pow(gradT, 1.85));

    float diagMask = smoothstep(-0.004, 0.004, uv.y - diagY);
    float3 base = mix(upperSky, lowerDeep, diagMask);

    // 2. Singola cupola morbida in basso a sinistra coerente con la palette
    float2 domeDelta = (uv - float2(0.16 + drift, 1.02)) * float2(aspect, 1.0);
    float domeR = 0.50;
    float dDome = length(domeDelta) / domeR;
    float domeMask = 1.0 - smoothstep(0.985, 1.012, dDome);
    float3 domeCore = mix(deepBase, tertiary, 0.84);
    float3 domeRim = mix(mix(tertiary, glow, 0.68), float3(1.0), 0.16);
    float3 domeBody = mix(domeCore, domeRim, smoothstep(0.45, 0.98, dDome));
    base = mix(base, domeBody, domeMask);

    float domeHalo = (1.0 - smoothstep(1.0, 1.12, dDome)) * smoothstep(0.97, 1.0, dDome);
    base = mix(base, domeRim, domeHalo * 0.24);

    // Leggero oscuramento superiore per leggibilità ottimale di titoli e numeri
    float headerShade = 1.0 - smoothstep(0.06, 0.44, uv.y);
    base = mix(base, deepBase, headerShade * 0.32);

    return half4(clamp(base, 0.0, 1.0), 0.92);
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .drawWithCache {
                val cachedShaderBrush = shader?.let { runtimeShader ->
                    try {
                        runtimeShader.setFloatUniform("resolution", size.width, size.height)
                        runtimeShader.setFloatUniform("time", movement * 6.28318f)
                        runtimeShader.setColorUniform("primaryColor", palette.cool)
                        runtimeShader.setColorUniform("secondaryColor", palette.violet)
                        runtimeShader.setColorUniform("tertiaryColor", palette.warm)
                        runtimeShader.setColorUniform("glowColor", palette.glow)
                        runtimeShader.setColorUniform("baseColor", baseColor)
                        ShaderBrush(runtimeShader.asComposeShader())
                    } catch (error: Throwable) {
                        logUniAppShaderError("Unable to draw hero card AGSL shader", error)
                        null
                    }
                }

                if (cachedShaderBrush != null) {
                    onDrawBehind {
                        drawRect(brush = cachedShaderBrush)
                    }
                } else {
                    val baseFill = lerp(baseColor, palette.violet, 0.42f)
                    val upperTop = lerp(baseColor, palette.violet, 0.28f)
                    val upperEdge = lerp(lerp(palette.cool, palette.glow, 0.72f), Color.White, 0.14f)
                    val upperDiag = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, size.height * 0.90f)
                        lineTo(0f, size.height * 0.56f)
                        close()
                    }
                    val upperBrush = Brush.verticalGradient(
                        colors = listOf(upperTop, lerp(baseColor, palette.cool, 0.72f), upperEdge),
                        startY = 0f,
                        endY = size.height * 0.90f,
                    )
                    val domeCenter = Offset(size.width * 0.16f, size.height * 1.02f)
                    val domeRadius = size.minDimension * 0.50f
                    val domeBrush = Brush.radialGradient(
                        colors = listOf(lerp(baseColor, palette.warm, 0.84f), lerp(palette.glow, Color.White, 0.16f)),
                        center = domeCenter,
                        radius = domeRadius,
                    )
                    onDrawBehind {
                        drawRect(color = baseFill)
                        drawPath(path = upperDiag, brush = upperBrush)
                        drawCircle(brush = domeBrush, radius = domeRadius, center = domeCenter)
                    }
                }
            },
    )
}

/**
 * Singola lente di vetro ottico per rifrazione minimale ad alta fedeltà.
 */
@Composable
fun BoxScope.UniHeroGlassLenses(
    artworkBackdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    UniHeroPureGlassLens(
        artworkBackdrop = artworkBackdrop,
        modifier = modifier
            .size(340.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 88.dp, y = 104.dp),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val sheenBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.28f, size.height * 0.20f),
                        radius = size.maxDimension * 0.94f,
                    )
                    onDrawBehind { drawRect(brush = sheenBrush) }
                },
        )
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
    val flipOnTap = backContent != null && flipTrigger == UniHeroFlipTrigger.CLICK
    val flipOnLongPress = backContent != null && flipTrigger == UniHeroFlipTrigger.LONG_PRESS
    val tapCard: (() -> Unit)? = if (flipped || flipOnTap || onClick != null) {
        {
            if (flipped) {
                setFlipped(false)
            } else {
                if (flipOnTap) setFlipped(true)
                onClick?.invoke()
            }
        }
    } else {
        null
    }
    val longPressCard: (() -> Unit)? = if (flipOnLongPress || onLongPress != null) {
        {
            if (flipOnLongPress) setFlipped(!flipped)
            onLongPress?.invoke()
        }
    } else {
        null
    }

    val performance = LocalLiquidGlassPerformance.current
    val rotationState = animateLiquidFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = remember(performance) { LiquidMotion.spatialSpring<Float>(performance) },
        label = "heroCardFlipRotation",
    )
    // Only recompose once at the 90° midpoint instead of every animation tick.
    val showFront by remember(rotationState) { derivedStateOf { rotationState.value <= 90f } }

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
            .heightIn(min = height)
            .graphicsLayer {
                rotationY = rotationState.value
                cameraDistance = 14f * density
            },
        shape = shape,
        contentPadding = 0.dp,
        colors = LiquidCardDefaults.colors(containerColor = Color.Transparent),
        onClick = tapCard,
        onLongClick = longPressCard,
    ) {
        MaterialTheme(colorScheme = heroColorScheme) {
            CompositionLocalProvider(
                LocalUniHeroCardPalette provides palette,
                LocalContentColor provides Color.White,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = height)
                        .clip(shape),
                    propagateMinConstraints = true,
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

                // 2. Lenti ottiche in vetro (vincolate a matchParentSize per non espandere le card compatte)
                Box(modifier = Modifier.matchParentSize()) {
                    UniHeroGlassLenses(cardArtworkBackdrop)
                }
            }

            // 3. Facce della card in base alla rotazione
            if (showFront) {
                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = height),
                    propagateMinConstraints = true,
                ) {
                    frontContent()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = height)
                        .graphicsLayer { rotationY = 180f },
                    propagateMinConstraints = true,
                ) {
                    backContent?.invoke(this)
                }
            }

            // 4. Bordo perimetrale di cristallo speculare
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithCache {
                        val rimBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.42f),
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.03f),
                                Color.White.copy(alpha = 0.28f),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                        )
                        val radiusPx = cornerRadius.toPx()
                        val corner = CornerRadius(radiusPx, radiusPx)
                        val stroke = Stroke(width = 1.2.dp.toPx())
                        onDrawBehind {
                            drawRoundRect(
                                brush = rimBrush,
                                cornerRadius = corner,
                                style = stroke,
                            )
                        }
                    },
            )
        }
    }
}
}
}
