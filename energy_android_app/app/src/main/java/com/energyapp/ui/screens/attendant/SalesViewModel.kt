package com.energyapp.ui.screens.attendant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.MpesaBackendService
import com.energyapp.data.remote.MpesaWebhookSupabaseService
import com.energyapp.data.remote.MpesaStkRequest
import com.energyapp.util.MpesaConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Data class representing a fuel pump
 */
data class Pump(
    val pumpId: Int,
    val pumpName: String,
    val currentShiftId: Int?
)

/**
 * UI State for Sales Screen
 */
data class SalesUiState(
    val pumps: List<Pump> = emptyList(),
    val selectedPump: Pump? = null,
    val saleIdNo: String = "",
    val amount: String = "",
    val customerMobile: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val mpesaReceipt: String? = null,
    val checkoutRequestId: String? = null,
    val validationError: String? = null,
    val saleId: String? = null
)

/**
 * ViewModel for Sales Screen with M-Pesa integration
 */
@HiltViewModel
class SalesViewModel @Inject constructor(
    private val supabaseApiService: SupabaseApiService,
    private val mpesaBackendService: MpesaBackendService,
    private val mpesaWebhookSupabaseService: MpesaWebhookSupabaseService
) : ViewModel() {

    private val TAG = "SalesViewModel"

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private var userId: Int = 0

    init {
        loadPumps()
        generateSaleId()
    }

    fun setUserId(id: Int) {
        userId = id
        Log.d(TAG, "User ID set to: $userId")
    }

    private fun loadPumps() {
        viewModelScope.launch {
            try {
                val result = supabaseApiService.getPumps()
                if (result.isSuccess) {
                    val pumpResponses = result.getOrThrow()
                    val openShiftsResult = supabaseApiService.getOpenShifts()
                    val openShifts = openShiftsResult.getOrNull() ?: emptyList()

                    val pumps = pumpResponses.mapNotNull { pumpResponse ->
                        val openShift = openShifts.find { it.pumpId == pumpResponse.pumpId && !it.isClosed }
                        if (openShift != null) {
                            Pump(
                                pumpId = pumpResponse.pumpId,
                                pumpName = pumpResponse.pumpName,
                                currentShiftId = openShift.pumpShiftId
                            )
                        } else null
                    }

                    _uiState.value = _uiState.value.copy(
                        pumps = pumps,
                        selectedPump = pumps.firstOrNull()
                    )

                    Log.d(TAG, "✅ Loaded ${pumps.size} pumps with open shifts")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load pumps: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load pumps: ${e.message}"
                )
            }
        }
    }

    private fun generateSaleId() {
        val saleId = "SALE-${System.currentTimeMillis()}"
        _uiState.value = _uiState.value.copy(saleIdNo = saleId)
    }

    fun selectPump(pump: Pump) {
        _uiState.value = _uiState.value.copy(selectedPump = pump, error = null)
        Log.d(TAG, "Pump selected: ${pump.pumpName}")
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount, validationError = null, error = null)
    }

    fun onCustomerMobileChange(mobile: String) {
        _uiState.value = _uiState.value.copy(customerMobile = mobile, validationError = null, error = null)
    }

    fun clearForm() {
        _uiState.value = _uiState.value.copy(
            amount = "",
            customerMobile = "",
            error = null,
            validationError = null,
            successMessage = null,
            mpesaReceipt = null,
            checkoutRequestId = null,
            selectedPump = _uiState.value.pumps.firstOrNull()
        )
        generateSaleId()
        Log.d(TAG, "Form cleared")
    }

    /**
     * Validate all inputs before payment - IMPROVED VALIDATION
     */
    private fun validateInputs(): String? {
        val pump = _uiState.value.selectedPump
        val amountStr = _uiState.value.amount.trim()
        val mobile = _uiState.value.customerMobile.trim()

        return when {
            pump == null -> "No pump with open shift available"
            pump.currentShiftId == null -> "No active shift for selected pump"
            amountStr.isEmpty() -> "Please enter an amount"
            amountStr.toDoubleOrNull() == null -> "Please enter a valid amount"
            amountStr.toDouble() <= 0 -> "Amount must be greater than 0"
            amountStr.toDouble() > 150000 -> "Amount cannot exceed KES 150,000"
            mobile.isEmpty() -> "Please enter customer mobile number"
            !MpesaConfig.isValidKenyanPhone(mobile) -> {
                "Invalid mobile format. Use 07XXXXXXXX or 254XXXXXXXXX"
            }
            else -> null
        }
    }

    fun initiateMpesaPayment() {
        Log.d(TAG, "🚀 Initiating M-Pesa payment...")

        // Clear any previous messages
        _uiState.value = _uiState.value.copy(
            error = null,
            validationError = null,
            successMessage = null
        )

        // Validate inputs
        val validationError = validateInputs()
        if (validationError != null) {
            Log.w(TAG, "⚠️ Validation failed: $validationError")
            _uiState.value = _uiState.value.copy(validationError = validationError)
            return
        }

        val pump = _uiState.value.selectedPump ?: return
        val amount = _uiState.value.amount.trim().toDoubleOrNull() ?: return
        val mobile = _uiState.value.customerMobile.trim()

        viewModelScope.launch {
            // Set processing state IMMEDIATELY
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                error = null,
                validationError = null
            )

            try {
                val shiftId = pump.currentShiftId ?: 0
                val formattedPhone = MpesaConfig.formatPhoneNumber(mobile)

                Log.d(TAG, "📞 Formatted phone: $formattedPhone")
                Log.d(TAG, "💰 Amount: KES $amount")
                Log.d(TAG, "⛽ Pump: ${pump.pumpName} (ID: ${pump.pumpId})")
                Log.d(TAG, "👤 User ID: $userId")

                // Short account reference for M-Pesa compliance (max 12 chars)
                val shortAccountReference = "P${pump.pumpId}".take(4)

                // Call STK Push
                Log.d(TAG, "📡 Calling STK Push API...")
                val stkResponse = try {
                    mpesaBackendService.initiateStkPush(
                        MpesaStkRequest(
                            amount = amount,
                            phone = formattedPhone,
                            account = shortAccountReference,
                            description = "Fuel - Pump ${pump.pumpName}",
                            userId = userId.toString(),
                            pumpId = pump.pumpId.toString(),
                            shiftId = shiftId.toString()
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Network error: ${e.message}")
                    throw Exception("Failed to reach payment service: ${e.message}")
                }

                Log.d(TAG, "📥 STK Response received: success=${stkResponse.success}")

                // Check if STK Push was successful
                if (!stkResponse.success) {
                    Log.e(TAG, "❌ STK Push rejected: ${stkResponse.message}")
                    throw Exception(stkResponse.message)
                }

                // CRITICAL: Validate CheckoutRequestID exists
                val checkoutRequestID = stkResponse.checkoutRequestID
                if (checkoutRequestID.isNullOrEmpty()) {
                    Log.e(TAG, "❌ FATAL: No CheckoutRequestID in response")
                    throw Exception("Payment request incomplete - no checkout ID received. Please try again.")
                }

                val saleId = stkResponse.saleId
                Log.d(TAG, "✅ STK Push successful!")
                Log.d(TAG, "🎫 CheckoutRequestID: $checkoutRequestID")
                Log.d(TAG, "🆔 Sale ID: $saleId")

                // Save to webhook project (non-blocking)
                try {
                    val mpesaSaveResult = mpesaWebhookSupabaseService.saveStkPushResponse(
                        checkoutRequestId = checkoutRequestID,
                        merchantRequestId = stkResponse.merchantRequestID,
                        amount = amount,
                        phoneNumber = formattedPhone,
                        accountReference = _uiState.value.saleIdNo
                    )

                    if (mpesaSaveResult.isFailure) {
                        Log.w(TAG, "⚠️ Warning: M-Pesa data not saved to webhook project")
                    } else {
                        Log.d(TAG, "✅ M-Pesa data saved to webhook project")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Webhook save error (non-critical): ${e.message}")
                }

                // Update UI to show "processing" with checkout ID
                _uiState.value = _uiState.value.copy(
                    isProcessing = true,
                    checkoutRequestId = checkoutRequestID,
                    saleId = saleId,
                    error = null
                )

                // Start polling for transaction status
                pollTransactionStatus(checkoutRequestID, saleId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Payment initiation error: ${e.message}")
                e.printStackTrace()

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Payment request failed. Please try again."
                )
            }
        }
    }

    /**
     * Poll for transaction status - IMPROVED WITH BETTER LOGGING
     */
    private fun pollTransactionStatus(checkoutRequestID: String, saleId: String?) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Starting status polling for: $checkoutRequestID")

            // Poll for about 2 minutes (24 attempts * 5 seconds)
            repeat(24) { attempt ->
                delay(5000) // 5 second intervals

                val attemptNum = attempt + 1
                Log.d(TAG, "🔍 Polling attempt $attemptNum/24...")

                try {
                    val statusResult = mpesaBackendService.checkTransactionStatus(checkoutRequestID)

                    Log.d(TAG, "📊 Status: success=${statusResult.success}, resultCode=${statusResult.resultCode}")

                    if (statusResult.success) {
                        when (statusResult.resultCode) {
                            null -> {
                                // Still pending
                                Log.d(TAG, "⏳ Payment pending (attempt $attemptNum/24)")
                            }

                            0 -> {
                                // ✅ SUCCESS
                                Log.d(TAG, "✅ PAYMENT SUCCESSFUL!")
                                Log.d(TAG, "💳 Receipt: ${statusResult.mpesaReceiptNumber}")

                                // Update webhook project
                                try {
                                    mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                        checkoutRequestId = checkoutRequestID,
                                        resultCode = 0,
                                        resultDesc = statusResult.resultDesc ?: "Success",
                                        mpesaReceiptNumber = statusResult.mpesaReceiptNumber,
                                        transactionDate = statusResult.transactionDate
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "⚠️ Webhook update failed: ${e.message}")
                                }

                                // Update main database
                                saleId?.toIntOrNull()?.let { id ->
                                    try {
                                        supabaseApiService.updateSaleTransactionStatus(
                                            id,
                                            "SUCCESS",
                                            statusResult.mpesaReceiptNumber
                                        )
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Sale update failed: ${e.message}")
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    successMessage = "Payment Successful! Receipt: ${statusResult.mpesaReceiptNumber}",
                                    mpesaReceipt = statusResult.mpesaReceiptNumber,
                                    error = null,
                                    amount = "",
                                    customerMobile = ""
                                )
                                generateSaleId()
                                return@launch
                            }

                            1032 -> {
                                // ❌ CANCELLED
                                Log.d(TAG, "❌ Payment cancelled by user")

                                try {
                                    mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                        checkoutRequestId = checkoutRequestID,
                                        resultCode = 1032,
                                        resultDesc = "Cancelled by user",
                                        mpesaReceiptNumber = null,
                                        transactionDate = null
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "⚠️ Webhook update failed: ${e.message}")
                                }

                                saleId?.toIntOrNull()?.let { id ->
                                    try {
                                        supabaseApiService.updateSaleTransactionStatus(id, "CANCELLED")
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Sale update failed: ${e.message}")
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Payment was cancelled by user"
                                )
                                return@launch
                            }

                            1 -> {
                                // ❌ INSUFFICIENT FUNDS
                                Log.d(TAG, "❌ Insufficient funds")

                                try {
                                    mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                        checkoutRequestId = checkoutRequestID,
                                        resultCode = 1,
                                        resultDesc = "Insufficient funds",
                                        mpesaReceiptNumber = null,
                                        transactionDate = null
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "⚠️ Webhook update failed: ${e.message}")
                                }

                                saleId?.toIntOrNull()?.let { id ->
                                    try {
                                        supabaseApiService.updateSaleTransactionStatus(id, "FAILED")
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Sale update failed: ${e.message}")
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Insufficient funds in M-Pesa account"
                                )
                                return@launch
                            }

                            1037 -> {
                                // ⏱️ TIMEOUT
                                Log.d(TAG, "⏱️ Transaction timeout")

                                try {
                                    mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                        checkoutRequestId = checkoutRequestID,
                                        resultCode = 1037,
                                        resultDesc = "Transaction timeout",
                                        mpesaReceiptNumber = null,
                                        transactionDate = null
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "⚠️ Webhook update failed: ${e.message}")
                                }

                                saleId?.toIntOrNull()?.let { id ->
                                    try {
                                        supabaseApiService.updateSaleTransactionStatus(id, "TIMEOUT")
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Sale update failed: ${e.message}")
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Payment request timed out"
                                )
                                return@launch
                            }

                            else -> {
                                // ❌ OTHER ERROR
                                Log.d(TAG, "❌ Payment failed: ${statusResult.resultDesc}")

                                try {
                                    mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                        checkoutRequestId = checkoutRequestID,
                                        resultCode = statusResult.resultCode ?: -1,
                                        resultDesc = statusResult.resultDesc ?: "Failed",
                                        mpesaReceiptNumber = null,
                                        transactionDate = null
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "⚠️ Webhook update failed: ${e.message}")
                                }

                                saleId?.toIntOrNull()?.let { id ->
                                    try {
                                        supabaseApiService.updateSaleTransactionStatus(id, "FAILED")
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Sale update failed: ${e.message}")
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Payment failed: ${statusResult.resultDesc}"
                                )
                                return@launch
                            }
                        }
                    } else {
                        Log.w(TAG, "⚠️ Status check returned success=false")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Status check error (attempt $attemptNum): ${e.message}")
                    // Continue polling even if one check fails
                }
            }

            // Polling timeout after 24 attempts
            Log.d(TAG, "⏰ Polling timeout after 24 attempts")

            saleId?.toIntOrNull()?.let { id ->
                try {
                    supabaseApiService.updateSaleTransactionStatus(id, "TIMEOUT")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Sale timeout update failed: ${e.message}")
                }
            }

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = "Payment confirmation timeout. Please check M-Pesa messages."
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null,
            mpesaReceipt = null,
            validationError = null
        )
    }
}