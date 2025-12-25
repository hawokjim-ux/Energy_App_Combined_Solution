package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.PumpResponse
import com.energyapp.data.remote.models.SaleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TransactionData(
    val saleId: Int,
    val saleIdNo: String,
    val amount: Double,
    val customerPhone: String?,
    val pumpName: String,
    val attendantName: String,
    val time: String,
    val date: String,
    val mpesaReceipt: String?,
    val status: String
)

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val transactions: List<TransactionData> = emptyList(),
    val filteredTransactions: List<TransactionData> = emptyList(),
    val searchQuery: String = "",
    val totalAmount: Double = 0.0
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private var allTransactions: List<TransactionData> = emptyList()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load pumps
                val pumpsResult = apiService.getPumps()
                val pumps = pumpsResult.getOrNull()?.associateBy { it.pumpId } ?: emptyMap()

                // Load users
                val usersResult = apiService.getAllUsers()
                val users = usersResult.getOrNull()?.associateBy { it.userId } ?: emptyMap()

                // Load all sales
                val salesResult = apiService.getAllSales()
                val sales = salesResult.getOrNull() ?: emptyList()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                allTransactions = sales.map { sale ->
                    val saleDateTime = try {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(sale.saleTime)
                    } catch (e: Exception) {
                        try {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(sale.saleTime)
                        } catch (e2: Exception) {
                            Date()
                        }
                    }

                    TransactionData(
                        saleId = sale.saleId,
                        saleIdNo = sale.saleIdNo,
                        amount = sale.amount,
                        customerPhone = sale.customerMobileNo,
                        pumpName = pumps[sale.pumpId]?.pumpName ?: "Pump ${sale.pumpId}",
                        attendantName = users[sale.attendantId]?.fullName ?: "Unknown",
                        time = saleDateTime?.let { timeFormat.format(it) } ?: "",
                        date = saleDateTime?.let { dateFormat.format(it) } ?: "",
                        mpesaReceipt = sale.mpesaReceiptNumber,
                        status = sale.transactionStatus
                    )
                }

                val totalAmount = allTransactions.filter { it.status == "SUCCESS" }.sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transactions = allTransactions,
                        totalAmount = totalAmount
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load transactions: ${e.message}"
                    )
                }
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isEmpty()) {
                allTransactions
            } else {
                allTransactions.filter { transaction ->
                    transaction.customerPhone?.contains(query, ignoreCase = true) == true ||
                    transaction.mpesaReceipt?.contains(query, ignoreCase = true) == true ||
                    transaction.amount.toString().contains(query) ||
                    transaction.saleIdNo.contains(query, ignoreCase = true)
                }
            }
            state.copy(
                searchQuery = query,
                transactions = filtered,
                totalAmount = filtered.filter { it.status == "SUCCESS" }.sumOf { it.amount }
            )
        }
    }

    fun refresh() {
        loadTransactions()
    }
}
