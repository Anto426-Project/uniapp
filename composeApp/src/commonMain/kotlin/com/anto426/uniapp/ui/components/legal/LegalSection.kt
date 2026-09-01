package com.anto426.uniapp.ui.components.legal

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidAccordionItem
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
import com.kyant.backdrop.Backdrop

@Composable
fun LegalSection(
    section: LegalSectionData,
    backdropState: Backdrop,
    defaultExpanded: Boolean = false,
    leadingIcon: ImageVector = LiquidIcons.Info
) {
    var isExpanded by remember { mutableStateOf(defaultExpanded) }

    LiquidAccordionItem(
        title = section.title,
        leadingIcon = leadingIcon,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        backdropState = backdropState
    ) {
        Text(
            text = section.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            lineHeight = 22.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}
