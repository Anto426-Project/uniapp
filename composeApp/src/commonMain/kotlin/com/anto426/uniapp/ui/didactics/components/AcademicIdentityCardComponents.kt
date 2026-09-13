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
import androidx.compose.foundation.Image
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
import com.kyant.shapes.RoundedRectangle
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidCardDefaults
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.codes.CodeGenerator
import com.anto426.uniapp.codes.QrCodeStyle
import com.anto426.uniapp.didactics.presentation.AcademicIdentityUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.banners.logUniAppShaderError
import com.anto426.uniapp.ui.components.banners.supportsUniAppRuntimeShader
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.ui.components.cards.UniHeroCardPalette
import com.anto426.uniapp.ui.components.cards.UniHeroCardShader
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroFluidBackground
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.cards.UniHeroGlassLenses
import com.anto426.uniapp.ui.components.cards.rememberUniHeroCardPalette

/**
 * Shared academic identity card with interactive 180-degree 3D flip animation on tap.
 * Front: Student / Professor photo, full name, course/department, and matricola badge.
 * Back: Artistic, scannable QR code without displaying the encoded payload.
 */
@Composable
fun AcademicIdentityBannerCard(
    uiState: AcademicIdentityUiState,
    modifier: Modifier = Modifier,
) {
    val identity = com.anto426.uniapp.ui.components.account.accountDisplayIdentity(
        uiState.fullName, uiState.initials, uiState.photoData,
    )
    val displayState = uiState.copy(fullName = identity.name, photoData = identity.photo)
    UniHeroGlassCard(
        modifier = modifier,
        height = 370.dp,
        flipTrigger = UniHeroFlipTrigger.CLICK,
        frontContent = {
            AcademicBadgeArtwork(Modifier.fillMaxSize())
            AcademicIdentityFrontFace(
                uiState = displayState,
            )
        },
        backContent = {
            AcademicBadgeArtwork(Modifier.fillMaxSize())
            AcademicIdentityBackFace(
                uiState = displayState,
            )
        },
    )
}

/** Front side of the student/professor badge card. */
@Composable
private fun AcademicIdentityFrontFace(
    uiState: AcademicIdentityUiState,
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
                            text = stringResource(Res.string.ui_unipass_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = if (uiState.isProfessor) stringResource(Res.string.ui_role_teacher_upper) else stringResource(Res.string.ui_role_student_upper),
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
                        shape = RoundedRectangle(16.dp),
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
                        contentDescription = stringResource(Res.string.ui_qr_code),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_transport_quick_qr),
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
                contentDescription = stringResource(Res.string.ui_profile_picture),
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
                            text = stringResource(Res.string.ui_unipass_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = if (uiState.isProfessor) stringResource(Res.string.ui_role_teacher_upper) else stringResource(Res.string.ui_role_student_upper),
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
                        shape = RoundedRectangle(16.dp),
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
                        contentDescription = stringResource(Res.string.ui_profile),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_flip_photo),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        AcademicBadgeQr(uiState.badgeQrValue)

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
 * Optical accents derived from Material tokens (delegated to UniHeroCardPalette).
 */
typealias AcademicIdentityCardPalette = UniHeroCardPalette

@Composable
fun academicIdentityCardPalette(): AcademicIdentityCardPalette = rememberUniHeroCardPalette()

const val AcademicIdentityCardShader = UniHeroCardShader

@Composable
fun AcademicIdentityFluidBackground(
    alpha: Float = 1f,
) {
    UniHeroFluidBackground(alpha = alpha)
}

/**
 * Bubble Glass Lenses for Card refraction (delegated to UniHeroGlassLenses).
 */
@Composable
fun BoxScope.AcademicIdentityGlassLenses(artworkBackdrop: Backdrop) {
    UniHeroGlassLenses(artworkBackdrop = artworkBackdrop)
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

/** Renders the exact payload using the shared, standards-compliant code generator. */
@Composable
fun QrCodeMatrixCanvas(
    codeValue: String,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") color: Color = Color.Black,
    style: QrCodeStyle = QrCodeStyle.Classic,
) {
    val painter = remember(codeValue, style) {
        if (codeValue.isBlank()) null else runCatching { CodeGenerator().qrCode(codeValue, style) }.getOrNull()
    }
    if (painter != null) {
        Image(painter = painter, contentDescription = null, modifier = modifier)
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.ui_code_unavailable),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
