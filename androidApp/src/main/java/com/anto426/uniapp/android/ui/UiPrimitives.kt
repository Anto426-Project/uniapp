package com.anto426.uniapp.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anto426.antoui.components.cards.AntoCard
import com.kyant.backdrop.Backdrop

@Composable
fun UniScreenColumn(content: @Composable () -> Unit) = Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
) { content() }

@Composable
fun UniHeroCard(backdropState: Backdrop, eyebrow: String, title: String, subtitle: String) {
    AntoCard(backdropState = backdropState) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(eyebrow.uppercase(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, color = Color.White.copy(alpha = .72f))
        }
    }
}

@Composable
fun UniSectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
        subtitle?.let { Text(it, color = Color.White.copy(alpha = .65f), style = MaterialTheme.typography.bodySmall) }
    }
}
