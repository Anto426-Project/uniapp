package com.anto426.uniapp.services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toTaxPayments
import com.anto426.uniapp.model.services.TaxPaymentData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaxesUiState(
    val pendingPayments: List<TaxPaymentData> = emptyList(),
    val paidPayments: List<TaxPaymentData> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class TaxesViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TaxesUiState())
    val uiState: StateFlow<TaxesUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val payments = dataSource.loadTaxes(force).toTaxPayments()
                mutableUiState.value =
                    TaxesUiState(
                        pendingPayments = payments.filterNot(TaxPaymentData::isPaid),
                        paidPayments = payments.filter(TaxPaymentData::isPaid),
                        loadState = if (payments.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                    )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare le tasse."),
                )
            }
        }
    }
}
