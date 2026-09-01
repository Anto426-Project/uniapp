package com.anto426.uniapp.ui.components.items

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidIconBox
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.services.TaxPaymentData
import com.kyant.backdrop.Backdrop

@Composable
fun TaxPaymentItem(data: TaxPaymentData, backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = if (data.isPaid) colorScheme.primary else colorScheme.error
    val statusContainer = if (data.isPaid) colorScheme.primary.copy(alpha = 0.12f) else colorScheme.error.copy(alpha = 0.12f)
    val statusIcon = if (data.isPaid) LiquidIcons.Check else LiquidIcons.Time

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(22.dp),
        contentPadding = 16.dp,
        modifier = Modifier.graphicsLayer(clip = false),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Status Icon Indicator
            LiquidIconBox(
                icon = statusIcon,
                size = 40.dp,
                iconSize = 20.dp,
                containerColor = statusContainer,
                iconTint = statusColor,
                shape = RoundedCornerShape(12.dp),
            )

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = data.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = data.date,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    text = "IUV: ${data.iuv}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    letterSpacing = 0.3.sp,
                    maxLines = 1,
                )
            }

            // Amount and Badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = data.amount,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurface,
                    letterSpacing = (-0.4).sp,
                )
                LiquidBadge(
                    text = if (data.isPaid) stringResource(Res.string.ui_tax_paid_badge) else stringResource(Res.string.ui_tax_pending_badge),
                    containerColor = statusColor.copy(alpha = 0.14f),
                    contentColor = statusColor,
                    backdropState = backdropState,
                )
            }
        }
    }
}
