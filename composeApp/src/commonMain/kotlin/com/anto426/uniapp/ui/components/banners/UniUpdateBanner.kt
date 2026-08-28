package com.anto426.uniapp.ui.components.banners

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.layout.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.LiquidSwitcherTransition
import com.anto426.liquidmonet.glass.LiquidGlass
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.uniapp.ui.components.interactive.UniCelebration
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlin.math.roundToInt

enum class UpdateState {
    CHECKING,
    UP_TO_DATE,
    AVAILABLE,
    DOWNLOADING,
    VERIFYING,
    INSTALLING,
    RESTART_REQUIRED,
    ERROR
}

/**
 * Optical accents stay derived from Material tokens, while deliberately separating the hue of
 * each lens so overlapping glass reads as a new colour instead of one uniform tint.
 */
private data class UpdateBannerPalette(
    val cool: Color,
    val violet: Color,
    val warm: Color,
    val glow: Color
)

@Composable
private fun updateBannerPalette(): UpdateBannerPalette {
    val scheme = MaterialTheme.colorScheme
    return UpdateBannerPalette(
        cool = lerp(scheme.primary, Color(0xFF3D8DFF), 0.74f),
        violet = lerp(scheme.tertiary, Color(0xFFB85CFF), 0.64f),
        warm = lerp(scheme.error, Color(0xFFFF6D54), 0.46f),
        glow = lerp(scheme.secondary, Color(0xFFFFC857), 0.50f)
    )
}

@Composable
private fun updateBannerBaseColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) scheme.surfaceContainerLowest else scheme.inverseSurface
}

@Composable
private fun updateBannerContentColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) scheme.onSurface else scheme.inverseOnSurface
}

@Composable
fun UniAppUpdateBanner(
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
    state: UpdateState = UpdateState.UP_TO_DATE,
    version: String = "2.0",
    progress: Float = 0f,
    downloadedMb: Float = 0f,
    totalMb: Float = 0f,
    onDownload: () -> Unit = {},
    onRestart: () -> Unit = {},
    onRetry: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    title: String = "UniApp",
    subtitle: String = "Università degli Studi del Molise",
    statusText: String? = null,
    channel: String? = null
) {
    var memorialVisible by remember { mutableStateOf(false) }
    val bannerContentColor = updateBannerContentColor()
    val artworkBackdrop = rememberLayerBackdrop()

    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .height(580.dp),
        backdropState = backdropState,
        shape = RoundedCornerShape(32.dp),
        contentPadding = 0.dp,
        containerColor = Color.Transparent,
        onClick = onClick,
        interactiveGelatin = onClick != null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .layerBackdrop(artworkBackdrop)
            ) {
                UniAppAgslBackground(alpha = 0.98f, onLongPress = { memorialVisible = true })
            }

            UpdateBannerBubbles(artworkBackdrop)

            LiquidAnimatedSwitcher(
                targetState = state,
                modifier = Modifier.fillMaxSize(),
                transition = LiquidSwitcherTransition.LiquidMorph,
                isForward = { initialState, targetState ->
                    targetState.ordinal >= initialState.ordinal
                },
                label = "UpdateBannerContent"
            ) { currentState ->
                when (currentState) {
                    UpdateState.CHECKING -> CheckingContent()
                    UpdateState.UP_TO_DATE -> UpToDateContent(
                        version = version,
                        title = title,
                        subtitle = subtitle,
                        statusText = statusText ?: "Versione aggiornata"
                    )
                    UpdateState.AVAILABLE -> AvailableContent(
                        version = version,
                        title = title,
                        subtitle = statusText ?: "Nuovo aggiornamento disponibile",
                        backdropState = backdropState,
                        onDownload = onDownload
                    )
                    UpdateState.DOWNLOADING -> DownloadingContent(
                        version = version,
                        progress = progress,
                        downloadedMb = downloadedMb,
                        totalMb = totalMb,
                        backdropState = backdropState
                    )
                    UpdateState.VERIFYING -> VerifyingContent()
                    UpdateState.INSTALLING -> InstallingContent(
                        progress = progress,
                        backdropState = backdropState
                    )
                    UpdateState.RESTART_REQUIRED -> RestartContent(
                        onRestart = onRestart,
                        backdropState = backdropState
                    )
                    UpdateState.ERROR -> ErrorContent(
                        onRetry = onRetry,
                        backdropState = backdropState
                    )
                }
            }

            channel?.takeIf { it.isNotBlank() }?.let { selectedChannel ->
                Text(
                    text = "CANALE ${selectedChannel.uppercase()}",
                    color = bannerContentColor.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(18.dp)
                )
            }

            AnimatedVisibility(
                visible = memorialVisible,
                enter = fadeIn(animationSpec = tween(800)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                UniCelebration(onClose = { memorialVisible = false })
            }
        }
    }
}

@Composable
private fun BoxScope.UpdateBannerBubbles(artworkBackdrop: Backdrop) {
    // The artwork has one continuous coloured backdrop and only two clear glass lenses. Their
    // colour comes from refracting that backdrop, including where the lenses overlap.
    UpdateBannerPureGlassLens(
        artworkBackdrop = artworkBackdrop,
        modifier = Modifier
            .size(390.dp)
            .align(Alignment.TopStart)
            .offset(x = (-146).dp, y = (-76).dp)
    )
    UpdateBannerPureGlassLens(
        artworkBackdrop = artworkBackdrop,
        modifier = Modifier
            .size(470.dp)
            .align(Alignment.BottomCenter)
            .offset(x = 112.dp, y = 228.dp)
    )
}

@Composable
private fun UpdateBannerPureGlassLens(
    artworkBackdrop: Backdrop,
    modifier: Modifier
) {
    LiquidGlass(
        modifier = modifier,
        backdropState = artworkBackdrop,
        backdropPolicy = LiquidGlassBackdropPolicy.ExplicitFirst,
        shape = CircleShape,
        blurRadius = 3.dp,
        refractionHeight = 50.dp,
        refractionAmount = 44.dp,
        containerColor = Color.White.copy(alpha = 0.12f)
    ) {
        // Almost colourless highlight: this is a clear lens, not a coloured disc.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.38f),
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.28f, size.height * 0.20f),
                    radius = size.maxDimension * 0.94f
                )
            )
        }
    }
}

private const val UniAppCardShader = """
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
    // Strong, broad colour fields reproduce the flowing red/blue/violet composition rather
    // than leaving a mostly neutral card under the lenses.
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
    base = mix(base, content, lowerRim * 0.28);

    float ribbonY = 0.67 - 0.11 * sin(uv.x * 2.75 + 0.32 + time * 0.035) - uv.x * 0.07;
    float ribbonDistance = abs(uv.y - ribbonY);
    float ribbonGlow = 1.0 - smoothstep(0.018, 0.095, ribbonDistance);
    float ribbonCore = 1.0 - smoothstep(0.002, 0.016, ribbonDistance);
    float3 ribbonColor = mix(tertiaryTone, primaryTone, 0.42);
    base = mix(base, ribbonColor, ribbonGlow * 0.52);
    base = mix(base, content, ribbonCore * 0.58);

    float2 lensCenter = float2(0.86 + drift * 0.35, 0.52);
    float lensDistance = length(uv - lensCenter);
    float lensSignedDistance = lensDistance - 0.72;
    float lensHalo = 1.0 - smoothstep(0.006, 0.032, abs(lensSignedDistance));
    float lensCore = 1.0 - smoothstep(0.0, 0.0045, abs(lensSignedDistance));
    float lensInterior = 1.0 - smoothstep(-0.035, 0.035, lensSignedDistance);
    base = mix(base, tertiaryTone, lensHalo * 0.42);
    base = mix(base, content, lensCore * 0.66);
    base = mix(base, container, lensInterior * 0.18);

    float2 centered = (uv - 0.5) * float2(resolution.x / resolution.y, 1.0);
    float vignette = smoothstep(0.35, 0.86, length(centered));
    base = mix(base, deepBase, vignette * 0.16);

    return half4(clamp(base, 0.0, 1.0), 0.88);
}
"""

@Composable
private fun UniAppAgslBackground(
    alpha: Float = 1f,
    onLongPress: (() -> Unit)?
) {
    val palette = updateBannerPalette()
    val baseColor = updateBannerBaseColor()
    val contentColor = updateBannerContentColor()
    val transition = rememberInfiniteTransition(label = "updateBannerBackground")
    val movement = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "updateBannerDrift"
    )
    val shader = remember {
        if (supportsUniAppRuntimeShader()) {
            try {
                RuntimeShader(UniAppCardShader)
            } catch (error: Throwable) {
                logUniAppShaderError("Impossibile creare lo shader AGSL della card", error)
                null
            }
        } else null
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .then(
                onLongPress?.let { callback ->
                    Modifier.pointerInput(callback) {
                        detectTapGestures(onLongPress = { callback() })
                    }
                } ?: Modifier
            )
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
                logUniAppShaderError("Errore durante il rendering dello shader AGSL della card", error)
                false
            }
        } ?: false
        if (!shaderDrawn) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(baseColor, palette.cool, palette.violet, palette.warm, palette.glow),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                alpha = 0.88f
            )
        }
    }
}

@Composable
private fun AvailableContent(
    version: String,
    title: String,
    subtitle: String,
    backdropState: Backdrop,
    onDownload: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, bottom = 48.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VersionText(version = version, fontSize = 104)
            Text(
                text = title,
                color = scheme.onSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = subtitle,
                color = scheme.onSurface.copy(alpha = 0.72f),
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            LiquidButton(
                text = "Scarica",
                onClick = onDownload,
                variant = LiquidButtonVariant.Glass,
                backdropState = backdropState,
                modifier = Modifier.width(190.dp)
            )
        }
    }
}

@Composable
private fun CheckingContent() {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition()
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1700, easing = LinearEasing))
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 155.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(84.dp)) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, scheme.primary.copy(alpha = 0.30f), scheme.primary)
                ),
                startAngle = rotation,
                sweepAngle = 275f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Ricerca aggiornamenti", color = scheme.onSurface, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Controllo della versione più recente…",
            color = scheme.onSurface.copy(alpha = 0.62f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun UpToDateContent(
    version: String,
    title: String,
    subtitle: String,
    statusText: String
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp, bottom = 56.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            VersionText(version = version, fontSize = 140)
            Text(
                text = title,
                color = scheme.onSurface,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = subtitle,
                color = scheme.onSurface.copy(alpha = 0.72f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = statusText,
            color = scheme.onSurface.copy(alpha = 0.66f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DownloadingContent(
    version: String,
    progress: Float,
    downloadedMb: Float,
    totalMb: Float,
    backdropState: Backdrop
) {
    val scheme = MaterialTheme.colorScheme
    val percent = (progress.coerceIn(0f, 1f) * 100).roundToInt()

    Column(
        modifier = Modifier.fillMaxSize().padding(start = 48.dp, end = 48.dp, top = 105.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VersionText(version = version, fontSize = 70)
        Text(text = "Download in corso", color = scheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(35.dp))
        LiquidLinearProgressIndicator(
            progress = progress,
            backdropState = backdropState
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(text = "$percent%", color = scheme.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "${downloadedMb} MB / ${totalMb} MB",
            color = scheme.onSurface.copy(alpha = 0.60f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun VerifyingContent() {
    CenterStatus(
        symbol = "◇",
        title = "Verifica aggiornamento",
        description = "Controllo dell'integrità del pacchetto…"
    )
}

@Composable
private fun InstallingContent(
    progress: Float,
    backdropState: Backdrop
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(start = 48.dp, end = 48.dp, top = 155.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Installazione", color = scheme.onSurface, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Non chiudere UniApp", color = scheme.onSurface.copy(alpha = 0.62f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(40.dp))
        LiquidLinearProgressIndicator(
            progress = progress,
            backdropState = backdropState
        )
    }
}

@Composable
private fun RestartContent(
    onRestart: () -> Unit,
    backdropState: Backdrop
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 130.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "✓", color = scheme.primary, fontSize = 62.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Aggiornamento pronto", color = scheme.onSurface, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = "Riavvia UniApp per completare", color = scheme.onSurface.copy(alpha = 0.62f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))
        LiquidButton(
            text = "Riavvia",
            onClick = onRestart,
            variant = LiquidButtonVariant.Glass,
            backdropState = backdropState,
            modifier = Modifier.width(190.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    backdropState: Backdrop
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 145.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "!", color = scheme.error, fontSize = 65.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Impossibile aggiornare", color = scheme.onSurface, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = "Controlla la connessione e riprova",
            color = scheme.onSurface.copy(alpha = 0.60f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(30.dp))
        LiquidButton(
            text = "Riprova",
            onClick = onRetry,
            variant = LiquidButtonVariant.Glass,
            backdropState = backdropState,
            modifier = Modifier.width(190.dp)
        )
    }
}

@Composable
private fun VersionText(
    version: String,
    fontSize: Int = 88
) {
    val scheme = MaterialTheme.colorScheme
    val palette = updateBannerPalette()
    val firstDigitIndex = version.indexOfFirst { it.isDigit() }
    Text(
        text = buildAnnotatedString {
            version.forEachIndexed { index, character ->
                if (index == firstDigitIndex) {
                    withStyle(SpanStyle(color = palette.warm)) {
                        append(character)
                    }
                } else {
                    withStyle(SpanStyle(color = scheme.onSurface)) {
                        append(character)
                    }
                }
            }
        },
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-6).sp
    )
}

@Composable
private fun CenterStatus(
    symbol: String,
    title: String,
    description: String
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 145.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = symbol, color = scheme.primary, fontSize = 60.sp)
        Spacer(modifier = Modifier.height(23.dp))
        Text(text = title, color = scheme.onSurface, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = description, color = scheme.onSurface.copy(alpha = 0.60f), fontSize = 14.sp)
    }
}
