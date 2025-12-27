package com.energyapp.ui.screens.attendant

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.components.ErrorDialog
import com.energyapp.ui.components.LoadingDialog
import com.energyapp.ui.theme.*

/**
 * Sales Screen - Modern Light Professional 2024 Design
 * Features:
 * - LIGHT gradient colors matching main screen
 * - Cash + M-Pesa payment options
 * - Auto-calculate liters from amount
 * - Receipt number like web app (RCP-XXXXX)
 * - Keyboard-aware scrolling (imePadding)
 * - User and Shift info display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            // LIGHT Gradient Header
            LightHeader(
                receiptNumber = uiState.receiptNumber,
                userName = uiState.loggedInUserName,
                shiftName = uiState.currentShiftName,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        // Main Content - Keyboard-aware scrollable column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding() // This handles keyboard
                .navigationBarsPadding() // This handles nav bar
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
                // User & Shift Info Card
                if (uiState.loggedInUserName.isNotEmpty() || uiState.currentShiftName.isNotEmpty()) {
                    AttendantInfoCard(
                        userName = uiState.loggedInUserName,
                        shiftName = uiState.currentShiftName,
                        pumpName = uiState.selectedPump?.pumpName ?: ""
                    )
                }

                // Pump Selection
                PumpSelectorCard(
                    pumps = uiState.pumps,
                    selectedPump = uiState.selectedPump,
                    onPumpSelect = viewModel::selectPump
                )

                if (uiState.pumps.isEmpty()) {
                    NoPumpsWarning()
                } else {
                    // Fuel Info
                    FuelInfoBanner(
                        fuelTypeName = uiState.selectedPump?.fuelTypeName ?: "",
                        pricePerLiter = uiState.pricePerLiter
                    )

                    // Amount & Liters
                    AmountLitersSection(
                        amount = uiState.amount,
                        onAmountChange = viewModel::onAmountChange,
                        litersSold = uiState.litersSold,
                        enabled = !uiState.isProcessing
                    )

                    // Payment Method
                    PaymentMethodSection(
                        selectedMethod = uiState.paymentMethod,
                        onMethodSelect = viewModel::setPaymentMethod,
                        enabled = !uiState.isProcessing
                    )

                    // Phone Number (M-Pesa only)
                    if (uiState.paymentMethod == PaymentMethod.MPESA) {
                        PhoneInputCard(
                            mobile = uiState.customerMobile,
                            onMobileChange = viewModel::onCustomerMobileChange,
                            enabled = !uiState.isProcessing
                        )
                    }

                    // Validation Error
                    uiState.validationError?.let { error ->
                        ErrorBanner(error)
                    }

                    // Main Payment Button
                    PaymentActionButton(
                        isProcessing = uiState.isProcessing,
                        isEnabled = !uiState.isProcessing && uiState.pumps.isNotEmpty(),
                        paymentMethod = uiState.paymentMethod,
                        pollingAttempt = uiState.pollingAttempt,
                        maxAttempts = uiState.maxPollingAttempts,
                        onClick = viewModel::processPayment
                    )

                    // Quick Actions
                    QuickActionsBar(
                        onClear = viewModel::clearForm,
                        onRefresh = viewModel::refresh
                    )
                // Extra space for keyboard
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Processing Dialog
    if (uiState.isProcessing && uiState.paymentMethod == PaymentMethod.MPESA) {
        MpesaProcessingDialog(
            pollingAttempt = uiState.pollingAttempt,
            maxAttempts = uiState.maxPollingAttempts
        )
    }

    // Error Dialog
    uiState.error?.let { error ->
        ErrorDialog(
            message = error,
            onDismiss = viewModel::clearMessages
        )
    }

    // Success Dialog
    uiState.successMessage?.let { message ->
        SuccessDialog(
            message = message,
            receipt = uiState.mpesaReceipt,
            receiptNumber = uiState.receiptNumber,
            paymentMethod = uiState.paymentMethod,
            onDismiss = {
                viewModel.clearMessages()
                viewModel.clearForm()
            }
        )
    }
}

// ==================== LIGHT HEADER ====================

@Composable
fun LightHeader(
    receiptNumber: String,
    userName: String = "",
    shiftName: String = "",
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE), // Light blue
                        Color(0xFFE0F7FA), // Light cyan
                        Color(0xFFF0FDF4)  // Light green
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "⛽ Record Sale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        "📝 $receiptNumber",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Right side: User & Shift
            if (userName.isNotEmpty() || shiftName.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (userName.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("👤", fontSize = 12.sp)
                            Text(
                                userName,
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (shiftName.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🕐", fontSize = 10.sp)
                            Text(
                                shiftName,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== ATTENDANT INFO CARD ====================

@Composable
fun AttendantInfoCard(
    userName: String,
    shiftName: String,
    pumpName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = if (userName.isNotEmpty()) userName else "Attendant",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    if (pumpName.isNotEmpty()) {
                        Text(
                            text = "📍 $pumpName",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
            
            if (shiftName.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF0EA5E9).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🕐 $shiftName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }
    }
}

// ==================== PUMP SELECTOR ====================

@Composable
fun PumpSelectorCard(
    pumps: List<Pump>,
    selectedPump: Pump?,
    onPumpSelect: (Pump) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "⛽ Select Pump",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pumps.forEach { pump ->
                    PumpChipModern(
                        pump = pump,
                        isSelected = selectedPump?.pumpId == pump.pumpId,
                        onClick = { onPumpSelect(pump) }
                    )
                }
            }
        }
    }
}

@Composable
fun PumpChipModern(
    pump: Pump,
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
                            listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                    )
                } else {
                    Modifier
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = pump.pumpName,
                    color = if (isSelected) Color.White else Color(0xFF334155),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (pump.fuelTypeName.isNotEmpty()) {
                Text(
                    text = pump.fuelTypeName,
                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun NoPumpsWarning() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 22.sp)
            Text(
                text = "No pumps available. Ask admin to open a shift.",
                color = Color(0xFFDC2626),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

// ==================== FUEL INFO ====================

@Composable
fun FuelInfoBanner(
    fuelTypeName: String,
    pricePerLiter: Double
) {
    if (pricePerLiter > 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💰 ${if (fuelTypeName.isNotEmpty()) fuelTypeName else "Fuel"} @ KES ${String.format("%.2f", pricePerLiter)}/L",
                fontSize = 14.sp,
                color = Color(0xFF059669),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==================== AMOUNT & LITERS ====================

@Composable
fun AmountLitersSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    litersSold: Double,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Amount Input
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "💵 Amount (KES)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    placeholder = {
                        Text("0", color = Color(0xFF94A3B8), fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = Color(0xFF1E293B),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF22C55E)
                    )
                )
            }
        }

        // Liters Display
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "⛽ Liters (Auto)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.5.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${String.format("%.2f", litersSold)} L",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

// ==================== PAYMENT METHOD ====================

@Composable
fun PaymentMethodSection(
    selectedMethod: PaymentMethod,
    onMethodSelect: (PaymentMethod) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // M-Pesa
        PaymentOptionCard(
            emoji = "📱",
            label = "M-Pesa",
            isSelected = selectedMethod == PaymentMethod.MPESA,
            enabled = enabled,
            selectedColor = Color(0xFF10B981),
            onClick = { onMethodSelect(PaymentMethod.MPESA) },
            modifier = Modifier.weight(1f)
        )

        // Cash
        PaymentOptionCard(
            emoji = "💵",
            label = "Cash",
            isSelected = selectedMethod == PaymentMethod.CASH,
            enabled = enabled,
            selectedColor = Color(0xFFF59E0B),
            onClick = { onMethodSelect(PaymentMethod.CASH) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PaymentOptionCard(
    emoji: String,
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(60.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isSelected) {
                        Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF334155)
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==================== PHONE INPUT ====================

@Composable
fun PhoneInputCard(
    mobile: String,
    onMobileChange: (String) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "📞 Customer Phone Number",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7C3AED)
            )
            TextField(
                value = mobile,
                onValueChange = onMobileChange,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F3FF))
                    .border(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                placeholder = {
                    Text("07XXXXXXXX", color = Color(0xFF94A3B8), fontSize = 16.sp)
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF1E293B),
                    fontSize = 17.sp,
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
                    cursorColor = Color(0xFF8B5CF6)
                )
            )
        }
    }
}

// ==================== ERROR BANNER ====================

@Composable
fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 18.sp)
            Text(
                text = message,
                color = Color(0xFFDC2626),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==================== PAYMENT BUTTON ====================

@Composable
fun PaymentActionButton(
    isProcessing: Boolean,
    isEnabled: Boolean,
    paymentMethod: PaymentMethod,
    pollingAttempt: Int,
    maxAttempts: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (paymentMethod == PaymentMethod.MPESA) Color(0xFF10B981) else Color(0xFFF59E0B),
            disabledContainerColor = Color(0xFFCBD5E1)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
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
                Text(
                    text = "Processing... ($pollingAttempt/$maxAttempts)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Text(
                    text = if (paymentMethod == PaymentMethod.MPESA) "📱" else "💵",
                    fontSize = 22.sp
                )
                Text(
                    text = if (paymentMethod == PaymentMethod.MPESA) "Send M-Pesa STK Push" else "Record Cash Sale",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ==================== QUICK ACTIONS ====================

@Composable
fun QuickActionsBar(
    onClear: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            emoji = "🔄",
            label = "Clear",
            onClick = onClear,
            backgroundColor = Color(0xFFFEE2E2),
            textColor = Color(0xFFDC2626),
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            emoji = "♻️",
            label = "Refresh",
            onClick = onRefresh,
            backgroundColor = Color(0xFFE0F2FE),
            textColor = Color(0xFF0284C7),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(46.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

// ==================== DIALOGS ====================

@Composable
fun MpesaProcessingDialog(
    pollingAttempt: Int,
    maxAttempts: Int
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = Color(0xFF10B981),
                    strokeWidth = 4.dp
                )
                Text("📱", fontSize = 28.sp)
            }
        },
        title = {
            Text(
                "Processing M-Pesa Payment",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Please check your phone for the STK Push prompt and enter your M-Pesa PIN",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
                Text(
                    "Checking... ($pollingAttempt/$maxAttempts)",
                    fontSize = 12.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SuccessDialog(
    message: String,
    receipt: String?,
    receiptNumber: String,
    paymentMethod: PaymentMethod,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        color = if (paymentMethod == PaymentMethod.MPESA) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (paymentMethod == PaymentMethod.MPESA) "✅" else "💵",
                    fontSize = 32.sp
                )
            }
        },
        title = {
            Text(
                if (paymentMethod == PaymentMethod.MPESA) "Payment Successful!" else "Cash Sale Recorded!",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF334155),
                    fontSize = 14.sp
                )
                
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (paymentMethod == PaymentMethod.MPESA) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Receipt Number",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = receiptNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (paymentMethod == PaymentMethod.MPESA) Color(0xFF059669) else Color(0xFFD97706)
                        )
                        if (!receipt.isNullOrEmpty() && paymentMethod == PaymentMethod.MPESA) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "M-Pesa: $receipt",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (paymentMethod == PaymentMethod.MPESA) Color(0xFF10B981) else Color(0xFFF59E0B)
                ),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    "✅ Done - New Sale",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

// Legacy compatibility
@Composable
fun ModernSaleIdCard(saleId: String) { }

@Composable
fun ModernPaymentSuccessDialog(
    message: String,
    receipt: String?,
    checkoutRequestId: String?,
    onDismiss: () -> Unit
) = SuccessDialog(
    message = message,
    receipt = receipt,
    receiptNumber = receipt ?: "",
    paymentMethod = PaymentMethod.MPESA,
    onDismiss = onDismiss
)

// Keep old names for backward compatibility
@Composable
fun CompactHeader(
    receiptNumber: String,
    userName: String = "",
    shiftName: String = "",
    onNavigateBack: () -> Unit
) = LightHeader(receiptNumber, userName, shiftName, onNavigateBack)

@Composable
fun UserShiftInfoCard(
    userName: String,
    shiftName: String,
    pumpName: String
) = AttendantInfoCard(userName, shiftName, pumpName)

@Composable
fun PumpSelectionRow(
    pumps: List<Pump>,
    selectedPump: Pump?,
    onPumpSelect: (Pump) -> Unit
) = PumpSelectorCard(pumps, selectedPump, onPumpSelect)

@Composable 
fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelect: (PaymentMethod) -> Unit,
    enabled: Boolean
) = PaymentMethodSection(selectedMethod, onMethodSelect, enabled)

@Composable
fun PhoneNumberInput(
    mobile: String,
    onMobileChange: (String) -> Unit,
    enabled: Boolean  
) = PhoneInputCard(mobile, onMobileChange, enabled)

@Composable
fun ValidationErrorCard(message: String) = ErrorBanner(message)

@Composable
fun MainPaymentButton(
    isProcessing: Boolean,
    isEnabled: Boolean,
    paymentMethod: PaymentMethod,
    pollingAttempt: Int,
    maxAttempts: Int,
    onClick: () -> Unit
) = PaymentActionButton(isProcessing, isEnabled, paymentMethod, pollingAttempt, maxAttempts, onClick)

@Composable
fun QuickActionsRow(
    onClear: () -> Unit,
    onRefresh: () -> Unit
) = QuickActionsBar(onClear, onRefresh)

@Composable
fun ProcessingDialog(
    pollingAttempt: Int,
    maxAttempts: Int
) = MpesaProcessingDialog(pollingAttempt, maxAttempts)

@Composable
fun PaymentSuccessDialog(
    message: String,
    receipt: String?,
    receiptNumber: String,
    paymentMethod: PaymentMethod,
    onDismiss: () -> Unit
) = SuccessDialog(message, receipt, receiptNumber, paymentMethod, onDismiss)

@Composable
fun PumpChip(
    pump: Pump,
    isSelected: Boolean,
    onClick: () -> Unit
) = PumpChipModern(pump, isSelected, onClick)

@Composable
fun AmountLitersCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    litersSold: Double,
    pricePerLiter: Double,
    fuelTypeName: String,
    enabled: Boolean
) = AmountLitersSection(amount, onAmountChange, litersSold, enabled)