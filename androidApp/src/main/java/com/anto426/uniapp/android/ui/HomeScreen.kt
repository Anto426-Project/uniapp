package com.anto426.uniapp.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anto426.antoui.components.cards.AntoCard
import com.anto426.antoui.components.cards.AntoAlertCard
import com.anto426.antoui.components.cards.AntoAlertType
import com.anto426.antoui.components.cards.AntoPreferenceItem
import com.anto426.antoui.components.cards.AntoStatusCard
import com.anto426.antoui.components.cards.AntoStatusType
import com.anto426.antoui.components.display.AntoAvatar
import com.anto426.antoui.components.display.AntoAvatarPresence
import com.anto426.antoui.components.display.AntoBadge
import com.anto426.antoui.components.feedback.AntoLinearProgressIndicator
import com.anto426.antoui.components.selection.AntoChip
import com.anto426.antoui.icons.AntoIcons
import com.kyant.backdrop.Backdrop

@Composable
fun HomeScreen(backdropState: Backdrop, onOpenCareer: () -> Unit) {
    UniScreenColumn {
        AntoCard(backdropState = backdropState) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    AntoAvatar(initials = "AM", presence = AntoAvatarPresence.Online, backdropState = backdropState)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Buongiorno, Antonio", color = Color.White)
                        Text("Ecco il tuo ritmo di oggi", color = Color.White.copy(alpha = .68f))
                    }
                    AntoBadge(text = "3", backdropState = backdropState)
                }
                Text("90/110", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
                Text("La tua carriera sta prendendo forma.", color = Color.White.copy(alpha = .72f))
                AntoLinearProgressIndicator(progress = .74f, backdropState = backdropState)
                Text("74% del percorso completato", color = Color.White.copy(alpha = .72f), style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            }
        }
        AntoStatusCard(
            title = "Prossima scadenza",
            description = "Tassa universitaria · EUR 395,33 entro il 29/05/2026",
            statusType = AntoStatusType.Warning,
            backdropState = backdropState
        )
        AntoAlertCard(
            title = "Appelli",
            message = "Nessun appello aperto",
            type = AntoAlertType.Info,
            supportingText = "Prossime date da consultare",
            actionLabel = "Apri appelli",
            onAction = {},
            backdropState = backdropState
        )
        AntoAlertCard(
            title = "Tasse",
            message = "1 pagamento da controllare",
            type = AntoAlertType.Warning,
            supportingText = "Scadenza 29/05/2026",
            actionLabel = "Controlla tasse",
            onAction = {},
            backdropState = backdropState
        )
        UniSectionTitle("Accesso rapido")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AntoChip("Focus oggi", onClick = {}, selected = true, leadingIcon = AntoIcons.Star, backdropState = backdropState)
            AntoChip("In scadenza", onClick = {}, leadingIcon = AntoIcons.Info, backdropState = backdropState)
        }
        AntoPreferenceItem("Carriera", subtitle = "Esami, crediti e media", icon = AntoIcons.Calendar, onClick = onOpenCareer, backdropState = backdropState)
        AntoPreferenceItem("Appelli", subtitle = "Consulta le prossime date", icon = AntoIcons.Star, backdropState = backdropState)
    }
}
