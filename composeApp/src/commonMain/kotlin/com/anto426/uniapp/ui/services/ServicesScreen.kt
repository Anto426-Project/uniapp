package com.anto426.uniapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.uniapp.services.presentation.ServicesUiState
import com.anto426.uniapp.ui.components.items.ServiceRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniSection
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ServicesScreen(
    uiState: ServicesUiState,
    onNavigateToService: (String) -> Unit = {}
) {
    UniScreenColumn {
        // 1. Student / Professor Core Services (displayed at top without redundant header)
        if (uiState.studentServices.isNotEmpty()) {
            UniSection(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.studentServices
                    .chunked(2)
                    .forEach { ServiceRow(it, onNavigateToService) }
            }
        }

        // 2. University Digital Portals
        UniSection(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_services_portals_title),
                subtitle = stringResource(Res.string.ui_services_portals_sub),
            )
            uiState.universityPortals
                .chunked(2)
                .forEach { ServiceRow(it, onNavigateToService) }
        }
    }
}
