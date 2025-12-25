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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class PumpSalesData(
    val pumpId: Int,
    val pumpName: String,
    val pumpNo: String,
    val totalSales: Double,
    val transactionCount: Int,
    val mpesaSales: Double
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val fullName: String = "Admin",
    
    // Today's Stats
    val todayTotalSales: Double = 0.0,
    val todayTransactionCount: Int = 0,
    val todayMpesaSales: Double = 0.0,
    
    // System Stats
    val activePumps: Int = 0,
    val activeUsers: Int = 0,
    val activeShifts: Int = 0,
    val mPesaStatus: String = "Active",
    
    // Pump-wise data
    val pumpSalesData: List<PumpSalesData> = emptyList(),
    
    // Recent transactions
    val recentTransactions: List<RecentTransaction> = emptyList()
)

data class RecentTransaction(
    val saleIdNo: String,
    val amount: Double,
    val pumpName: String,
    val attendantName: String,
    val time: String,
    val mpesaReceipt: String?,
    val status: String
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var allSales: List<SaleResponse> = emptyList()
    private var allPumps: Map<Int, PumpResponse> = emptyMap()
    private var allUsers: Map<Int, UserResponse> = emptyMap()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load pumps
                val pumpsResult = apiService.getPumps()
                val pumps = pumpsResult.getOrNull() ?: emptyList()
                allPumps = pumps.associateBy { it.pumpId }
                val activePumps = pumps.count { it.isActive }

                // Load users
                val usersResult = apiService.getAllUsers()
                val users = usersResult.getOrNull() ?: emptyList()
                allUsers = users.associateBy { it.userId }
                val activeUsers = users.count { it.isActive }

                // Load sales
                val salesResult = apiService.getAllSales()
                allSales = salesResult.getOrNull() ?: emptyList()

                // Load active shifts count
                val shiftsResult = apiService.getOpenShifts()
                val activeShifts = shiftsResult.getOrNull()?.size ?: 0

                // Calculate today's stats
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                // Handle various date formats from the database (created_at)
                val todaySales = allSales.filter { sale ->
                    try {
                        // Try to extract just the date part (handles "2025-12-16 05:30:15" format)
                        sale.saleTime.take(10) == today
                    } catch (e: Exception) {
                        false
                    }
                }
                val successfulTodaySales = todaySales.filter { 
                    it.transactionStatus.uppercase() in listOf("SUCCESS", "COMPLETED") 
                }

                // Count ALL sales (not just SUCCESS since most are PENDING in DB)
                val todayTotalSales = todaySales.sumOf { it.amount }
                val todayTransactionCount = todaySales.size
                val todayMpesaSales = todaySales
                    .filter { !it.mpesaReceiptNumber.isNullOrEmpty() }
                    .sumOf { it.amount }

                // Calculate pump-wise sales - count all
                val pumpSalesData = pumps.map { pump ->
                    val pumpSales = todaySales.filter { it.pumpId == pump.pumpId }
                    PumpSalesData(
                        pumpId = pump.pumpId,
                        pumpName = pump.pumpName,
                        pumpNo = pump.pumpId.toString(),
                        totalSales = pumpSales.sumOf { it.amount },
                        transactionCount = pumpSales.size,
                        mpesaSales = pumpSales
                            .filter { !it.mpesaReceiptNumber.isNullOrEmpty() }
                            .sumOf { it.amount }
                    )
                }

                // Recent transactions (last 5)
                val recentTransactions = allSales.take(5).map { sale ->
                    RecentTransaction(
                        saleIdNo = sale.saleIdNo,
                        amount = sale.amount,
                        pumpName = allPumps[sale.pumpId]?.pumpName ?: "Unknown",
                        attendantName = allUsers[sale.attendantId]?.fullName ?: "Unknown",
                        time = sale.saleTime,
                        mpesaReceipt = sale.mpesaReceiptNumber,
                        status = sale.transactionStatus
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayTotalSales = todayTotalSales,
                        todayTransactionCount = todayTransactionCount,
                        todayMpesaSales = todayMpesaSales,
                        activePumps = activePumps,
                        activeUsers = activeUsers,
                        activeShifts = activeShifts,
                        pumpSalesData = pumpSalesData,
                        recentTransactions = recentTransactions
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    fun setFullName(name: String) {
        _uiState.update { it.copy(fullName = name) }
    }

    fun refresh() {
        loadDashboardData()
    }
}
