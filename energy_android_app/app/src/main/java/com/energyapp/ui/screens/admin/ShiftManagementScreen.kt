package com.energyapp.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.energyapp.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftManagementScreen(
    viewModel: ShiftManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shift Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Open New Shift",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select Pump",
                            style = MaterialTheme.typography.titleMedium
                        )

                        uiState.pumps.forEach { pump ->
                            FilterChip(
                                selected = uiState.selectedPump == pump,
                                onClick = { viewModel.selectPump(pump) },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(pump.pumpName)
                                        if (pump.isShiftOpen) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Open",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (uiState.selectedPump == pump)
                                            Icons.Default.CheckCircle
                                        else
                                            Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                        Divider()

                        Text(
                            text = "Select Shift Type",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.shifts.forEach { shift ->
                                FilterChip(
                                    selected = uiState.selectedShift == shift,
                                    onClick = { viewModel.selectShift(shift) },
                                    label = { Text(shift.shiftName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        EnergyTextField(
                            value = uiState.openingMeterReading,
                            onValueChange = viewModel::onOpeningMeterReadingChange,
                            label = "Opening Meter Reading",
                            keyboardType = KeyboardType.Decimal,
                            enabled = !uiState.isLoading
                        )

                        EnergyButton(
                            text = "Open Shift",
                            onClick = viewModel::openShift,
                            enabled = !uiState.isLoading
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Active Shifts",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(uiState.pumps.filter { it.isShiftOpen }) { pump ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = pump.pumpName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Shift is currently open",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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