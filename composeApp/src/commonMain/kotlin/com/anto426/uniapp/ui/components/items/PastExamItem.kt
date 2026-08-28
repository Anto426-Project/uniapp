package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidAccordionItem
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.models.PastExam
import com.kyant.backdrop.Backdrop

@Composable
fun PastExamItem(exam: PastExam, backdropState: Backdrop) {
    var isExpanded by remember { mutableStateOf(false) }
    LiquidAccordionItem(
        title = exam.name,
        subtitle = "${exam.date} • Voto: ${exam.grade}",
        leadingIcon = LiquidIcons.Check,
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        backdropState = backdropState
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(LiquidIcons.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text("Stato: ${exam.status}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
