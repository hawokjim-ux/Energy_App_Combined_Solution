package com.energyapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.energyapp.data.remote.models.UserResponse
import com.energyapp.ui.components.ErrorDialog
import com.energyapp.ui.components.LoadingDialog
import com.energyapp.ui.theme.*

/**
 * User Role Definition
 */
data class UserRoleType(
    val roleId: Int,
    val roleName: String,
    val icon: String,
    val color: Color,
    val description: String
)

// Role IDs must match database user_roles table:
// role_id=1: Admin, role_id=2: Pump Attendant, role_id=5: Super Admin
val userRoleTypes = listOf(
    UserRoleType(5, "Super Admin", "👑", GradientPurple, "Full system access"),
    UserRoleType(1, "Admin", "🎯", GradientPink, "Full access"),
    UserRoleType(3, "Manager", "💼", GradientCyan, "Full operations"),
    UserRoleType(4, "Supervisor", "👔", NeonOrange, "Oversee attendants"),
    UserRoleType(2, "Pump Attendant", "⛽", NeonGreen, "Sales only")
)

/**
 * User Management Screen - Super Modern 2024 Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

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
                                    "User Management",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Create & manage users",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        // Add User Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable { showCreateDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.PersonAdd,
                                contentDescription = "Add User",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = GradientPurple,
                    contentColor = Color.White,
                    modifier = Modifier.shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = GradientPurple.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add User",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            containerColor = LightBackground
        ) { paddingValues ->
            if (uiState.isLoading && uiState.users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GradientPurple,
                        strokeWidth = 3.dp
                    )
                }
            } else if (uiState.users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(IconGradientPurple1, IconGradientPurple2)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Group,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "No users found",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(GradientPurple, GradientCyan)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Create First User", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.users) { user ->
                        ModernUserCard(
                            user = user,
                            onToggleStatus = { viewModel.toggleUserStatus(user.userId) },
                            onDelete = { viewModel.deleteUser(user.userId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showCreateDialog) {
        ModernCreateUserDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }

    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error!!,
            onDismiss = viewModel::clearError
        )
    }

    if (uiState.successMessage != null) {
        LaunchedEffect(uiState.successMessage) {
            viewModel.clearSuccess()
        }
    }
}

@Composable
fun ModernUserCard(
    user: UserResponse,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val roleType = userRoleTypes.find { it.roleId == user.roleId } 
        ?: userRoleTypes.last()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = roleType.color.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Role Color Accent
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(140.dp)
                    .background(
                        color = roleType.color,
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User Avatar with Role Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = roleType.color.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = roleType.icon,
                                fontSize = 22.sp
                            )
                        }
                        Column {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    // Status Badge
                    Surface(
                        color = if (user.isActive) Success.copy(alpha = 0.15f) else Error.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (user.isActive) "Active" else "Inactive",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (user.isActive) Success else Error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Role Badge
                Surface(
                    color = roleType.color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = roleType.icon, fontSize = 12.sp)
                        Text(
                            text = roleType.roleName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = roleType.color
                        )
                    }
                }

                // Contact Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = GradientCyan
                        )
                        Text(
                            text = user.mobileNo ?: "No mobile",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onToggleStatus,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isActive) Error.copy(alpha = 0.15f) else Success.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (user.isActive) "Deactivate" else "Activate",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (user.isActive) Error else Success
                        )
                    }
                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardBorder.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = Error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCreateUserDialog(
    viewModel: UserManagementViewModel,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(userRoleTypes.last()) } // Default to Pump Attendant
    var showPassword by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = GradientPurple.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(GradientPurple, GradientCyan)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Create New User",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "Add a new team member",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Person, null, tint = GradientPurple)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientPurple,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Rounded.AccountCircle, null, tint = GradientCyan)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientCyan,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Lock, null, tint = GradientPink)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                null,
                                tint = GradientPink
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientPink,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                // Mobile
                OutlinedTextField(
                    value = mobileNo,
                    onValueChange = { mobileNo = it },
                    label = { Text("Mobile (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Phone, null, tint = NeonGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                // Role Selection Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "User Role",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = showRoleDropdown,
                        onExpandedChange = { showRoleDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedRole.icon} ${selectedRole.roleName}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRoleDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = selectedRole.color,
                                unfocusedBorderColor = selectedRole.color.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false }
                        ) {
                            userRoleTypes.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(role.icon, fontSize = 18.sp)
                                            Column {
                                                Text(
                                                    role.roleName,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = OnSurface
                                                )
                                                Text(
                                                    role.description,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedRole = role
                                        showRoleDropdown = false
                                    },
                                    leadingIcon = {
                                        if (selectedRole.roleId == role.roleId) {
                                            Icon(
                                                Icons.Rounded.Check,
                                                null,
                                                tint = role.color
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    // Role Description Badge
                    Surface(
                        color = selectedRole.color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = selectedRole.description,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = selectedRole.color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                viewModel.createUser(
                                    fullName = fullName,
                                    username = username,
                                    password = password,
                                    mobile = mobileNo.ifBlank { null },
                                    roleId = selectedRole.roleId
                                )
                                onDismiss()
                            }
                        },
                        enabled = fullName.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (fullName.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                        Brush.horizontalGradient(listOf(GradientPurple, GradientCyan))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Create User",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Legacy alias for compatibility
@Composable
fun UserCard(
    user: UserResponse,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) = ModernUserCard(user, onToggleStatus, onDelete)

@Composable
fun CreateUserDialog(
    viewModel: UserManagementViewModel,
    onDismiss: () -> Unit
) = ModernCreateUserDialog(viewModel, onDismiss)