package com.anto426.uniapp.services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.toTaxPayments
import com.anto426.uniapp.model.didactics.toFixedTwoDecimals
import com.anto426.uniapp.model.services.TaxPaymentData
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

class TaxesViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TaxesUiState())
    val uiState: StateFlow<TaxesUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Taxes)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val taxes = snapshot.require(UniAppDataRequests.Taxes)
            val payments = taxes.toTaxPayments()
            val pending = payments.filterNot(TaxPaymentData::isPaid)
            val paid = payments.filter(TaxPaymentData::isPaid)
            val pendingSum = pending.sumOf { parseEuroAmount(it.amount) }
            val paidSum = paid.sumOf { parseEuroAmount(it.amount) }
            val rawDue = parseEuroAmount(taxes.dueAmount)
            val effectiveDue = when {
                rawDue > 0.0 -> rawDue
                pendingSum > 0.0 -> pendingSum
                else -> 0.0
            }
            val nextDue = pending.firstNotNullOfOrNull { payment ->
                payment.date.trim().takeIf { it.isNotBlank() && it != "-" }
            }

            val totalVolume = paidSum + effectiveDue
            val paidProgress = when {
                totalVolume > 0.0 -> (paidSum / totalVolume).toFloat().coerceIn(0f, 1f)
                pending.isEmpty() && paid.isNotEmpty() -> 1f
                else -> 0f
            }

            mutableUiState.value = TaxesUiState(
                pendingPayments = pending,
                paidPayments = paid,
                dueAmountLabel = formatEuroAmount(effectiveDue),
                paidTotalLabel = formatEuroAmount(paidSum),
                paidProgress = paidProgress,
                nextDueDate = nextDue,
                filter = mutableUiState.value.filter,
                loadState = if (payments.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
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
        sharedData.refresh(dataRequests, force)
    }

    fun selectFilter(filter: TaxesFilter) {
        mutableUiState.value = mutableUiState.value.copy(filter = filter)
    }
}

internal fun formatTaxCurrencyLabel(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return "—"
    return if (trimmed.contains("€") || trimmed.contains("EUR", ignoreCase = true)) trimmed else "€ $trimmed"
}

private fun formatEuroAmount(value: Double): String = "€ ${value.toFixedTwoDecimals()}"

private fun parseEuroAmount(raw: String): Double {
    val cleaned = raw.trim().replace(Regex("[^0-9,.-]"), "")
    if (cleaned.isBlank()) return 0.0
    val normalized = when {
        ',' in cleaned && '.' in cleaned -> {
            if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) {
                cleaned.replace(".", "").replace(',', '.')
            } else {
                cleaned.replace(",", "")
            }
        }
        ',' in cleaned -> cleaned.replace(',', '.')
        else -> cleaned
    }
    return normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
}
