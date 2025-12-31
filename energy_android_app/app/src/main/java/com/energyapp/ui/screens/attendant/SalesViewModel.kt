package com.energyapp.ui.screens.attendant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.SupabaseApiService
import com.energyapp.data.remote.MpesaBackendService
import com.energyapp.data.remote.MpesaWebhookSupabaseService
import com.energyapp.data.remote.MpesaStkRequest
import com.energyapp.data.remote.SupabaseRealtimeService
import com.energyapp.data.remote.TransactionStatusEvent
import com.energyapp.data.remote.models.FuelTypeResponse
import com.energyapp.util.MpesaConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Data class representing a fuel pump with fuel type, shift, and attendant info
 */
data class Pump(
    val pumpId: Int,
    val pumpName: String,
    val currentShiftId: Int?,
    val fuelTypeId: Int? = null,
    val pricePerLiter: Double = 0.0,
    val fuelTypeName: String = "",
    val shiftName: String = "",          // Shift name (Morning, Evening, Night)
    val attendantId: Int? = null,        // Attendant assigned to this pump
    val attendantName: String = ""       // Attendant name for display
)

/**
 * Payment method enum
 */
enum class PaymentMethod {
    MPESA, CASH
}

/**
 * UI State for Sales Screen - Modern Compact Design
 * Now includes logged-in user info, shift details, and station assignment
 */
data class SalesUiState(
    val pumps: List<Pump> = emptyList(),
    val selectedPump: Pump? = null,
    val fuelTypes: List<FuelTypeResponse> = emptyList(),
    val receiptNumber: String = "",
    val amount: String = "",
    val litersSold: Double = 0.0,
    val pricePerLiter: Double = 0.0,
    val customerMobile: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.MPESA,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val mpesaReceipt: String? = null,
    val checkoutRequestId: String? = null,
    val validationError: String? = null,
    val saleId: String? = null,
    val pollingAttempt: Int = 0,
    val maxPollingAttempts: Int = 20,
    val salesCount: Int = 0,
    // User and Shift info for display
    val loggedInUserName: String = "",   // Logged-in user's full name
    val loggedInUserId: Int = 0,         // Logged-in user's ID
    val currentShiftName: String = "",   // Current shift name (from selected pump)
    // Station Assignment info (from attendant_stations table)
    val assignedStationId: Int = 1,      // User's assigned station ID (default to 1)
    val assignedStationName: String = "", // Station name for display
    val assignedStationCode: String = "", // Station code (e.g., STN-001)
    val hasStationAssignment: Boolean = false // Whether user has a valid assignment
)

/**
 * ViewModel for Sales Screen with M-Pesa and Cash payment support
 * Optimized for faster STK Push and auto-calculation of liters
 */
@HiltViewModel
class SalesViewModel @Inject constructor(
    private val supabaseApiService: SupabaseApiService,
    private val mpesaBackendService: MpesaBackendService,
    private val mpesaWebhookSupabaseService: MpesaWebhookSupabaseService,
    private val supabaseRealtimeService: SupabaseRealtimeService
) : ViewModel() {

    private val TAG = "SalesViewModel"

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private var userId: Int = 0
    
    // Track realtime subscription job for cleanup
    private var realtimeJob: Job? = null
    
    // Track payment start time for performance measurement
    private var paymentStartTime: Long = 0L

    init {
        loadData()
    }

    fun setUserId(id: Int) {
        userId = id
        Log.d(TAG, "User ID set to: $userId")
        // Reload data when user is set to get proper user info
        loadData()
    }

    /**
     * Set user info directly (from login)
     */
    fun setUserInfo(id: Int, name: String) {
        userId = id
        _uiState.value = _uiState.value.copy(
            loggedInUserId = id,
            loggedInUserName = name
        )
        Log.d(TAG, "User info set: $name (ID: $id)")
        loadData()
    }

    /**
     * Load pumps, fuel types, shifts, user station assignment together
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load all base data in parallel
                val fuelTypesResult = supabaseApiService.getFuelTypes()
                val fuelTypes = fuelTypesResult.getOrNull() ?: emptyList()
                
                val shiftsResult = supabaseApiService.getShifts()
                val shifts = shiftsResult.getOrNull() ?: emptyList()
                
                // Load user's station assignment (from attendant_stations table)
                var assignedStationId = 1 // Default fallback
                var assignedStationName = ""
                var assignedStationCode = ""
                var hasAssignment = false
                
                if (userId > 0) {
                    Log.d(TAG, "🔗 Loading station assignment for user: $userId")
                    val assignmentResult = supabaseApiService.getUserStationAssignment(userId)
                    val assignment = assignmentResult.getOrNull()
                    if (assignment != null) {
                        assignedStationId = assignment.stationId
                        assignedStationName = assignment.stationName
                        assignedStationCode = assignment.stationCode
                        hasAssignment = true
                        Log.d(TAG, "✅ User assigned to station: ${assignment.stationName} (ID: ${assignment.stationId})")
                    } else {
                        Log.w(TAG, "⚠️ No station assignment found for user $userId, using default station 1")
                    }
                }
                
                // Load pumps
                val pumpsResult = supabaseApiService.getPumps()
                if (pumpsResult.isSuccess) {
                    val pumpResponses = pumpsResult.getOrThrow()
                    val openShiftsResult = supabaseApiService.getOpenShifts()
                    val openShifts = openShiftsResult.getOrNull() ?: emptyList()

                    // Build pump list with full info - filter by assigned station
                    val allPumps = pumpResponses.mapNotNull { pumpResponse ->
                        // Find open shift for this pump
                        val openShift = openShifts.find { it.pumpId == pumpResponse.pumpId && !it.isClosed }
                        if (openShift != null) {
                            // Get fuel type info
                            val fuelType = fuelTypes.find { it.fuelTypeId == pumpResponse.fuelTypeId }
                            // Get shift definition name
                            val shiftDef = shifts.find { it.shiftId == openShift.shiftId }
                            
                            Pump(
                                pumpId = pumpResponse.pumpId,
                                pumpName = pumpResponse.pumpName,
                                currentShiftId = openShift.pumpShiftId,
                                fuelTypeId = pumpResponse.fuelTypeId,
                                pricePerLiter = fuelType?.pricePerLiter ?: 0.0,
                                fuelTypeName = fuelType?.fuelName ?: "",
                                shiftName = shiftDef?.shiftName ?: "Shift",
                                attendantId = openShift.attendantId,
                                attendantName = "" // Will be set from logged-in user
                            )
                        } else null
                    }
                    
                    // Filter pumps by assigned station if user has assignment
                    // For now, show all pumps (station filter can be added when pumps have station_id)
                    val pumps = if (hasAssignment) {
                        // Filter pumps by station_id if available in pump response
                        allPumps.filter { pump -> 
                            // Check if pump belongs to assigned station
                            val pumpResponse = pumpResponses.find { it.pumpId == pump.pumpId }
                            pumpResponse?.stationId == null || pumpResponse.stationId == assignedStationId
                        }
                    } else {
                        allPumps
                    }

                    // Get current sales count for receipt number
                    val salesResult = supabaseApiService.getAllSales()
                    val salesCount = salesResult.getOrNull()?.size ?: 0

                    // Get current shift name from first pump (or selected)
                    val currentShift = pumps.firstOrNull()?.shiftName ?: ""

                    _uiState.value = _uiState.value.copy(
                        pumps = pumps,
                        selectedPump = pumps.firstOrNull(),
                        fuelTypes = fuelTypes,
                        pricePerLiter = pumps.firstOrNull()?.pricePerLiter ?: fuelTypes.firstOrNull()?.pricePerLiter ?: 0.0,
                        salesCount = salesCount,
                        currentShiftName = currentShift,
                        // Station assignment
                        assignedStationId = assignedStationId,
                        assignedStationName = assignedStationName,
                        assignedStationCode = assignedStationCode,
                        hasStationAssignment = hasAssignment
                    )
                    
                    generateReceiptNumber(salesCount)

                    Log.d(TAG, "✅ Loaded ${pumps.size} pumps, ${fuelTypes.size} fuel types, ${shifts.size} shifts")
                    Log.d(TAG, "✅ Station: $assignedStationName, Shift: $currentShift, User: ${_uiState.value.loggedInUserName}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to load data: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun loadPumps() = loadData()

    /**
     * Generate receipt number like RCP-00001
     */
    private fun generateReceiptNumber(count: Int) {
        val nextNum = count + 1
        val paddedNum = nextNum.toString().padStart(5, '0')
        val receiptNumber = "RCP-$paddedNum"
        _uiState.value = _uiState.value.copy(receiptNumber = receiptNumber)
        Log.d(TAG, "📝 Generated receipt: $receiptNumber")
    }

    fun selectPump(pump: Pump) {
        _uiState.value = _uiState.value.copy(
            selectedPump = pump, 
            pricePerLiter = pump.pricePerLiter,
            currentShiftName = pump.shiftName,
            error = null
        )
        // Recalculate liters with new price
        calculateLiters(_uiState.value.amount)
        Log.d(TAG, "Pump selected: ${pump.pumpName}, Shift: ${pump.shiftName}, Price: ${pump.pricePerLiter}/L")
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount, validationError = null, error = null)
        calculateLiters(amount)
    }

    /**
     * Calculate liters sold based on amount and price per liter
     */
    private fun calculateLiters(amountStr: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val pricePerLiter = _uiState.value.pricePerLiter
        val liters = if (pricePerLiter > 0) amount / pricePerLiter else 0.0
        _uiState.value = _uiState.value.copy(litersSold = liters)
    }

    fun onCustomerMobileChange(mobile: String) {
        _uiState.value = _uiState.value.copy(customerMobile = mobile, validationError = null, error = null)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(paymentMethod = method, error = null)
        Log.d(TAG, "Payment method: $method")
    }

    fun clearForm() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            amount = "",
            litersSold = 0.0,
            customerMobile = "",
            error = null,
            validationError = null,
            successMessage = null,
            mpesaReceipt = null,
            checkoutRequestId = null,
            selectedPump = currentState.pumps.firstOrNull(),
            paymentMethod = PaymentMethod.MPESA,
            pollingAttempt = 0
        )
        generateReceiptNumber(_uiState.value.salesCount + 1)
        Log.d(TAG, "Form cleared")
    }

    /**
     * Validate all inputs before payment
     */
    private fun validateInputs(): String? {
        val pump = _uiState.value.selectedPump
        val amountStr = _uiState.value.amount.trim()
        val mobile = _uiState.value.customerMobile.trim()
        val paymentMethod = _uiState.value.paymentMethod

        return when {
            pump == null -> "No pump with open shift available"
            pump.currentShiftId == null -> "No active shift for selected pump"
            amountStr.isEmpty() -> "Please enter an amount"
            amountStr.toDoubleOrNull() == null -> "Please enter a valid amount"
            amountStr.toDouble() <= 0 -> "Amount must be greater than 0"
            amountStr.toDouble() > 150000 -> "Amount cannot exceed KES 150,000"
            paymentMethod == PaymentMethod.MPESA && mobile.isEmpty() -> "Please enter customer mobile number for M-Pesa"
            paymentMethod == PaymentMethod.MPESA && !MpesaConfig.isValidKenyanPhone(mobile) -> {
                "Invalid mobile format. Use 07XXXXXXXX or 254XXXXXXXXX"
            }
            else -> null
        }
    }

    /**
     * Process payment - M-Pesa STK Push or Cash
     */
    fun processPayment() {
        val paymentMethod = _uiState.value.paymentMethod
        if (paymentMethod == PaymentMethod.MPESA) {
            initiateMpesaPayment()
        } else {
            processCashPayment()
        }
    }

    /**
     * Process Cash Payment - Direct save without M-Pesa
     */
    private fun processCashPayment() {
        Log.d(TAG, "💵 Processing cash payment...")

        _uiState.value = _uiState.value.copy(
            error = null,
            validationError = null,
            successMessage = null
        )

        val validationError = validateInputs()
        if (validationError != null) {
            Log.w(TAG, "⚠️ Validation failed: $validationError")
            _uiState.value = _uiState.value.copy(validationError = validationError)
            return
        }

        val pump = _uiState.value.selectedPump ?: return
        val amount = _uiState.value.amount.trim().toDoubleOrNull() ?: return
        val receiptNumber = _uiState.value.receiptNumber
        val litersSold = _uiState.value.litersSold
        val pricePerLiter = _uiState.value.pricePerLiter

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)

            try {
                val shiftId = pump.currentShiftId ?: 0
                // Use RCP-XXXXX format matching web app
                val saleIdNo = receiptNumber
                val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())

                // Create sale with ALL fields matching web app
                val result = supabaseApiService.createSale(
                    com.energyapp.data.remote.models.CreateSaleRequest(
                        saleIdNo = saleIdNo,
                        pumpShiftId = shiftId,
                        pumpId = pump.pumpId,
                        attendantId = userId,
                        amount = amount,
                        customerMobileNo = _uiState.value.customerMobile.ifEmpty { "CASH" },
                        transactionStatus = "CASH",  // CASH instead of SUCCESS (matching web app)
                        stationId = _uiState.value.assignedStationId,  // Use assigned station from DB
                        fuelTypeId = pump.fuelTypeId,
                        litersSold = litersSold,
                        pricePerLiter = pricePerLiter,
                        totalAmount = amount,  // Same as amount
                        paymentMethod = "cash",  // Explicit payment method
                        saleTime = currentTime
                    )
                )

                if (result.isSuccess) {
                    Log.d(TAG, "✅ Cash sale recorded successfully: $saleIdNo")
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "💵 Cash Payment Recorded!\nReceipt: $saleIdNo\nAmount: KES ${String.format("%,.2f", amount)}\nLiters: ${String.format("%.2f", litersSold)} L",
                        mpesaReceipt = saleIdNo,
                        salesCount = _uiState.value.salesCount + 1
                    )
                } else {
                    throw Exception("Failed to record sale")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Cash payment error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to record cash payment"
                )
            }
        }
    }

    /**
     * Initiate M-Pesa STK Push - Optimized for speed
     */
    fun initiateMpesaPayment() {
        Log.d(TAG, "🚀 Initiating M-Pesa payment...")

        _uiState.value = _uiState.value.copy(
            error = null,
            validationError = null,
            successMessage = null,
            pollingAttempt = 0
        )

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
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            // Start tracking payment time
            paymentStartTime = System.currentTimeMillis()

            try {
                val shiftId = pump.currentShiftId ?: 0
                val formattedPhone = MpesaConfig.formatPhoneNumber(mobile)

                Log.d(TAG, "📞 Formatted phone: $formattedPhone")
                Log.d(TAG, "💰 Amount: KES $amount")
                Log.d(TAG, "⛽ Pump: ${pump.pumpName} (ID: ${pump.pumpId})")

                val shortAccountReference = "P${pump.pumpId}".take(4)

                // Call STK Push - Optimized request
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
                            shiftId = shiftId.toString(),
                            stationId = _uiState.value.assignedStationId.toString()  // Pass assigned station
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Network error: ${e.message}")
                    throw Exception("Failed to reach payment service: ${e.message}")
                }

                Log.d(TAG, "📥 STK Response received: success=${stkResponse.success}")

                if (!stkResponse.success) {
                    Log.e(TAG, "❌ STK Push rejected: ${stkResponse.message}")
                    throw Exception(stkResponse.message)
                }

                val checkoutRequestID = stkResponse.checkoutRequestID
                if (checkoutRequestID.isNullOrEmpty()) {
                    Log.e(TAG, "❌ FATAL: No CheckoutRequestID in response")
                    throw Exception("Payment request incomplete - no checkout ID received. Please try again.")
                }

                val saleId = stkResponse.saleId
                Log.d(TAG, "✅ STK Push successful!")
                Log.d(TAG, "🎫 CheckoutRequestID: $checkoutRequestID")

                _uiState.value = _uiState.value.copy(
                    isProcessing = true,
                    checkoutRequestId = checkoutRequestID,
                    saleId = saleId,
                    error = null
                )

                // 🔄 Use reliable polling for transaction status
                // (Realtime requires additional server configuration)
                pollTransactionStatus(checkoutRequestID, saleId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Payment initiation error: ${e.message}")
                e.printStackTrace()

                // Handle "Similar Transaction" error gracefully
                val errorMessage = e.message ?: "Payment request failed. Please try again."
                val userFriendlyError = when {
                    errorMessage.contains("similar", ignoreCase = true) ||
                    errorMessage.contains("duplicate", ignoreCase = true) ||
                    errorMessage.contains("already", ignoreCase = true) -> {
                        "⚠️ Similar transaction detected!\n\n" +
                        "This happens when the same amount is sent to the same number within 2 minutes.\n\n" +
                        "Solutions:\n" +
                        "1️⃣ Wait 2 minutes and try again\n" +
                        "2️⃣ Add KES 1 to the amount (e.g., 1001 instead of 1000)\n" +
                        "3️⃣ Use Cash payment instead"
                    }
                    errorMessage.contains("timeout", ignoreCase = true) ||
                    errorMessage.contains("timed out", ignoreCase = true) -> {
                        "⏱️ Request timed out. Please check your internet connection and try again."
                    }
                    errorMessage.contains("network", ignoreCase = true) ||
                    errorMessage.contains("connection", ignoreCase = true) -> {
                        "📶 Network error. Please check your internet connection and try again."
                    }
                    else -> errorMessage
                }

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = userFriendlyError
                )
            }
        }
    }

    /**
     * Poll for transaction status - RELIABLE polling method
     */
    private fun pollTransactionStatus(checkoutRequestID: String, saleId: String?) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Starting status polling for: $checkoutRequestID")

            // Polling: 20 attempts × 3 seconds = 1 minute timeout
            val maxAttempts = 20
            
            repeat(maxAttempts) { attempt ->
                delay(3000) // 3 second intervals

                val attemptNum = attempt + 1
                _uiState.value = _uiState.value.copy(pollingAttempt = attemptNum)
                Log.d(TAG, "🔍 Polling attempt $attemptNum/$maxAttempts...")

                try {
                    val statusResult = mpesaBackendService.checkTransactionStatus(checkoutRequestID)

                    Log.d(TAG, "📊 Status: success=${statusResult.success}, resultCode=${statusResult.resultCode}")

                    if (statusResult.success) {
                        when (statusResult.resultCode) {
                            null -> {
                                Log.d(TAG, "⏳ Payment pending (attempt $attemptNum/$maxAttempts)")
                            }

                            0 -> {
                                // ✅ SUCCESS
                                Log.d(TAG, "✅ PAYMENT SUCCESSFUL!")
                                Log.d(TAG, "💳 Receipt: ${statusResult.mpesaReceiptNumber}")

                                saleId?.toIntOrNull()?.let { id ->
                                    try {
                                        supabaseApiService.updateSaleTransactionStatus(
                                            id, "SUCCESS", statusResult.mpesaReceiptNumber
                                        )
                                    } catch (e: Exception) {
                                        Log.w(TAG, "⚠️ Sale update failed: ${e.message}")
                                    }
                                }

                                val amount = _uiState.value.amount.toDoubleOrNull() ?: 0.0
                                val elapsedSeconds = (System.currentTimeMillis() - paymentStartTime) / 1000.0
                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    successMessage = "✅ M-Pesa Payment Successful!\nAmount: KES ${String.format("%,.2f", amount)}\nLiters: ${String.format("%.2f", _uiState.value.litersSold)} L\nReceipt: ${statusResult.mpesaReceiptNumber}\n⚡ Completed in ${String.format("%.1f", elapsedSeconds)} seconds",
                                    mpesaReceipt = statusResult.mpesaReceiptNumber,
                                    error = null,
                                    salesCount = _uiState.value.salesCount + 1
                                )
                                return@launch
                            }

                            1032 -> {
                                Log.d(TAG, "❌ Payment cancelled by user")
                                saleId?.toIntOrNull()?.let { id ->
                                    try { supabaseApiService.updateSaleTransactionStatus(id, "CANCELLED") } 
                                    catch (e: Exception) { Log.w(TAG, "⚠️ Sale update failed") }
                                }
                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Payment was cancelled by user"
                                )
                                return@launch
                            }

                            1 -> {
                                Log.d(TAG, "❌ Insufficient funds")
                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Insufficient funds in M-Pesa account"
                                )
                                return@launch
                            }

                            1037 -> {
                                Log.d(TAG, "⏱️ Transaction timeout")
                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Payment request timed out"
                                )
                                return@launch
                            }

                            else -> {
                                Log.d(TAG, "❌ Payment failed: ${statusResult.resultDesc}")
                                _uiState.value = _uiState.value.copy(
                                    isProcessing = false,
                                    error = "Payment failed: ${statusResult.resultDesc}"
                                )
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Status check error (attempt $attemptNum): ${e.message}")
                }
            }

            // Polling timeout
            Log.d(TAG, "⏰ Polling timeout after $maxAttempts attempts")
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = "Payment confirmation timeout. Please check M-Pesa messages."
            )
        }
    }

    /**
     * ⚡ REALTIME: Subscribe to instant transaction updates via Supabase Realtime
     * (Currently disabled - requires additional server configuration)
     * 
     * This replaces the old polling method and provides:
     * - INSTANT payment confirmation (< 1 second)
     * - Lower battery usage (no repeated API calls)
     * - Better user experience
     */
    private fun subscribeToRealtimeUpdates(checkoutRequestID: String, saleId: String?) {
        Log.d(TAG, "⚡ Starting REALTIME subscription for: $checkoutRequestID")
        
        // Cancel any existing subscription
        realtimeJob?.cancel()
        
        // Subscribe to realtime updates
        realtimeJob = supabaseRealtimeService.subscribeToTransactionStatus(
            checkoutRequestId = checkoutRequestID,
            scope = viewModelScope
        ) { event ->
            Log.d(TAG, "📥 Realtime event received: $event")
            
            when (event) {
                is TransactionStatusEvent.Completed -> {
                    Log.d(TAG, "✅ INSTANT PAYMENT CONFIRMATION!")
                    Log.d(TAG, "💳 Receipt: ${event.receiptNumber}")
                    
                    // Update sale status in database
                    saleId?.toIntOrNull()?.let { id ->
                        viewModelScope.launch {
                            try {
                                supabaseApiService.updateSaleTransactionStatus(
                                    id, "SUCCESS", event.receiptNumber
                                )
                            } catch (e: Exception) {
                                Log.w(TAG, "⚠️ Sale update failed: ${e.message}")
                            }
                        }
                    }
                    
                    val amount = _uiState.value.amount.toDoubleOrNull() ?: event.amount
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "✅ M-Pesa Payment Successful!\n" +
                            "Amount: KES ${String.format("%,.2f", amount)}\n" +
                            "Liters: ${String.format("%.2f", _uiState.value.litersSold)} L\n" +
                            "Receipt: ${event.receiptNumber}",
                        mpesaReceipt = event.receiptNumber,
                        error = null,
                        salesCount = _uiState.value.salesCount + 1
                    )
                }
                
                is TransactionStatusEvent.Cancelled -> {
                    Log.d(TAG, "❌ Payment cancelled")
                    saleId?.toIntOrNull()?.let { id ->
                        viewModelScope.launch {
                            try { 
                                supabaseApiService.updateSaleTransactionStatus(id, "CANCELLED") 
                            } catch (e: Exception) { 
                                Log.w(TAG, "⚠️ Sale update failed") 
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Payment was cancelled by user"
                    )
                }
                
                is TransactionStatusEvent.Failed -> {
                    Log.d(TAG, "❌ Payment failed: ${event.reason}")
                    
                    val errorMessage = when (event.resultCode) {
                        1 -> "Insufficient funds in M-Pesa account"
                        1037 -> "Payment request timed out"
                        else -> "Payment failed: ${event.reason}"
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = errorMessage
                    )
                }
                
                is TransactionStatusEvent.Timeout -> {
                    Log.d(TAG, "⏰ Realtime timeout - falling back to final check")
                    // Do one final status check
                    viewModelScope.launch {
                        performFinalStatusCheck(checkoutRequestID, saleId)
                    }
                }
                
                is TransactionStatusEvent.Error -> {
                    Log.e(TAG, "❌ Realtime error: ${event.message}")
                    // Fall back to polling on realtime error
                    fallbackToPollling(checkoutRequestID, saleId)
                }
                
                is TransactionStatusEvent.Pending -> {
                    // Still waiting, update UI
                    val currentAttempt = _uiState.value.pollingAttempt + 1
                    _uiState.value = _uiState.value.copy(pollingAttempt = currentAttempt)
                    Log.d(TAG, "⏳ Payment still pending...")
                }
            }
        }
    }
    
    /**
     * Perform a final status check when realtime times out
     */
    private suspend fun performFinalStatusCheck(checkoutRequestID: String, saleId: String?) {
        try {
            val statusResult = mpesaBackendService.checkTransactionStatus(checkoutRequestID)
            
            when (statusResult.resultCode) {
                0 -> {
                    Log.d(TAG, "✅ Final check: Payment successful!")
                    saleId?.toIntOrNull()?.let { id ->
                        try {
                            supabaseApiService.updateSaleTransactionStatus(
                                id, "SUCCESS", statusResult.mpesaReceiptNumber
                            )
                        } catch (e: Exception) { }
                    }
                    val amount = _uiState.value.amount.toDoubleOrNull() ?: 0.0
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "✅ M-Pesa Payment Successful!\nReceipt: ${statusResult.mpesaReceiptNumber}",
                        mpesaReceipt = statusResult.mpesaReceiptNumber,
                        salesCount = _uiState.value.salesCount + 1
                    )
                }
                1032 -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Payment was cancelled by user"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Payment confirmation timeout. Please check M-Pesa messages."
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = "Payment confirmation timeout. Please check M-Pesa messages."
            )
        }
    }
    
    /**
     * Fallback to polling if realtime connection fails
     */
    private fun fallbackToPollling(checkoutRequestID: String, saleId: String?) {
        Log.d(TAG, "🔄 Falling back to polling...")
        
        viewModelScope.launch {
            // Simpler polling with fewer attempts since realtime failed
            repeat(10) { attempt ->
                delay(3000)
                
                try {
                    val statusResult = mpesaBackendService.checkTransactionStatus(checkoutRequestID)
                    
                    when (statusResult.resultCode) {
                        0 -> {
                            Log.d(TAG, "✅ Polling: Payment successful!")
                            saleId?.toIntOrNull()?.let { id ->
                                try {
                                    supabaseApiService.updateSaleTransactionStatus(
                                        id, "SUCCESS", statusResult.mpesaReceiptNumber
                                    )
                                } catch (e: Exception) { }
                            }
                            val amount = _uiState.value.amount.toDoubleOrNull() ?: 0.0
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                successMessage = "✅ M-Pesa Payment Successful!\nReceipt: ${statusResult.mpesaReceiptNumber}",
                                mpesaReceipt = statusResult.mpesaReceiptNumber,
                                salesCount = _uiState.value.salesCount + 1
                            )
                            return@launch
                        }
                        1032, 1, 1037 -> {
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                error = statusResult.resultDesc ?: "Payment failed"
                            )
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Polling error: ${e.message}")
                }
            }
            
            // Timeout
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = "Payment confirmation timeout. Please check M-Pesa messages."
            )
        }
    }
    
    /**
     * Cleanup when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
        supabaseRealtimeService.disconnect()
        Log.d(TAG, "🧹 ViewModel cleared, realtime disconnected")
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null,
            mpesaReceipt = null,
            validationError = null
        )
    }

    /**
     * Refresh data
     */
    fun refresh() {
        loadData()
    }
}