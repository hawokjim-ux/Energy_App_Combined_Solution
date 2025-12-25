package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.PumpResponse
import com.energyapp.data.remote.models.SaleResponse
import com.energyapp.data.remote.models.UserResponse
import com.energyapp.ui.components.TransactionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
    val attendantId: Int,
    val attendantName: String
)

enum class DateFilter {
    TODAY,
    WEEK,
    MONTH,
    CUSTOM
}

enum class StatusFilter {
    ALL,
    SUCCESS,
    PENDING,
    FAILED
}

enum class ShiftFilter {
    ALL,
    DAY,
    NIGHT
}

data class PumpChartData(
    val pumpName: String,
    val totalSales: Double
)

data class UserSalesChartData(
    val userName: String,
    val userId: Int,
    val totalSales: Double
)

data class HourlySalesData(
    val hour: Int,
    val label: String,
    val amount: Double
)

data class ReportsUiState(
    val sales: List<SalesRecord> = emptyList(),
    val filteredSales: List<SalesRecord> = emptyList(),
    val pumps: List<PumpResponse> = emptyList(),
    val attendants: List<UserResponse> = emptyList(),
    val selectedPump: PumpResponse? = null,
    val selectedAttendant: UserResponse? = null,
    val searchQuery: String = "",
    val totalSales: Double = 0.0,
    val mpesaSales: Double = 0.0,
    val transactionCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Date filtering
    val dateFilter: DateFilter = DateFilter.TODAY,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    
    // Status filtering
    val statusFilter: StatusFilter = StatusFilter.ALL,
    
    // Shift (Day/Night) filtering
    val shiftFilter: ShiftFilter = ShiftFilter.ALL,
    
    // Chart data
    val salesTrendData: List<Double> = emptyList(),
    val salesTrendLabels: List<String> = emptyList(),
    val pumpChartData: List<PumpChartData> = emptyList(),
    val userSalesChartData: List<UserSalesChartData> = emptyList(),
    val salesDistributionData: List<Double> = emptyList(),
    val hourlySalesData: List<HourlySalesData> = emptyList(),
    val cashSales: Double = 0.0,
    val successfulCount: Int = 0,
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    
    // Filters visibility
    val showFilters: Boolean = false,
    
    // Pagination
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    
    // Transaction items for table
    val transactionItems: List<TransactionItem> = emptyList()
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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

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
                
                _uiState.update {
                    it.copy(
                        pumps = pumps,
                        attendants = attendants,
                        sales = salesRecords,
                        isLoading = false
                    )
                }
                
                // Apply initial filter
                applyFilters()
                
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

    private fun mapSalesToRecords(sales: List<SaleResponse>): List<SalesRecord> {
        return sales.map { sale ->
            val pump = allPumps[sale.pumpId]
            val attendant = allUsers[sale.attendantId]

            SalesRecord(
                saleId = sale.saleId,
                saleIdNo = sale.saleIdNo,
                amount = sale.amount,
                saleTime = sale.saleTime,
                customerMobileNo = sale.customerMobileNo ?: "",
                mpesaReceiptNumber = sale.mpesaReceiptNumber,
                transactionStatus = sale.transactionStatus,
                pumpId = sale.pumpId,
                pumpName = pump?.pumpName ?: "Unknown",
                attendantId = sale.attendantId,
                attendantName = attendant?.fullName ?: "Unknown"
            )
        }
    }

    fun setDateFilter(filter: DateFilter) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        val startDate = when (filter) {
            DateFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            DateFilter.WEEK -> {
                calendar.add(Calendar.DAY_OF_MONTH, -7)
                calendar.timeInMillis
            }
            DateFilter.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            DateFilter.CUSTOM -> _uiState.value.startDate
        }
        
        _uiState.update { 
            it.copy(
                dateFilter = filter,
                startDate = startDate,
                endDate = endDate
            )
        }
        applyFilters()
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _uiState.update {
            it.copy(
                dateFilter = DateFilter.CUSTOM,
                startDate = start,
                endDate = end
            )
        }
        applyFilters()
    }

    fun setStatusFilter(status: StatusFilter) {
        _uiState.update { it.copy(statusFilter = status, currentPage = 0) }
        applyFilters()
    }

    fun setShiftFilter(shift: ShiftFilter) {
        _uiState.update { it.copy(shiftFilter = shift, currentPage = 0) }
        applyFilters()
    }

    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    fun selectPump(pump: PumpResponse?) {
        _uiState.update { it.copy(selectedPump = pump, currentPage = 0) }
        applyFilters()
    }

    fun selectAttendant(attendant: UserResponse?) {
        _uiState.update { it.copy(selectedAttendant = attendant, currentPage = 0) }
        applyFilters()
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, currentPage = 0) }
        applyFilters()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
        updatePaginatedItems()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val startDateStr = dateFormat.format(Date(state.startDate))
        val endDateStr = dateFormat.format(Date(state.endDate))

        var filteredSales = state.sales.filter { sale ->
            // Date filter
            val saleDate = sale.saleTime.substringBefore("T").substringBefore(" ")
            val matchesDate = saleDate >= startDateStr && saleDate <= endDateStr

            // Pump filter
            val matchesPump = state.selectedPump?.let { sale.pumpId == it.pumpId } ?: true

            // Attendant filter
            val matchesAttendant = state.selectedAttendant?.let { 
                sale.attendantId == it.userId 
            } ?: true

            // Status filter
            val matchesStatus = when (state.statusFilter) {
                StatusFilter.ALL -> true
                StatusFilter.SUCCESS -> sale.transactionStatus.uppercase() == "SUCCESS"
                StatusFilter.PENDING -> sale.transactionStatus.uppercase() == "PENDING"
                StatusFilter.FAILED -> sale.transactionStatus.uppercase() in listOf("FAILED", "CANCELLED", "ERROR")
            }

            // Shift (Day/Night) filter - Day: 6AM-6PM, Night: 6PM-6AM
            val matchesShift = when (state.shiftFilter) {
                ShiftFilter.ALL -> true
                ShiftFilter.DAY -> {
                    try {
                        val timeStr = sale.saleTime.substringAfter("T").substringAfter(" ").take(5)
                        val hour = timeStr.substringBefore(":").toIntOrNull() ?: 12
                        hour in 6..17 // 6AM to 5:59PM
                    } catch (e: Exception) { true }
                }
                ShiftFilter.NIGHT -> {
                    try {
                        val timeStr = sale.saleTime.substringAfter("T").substringAfter(" ").take(5)
                        val hour = timeStr.substringBefore(":").toIntOrNull() ?: 12
                        hour < 6 || hour >= 18 // 6PM to 5:59AM
                    } catch (e: Exception) { true }
                }
            }

            // Search filter
            val matchesSearch = state.searchQuery.takeIf { it.isNotEmpty() }?.let { query ->
                sale.saleIdNo.contains(query, ignoreCase = true) ||
                sale.attendantName.contains(query, ignoreCase = true) ||
                sale.pumpName.contains(query, ignoreCase = true) ||
                sale.mpesaReceiptNumber?.contains(query, ignoreCase = true) == true ||
                sale.amount.toString().contains(query)
            } ?: true

            matchesDate && matchesPump && matchesAttendant && matchesStatus && matchesShift && matchesSearch
        }

        // Calculate counts by status
        val successfulCount = filteredSales.count { it.transactionStatus.uppercase() == "SUCCESS" }
        val pendingCount = filteredSales.count { it.transactionStatus.uppercase() == "PENDING" }
        val failedCount = filteredSales.count { it.transactionStatus.uppercase() in listOf("FAILED", "CANCELLED", "ERROR") }

        val totalSales = filteredSales.sumOf { it.amount }
        val mpesaSales = filteredSales.filter { !it.mpesaReceiptNumber.isNullOrEmpty() }.sumOf { it.amount }
        val cashSales = totalSales - mpesaSales

        // Calculate chart data
        val pumpChartData = state.pumps.map { pump ->
            PumpChartData(
                pumpName = pump.pumpName,
                totalSales = filteredSales.filter { it.pumpId == pump.pumpId }.sumOf { it.amount }
            )
        }.filter { it.totalSales > 0 }

        // Calculate user/attendant sales data
        val userSalesChartData = state.attendants.map { attendant ->
            UserSalesChartData(
                userName = attendant.fullName,
                userId = attendant.userId,
                totalSales = filteredSales.filter { it.attendantId == attendant.userId }.sumOf { it.amount }
            )
        }.filter { it.totalSales > 0 }.sortedByDescending { it.totalSales }

        // Calculate sales distribution data (amounts for histogram)
        val salesDistributionData = filteredSales.map { it.amount }

        // Calculate hourly sales data
        val hourlySalesData = (0..23).map { hour ->
            val hourLabel = if (hour < 12) "${hour}AM" else if (hour == 12) "12PM" else "${hour - 12}PM"
            val hourSales = filteredSales.filter { sale ->
                try {
                    val timeStr = sale.saleTime.substringAfter("T").substringAfter(" ").take(5)
                    val saleHour = timeStr.substringBefore(":").toIntOrNull() ?: 0
                    saleHour == hour
                } catch (e: Exception) { false }
            }.sumOf { it.amount }
            HourlySalesData(hour, hourLabel, hourSales)
        }.filter { it.amount > 0 }

        // Calculate sales trend (last 7 days)
        val trendData = mutableListOf<Double>()
        val trendLabels = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            val dayStr = dateFormat.format(calendar.time)
            val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            
            val daySales = state.sales.filter { sale ->
                sale.saleTime.startsWith(dayStr)
            }.sumOf { it.amount }
            
            trendData.add(daySales)
            trendLabels.add(dayLabel)
        }

        _uiState.update {
            it.copy(
                filteredSales = filteredSales,
                totalSales = totalSales,
                mpesaSales = mpesaSales,
                cashSales = cashSales,
                successfulCount = successfulCount,
                pendingCount = pendingCount,
                failedCount = failedCount,
                transactionCount = filteredSales.size,
                pumpChartData = pumpChartData,
                userSalesChartData = userSalesChartData,
                salesDistributionData = salesDistributionData,
                hourlySalesData = hourlySalesData,
                salesTrendData = trendData,
                salesTrendLabels = trendLabels,
                currentPage = 0
            )
        }
        
        updatePaginatedItems()
    }

    private fun updatePaginatedItems() {
        val state = _uiState.value
        val startIndex = state.currentPage * state.pageSize
        val endIndex = minOf(startIndex + state.pageSize, state.filteredSales.size)
        
        val paginatedSales = if (startIndex < state.filteredSales.size) {
            state.filteredSales.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        val transactionItems = paginatedSales.map { sale ->
            val datePart = sale.saleTime.substringBefore("T").substringBefore(" ")
            val timePart = sale.saleTime.substringAfter("T").substringAfter(" ").take(5)
            
            TransactionItem(
                saleId = sale.saleId,
                saleIdNo = sale.saleIdNo,
                userName = sale.attendantName,
                userId = sale.attendantId,
                mpesaReceipt = sale.mpesaReceiptNumber,
                amount = sale.amount,
                date = datePart,
                time = timePart,
                status = sale.transactionStatus,
                pumpName = sale.pumpName
            )
        }

        _uiState.update { it.copy(transactionItems = transactionItems) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedPump = null,
                selectedAttendant = null,
                searchQuery = "",
                statusFilter = StatusFilter.ALL,
                shiftFilter = ShiftFilter.ALL,
                dateFilter = DateFilter.TODAY
            )
        }
        setDateFilter(DateFilter.TODAY)
    }

    fun refresh() {
        loadData()
    }
}