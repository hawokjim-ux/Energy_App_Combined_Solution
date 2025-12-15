package com.energyapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.models.UserResponse
import com.energyapp.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: UserResponse? = null,
    val isPasswordVisible: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: SupabaseApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username.trim(),
            error = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            error = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun login() {
        val username = _uiState.value.username.trim()
        val password = _uiState.value.password

        when {
            username.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    error = "Username is required"
                )
                return
            }
            password.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    error = "Password is required"
                )
                return
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(
                    error = "Password must be at least 6 characters"
                )
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Add logging to see what's happening
                println("🔐 DEBUG: Starting login for username: $username")
                
                val result = apiService.login(username, password)
                
                println("🔐 DEBUG: Login result - isSuccess: ${result.isSuccess}")
                if (result.exceptionOrNull() != null) {
                    println("🔐 DEBUG: Exception: ${result.exceptionOrNull()?.message}")
                    result.exceptionOrNull()?.printStackTrace()
                }

                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    
                    println("✅ DEBUG: Login successful - User: ${user.fullName}, Role: ${user.roleName}")
                    
                    // Save user session to preferences
                    preferencesManager.saveUserSession(
                        userId = user.userId,
                        username = user.username,
                        fullName = user.fullName,
                        roleName = user.roleName ?: "Unknown"
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        user = user,
                        username = "",
                        password = ""
                    )
                } else {
                    val errorMsg = result.exceptionOrNull()?.message 
                        ?: "Invalid username or password. Please try again."
                    
                    println("❌ DEBUG: Login failed - $errorMsg")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Authentication failed. Please check your connection."
                
                println("❌ DEBUG: Exception caught - $errorMsg")
                e.printStackTrace()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMsg
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}