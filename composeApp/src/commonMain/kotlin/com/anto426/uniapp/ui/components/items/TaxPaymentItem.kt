package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.TaxPaymentData
import com.anto426.uniapp.services.presentation.formatTaxCurrencyLabel
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TaxPaymentItem(
    data: TaxPaymentData,
    showDivider: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val statusColor = if (data.isPaid) glassColors.success else glassColors.warning
    val statusContainer = statusColor.copy(alpha = 0.13f)
    val statusIcon = if (data.isPaid) LiquidIcons.Check else LiquidIcons.Time

    val formattedDate = when {
        data.date.isNotBlank() && data.date != "-" -> {
            stringResource(
                if (data.isPaid) Res.string.ui_taxes_paid_on else Res.string.ui_taxes_due_on,
                data.date,
            )
        }
        data.isPaid -> stringResource(Res.string.msg_versamento_completato)
        else -> stringResource(Res.string.ui_taxes_due_pending)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 40.dp,
                    iconSize = 20.dp,
                    containerColor = statusContainer,
                    shape = RoundedRectangle(12.dp),
                ),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (data.iuv.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(
                                color = colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedRectangle(6.dp),
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_iuv_prefix, data.iuv),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Text(
                text = formatTaxCurrencyLabel(data.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (data.isPaid) colorScheme.onSurface else statusColor,
                maxLines = 1,
            )
        }

        if (showDivider) {
            LiquidHorizontalDivider(
                modifier = Modifier.padding(start = 70.dp, end = 16.dp),
                color = colorScheme.outlineVariant.copy(alpha = 0.25f),
            )
        }
    }
}
