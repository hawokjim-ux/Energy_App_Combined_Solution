package com.energyapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object AttendantDashboard : Screen("attendant_dashboard")
    object UserManagement : Screen("user_management")
    object ShiftManagement : Screen("shift_management")
    object Sales : Screen("sales")
    object Reports : Screen("reports")
    object PumpAttendants : Screen("pump_attendants")
}