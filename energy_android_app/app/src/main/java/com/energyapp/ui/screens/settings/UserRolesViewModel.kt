package com.energyapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Comprehensive Permission System for Role-Based Access Control
 */
data class Permission(
    val id: String,
    val name: String,
    val description: String,
    val category: PermissionCategory,
    val icon: String = "🔒"
)

enum class PermissionCategory {
    DASHBOARD,
    SALES,
    REPORTS,
    USER_MANAGEMENT,
    PUMP_MANAGEMENT,
    SHIFT_MANAGEMENT,
    SETTINGS,
    TRANSACTIONS
}

// All available permissions in the app
object AppPermissions {
    // Dashboard Permissions
    val VIEW_DASHBOARD = Permission("view_dashboard", "View Dashboard", "Access admin dashboard with stats", PermissionCategory.DASHBOARD, "📊")
    val VIEW_DASHBOARD_STATS = Permission("view_dashboard_stats", "View Dashboard Stats", "See sales totals and metrics", PermissionCategory.DASHBOARD, "📈")
    
    // Sales Permissions
    val MAKE_SALE = Permission("make_sale", "Make Sale", "Record new sales transactions", PermissionCategory.SALES, "💳")
    val VIEW_SALES = Permission("view_sales", "View Sales", "See sales history", PermissionCategory.SALES, "👁️")
    val CANCEL_SALE = Permission("cancel_sale", "Cancel Sale", "Cancel pending transactions", PermissionCategory.SALES, "❌")
    val REFUND_SALE = Permission("refund_sale", "Refund Sale", "Process refunds", PermissionCategory.SALES, "💰")
    
    // Reports Permissions  
    val VIEW_REPORTS = Permission("view_reports", "View Reports", "Access sales reports", PermissionCategory.REPORTS, "📋")
    val EXPORT_REPORTS = Permission("export_reports", "Export Reports", "Download report data", PermissionCategory.REPORTS, "📤")
    val VIEW_ANALYTICS = Permission("view_analytics", "View Analytics", "Access charts and trends", PermissionCategory.REPORTS, "📉")
    
    // User Management Permissions
    val VIEW_USERS = Permission("view_users", "View Users", "See user list", PermissionCategory.USER_MANAGEMENT, "👥")
    val CREATE_USER = Permission("create_user", "Create User", "Add new users", PermissionCategory.USER_MANAGEMENT, "➕")
    val EDIT_USER = Permission("edit_user", "Edit User", "Modify user details", PermissionCategory.USER_MANAGEMENT, "✏️")
    val DELETE_USER = Permission("delete_user", "Delete User", "Remove users", PermissionCategory.USER_MANAGEMENT, "🗑️")
    val MANAGE_ATTENDANTS = Permission("manage_attendants", "Manage Attendants", "Control pump attendants", PermissionCategory.USER_MANAGEMENT, "👷")
    val MANAGE_LOGIN = Permission("manage_login", "Manage Login", "Control user logins", PermissionCategory.USER_MANAGEMENT, "🔐")
    
    // Pump Management Permissions
    val VIEW_PUMPS = Permission("view_pumps", "View Pumps", "See pump list", PermissionCategory.PUMP_MANAGEMENT, "⛽")
    val CREATE_PUMP = Permission("create_pump", "Create Pump", "Add new pumps", PermissionCategory.PUMP_MANAGEMENT, "➕")
    val EDIT_PUMP = Permission("edit_pump", "Edit Pump", "Modify pump details", PermissionCategory.PUMP_MANAGEMENT, "✏️")
    val DELETE_PUMP = Permission("delete_pump", "Delete Pump", "Remove pumps", PermissionCategory.PUMP_MANAGEMENT, "🗑️")
    
    // Shift Management Permissions
    val VIEW_SHIFTS = Permission("view_shifts", "View Shifts", "See shift schedule", PermissionCategory.SHIFT_MANAGEMENT, "📅")
    val OPEN_SHIFT = Permission("open_shift", "Open Shift", "Start a new shift", PermissionCategory.SHIFT_MANAGEMENT, "▶️")
    val CLOSE_SHIFT = Permission("close_shift", "Close Shift", "End a shift", PermissionCategory.SHIFT_MANAGEMENT, "⏹️")
    val MANAGE_SHIFT_DEFINITIONS = Permission("manage_shift_definitions", "Manage Shift Definitions", "Define shift times", PermissionCategory.SHIFT_MANAGEMENT, "⏰")
    
    // Settings Permissions
    val VIEW_SETTINGS = Permission("view_settings", "View Settings", "Access settings", PermissionCategory.SETTINGS, "⚙️")
    val MANAGE_ROLES = Permission("manage_roles", "Manage Roles", "Configure user roles", PermissionCategory.SETTINGS, "🛡️")
    val SYSTEM_CONFIG = Permission("system_config", "System Configuration", "Advanced settings", PermissionCategory.SETTINGS, "🔧")
    
    // Transaction Permissions
    val VIEW_TRANSACTIONS = Permission("view_transactions", "View Transactions", "See all transactions", PermissionCategory.TRANSACTIONS, "📝")
    val EDIT_TRANSACTION = Permission("edit_transaction", "Edit Transaction", "Modify transactions", PermissionCategory.TRANSACTIONS, "✏️")
    
    val allPermissions = listOf(
        VIEW_DASHBOARD, VIEW_DASHBOARD_STATS,
        MAKE_SALE, VIEW_SALES, CANCEL_SALE, REFUND_SALE,
        VIEW_REPORTS, EXPORT_REPORTS, VIEW_ANALYTICS,
        VIEW_USERS, CREATE_USER, EDIT_USER, DELETE_USER, MANAGE_ATTENDANTS, MANAGE_LOGIN,
        VIEW_PUMPS, CREATE_PUMP, EDIT_PUMP, DELETE_PUMP,
        VIEW_SHIFTS, OPEN_SHIFT, CLOSE_SHIFT, MANAGE_SHIFT_DEFINITIONS,
        VIEW_SETTINGS, MANAGE_ROLES, SYSTEM_CONFIG,
        VIEW_TRANSACTIONS, EDIT_TRANSACTION
    )
    
    fun getByCategory(category: PermissionCategory): List<Permission> {
        return allPermissions.filter { it.category == category }
    }
}

data class RolePermission(
    val roleId: Int,
    val roleName: String,
    val description: String,
    val colorHex: String,
    val icon: String,
    val permissions: Set<String> = emptySet(),
    val isEditable: Boolean = true
)

data class UserRolesUiState(
    val roles: List<RolePermission> = emptyList(),
    val selectedRole: RolePermission? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val editingRole: RolePermission? = null,
    val showAddRoleDialog: Boolean = false
)

@HiltViewModel
class UserRolesViewModel @Inject constructor(
    private val apiService: SupabaseApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserRolesUiState())
    val uiState: StateFlow<UserRolesUiState> = _uiState.asStateFlow()

    init {
        loadRoles()
    }

    private fun loadRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Default roles with their permissions
            val defaultRoles = listOf(
                RolePermission(
                    roleId = 1,
                    roleName = "Super Admin",
                    description = "Full system access with all permissions",
                    colorHex = "#7C3AED",
                    icon = "👑",
                    permissions = AppPermissions.allPermissions.map { it.id }.toSet(),
                    isEditable = false // Super Admin cannot be edited
                ),
                RolePermission(
                    roleId = 2,
                    roleName = "Director",
                    description = "Executive access to reports and oversight",
                    colorHex = "#EC4899",
                    icon = "🎯",
                    permissions = setOf(
                        "view_dashboard", "view_dashboard_stats",
                        "view_sales", "view_reports", "export_reports", "view_analytics",
                        "view_users", "view_pumps", "view_shifts",
                        "view_settings", "view_transactions"
                    )
                ),
                RolePermission(
                    roleId = 3,
                    roleName = "Manager",
                    description = "Full operational control",
                    colorHex = "#06B6D4",
                    icon = "💼",
                    permissions = setOf(
                        "view_dashboard", "view_dashboard_stats",
                        "make_sale", "view_sales", "cancel_sale",
                        "view_reports", "view_analytics",
                        "view_users", "create_user", "edit_user", "manage_attendants",
                        "view_pumps", "create_pump", "edit_pump",
                        "view_shifts", "open_shift", "close_shift", "manage_shift_definitions",
                        "view_transactions"
                    )
                ),
                RolePermission(
                    roleId = 4,
                    roleName = "Supervisor",
                    description = "Oversee attendants and shifts",
                    colorHex = "#F97316",
                    icon = "👔",
                    permissions = setOf(
                        "view_dashboard", "view_dashboard_stats",
                        "make_sale", "view_sales",
                        "view_reports",
                        "view_users", "manage_attendants",
                        "view_pumps",
                        "view_shifts", "open_shift", "close_shift",
                        "view_transactions"
                    )
                ),
                RolePermission(
                    roleId = 5,
                    roleName = "Pump Attendant",
                    description = "Record sales only - no dashboard access",
                    colorHex = "#22C55E",
                    icon = "⛽",
                    permissions = setOf(
                        "make_sale", "view_sales"
                    )
                )
            )
            
            _uiState.update {
                it.copy(
                    roles = defaultRoles,
                    selectedRole = defaultRoles.firstOrNull(),
                    isLoading = false
                )
            }
        }
    }

    fun selectRole(role: RolePermission) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun togglePermission(roleId: Int, permissionId: String) {
        viewModelScope.launch {
            val updatedRoles = _uiState.value.roles.map { role ->
                if (role.roleId == roleId && role.isEditable) {
                    val newPermissions = if (role.permissions.contains(permissionId)) {
                        role.permissions - permissionId
                    } else {
                        role.permissions + permissionId
                    }
                    role.copy(permissions = newPermissions)
                } else role
            }
            
            _uiState.update { 
                it.copy(
                    roles = updatedRoles,
                    selectedRole = updatedRoles.find { it.roleId == roleId },
                    successMessage = "Permission updated"
                )
            }
            
            // Clear success message after delay
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun toggleCategoryPermissions(roleId: Int, category: PermissionCategory, enable: Boolean) {
        viewModelScope.launch {
            val categoryPermissions = AppPermissions.getByCategory(category).map { it.id }.toSet()
            
            val updatedRoles = _uiState.value.roles.map { role ->
                if (role.roleId == roleId && role.isEditable) {
                    val newPermissions = if (enable) {
                        role.permissions + categoryPermissions
                    } else {
                        role.permissions - categoryPermissions
                    }
                    role.copy(permissions = newPermissions)
                } else role
            }
            
            _uiState.update { 
                it.copy(
                    roles = updatedRoles,
                    selectedRole = updatedRoles.find { it.roleId == roleId },
                    successMessage = "${category.name.replace("_", " ")} permissions updated"
                )
            }
            
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun saveRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            // Simulate saving to backend
            kotlinx.coroutines.delay(1000)
            
            _uiState.update { 
                it.copy(
                    isSaving = false,
                    successMessage = "All roles saved successfully!"
                )
            }
            
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun showAddRoleDialog() {
        _uiState.update { it.copy(showAddRoleDialog = true) }
    }

    fun hideAddRoleDialog() {
        _uiState.update { it.copy(showAddRoleDialog = false) }
    }

    fun addRole(name: String, description: String) {
        viewModelScope.launch {
            val newRoleId = (_uiState.value.roles.maxOfOrNull { it.roleId } ?: 0) + 1
            val newRole = RolePermission(
                roleId = newRoleId,
                roleName = name,
                description = description,
                colorHex = "#6366F1",
                icon = "🔰",
                permissions = emptySet(),
                isEditable = true
            )
            
            _uiState.update {
                it.copy(
                    roles = it.roles + newRole,
                    showAddRoleDialog = false,
                    successMessage = "Role '$name' created successfully"
                )
            }
            
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun deleteRole(roleId: Int) {
        viewModelScope.launch {
            val roleToDelete = _uiState.value.roles.find { it.roleId == roleId }
            if (roleToDelete?.isEditable == true) {
                _uiState.update {
                    it.copy(
                        roles = it.roles.filter { role -> role.roleId != roleId },
                        selectedRole = it.roles.firstOrNull { role -> role.roleId != roleId },
                        successMessage = "Role deleted"
                    )
                }
                
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(successMessage = null) }
            }
        }
    }

    fun hasPermission(roleName: String, permissionId: String): Boolean {
        val role = _uiState.value.roles.find { it.roleName == roleName }
        return role?.permissions?.contains(permissionId) == true
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

/**
 * Helper object to check permissions at runtime
 * Can be used in any screen to check if current user has a permission
 */
object PermissionChecker {
    private var currentUserRole: String = ""
    private var rolePermissions: Map<String, Set<String>> = emptyMap()
    
    fun setCurrentRole(roleName: String, permissions: Set<String>) {
        currentUserRole = roleName
        rolePermissions = mapOf(roleName to permissions)
    }
    
    fun hasPermission(permissionId: String): Boolean {
        return rolePermissions[currentUserRole]?.contains(permissionId) == true
    }
    
    fun canViewDashboard(): Boolean = hasPermission("view_dashboard")
    fun canMakeSale(): Boolean = hasPermission("make_sale")
    fun canViewReports(): Boolean = hasPermission("view_reports")
    fun canManageUsers(): Boolean = hasPermission("create_user") || hasPermission("edit_user")
    fun canManagePumps(): Boolean = hasPermission("create_pump") || hasPermission("edit_pump")
    fun canManageShifts(): Boolean = hasPermission("open_shift") || hasPermission("close_shift")
    fun canManageSettings(): Boolean = hasPermission("manage_roles") || hasPermission("system_config")
}
