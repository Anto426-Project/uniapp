package com.anto426.uniapp.ui.didactics.components

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
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.glass.LiquidGlass
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AcademicIdentityUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.banners.logUniAppShaderError
import com.anto426.uniapp.ui.components.banners.supportsUniAppRuntimeShader
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*
import kotlin.math.abs

/**
 * Shared academic identity card with interactive 180-degree 3D flip animation on tap.
 * Front: Student / Professor photo, full name, course/department, and matricola badge.
 * Back: Scannable vector QR code, digital identity code, and authenticity mark.
 */
@Composable
fun AcademicIdentityBannerCard(
    uiState: AcademicIdentityUiState,
    rawCode: String,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "badgeFlipRotation",
    )

    val cardShape = RoundedCornerShape(32.dp)
    val cardArtworkBackdrop = rememberLayerBackdrop()

    LiquidCard(
        modifier = modifier
            .fillMaxWidth()
            .height(370.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        backdropState = backdropState,
        shape = cardShape,
        contentPadding = 0.dp,
        containerColor = Color.Transparent,
        interactiveGelatin = false,
        onClick = { isFlipped = !isFlipped },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape),
        ) {
            // 1. Fluid Shader Background with LayerBackdrop
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .layerBackdrop(cardArtworkBackdrop),
            ) {
                AcademicIdentityFluidBackground(alpha = 0.98f)
            }

            // 2. Refractive Glass Lenses
            AcademicIdentityGlassLenses(cardArtworkBackdrop)

            // 3. Card Faces based on rotation angle
            if (rotation <= 90f) {
                AcademicIdentityFrontFace(
                    uiState = uiState,
                    backdropState = backdropState,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                ) {
                    AcademicIdentityBackFace(
                        uiState = uiState,
                        rawCode = rawCode,
                        backdropState = backdropState,
                    )
                }
            }
        }
    }
}

/** Front side of the student/professor badge card. */
@Composable
private fun AcademicIdentityFrontFace(
    uiState: AcademicIdentityUiState,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Header: UniApp Brand Logo, Badge Title & Flip Indicator Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                UniAppBrandLogo(modifier = Modifier.size(40.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "UniPass",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = if (uiState.isProfessor) "DOCENTE" else "STUDENTE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.ui_university),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }

            // Quick Flip Indicator Pill
            Box(
                modifier = Modifier
                    .background(
                        color = colorScheme.surface.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.QrCode,
                        contentDescription = "QR Code",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "QR ↻",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Academic profile: Avatar, Full Name, Degree/Department & Matricola badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UniAccountAvatar(
                imageData = uiState.photoData,
                initials = uiState.initials.ifBlank { if (uiState.isProfessor) "DO" else "ST" },
                size = 86.dp,
                contentDescription = "Foto profilo",
                backdropState = backdropState,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = uiState.fullName.ifBlank {
                        stringResource(
                            if (uiState.isProfessor) Res.string.ui_professor_role
                            else Res.string.ui_student_name_fallback,
                        )
                    }.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    maxLines = 2,
                )

                Text(
                    text = if (uiState.isProfessor) {
                        uiState.departmentName.ifBlank { stringResource(Res.string.ui_university) }
                    } else {
                        uiState.degreeName.ifBlank { "—" }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }

            LiquidBadge(
                text = if (uiState.isProfessor) {
                    stringResource(Res.string.ui_professor_role).uppercase()
                } else {
                    stringResource(
                        Res.string.ui_matricola_prefix,
                        uiState.matricola.ifBlank { "—" },
                    ).uppercase()
                },
                containerColor = colorScheme.primaryContainer.copy(alpha = 0.65f),
                contentColor = colorScheme.primary,
                backdropState = backdropState,
            )
        }

        // Bottom Tap to Flip Hint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LiquidIcons.Refresh,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.ui_badge_tap_to_flip_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}

/** Back side of the student/professor badge card. */
@Composable
private fun AcademicIdentityBackFace(
    uiState: AcademicIdentityUiState,
    rawCode: String,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Header: UniApp Brand Logo, Pass Title & Flip Back Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                UniAppBrandLogo(modifier = Modifier.size(40.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "UniPass",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = if (uiState.isProfessor) "DOCENTE" else "STUDENTE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.ui_badge_scan_gates_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }

            // Flip Back Pill
            Box(
                modifier = Modifier
                    .background(
                        color = colorScheme.surface.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.AccountCircle,
                        contentDescription = "Profilo",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Foto ↻",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Center: Scannable QR Code Canvas in rounded container + Monospace Code
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = colorScheme.surface.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                QrCodeMatrixCanvas(
                    codeValue = rawCode.ifBlank { uiState.matricola.ifBlank { "UNIAPP-ID" } },
                    color = colorScheme.onSurface,
                    modifier = Modifier.size(136.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = rawCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurface,
                    letterSpacing = 1.8.sp,
                )

                if (uiState.fullName.isNotBlank()) {
                    Text(
                        text = uiState.fullName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1,
                    )
                }
            }
        }

        // Bottom: Validity verified note
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LiquidIcons.Check,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.ui_badge_tap_to_flip_back_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}

/**
 * Optical accents derived from Material tokens.
 */
data class AcademicIdentityCardPalette(
    val cool: Color,
    val violet: Color,
    val warm: Color,
    val glow: Color,
)

@Composable
fun academicIdentityCardPalette(): AcademicIdentityCardPalette {
    val scheme = MaterialTheme.colorScheme
    return AcademicIdentityCardPalette(
        cool = lerp(scheme.primary, Color(0xFF3D8DFF), 0.74f),
        violet = lerp(scheme.tertiary, Color(0xFFB85CFF), 0.64f),
        warm = lerp(scheme.error, Color(0xFFFF6D54), 0.46f),
        glow = lerp(scheme.secondary, Color(0xFFFFC857), 0.50f),
    )
}

const val AcademicIdentityCardShader = """
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

@Composable
fun AcademicIdentityFluidBackground(
    alpha: Float = 1f,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = academicIdentityCardPalette()
    val baseColor = if (isSystemInDarkTheme()) scheme.surfaceContainerLowest else scheme.inverseSurface
    val contentColor = if (isSystemInDarkTheme()) scheme.onSurface else scheme.inverseOnSurface
    val transition = rememberInfiniteTransition(label = "studentBannerBackground")
    val movement = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "studentBannerDrift",
    )
    val shader = remember {
        if (supportsUniAppRuntimeShader()) {
            try {
                RuntimeShader(AcademicIdentityCardShader)
            } catch (error: Throwable) {
                logUniAppShaderError("Impossibile creare lo shader AGSL della card", error)
                null
            }
        } else null
    }

    Canvas(
        modifier = Modifier
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
 * Bubble Glass Lenses for Card refraction matching Update Banner.
 */
@Composable
fun BoxScope.AcademicIdentityGlassLenses(artworkBackdrop: Backdrop) {
    AcademicIdentityPureGlassLens(
        artworkBackdrop = artworkBackdrop,
        modifier = Modifier
            .size(390.dp)
            .align(Alignment.TopStart)
            .offset(x = (-146).dp, y = (-76).dp),
    )
    AcademicIdentityPureGlassLens(
        artworkBackdrop = artworkBackdrop,
        modifier = Modifier
            .size(470.dp)
            .align(Alignment.BottomCenter)
            .offset(x = 112.dp, y = 228.dp),
    )
}

@Composable
private fun AcademicIdentityPureGlassLens(
    artworkBackdrop: Backdrop,
    modifier: Modifier,
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
 * Procedural UniApp Modern Brand Logo (Stylized glass U emblem).
 */
@Composable
fun UniAppBrandLogo(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Canvas(modifier = modifier) {
        val strokeWidth = 3.5.dp.toPx()
        val w = size.width
        val h = size.height

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.20f, h * 0.15f)
            lineTo(w * 0.20f, h * 0.55f)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = w * 0.20f,
                    top = h * 0.30f,
                    right = w * 0.80f,
                    bottom = h * 0.85f,
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )
            lineTo(w * 0.80f, h * 0.15f)
        }

        drawPath(
            path = path,
            brush = Brush.linearGradient(listOf(colorScheme.primary, colorScheme.tertiary)),
            style = Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            ),
        )
    }
}

/**
 * Procedural Vector QR Code Matrix drawing canvas (Transparent background, vector color).
 */
@Composable
fun QrCodeMatrixCanvas(
    codeValue: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val hash = codeValue.hashCode()

    Canvas(modifier = modifier) {
        val matrixSize = 25
        val cellSize = size.width / matrixSize

        // 1. Draw 3 Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(0f, 0f, cellSize, color)
        drawFinderPattern((matrixSize - 7) * cellSize, 0f, cellSize, color)
        drawFinderPattern(0f, (matrixSize - 7) * cellSize, cellSize, color)

        // 2. Draw Data Modules deterministically
        for (row in 0 until matrixSize) {
            for (col in 0 until matrixSize) {
                val isFinderArea = (row < 8 && col < 8) ||
                    (row < 8 && col >= matrixSize - 8) ||
                    (row >= matrixSize - 8 && col < 8)

                if (!isFinderArea) {
                    val pseudoRandom = abs((hash xor (row * 31 + col * 17) xor (row * col)).hashCode()) % 3
                    if (pseudoRandom != 0 || (row % 2 == 0 && col % 3 == 0)) {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(col * cellSize + cellSize * 0.05f, row * cellSize + cellSize * 0.05f),
                            size = Size(cellSize * 0.90f, cellSize * 0.90f),
                            cornerRadius = CornerRadius(cellSize * 0.25f),
                        )
                    }
                }
            }
        }
    }
}

fun DrawScope.drawFinderPattern(
    x: Float,
    y: Float,
    cellSize: Float,
    color: Color,
) {
    val strokeWidth = cellSize * 0.95f
    // Outer 7x7 rounded ring (Stroke width = 1 cell)
    drawRoundRect(
        color = color,
        topLeft = Offset(x + strokeWidth / 2f, y + strokeWidth / 2f),
        size = Size(7 * cellSize - strokeWidth, 7 * cellSize - strokeWidth),
        cornerRadius = CornerRadius(cellSize * 1.5f),
        style = Stroke(width = strokeWidth),
    )
    // Center 3x3 solid square
    drawRoundRect(
        color = color,
        topLeft = Offset(x + 2 * cellSize, y + 2 * cellSize),
        size = Size(3 * cellSize, 3 * cellSize),
        cornerRadius = CornerRadius(cellSize * 0.8f),
    )
}
