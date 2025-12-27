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
 * Sales Screen - Super Modern Compact 2024 Design
 * Features:
 * - Cash + M-Pesa payment options
 * - Auto-calculate liters from amount
 * - Receipt number like web app (RCP-XXXXX)
 * - Compact layout - no scrolling needed
 * - Faster STK Push polling
 * - Light theme with vibrant gradients
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Compact Gradient Header
            CompactHeader(
                receiptNumber = uiState.receiptNumber,
                onNavigateBack = onNavigateBack
            )

            // Main Content - Single scrollable column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pump Selection - Horizontal chips
                PumpSelectionRow(
                    pumps = uiState.pumps,
                    selectedPump = uiState.selectedPump,
                    onPumpSelect = viewModel::selectPump
                )

                if (uiState.pumps.isEmpty()) {
                    NoPumpsWarning()
                } else {
                    // Amount & Liters Card - Side by side
                    AmountLitersCard(
                        amount = uiState.amount,
                        onAmountChange = viewModel::onAmountChange,
                        litersSold = uiState.litersSold,
                        pricePerLiter = uiState.pricePerLiter,
                        fuelTypeName = uiState.selectedPump?.fuelTypeName ?: "",
                        enabled = !uiState.isProcessing
                    )

                    // Payment Method Selection
                    PaymentMethodSelector(
                        selectedMethod = uiState.paymentMethod,
                        onMethodSelect = viewModel::setPaymentMethod,
                        enabled = !uiState.isProcessing
                    )

                    // Phone Number (only for M-Pesa)
                    if (uiState.paymentMethod == PaymentMethod.MPESA) {
                        PhoneNumberInput(
                            mobile = uiState.customerMobile,
                            onMobileChange = viewModel::onCustomerMobileChange,
                            enabled = !uiState.isProcessing
                        )
                    }

                    // Validation Error
                    if (uiState.validationError != null) {
                        ValidationErrorCard(uiState.validationError!!)
                    }

                    // Main Action Button
                    MainPaymentButton(
                        isProcessing = uiState.isProcessing,
                        isEnabled = !uiState.isProcessing && uiState.pumps.isNotEmpty(),
                        paymentMethod = uiState.paymentMethod,
                        pollingAttempt = uiState.pollingAttempt,
                        maxAttempts = uiState.maxPollingAttempts,
                        onClick = viewModel::processPayment
                    )

                    // Quick Actions Row
                    QuickActionsRow(
                        onClear = viewModel::clearForm,
                        onRefresh = viewModel::refresh
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Processing Dialog
    if (uiState.isProcessing && uiState.paymentMethod == PaymentMethod.MPESA) {
        ProcessingDialog(
            pollingAttempt = uiState.pollingAttempt,
            maxAttempts = uiState.maxPollingAttempts
        )
    }

    // Error Dialog
    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error!!,
            onDismiss = viewModel::clearMessages
        )
    }

    // Success Dialog
    if (uiState.successMessage != null) {
        PaymentSuccessDialog(
            message = uiState.successMessage!!,
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

// ==================== COMPACT HEADER ====================

@Composable
fun CompactHeader(
    receiptNumber: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF7C3AED), // Purple
                        Color(0xFF06B6D4), // Cyan
                        Color(0xFFEC4899)  // Pink
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "⛽ Record Sale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        "📝 $receiptNumber",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==================== PUMP SELECTION ====================

@Composable
fun PumpSelectionRow(
    pumps: List<Pump>,
    selectedPump: Pump?,
    onPumpSelect: (Pump) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("⛽", fontSize = 16.sp)
                Text(
                    "Select Pump",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pumps.forEach { pump ->
                    PumpChip(
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
fun PumpChip(
    pump: Pump,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF06B6D4), Color(0xFF14B8A6))
                        )
                    )
                } else {
                    Modifier
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.LocalGasStation,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = pump.pumpName,
                    color = if (isSelected) Color.White else OnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (pump.fuelTypeName.isNotEmpty()) {
                Text(
                    text = pump.fuelTypeName,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
                    fontSize = 10.sp
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 20.sp)
            Text(
                text = "No pumps available. Ask admin to open a shift.",
                color = Error,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

// ==================== AMOUNT & LITERS CARD ====================

@Composable
fun AmountLitersCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    litersSold: Double,
    pricePerLiter: Double,
    fuelTypeName: String,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Price info row
            if (pricePerLiter > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💰 ${if (fuelTypeName.isNotEmpty()) fuelTypeName else "Fuel"} @ KES ${String.format("%.2f", pricePerLiter)}/L",
                        fontSize = 12.sp,
                        color = Color(0xFF06B6D4),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Amount and Liters side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Amount Input
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "💵 Amount (KES)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                    TextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(2.dp, Color(0xFF22C55E).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        placeholder = {
                            Text("0", color = TextSecondary, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = OnSurface,
                            fontSize = 20.sp,
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

                // Liters Display
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "⛽ Liters (Auto)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(2.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${String.format("%.2f", litersSold)} L",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

// ==================== PAYMENT METHOD SELECTOR ====================

@Composable
fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelect: (PaymentMethod) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // M-Pesa Button
        PaymentMethodButton(
            emoji = "📱",
            label = "M-Pesa",
            isSelected = selectedMethod == PaymentMethod.MPESA,
            enabled = enabled,
            gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
            onClick = { onMethodSelect(PaymentMethod.MPESA) },
            modifier = Modifier.weight(1f)
        )

        // Cash Button
        PaymentMethodButton(
            emoji = "💵",
            label = "Cash",
            isSelected = selectedMethod == PaymentMethod.CASH,
            enabled = enabled,
            gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
            onClick = { onMethodSelect(PaymentMethod.CASH) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PaymentMethodButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(60.dp)
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Transparent else Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            brush = Brush.horizontalGradient(gradientColors)
                        )
                    } else {
                        Modifier.border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else OnSurface
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

// ==================== PHONE NUMBER INPUT ====================

@Composable
fun PhoneNumberInput(
    mobile: String,
    onMobileChange: (String) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "📞 Customer Phone Number",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDB2777)
            )
            TextField(
                value = mobile,
                onValueChange = onMobileChange,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFDF2F8))
                    .border(1.5.dp, Color(0xFFEC4899).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                placeholder = {
                    Text("07XXXXXXXX", color = TextSecondary, fontSize = 15.sp)
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
                    cursorColor = Color(0xFFEC4899)
                )
            )
        }
    }
}

// ==================== VALIDATION ERROR ====================

@Composable
fun ValidationErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 16.sp)
            Text(
                text = message,
                color = Error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==================== MAIN PAYMENT BUTTON ====================

@Composable
fun MainPaymentButton(
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
            .height(58.dp)
            .shadow(
                elevation = if (isEnabled) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isEnabled) Color(0xFF7C3AED).copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
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
                        if (paymentMethod == PaymentMethod.MPESA) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF22C55E), Color(0xFF16A34A), Color(0xFF15803D))
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color(0xFFF97316), Color(0xFFEA580C), Color(0xFFC2410C))
                            )
                        }
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.3f))
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Processing... ($pollingAttempt/$maxAttempts)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (paymentMethod == PaymentMethod.MPESA) "📱" else "💵",
                        fontSize = 20.sp
                    )
                    Text(
                        text = if (paymentMethod == PaymentMethod.MPESA) "Send M-Pesa STK Push" else "Record Cash Sale",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==================== QUICK ACTIONS ====================

@Composable
fun QuickActionsRow(
    onClear: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionChip(
            emoji = "🔄",
            label = "Clear",
            onClick = onClear,
            modifier = Modifier.weight(1f),
            backgroundColor = Color(0xFFFEE2E2),
            textColor = Error
        )
        QuickActionChip(
            emoji = "♻️",
            label = "Refresh",
            onClick = onRefresh,
            modifier = Modifier.weight(1f),
            backgroundColor = Color(0xFFEFF6FF),
            textColor = Color(0xFF2563EB)
        )
    }
}

@Composable
fun QuickActionChip(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

// ==================== PROCESSING DIALOG ====================

@Composable
fun ProcessingDialog(
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
                    color = Color(0xFF22C55E),
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
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    "Checking... ($pollingAttempt/$maxAttempts)",
                    fontSize = 12.sp,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

// ==================== SUCCESS DIALOG ====================

@Composable
fun PaymentSuccessDialog(
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
                        brush = Brush.linearGradient(
                            if (paymentMethod == PaymentMethod.MPESA)
                                listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                            else
                                listOf(Color(0xFFF97316), Color(0xFFEA580C))
                        ),
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
                    color = OnSurface,
                    fontSize = 14.sp
                )
                
                // Receipt Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (paymentMethod == PaymentMethod.MPESA)
                            Color(0xFFF0FDF4) else Color(0xFFFFF7ED)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Receipt Number",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = receiptNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (paymentMethod == PaymentMethod.MPESA) 
                                Color(0xFF16A34A) else Color(0xFFEA580C)
                        )
                        if (!receipt.isNullOrEmpty() && paymentMethod == PaymentMethod.MPESA) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "M-Pesa: $receipt",
                                fontSize = 12.sp,
                                color = Color(0xFF22C55E),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                if (paymentMethod == PaymentMethod.MPESA)
                                    listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                                else
                                    listOf(Color(0xFFF97316), Color(0xFFEA580C))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✅ Done - New Sale",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

// Legacy function aliases for compatibility
@Composable
fun ModernSaleIdCard(saleId: String) {
    // Kept for compatibility - handled in header now
}

@Composable
fun ModernPaymentSuccessDialog(
    message: String,
    receipt: String?,
    checkoutRequestId: String?,
    onDismiss: () -> Unit
) = PaymentSuccessDialog(
    message = message,
    receipt = receipt,
    receiptNumber = receipt ?: "",
    paymentMethod = PaymentMethod.MPESA,
    onDismiss = onDismiss
)