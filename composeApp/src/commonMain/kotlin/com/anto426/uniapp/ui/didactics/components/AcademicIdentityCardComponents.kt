package com.anto426.uniapp.ui.didactics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.codes.CodeGenerator
import com.anto426.uniapp.codes.QrCodeStyle
import com.anto426.uniapp.didactics.presentation.AcademicIdentityUiState
import com.anto426.uniapp.ui.components.account.UniAccountAvatar
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).padding(end = 6.dp),
            ) {
                UniAppBrandLogo(modifier = Modifier.size(36.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_unipass_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.2).sp,
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
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Quick Flip Indicator Pill
            Box(
                modifier = Modifier
                    .clip(RoundedRectangle(16.dp))
                    .background(color = colorScheme.surface.copy(alpha = 0.24f))
                    .border(
                        width = 0.6.dp,
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedRectangle(16.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
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
                modifier = Modifier
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = colorScheme.primary.copy(alpha = 0.42f),
                        shape = CircleShape,
                    )
                    .padding(3.dp),
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
                    overflow = TextOverflow.Ellipsis,
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).padding(end = 6.dp),
            ) {
                UniAppBrandLogo(modifier = Modifier.size(36.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_unipass_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.2).sp,
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
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Flip Back Pill
            Box(
                modifier = Modifier
                    .clip(RoundedRectangle(16.dp))
                    .background(color = colorScheme.surface.copy(alpha = 0.24f))
                    .border(
                        width = 0.6.dp,
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedRectangle(16.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
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
                color = colorScheme.onSurface.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}

/**
 * Procedural UniApp Apple Spark U Brand Logo.
 */
@Composable
fun UniAppBrandLogo(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Canvas(modifier = modifier) {
        val side = size.minDimension
        val ox = (size.width - side) / 2f
        val oy = (size.height - side) / 2f
        val scale = side / 680f

        fun tx(x: Float): Float = ox + (x - 172f) * scale
        fun ty(y: Float): Float = oy + (y - 172f) * scale

        val uPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(tx(359.8f), ty(289.8f))
            cubicTo(tx(384.3f), ty(289.8f), tx(404.1f), ty(309.6f), tx(404.1f), ty(334.0f))
            lineTo(tx(404.1f), ty(568.6f))
            cubicTo(tx(404.1f), ty(625.2f), tx(446.1f), ty(662.7f), tx(500.4f), ty(662.7f))
            cubicTo(tx(554.7f), ty(662.7f), tx(596.7f), ty(625.2f), tx(596.7f), ty(568.6f))
            lineTo(tx(596.7f), ty(393.9f))
            cubicTo(tx(596.7f), ty(327.8f), tx(630.3f), ty(289.8f), tx(699.8f), ty(289.8f))
            cubicTo(tx(704.5f), ty(289.8f), tx(706.7f), ty(292.9f), tx(704.0f), ty(296.9f))
            cubicTo(tx(692.1f), ty(314.6f), tx(685.2f), ty(347.4f), tx(685.2f), ty(393.9f))
            lineTo(tx(685.2f), ty(568.6f))
            cubicTo(tx(685.2f), ty(673.4f), tx(605.1f), ty(747.8f), tx(500.4f), ty(747.8f))
            cubicTo(tx(395.7f), ty(747.8f), tx(315.6f), ty(673.4f), tx(315.6f), ty(568.6f))
            lineTo(tx(315.6f), ty(334.0f))
            cubicTo(tx(315.6f), ty(309.6f), tx(335.4f), ty(289.8f), tx(359.8f), ty(289.8f))
            close()
        }

        val sparkPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(tx(747.4f), ty(187.8f))
            cubicTo(tx(752.6f), ty(227.6f), tx(765.3f), ty(240.3f), tx(805.0f), ty(245.5f))
            cubicTo(tx(765.3f), ty(250.8f), tx(752.6f), ty(263.4f), tx(747.4f), ty(303.2f))
            cubicTo(tx(742.1f), ty(263.4f), tx(729.4f), ty(250.8f), tx(689.7f), ty(245.5f))
            cubicTo(tx(729.4f), ty(240.3f), tx(742.1f), ty(227.6f), tx(747.4f), ty(187.8f))
            close()
        }

        val brush = Brush.linearGradient(listOf(colorScheme.primary, colorScheme.tertiary))
        drawPath(path = uPath, brush = brush)
        drawPath(path = sparkPath, brush = brush)
    }
}

/** Renders the exact payload using the shared, standards-compliant code generator. */
@Composable
fun QrCodeMatrixCanvas(
    codeValue: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    style: QrCodeStyle = QrCodeStyle.Classic,
    accentColor: Color = color,
    backgroundColor: Color = Color.White,
) {
    val resolvedModule = if (style == QrCodeStyle.Classic && color == Color.Black) null else color
    val resolvedAccent = if (style == QrCodeStyle.Classic && accentColor == Color.Black) null else accentColor
    val painter = remember(codeValue, style, resolvedModule, resolvedAccent, backgroundColor) {
        if (codeValue.isBlank()) {
            null
        } else {
            runCatching {
                CodeGenerator().qrCode(
                    value = codeValue,
                    style = style,
                    moduleColor = resolvedModule,
                    accentColor = resolvedAccent,
                    backgroundColor = backgroundColor,
                )
            }.getOrNull()
        }
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
