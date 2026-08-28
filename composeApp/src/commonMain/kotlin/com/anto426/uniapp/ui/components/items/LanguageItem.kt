package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.models.LanguageInfo
import com.kyant.backdrop.Backdrop

@Composable
fun LanguageItem(
    language: LanguageInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    backdropState: Backdrop
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidPreferenceItem(
        title = language.name,
        subtitle = language.region,
        onClick = onClick,
        backdropState = backdropState,
        leadingContent = {
            LiquidAvatar(
                initials = language.code.uppercase(),
                size = 40.dp,
                backdropState = backdropState
            )
        },
        trailingContent = {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    )
}
