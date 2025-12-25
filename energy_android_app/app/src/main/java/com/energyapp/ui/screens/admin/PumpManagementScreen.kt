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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.data.remote.models.PumpResponse
import com.energyapp.ui.components.BottomNavBar
import com.energyapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpManagementScreen(
    viewModel: PumpManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pump Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPumps() }) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightPrimary
                ),
                modifier = Modifier.shadow(elevation = 4.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = LightPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Pump")
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "pump_management",
                onNavigate = onNavigate
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = LightPrimary
                )
            } else if (uiState.pumps.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.LocalGasStation,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No pumps found",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.showAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
                    ) {
                        Text("Add First Pump")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.pumps) { pump ->
                        PumpCard(
                            pump = pump,
                            onEdit = { viewModel.showEditDialog(pump) },
                            onToggleActive = { viewModel.togglePumpActive(pump) },
                            onDelete = { viewModel.showDeleteConfirmation(pump) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Dialog
    if (uiState.showAddDialog) {
        AddEditPumpDialog(
            title = "Add New Pump",
            initialName = "",
            initialType = "Petrol",
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, type -> viewModel.createPump(name, type) }
        )
    }

    // Edit Dialog
    if (uiState.showEditDialog && uiState.editingPump != null) {
        AddEditPumpDialog(
            title = "Edit Pump",
            initialName = uiState.editingPump!!.pumpName,
            initialType = uiState.editingPump!!.pumpType ?: "Petrol",
            isActive = uiState.editingPump!!.isActive,
            showActiveToggle = true,
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { name, type ->
                viewModel.updatePump(
                    uiState.editingPump!!.pumpId,
                    name,
                    type,
                    uiState.editingPump!!.isActive
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmation && uiState.pumpToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("Delete Pump") },
            text = { 
                Text("Are you sure you want to delete '${uiState.pumpToDelete!!.pumpName}'? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletePump(uiState.pumpToDelete!!.pumpId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Snackbar
    uiState.operationSuccess?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSuccess()
        }
    }

    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
}

@Composable
fun PumpCard(
    pump: PumpResponse,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pump.isActive) CardBackground else CardBackground.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (pump.isActive) LightPrimary.copy(alpha = 0.1f)
                            else TextSecondary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.LocalGasStation,
                        contentDescription = null,
                        tint = if (pump.isActive) LightPrimary else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = pump.pumpName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (pump.isActive) OnSurface else TextSecondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pump.pumpType ?: "Fuel Pump",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (pump.isActive) Success.copy(alpha = 0.1f)
                                    else Error.copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (pump.isActive) "Active" else "Inactive",
                                fontSize = 10.sp,
                                color = if (pump.isActive) Success else Error
                            )
                        }
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onToggleActive) {
                    Icon(
                        if (pump.isActive) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                        contentDescription = if (pump.isActive) "Deactivate" else "Activate",
                        tint = if (pump.isActive) Success else TextSecondary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = LightPrimary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPumpDialog(
    title: String,
    initialName: String,
    initialType: String = "Petrol",
    isActive: Boolean = true,
    showActiveToggle: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var expanded by remember { mutableStateOf(false) }
    val pumpTypes = listOf("Petrol", "Diesel", "Super", "Premium", "LPG")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pump Name") },
                    placeholder = { Text("e.g., Pump 1") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightPrimary
                    ),
                    singleLine = true
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pump Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        pumpTypes.forEach { pumpType ->
                            DropdownMenuItem(
                                text = { Text(pumpType) },
                                onClick = {
                                    type = pumpType
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, type) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
