package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.pickers.colorpicker.LiquidColorPicker
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteSelector
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.settings.presentation.ColorLabUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ColorLabScreen(
    uiState: ColorLabUiState,
    onColorSelected: (Color) -> Unit,
) {
    UniScreenColumn {
        // 1. Tonalità di base rapide tramite LiquidPaletteSelector
        LiquidPaletteSelector(
            options = UniAppInitialData.palettes,
            selectedIndex = UniAppInitialData.palettes.indexOfFirst { it.color == uiState.selectedColor },
            onSelectIndex = { index -> onColorSelected(UniAppInitialData.palettes[index].color) },
            title = stringResource(Res.string.ui_theme_selection_title),
            showBadge = true,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        // 2. Interactive Liquid Color Picker
        LiquidColorPicker(
            selectedColor = uiState.selectedColor,
            onColorSelected = onColorSelected,
        )

        // 3. Info Section
        LiquidPreferenceGroup {
            Text(
                text = stringResource(Res.string.ui_colors_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(16.dp),
                lineHeight = 20.sp,
            )
        }
    }
}
