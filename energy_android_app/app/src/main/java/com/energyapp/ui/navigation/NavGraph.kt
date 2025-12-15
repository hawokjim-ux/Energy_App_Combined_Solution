package com.energyapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.energyapp.ui.screens.admin.*
import com.energyapp.ui.screens.attendant.AttendantDashboardScreen
import com.energyapp.ui.screens.attendant.SalesScreen
import com.energyapp.ui.screens.attendant.SalesViewModel
import com.energyapp.ui.screens.login.LoginScreen
import com.energyapp.ui.screens.login.LoginViewModel
import com.energyapp.ui.screens.PumpAttendantsScreen
import com.energyapp.ui.viewmodels.PumpAttendantsViewModel
import com.energyapp.util.Constants
import com.energyapp.util.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { roleName ->
                    if (roleName == "Admin") {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.AttendantDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            val fullName by preferencesManager.fullName.collectAsState(initial = "Admin")
            AdminDashboardScreen(
                fullName = fullName ?: "Admin",
                onNavigateToUserManagement = {
                    navController.navigate(Screen.UserManagement.route)
                },
                onNavigateToShiftManagement = {
                    navController.navigate(Screen.ShiftManagement.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onNavigateToSales = {
                    navController.navigate(Screen.Sales.route)
                },
                onNavigateToPumpAttendants = {
                    navController.navigate(Screen.PumpAttendants.route)
                },
                onLogout = {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        preferencesManager.clearUserSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.AttendantDashboard.route) {
            val fullName by preferencesManager.fullName.collectAsState(initial = "Attendant")
            AttendantDashboardScreen(
                fullName = fullName ?: "Attendant",
                onNavigateToSales = {
                    navController.navigate(Screen.Sales.route)
                },
                onLogout = {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        preferencesManager.clearUserSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.UserManagement.route) {
            val viewModel: UserManagementViewModel = hiltViewModel()
            UserManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ShiftManagement.route) {
            val userId by preferencesManager.userId.collectAsState(initial = null)
            if (userId != null) {
                val viewModel: ShiftManagementViewModel = hiltViewModel()
                viewModel.setUserId(userId!!)
                ShiftManagementScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Sales.route) {
            val userId by preferencesManager.userId.collectAsState(initial = null)
            if (userId != null) {
                val viewModel: SalesViewModel = hiltViewModel()
                viewModel.setUserId(userId!!)
                SalesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Reports.route) {
            val viewModel: ReportsViewModel = hiltViewModel()
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PumpAttendants.route) {
            val viewModel: PumpAttendantsViewModel = hiltViewModel()
            PumpAttendantsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}