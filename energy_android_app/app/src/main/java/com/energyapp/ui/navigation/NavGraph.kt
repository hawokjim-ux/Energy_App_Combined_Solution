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
import com.energyapp.ui.screens.settings.SettingsScreen
import com.energyapp.ui.screens.settings.SettingsViewModel
import com.energyapp.ui.screens.settings.UserRolesScreen
import com.energyapp.ui.screens.settings.UserRolesViewModel
import com.energyapp.ui.screens.license.LicenseManagementScreen
import com.energyapp.ui.screens.license.LicenseViewModel
import com.energyapp.ui.viewmodels.PumpAttendantsViewModel
import com.energyapp.util.Constants
import com.energyapp.util.LicenseManager
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
                    // Navigate based on role - use permissions for access control
                    when (roleName?.lowercase()) {
                        "pump attendant", "attendant" -> {
                            navController.navigate(Screen.AttendantDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        else -> {
                            // Admin, Manager, Supervisor, Director, Super Admin
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            val fullName by preferencesManager.fullName.collectAsState(initial = "Admin")
            val roleName by preferencesManager.roleName.collectAsState(initial = null)
            val viewModel: AdminDashboardViewModel = hiltViewModel()
            
            // Check if user is Super Admin
            val isSuperAdmin = roleName?.lowercase() == "super admin"
            
            AdminDashboardScreen(
                viewModel = viewModel,
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
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToPumpManagement = {
                    navController.navigate(Screen.PumpManagement.route)
                },
                onNavigateToShiftDefinition = {
                    navController.navigate(Screen.ShiftDefinition.route)
                },
                onNavigateToAttendantManagement = {
                    navController.navigate(Screen.AttendantManagement.route)
                },
                onNavigateToUserLoginManagement = {
                    navController.navigate(Screen.UserLoginManagement.route)
                },
                onNavigateToLicenseManagement = {
                    navController.navigate(Screen.LicenseManagement.route)
                },
                onNavigateToLicenseList = {
                    navController.navigate(Screen.LicenseList.route)
                },
                onNavigateToMultiStationDashboard = {
                    navController.navigate(Screen.MultiStationDashboard.route)
                },
                isSuperAdmin = isSuperAdmin,
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
            val fullName by preferencesManager.fullName.collectAsState(initial = "")
            if (userId != null) {
                val viewModel: SalesViewModel = hiltViewModel()
                // Pass both userId and fullName to the ViewModel
                viewModel.setUserInfo(userId!!, fullName ?: "Attendant")
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

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserRoles = {
                    navController.navigate(Screen.UserRoles.route)
                }
            )
        }

        composable(Screen.UserRoles.route) {
            val viewModel: UserRolesViewModel = hiltViewModel()
            UserRolesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Transactions.route) {
            val viewModel: TransactionsViewModel = hiltViewModel()
            TransactionsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PumpManagement.route) {
            val viewModel: PumpManagementViewModel = hiltViewModel()
            PumpManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.ShiftDefinition.route) {
            val viewModel: ShiftDefinitionViewModel = hiltViewModel()
            ShiftDefinitionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AttendantManagement.route) {
            val viewModel: AttendantManagementViewModel = hiltViewModel()
            AttendantManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.UserLoginManagement.route) {
            val viewModel: LoginManagementViewModel = hiltViewModel()
            LoginManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Super Admin only - License Management (Generate)
        composable(Screen.LicenseManagement.route) {
            val viewModel: LicenseViewModel = hiltViewModel()
            LicenseManagementScreen(
                licenseManager = viewModel.licenseManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Super Admin only - License List (View All)
        composable(Screen.LicenseList.route) {
            val viewModel: LicenseViewModel = hiltViewModel()
            com.energyapp.ui.screens.license.LicenseListScreen(
                licenseManager = viewModel.licenseManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Super Admin only - Multi-Station Dashboard (View All Stations)
        composable(Screen.MultiStationDashboard.route) {
            MultiStationDashboardScreen(
                onNavigateToStation = { stationId ->
                    // Navigate to specific station details (can be implemented later)
                    // For now, just go back
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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
    }
}