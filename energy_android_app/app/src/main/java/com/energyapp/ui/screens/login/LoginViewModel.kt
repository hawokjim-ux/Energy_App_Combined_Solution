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
    val isPasswordVisible: Boolean = false,
    val userPermissions: Set<String> = emptySet()
)

/**
 * Role-based permission definitions
 * These match the roles defined in UserRolesViewModel
 */
object RolePermissions {
    // Super Admin - Full access (roleId = 1)
    val SUPER_ADMIN = setOf(
        "view_dashboard", "view_dashboard_stats",
        "make_sale", "view_sales", "cancel_sale", "refund_sale",
        "view_reports", "export_reports", "view_analytics",
        "view_users", "create_user", "edit_user", "delete_user", "manage_attendants", "manage_login",
        "view_pumps", "create_pump", "edit_pump", "delete_pump",
        "view_shifts", "open_shift", "close_shift", "manage_shift_definitions",
        "view_settings", "manage_roles", "system_config",
        "view_transactions", "edit_transaction"
    )

    // Director - View-only (roleId = 2) - corresponding to old "Admin" role
    val DIRECTOR = setOf(
        "view_dashboard", "view_dashboard_stats",
        "view_sales", "view_reports", "export_reports", "view_analytics",
        "view_users", "view_pumps", "view_shifts",
        "view_settings", "view_transactions"
    )

    // Manager - Full operations (roleId = 3)
    val MANAGER = setOf(
        "view_dashboard", "view_dashboard_stats",
        "make_sale", "view_sales", "cancel_sale",
        "view_reports", "view_analytics",
        "view_users", "create_user", "edit_user", "manage_attendants",
        "view_pumps", "create_pump", "edit_pump",
        "view_shifts", "open_shift", "close_shift", "manage_shift_definitions",
        "view_transactions"
    )

    // Supervisor - Oversight (roleId = 4)
    val SUPERVISOR = setOf(
        "view_dashboard", "view_dashboard_stats",
        "make_sale", "view_sales",
        "view_reports",
        "view_users", "manage_attendants",
        "view_pumps",
        "view_shifts", "open_shift", "close_shift",
        "view_transactions"
    )

    // Pump Attendant - Sales only (roleId = 5) - corresponding to old "Pump Attendant" role
    val PUMP_ATTENDANT = setOf(
        "make_sale", "view_sales"
    )

    /**
     * Get permissions for a role name or role ID
     */
    fun getPermissionsForRole(roleName: String?, roleId: Int = 0): Set<String> {
        // First try by role name
        return when (roleName?.lowercase()?.trim()) {
            "super admin", "superadmin" -> SUPER_ADMIN
            "director" -> DIRECTOR
            "admin" -> SUPER_ADMIN // Map old "Admin" to Super Admin
            "manager" -> MANAGER
            "supervisor" -> SUPERVISOR
            "pump attendant", "attendant" -> PUMP_ATTENDANT
            else -> {
                // Fallback to roleId - MUST MATCH DATABASE user_roles table!
                // role_id=1: Admin, role_id=2: Pump Attendant, role_id=5: Super Admin
                when (roleId) {
                    1 -> SUPER_ADMIN  // Admin has full access
                    2 -> PUMP_ATTENDANT  // Pump Attendant
                    3 -> MANAGER
                    4 -> SUPERVISOR
                    5 -> SUPER_ADMIN  // Super Admin
                    else -> PUMP_ATTENDANT // Default to minimal permissions
                }
            }
        }
    }

    /**
     * Check if role can view dashboard
     */
    fun canViewDashboard(roleName: String?): Boolean {
        val permissions = getPermissionsForRole(roleName)
        return permissions.contains("view_dashboard")
    }

    /**
     * Get dashboard route based on role
     */
    fun getDashboardRoute(roleName: String?): String {
        return if (canViewDashboard(roleName)) {
            "admin_dashboard"  // Admin dashboard route
        } else {
            "attendant_dashboard"  // Attendant dashboard (sales only)
        }
    }
}

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
                println("🔐 DEBUG: Starting login for username: $username")
                
                val result = apiService.login(username, password)
                
                println("🔐 DEBUG: Login result - isSuccess: ${result.isSuccess}")
                if (result.exceptionOrNull() != null) {
                    println("🔐 DEBUG: Exception: ${result.exceptionOrNull()?.message}")
                    result.exceptionOrNull()?.printStackTrace()
                }

                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    
                    // Get permissions based on role
                    val permissions = RolePermissions.getPermissionsForRole(
                        roleName = user.roleName,
                        roleId = user.roleId
                    )
                    
                    println("✅ DEBUG: Login successful - User: ${user.fullName}, Role: ${user.roleName}")
                    println("🔐 DEBUG: Assigned ${permissions.size} permissions for role: ${user.roleName}")
                    
                    // Save user session with permissions
                    preferencesManager.saveUserSession(
                        userId = user.userId,
                        username = user.username,
                        fullName = user.fullName,
                        roleName = user.roleName ?: "Unknown",
                        roleId = user.roleId,
                        permissions = permissions
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        user = user,
                        userPermissions = permissions,
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