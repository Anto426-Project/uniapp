package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
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
import com.anto426.uniapp.android.R
import com.anto426.uniapp.android.ui.components.layout.UniHeroCard
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.models.TransportTicket
import com.kyant.backdrop.Backdrop

@Composable
fun TicketDetailScreen(ticket: TransportTicket, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Ticket Hero Card
        UniHeroCard(
            backdropState = backdropState,
            eyebrow = ticket.type.uppercase(),
            title = ticket.title,
            subtitle = ticket.validity,
            icon = ticket.icon
        )

        // 2. High Fidelity Ticket Section (The "Hard Core" part)
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
                // Price Tag
                LiquidBadge(
                    text = ticket.price,
                    containerColor = colorScheme.primary,
                    contentColor = Color.White,
                    backdropState = backdropState,
                    modifier = Modifier.size(width = 100.dp, height = 36.dp)
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
                        contentDescription = "Ticket QR Code",
                        modifier = Modifier
                            .size(140.dp)
                            .graphicsLayer { alpha = 0.8f },
                        tint = colorScheme.onSurface
                    )
                }

                Text(
                    text = stringResource(R.string.ui_verified_validity),
                    fontSize = 12.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3. Information Group
        LiquidPreferenceGroup(title = "Dettagli", backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_transport_price),
                subtitle = ticket.price,
                icon = LiquidIcons.Star,
                backdropState = backdropState
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_transport_validity),
                subtitle = ticket.validity,
                icon = LiquidIcons.Time,
                backdropState = backdropState
            )
        }

        Spacer(Modifier.height(16.dp))

        // 4. Action Button
        LiquidButton(
            text = stringResource(R.string.ui_transport_buy),
            onClick = { /* Buy logic */ },
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Primary,
            backdropState = backdropState
        )
    }
}
