package com.energyapp.ui.navigation

sealed class Screen(val route: String) {
    object License : Screen("license")
    object LicenseManagement : Screen("license_management")
    object LicenseList : Screen("license_list")  // View All Licenses - Super Admin
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object AttendantDashboard : Screen("attendant_dashboard")
    object UserManagement : Screen("user_management")
    object ShiftManagement : Screen("shift_management")
    object Sales : Screen("sales")
    object Reports : Screen("reports")
    object PumpAttendants : Screen("pump_attendants")
    object Settings : Screen("settings")
    object UserRoles : Screen("user_roles")
    object Transactions : Screen("transactions")
    object PumpManagement : Screen("pump_management")
    object ShiftDefinition : Screen("shift_definition")
    object AttendantManagement : Screen("attendant_management")
    object UserLoginManagement : Screen("user_login_management")
}