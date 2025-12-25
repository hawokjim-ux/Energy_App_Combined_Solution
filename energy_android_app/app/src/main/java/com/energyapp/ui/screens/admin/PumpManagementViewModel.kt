package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.PumpResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PumpUiState(
    val pumps: List<PumpResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingPump: PumpResponse? = null,
    val showDeleteConfirmation: Boolean = false,
    val pumpToDelete: PumpResponse? = null,
    val operationSuccess: String? = null
)

@HiltViewModel
class PumpManagementViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PumpUiState())
    val uiState: StateFlow<PumpUiState> = _uiState.asStateFlow()

    init {
        loadPumps()
    }

    fun loadPumps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.getAllPumps()
                result.onSuccess { pumps ->
                    _uiState.update { it.copy(pumps = pumps, isLoading = false) }
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

    fun showEditDialog(pump: PumpResponse) {
        _uiState.update { it.copy(showEditDialog = true, editingPump = pump) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, editingPump = null) }
    }

    fun showDeleteConfirmation(pump: PumpResponse) {
        _uiState.update { it.copy(showDeleteConfirmation = true, pumpToDelete = pump) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false, pumpToDelete = null) }
    }

    fun createPump(name: String, type: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.createPump(name, type)
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            showAddDialog = false, 
                            isLoading = false,
                            operationSuccess = "Pump created successfully"
                        ) 
                    }
                    loadPumps()
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updatePump(pumpId: Int, name: String, type: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.updatePump(pumpId, name, type, isActive)
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            showEditDialog = false,
                            editingPump = null,
                            isLoading = false,
                            operationSuccess = "Pump updated successfully"
                        ) 
                    }
                    loadPumps()
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun togglePumpActive(pump: PumpResponse) {
        updatePump(pump.pumpId, pump.pumpName, pump.pumpType ?: "Petrol", !pump.isActive)
    }

    fun deletePump(pumpId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.deletePump(pumpId)
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            showDeleteConfirmation = false,
                            pumpToDelete = null,
                            isLoading = false,
                            operationSuccess = "Pump deleted successfully"
                        ) 
                    }
                    loadPumps()
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
