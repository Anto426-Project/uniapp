package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AcademicSectionUiState
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.didactics.components.AcademicContentCard
import com.anto426.unisdk.backend.model.ProfessorContentItem

@Composable
fun AcademicSectionScreen(
    uiState: AcademicSectionUiState,
    onItemClick: (ProfessorContentItem) -> Unit,
) {
    UniScreenLazyColumn {
        items(
            items = uiState.visibleItems,
            key = { item -> listOf(item.id, item.code, item.title, item.date).joinToString("|") },
        ) { item ->
            AcademicContentCard(
                item = item,
                onClick = { onItemClick(item) },
            )
        }
    }
}

