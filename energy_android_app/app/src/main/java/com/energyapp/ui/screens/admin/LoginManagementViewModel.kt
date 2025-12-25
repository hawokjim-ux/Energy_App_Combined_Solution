package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginManagementUiState(
    val users: List<UserResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val operationSuccess: String? = null
)

@HiltViewModel
class LoginManagementViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginManagementUiState())
    val uiState: StateFlow<LoginManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = apiService.getAllUsers()
                result.onSuccess { users ->
                    _uiState.update { it.copy(users = users, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun toggleUserActive(userId: Int, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            try {
                val result = apiService.updateUser(
                    userId = userId,
                    isActive = isActive
                )
                result.onSuccess { updatedUser ->
                    _uiState.update { state ->
                        state.copy(
                            users = state.users.map { user ->
                                if (user.userId == userId) updatedUser else user
                            },
                            operationSuccess = if (isActive) 
                                "${updatedUser.fullName} can now login" 
                            else 
                                "${updatedUser.fullName} login disabled"
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
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
