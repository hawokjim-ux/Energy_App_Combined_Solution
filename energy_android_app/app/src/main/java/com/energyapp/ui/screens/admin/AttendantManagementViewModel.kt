package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.AttendantSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AttendantManagementUiState(
    val attendants: List<AttendantSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val operationSuccess: String? = null
)

@HiltViewModel
class AttendantManagementViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendantManagementUiState())
    val uiState: StateFlow<AttendantManagementUiState> = _uiState.asStateFlow()

    init {
        loadAttendants()
    }

    fun loadAttendants() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.getAttendants()
                result.onSuccess { attendants ->
                    _uiState.update { it.copy(attendants = attendants, isLoading = false) }
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

    fun createAttendant(fullName: String, username: String, password: String, mobileNo: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Role ID 2 is for Pump Attendant
                val result = apiService.createUser(fullName, username, password, mobileNo, 2)
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            showAddDialog = false, 
                            isLoading = false,
                            operationSuccess = "Attendant created successfully"
                        ) 
                    }
                    loadAttendants()
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun toggleAttendantActive(attendant: AttendantSummary) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.updateUser(
                    userId = attendant.userId,
                    isActive = !attendant.isActive
                )
                result.onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            operationSuccess = "Attendant ${if (!attendant.isActive) "activated" else "deactivated"}"
                        ) 
                    }
                    loadAttendants()
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
