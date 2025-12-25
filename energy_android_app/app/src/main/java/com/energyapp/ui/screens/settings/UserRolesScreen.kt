package com.energyapp.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.energyapp.ui.theme.*

/**
 * User Roles and Rights Screen - Super Modern 2024 Design
 * Complete role-based access control with stunning visuals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRolesScreen(
    viewModel: UserRolesViewModel,
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
                            // Back Button
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
                                    "User Roles & Rights",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Access Control Management",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Add Role Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { viewModel.showAddRoleDialog() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Add Role",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            // Save Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.isSaving) Color.White.copy(alpha = 0.4f)
                                        else Color.White.copy(alpha = 0.2f)
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { if (!uiState.isSaving) viewModel.saveRoles() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Save,
                                        contentDescription = "Save",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = LightBackground,
            snackbarHost = {
                if (uiState.successMessage != null) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = Success,
                        contentColor = Color.White
                    ) {
                        Text(uiState.successMessage!!)
                    }
                }
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GradientPurple, strokeWidth = 3.dp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Role Selection Tabs
                    RoleTabsSection(
                        roles = uiState.roles,
                        selectedRole = uiState.selectedRole,
                        onRoleSelect = viewModel::selectRole
                    )

                    // Permissions Content
                    uiState.selectedRole?.let { role ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Role Info Card
                            item {
                                RoleInfoCard(
                                    role = role,
                                    onDelete = if (role.isEditable) {
                                        { viewModel.deleteRole(role.roleId) }
                                    } else null
                                )
                            }

                            // Permission Categories
                            PermissionCategory.values().forEach { category ->
                                val permissions = AppPermissions.getByCategory(category)
                                if (permissions.isNotEmpty()) {
                                    item {
                                        PermissionCategoryCard(
                                            category = category,
                                            permissions = permissions,
                                            rolePermissions = role.permissions,
                                            isEditable = role.isEditable,
                                            onTogglePermission = { permId ->
                                                viewModel.togglePermission(role.roleId, permId)
                                            },
                                            onToggleAll = { enabled ->
                                                viewModel.toggleCategoryPermissions(role.roleId, category, enabled)
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Role Dialog
    if (uiState.showAddRoleDialog) {
        AddRoleDialog(
            onDismiss = viewModel::hideAddRoleDialog,
            onConfirm = viewModel::addRole
        )
    }
}

// ==================== ROLE TABS SECTION ====================

@Composable
fun RoleTabsSection(
    roles: List<RolePermission>,
    selectedRole: RolePermission?,
    onRoleSelect: (RolePermission) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(0.dp),
                spotColor = GradientPurple.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            roles.forEach { role ->
                RoleTab(
                    role = role,
                    isSelected = selectedRole?.roleId == role.roleId,
                    onClick = { onRoleSelect(role) }
                )
            }
        }
    }
}

@Composable
fun RoleTab(
    role: RolePermission,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val roleColor = try {
        Color(android.graphics.Color.parseColor(role.colorHex))
    } catch (e: Exception) {
        GradientPurple
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) roleColor else roleColor.copy(alpha = 0.1f),
        animationSpec = tween(200),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 1.5.dp,
                color = if (isSelected) Color.Transparent else roleColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = role.icon,
                fontSize = 16.sp
            )
            Text(
                text = role.roleName,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else roleColor
            )
        }
    }
}

// ==================== ROLE INFO CARD ====================

@Composable
fun RoleInfoCard(
    role: RolePermission,
    onDelete: (() -> Unit)?
) {
    val roleColor = try {
        Color(android.graphics.Color.parseColor(role.colorHex))
    } catch (e: Exception) {
        GradientPurple
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = roleColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Color Accent Line
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(100.dp)
                    .background(
                        color = roleColor,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Role Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = roleColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role.icon,
                            fontSize = 28.sp
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = role.roleName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = role.description,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Permission Count Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = roleColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${role.permissions.size} permissions",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = roleColor
                            )
                        }
                    }
                }

                // Delete Button (if editable)
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Error.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Lock Icon for non-editable
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = TextSecondary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = "Locked",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== PERMISSION CATEGORY CARD ====================

@Composable
fun PermissionCategoryCard(
    category: PermissionCategory,
    permissions: List<Permission>,
    rolePermissions: Set<String>,
    isEditable: Boolean,
    onTogglePermission: (String) -> Unit,
    onToggleAll: (Boolean) -> Unit
) {
    val categoryInfo = getCategoryInfo(category)
    val allEnabled = permissions.all { rolePermissions.contains(it.id) }
    val someEnabled = permissions.any { rolePermissions.contains(it.id) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = categoryInfo.color.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Category Header
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
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(categoryInfo.gradientColors),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryInfo.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = categoryInfo.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "${permissions.count { rolePermissions.contains(it.id) }}/${permissions.size} enabled",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Toggle All Switch
                if (isEditable) {
                    Switch(
                        checked = allEnabled,
                        onCheckedChange = { onToggleAll(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = categoryInfo.color,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = CardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Permissions Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                permissions.chunked(2).forEach { rowPermissions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPermissions.forEach { permission ->
                            PermissionToggle(
                                permission = permission,
                                isEnabled = rolePermissions.contains(permission.id),
                                isEditable = isEditable,
                                accentColor = categoryInfo.color,
                                onToggle = { onTogglePermission(permission.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining space if odd number
                        if (rowPermissions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionToggle(
    permission: Permission,
    isEnabled: Boolean,
    isEditable: Boolean,
    accentColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) accentColor.copy(alpha = 0.12f) else LightSurfaceVariant,
        animationSpec = tween(200),
        label = "permBg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isEnabled) 1.5.dp else 1.dp,
                color = if (isEnabled) accentColor.copy(alpha = 0.4f) else CardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (isEditable) Modifier.clickable { onToggle() }
                else Modifier
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox/Status
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isEnabled) accentColor else CardBorder,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isEnabled) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = permission.icon,
                        fontSize = 14.sp
                    )
                    Text(
                        text = permission.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) accentColor else OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = permission.description,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==================== ADD ROLE DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var roleName by remember { mutableStateOf("") }
    var roleDescription by remember { mutableStateOf("") }

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
                modifier = Modifier.padding(24.dp),
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
                            text = "Add New Role",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "Create a custom role",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Role Name Field
                OutlinedTextField(
                    value = roleName,
                    onValueChange = { roleName = it },
                    label = { Text("Role Name") },
                    placeholder = { Text("e.g., Cashier") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientPurple,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                // Description Field
                OutlinedTextField(
                    value = roleDescription,
                    onValueChange = { roleDescription = it },
                    label = { Text("Description") },
                    placeholder = { Text("Brief description of role") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientPurple,
                        unfocusedBorderColor = CardBorder
                    ),
                    maxLines = 2
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    // Create Button
                    Button(
                        onClick = {
                            if (roleName.isNotBlank()) {
                                onConfirm(roleName, roleDescription)
                            }
                        },
                        enabled = roleName.isNotBlank(),
                        modifier = Modifier.weight(1f),
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
                                    brush = if (roleName.isNotBlank()) {
                                        Brush.horizontalGradient(listOf(GradientPurple, GradientCyan))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Create Role",
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

// ==================== CATEGORY INFO HELPER ====================

data class CategoryInfo(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val gradientColors: List<Color>
)

fun getCategoryInfo(category: PermissionCategory): CategoryInfo {
    return when (category) {
        PermissionCategory.DASHBOARD -> CategoryInfo(
            "Dashboard Access",
            Icons.Rounded.Dashboard,
            GradientPurple,
            listOf(IconGradientPurple1, IconGradientPurple2)
        )
        PermissionCategory.SALES -> CategoryInfo(
            "Sales Operations",
            Icons.Rounded.PointOfSale,
            NeonGreen,
            listOf(IconGradientGreen1, IconGradientGreen2)
        )
        PermissionCategory.REPORTS -> CategoryInfo(
            "Reports & Analytics",
            Icons.Rounded.Assessment,
            GradientCyan,
            listOf(IconGradientCyan1, IconGradientCyan2)
        )
        PermissionCategory.USER_MANAGEMENT -> CategoryInfo(
            "User Management",
            Icons.Rounded.People,
            GradientPink,
            listOf(IconGradientPink1, IconGradientPink2)
        )
        PermissionCategory.PUMP_MANAGEMENT -> CategoryInfo(
            "Pump Management",
            Icons.Rounded.LocalGasStation,
            GradientTeal,
            listOf(IconGradientTeal1, IconGradientTeal2)
        )
        PermissionCategory.SHIFT_MANAGEMENT -> CategoryInfo(
            "Shift Management",
            Icons.Rounded.Schedule,
            NeonOrange,
            listOf(IconGradientOrange1, IconGradientOrange2)
        )
        PermissionCategory.SETTINGS -> CategoryInfo(
            "System Settings",
            Icons.Rounded.Settings,
            NeonPurple,
            listOf(GradientPurple, NeonPink)
        )
        PermissionCategory.TRANSACTIONS -> CategoryInfo(
            "Transaction Records",
            Icons.Rounded.Receipt,
            NeonBlue,
            listOf(IconGradientBlue1, IconGradientBlue2)
        )
    }
}
