package com.anto426.uniapp.ui.services.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TaxesHeroMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal data class ContactDetailField(
    val label: StringResource,
    val value: String,
    val icon: ImageVector,
    val uri: String? = null,
)

@Composable
internal fun ContactDetailGroup(
    title: StringResource,
    fields: List<ContactDetailField>,
    entranceOrder: Int = 0,
    onOpenUri: (String) -> Unit = {},
) {
    UniInsetGroup(
        title = stringResource(title),
        entranceOrder = entranceOrder,
    ) {
        fields.forEachIndexed { index, field ->
            UniInsetRow(
                title = stringResource(field.label),
                subtitle = field.value,
                icon = field.icon,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = field.uri?.let { uri -> { onOpenUri(uri) } },
                showDivider = index < fields.lastIndex,
            )
        }
    }
}
