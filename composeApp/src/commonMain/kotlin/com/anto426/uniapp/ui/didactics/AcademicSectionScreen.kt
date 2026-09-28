package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AcademicSection
import com.anto426.uniapp.didactics.presentation.AcademicSectionUiState
import com.anto426.uniapp.ui.components.layout.UniScreenLazyColumn
import com.anto426.uniapp.ui.didactics.components.AcademicContentCard
import com.anto426.unisdk.backend.model.ProfessorContentItem

@Composable
fun AcademicSectionScreen(
    uiState: AcademicSectionUiState,
    section: AcademicSection = AcademicSection.Teachings,
    onItemClick: (ProfessorContentItem) -> Unit,
) {
    val sectionIcon = when (section) {
        AcademicSection.Teachings -> LiquidIcons.MenuBook
        AcademicSection.Theses -> LiquidIcons.Assignment
        AcademicSection.Reports -> LiquidIcons.Edit
        AcademicSection.ExamRounds -> LiquidIcons.Calendar
    }

    UniScreenLazyColumn {
        itemsIndexed(
            items = uiState.visibleItems,
            key = { index, item -> "${item.id}|${item.code}|${item.title}|${item.date}|$index" },
        ) { _, item ->
            AcademicContentCard(
                item = item,
                icon = sectionIcon,
                onClick = { onItemClick(item) },
            )
        }
    }
}

