package com.energyapp.ui.screens.license

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.energyapp.ui.theme.*
import com.energyapp.util.LicenseContact
import com.energyapp.util.LicenseInfo
import com.energyapp.util.LicenseManager
import com.energyapp.util.LicenseType
import com.energyapp.util.LicenseValidationResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * License Activation Screen - Shown when app is not licensed
 * Includes superuser login option for license generation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseActivationScreen(
    licenseManager: LicenseManager,
    onLicenseActivated: () -> Unit,
    onSuperUserLogin: ((String, String) -> Boolean)? = null
) {
    var licenseKey by remember { mutableStateOf("") }
    var isActivating by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<LicenseValidationResult?>(null) }
    var showError by remember { mutableStateOf(false) }
    var showSuperUserLogin by remember { mutableStateOf(false) }
    var superUserError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val deviceId = remember { licenseManager.getDeviceId() }
    val deviceInfo = remember { licenseManager.getDeviceInfo() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Logo
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GradientPurple,
                                    GradientCyan,
                                    GradientPink.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⛽", fontSize = 36.sp)
                        }
                        Text(
                            text = "Alpha Energy",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "License Activation Required",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // License Input Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = GradientPurple.copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
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
                                    Icons.Rounded.Key,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Enter License Key",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                                Text(
                                    text = "Format: ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        OutlinedTextField(
                            value = licenseKey,
                            onValueChange = { 
                                licenseKey = it.uppercase()
                                showError = false
                                validationResult = null
                            },
                            label = { Text("License Key") },
                            placeholder = { Text("ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientPurple,
                                unfocusedBorderColor = CardBorder,
                                errorBorderColor = Error
                            ),
                            isError = showError,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        validationResult?.let { result ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (result.isValid) Success.copy(alpha = 0.12f) else Error.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        if (result.isValid) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                        contentDescription = null,
                                        tint = if (result.isValid) Success else Error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = result.message,
                                        fontSize = 13.sp,
                                        color = if (result.isValid) Success else Error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isActivating = true
                                    val result = licenseManager.activateLicense(licenseKey)
                                    validationResult = result
                                    showError = !result.isValid
                                    isActivating = false
                                    
                                    if (result.isValid) {
                                        kotlinx.coroutines.delay(1500)
                                        onLicenseActivated()
                                    }
                                }
                            },
                            enabled = licenseKey.isNotBlank() && !isActivating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = if (licenseKey.isNotBlank() && !isActivating) {
                                            Brush.horizontalGradient(listOf(GradientPurple, GradientCyan))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActivating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.VpnKey,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                        Text(
                                            "Activate License",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SuperUser Login Card
            if (onSuperUserLogin != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = NeonPurple.copy(alpha = 0.15f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(if (showSuperUserLogin) 280.dp else 80.dp)
                                    .background(
                                        brush = Brush.verticalGradient(listOf(NeonPurple, GradientPink)),
                                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                                    )
                            )
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showSuperUserLogin = !showSuperUserLogin },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    brush = Brush.linearGradient(listOf(NeonPurple, GradientPink)),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "License Administrator",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurface
                                            )
                                            Text(
                                                "Generate new licenses",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    Icon(
                                        if (showSuperUserLogin) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                        contentDescription = null,
                                        tint = NeonPurple
                                    )
                                }

                                if (showSuperUserLogin) {
                                    SuperUserLoginForm(
                                        onLogin = onSuperUserLogin,
                                        error = superUserError,
                                        onErrorChange = { superUserError = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Device Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.PhoneAndroid,
                                contentDescription = null,
                                tint = GradientCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "Device Information",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = OnSurface
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Device:", fontSize = 13.sp, color = TextSecondary)
                            Text(deviceInfo, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Device ID:", fontSize = 13.sp, color = TextSecondary)
                            Text(
                                deviceId,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = GradientPurple,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Contact Card
            item {
                ContactCard()
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperUserLoginForm(
    onLogin: (String, String) -> Boolean,
    error: String?,
    onErrorChange: (String?) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HorizontalDivider(color = CardBorder)
        
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                onErrorChange(null)
            },
            label = { Text("Username") },
            leadingIcon = {
                Icon(Icons.Rounded.Person, null, tint = NeonPurple)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = CardBorder
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                onErrorChange(null)
            },
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
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientPink,
                unfocusedBorderColor = CardBorder
            ),
            singleLine = true
        )

        if (error != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Error.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                    Text(error, fontSize = 12.sp, color = Error, fontWeight = FontWeight.Medium)
                }
            }
        }

        Button(
            onClick = {
                isLoading = true
                val success = onLogin(username, password)
                isLoading = false
                if (!success) {
                    onErrorChange("Invalid credentials. Access denied.")
                }
            },
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
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
                        brush = if (username.isNotBlank() && password.isNotBlank()) {
                            Brush.horizontalGradient(listOf(NeonPurple, GradientPink))
                        } else {
                            Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Login, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text("Access License Generator", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Dummy for backwards compatibility
@Composable
fun SuperUserLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // Not used directly anymore
}

/**
 * License Management Screen - For Super Admin
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseManagementScreen(
    licenseManager: LicenseManager,
    onNavigateBack: () -> Unit
) {
    var currentLicense by remember { mutableStateOf<LicenseInfo?>(null) }
    var daysRemaining by remember { mutableStateOf(0) }
    var showGenerateDialog by remember { mutableStateOf(false) }
    var generatedLicenses by remember { mutableStateOf<List<Pair<LicenseType, String>>>(emptyList()) }
    
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        currentLicense = licenseManager.getLicenseInfo()
        daysRemaining = licenseManager.getDaysRemaining()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientPurple, GradientCyan, GradientPink)
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
                                    "License Generator",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Super Admin Access",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable { showGenerateDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Generate",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            },
            containerColor = LightBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Quick Generate Button
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = NeonGreen.copy(alpha = 0.2f)
                            )
                            .clickable { showGenerateDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(NeonGreen.copy(alpha = 0.1f), GradientCyan.copy(alpha = 0.1f))
                                    )
                                )
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            brush = Brush.linearGradient(listOf(NeonGreen, GradientCyan)),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        "Generate New License",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Text(
                                        "Create and copy license key",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Generated Licenses
                if (generatedLicenses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Generated Licenses (${generatedLicenses.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }

                    items(generatedLicenses.reversed()) { (type, key) ->
                        GeneratedLicenseCard(type = type, licenseKey = key)
                    }
                }

                // License Types
                item {
                    Text(
                        text = "License Types Available",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }

                items(LicenseType.values().toList()) { type ->
                    LicenseTypeCard(type = type)
                }

                // Contact Card
                item {
                    ContactCard()
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }

    if (showGenerateDialog) {
        GenerateLicenseDialog(
            licenseManager = licenseManager,
            onDismiss = { showGenerateDialog = false },
            onGenerate = { type, key ->
                generatedLicenses = generatedLicenses + (type to key)
            }
        )
    }
}

@Composable
fun GeneratedLicenseCard(type: LicenseType, licenseKey: String) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val typeColor = try {
        Color(android.graphics.Color.parseColor(type.colorHex))
    } catch (e: Exception) { GradientPurple }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = typeColor.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(90.dp)
                    .background(typeColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(type.icon, fontSize = 20.sp)
                        Text(
                            type.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightSurfaceVariant
                    ) {
                        Text(
                            licenseKey,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = typeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Button(
                    onClick = { 
                        clipboardManager.setText(AnnotatedString(licenseKey))
                        copied = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (copied) Success else typeColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (copied) "Copied!" else "Copy",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LicenseTypeCard(type: LicenseType) {
    val typeColor = try {
        Color(android.graphics.Color.parseColor(type.colorHex))
    } catch (e: Exception) { GradientPurple }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(type.icon, fontSize = 22.sp)
                }
                Column {
                    Text(
                        type.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                    Text(
                        "${type.durationDays} days • ${type.maxDevices} device(s)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = typeColor.copy(alpha = 0.12f)
            ) {
                Text(
                    type.code,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
            }
        }
    }
}

@Composable
fun ContactCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = NeonGreen.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(130.dp)
                    .background(
                        brush = Brush.verticalGradient(listOf(NeonGreen, GradientCyan)),
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(NeonGreen, GradientCyan)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.SupportAgent, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Text(
                        "Support & Licensing",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                Text(
                    "${LicenseContact.DEVELOPER_NAME} @ ${LicenseContact.COMPANY_NAME}",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Phone, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    Text(
                        LicenseContact.PHONE,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateLicenseDialog(
    licenseManager: LicenseManager,
    onDismiss: () -> Unit,
    onGenerate: (LicenseType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(LicenseType.FULL_3_DEVICES) }
    var phoneNumber by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }  // Phone validation error
    var isCheckingPhone by remember { mutableStateOf(false) }    // Phone check in progress
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(GradientPurple, GradientCyan)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Key,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            "Generate License",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            "Create new license key",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Client Name Field
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client/Business Name") },
                    placeholder = { Text("e.g., Shell Petrol Station") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Business, null, tint = GradientPurple)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientPurple,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                // Phone Number Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it
                        phoneError = null  // Clear error when user types
                    },
                    label = { Text("Linked Phone Number") },
                    placeholder = { Text("+254720316175") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Phone, null, tint = if (phoneError != null) Error else NeonGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (phoneError != null) Error else NeonGreen,
                        unfocusedBorderColor = if (phoneError != null) Error else CardBorder,
                        errorBorderColor = Error
                    ),
                    isError = phoneError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    ),
                    supportingText = if (phoneError != null) {
                        { Text(phoneError!!, color = Error, fontSize = 12.sp) }
                    } else null
                )

                // License Type Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Select License Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                    ExposedDropdownMenuBox(
                        expanded = showDropdown,
                        onExpandedChange = { showDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedType.icon} ${selectedType.displayName}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientPurple,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            LicenseType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(type.icon, fontSize = 20.sp)
                                            Column {
                                                Text(type.displayName, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "${type.durationDays} days • ${type.maxDevices} device(s)",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedType = type
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // License Info Summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedType.icon, fontSize = 16.sp)
                            Text(
                                "License Summary",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }
                        if (clientName.isNotBlank()) {
                            Text("Client: $clientName", fontSize = 12.sp, color = TextSecondary)
                        }
                        if (phoneNumber.isNotBlank()) {
                            Text("Phone: $phoneNumber", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text("Duration: ${selectedType.durationDays} days", fontSize = 12.sp, color = TextSecondary)
                        Text("Devices: ${selectedType.maxDevices}", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                    
                    var isGenerating by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isGenerating = true
                                phoneError = null
                                
                                // Check for duplicate phone number before generating
                                if (phoneNumber.isNotBlank()) {
                                    val existingLicense = licenseManager.checkPhoneDuplicate(phoneNumber)
                                    if (existingLicense != null) {
                                        phoneError = "⚠️ This phone already has a license!\nClient: ${existingLicense.clientName ?: "Unknown"}\nLicense: ${existingLicense.licenseKey.takeLast(16)}"
                                        isGenerating = false
                                        return@launch
                                    }
                                }
                                
                                // Generate the license
                                val key = licenseManager.generateLicenseKey(
                                    licenseType = selectedType,
                                    clientName = clientName.takeIf { it.isNotBlank() },
                                    clientPhone = phoneNumber.takeIf { it.isNotBlank() }
                                )
                                // Copy license with client info
                                val fullInfo = buildString {
                                    append(key)
                                    if (clientName.isNotBlank() || phoneNumber.isNotBlank()) {
                                        append("\n---")
                                        if (clientName.isNotBlank()) append("\nClient: $clientName")
                                        if (phoneNumber.isNotBlank()) append("\nPhone: $phoneNumber")
                                        append("\nType: ${selectedType.displayName}")
                                        append("\nDuration: ${selectedType.durationDays} days")
                                    }
                                }
                                clipboardManager.setText(AnnotatedString(fullInfo))
                                onGenerate(selectedType, key)
                                isGenerating = false
                                onDismiss()
                            }
                        },
                        enabled = !isGenerating,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(GradientPurple, GradientCyan)),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.ContentCopy, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Text("Generate & Copy", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
