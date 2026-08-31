package com.anto426.uniapp.model.news

import com.anto426.liquidmonet.components.cards.LiquidStatusType

data class NewsItem(
    val title: String,
    val description: String,
    val fullContent: String,
    val type: LiquidStatusType = LiquidStatusType.Info,
)
