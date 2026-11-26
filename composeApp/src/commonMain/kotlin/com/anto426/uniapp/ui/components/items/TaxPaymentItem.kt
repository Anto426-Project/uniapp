package com.anto426.uniapp.ui.components.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.TaxPaymentData
import com.anto426.uniapp.services.presentation.formatTaxCurrencyLabel
import com.anto426.uniapp.ui.components.display.UniInsetRow
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

    val subtitleText = if (data.iuv.isNotBlank()) {
        "$formattedDate • ${stringResource(Res.string.ui_iuv_prefix, data.iuv)}"
    } else {
        formattedDate
    }

    UniInsetRow(
        title = data.title,
        subtitle = subtitleText,
        icon = statusIcon,
        iconTint = statusColor,
        showDivider = showDivider,
        trailingContent = {
            Text(
                text = formatTaxCurrencyLabel(data.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (data.isPaid) colorScheme.onSurface else statusColor,
                maxLines = 1,
            )
        },
    )
}
