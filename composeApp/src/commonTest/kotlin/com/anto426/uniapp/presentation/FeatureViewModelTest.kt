package com.anto426.uniapp.presentation

import com.anto426.uniapp.didactics.presentation.QuestionnairesViewModel
import com.anto426.uniapp.didactics.presentation.StatisticsViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.uniapp.ui.data.UiInitialData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureViewModelTest {
    @Test
    fun taxesArePartitionedOnceInPresentationState() {
        val viewModel = TaxesViewModel(UiInitialData.taxPayments)
        val state = viewModel.uiState.value

        assertTrue(state.pendingPayments.all { !it.isPaid })
        assertTrue(state.paidPayments.all { it.isPaid })
        assertEquals(UiInitialData.taxPayments.size, state.pendingPayments.size + state.paidPayments.size)
    }

    @Test
    fun questionnaireProgressIsDerivedFromCurrentDataset() {
        val viewModel = QuestionnairesViewModel(UiInitialData.questionnaires)
        val state = viewModel.uiState.value

        assertEquals(UiInitialData.questionnaires.size, state.totalCount)
        assertEquals(state.completed.size.toFloat() / state.totalCount, state.completedProgress)
    }

    @Test
    fun transportReservationsAreGroupedByDayOutsideTheScreen() {
        val viewModel = TransportViewModel(UiInitialData.myTransportReservations)
        val state = viewModel.uiState.value

        assertEquals(
            UiInitialData.myTransportReservations.size,
            state.days.sumOf { it.reservations.size },
        )
        assertEquals(state.days.map { it.date }.distinct(), state.days.map { it.date })
    }

    @Test
    fun selectingStatisticsTabPreservesChartData() {
        val viewModel = StatisticsViewModel()
        val initial = viewModel.uiState.value

        viewModel.selectTab(2)

        assertEquals(2, viewModel.uiState.value.selectedTabIndex)
        assertEquals(initial.gradeEntries, viewModel.uiState.value.gradeEntries)
        assertEquals(14, viewModel.uiState.value.totalExams)
    }
}
