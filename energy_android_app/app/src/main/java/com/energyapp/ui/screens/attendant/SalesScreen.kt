package com.energyapp.ui.screens.attendant

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.components.ErrorDialog
import com.energyapp.ui.components.LoadingDialog
import com.energyapp.ui.theme.*

/**
 * Sales Screen - Super Modern 2024 Design
 * Light theme with vibrant gradients for outdoor visibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
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
                // Stunning Gradient Header
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
                                    "Record Sale",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    "M-Pesa STK Push",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        // Glassmorphism More Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "More",
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
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ModernSaleIdCard(saleId = uiState.saleIdNo)
                }

                item {
                    ModernPumpSelectionCard(
                        pumps = uiState.pumps,
                        selectedPump = uiState.selectedPump,
                        onPumpSelect = viewModel::selectPump
                    )
                }

                if (uiState.pumps.isEmpty()) {
                    item {
                        ModernNoPumpsCard()
                    }
                } else {
                    item {
                        ModernSaleAmountCard(
                            amount = uiState.amount,
                            onAmountChange = viewModel::onAmountChange,
                            enabled = !uiState.isProcessing
                        )
                    }

                    item {
                        ModernCustomerDetailsCard(
                            mobile = uiState.customerMobile,
                            onMobileChange = viewModel::onCustomerMobileChange,
                            enabled = !uiState.isProcessing
                        )
                    }

                    item {
                        ModernPaymentMethodCard()
                    }

                    item {
                        ModernPaymentButton(
                            isProcessing = uiState.isProcessing,
                            isEnabled = !uiState.isProcessing && uiState.pumps.isNotEmpty(),
                            onClick = viewModel::initiateMpesaPayment
                        )
                    }

                    item {
                        ModernQuickActionButtons(
                            onClear = viewModel::clearForm,
                            onHistory = { /* TODO */ },
                            onReceipt = { /* TODO */ }
                        )
                    }

                    item {
                        ModernMPesaInfoCard()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (uiState.isProcessing) {
        LoadingDialog(message = "Processing M-Pesa payment...\nPlease check your phone for STK Push")
    }

    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error!!,
            onDismiss = viewModel::clearMessages
        )
    }

    if (uiState.successMessage != null) {
        ModernPaymentSuccessDialog(
            message = uiState.successMessage!!,
            receipt = uiState.mpesaReceipt,
            checkoutRequestId = uiState.checkoutRequestId,
            onDismiss = viewModel::clearMessages
        )
    }
}

// ==================== MODERN SALE ID CARD ====================

@Composable
fun ModernSaleIdCard(saleId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = GradientPurple.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Purple Accent Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(70.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(GradientPurple, GradientCyan)
                        ),
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(IconGradientPurple1, IconGradientPurple2)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Sale Transaction ID",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = saleId,
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = GradientPurple.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .clickable { /* Copy */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = GradientPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==================== MODERN PUMP SELECTION ====================

@Composable
fun ModernPumpSelectionCard(
    pumps: List<Pump>,
    selectedPump: Pump?,
    onPumpSelect: (Pump) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = GradientCyan.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
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
                                listOf(IconGradientCyan1, IconGradientCyan2)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.LocalGasStation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Select Pump",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pumps.forEach { pump ->
                    ModernPumpChip(
                        pumpName = pump.pumpName,
                        isSelected = selectedPump?.pumpId == pump.pumpId,
                        onClick = { onPumpSelect(pump) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernPumpChip(
    pumpName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            listOf(GradientCyan, GradientTeal)
                        )
                    )
                } else {
                    Modifier
                        .background(CardBackground)
                        .border(1.5.dp, CardBorder, RoundedCornerShape(14.dp))
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = pumpName,
                color = if (isSelected) Color.White else OnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ModernNoPumpsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Error.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Error.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "No pumps available. Ask admin to open a shift.",
                style = MaterialTheme.typography.bodyMedium,
                color = Error,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

// ==================== MODERN AMOUNT INPUT ====================

@Composable
fun ModernSaleAmountCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = NeonGreen.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Green Accent Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(110.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(IconGradientGreen1, IconGradientGreen2)
                        ),
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    listOf(IconGradientGreen1, IconGradientGreen2)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AttachMoney,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Sale Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightSurfaceVariant)
                        .border(1.5.dp, Success.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    placeholder = {
                        Text(
                            "Enter amount in KES",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = OnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Success
                    )
                )
            }
        }
    }
}

// ==================== MODERN CUSTOMER DETAILS ====================

@Composable
fun ModernCustomerDetailsCard(
    mobile: String,
    onMobileChange: (String) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = GradientPink.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Pink Accent Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(110.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(IconGradientPink1, IconGradientPink2)
                        ),
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icons.Rounded.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Customer Mobile",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextField(
                    value = mobile,
                    onValueChange = onMobileChange,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightSurfaceVariant)
                        .border(1.5.dp, GradientPink.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    placeholder = {
                        Text(
                            "e.g., 0712345678",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = GradientPink
                    )
                )
            }
        }
    }
}

// ==================== MODERN PAYMENT METHOD ====================

@Composable
fun ModernPaymentMethodCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = NeonOrange.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarningLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(IconGradientOrange1, IconGradientOrange2)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Payment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "M-Pesa STK Push",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Success.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ==================== MODERN PAYMENT BUTTON ====================

@Composable
fun ModernPaymentButton(
    isProcessing: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isEnabled) GradientPurple.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isEnabled) {
                        Brush.horizontalGradient(
                            listOf(GradientPurple, GradientCyan, GradientPink)
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.3f))
                        )
                    },
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        Icons.Rounded.Payment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = if (isProcessing) "Processing..." else "Initiate M-Pesa Payment",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ==================== MODERN QUICK ACTIONS ====================

@Composable
fun ModernQuickActionButtons(
    onClear: () -> Unit,
    onHistory: () -> Unit,
    onReceipt: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleSmall,
            color = OnSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernQuickActionButton(
                label = "Clear",
                icon = Icons.Rounded.Clear,
                gradientColors = listOf(IconGradientPink1, IconGradientPink2),
                onClick = onClear,
                modifier = Modifier.weight(1f)
            )
            ModernQuickActionButton(
                label = "History",
                icon = Icons.Rounded.History,
                gradientColors = listOf(IconGradientPurple1, IconGradientPurple2),
                onClick = onHistory,
                modifier = Modifier.weight(1f)
            )
            ModernQuickActionButton(
                label = "Receipt",
                icon = Icons.Rounded.Receipt,
                gradientColors = listOf(IconGradientCyan1, IconGradientCyan2),
                onClick = onReceipt,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ModernQuickActionButton(
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = gradientColors[0].copy(alpha = 0.2f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )
        }
    }
}

// ==================== MODERN MPESA INFO ====================

@Composable
fun ModernMPesaInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = NeonPurple.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = InfoLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(IconGradientBlue1, IconGradientBlue2)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "How M-Pesa Payment Works",
                    style = MaterialTheme.typography.titleSmall,
                    color = Info,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("STK Push will appear on customer's phone")
                InfoRow("Customer enters their M-Pesa PIN")
                InfoRow("Transaction completes instantly")
                InfoRow("Receipt will be generated automatically")
            }
        }
    }
}

@Composable
fun InfoRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Info, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurface.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

// ==================== MODERN SUCCESS DIALOG ====================

@Composable
fun ModernPaymentSuccessDialog(
    message: String,
    receipt: String?,
    checkoutRequestId: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(IconGradientGreen1, IconGradientGreen2)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        },
        title = {
            Text(
                "Payment Successful",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (!receipt.isNullOrEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "M-Pesa Receipt",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = receipt,
                                style = MaterialTheme.typography.titleMedium,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(IconGradientGreen1, IconGradientGreen2)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Done",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp)
    )
}

// Legacy function aliases for compatibility
@Composable
fun SaleIdCard(saleId: String) = ModernSaleIdCard(saleId)

@Composable
fun PumpSelectionCard(
    pumps: List<Pump>,
    selectedPump: Pump?,
    onPumpSelect: (Pump) -> Unit
) = ModernPumpSelectionCard(pumps, selectedPump, onPumpSelect)

@Composable
fun PumpChip(
    pumpName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) = ModernPumpChip(pumpName, isSelected, onClick)

@Composable
fun NoPumpsAvailableCard() = ModernNoPumpsCard()

@Composable
fun SaleAmountCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    enabled: Boolean
) = ModernSaleAmountCard(amount, onAmountChange, enabled)

@Composable
fun CustomerDetailsCard(
    mobile: String,
    onMobileChange: (String) -> Unit,
    enabled: Boolean
) = ModernCustomerDetailsCard(mobile, onMobileChange, enabled)

@Composable
fun PaymentMethodCard() = ModernPaymentMethodCard()

@Composable
fun PremiumPaymentButton(
    isProcessing: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) = ModernPaymentButton(isProcessing, isEnabled, onClick)

@Composable
fun QuickActionButtons(
    onClear: () -> Unit,
    onHistory: () -> Unit,
    onReceipt: () -> Unit
) = ModernQuickActionButtons(onClear, onHistory, onReceipt)

@Composable
fun MPesaInfoCard() = ModernMPesaInfoCard()

@Composable
fun PaymentSuccessDialog(
    message: String,
    receipt: String?,
    checkoutRequestId: String?,
    onDismiss: () -> Unit
) = ModernPaymentSuccessDialog(message, receipt, checkoutRequestId, onDismiss)