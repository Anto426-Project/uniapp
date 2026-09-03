package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.transport.TransportTicket
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.QrCodeMatrixCanvas
import com.anto426.uniapp.ui.didactics.components.UniAppBrandLogo
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TicketDetailScreen(
    ticket: TransportTicket,
    backdropState: Backdrop,
    onBook: () -> Unit,
) {
    val rawCode = "TKT-${ticket.id.uppercase()}"

    UniScreenColumn {
        // 1. Hero Ticket Card (Student Banner Structure with 3D Flip)
        UniHeroGlassCard(
            backdropState = backdropState,
            height = 370.dp,
            flipTrigger = UniHeroFlipTrigger.CLICK,
            frontContent = {
                TicketHeroFrontFace(
                    ticket = ticket,
                    backdropState = backdropState,
                )
            },
            backContent = {
                TicketHeroBackFace(
                    ticket = ticket,
                    rawCode = rawCode,
                )
            },
        )

        // 2. Action Button
        LiquidButton(
            text = stringResource(Res.string.ui_transport_book_ride),
            onClick = onBook,
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Primary,
            backdropState = backdropState,
        )
    }
}

@Composable
private fun TicketHeroFrontFace(
    ticket: TransportTicket,
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
        // Top Header
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
                            text = stringResource(Res.string.ui_transport_brand_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = stringResource(Res.string.ui_transport_ticket_title_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    Text(
                        text = ticket.type.ifBlank { stringResource(Res.string.ui_transport_shuttle_campus_default) },
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

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .liquidGlass(
                        backdrop = backdropState,
                        shape = Capsule(),
                        role = LiquidGlassRole.Control,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ticket.icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(38.dp),
                )
            }

            Text(
                text = ticket.price.ifBlank { stringResource(Res.string.ui_free_price) },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface,
                letterSpacing = (-0.5).sp,
            )

            LiquidBadge(
                text = ticket.validity,
                containerColor = colorScheme.primaryContainer,
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
                text = stringResource(Res.string.ui_transport_pass_flip_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun TicketHeroBackFace(
    ticket: TransportTicket,
    rawCode: String,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Header
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
                            text = stringResource(Res.string.ui_transport_brand_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = stringResource(Res.string.ui_transport_validation_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.ui_transport_scan_turnstile_hint),
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
                        imageVector = LiquidIcons.Info,
                        contentDescription = stringResource(Res.string.ui_info),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_transport_quick_front),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Center: Scannable Vector QR Code Canvas in rounded container + Monospace Code
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
                    codeValue = rawCode,
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

                Text(
                    text = stringResource(Res.string.ui_transport_ticket_back_instruction),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
        }

        // Bottom: Flip back note
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
                text = stringResource(Res.string.ui_transport_tap_return_front),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}
