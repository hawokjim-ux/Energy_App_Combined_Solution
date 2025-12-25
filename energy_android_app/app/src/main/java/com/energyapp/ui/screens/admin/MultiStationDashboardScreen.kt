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
import androidx.compose.material.icons.outlined.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.energyapp.ui.theme.*

/**
 * Multi-Station Dashboard Screen - Super Modern 2024 Design
 * 
 * Features:
 * - View all 50+ stations at a glance
 * - Station selector dropdown
 * - Aggregate sales across all stations
 * - Per-station breakdown
 * - Day/Night shift summaries
 * - Real-time M-Pesa status per station
 * 
 * Design: Glassmorphism, Vibrant Gradients, Bento Grid, Neon Accents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiStationDashboardScreen(
    viewModel: MultiStationViewModel = hiltViewModel(),
    onNavigateToStation: (stationId: Int) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Scaffold(
            topBar = {
                // =============== STUNNING MULTI-STATION HEADER ===============
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E3A5F),  // Deep Navy
                                    Color(0xFF2E5984),  // Rich Blue
                                    Color(0xFF1E88E5)   // Bright Blue
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🏢 Multi-Station Dashboard",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${uiState.stations.size} Active Stations",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Refresh Button
                                GlassmorphicButton(
                                    icon = Icons.Rounded.Refresh,
                                    onClick = { viewModel.refresh() }
                                )
                                // Settings Button
                                GlassmorphicButton(
                                    icon = Icons.Rounded.Settings,
                                    onClick = onNavigateToSettings
                                )
                                // Logout Button
                                GlassmorphicButton(
                                    icon = Icons.Rounded.ExitToApp,
                                    onClick = onLogout
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // =============== STATION FILTER CHIPS ===============
                        StationFilterChips(
                            selectedFilter = uiState.selectedFilter,
                            onFilterSelected = { viewModel.setFilter(it) }
                        )
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFF1E88E5),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading stations...",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // =============== AGGREGATE SUMMARY CARDS ===============
                    item {
                        AggregateSummarySection(
                            totalStations = uiState.stations.size,
                            totalSales = uiState.totalSales,
                            totalTransactions = uiState.totalTransactions,
                            totalMpesaSales = uiState.totalMpesaSales,
                            dayShiftSales = uiState.dayShiftSales,
                            nightShiftSales = uiState.nightShiftSales
                        )
                    }
                    
                    // =============== SHIFT BREAKDOWN ===============
                    item {
                        ShiftBreakdownCard(
                            dayShiftSales = uiState.dayShiftSales,
                            nightShiftSales = uiState.nightShiftSales,
                            dayShiftTransactions = uiState.dayShiftTransactions,
                            nightShiftTransactions = uiState.nightShiftTransactions
                        )
                    }
                    
                    // =============== TOP PERFORMING STATIONS ===============
                    item {
                        SectionHeader(
                            title = "🏆 Top Performing Stations",
                            subtitle = "Today's sales ranking"
                        )
                    }
                    
                    item {
                        TopStationsRow(
                            stations = uiState.topStations,
                            onStationClick = onNavigateToStation
                        )
                    }
                    
                    // =============== ALL STATIONS GRID ===============
                    item {
                        SectionHeader(
                            title = "📍 All Stations",
                            subtitle = "Tap to view details"
                        )
                    }
                    
                    items(uiState.filteredStations.chunked(2)) { rowStations ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowStations.forEach { station ->
                                StationCard(
                                    station = station,
                                    onClick = { onNavigateToStation(station.stationId) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Add empty space if odd number of stations
                            if (rowStations.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    
                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ==================== GLASSMORPHIC BUTTON ====================

@Composable
fun GlassmorphicButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==================== STATION FILTER CHIPS ====================

@Composable
fun StationFilterChips(
    selectedFilter: StationFilter,
    onFilterSelected: (StationFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(StationFilter.values().toList()) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Text(text = filter.emoji, fontSize = 14.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.White.copy(alpha = 0.3f),
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = Color.White.copy(alpha = 0.8f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.White.copy(alpha = 0.3f),
                    selectedBorderColor = Color.White.copy(alpha = 0.5f),
                    enabled = true,
                    selected = selectedFilter == filter
                )
            )
        }
    }
}

enum class StationFilter(val label: String, val emoji: String) {
    ALL("All Stations", "🏢"),
    ONLINE("Online", "🟢"),
    OFFLINE("Offline", "🔴"),
    TOP_SALES("Top Sales", "📈"),
    NAIROBI("Nairobi", "🏙️"),
    MOMBASA("Mombasa", "🌊"),
    COAST("Coast", "🏖️"),
    CENTRAL("Central", "⛰️"),
    WESTERN("Western", "🌾")
}

// ==================== AGGREGATE SUMMARY SECTION ====================

@Composable
fun AggregateSummarySection(
    totalStations: Int,
    totalSales: Double,
    totalTransactions: Int,
    totalMpesaSales: Double,
    dayShiftSales: Double,
    nightShiftSales: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Main Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Sales Card - Prominent
            AggregateStatCard(
                icon = Icons.Rounded.AttachMoney,
                label = "Total Sales Today",
                value = "KES ${String.format("%,.0f", totalSales)}",
                gradientColors = listOf(Color(0xFF00C853), Color(0xFF1DE9B6)),
                modifier = Modifier.weight(1f)
            )
            
            // Stations Count
            AggregateStatCard(
                icon = Icons.Rounded.LocationCity,
                label = "Active Stations",
                value = totalStations.toString(),
                gradientColors = listOf(Color(0xFF1E88E5), Color(0xFF64B5F6)),
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Transactions
            AggregateStatCard(
                icon = Icons.Rounded.SwapHoriz,
                label = "Transactions",
                value = totalTransactions.toString(),
                gradientColors = listOf(Color(0xFF7C4DFF), Color(0xFFB388FF)),
                modifier = Modifier.weight(1f)
            )
            
            // M-Pesa Collections
            AggregateStatCard(
                icon = Icons.Rounded.PhoneAndroid,
                label = "M-Pesa",
                value = "KES ${String.format("%,.0f", totalMpesaSales)}",
                gradientColors = listOf(Color(0xFF00E676), Color(0xFF69F0AE)),
                modifier = Modifier.weight(1f),
                isLive = true
            )
        }
    }
}

@Composable
fun AggregateStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    isLive: Boolean = false
) {
    // Animated pulse for LIVE indicator
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
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = gradientColors[0].copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                
                // LIVE Badge
                if (isLive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = gradientColors[0].copy(alpha = pulseAlpha * 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = gradientColors[0].copy(alpha = pulseAlpha),
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = "LIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = gradientColors[0]
                            )
                        }
                    }
                }
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

// ==================== SHIFT BREAKDOWN CARD ====================

@Composable
fun ShiftBreakdownCard(
    dayShiftSales: Double,
    nightShiftSales: Double,
    dayShiftTransactions: Int,
    nightShiftTransactions: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFFF9800).copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰ Shift Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "All Stations",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Day Shift Card
                ShiftCard(
                    shiftName = "☀️ Day Shift",
                    sales = dayShiftSales,
                    transactions = dayShiftTransactions,
                    gradientColors = listOf(Color(0xFFFFA726), Color(0xFFFFCC80)),
                    modifier = Modifier.weight(1f)
                )
                
                // Night Shift Card
                ShiftCard(
                    shiftName = "🌙 Night Shift",
                    sales = nightShiftSales,
                    transactions = nightShiftTransactions,
                    gradientColors = listOf(Color(0xFF5C6BC0), Color(0xFF9FA8DA)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ShiftCard(
    shiftName: String,
    sales: Double,
    transactions: Int,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = gradientColors[1].copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = shiftName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = gradientColors[0]
            )
            
            Text(
                text = "KES ${String.format("%,.0f", sales)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.SwapHoriz,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$transactions transactions",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// ==================== SECTION HEADER ====================

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

// ==================== TOP STATIONS ROW ====================

@Composable
fun TopStationsRow(
    stations: List<StationSummary>,
    onStationClick: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(stations.take(5)) { station ->
            TopStationCard(
                station = station,
                rank = stations.indexOf(station) + 1,
                onClick = { onStationClick(station.stationId) }
            )
        }
    }
}

@Composable
fun TopStationCard(
    station: StationSummary,
    rank: Int,
    onClick: () -> Unit
) {
    val rankColors = listOf(
        listOf(Color(0xFFFFD700), Color(0xFFFFA000)),  // Gold
        listOf(Color(0xFFC0C0C0), Color(0xFF9E9E9E)),  // Silver
        listOf(Color(0xFFCD7F32), Color(0xFFBF8970)),  // Bronze
        listOf(Color(0xFF1E88E5), Color(0xFF64B5F6)),  // Blue
        listOf(Color(0xFF7C4DFF), Color(0xFFB388FF))   // Purple
    )
    val gradientColors = if (rank <= 5) rankColors[rank - 1] else rankColors.last()
    
    Card(
        modifier = Modifier
            .width(180.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = gradientColors[0].copy(alpha = 0.2f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.linearGradient(gradientColors),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Online Status
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (station.isOnline) Color(0xFF00E676) else Color(0xFFFF5252),
                            shape = CircleShape
                        )
                )
            }
            
            Text(
                text = station.stationName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = station.city ?: "Unknown",
                fontSize = 12.sp,
                color = TextSecondary
            )
            
            HorizontalDivider(color = CardBorder)
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sales", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "KES ${String.format("%,.0f", station.todaySales)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gradientColors[0]
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pumps", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${station.pumpCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
            }
        }
    }
}

// ==================== STATION CARD ====================

@Composable
fun StationCard(
    station: StationSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Station Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF1E88E5), Color(0xFF64B5F6))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.LocalGasStation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Online Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (station.isOnline) Color(0xFF00E676) else Color(0xFFFF5252),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (station.isOnline) "Online" else "Offline",
                        fontSize = 10.sp,
                        color = if (station.isOnline) Color(0xFF00E676) else Color(0xFFFF5252),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = station.stationName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = station.city ?: "Unknown",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
            
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "KES ${String.format("%,.0f", station.todaySales)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C853)
                    )
                    Text(
                        text = "Today's Sales",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${station.todayTransactions}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C4DFF)
                    )
                    Text(
                        text = "Transactions",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Pumps & Shifts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E88E5).copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.LocalGasStation,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${station.pumpCount} Pumps",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${station.activeShifts} Shifts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

// ==================== DATA CLASSES ====================

data class StationSummary(
    val stationId: Int,
    val stationCode: String,
    val stationName: String,
    val city: String?,
    val county: String? = null,
    val region: String? = null,
    val pumpCount: Int = 0,
    val activeShifts: Int = 0,
    val todaySales: Double = 0.0,
    val todayTransactions: Int = 0,
    val mpesaSales: Double = 0.0,
    val isOnline: Boolean = true
)

data class MultiStationUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val stations: List<StationSummary> = emptyList(),
    val filteredStations: List<StationSummary> = emptyList(),
    val topStations: List<StationSummary> = emptyList(),
    val selectedFilter: StationFilter = StationFilter.ALL,
    
    // Aggregate stats
    val totalSales: Double = 0.0,
    val totalTransactions: Int = 0,
    val totalMpesaSales: Double = 0.0,
    val dayShiftSales: Double = 0.0,
    val nightShiftSales: Double = 0.0,
    val dayShiftTransactions: Int = 0,
    val nightShiftTransactions: Int = 0
)
