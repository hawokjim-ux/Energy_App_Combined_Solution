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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.data.remote.models.AttendantSummary
import com.energyapp.ui.components.BottomNavBar
import com.energyapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendantManagementScreen(
    viewModel: AttendantManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Attendant Management",
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
                    IconButton(onClick = { viewModel.loadAttendants() }) {
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
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add Attendant")
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "attendant_management",
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
            } else if (uiState.attendants.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No attendants found",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.showAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
                    ) {
                        Text("Add First Attendant")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.attendants) { attendant ->
                        AttendantCard(
                            attendant = attendant,
                            onToggleActive = { viewModel.toggleAttendantActive(attendant) }
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
        AddAttendantDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { fullName, username, password, mobileNo ->
                viewModel.createAttendant(fullName, username, password, mobileNo)
            }
        )
    }
}

@Composable
fun AttendantCard(
    attendant: AttendantSummary,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (attendant.isActive) CardBackground else CardBackground.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (attendant.isActive) LightPrimary.copy(alpha = 0.1f)
                                else TextSecondary.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = if (attendant.isActive) LightPrimary else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = attendant.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (attendant.isActive) OnSurface else TextSecondary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            attendant.mobileNo?.let { mobile ->
                                Text(
                                    text = mobile,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (attendant.isActive) Success.copy(alpha = 0.1f)
                                        else Error.copy(alpha = 0.1f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (attendant.isActive) "Active" else "Inactive",
                                    fontSize = 10.sp,
                                    color = if (attendant.isActive) Success else Error
                                )
                            }
                        }
                    }
                }
                
                IconButton(onClick = onToggleActive) {
                    Icon(
                        if (attendant.isActive) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                        contentDescription = if (attendant.isActive) "Deactivate" else "Activate",
                        tint = if (attendant.isActive) Success else TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sales Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Today's Sales",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "KES ${String.format("%,.0f", attendant.todaySales)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightPrimary
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transactions",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = attendant.transactionCount.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAttendantDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Attendant", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g., John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightPrimary
                    ),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("e.g., johndoe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightPrimary
                    ),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Min 6 characters") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightPrimary
                    ),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = mobileNo,
                    onValueChange = { mobileNo = it },
                    label = { Text("Mobile Number (Optional)") },
                    placeholder = { Text("e.g., 0712345678") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LightPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(fullName, username, password, mobileNo.takeIf { it.isNotBlank() }) 
                },
                enabled = fullName.isNotBlank() && username.isNotBlank() && password.length >= 6,
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
