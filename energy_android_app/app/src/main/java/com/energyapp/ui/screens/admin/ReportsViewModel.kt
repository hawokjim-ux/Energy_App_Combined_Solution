package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.PumpResponse
import com.energyapp.data.remote.models.SaleResponse
import com.energyapp.data.remote.models.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesRecord(
    val saleId: Int,
    val saleIdNo: String,
    val amount: Double,
    val saleTime: String,
    val customerMobileNo: String,
    val mpesaReceiptNumber: String?,
    val transactionStatus: String,
    val pumpId: Int,
    val pumpName: String,
    val attendantName: String
)

data class ReportsUiState(
    val sales: List<SalesRecord> = emptyList(),
    val pumps: List<PumpResponse> = emptyList(),
    val attendants: List<UserResponse> = emptyList(),
    val selectedPump: PumpResponse? = null,
    val selectedAttendant: UserResponse? = null,
    val searchMobile: String = "",
    val totalSales: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var allSales: List<SaleResponse> = emptyList()
    private var allPumps: Map<Int, PumpResponse> = emptyMap()
    private var allUsers: Map<Int, UserResponse> = emptyMap()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load pumps
                val pumpsResult = apiService.getPumps()
                val pumps = pumpsResult.getOrNull() ?: emptyList()
                allPumps = pumps.associateBy { it.pumpId }

                // Load users (attendants)
                val usersResult = apiService.getAllUsers()
                val users = usersResult.getOrNull() ?: emptyList()
                val attendants = users.filter { it.roleName == "Pump Attendant" }
                allUsers = users.associateBy { it.userId }

                // Load sales
                val salesResult = apiService.getAllSales()
                allSales = salesResult.getOrNull() ?: emptyList()

                val salesRecords = mapSalesToRecords(allSales)
                val totalSales = salesRecords
                    .filter { it.transactionStatus == "SUCCESS" }
                    .sumOf { it.amount }

                _uiState.value = _uiState.value.copy(
                    pumps = pumps,
                    attendants = attendants,
                    sales = salesRecords,
                    totalSales = totalSales,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    private fun mapSalesToRecords(sales: List<SaleResponse>): List<SalesRecord> {
        return sales.map { sale ->
            val pump = allPumps[sale.pumpId]
            val attendant = allUsers[sale.attendantId]

            SalesRecord(
                saleId = sale.saleId,
                saleIdNo = sale.saleIdNo,
                amount = sale.amount,
                saleTime = sale.saleTime,
                customerMobileNo = sale.customerMobileNo,
                mpesaReceiptNumber = sale.mpesaReceiptNumber,
                transactionStatus = sale.transactionStatus,
                pumpId = sale.pumpId,
                pumpName = pump?.pumpName ?: "Unknown",
                attendantName = attendant?.fullName ?: "Unknown"
            )
        }
    }

    fun selectPump(pump: PumpResponse?) {
        _uiState.value = _uiState.value.copy(selectedPump = pump)
        applyFilters()
    }

    fun selectAttendant(attendant: UserResponse?) {
        _uiState.value = _uiState.value.copy(selectedAttendant = attendant)
        applyFilters()
    }

    fun onSearchMobileChange(mobile: String) {
        _uiState.value = _uiState.value.copy(searchMobile = mobile)
        applyFilters()
    }

    private fun applyFilters() {
        val filteredSales = allSales.filter { sale ->
            val matchesPump = _uiState.value.selectedPump?.let {
                sale.pumpId == it.pumpId
            } ?: true

            val matchesAttendant = _uiState.value.selectedAttendant?.let {
                sale.attendantId == it.userId
            } ?: true

            val matchesMobile = _uiState.value.searchMobile.takeIf { it.isNotEmpty() }?.let {
                sale.customerMobileNo.contains(it, ignoreCase = true)
            } ?: true

            matchesPump && matchesAttendant && matchesMobile
        }

        val salesRecords = mapSalesToRecords(filteredSales)
        val totalSales = salesRecords
            .filter { it.transactionStatus == "SUCCESS" }
            .sumOf { it.amount }

        _uiState.value = _uiState.value.copy(
            sales = salesRecords,
            totalSales = totalSales
        )
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedPump = null,
            selectedAttendant = null,
            searchMobile = ""
        )

        val salesRecords = mapSalesToRecords(allSales)
        val totalSales = salesRecords
            .filter { it.transactionStatus == "SUCCESS" }
            .sumOf { it.amount }

        _uiState.value = _uiState.value.copy(
            sales = salesRecords,
            totalSales = totalSales
        )
    }
}