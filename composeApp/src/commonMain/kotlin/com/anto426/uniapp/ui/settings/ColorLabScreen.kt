package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.pickers.LiquidColorPicker
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
        // 1. Interactive Liquid Color Picker
        LiquidColorPicker(
            selectedColor = uiState.selectedColor,
            onColorSelected = onColorSelected,
        )

        // 2. Info Section
        LiquidPreferenceGroup {
            Text(
                text = stringResource(Res.string.ui_colors_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(16.dp),
                lineHeight = 20.sp,
            )
        }

        // Spaziatura per non coprire il contenuto con la floating dock bar
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(110.dp))
    }
}

