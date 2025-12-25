package com.energyapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.theme.*

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(
            route = "admin_dashboard",
            label = "Home",
            icon = Icons.Rounded.Home,
            selectedIcon = Icons.Filled.Home
        ),
        NavItem(
            route = "transactions",
            label = "Txns",
            icon = Icons.Rounded.Receipt,
            selectedIcon = Icons.Filled.Receipt
        ),
        NavItem(
            route = "reports",
            label = "Reports",
            icon = Icons.Rounded.Assessment,
            selectedIcon = Icons.Filled.Assessment
        ),
        NavItem(
            route = "pump_management",
            label = "Pumps",
            icon = Icons.Rounded.LocalGasStation,
            selectedIcon = Icons.Filled.LocalGasStation
        ),
        NavItem(
            route = "attendant_management",
            label = "Staff",
            icon = Icons.Rounded.People,
            selectedIcon = Icons.Filled.People
        )
    )

    NavigationBar(
        modifier = modifier,
        containerColor = CardBackground,
        contentColor = OnSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LightPrimary,
                    selectedTextColor = LightPrimary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = LightPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun AttendantBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(
            route = "attendant_dashboard",
            label = "Home",
            icon = Icons.Rounded.Home,
            selectedIcon = Icons.Filled.Home
        ),
        NavItem(
            route = "sales",
            label = "Sales",
            icon = Icons.Rounded.PointOfSale,
            selectedIcon = Icons.Filled.PointOfSale
        ),
        NavItem(
            route = "settings",
            label = "Settings",
            icon = Icons.Rounded.Settings,
            selectedIcon = Icons.Filled.Settings
        )
    )

    NavigationBar(
        modifier = modifier,
        containerColor = CardBackground,
        contentColor = OnSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LightPrimary,
                    selectedTextColor = LightPrimary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = LightPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}
