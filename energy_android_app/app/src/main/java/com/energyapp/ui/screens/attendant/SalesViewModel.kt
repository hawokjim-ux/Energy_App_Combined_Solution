package com.energyapp.ui.screens.attendant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.MpesaBackendService
import com.energyapp.data.remote.MpesaWebhookSupabaseService
import com.energyapp.data.remote.models.CreateSaleRequest
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
    val validationError: String? = null
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
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load pumps: ${e.message}"
                )
            }
        }
    }

    /**
     * The `saleIdNo` is used for your internal Supabase records,
     * where a long ID is safe and necessary for uniqueness.
     */
    private fun generateSaleId() {
        val saleId = "SALE-${System.currentTimeMillis()}"
        _uiState.value = _uiState.value.copy(saleIdNo = saleId)
    }

    fun selectPump(pump: Pump) {
        _uiState.value = _uiState.value.copy(selectedPump = pump, error = null)
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount, validationError = null)
    }

    fun onCustomerMobileChange(mobile: String) {
        _uiState.value = _uiState.value.copy(customerMobile = mobile, validationError = null)
    }

    fun clearForm() {
        _uiState.value = _uiState.value.copy(
            amount = "",
            customerMobile = "",
            error = null,
            validationError = null,
            selectedPump = _uiState.value.pumps.firstOrNull()
        )
        generateSaleId()
    }

    /**
     * Validate all inputs before payment
     */
    private fun validateInputs(): String? {
        val pump = _uiState.value.selectedPump
        val amount = _uiState.value.amount.toDoubleOrNull()
        val mobile = _uiState.value.customerMobile.trim()

        return when {
            pump == null -> "No pump with open shift available"
            amount == null || amount <= 0 -> "Please enter a valid amount (greater than 0)"
            mobile.isEmpty() -> "Please enter customer mobile number"
            // Assuming MpesaConfig.isValidKenyanPhone handles validation logic
            !MpesaConfig.isValidKenyanPhone(mobile) -> "Invalid mobile format. Use 07XXXXXXXX or 2547XXXXXXXX"
            pump.currentShiftId == null -> "No active shift for selected pump"
            else -> null
        }
    }

    fun initiateMpesaPayment() {
        val validationError = validateInputs()
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(validationError = validationError)
            return
        }

        val pump = _uiState.value.selectedPump ?: return
        val amount = _uiState.value.amount.toDoubleOrNull() ?: return
        val mobile = _uiState.value.customerMobile.trim()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

            try {
                val shiftId = pump.currentShiftId ?: 0
                val saleRequest = CreateSaleRequest(
                    saleIdNo = _uiState.value.saleIdNo,
                    pumpShiftId = shiftId,
                    pumpId = pump.pumpId,
                    attendantId = userId,
                    amount = amount,
                    customerMobileNo = mobile,
                    transactionStatus = "PENDING",
                    checkoutRequestId = null
                )

                val saleResult = supabaseApiService.createSale(saleRequest)
                if (saleResult.isFailure) {
                    throw Exception("Failed to create sale: ${saleResult.exceptionOrNull()?.message}")
                }

                val sale = saleResult.getOrThrow()
                val saleId = sale.saleId

                val formattedPhone = MpesaConfig.formatPhoneNumber(mobile)

                // *** FIX APPLIED HERE: Shorten Account Reference to max 4 chars for M-Pesa compliance ***
                // Use "P" prefix + Pump ID. e.g., P1, P12, P123
                val shortAccountReference = "P${pump.pumpId}".take(4)

                val stkResponse = try {
                    mpesaBackendService.initiateStkPush(
                        MpesaStkRequest(
                            amount = amount,
                            phone = formattedPhone,
                            // Use the short, compliant AccountReference for the M-Pesa API
                            account = shortAccountReference,
                            description = "Fuel Payment - Pump ${pump.pumpName}"
                        )
                    )
                } catch (e: Exception) {
                    supabaseApiService.updateSaleTransactionStatus(saleId, "FAILED")
                    throw Exception("Failed to reach M-Pesa service: ${e.message}")
                }

                if (!stkResponse.success) {
                    supabaseApiService.updateSaleTransactionStatus(saleId, "FAILED")
                    throw Exception("M-Pesa rejected request: ${stkResponse.message}")
                }

                val checkoutRequestID = stkResponse.checkoutRequestID
                if (checkoutRequestID.isNullOrEmpty()) {
                    supabaseApiService.updateSaleTransactionStatus(saleId, "FAILED")
                    throw Exception("No checkout request ID received from M-Pesa")
                }

                Log.d(TAG, "💾 Saving M-Pesa response to mpesa-webhook project...")
                val mpesaSaveResult = mpesaWebhookSupabaseService.saveStkPushResponse(
                    checkoutRequestId = checkoutRequestID,
                    merchantRequestId = stkResponse.merchantRequestID,
                    amount = amount,
                    phoneNumber = formattedPhone,
                    // Use the original long unique ID for your internal Supabase records
                    accountReference = _uiState.value.saleIdNo
                )

                if (mpesaSaveResult.isFailure) {
                    Log.e(TAG, "⚠️ Warning: M-Pesa data not saved to webhook project")
                } else {
                    Log.d(TAG, "✅ M-Pesa data saved successfully!")
                }

                supabaseApiService.updateSaleTransactionStatus(
                    saleId = saleId,
                    status = "PENDING"
                )

                // ✅ DON'T SHOW SUCCESS YET - Keep processing = true
                _uiState.value = _uiState.value.copy(
                    isProcessing = true,
                    successMessage = null,
                    checkoutRequestId = checkoutRequestID
                )

                pollTransactionStatus(checkoutRequestID, saleId)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Poll for transaction status - ONLY show success when resultCode == 0
     */
    private fun pollTransactionStatus(checkoutRequestID: String, saleId: Int) {
        viewModelScope.launch {
            // Poll for about 2 minutes (24 attempts * 5 seconds)
            repeat(24) { attempt ->
                delay(5000)
                Log.d(TAG, "🔍 Polling status (attempt ${attempt + 1}/24)...")

                val statusResult = mpesaBackendService.checkTransactionStatus(checkoutRequestID)

                if (statusResult.success) {
                    // Handle result code
                    when {
                        statusResult.resultCode == null -> {
                            // Still pending - continue polling
                            Log.d(TAG, "⏳ Still pending, continuing to poll...")
                        }
                        statusResult.resultCode == 0 -> {
                            // ✅ SUCCESS
                            Log.d(TAG, "✅ Payment successful!")

                            supabaseApiService.updateSaleTransactionStatus(
                                saleId,
                                "SUCCESS",
                                statusResult.mpesaReceiptNumber
                            )

                            mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                checkoutRequestId = checkoutRequestID,
                                resultCode = 0,
                                resultDesc = statusResult.resultDesc ?: "Success",
                                mpesaReceiptNumber = statusResult.mpesaReceiptNumber,
                                transactionDate = statusResult.transactionDate
                            )

                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                successMessage = "Payment Successful!",
                                mpesaReceipt = statusResult.mpesaReceiptNumber,
                                checkoutRequestId = checkoutRequestID,
                                amount = "",
                                customerMobile = ""
                            )
                            generateSaleId()
                            return@launch
                        }
                        statusResult.resultCode == 1032 -> {
                            // CANCELLED
                            Log.d(TAG, "❌ User cancelled")
                            supabaseApiService.updateSaleTransactionStatus(saleId, "CANCELLED")
                            mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                checkoutRequestId = checkoutRequestID,
                                resultCode = 1032,
                                resultDesc = "Cancelled by user",
                                mpesaReceiptNumber = null,
                                transactionDate = null
                            )
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = "Payment cancelled by user"
                            )
                            return@launch
                        }
                        statusResult.resultCode == 1 -> {
                            // INSUFFICIENT FUNDS
                            Log.d(TAG, "❌ Insufficient funds")
                            supabaseApiService.updateSaleTransactionStatus(saleId, "FAILED")
                            mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                checkoutRequestId = checkoutRequestID,
                                resultCode = 1,
                                resultDesc = "Insufficient funds",
                                mpesaReceiptNumber = null,
                                transactionDate = null
                            )
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = "Insufficient funds in M-Pesa account"
                            )
                            return@launch
                        }
                        statusResult.resultCode == 1037 -> {
                            // TIMEOUT
                            Log.d(TAG, "⏱️ Transaction timeout")
                            supabaseApiService.updateSaleTransactionStatus(saleId, "TIMEOUT")
                            mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                checkoutRequestId = checkoutRequestID,
                                resultCode = 1037,
                                resultDesc = "Transaction timeout",
                                mpesaReceiptNumber = null,
                                transactionDate = null
                            )
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = "Payment timeout"
                            )
                            return@launch
                        }
                        else -> {
                            // Other error
                            Log.d(TAG, "❌ Transaction failed: ${statusResult.resultDesc}")
                            supabaseApiService.updateSaleTransactionStatus(saleId, "FAILED")
                            mpesaWebhookSupabaseService.updateTransactionWithWebhookResponse(
                                checkoutRequestId = checkoutRequestID,
                                resultCode = statusResult.resultCode ?: -1,
                                resultDesc = statusResult.resultDesc ?: "Failed",
                                mpesaReceiptNumber = null,
                                transactionDate = null
                            )
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = "Payment failed: ${statusResult.resultDesc}"
                            )
                            return@launch
                        }
                    }
                }
            }

            // Polling timeout
            Log.d(TAG, "⏱️ Polling timeout")
            supabaseApiService.updateSaleTransactionStatus(saleId, "TIMEOUT")
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = "Payment confirmation timeout"
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null,
            mpesaReceipt = null,
            checkoutRequestId = null,
            validationError = null
        )
    }
}