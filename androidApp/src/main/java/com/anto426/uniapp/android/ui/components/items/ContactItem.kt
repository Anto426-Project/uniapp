package com.anto426.uniapp.android.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.models.ContactData
import com.kyant.backdrop.Backdrop

@Composable
fun ContactItem(
    contact: ContactData,
    backdropState: Backdrop,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        interactiveGelatin = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LiquidAvatar(initials = contact.initials, size = 48.dp, backdropState = backdropState)
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, fontSize = 16.sp)
                Text(contact.role, color = colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Icon(LiquidIcons.Phone, contentDescription = null, tint = colorScheme.primary)
        }
    }
}
