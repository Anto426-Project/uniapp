package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.transport.TransportTicket
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.transport.components.TicketHeroBackFace
import com.anto426.uniapp.ui.transport.components.TicketHeroFrontFace
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TicketDetailScreen(
    ticket: TransportTicket,
    onBook: () -> Unit,
) {
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
                )
            },
        )

        // 2. Ticket Details Group
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_transport_ticket_detail),
        ) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_transport_catalog_title),
                subtitle = ticket.title,
                icon = LiquidIcons.Star,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_transport_price),
                subtitle = ticket.price.ifBlank { stringResource(Res.string.ui_free_price) },
                icon = LiquidIcons.Info,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_transport_validity),
                subtitle = ticket.validity,
                icon = LiquidIcons.Calendar,
            )
            if (ticket.type.isNotBlank()) {
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_transport_tickets),
                    subtitle = ticket.type,
                    icon = LiquidIcons.Home,
                )
            }
        }

        // 3. Action Button
        LiquidButton(
            text = stringResource(Res.string.ui_transport_book_ride),
            onClick = onBook,
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Primary,
        )
    }
}
