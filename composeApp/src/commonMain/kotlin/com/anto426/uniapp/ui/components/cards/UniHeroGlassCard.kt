package com.anto426.uniapp.ui.components.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
import com.anto426.liquidmonet.glass.LiquidGlass
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.uniapp.ui.components.banners.logUniAppShaderError
import com.anto426.uniapp.ui.components.banners.supportsUniAppRuntimeShader
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Palette cromatica condivisa per lo shader fluido hero delle card di UniApp.
 */
data class UniHeroCardPalette(
    val cool: Color,
    val violet: Color,
    val warm: Color,
    val glow: Color,
)

@Composable
fun rememberUniHeroCardPalette(): UniHeroCardPalette {
    val scheme = MaterialTheme.colorScheme
    return UniHeroCardPalette(
        cool = lerp(scheme.primary, Color(0xFF3D8DFF), 0.74f),
        violet = lerp(scheme.tertiary, Color(0xFFB85CFF), 0.64f),
        warm = lerp(scheme.error, Color(0xFFFF6D54), 0.46f),
        glow = lerp(scheme.secondary, Color(0xFFFFC857), 0.50f),
    )
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
layout(color) uniform float4 contentColor;

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
    float3 content = contentColor.rgb;

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
    val baseColor = if (isSystemInDarkTheme()) scheme.surfaceContainerLowest else scheme.inverseSurface
    val contentColor = if (isSystemInDarkTheme()) scheme.onSurface else scheme.inverseOnSurface
    val transition = rememberInfiniteTransition(label = "uniHeroBackground")
    val movement = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "uniHeroDrift",
    )
    val shader = remember {
        if (supportsUniAppRuntimeShader()) {
            try {
                RuntimeShader(UniHeroCardShader)
            } catch (error: Throwable) {
                logUniAppShaderError("Impossibile creare lo shader AGSL della card", error)
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
                runtimeShader.setFloatUniform("time", movement.value * 6.28318f)
                runtimeShader.setColorUniform("primaryColor", palette.cool)
                runtimeShader.setColorUniform("secondaryColor", palette.violet)
                runtimeShader.setColorUniform("tertiaryColor", palette.warm)
                runtimeShader.setColorUniform("glowColor", palette.glow)
                runtimeShader.setColorUniform("baseColor", baseColor)
                runtimeShader.setColorUniform("contentColor", contentColor)
                drawRect(brush = ShaderBrush(runtimeShader.asComposeShader()))
                true
            } catch (error: Throwable) {
                logUniAppShaderError("Errore durante il rendering dello shader AGSL", error)
                false
            }
        } ?: false

        if (!shaderDrawn) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(palette.cool, palette.violet, baseColor),
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
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
    height: Dp = 480.dp,
    shape: Shape = RoundedCornerShape(32.dp),
    flipTrigger: UniHeroFlipTrigger = UniHeroFlipTrigger.LONG_PRESS,
    isFlipped: Boolean = false,
    onFlippedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    palette: UniHeroCardPalette = rememberUniHeroCardPalette(),
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

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "heroCardFlipRotation",
    )

    val cardArtworkBackdrop = rememberLayerBackdrop()

    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        backdropState = backdropState,
        shape = shape,
        contentPadding = 0.dp,
        colors = LiquidCardDefaults.colors(containerColor = Color.Transparent),
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
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx(), 32.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx()),
                )
            }
        }
    }
}
