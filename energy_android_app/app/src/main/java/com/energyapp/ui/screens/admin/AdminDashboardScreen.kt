package com.energyapp.ui.screens.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.theme.*
import com.energyapp.ui.components.BottomNavBar

/**
 * Admin Dashboard Screen - Super Modern 2024 Design
 * Features: Glassmorphism, Vibrant Gradients, Bento Grid, Neon Accents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    fullName: String,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToShiftManagement: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToPumpAttendants: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToPumpManagement: () -> Unit = {},
    onNavigateToShiftDefinition: () -> Unit = {},
    onNavigateToAttendantManagement: () -> Unit = {},
    onNavigateToUserLoginManagement: () -> Unit = {},
    onNavigateToLicenseManagement: () -> Unit = {},  // Super Admin only - Generate
    onNavigateToLicenseList: () -> Unit = {},  // Super Admin only - View All
    onNavigateToMultiStationDashboard: () -> Unit = {},  // Super Admin only - Multi-Station
    isSuperAdmin: Boolean = false,  // Controls visibility of License Management
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(fullName) {
        viewModel.setFullName(fullName)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Scaffold(
            topBar = {
                // =============== STUNNING MESH GRADIENT HEADER ===============
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GradientPurple,
                                    GradientCyan,
                                    GradientPink
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back,",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = fullName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Glassmorphism Refresh Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { viewModel.refresh() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // Glassmorphism Logout Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { onLogout() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = LightBackground
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GradientPurple,
                        strokeWidth = 3.dp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Today's Sales Cards
                    item {
                        ModernSalesCards(
                            totalSales = uiState.todayTotalSales,
                            transactionCount = uiState.todayTransactionCount,
                            mpesaSales = uiState.todayMpesaSales,
                            onViewReports = onNavigateToReports,
                            onRecordSale = onNavigateToSales
                        )
                    }

                    // System Overview
                    item {
                        ModernSystemOverviewCards(
                            activePumps = uiState.activePumps,
                            activeUsers = uiState.activeUsers,
                            activeShifts = uiState.activeShifts
                        )
                    }

                    // Pump Sales Section
                    if (uiState.pumpSalesData.isNotEmpty()) {
                        item {
                            ModernSectionHeader(title = "Pump Sales Today")
                        }
                        item {
                            ModernPumpSalesCards(pumps = uiState.pumpSalesData)
                        }
                    }

                    // Quick Actions
                    item {
                        ModernSectionHeader(title = "Quick Actions")
                    }
                    item {
                        ModernQuickActionsGrid(
                            onUserManagement = onNavigateToUserManagement,
                            onShiftManagement = onNavigateToShiftManagement,
                            onReports = onNavigateToReports,
                            onSales = onNavigateToSales,
                            onAttendants = onNavigateToPumpAttendants,
                            onSettings = onNavigateToSettings,
                            onPumpManagement = onNavigateToPumpManagement,
                            onShiftDefinition = onNavigateToShiftDefinition,
                            onAttendantManagement = onNavigateToAttendantManagement,
                            onUserLoginManagement = onNavigateToUserLoginManagement,
                            onLicenseManagement = onNavigateToLicenseManagement,
                            onLicenseList = onNavigateToLicenseList,
                            onMultiStationDashboard = onNavigateToMultiStationDashboard,
                            isSuperAdmin = isSuperAdmin
                        )
                    }

                    // System Status
                    item {
                        ModernSystemStatusCard(
                            mPesaStatus = uiState.mPesaStatus,
                            activePumps = uiState.activePumps,
                            activeUsers = uiState.activeUsers
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ==================== MODERN SALES CARDS WITH GLASSMORPHISM ====================

@Composable
fun ModernSalesCards(
    totalSales: Double,
    transactionCount: Int,
    mpesaSales: Double,
    onViewReports: () -> Unit,
    onRecordSale: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Sales Card with Cyan Accent
            ModernStatCard(
                icon = Icons.Rounded.AttachMoney,
                label = "Today's Sales",
                value = "KES ${String.format("%,.0f", totalSales)}",
                accentColor = AccentCyan,
                gradientColors = listOf(IconGradientCyan1, IconGradientCyan2),
                modifier = Modifier.weight(1f)
            )
            
            // Transaction Count Card with Purple Accent
            ModernStatCard(
                icon = Icons.Rounded.SwapHoriz,
                label = "Transactions",
                value = transactionCount.toString(),
                accentColor = AccentPurple,
                gradientColors = listOf(IconGradientPurple1, IconGradientPurple2),
                modifier = Modifier.weight(1f)
            )
        }

        // M-Pesa Card with Live Badge
        ModernMpesaCard(mpesaSales = mpesaSales)

        // Gradient Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernGradientButton(
                label = "View Reports",
                icon = Icons.Rounded.Assessment,
                gradientColors = listOf(ButtonGradient1Start, ButtonGradient1End),
                modifier = Modifier.weight(1f),
                onClick = onViewReports
            )
            ModernGradientButton(
                label = "Record Sale",
                icon = Icons.Rounded.Add,
                gradientColors = listOf(ButtonGradient2Start, ButtonGradient2End),
                modifier = Modifier.weight(1f),
                onClick = onRecordSale
            )
        }
    }
}

@Composable
fun ModernStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = accentColor.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Glowing Accent Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(100.dp)
                    .background(
                        brush = Brush.verticalGradient(gradientColors),
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gradient Icon Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(gradientColors),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ModernMpesaCard(mpesaSales: Double) {
    // Animated pulse for LIVE badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = AccentGreen.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Green Accent Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(IconGradientGreen1, IconGradientGreen2)
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Gradient Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(IconGradientGreen1, IconGradientGreen2)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.PhoneAndroid,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "M-Pesa Collections",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "KES ${String.format("%,.0f", mpesaSales)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                }
                // Animated LIVE Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Success.copy(alpha = pulseAlpha * 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = Success.copy(alpha = pulseAlpha),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = "LIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernGradientButton(
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = gradientColors[0].copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// ==================== MODERN SYSTEM OVERVIEW ====================

@Composable
fun ModernSystemOverviewCards(
    activePumps: Int,
    activeUsers: Int,
    activeShifts: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ModernSectionHeader(title = "System Overview")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoStatCard(
                label = "Pumps",
                value = activePumps.toString(),
                icon = Icons.Rounded.LocalGasStation,
                gradientColors = listOf(IconGradientCyan1, IconGradientCyan2),
                modifier = Modifier.weight(1f)
            )
            BentoStatCard(
                label = "Users",
                value = activeUsers.toString(),
                icon = Icons.Rounded.People,
                gradientColors = listOf(IconGradientPurple1, IconGradientPurple2),
                modifier = Modifier.weight(1f)
            )
            BentoStatCard(
                label = "Shifts",
                value = activeShifts.toString(),
                icon = Icons.Rounded.Schedule,
                gradientColors = listOf(IconGradientOrange1, IconGradientOrange2),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BentoStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = gradientColors[0].copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gradient Icon with Glow Ring
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

// ==================== MODERN PUMP SALES ====================

@Composable
fun ModernPumpSalesCards(pumps: List<PumpSalesData>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(pumps) { pump ->
            ModernPumpCard(pump)
        }
    }
}

@Composable
fun ModernPumpCard(pump: PumpSalesData) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = GradientCyan.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(IconGradientTeal1, IconGradientTeal2)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.LocalGasStation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GradientCyan.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = pump.pumpNo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GradientCyan
                    )
                }
            }
            
            Text(
                text = pump.pumpName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "KES ${String.format("%,.0f", pump.totalSales)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("M-Pesa", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "KES ${String.format("%,.0f", pump.mpesaSales)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Txns", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        pump.transactionCount.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                }
            }
        }
    }
}

// ==================== MODERN SECTION HEADER ====================

@Composable
fun ModernSectionHeader(title: String, showViewAll: Boolean = false, onViewAll: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        if (showViewAll) {
            Text(
                text = "View All",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GradientPurple,
                modifier = Modifier.clickable { onViewAll() }
            )
        }
    }
}

// ==================== MODERN QUICK ACTIONS BENTO GRID ====================

@Composable
fun ModernQuickActionsGrid(
    onUserManagement: () -> Unit,
    onShiftManagement: () -> Unit,
    onReports: () -> Unit,
    onSales: () -> Unit,
    onAttendants: () -> Unit,
    onSettings: () -> Unit,
    onPumpManagement: () -> Unit = {},
    onShiftDefinition: () -> Unit = {},
    onAttendantManagement: () -> Unit = {},
    onUserLoginManagement: () -> Unit = {},
    onLicenseManagement: () -> Unit = {},  // Super Admin only - Generate
    onLicenseList: () -> Unit = {},  // Super Admin only - View All
    onMultiStationDashboard: () -> Unit = {},  // Super Admin only - Multi-Station
    isSuperAdmin: Boolean = false  // Controls visibility of License Management
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ModernQuickActionCard(
                label = "Users",
                icon = Icons.Rounded.People,
                gradientColors = listOf(IconGradientPurple1, IconGradientPurple2),
                modifier = Modifier.weight(1f),
                onClick = onUserManagement
            )
            ModernQuickActionCard(
                label = "Shifts",
                icon = Icons.Rounded.Schedule,
                gradientColors = listOf(IconGradientOrange1, IconGradientOrange2),
                modifier = Modifier.weight(1f),
                onClick = onShiftManagement
            )
            ModernQuickActionCard(
                label = "Reports",
                icon = Icons.Rounded.Assessment,
                gradientColors = listOf(IconGradientPink1, IconGradientPink2),
                modifier = Modifier.weight(1f),
                onClick = onReports
            )
            ModernQuickActionCard(
                label = "Sales",
                icon = Icons.Rounded.PointOfSale,
                gradientColors = listOf(IconGradientCyan1, IconGradientCyan2),
                modifier = Modifier.weight(1f),
                onClick = onSales
            )
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ModernQuickActionCard(
                label = "Pumps",
                icon = Icons.Rounded.LocalGasStation,
                gradientColors = listOf(IconGradientTeal1, IconGradientTeal2),
                modifier = Modifier.weight(1f),
                onClick = onPumpManagement
            )
            ModernQuickActionCard(
                label = "Shift Defs",
                icon = Icons.Rounded.AccessTime,
                gradientColors = listOf(IconGradientBlue1, IconGradientBlue2),
                modifier = Modifier.weight(1f),
                onClick = onShiftDefinition
            )
            ModernQuickActionCard(
                label = "Login",
                icon = Icons.Rounded.PhoneAndroid,
                gradientColors = listOf(IconGradientGreen1, IconGradientGreen2),
                modifier = Modifier.weight(1f),
                onClick = onUserLoginManagement
            )
            ModernQuickActionCard(
                label = "Settings",
                icon = Icons.Rounded.Settings,
                gradientColors = listOf(GradientPurple, NeonPink),
                modifier = Modifier.weight(1f),
                onClick = onSettings
            )
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ModernQuickActionCard(
                label = "Admin",
                icon = Icons.Rounded.AdminPanelSettings,
                gradientColors = listOf(QuickActionAdmin, NeonPurple),
                modifier = Modifier.weight(1f),
                onClick = onAttendants
            )
            ModernQuickActionCard(
                label = "Attendants",
                icon = Icons.Rounded.Badge,
                gradientColors = listOf(QuickActionAttendant, NeonCyan),
                modifier = Modifier.weight(1f),
                onClick = onAttendantManagement
            )
            // License Buttons - SUPER ADMIN ONLY
            if (isSuperAdmin) {
                ModernQuickActionCard(
                    label = "Generate",
                    icon = Icons.Rounded.Add,
                    gradientColors = listOf(Color(0xFFFFD700), Color(0xFFFFA500)), // Gold gradient
                    modifier = Modifier.weight(1f),
                    onClick = onLicenseManagement
                )
                ModernQuickActionCard(
                    label = "View All",
                    icon = Icons.Rounded.List,
                    gradientColors = listOf(Color(0xFFFF6B35), Color(0xFFFF8C42)), // Orange gradient
                    modifier = Modifier.weight(1f),
                    onClick = onLicenseList
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Row 4 - SUPER ADMIN ONLY - Multi-Station Dashboard
        if (isSuperAdmin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Multi-Station Dashboard - Prominent Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFF00D4FF).copy(alpha = 0.3f)
                        )
                        .clickable { onMultiStationDashboard() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2),
                                        Color(0xFFFF6B9D)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Business,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🏢 Multi-Station Dashboard",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "View all 50+ stations • Aggregated sales • Real-time status",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Icon(
                                Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernQuickActionCard(
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = gradientColors[0].copy(alpha = 0.2f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gradient Icon Circle with Glow Effect
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ==================== MODERN SYSTEM STATUS ====================

@Composable
fun ModernSystemStatusCard(
    mPesaStatus: String,
    activePumps: Int,
    activeUsers: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Success.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuccessLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(IconGradientGreen1, IconGradientGreen2)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "System Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Success
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusRow(icon = Icons.Rounded.Check, text = "All systems operational")
                StatusRow(icon = Icons.Rounded.LocalGasStation, text = "$activePumps active pumps online")
                StatusRow(icon = Icons.Rounded.People, text = "$activeUsers registered users")
                StatusRow(icon = Icons.Rounded.PhoneAndroid, text = "M-Pesa Integration: $mPesaStatus")
                StatusRow(icon = Icons.Rounded.Speed, text = "Real-time monitoring enabled")
            }
        }
    }
}

@Composable
fun StatusRow(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = OnSurface.copy(alpha = 0.85f)
        )
    }
}

// Keep legacy function names for compatibility
@Composable
fun TodaySalesCards(
    totalSales: Double,
    transactionCount: Int,
    mpesaSales: Double,
    onViewReports: () -> Unit,
    onRecordSale: () -> Unit
) = ModernSalesCards(totalSales, transactionCount, mpesaSales, onViewReports, onRecordSale)

@Composable
fun SystemOverviewCards(
    activePumps: Int,
    activeUsers: Int,
    activeShifts: Int
) = ModernSystemOverviewCards(activePumps, activeUsers, activeShifts)

@Composable
fun SectionHeader(title: String, showViewAll: Boolean = false, onViewAll: () -> Unit = {}) = 
    ModernSectionHeader(title, showViewAll, onViewAll)

@Composable
fun QuickActionsGrid(
    onUserManagement: () -> Unit,
    onShiftManagement: () -> Unit,
    onReports: () -> Unit,
    onSales: () -> Unit,
    onAttendants: () -> Unit,
    onSettings: () -> Unit,
    onPumpManagement: () -> Unit = {},
    onShiftDefinition: () -> Unit = {},
    onAttendantManagement: () -> Unit = {},
    onUserLoginManagement: () -> Unit = {}
) = ModernQuickActionsGrid(
    onUserManagement, onShiftManagement, onReports, onSales,
    onAttendants, onSettings, onPumpManagement, onShiftDefinition,
    onAttendantManagement, onUserLoginManagement
)

@Composable
fun PumpSalesCards(pumps: List<PumpSalesData>) = ModernPumpSalesCards(pumps)

@Composable
fun SystemStatusCard(
    mPesaStatus: String,
    activePumps: Int,
    activeUsers: Int
) = ModernSystemStatusCard(mPesaStatus, activePumps, activeUsers)

@Composable
fun ActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = ModernGradientButton(label, icon, listOf(ButtonGradient1Start, ButtonGradient1End), modifier, onClick)