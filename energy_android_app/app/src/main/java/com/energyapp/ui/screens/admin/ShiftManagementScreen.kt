package com.energyapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.components.*
import com.energyapp.ui.theme.*

// Local color definitions for ShiftManagementScreen
private val PrimaryBlue = Color(0xFF4F7CFF)
private val PrimaryPurple = Color(0xFF9747FF)
private val AccentGreen = Color(0xFF00E676)
private val AccentOrange = Color(0xFFFF6B35)
private val CardDark = Color(0xFF1A2038)
private val TextPrimary = Color(0xFFFFFFFF)
private val DarkSurface = Color(0xFF151B3D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftManagementScreen(
    viewModel: ShiftManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Gradient background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0E27),
            Color(0xFF1A1F3A),
            Color(0xFF0F1629)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Shift Management",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A2038))
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E27),
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Open New Shift",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage pump shifts efficiently",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AccentGreen, shape = RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Main Control Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardDark
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Pump Selection
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Select Pump",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.pumps.forEach { pump ->
                                        Surface(
                                            onClick = { viewModel.selectPump(pump) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (uiState.selectedPump == pump) {
                                                PrimaryBlue.copy(alpha = 0.15f)
                                            } else {
                                                DarkSurface
                                            },
                                            border = if (uiState.selectedPump == pump) {
                                                androidx.compose.foundation.BorderStroke(
                                                    2.dp,
                                                    PrimaryBlue
                                                )
                                            } else null
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (uiState.selectedPump == pump) {
                                                            PrimaryBlue
                                                        } else {
                                                            CardDark
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.LocalGasStation,
                                                            contentDescription = null,
                                                            tint = TextPrimary,
                                                            modifier = Modifier
                                                                .padding(8.dp)
                                                                .size(24.dp)
                                                        )
                                                    }

                                                    Text(
                                                        text = pump.pumpName,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                if (pump.isShiftOpen) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = AccentGreen.copy(alpha = 0.2f)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = "Open",
                                                                tint = AccentGreen,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text(
                                                                text = "Active",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = AccentGreen,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                } else if (uiState.selectedPump == pump) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = PrimaryBlue,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Divider(color = DarkSurface, thickness = 1.dp)

                            // Shift Type Selection
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Shift Type",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    uiState.shifts.forEach { shift ->
                                        Surface(
                                            onClick = { viewModel.selectShift(shift) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (uiState.selectedShift == shift) {
                                                Brush.horizontalGradient(
                                                    colors = listOf(PrimaryBlue, PrimaryPurple)
                                                ).let { Color.Transparent }
                                            } else {
                                                DarkSurface
                                            }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (uiState.selectedShift == shift) {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(PrimaryBlue, PrimaryPurple)
                                                            )
                                                        } else {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(DarkSurface, DarkSurface)
                                                            )
                                                        }
                                                    )
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (shift.shiftName.contains("Day", ignoreCase = true)) {
                                                            Icons.Default.WbSunny
                                                        } else {
                                                            Icons.Default.DarkMode
                                                        },
                                                        contentDescription = null,
                                                        tint = TextPrimary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(
                                                        text = shift.shiftName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Meter Reading Input
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Opening Reading",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                OutlinedTextField(
                                    value = uiState.openingMeterReading,
                                    onValueChange = viewModel::onOpeningMeterReadingChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "Enter meter reading",
                                            color = TextSecondary
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = DarkSurface,
                                        unfocusedContainerColor = DarkSurface,
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    enabled = !uiState.isLoading
                                )
                            }

                            // Open Shift Button
                            Button(
                                onClick = viewModel::openShift,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = DarkSurface
                                ),
                                enabled = !uiState.isLoading
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (!uiState.isLoading) {
                                                Brush.horizontalGradient(
                                                    colors = listOf(PrimaryBlue, PrimaryPurple)
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    colors = listOf(DarkSurface, DarkSurface)
                                                )
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = TextPrimary
                                        )
                                        Text(
                                            text = "Open Shift",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Shifts Section
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Workspaces,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Active Shifts",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${uiState.pumps.count { it.isShiftOpen }}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val activeShifts = uiState.pumps.filter { it.isShiftOpen }

                if (activeShifts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardDark
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No Active Shifts",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Open a shift to get started",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    items(activeShifts) { pump ->
                        var showCloseDialog by remember { mutableStateOf(false) }
                        var closingReading by remember { mutableStateOf("") }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Success.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.LocalGasStation,
                                                contentDescription = null,
                                                tint = Success,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = pump.pumpName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurface
                                            )
                                            Text(
                                                text = "Shift in Progress",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Success.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Success, shape = RoundedCornerShape(4.dp))
                                            )
                                            Text(
                                                text = "Active",
                                                fontSize = 10.sp,
                                                color = Success,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Close Shift Section
                                if (showCloseDialog) {
                                    Divider(color = CardBorder, thickness = 0.5.dp)
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Close Shift",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurface
                                        )
                                        OutlinedTextField(
                                            value = closingReading,
                                            onValueChange = { 
                                                closingReading = it
                                                viewModel.onClosingMeterReadingChange(it)
                                            },
                                            label = { Text("Closing Meter Reading", fontSize = 12.sp) },
                                            placeholder = { Text("Enter closing reading", fontSize = 12.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = LightPrimary,
                                                unfocusedBorderColor = CardBorder
                                            ),
                                            singleLine = true
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { showCloseDialog = false },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Cancel")
                                            }
                                            Button(
                                                onClick = {
                                                    pump.pumpShiftId?.let { viewModel.closeShift(it) }
                                                    showCloseDialog = false
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Error),
                                                enabled = closingReading.isNotEmpty()
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Stop,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Close Shift")
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { showCloseDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Error.copy(alpha = 0.1f),
                                            contentColor = Error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Rounded.Stop,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Close This Shift", fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isLoading) {
        LoadingDialog()
    }

    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error!!,
            onDismiss = viewModel::clearMessages
        )
    }

    if (uiState.successMessage != null) {
        SuccessDialog(
            message = uiState.successMessage!!,
            onDismiss = viewModel::clearMessages
        )
    }
}