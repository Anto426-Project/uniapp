package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.services.presentation.ServicesUiState
import com.anto426.uniapp.ui.components.items.ServiceRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun ServicesScreen(
    backdropState: Backdrop,
    uiState: ServicesUiState,
    onNavigateToService: (String) -> Unit = {}
) {
    UniScreenColumn {
        // 1. Campus Services & Student Administration
        LiquidSectionHeader(
            title = "Campus e Mobilità",
            subtitle = "Trasporti, tasse e supporto studente"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.studentServices
                .chunked(2)
                .forEach { ServiceRow(it, backdropState, onNavigateToService) }
        }

        // 2. University Digital Portals
        LiquidSectionHeader(
            title = "Portali di Ateneo",
            subtitle = "Piattaforme digitali e servizi online"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiquidStatusCard(
                title = "Manutenzione Esse3",
                description = "Il portale sarà offline Domenica 30 dalle 08:00 alle 14:00.",
                statusType = LiquidStatusType.Warning,
                backdropState = backdropState
            )

            uiState.universityPortals
                .chunked(2)
                .forEach { ServiceRow(it, backdropState, onNavigateToService) }
        }
    }
}
