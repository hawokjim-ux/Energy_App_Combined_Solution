package com.energyapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Admin Dashboard Screen - Airtel Style Light Theme
 * Shows real data from backend services
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    fullName: String,
    totalSales: String = "0",
    totalAmount: String = "KES 0",
    activePumps: Int = 0,
    activeUsers: Int = 0,
    activeShifts: Int = 0,
    mPesaStatus: String = "Active",
    onNavigateToUserManagement: () -> Unit,
    onNavigateToShiftManagement: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToPumpAttendants: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Admin Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.Rounded.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    modifier = Modifier.shadow(elevation = 4.dp)
                )
            },
            containerColor = Color(0xFFF5F5F5),
            contentColor = Color.Black
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Admin Profile Card
                item {
                    AdminProfileCard(
                        adminName = fullName
                    )
                }

                // Sales Overview Cards
                item {
                    SalesOverviewCards(
                        totalSales = totalSales,
                        totalAmount = totalAmount,
                        onViewReports = onNavigateToReports,
                        onRecordSale = onNavigateToSales
                    )
                }

                // System Overview Cards
                item {
                    SystemOverviewCards(
                        activePumps = activePumps,
                        activeUsers = activeUsers,
                        activeShifts = activeShifts
                    )
                }

                // Quick Actions Section
                item {
                    QuickActionsHeader()
                }

                item {
                    QuickActionsGrid(
                        onUserManagement = onNavigateToUserManagement,
                        onShiftManagement = onNavigateToShiftManagement,
                        onReports = onNavigateToReports,
                        onSales = onNavigateToSales,
                        onAttendants = onNavigateToPumpAttendants
                    )
                }

                // System Status Card
                item {
                    SystemStatusCard(
                        mPesaStatus = mPesaStatus,
                        activePumps = activePumps,
                        activeUsers = activeUsers
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ==================== PROFILE CARD ====================

@Composable
fun AdminProfileCard(adminName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Welcome, Admin",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
                fontSize = 12.sp
            )
            Text(
                text = adminName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )
            Text(
                text = "System Administrator",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1976D2),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== SALES OVERVIEW ====================

@Composable
fun SalesOverviewCards(
    totalSales: String,
    totalAmount: String,
    onViewReports: () -> Unit,
    onRecordSale: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sales Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Amount Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.AttachMoney,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Today's Sales",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF999999),
                        fontSize = 11.sp
                    )
                    Text(
                        text = totalAmount,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFE53935)
                    )
                }
            }

            // Transaction Count Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF999999),
                        fontSize = 11.sp
                    )
                    Text(
                        text = totalSales,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                label = "View Reports",
                icon = Icons.Rounded.Assessment,
                modifier = Modifier.weight(1f),
                onClick = onViewReports
            )
            ActionButton(
                label = "Record Sale",
                icon = Icons.Rounded.Add,
                modifier = Modifier.weight(1f),
                onClick = onRecordSale
            )
        }
    }
}

// ==================== SYSTEM OVERVIEW ====================

@Composable
fun SystemOverviewCards(
    activePumps: Int,
    activeUsers: Int,
    activeShifts: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "System Overview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Active Pumps",
                value = activePumps.toString(),
                icon = Icons.Rounded.LocalGasStation,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Active Users",
                value = activeUsers.toString(),
                icon = Icons.Rounded.People,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Active Shifts",
                value = activeShifts.toString(),
                icon = Icons.Rounded.Schedule,
                color = Color(0xFFFFA500),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color(0xFF999999)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        }
    }
}

// ==================== QUICK ACTIONS ====================

@Composable
fun QuickActionsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = "View All",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            color = Color(0xFF1976D2),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun QuickActionsGrid(
    onUserManagement: () -> Unit,
    onShiftManagement: () -> Unit,
    onReports: () -> Unit,
    onSales: () -> Unit,
    onAttendants: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                label = "Users",
                icon = Icons.Rounded.People,
                modifier = Modifier.weight(1f),
                onClick = onUserManagement
            )
            QuickActionButton(
                label = "Shifts",
                icon = Icons.Rounded.Schedule,
                modifier = Modifier.weight(1f),
                onClick = onShiftManagement
            )
            QuickActionButton(
                label = "Reports",
                icon = Icons.Rounded.Assessment,
                modifier = Modifier.weight(1f),
                onClick = onReports
            )
            QuickActionButton(
                label = "Sales",
                icon = Icons.Rounded.PointOfSale,
                modifier = Modifier.weight(1f),
                onClick = onSales
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                label = "Attendants",
                icon = Icons.Rounded.LocalGasStation,
                modifier = Modifier.weight(1f),
                onClick = onAttendants
            )
            QuickActionButton(
                label = "Settings",
                icon = Icons.Rounded.Settings,
                modifier = Modifier.weight(1f),
                onClick = { }
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== ACTION BUTTON ====================

@Composable
fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE53935))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== SYSTEM STATUS ====================

@Composable
fun SystemStatusCard(
    mPesaStatus: String,
    activePumps: Int,
    activeUsers: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF3E0))
            .border(1.5.dp, Color(0xFFFFE0B2), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFFFFA500),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "System Status",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFFFA500),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "✅ All systems operational\n\n• $activePumps active pumps online\n• $activeUsers registered users\n• M-Pesa Integration: $mPesaStatus\n• Real-time transaction monitoring enabled",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}