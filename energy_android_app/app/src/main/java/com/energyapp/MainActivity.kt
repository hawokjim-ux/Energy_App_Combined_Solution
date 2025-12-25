package com.energyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.energyapp.ui.navigation.NavGraph
import com.energyapp.ui.navigation.Screen
import com.energyapp.ui.screens.license.LicenseActivationScreen
import com.energyapp.ui.screens.license.SuperUserLoginScreen
import com.energyapp.ui.screens.license.LicenseManagementScreen
import com.energyapp.ui.theme.EnergyAppTheme
import com.energyapp.ui.theme.ThemeMode
import com.energyapp.util.LicenseManager
import com.energyapp.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Hardcoded Superuser Credentials for License Generation
 * This user can bypass license check and generate licenses
 */
object SuperUserCredentials {
    const val USERNAME = "superuser"
    const val PASSWORD = "@jiM43K450"
    
    fun validate(username: String, password: String): Boolean {
        return username.trim().lowercase() == USERNAME.lowercase() && password == PASSWORD
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var licenseManager: LicenseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Collect theme mode from preferences
            val themeModeString by preferencesManager.themeMode.collectAsState(initial = "light")
            val themeMode = when (themeModeString) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.LIGHT
            }
            
            // License check state
            var isLicenseValid by remember { mutableStateOf<Boolean?>(null) }
            var isSuperUserLoggedIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            
            // Check license on startup
            LaunchedEffect(Unit) {
                isLicenseValid = licenseManager.isLicenseValid()
            }
            
            EnergyAppTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        // SuperUser logged in - show license management directly
                        isSuperUserLoggedIn -> {
                            LicenseManagementScreen(
                                licenseManager = licenseManager,
                                onNavigateBack = {
                                    isSuperUserLoggedIn = false
                                }
                            )
                        }
                        
                        // Still checking license
                        isLicenseValid == null -> {
                            // Loading state - show nothing
                        }
                        
                        // License NOT valid - show activation screen with superuser option
                        isLicenseValid == false -> {
                            LicenseActivationScreen(
                                licenseManager = licenseManager,
                                onLicenseActivated = {
                                    scope.launch {
                                        isLicenseValid = licenseManager.isLicenseValid()
                                    }
                                },
                                onSuperUserLogin = { username, password ->
                                    if (SuperUserCredentials.validate(username, password)) {
                                        isSuperUserLoggedIn = true
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )
                        }
                        
                        // License valid - show normal app flow
                        isLicenseValid == true -> {
                            val navController = rememberNavController()
                            val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
                            val roleName by preferencesManager.roleName.collectAsState(initial = null)
                            val userPermissions by preferencesManager.userPermissions.collectAsState(initial = emptySet())

                            val startDestination = when {
                                !isLoggedIn -> Screen.Login.route
                                userPermissions.contains("view_dashboard") -> Screen.AdminDashboard.route
                                roleName == "Admin" || roleName == "Super Admin" || 
                                roleName == "Manager" || roleName == "Director" || 
                                roleName == "Supervisor" -> Screen.AdminDashboard.route
                                roleName == "Pump Attendant" -> Screen.AttendantDashboard.route
                                else -> Screen.AttendantDashboard.route
                            }

                            NavGraph(
                                navController = navController,
                                preferencesManager = preferencesManager,
                                startDestination = startDestination
                            )
                        }
                    }
                }
            }
        }
    }
}