package com.energyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AttendantState(
    val attendants: List<PumpAttendant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class PumpAttendant(
    val id: String,
    val fullName: String,
    val phoneNumber: String,
    val isActive: Boolean,
    val todaySales: String,
    val transactionCount: Int
)

@HiltViewModel
class PumpAttendantsViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AttendantState())
    val uiState: StateFlow<AttendantState> = _uiState.asStateFlow()

    init {
        loadAttendants()
    }

    private fun loadAttendants() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = apiService.getAttendants()
                
                if (result.isSuccess) {
                    val attendantSummaries = result.getOrThrow()
                    val attendants = attendantSummaries.map { summary ->
                        PumpAttendant(
                            id = summary.userId.toString(),
                            fullName = summary.fullName,
                            phoneNumber = summary.mobileNo ?: "N/A",
                            isActive = summary.isActive,
                            todaySales = "KES ${String.format("%,.2f", summary.todaySales)}",
                            transactionCount = summary.transactionCount
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        attendants = attendants,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load attendants"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun refreshAttendants() {
        loadAttendants()
    }

    fun toggleAttendantStatus(attendantId: String) {
        viewModelScope.launch {
            // TODO: Implement status toggle with Supabase
            val updatedAttendants = _uiState.value.attendants.map { attendant ->
                if (attendant.id == attendantId) {
                    attendant.copy(isActive = !attendant.isActive)
                } else {
                    attendant
                }
            }
            _uiState.value = _uiState.value.copy(attendants = updatedAttendants)
        }
    }
}