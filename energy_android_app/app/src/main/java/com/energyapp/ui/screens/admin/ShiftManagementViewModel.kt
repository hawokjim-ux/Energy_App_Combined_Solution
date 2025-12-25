package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.CloseShiftRequest
import com.energyapp.data.remote.models.OpenShiftRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class Pump(
    val pumpId: Int,
    val pumpName: String,
    val isShiftOpen: Boolean,
    val pumpShiftId: Int? = null
)

data class Shift(
    val shiftId: Int,
    val shiftName: String
)

data class ShiftManagementUiState(
    val pumps: List<Pump> = emptyList(),
    val shifts: List<Shift> = emptyList(),
    val selectedPump: Pump? = null,
    val selectedShift: Shift? = null,
    val openingMeterReading: String = "",
    val closingMeterReading: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ShiftManagementViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftManagementUiState())
    val uiState: StateFlow<ShiftManagementUiState> = _uiState.asStateFlow()

    private var userId: Int = 0

    // Date formatter compatible with API 24+
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    init {
        loadData()
    }

    fun setUserId(id: Int) {
        userId = id
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load pumps
                val pumpsResult = apiService.getPumps()
                val pumpResponses = pumpsResult.getOrNull() ?: emptyList()

                // Load open shifts
                val openShiftsResult = apiService.getOpenShifts()
                val openShifts = openShiftsResult.getOrNull() ?: emptyList()

                val pumps = pumpResponses.map { pump ->
                    val openShift = openShifts.find { it.pumpId == pump.pumpId && !it.isClosed }
                    Pump(
                        pumpId = pump.pumpId,
                        pumpName = pump.pumpName,
                        isShiftOpen = openShift != null,
                        pumpShiftId = openShift?.pumpShiftId
                    )
                }

                // Load shifts (now only Day and Night)
                val shiftsResult = apiService.getShifts()
                val shiftResponses = shiftsResult.getOrNull() ?: emptyList()
                val shifts = shiftResponses.map { Shift(it.shiftId, it.shiftName) }

                _uiState.value = _uiState.value.copy(
                    pumps = pumps,
                    shifts = shifts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun selectPump(pump: Pump) {
        _uiState.value = _uiState.value.copy(selectedPump = pump, error = null)
    }

    fun selectShift(shift: Shift) {
        _uiState.value = _uiState.value.copy(selectedShift = shift, error = null)
    }

    fun onOpeningMeterReadingChange(reading: String) {
        _uiState.value = _uiState.value.copy(openingMeterReading = reading, error = null)
    }

    fun onClosingMeterReadingChange(reading: String) {
        _uiState.value = _uiState.value.copy(closingMeterReading = reading, error = null)
    }

    fun openShift() {
        val pump = _uiState.value.selectedPump
        val shift = _uiState.value.selectedShift
        val reading = _uiState.value.openingMeterReading.toDoubleOrNull()

        if (pump == null) {
            _uiState.value = _uiState.value.copy(error = "Please select a pump")
            return
        }

        if (pump.isShiftOpen) {
            _uiState.value = _uiState.value.copy(error = "This pump already has an open shift")
            return
        }

        if (shift == null) {
            _uiState.value = _uiState.value.copy(error = "Please select a shift type")
            return
        }

        if (reading == null || reading <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid meter reading")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Use API 24+ compatible date formatting
                val currentTime = dateFormatter.format(Date())

                val request = OpenShiftRequest(
                    pumpId = pump.pumpId,
                    shiftId = shift.shiftId,
                    attendantId = userId,
                    openingReading = reading,
                    openingTime = currentTime
                )

                val result = apiService.openShift(request)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Shift opened successfully for ${pump.pumpName}",
                        selectedPump = null,
                        selectedShift = null,
                        openingMeterReading = ""
                    )
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to open shift"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to open shift"
                )
            }
        }
    }

    fun closeShift(pumpShiftId: Int) {
        val reading = _uiState.value.closingMeterReading.toDoubleOrNull()

        if (reading == null || reading <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid meter reading")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Use API 24+ compatible date formatting
                val currentTime = dateFormatter.format(Date())

                val request = CloseShiftRequest(
                    closingReading = reading,
                    closingTime = currentTime,
                    amountReceived = 0.0,
                    isClosed = true
                )

                val result = apiService.closeShift(pumpShiftId, request)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Shift closed successfully",
                        closingMeterReading = ""
                    )
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to close shift"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to close shift"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}