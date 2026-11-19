package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.pickers.colorpicker.LiquidColorPicker
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteSelector
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.settings.presentation.ColorLabUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ColorLabScreen(
    uiState: ColorLabUiState,
    onColorSelected: (Color) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        LiquidPaletteSelector(
            options = UniAppInitialData.palettes,
            selectedIndex = UniAppInitialData.palettes.indexOfFirst { it.color == uiState.selectedColor },
            onSelectIndex = { index -> onColorSelected(UniAppInitialData.palettes[index].color) },
            title = stringResource(Res.string.ui_theme_selection_title),
            showBadge = true,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        LiquidColorPicker(
            selectedColor = uiState.selectedColor,
            onColorSelected = onColorSelected,
        )

        LiquidCard(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Info,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 32.dp,
                        iconSize = 17.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        shape = RoundedRectangle(9.dp),
                    ),
                )
                Text(
                    text = stringResource(Res.string.ui_colors_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
