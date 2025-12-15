package com.energyapp.ui.screens.attendant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.components.ErrorDialog
import com.energyapp.ui.components.LoadingDialog

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
            .background(Color(0xFFF5F5F5))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Record Sale",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* More options */ }) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    modifier = Modifier.shadow(elevation = 4.dp)
                )
            },
            containerColor = Color(0xFFF5F5F5),
            contentColor = Color.Black
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SaleIdCard(saleId = uiState.saleIdNo)
                }

                item {
                    PumpSelectionCard(
                        pumps = uiState.pumps,
                        selectedPump = uiState.selectedPump,
                        onPumpSelect = viewModel::selectPump
                    )
                }

                if (uiState.pumps.isEmpty()) {
                    item {
                        NoPumpsAvailableCard()
                    }
                } else {
                    item {
                        SaleAmountCard(
                            amount = uiState.amount,
                            onAmountChange = viewModel::onAmountChange,
                            enabled = !uiState.isProcessing
                        )
                    }

                    item {
                        CustomerDetailsCard(
                            mobile = uiState.customerMobile,
                            onMobileChange = viewModel::onCustomerMobileChange,
                            enabled = !uiState.isProcessing
                        )
                    }

                    item {
                        PaymentMethodCard()
                    }

                    item {
                        PremiumPaymentButton(
                            isProcessing = uiState.isProcessing,
                            isEnabled = !uiState.isProcessing && uiState.pumps.isNotEmpty(),
                            onClick = viewModel::initiateMpesaPayment
                        )
                    }

                    item {
                        QuickActionButtons(
                            onClear = viewModel::clearForm,
                            onHistory = { /* TODO: Navigate to history */ },
                            onReceipt = { /* TODO: Show receipt */ }
                        )
                    }

                    item {
                        MPesaInfoCard()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
        PaymentSuccessDialog(
            message = uiState.successMessage!!,
            receipt = uiState.mpesaReceipt,
            checkoutRequestId = uiState.checkoutRequestId,
            onDismiss = viewModel::clearMessages
        )
    }
}

@Composable
fun SaleIdCard(saleId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sale Transaction ID",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666),
                    fontSize = 11.sp
                )
                Text(
                    text = saleId,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "Copy",
                tint = Color(0xFFE53935),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PumpSelectionCard(
    pumps: List<Pump>,
    selectedPump: Pump?,
    onPumpSelect: (Pump) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Pump",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pumps.forEach { pump ->
                    PumpChip(
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
fun PumpChip(
    pumpName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFFE53935) else Color.White
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color(0xFFE53935) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFF999999),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = pumpName,
                color = if (isSelected) Color.White else Color.Black,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NoPumpsAvailableCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFEBEE))
            .border(
                width = 1.5.dp,
                color = Color(0xFFEF5350),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "No pumps available. Ask admin to open a shift.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE53935),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun SaleAmountCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AttachMoney,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Sale Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextField(
                value = amount,
                onValueChange = onAmountChange,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        "Enter amount in KES",
                        color = Color(0xFFCCCCCC),
                        fontSize = 13.sp
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.Black,
                    fontSize = 18.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFE53935),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0),
                    disabledIndicatorColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFFE53935)
                )
            )
        }
    }
}

@Composable
fun CustomerDetailsCard(
    mobile: String,
    onMobileChange: (String) -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Phone,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Customer Mobile",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextField(
                value = mobile,
                onValueChange = onMobileChange,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        "e.g., 0712345678",
                        color = Color(0xFFCCCCCC),
                        fontSize = 13.sp
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFE53935),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0),
                    disabledIndicatorColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFFE53935)
                )
            )
        }
    }
}

@Composable
fun PaymentMethodCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF3E0))
            .border(
                width = 1.5.dp,
                color = Color(0xFFFFE0B2),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE53935).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Payment,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF666666),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "M-Pesa STK Push",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PremiumPaymentButton(
    isProcessing: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFFE53935).copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53935),
            disabledContainerColor = Color(0xFFCCCCCC)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Payment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = if (isProcessing) "Processing..." else "Initiate M-Pesa Payment",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun QuickActionButtons(
    onClear: () -> Unit,
    onHistory: () -> Unit,
    onReceipt: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF666666),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                label = "Clear",
                icon = Icons.Rounded.Clear,
                onClick = onClear,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "History",
                icon = Icons.Rounded.History,
                onClick = onHistory,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Receipt",
                icon = Icons.Rounded.Receipt,
                onClick = onReceipt,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun MPesaInfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3E5F5))
            .border(
                width = 1.5.dp,
                color = Color(0xFFE1BEE7),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "How M-Pesa Payment Works",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF7B1FA2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "• STK Push will appear on customer's phone\n• Customer enters their M-Pesa PIN\n• Transaction completes instantly\n• Receipt will be generated automatically",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF555555),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PaymentSuccessDialog(
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFC8E6C9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                "Payment Successful",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF555555)
                )
                if (!receipt.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "M-Pesa Receipt",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF666666),
                                fontSize = 10.sp
                            )
                            Text(
                                text = receipt,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFE53935),
                                fontSize = 12.sp,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                )
            ) {
                Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color(0xFF555555)
    )
}