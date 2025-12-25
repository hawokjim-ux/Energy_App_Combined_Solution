package com.energyapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val FULL_NAME_KEY = stringPreferencesKey("full_name")
        private val ROLE_NAME_KEY = stringPreferencesKey("role_name")
        private val ROLE_ID_KEY = intPreferencesKey("role_id")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val USER_PERMISSIONS_KEY = stringPreferencesKey("user_permissions")
        
        // License keys
        private val LICENSE_KEY = stringPreferencesKey("license_key")
        private val LICENSE_TYPE_CODE_KEY = stringPreferencesKey("license_type_code")
        private val LICENSE_DEVICE_ID_KEY = stringPreferencesKey("license_device_id")
        private val LICENSE_ACTIVATION_DATE_KEY = longPreferencesKey("license_activation_date")
        private val LICENSE_EXPIRATION_DATE_KEY = longPreferencesKey("license_expiration_date")
        private val LICENSE_MAX_DEVICES_KEY = intPreferencesKey("license_max_devices")
    }

    // Save user session with role and permissions
    suspend fun saveUserSession(
        userId: Int,
        username: String,
        fullName: String,
        roleName: String,
        roleId: Int = 0,
        permissions: Set<String> = emptySet()
    ) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[FULL_NAME_KEY] = fullName
            preferences[ROLE_NAME_KEY] = roleName
            preferences[ROLE_ID_KEY] = roleId
            preferences[IS_LOGGED_IN_KEY] = true
            // Store permissions as comma-separated string
            preferences[USER_PERMISSIONS_KEY] = permissions.joinToString(",")
        }
    }

    // Save role permissions separately (for runtime updates)
    suspend fun saveUserPermissions(permissions: Set<String>) {
        dataStore.edit { preferences ->
            preferences[USER_PERMISSIONS_KEY] = permissions.joinToString(",")
        }
    }

    // Clear user session
    suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            // Keep theme preference when logging out
            val themeMode = preferences[THEME_MODE_KEY]
            preferences.clear()
            themeMode?.let { preferences[THEME_MODE_KEY] = it }
        }
    }

    // Theme mode preference
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    val themeMode: Flow<String?> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY]
    }

    // Read user session data
    val userId: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val username: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    val fullName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[FULL_NAME_KEY]
    }

    val roleName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ROLE_NAME_KEY]
    }

    val roleId: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[ROLE_ID_KEY]
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    // User permissions as Flow
    val userPermissions: Flow<Set<String>> = dataStore.data.map { preferences ->
        val permissionsString = preferences[USER_PERMISSIONS_KEY] ?: ""
        if (permissionsString.isBlank()) emptySet()
        else permissionsString.split(",").toSet()
    }

    // Synchronous permission check (use in composables with collectAsState)
    suspend fun hasPermission(permissionId: String): Boolean {
        val permissions = userPermissions.first()
        return permissions.contains(permissionId)
    }

    // Get current role name synchronously
    suspend fun getCurrentRoleName(): String {
        return roleName.first() ?: "Unknown"
    }

    // Permission helper functions
    suspend fun canViewDashboard(): Boolean = hasPermission("view_dashboard")
    suspend fun canMakeSale(): Boolean = hasPermission("make_sale")
    suspend fun canViewReports(): Boolean = hasPermission("view_reports")
    suspend fun canManageUsers(): Boolean = hasPermission("create_user") || hasPermission("edit_user")
    suspend fun canManagePumps(): Boolean = hasPermission("create_pump") || hasPermission("edit_pump")
    suspend fun canManageShifts(): Boolean = hasPermission("open_shift") || hasPermission("close_shift")
    suspend fun canManageSettings(): Boolean = hasPermission("manage_roles") || hasPermission("system_config")
    suspend fun canViewTransactions(): Boolean = hasPermission("view_transactions")

    // ==================== LICENSE MANAGEMENT ====================

    // License data flows
    val licenseKey: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LICENSE_KEY]
    }

    val licenseTypeCode: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LICENSE_TYPE_CODE_KEY]
    }

    val licenseDeviceId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LICENSE_DEVICE_ID_KEY]
    }

    val licenseActivationDate: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LICENSE_ACTIVATION_DATE_KEY]
    }

    val licenseExpirationDate: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LICENSE_EXPIRATION_DATE_KEY]
    }

    val licenseMaxDevices: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[LICENSE_MAX_DEVICES_KEY]
    }

    val isLicensed: Flow<Boolean> = dataStore.data.map { preferences ->
        val key = preferences[LICENSE_KEY]
        val expiration = preferences[LICENSE_EXPIRATION_DATE_KEY] ?: 0L
        !key.isNullOrEmpty() && expiration > System.currentTimeMillis()
    }

    // Save license information
    suspend fun saveLicense(
        licenseKey: String,
        licenseTypeCode: String,
        deviceId: String,
        activationDate: Long,
        expirationDate: Long,
        maxDevices: Int
    ) {
        dataStore.edit { preferences ->
            preferences[LICENSE_KEY] = licenseKey
            preferences[LICENSE_TYPE_CODE_KEY] = licenseTypeCode
            preferences[LICENSE_DEVICE_ID_KEY] = deviceId
            preferences[LICENSE_ACTIVATION_DATE_KEY] = activationDate
            preferences[LICENSE_EXPIRATION_DATE_KEY] = expirationDate
            preferences[LICENSE_MAX_DEVICES_KEY] = maxDevices
        }
    }

    // Clear license (revoke)
    suspend fun clearLicense() {
        dataStore.edit { preferences ->
            preferences.remove(LICENSE_KEY)
            preferences.remove(LICENSE_TYPE_CODE_KEY)
            preferences.remove(LICENSE_DEVICE_ID_KEY)
            preferences.remove(LICENSE_ACTIVATION_DATE_KEY)
            preferences.remove(LICENSE_EXPIRATION_DATE_KEY)
            preferences.remove(LICENSE_MAX_DEVICES_KEY)
        }
    }
}