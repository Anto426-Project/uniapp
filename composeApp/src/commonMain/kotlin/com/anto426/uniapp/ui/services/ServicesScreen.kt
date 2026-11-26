package com.anto426.uniapp.ui.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.services.presentation.ServicesUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.items.ServiceRow
import com.anto426.uniapp.ui.components.items.serviceAccentColor
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniSection
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ServicesScreen(
    uiState: ServicesUiState,
    onNavigateToService: (String) -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        // 1. Servizi interni in griglia Bento 2x2 allineata ai widget Apple Home
        if (uiState.studentServices.isNotEmpty()) {
            UniSection(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                entranceOrder = 0,
            ) {
                uiState.studentServices
                    .chunked(2)
                    .forEach { ServiceRow(it, onNavigateToService) }
            }
        }

        // 2. Portali digitali esterni in Apple Inset Grouped List (raggio 24dp coerente)
        if (uiState.universityPortals.isNotEmpty()) {
            UniSection(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                entranceOrder = 1,
            ) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_services_portals_title),
                    subtitle = stringResource(Res.string.ui_services_portals_sub),
                )
                UniInsetGroup(cornerRadius = 24.dp, animateEntrance = false) {
                    uiState.universityPortals.forEachIndexed { index, portal ->
                        val navTarget = portal.id.ifBlank { portal.title }
                        val accent = serviceAccentColor(portal.id)
                        UniInsetRow(
                            title = portal.title,
                            subtitle = portal.subtitle,
                            icon = portal.icon,
                            iconTint = accent,
                            onClick = { onNavigateToService(navTarget) },
                            showDivider = index < uiState.universityPortals.lastIndex,
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.onSurface.copy(alpha = 0.06f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = LiquidIcons.ArrowForward,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                        modifier = Modifier
                                            .size(13.dp)
                                            .rotate(-45f),
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
