package com.energyapp.ui.screens.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.StationApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Multi-Station Dashboard
 * 
 * Handles:
 * - Loading all stations with sales summaries
 * - Filtering stations by region, status, etc.
 * - Calculating aggregate statistics across all stations
 * - Day/Night shift breakdown
 */
@HiltViewModel
class MultiStationViewModel @Inject constructor(
    private val stationApiService: StationApiService
) : ViewModel() {

    private val TAG = "MultiStationViewModel"

    private val _uiState = MutableStateFlow(MultiStationUiState())
    val uiState: StateFlow<MultiStationUiState> = _uiState.asStateFlow()

    init {
        loadStations()
    }

    fun loadStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                Log.d(TAG, "Loading all stations...")
                
                val result = stationApiService.getAllStations()
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Loaded ${response.data.size} stations")
                        
                        val stations = response.data.map { station ->
                            StationSummary(
                                stationId = station.stationId,
                                stationCode = station.stationCode,
                                stationName = station.stationName,
                                city = station.city,
                                county = station.county,
                                region = station.region,
                                pumpCount = station.pumpCount ?: 0,
                                activeShifts = station.activeShifts ?: 0,
                                todaySales = station.todaySales ?: 0.0,
                                todayTransactions = station.todayTransactions ?: 0,
                                mpesaSales = station.mpesaSales ?: 0.0,
                                isOnline = station.isOnline
                            )
                        }
                        
                        // Calculate aggregates
                        val totalSales = stations.sumOf { it.todaySales }
                        val totalTransactions = stations.sumOf { it.todayTransactions }
                        val totalMpesaSales = stations.sumOf { it.mpesaSales }
                        
                        // Sort by sales for top stations
                        val topStations = stations.sortedByDescending { it.todaySales }.take(5)
                        
                        // Day/Night breakdown (estimated - actual would come from API)
                        val dayShiftSales = totalSales * 0.6  // Estimate 60% day shift
                        val nightShiftSales = totalSales * 0.4  // Estimate 40% night shift
                        val dayShiftTransactions = (totalTransactions * 0.6).toInt()
                        val nightShiftTransactions = (totalTransactions * 0.4).toInt()
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                stations = stations,
                                filteredStations = stations,
                                topStations = topStations,
                                totalSales = totalSales,
                                totalTransactions = totalTransactions,
                                totalMpesaSales = totalMpesaSales,
                                dayShiftSales = dayShiftSales,
                                nightShiftSales = nightShiftSales,
                                dayShiftTransactions = dayShiftTransactions,
                                nightShiftTransactions = nightShiftTransactions
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to load stations: ${error.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                        
                        // Load mock data for testing
                        loadMockStations()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading stations: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                
                // Load mock data for testing
                loadMockStations()
            }
        }
    }
    
    private fun loadMockStations() {
        // Mock data for UI development/testing
        val mockStations = listOf(
            StationSummary(1, "NRB-001", "Mombasa Road Station", "Nairobi", "Nairobi", "Central", 
                4, 2, 125000.0, 45, 85000.0, true),
            StationSummary(2, "NRB-002", "Thika Road Station", "Nairobi", "Nairobi", "Central",
                3, 2, 98000.0, 38, 72000.0, true),
            StationSummary(3, "MSA-001", "Nyali Station", "Mombasa", "Mombasa", "Coast",
                4, 2, 87500.0, 32, 65000.0, true),
            StationSummary(4, "KSM-001", "Kisumu Central", "Kisumu", "Kisumu", "Western",
                3, 2, 76000.0, 28, 55000.0, true),
            StationSummary(5, "NKR-001", "Nakuru Town", "Nakuru", "Nakuru", "Central",
                3, 1, 68000.0, 25, 48000.0, false),
            StationSummary(6, "ELD-001", "Eldoret West", "Eldoret", "Uasin Gishu", "Western",
                2, 2, 54000.0, 20, 38000.0, true),
            StationSummary(7, "NRB-003", "Karen Station", "Nairobi", "Nairobi", "Central",
                3, 2, 112000.0, 42, 82000.0, true),
            StationSummary(8, "MSA-002", "Mtwapa Station", "Mombasa", "Kilifi", "Coast",
                2, 1, 45000.0, 18, 32000.0, true),
        )
        
        val totalSales = mockStations.sumOf { it.todaySales }
        val totalTransactions = mockStations.sumOf { it.todayTransactions }
        val totalMpesaSales = mockStations.sumOf { it.mpesaSales }
        val topStations = mockStations.sortedByDescending { it.todaySales }.take(5)
        
        _uiState.update {
            it.copy(
                isLoading = false,
                stations = mockStations,
                filteredStations = mockStations,
                topStations = topStations,
                totalSales = totalSales,
                totalTransactions = totalTransactions,
                totalMpesaSales = totalMpesaSales,
                dayShiftSales = totalSales * 0.6,
                nightShiftSales = totalSales * 0.4,
                dayShiftTransactions = (totalTransactions * 0.6).toInt(),
                nightShiftTransactions = (totalTransactions * 0.4).toInt(),
                error = null
            )
        }
    }

    fun setFilter(filter: StationFilter) {
        val currentStations = _uiState.value.stations
        
        val filteredStations = when (filter) {
            StationFilter.ALL -> currentStations
            StationFilter.ONLINE -> currentStations.filter { it.isOnline }
            StationFilter.OFFLINE -> currentStations.filter { !it.isOnline }
            StationFilter.TOP_SALES -> currentStations.sortedByDescending { it.todaySales }
            StationFilter.NAIROBI -> currentStations.filter { it.city?.contains("Nairobi", ignoreCase = true) == true }
            StationFilter.MOMBASA -> currentStations.filter { it.city?.contains("Mombasa", ignoreCase = true) == true }
            StationFilter.COAST -> currentStations.filter { it.region?.contains("Coast", ignoreCase = true) == true }
            StationFilter.CENTRAL -> currentStations.filter { it.region?.contains("Central", ignoreCase = true) == true }
            StationFilter.WESTERN -> currentStations.filter { it.region?.contains("Western", ignoreCase = true) == true }
        }
        
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredStations = filteredStations
            )
        }
    }

    fun refresh() {
        loadStations()
    }
}
