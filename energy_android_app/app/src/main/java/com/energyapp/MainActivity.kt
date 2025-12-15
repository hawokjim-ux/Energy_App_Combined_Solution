package com.energyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.energyapp.ui.navigation.NavGraph
import com.energyapp.ui.navigation.Screen
import com.energyapp.ui.theme.EnergyAppTheme
import com.energyapp.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EnergyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
                    val roleName by preferencesManager.roleName.collectAsState(initial = null)

                    // Determine start destination based on login state
                    val startDestination = when {
                        !isLoggedIn -> Screen.Login.route
                        roleName == "Admin" -> Screen.AdminDashboard.route
                        roleName == "Pump Attendant" -> Screen.AttendantDashboard.route
                        else -> Screen.Login.route
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