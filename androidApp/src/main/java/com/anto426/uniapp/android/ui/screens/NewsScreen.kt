package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.feedback.LiquidSheet
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTabBar
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.components.items.NewsList
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.anto426.uniapp.android.ui.models.NewsItem
import com.kyant.backdrop.Backdrop

@Composable
fun NewsScreen(backdropState: Backdrop) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedNews by remember { mutableStateOf<NewsItem?>(null) }
    val tabs = listOf(
        LiquidNavigationItem(label = "Ateneo", icon = LiquidIcons.Home),
        LiquidNavigationItem(label = "Dipartimento", icon = LiquidIcons.Star),
        LiquidNavigationItem(label = "Eventi", icon = LiquidIcons.Calendar)
    )
    val newsByTab = listOf(UiInitialData.universityNews, UiInitialData.departmentNews, UiInitialData.eventNews)
    UniScreenColumn {
        LiquidTabBar(items = tabs, selectedIndex = selectedTab, onTabSelected = { selectedTab = it }, backdropState = backdropState)
        Spacer(Modifier.height(8.dp))
        NewsList(newsByTab[selectedTab], backdropState) { selectedNews = it }
    }
    selectedNews?.let { news ->
        LiquidSheet(onDismissRequest = { selectedNews = null }, title = news.title, subtitle = news.description, backdropState = backdropState) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(news.fullContent, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
            }
        }
    }
}
