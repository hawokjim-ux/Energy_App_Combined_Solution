package com.energyapp.ui.screens.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import com.energyapp.ui.components.TransactionItem
import com.energyapp.ui.components.charts.*
import com.energyapp.ui.theme.*

/**
 * Reports Screen - Super Modern 2024 Design
 * Features stunning charts, comprehensive filters, and beautiful transaction grid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Scaffold(
            topBar = {
                // Stunning Mesh Gradient Header
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Glassmorphism Back Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { onNavigateBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Sales Reports",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Analytics & Insights",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Refresh Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
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
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            // Filter Toggle Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.showFilters) Color.White.copy(alpha = 0.4f)
                                        else Color.White.copy(alpha = 0.2f)
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { viewModel.toggleFilters() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.FilterList,
                                    contentDescription = "Filters",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
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
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Date Filter Section
                    item {
                        ModernDateFilterSection(
                            currentFilter = uiState.dateFilter,
                            onFilterChange = viewModel::setDateFilter,
                            startDate = uiState.startDate,
                            endDate = uiState.endDate,
                            onCustomDateRangeChange = viewModel::setCustomDateRange
                        )
                    }

                    // Expandable Filters Section
                    if (uiState.showFilters) {
                        item {
                            ModernFiltersSection(
                                pumps = uiState.pumps,
                                attendants = uiState.attendants,
                                selectedPump = uiState.selectedPump,
                                selectedAttendant = uiState.selectedAttendant,
                                statusFilter = uiState.statusFilter,
                                shiftFilter = uiState.shiftFilter,
                                onPumpSelect = viewModel::selectPump,
                                onAttendantSelect = viewModel::selectAttendant,
                                onStatusChange = viewModel::setStatusFilter,
                                onShiftChange = viewModel::setShiftFilter,
                                onClearFilters = viewModel::clearFilters
                            )
                        }
                    }

                    // Stats Summary Cards
                    item {
                        ModernStatsSummary(
                            totalSales = uiState.totalSales,
                            mpesaSales = uiState.mpesaSales,
                            cashSales = uiState.cashSales,
                            transactionCount = uiState.transactionCount,
                            successfulCount = uiState.successfulCount,
                            pendingCount = uiState.pendingCount,
                            failedCount = uiState.failedCount
                        )
                    }

                    // Sales Trend Line Chart
                    if (uiState.salesTrendData.isNotEmpty()) {
                        item {
                            ModernChartCard(
                                title = "Sales Trend",
                                subtitle = "Last 7 days",
                                icon = Icons.Rounded.TrendingUp,
                                gradientColors = listOf(GradientCyan, GradientTeal)
                            ) {
                                SalesLineChart(
                                    salesData = uiState.salesTrendData,
                                    labels = uiState.salesTrendLabels
                                )
                            }
                        }
                    }

                    // User/Attendant Bar Chart
                    if (uiState.userSalesChartData.isNotEmpty()) {
                        item {
                            ModernChartCard(
                                title = "Sales by Attendant",
                                subtitle = "Top performers",
                                icon = Icons.Rounded.People,
                                gradientColors = listOf(GradientPurple, NeonPink)
                            ) {
                                UserSalesBarChart(
                                    userNames = uiState.userSalesChartData.map { it.userName },
                                    salesAmounts = uiState.userSalesChartData.map { it.totalSales }
                                )
                            }
                        }
                    }

                    // Pump Bar Chart
                    if (uiState.pumpChartData.isNotEmpty()) {
                        item {
                            ModernChartCard(
                                title = "Sales by Pump",
                                subtitle = "Pump comparison",
                                icon = Icons.Rounded.LocalGasStation,
                                gradientColors = listOf(GradientTeal, NeonGreen)
                            ) {
                                PumpBarChart(
                                    pumpNames = uiState.pumpChartData.map { it.pumpName },
                                    salesAmounts = uiState.pumpChartData.map { it.totalSales }
                                )
                            }
                        }
                    }

                    // Sales Distribution Histogram
                    if (uiState.salesDistributionData.isNotEmpty()) {
                        item {
                            ModernChartCard(
                                title = "Sales Distribution",
                                subtitle = "Transaction amounts",
                                icon = Icons.Rounded.BarChart,
                                gradientColors = listOf(NeonOrange, NeonYellow)
                            ) {
                                SalesHistogramChart(
                                    salesData = uiState.salesDistributionData
                                )
                            }
                        }
                    }

                    // Transaction Table
                    item {
                        ModernTransactionTable(
                            transactions = uiState.transactionItems,
                            currentPage = uiState.currentPage,
                            pageSize = uiState.pageSize,
                            totalCount = uiState.filteredSales.size,
                            searchQuery = uiState.searchQuery,
                            onSearchChange = viewModel::onSearchChange,
                            onPageChange = viewModel::setPage
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

// ==================== MODERN DATE FILTER SECTION ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDateFilterSection(
    currentFilter: DateFilter,
    onFilterChange: (DateFilter) -> Unit,
    startDate: Long,
    endDate: Long,
    onCustomDateRangeChange: (Long, Long) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var tempStartDate by remember { mutableStateOf(startDate) }
    var tempEndDate by remember { mutableStateOf(endDate) }
    
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = GradientPurple.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(IconGradientPurple1, IconGradientPurple2)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Date Range",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }

            // Date Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DateFilter.values().forEach { filter ->
                    ModernDateChip(
                        text = when (filter) {
                            DateFilter.TODAY -> "Today"
                            DateFilter.WEEK -> "This Week"
                            DateFilter.MONTH -> "This Month"
                            DateFilter.CUSTOM -> "Custom"
                        },
                        isSelected = currentFilter == filter,
                        onClick = {
                            if (filter == DateFilter.CUSTOM) {
                                showStartDatePicker = true
                            } else {
                                onFilterChange(filter)
                            }
                        }
                    )
                }
            }

            // Custom Date Range Display
            if (currentFilter == DateFilter.CUSTOM) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GradientPurple.copy(alpha = 0.08f))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showStartDatePicker = true }
                    ) {
                        Text("From", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = dateFormat.format(java.util.Date(startDate)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientPurple
                        )
                    }
                    
                    Icon(
                        Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = GradientPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showEndDatePicker = true }
                    ) {
                        Text("To", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = dateFormat.format(java.util.Date(endDate)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientPurple
                        )
                    }
                }
            }
        }
    }

    // Date Picker Dialogs
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tempStartDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selected ->
                        tempStartDate = selected
                        showStartDatePicker = false
                        showEndDatePicker = true
                    }
                }) {
                    Text("Next: Select End Date", color = GradientPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tempEndDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selected ->
                        tempEndDate = selected
                        showEndDatePicker = false
                        onCustomDateRangeChange(tempStartDate, tempEndDate)
                    }
                }) {
                    Text("Apply", color = GradientPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ModernDateChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            listOf(GradientPurple, GradientCyan)
                        )
                    )
                } else {
                    Modifier
                        .background(CardBackground)
                        .border(1.5.dp, CardBorder, RoundedCornerShape(12.dp))
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else OnSurface
        )
    }
}

// ==================== MODERN FILTERS SECTION ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFiltersSection(
    pumps: List<com.energyapp.data.remote.models.PumpResponse>,
    attendants: List<com.energyapp.data.remote.models.UserResponse>,
    selectedPump: com.energyapp.data.remote.models.PumpResponse?,
    selectedAttendant: com.energyapp.data.remote.models.UserResponse?,
    statusFilter: StatusFilter,
    shiftFilter: ShiftFilter,
    onPumpSelect: (com.energyapp.data.remote.models.PumpResponse?) -> Unit,
    onAttendantSelect: (com.energyapp.data.remote.models.UserResponse?) -> Unit,
    onStatusChange: (StatusFilter) -> Unit,
    onShiftChange: (ShiftFilter) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(IconGradientCyan1, IconGradientCyan2)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.FilterList,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Filters",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                TextButton(onClick = onClearFilters) {
                    Icon(
                        Icons.Rounded.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 12.sp, color = Error)
                }
            }

            // Status Filter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Transaction Status",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusFilterChip("All", StatusFilter.ALL, statusFilter, NeonBlue, onStatusChange)
                    StatusFilterChip("Paid", StatusFilter.SUCCESS, statusFilter, Success, onStatusChange)
                    StatusFilterChip("Pending", StatusFilter.PENDING, statusFilter, Warning, onStatusChange)
                    StatusFilterChip("Failed", StatusFilter.FAILED, statusFilter, Error, onStatusChange)
                }
            }

            // Shift Filter (Day/Night)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Shift",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShiftFilterChip("All", ShiftFilter.ALL, shiftFilter, GradientPurple, onShiftChange)
                    ShiftFilterChip("☀️ Day", ShiftFilter.DAY, shiftFilter, NeonOrange, onShiftChange)
                    ShiftFilterChip("🌙 Night", ShiftFilter.NIGHT, shiftFilter, NeonPurple, onShiftChange)
                }
            }

            // Pump and Attendant Dropdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pump Dropdown
                var expandedPump by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedPump,
                    onExpandedChange = { expandedPump = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedPump?.pumpName ?: "All Pumps",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pump", fontSize = 12.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPump) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GradientCyan,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPump,
                        onDismissRequest = { expandedPump = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Pumps") },
                            onClick = {
                                onPumpSelect(null)
                                expandedPump = false
                            }
                        )
                        pumps.forEach { pump ->
                            DropdownMenuItem(
                                text = { Text(pump.pumpName) },
                                onClick = {
                                    onPumpSelect(pump)
                                    expandedPump = false
                                }
                            )
                        }
                    }
                }

                // Attendant Dropdown
                var expandedAttendant by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAttendant,
                    onExpandedChange = { expandedAttendant = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedAttendant?.fullName ?: "All Attendants",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Attendant", fontSize = 12.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAttendant) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GradientCyan,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAttendant,
                        onDismissRequest = { expandedAttendant = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Attendants") },
                            onClick = {
                                onAttendantSelect(null)
                                expandedAttendant = false
                            }
                        )
                        attendants.forEach { attendant ->
                            DropdownMenuItem(
                                text = { Text(attendant.fullName) },
                                onClick = {
                                    onAttendantSelect(attendant)
                                    expandedAttendant = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusFilterChip(
    text: String,
    filter: StatusFilter,
    currentFilter: StatusFilter,
    color: Color,
    onSelect: (StatusFilter) -> Unit
) {
    val isSelected = currentFilter == filter
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) color else color.copy(alpha = 0.1f)
            )
            .border(
                1.5.dp,
                if (isSelected) color else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onSelect(filter) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else color
        )
    }
}

@Composable
fun ShiftFilterChip(
    text: String,
    filter: ShiftFilter,
    currentFilter: ShiftFilter,
    color: Color,
    onSelect: (ShiftFilter) -> Unit
) {
    val isSelected = currentFilter == filter
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) color else color.copy(alpha = 0.1f)
            )
            .border(
                1.5.dp,
                if (isSelected) color else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onSelect(filter) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else color
        )
    }
}

// ==================== MODERN STATS SUMMARY ====================

@Composable
fun ModernStatsSummary(
    totalSales: Double,
    mpesaSales: Double,
    cashSales: Double,
    transactionCount: Int,
    successfulCount: Int,
    pendingCount: Int,
    failedCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Sales Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernStatCard(
                title = "Total Sales",
                value = "KES ${String.format("%,.0f", totalSales)}",
                icon = Icons.Rounded.AttachMoney,
                gradientColors = listOf(GradientPurple, GradientCyan),
                modifier = Modifier.weight(1f)
            )
            ModernStatCard(
                title = "M-Pesa",
                value = "KES ${String.format("%,.0f", mpesaSales)}",
                icon = Icons.Rounded.PhoneAndroid,
                gradientColors = listOf(IconGradientGreen1, IconGradientGreen2),
                modifier = Modifier.weight(1f)
            )
        }

        // Transaction Counts Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                title = "Total",
                value = transactionCount.toString(),
                color = NeonBlue,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                title = "Paid",
                value = successfulCount.toString(),
                color = Success,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                title = "Pending",
                value = pendingCount.toString(),
                color = Warning,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                title = "Failed",
                value = failedCount.toString(),
                color = Error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = gradientColors[0].copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(90.dp)
                    .background(
                        brush = Brush.verticalGradient(gradientColors),
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
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
fun MiniStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = color.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

// ==================== MODERN CHART CARD ====================

@Composable
fun ModernChartCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = gradientColors[0].copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            content()
        }
    }
}

// ==================== MODERN TRANSACTION TABLE ====================

@Composable
fun ModernTransactionTable(
    transactions: List<TransactionItem>,
    currentPage: Int,
    pageSize: Int,
    totalCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onPageChange: (Int) -> Unit
) {
    val totalPages = if (totalCount == 0) 1 else (totalCount + pageSize - 1) / pageSize
    val startItem = currentPage * pageSize + 1
    val endItem = minOf((currentPage + 1) * pageSize, totalCount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = GradientPink.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(IconGradientPink1, IconGradientPink2)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Transaction History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GradientPink.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "$startItem-$endItem of $totalCount",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GradientPink
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by receipt, user, pump, or amount...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = GradientPink,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientPink,
                    unfocusedBorderColor = CardBorder
                ),
                singleLine = true
            )

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                GradientPurple.copy(alpha = 0.08f),
                                GradientCyan.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableHeaderText("Pump", Modifier.weight(0.8f))
                TableHeaderText("Attendant", Modifier.weight(1f))
                TableHeaderText("Receipt", Modifier.weight(1f))
                TableHeaderText("Amount", Modifier.weight(0.8f))
                TableHeaderText("Status", Modifier.weight(0.5f))
            }

            // Table Body
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    transactions.forEach { transaction ->
                        ModernTransactionRow(transaction)
                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                    }
                }
            }

            // Pagination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous",
                        tint = if (currentPage > 0) GradientPurple else TextSecondary
                    )
                }

                for (page in maxOf(0, currentPage - 2)..minOf(totalPages - 1, currentPage + 2)) {
                    val isCurrentPage = page == currentPage
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCurrentPage) {
                                    Brush.horizontalGradient(listOf(GradientPurple, GradientCyan))
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { onPageChange(page) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (page + 1).toString(),
                            fontSize = 13.sp,
                            fontWeight = if (isCurrentPage) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentPage) Color.White else TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = "Next",
                        tint = if (currentPage < totalPages - 1) GradientPurple else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TableHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = GradientPurple
    )
}

@Composable
fun ModernTransactionRow(transaction: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pump column
        Text(
            text = transaction.pumpName,
            modifier = Modifier.weight(0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Attendant column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.userName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${transaction.date} ${transaction.time}",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        // Receipt column
        Text(
            text = transaction.mpesaReceipt ?: "-",
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            color = if (transaction.mpesaReceipt != null) GradientCyan else TextSecondary,
            fontWeight = if (transaction.mpesaReceipt != null) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Amount column
        Text(
            text = String.format("%,.0f", transaction.amount),
            modifier = Modifier.weight(0.8f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )

        // Status column
        val statusColor = when (transaction.status.uppercase()) {
            "SUCCESS" -> Success
            "PENDING" -> Warning
            else -> Error
        }
        Box(
            modifier = Modifier
                .weight(0.5f)
                .clip(RoundedCornerShape(6.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (transaction.status.uppercase()) {
                    "SUCCESS" -> "✓"
                    "PENDING" -> "⏳"
                    else -> "✗"
                },
                fontSize = 12.sp,
                color = statusColor
            )
        }
    }
}

// Legacy aliases for compatibility
@Composable
fun DateFilterChips(
    currentFilter: DateFilter,
    onFilterChange: (DateFilter) -> Unit,
    startDate: Long = System.currentTimeMillis(),
    endDate: Long = System.currentTimeMillis(),
    onCustomDateRangeChange: (Long, Long) -> Unit = { _, _ -> }
) = ModernDateFilterSection(currentFilter, onFilterChange, startDate, endDate, onCustomDateRangeChange)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersCard(
    pumps: List<com.energyapp.data.remote.models.PumpResponse>,
    attendants: List<com.energyapp.data.remote.models.UserResponse>,
    selectedPump: com.energyapp.data.remote.models.PumpResponse?,
    selectedAttendant: com.energyapp.data.remote.models.UserResponse?,
    onPumpSelect: (com.energyapp.data.remote.models.PumpResponse?) -> Unit,
    onAttendantSelect: (com.energyapp.data.remote.models.UserResponse?) -> Unit,
    onClearFilters: () -> Unit
) {
    // Simplified legacy compatibility
}

@Composable
fun EnhancedStatsCard(
    totalSales: Double,
    mpesaSales: Double,
    cashSales: Double,
    transactionCount: Int,
    successfulCount: Int
) = ModernStatsSummary(totalSales, mpesaSales, cashSales, transactionCount, successfulCount, 0, 0)