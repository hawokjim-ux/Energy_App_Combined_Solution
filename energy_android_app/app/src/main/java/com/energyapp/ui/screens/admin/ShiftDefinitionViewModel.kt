package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.ShiftResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShiftDefinitionUiState(
    val shifts: List<ShiftResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingShift: ShiftResponse? = null,
    val showDeleteConfirmation: Boolean = false,
    val shiftToDelete: ShiftResponse? = null,
    val operationSuccess: String? = null
)

@HiltViewModel
class ShiftDefinitionViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftDefinitionUiState())
    val uiState: StateFlow<ShiftDefinitionUiState> = _uiState.asStateFlow()

    init {
        loadShifts()
    }

    fun loadShifts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.getShifts()
                result.onSuccess { shifts ->
                    _uiState.update { it.copy(shifts = shifts, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(shift: ShiftResponse) {
        _uiState.update { it.copy(showEditDialog = true, editingShift = shift) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, editingShift = null) }
    }

    fun showDeleteConfirmation(shift: ShiftResponse) {
        _uiState.update { it.copy(showDeleteConfirmation = true, shiftToDelete = shift) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false, shiftToDelete = null) }
    }

    fun createShift(name: String, startTime: String, endTime: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.createShiftDefinition(name, startTime, endTime)
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            showAddDialog = false, 
                            isLoading = false,
                            operationSuccess = "Shift created successfully"
                        ) 
                    }
                    loadShifts()
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateShift(shiftId: Int, name: String, startTime: String, endTime: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.updateShiftDefinition(shiftId, name, startTime, endTime)
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            showEditDialog = false,
                            editingShift = null,
                            isLoading = false,
                            operationSuccess = "Shift updated successfully"
                        ) 
                    }
                    loadShifts()
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun deleteShift(shiftId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.deleteShiftDefinition(shiftId)
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            showDeleteConfirmation = false,
                            shiftToDelete = null,
                            isLoading = false,
                            operationSuccess = "Shift deleted successfully"
                        ) 
                    }
                    loadShifts()
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(operationSuccess = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
