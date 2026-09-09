package com.anto426.uniapp.services.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toTaxPayments
import com.anto426.uniapp.model.services.TaxPaymentData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
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

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Taxes)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val payments = snapshot.require(UniAppDataRequests.Taxes).toTaxPayments()
            mutableUiState.value =
                TaxesUiState(
                    pendingPayments = payments.filterNot(TaxPaymentData::isPaid),
                    paidPayments = payments.filter(TaxPaymentData::isPaid),
                    loadState = if (payments.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_le_tasse)),
            )
        }

    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }
}
