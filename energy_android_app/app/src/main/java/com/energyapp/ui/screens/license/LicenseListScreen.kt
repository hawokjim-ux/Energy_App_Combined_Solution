package com.energyapp.ui.screens.license

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.energyapp.data.remote.LicenseDbResponse
import com.energyapp.ui.theme.*
import com.energyapp.util.LicenseManager
import com.energyapp.util.LicenseType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * License List Screen - View ALL Licenses with Full Management
 * Super Admin Only - Accessed from Admin Dashboard
 * 
 * Features:
 * - View all licenses from database
 * - See full details: type, status, remaining days, client info
 * - Copy license keys
 * - Revoke/Suspend licenses
 * - Delete licenses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseListScreen(
    licenseManager: LicenseManager,
    onNavigateBack: () -> Unit
) {
    var allLicenses by remember { mutableStateOf<List<LicenseDbResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showRevokeDialog by remember { mutableStateOf<LicenseDbResponse?>(null) }
    var revokeReason by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("all") } // all, active, expired, revoked
    
    val scope = rememberCoroutineScope()
    
    // Load all licenses
    LaunchedEffect(Unit) {
        isLoading = true
        allLicenses = licenseManager.getAllLicensesFromDb()
        isLoading = false
    }
    
    // Filter licenses
    val filteredLicenses = remember(allLicenses, filterStatus) {
        when (filterStatus) {
            "active" -> allLicenses.filter { it.isActivated && !it.isRevoked && !isExpired(it) }
            "expired" -> allLicenses.filter { isExpired(it) }
            "revoked" -> allLicenses.filter { it.isRevoked }
            "pending" -> allLicenses.filter { !it.isActivated && !it.isRevoked }
            else -> allLicenses
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Scaffold(
            topBar = {
                // Gradient Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFFD700), // Gold
                                    Color(0xFFFFA500), // Orange
                                    Color(0xFFFF6B35)  // Red-Orange
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
                                    "All Licenses",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    "${allLicenses.size} total licenses",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        // Refresh Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable {
                                    scope.launch {
                                        isLoading = true
                                        allLicenses = licenseManager.getAllLicensesFromDb()
                                        isLoading = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            },
            containerColor = LightBackground
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        strokeWidth = 3.dp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Filter Chips
                    item {
                        LicenseFilterChips(
                            selectedFilter = filterStatus,
                            onFilterSelected = { filterStatus = it },
                            counts = mapOf(
                                "all" to allLicenses.size,
                                "active" to allLicenses.count { it.isActivated && !it.isRevoked && !isExpired(it) },
                                "pending" to allLicenses.count { !it.isActivated && !it.isRevoked },
                                "expired" to allLicenses.count { isExpired(it) },
                                "revoked" to allLicenses.count { it.isRevoked }
                            )
                        )
                    }
                    
                    if (filteredLicenses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.SearchOff,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "No licenses found",
                                        fontSize = 16.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredLicenses) { license ->
                            LicenseDetailCard(
                                license = license,
                                onCopy = { /* Handled inside card */ },
                                onRevoke = { showRevokeDialog = license },
                                onDelete = {
                                    scope.launch {
                                        // Delete is same as revoke with reason "Deleted"
                                        val success = licenseManager.revokeLicense(license.licenseId, "Deleted by Admin")
                                        if (success) {
                                            allLicenses = licenseManager.getAllLicensesFromDb()
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
    
    // Revoke Dialog
    showRevokeDialog?.let { license ->
        RevokeDialog(
            license = license,
            reason = revokeReason,
            onReasonChange = { revokeReason = it },
            onConfirm = {
                scope.launch {
                    val success = licenseManager.revokeLicense(
                        license.licenseId, 
                        revokeReason.ifBlank { "Revoked by Admin" }
                    )
                    if (success) {
                        allLicenses = licenseManager.getAllLicensesFromDb()
                    }
                    showRevokeDialog = null
                    revokeReason = ""
                }
            },
            onDismiss = {
                showRevokeDialog = null
                revokeReason = ""
            }
        )
    }
}

/**
 * Filter Chips for License Status
 */
@Composable
fun LicenseFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    counts: Map<String, Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterChipItem("All", "all", counts["all"] ?: 0, selectedFilter, onFilterSelected, Color(0xFF6366F1))
        FilterChipItem("Active", "active", counts["active"] ?: 0, selectedFilter, onFilterSelected, Color(0xFF22C55E))
        FilterChipItem("Pending", "pending", counts["pending"] ?: 0, selectedFilter, onFilterSelected, Color(0xFFF59E0B))
        FilterChipItem("Expired", "expired", counts["expired"] ?: 0, selectedFilter, onFilterSelected, Color(0xFFEF4444))
        FilterChipItem("Revoked", "revoked", counts["revoked"] ?: 0, selectedFilter, onFilterSelected, Color(0xFF64748B))
    }
}

@Composable
fun FilterChipItem(
    label: String,
    value: String,
    count: Int,
    selectedFilter: String,
    onSelect: (String) -> Unit,
    color: Color
) {
    val isSelected = selectedFilter == value
    
    Surface(
        modifier = Modifier.clickable { onSelect(value) },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color else color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else color
            )
            Surface(
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.3f) else color.copy(alpha = 0.2f)
            ) {
                Text(
                    count.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else color
                )
            }
        }
    }
}

/**
 * License Detail Card - Shows full license information
 */
@Composable
fun LicenseDetailCard(
    license: LicenseDbResponse,
    onCopy: () -> Unit,
    onRevoke: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    
    val licenseType = LicenseType.fromCode(license.licenseType)
    val typeColor = try {
        Color(android.graphics.Color.parseColor(licenseType?.colorHex ?: "#7C3AED"))
    } catch (e: Exception) { GradientPurple }
    
    val status = getLicenseStatus(license)
    val statusColor = when (status) {
        "Active" -> Color(0xFF22C55E)
        "Pending" -> Color(0xFFF59E0B)
        "Expired" -> Color(0xFFEF4444)
        "Revoked" -> Color(0xFF64748B)
        else -> TextSecondary
    }
    
    val daysRemaining = calculateDaysRemaining(license)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = typeColor.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Status Color Accent
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(180.dp)
                    .background(
                        color = statusColor,
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type Icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(licenseType?.icon ?: "🔑", fontSize = 22.sp)
                        }
                        
                        Column {
                            Text(
                                licenseType?.displayName ?: license.licenseType,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                license.clientName ?: "No Client Name",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            status,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                
                // License Key
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = LightSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            license.licenseKey,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = typeColor,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(license.licenseKey))
                                copied = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (copied) Color(0xFF22C55E) else typeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Info Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Days Remaining
                    InfoChip(
                        icon = Icons.Rounded.AccessTime,
                        label = "Remaining",
                        value = if (daysRemaining > 0) "$daysRemaining days" else if (status == "Pending") "Not activated" else "Expired",
                        color = if (daysRemaining > 30) Color(0xFF22C55E) else if (daysRemaining > 0) Color(0xFFF59E0B) else Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Max Devices
                    InfoChip(
                        icon = Icons.Rounded.Devices,
                        label = "Devices",
                        value = "${license.activationCount}/${license.maxDevices}",
                        color = GradientCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Client Phone & Created Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Phone,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            license.clientPhone ?: "No Phone",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        "Created: ${license.createdAt?.take(10) ?: "Unknown"}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                
                // Action Buttons
                if (!license.isRevoked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Revoke Button
                        Button(
                            onClick = onRevoke,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Block,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Revoke",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                        
                        // Delete Button
                        Button(
                            onClick = onDelete,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Delete",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                } else {
                    // Show revoked reason
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Cancel,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Revoked: ${license.revokedReason ?: "No reason"}",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    label,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

/**
 * Revoke Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevokeDialog(
    license: LicenseDbResponse,
    reason: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
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
                                Color(0xFFF59E0B).copy(alpha = 0.15f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Block,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            "Revoke License",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            "This action cannot be undone",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // License Info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = LightSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            license.clientName ?: "Unknown Client",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            license.licenseKey.take(30) + "...",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                }
                
                // Reason Input
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text("Revocation Reason") },
                    placeholder = { Text("Enter reason for revoking...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = CardBorder
                    ),
                    minLines = 2,
                    maxLines = 3
                )
                
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B)
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Block,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Revoke", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper functions
private fun isExpired(license: LicenseDbResponse): Boolean {
    if (!license.isActivated) return false
    val expDate = license.expirationDate ?: return false
    return try {
        val expInstant = Instant.parse(expDate)
        Instant.now().isAfter(expInstant)
    } catch (e: Exception) {
        false
    }
}

private fun getLicenseStatus(license: LicenseDbResponse): String {
    return when {
        license.isRevoked -> "Revoked"
        isExpired(license) -> "Expired"
        license.isActivated -> "Active"
        else -> "Pending"
    }
}

private fun calculateDaysRemaining(license: LicenseDbResponse): Int {
    if (!license.isActivated) return 0
    val expDate = license.expirationDate ?: return 0
    return try {
        val expInstant = Instant.parse(expDate)
        val now = Instant.now()
        if (now.isAfter(expInstant)) 0
        else ChronoUnit.DAYS.between(now, expInstant).toInt()
    } catch (e: Exception) {
        0
    }
}
