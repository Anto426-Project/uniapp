package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.uniapp.ui.components.items.QuestionnaireItem
import com.anto426.uniapp.ui.data.UiInitialData
import com.anto426.uniapp.ui.models.QuestionnaireStatus
import com.kyant.backdrop.Backdrop

@Composable
fun QuestionnairesScreen(backdropState: Backdrop) {
    val questionnaires = UiInitialData.questionnaires

    UniScreenColumn {
        // 1. Progress Summary Hero
        LiquidCard(
            backdropState = backdropState,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            contentPadding = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "STATO VALUTAZIONI",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1 di 3 completati",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "33%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LiquidLinearProgressIndicator(
                    progress = 0.33f,
                    backdropState = backdropState
                )
                Text(
                    text = "La valutazione è obbligatoria per prenotare gli appelli.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LiquidSectionTitle(title = "Corsi da Valutare", subtitle = "Necessario per l'esame")

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            questionnaires.filter { it.status == QuestionnaireStatus.PENDING }.forEach { data ->
                QuestionnaireItem(data, backdropState)
            }
        }

        LiquidSectionTitle(title = "Completati", subtitle = "Storico valutazioni")

        Column(
            modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            questionnaires.filter { it.status == QuestionnaireStatus.COMPLETED }.forEach { data ->
                QuestionnaireItem(data, backdropState)
            }
        }
    }
}
