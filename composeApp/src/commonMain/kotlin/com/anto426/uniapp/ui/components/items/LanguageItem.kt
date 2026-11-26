package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.settings.LanguageInfo
import com.anto426.uniapp.ui.components.display.UniBlockDefaults
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguageItem(
    language: LanguageInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant

    UniInsetRow(
        title = stringResource(language.nameRes),
        subtitle = stringResource(language.regionRes),
        onClick = onClick,
        clickRole = Role.RadioButton,
        showDivider = showDivider,
        modifier = Modifier.semantics { selected = isSelected },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(UniBlockDefaults.IconContainerSize)
                    .clip(RoundedRectangle(UniBlockDefaults.IconCornerRadius))
                    .background(accentColor.copy(alpha = if (isSelected) 0.16f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = language.code.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
        },
        trailingContent = {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        },
    )
}
