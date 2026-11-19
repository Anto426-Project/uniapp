package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.services.ContactData
import com.kyant.shapes.RoundedRectangle

@Composable
fun ContactItem(
    contact: ContactData,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val quickChannelText = listOfNotNull(
        contact.phoneNumbers.firstOrNull(),
        contact.email.takeIf { it.isNotBlank() },
        contact.building.takeIf { it.isNotBlank() },
    ).joinToString(" • ")

    val actionIcon = when {
        contact.phoneNumbers.isNotEmpty() -> LiquidIcons.Phone
        contact.email.isNotBlank() -> LiquidIcons.Feedback
        else -> LiquidIcons.Info
    }

    LiquidCard(
        shape = RoundedRectangle(22.dp),
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LiquidAvatar(
                initials = contact.initials,
                size = 46.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (contact.role.isNotBlank()) {
                    Text(
                        text = contact.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (quickChannelText.isNotBlank()) {
                    Text(
                        text = quickChannelText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 36.dp,
                        iconSize = 18.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedRectangle(11.dp),
                    ),
                )
                Icon(
                    imageVector = LiquidIcons.ChevronRight,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
