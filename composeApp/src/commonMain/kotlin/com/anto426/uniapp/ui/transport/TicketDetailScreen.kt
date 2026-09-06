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
import com.anto426.uniapp.ui.transport.components.TicketHeroBackFace
import com.anto426.uniapp.ui.transport.components.TicketHeroFrontFace
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TicketDetailScreen(
    ticket: TransportTicket,
    onBook: () -> Unit,
) {
    val rawCode = "TKT-${ticket.id.uppercase()}"

    UniScreenColumn {
        // 1. Hero Ticket Card (Student Banner Structure with 3D Flip)
        UniHeroGlassCard(
            height = 370.dp,
            flipTrigger = UniHeroFlipTrigger.CLICK,
            frontContent = {
                TicketHeroFrontFace(
                    ticket = ticket,
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
        )
    }
}
