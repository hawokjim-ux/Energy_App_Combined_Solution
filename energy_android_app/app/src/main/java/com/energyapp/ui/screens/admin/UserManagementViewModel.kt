package com.energyapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementUiState(
    val users: List<UserResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = apiService.getAllUsers()

                if (result.isSuccess) {
                    val users = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        users = users,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load users"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading users: ${e.message}"
                )
            }
        }
    }

    fun createUser(
        fullName: String,
        username: String,
        password: String,
        mobile: String?,
        roleId: Int
    ) {
        // Validate inputs
        if (fullName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Full name cannot be empty")
            return
        }
        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Username cannot be empty")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password cannot be empty")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = apiService.createUser(
                    fullName = fullName,
                    username = username,
                    password = password,
                    mobileNo = mobile,
                    roleId = roleId
                )

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "User created successfully"
                    )
                    // Clear success message after a short delay
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(successMessage = null)
                    loadUsers()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to create user"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error: $errorMsg"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create user: ${e.message}"
                )
            }
        }
    }

    fun toggleUserStatus(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userToUpdate = _uiState.value.users.find { it.userId == userId }
                if (userToUpdate != null) {
                    val result = apiService.updateUser(
                        userId = userId,
                        isActive = !userToUpdate.isActive
                    )

                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "User status updated"
                        )
                        // Clear success message after a short delay
                        kotlinx.coroutines.delay(2000)
                        _uiState.value = _uiState.value.copy(successMessage = null)
                        loadUsers()
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Failed to update user status"
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error: $errorMsg"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update user status: ${e.message}"
                )
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = apiService.deleteUser(userId)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "User deleted successfully"
                    )
                    // Clear success message after a short delay
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(successMessage = null)
                    loadUsers()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to delete user"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error: $errorMsg"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete user: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}