package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import uniapp.composeapp.generated.resources.*
import com.anto426.uniapp.ui.components.layout.UniHeroCard
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.models.TransportReservation
import com.kyant.backdrop.Backdrop

@Composable
fun ReservationDetailScreen(reservation: TransportReservation, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Reservation Hero
        UniHeroCard(
            backdropState = backdropState,
            eyebrow = stringResource(Res.string.ui_transport_reservation_detail).uppercase(),
            title = reservation.route,
            subtitle = "${reservation.date} • ${reservation.time}",
            icon = LiquidIcons.Calendar
        )

        // 2. High Fidelity Ticket Section
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(32.dp),
            contentPadding = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Status Badge
                LiquidBadge(
                    text = reservation.status.uppercase(),
                    containerColor = if (reservation.status == "Confermato") colorScheme.primary else colorScheme.secondary,
                    contentColor = Color.White,
                    backdropState = backdropState
                )

                // QR Code Visual
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .liquidGlass(
                            backdrop = backdropState,
                            shape = RoundedCornerShape(28.dp),
                            role = LiquidGlassRole.Surface,
                            containerColor = Color.White.copy(alpha = 0.08f)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LiquidIcons.Search,
                        contentDescription = "Reservation QR Code",
                        modifier = Modifier
                            .size(140.dp)
                            .graphicsLayer { alpha = 0.8f },
                        tint = colorScheme.onSurface
                    )
                }

                Text(
                    text = reservation.qrCodeData,
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        // 3. Info Group
        LiquidPreferenceGroup(title = "Viaggio", backdropState = backdropState) {
            LiquidPreferenceItem(
                title = "Linea",
                subtitle = reservation.route,
                icon = LiquidIcons.Star,
                backdropState = backdropState
            )
            LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = "Data e Ora",
                subtitle = "${reservation.date} alle ${reservation.time}",
                icon = LiquidIcons.Time,
                backdropState = backdropState
            )
        }

        Spacer(Modifier.height(16.dp))

        // 4. Action Button
        LiquidButton(
            text = "Annulla Prenotazione",
            onClick = { /* Cancel logic */ },
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Secondary,
            backdropState = backdropState
        )
    }
}
