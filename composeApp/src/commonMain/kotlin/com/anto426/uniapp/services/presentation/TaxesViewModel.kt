package com.anto426.uniapp.services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.TaxPaymentData
import com.anto426.uniapp.data.contract.formatTaxCurrencyLabel as contractFormatTaxCurrencyLabel
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.taxesContract
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

enum class TaxesFilter {
    ALL,
    PENDING,
    PAID,
}

data class TaxesUiState(
    val pendingPayments: List<TaxPaymentData> = emptyList(),
    val paidPayments: List<TaxPaymentData> = emptyList(),
    val dueAmountLabel: String = "€ 0.00",
    val paidTotalLabel: String = "€ 0.00",
    val paidProgress: Float = 0f,
    val nextDueDate: String? = null,
    val filter: TaxesFilter = TaxesFilter.ALL,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class TaxesViewModel(private val dataSource: UniGateway) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TaxesUiState())
    val uiState: StateFlow<TaxesUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Taxes)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val taxes = snapshot.taxesContract(requireTaxes = true)
            mutableUiState.value = TaxesUiState(
                pendingPayments = taxes.pendingPayments,
                paidPayments = taxes.paidPayments,
                dueAmountLabel = taxes.dueAmountLabel,
                paidTotalLabel = taxes.paidTotalLabel,
                paidProgress = taxes.paidProgress,
                nextDueDate = taxes.nextDueDate,
                filter = mutableUiState.value.filter,
                loadState = if (taxes.allPayments.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { getString(Res.string.msg_impossibile_caricare_le_tasse) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        if (mutableUiState.value.loadState == FeatureLoadState.Error) {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
        }
        sharedData.refresh(dataRequests, force)
    }

    fun selectFilter(filter: TaxesFilter) {
        mutableUiState.value = mutableUiState.value.copy(filter = filter)
    }
}

internal fun formatTaxCurrencyLabel(raw: String): String =
    contractFormatTaxCurrencyLabel(raw)
