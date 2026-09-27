package com.anto426.uniapp.model.news

import androidx.compose.runtime.Immutable
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType

data class NewsItem(
    val title: String,
    val description: String,
    val fullContent: String,
    val type: LiquidStatusType = LiquidStatusType.Info,
    val category: String = "",
    val publishedAt: String = "",
)
