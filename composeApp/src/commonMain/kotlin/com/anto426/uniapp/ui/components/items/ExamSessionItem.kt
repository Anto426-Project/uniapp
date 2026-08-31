package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidAccordionItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.ExamSession
import com.kyant.backdrop.Backdrop

@Composable
fun ExamSessionItem(exam: ExamSession, backdropState: Backdrop) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    LiquidAccordionItem(
        title = exam.name,
        subtitle = "${exam.date} • ${exam.time}",
        leadingIcon = if (exam.isBooked) LiquidIcons.Check else LiquidIcons.Calendar,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        backdropState = backdropState
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LiquidAvatar(initials = exam.professor.split(" ").filter(String::isNotEmpty).map { it.first() }.joinToString("").take(2), size = 40.dp, backdropState = backdropState)
                Column {
                    Text("Professore", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                    Text(exam.professor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { ExamDetailRow("Tipo", exam.type, LiquidIcons.Edit) }
                    Box(Modifier.weight(1f)) { ExamDetailRow("Aula", exam.room, LiquidIcons.Info) }
                }
                ExamDetailRow("Finestra Prenotazioni", "${exam.bookingOpenDate} - ${exam.bookingCloseDate}", LiquidIcons.Time)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(LiquidIcons.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Studenti prenotati:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(exam.bookedUsersCount.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (exam.isBooked) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidButton(onClick = {}, text = "Aggiungi a Google Calendar", modifier = Modifier.fillMaxWidth(), variant = LiquidButtonVariant.Primary, size = LiquidButtonSize.Small, backdropState = backdropState)
                    LiquidButton(onClick = {}, text = "Annulla Prenotazione", modifier = Modifier.fillMaxWidth(), variant = LiquidButtonVariant.Secondary, size = LiquidButtonSize.Small, backdropState = backdropState)
                }
            } else {
                LiquidButton(onClick = {}, text = "Prenota Esame", modifier = Modifier.fillMaxWidth(), variant = LiquidButtonVariant.Primary, size = LiquidButtonSize.Small, backdropState = backdropState)
            }
        }
    }
}

@Composable
private fun ExamDetailRow(label: String, value: String, icon: ImageVector) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}
