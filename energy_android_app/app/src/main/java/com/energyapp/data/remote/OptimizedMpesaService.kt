package com.energyapp.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.energyapp.util.MpesaConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable
import kotlinx.coroutines.delay

/**
 * OPTIMIZED M-PESA BACKEND SERVICE
 * 
 * Performance Features:
 * 1. Aggressive polling (1-2 seconds initially)
 * 2. Connection pooling
 * 3. Reduced timeouts for fast-fail
 * 4. Multi-station support
 * 5. FCM token registration for push notifications
 * 
 * @version 2.0.0
 */

// ==================== REQUEST/RESPONSE MODELS ====================

@Serializable
data class OptimizedMpesaStkRequest(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("account")
    val account: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("pump_id")
    val pumpId: String? = null,
    @SerializedName("shift_id")
    val shiftId: String? = null,
    @SerializedName("station_id")
    val stationId: Int? = null,  // NEW: Multi-station support
    @SerializedName("fcm_token")
    val fcmToken: String? = null,  // NEW: For push notifications
    @SerializedName("fuel_type")
    val fuelType: String? = null,
    @SerializedName("quantity")
    val quantity: Double? = null
)

@Serializable
data class OptimizedMpesaStkResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("sale_id")
    val saleId: String?,
    @SerializedName("checkout_request_id")
    val checkoutRequestID: String?,
    @SerializedName("merchant_request_id")
    val merchantRequestID: String?,
    @SerializedName("station_id")
    val stationId: Int?,
    @SerializedName("processing_time_ms")
    val processingTimeMs: Double?
)

@Serializable
data class OptimizedMpesaStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("resultCode")
    val resultCode: Int?,
    @SerializedName("resultDesc")
    val resultDesc: String?,
    @SerializedName("checkoutRequestID")
    val checkoutRequestID: String?,
    @SerializedName("amount")
    val amount: Double?,
    @SerializedName("mpesaReceiptNumber")
    val mpesaReceiptNumber: String?,
    @SerializedName("transactionDate")
    val transactionDate: String?,
    @SerializedName("stationId")
    val stationId: Int?,
    @SerializedName("stationName")
    val stationName: String?,
    @SerializedName("response_time_ms")
    val responseTimeMs: Double?
)

// ==================== RETROFIT INTERFACE ====================

interface IOptimizedMpesaApi {
    
    @POST("stkpush_optimized.php")
    suspend fun initiateStkPush(
        @Body request: OptimizedMpesaStkRequest
    ): OptimizedMpesaStkResponse

    @GET("check_status_optimized.php")
    suspend fun checkStatus(
        @Query("checkout_request_id") checkoutRequestId: String
    ): OptimizedMpesaStatusResponse
}

// ==================== OPTIMIZED SERVICE ====================

/**
 * Ultra-fast M-Pesa service with aggressive polling and multi-station support
 */
@Singleton
class OptimizedMpesaService @Inject constructor() {

    private val TAG = "OptimizedMpesaService"
    private val api: IOptimizedMpesaApi

    // FCM Token storage (set by the app)
    private var currentFcmToken: String? = null
    
    // Current station context
    private var currentStationId: Int? = null

    init {
        Log.d(TAG, "🚀 Initializing OPTIMIZED MpesaService...")

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OPTIMIZED HTTP Client with aggressive timeouts
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)  // Reduced from 30
            .readTimeout(20, TimeUnit.SECONDS)     // Reduced from 30
            .writeTimeout(15, TimeUnit.SECONDS)    // Reduced from 30
            .retryOnConnectionFailure(true)        // Auto-retry on failure
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(MpesaConfig.RENDER_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(IOptimizedMpesaApi::class.java)

        Log.d(TAG, "✅ OptimizedMpesaService initialized")
        Log.d(TAG, "🌐 Base URL: ${MpesaConfig.RENDER_BASE_URL}")
    }

    /**
     * Set FCM token for push notifications
     */
    fun setFcmToken(token: String?) {
        currentFcmToken = token
        Log.d(TAG, "📱 FCM Token set: ${token?.take(20)}...")
    }

    /**
     * Set current station for multi-station support
     */
    fun setCurrentStation(stationId: Int?) {
        currentStationId = stationId
        Log.d(TAG, "🏪 Current station set: $stationId")
    }

    /**
     * Initiate STK Push with optimized parameters
     */
    suspend fun initiateStkPush(
        phone: String,
        amount: Double,
        account: String,
        description: String = "Fuel Payment",
        userId: String? = null,
        pumpId: String? = null,
        shiftId: String? = null,
        stationId: Int? = null,
        fuelType: String? = null,
        quantity: Double? = null
    ): OptimizedMpesaStkResponse {
        val startTime = System.currentTimeMillis()
        
        return try {
            Log.d(TAG, "🚀 Initiating OPTIMIZED STK Push...")
            Log.d(TAG, "📱 Phone: $phone, Amount: $amount, Station: ${stationId ?: currentStationId}")

            val request = OptimizedMpesaStkRequest(
                amount = amount,
                phone = phone,
                account = account,
                description = description,
                userId = userId,
                pumpId = pumpId,
                shiftId = shiftId,
                stationId = stationId ?: currentStationId,
                fcmToken = currentFcmToken,  // Include FCM token for push notifications
                fuelType = fuelType,
                quantity = quantity
            )

            val response = api.initiateStkPush(request)
            
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "⚡ STK Push completed in ${duration}ms")

            if (response.success) {
                Log.d(TAG, "✅ STK Push initiated successfully")
                Log.d(TAG, "🎫 CheckoutRequestID: ${response.checkoutRequestID}")
                Log.d(TAG, "⏱️ Server processing time: ${response.processingTimeMs}ms")
            } else {
                Log.e(TAG, "❌ STK Push failed: ${response.message}")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ STK Push error: ${e.message}")
            e.printStackTrace()
            OptimizedMpesaStkResponse(
                success = false,
                message = "Network error: ${e.message ?: "Connection failed"}",
                saleId = null,
                checkoutRequestID = null,
                merchantRequestID = null,
                stationId = null,
                processingTimeMs = null
            )
        }
    }

    /**
     * Check transaction status with optimized polling
     */
    suspend fun checkTransactionStatus(checkoutRequestId: String): OptimizedMpesaStatusResponse {
        val startTime = System.currentTimeMillis()
        
        return try {
            Log.d(TAG, "🔍 Checking status for: $checkoutRequestId")

            val response = api.checkStatus(checkoutRequestId)
            
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "⚡ Status check completed in ${duration}ms")

            when (response.resultCode) {
                0 -> Log.d(TAG, "💰 Payment SUCCESS! Receipt: ${response.mpesaReceiptNumber}")
                null -> Log.d(TAG, "⏳ Payment PENDING...")
                else -> Log.d(TAG, "❌ Payment FAILED with code: ${response.resultCode}")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Status check error: ${e.message}")
            OptimizedMpesaStatusResponse(
                success = false,
                message = "Network error: ${e.message}",
                status = "error",
                resultCode = null,
                resultDesc = e.message,
                checkoutRequestID = checkoutRequestId,
                amount = null,
                mpesaReceiptNumber = null,
                transactionDate = null,
                stationId = null,
                stationName = null,
                responseTimeMs = null
            )
        }
    }

    /**
     * AGGRESSIVE POLLING - Poll for payment status with adaptive intervals
     * 
     * Polling Strategy:
     * - First 10 seconds: Poll every 1 second (super fast)
     * - Next 20 seconds: Poll every 2 seconds
     * - After 30 seconds: Poll every 3 seconds
     * - Timeout after 90 seconds
     */
    suspend fun pollForPaymentStatus(
        checkoutRequestId: String,
        onStatusUpdate: (OptimizedMpesaStatusResponse) -> Unit,
        maxWaitSeconds: Int = 90
    ): OptimizedMpesaStatusResponse {
        Log.d(TAG, "⚡ Starting AGGRESSIVE polling for: $checkoutRequestId")
        
        val startTime = System.currentTimeMillis()
        var attempt = 0
        
        while (true) {
            attempt++
            val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
            
            if (elapsedSeconds >= maxWaitSeconds) {
                Log.w(TAG, "⏰ Polling timeout after ${maxWaitSeconds}s")
                return OptimizedMpesaStatusResponse(
                    success = false,
                    message = "Payment confirmation timeout. Check M-Pesa messages.",
                    status = "timeout",
                    resultCode = 1037,
                    resultDesc = "Transaction timeout",
                    checkoutRequestID = checkoutRequestId,
                    amount = null,
                    mpesaReceiptNumber = null,
                    transactionDate = null,
                    stationId = null,
                    stationName = null,
                    responseTimeMs = null
                )
            }
            
            try {
                val response = checkTransactionStatus(checkoutRequestId)
                onStatusUpdate(response)
                
                // Check if we have a final status
                when (response.resultCode) {
                    0 -> {
                        Log.d(TAG, "✅ Payment confirmed after ${elapsedSeconds}s, $attempt attempts")
                        return response
                    }
                    1032 -> {
                        Log.d(TAG, "❌ Payment cancelled after ${elapsedSeconds}s")
                        return response
                    }
                    1, 1037 -> {
                        Log.d(TAG, "❌ Payment failed (code: ${response.resultCode})")
                        return response
                    }
                    null -> {
                        // Still pending, continue polling
                        Log.d(TAG, "⏳ Still pending... (attempt $attempt, ${elapsedSeconds}s elapsed)")
                    }
                    else -> {
                        // Unknown result code, treat as failure
                        Log.w(TAG, "⚠️ Unknown result code: ${response.resultCode}")
                        return response
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Polling attempt $attempt failed: ${e.message}")
                // Continue polling despite errors
            }
            
            // Adaptive delay based on elapsed time
            val delayMs = when {
                elapsedSeconds < 10 -> 1000L   // First 10 seconds: 1 second interval
                elapsedSeconds < 30 -> 2000L   // Next 20 seconds: 2 second interval
                else -> 3000L                   // After 30 seconds: 3 second interval
            }
            
            Log.d(TAG, "⏳ Next poll in ${delayMs}ms...")
            delay(delayMs)
        }
    }

    /**
     * Quick payment - Combines STK push and polling in one call
     * Returns when payment is confirmed or times out
     */
    suspend fun quickPayment(
        phone: String,
        amount: Double,
        account: String,
        description: String = "Fuel Payment",
        userId: String? = null,
        pumpId: String? = null,
        shiftId: String? = null,
        stationId: Int? = null,
        onProgress: (String) -> Unit = {}
    ): PaymentResult {
        val startTime = System.currentTimeMillis()
        
        // Step 1: Initiate STK Push
        onProgress("Sending payment request...")
        
        val stkResponse = initiateStkPush(
            phone = phone,
            amount = amount,
            account = account,
            description = description,
            userId = userId,
            pumpId = pumpId,
            shiftId = shiftId,
            stationId = stationId
        )
        
        if (!stkResponse.success || stkResponse.checkoutRequestID == null) {
            return PaymentResult.Failed(
                message = stkResponse.message,
                checkoutRequestId = null
            )
        }
        
        onProgress("Enter M-Pesa PIN on your phone...")
        
        // Step 2: Poll for result
        val finalStatus = pollForPaymentStatus(
            checkoutRequestId = stkResponse.checkoutRequestID,
            onStatusUpdate = { status ->
                status.message?.let { onProgress(it) }
            }
        )
        
        val totalDuration = System.currentTimeMillis() - startTime
        Log.d(TAG, "💰 Total payment flow completed in ${totalDuration}ms")
        
        return when (finalStatus.resultCode) {
            0 -> PaymentResult.Success(
                checkoutRequestId = stkResponse.checkoutRequestID,
                receiptNumber = finalStatus.mpesaReceiptNumber ?: "",
                amount = amount,
                transactionDate = finalStatus.transactionDate
            )
            1032 -> PaymentResult.Cancelled(
                checkoutRequestId = stkResponse.checkoutRequestID
            )
            else -> PaymentResult.Failed(
                message = finalStatus.resultDesc ?: "Payment failed",
                checkoutRequestId = stkResponse.checkoutRequestID
            )
        }
    }
}

/**
 * Sealed class for payment results
 */
sealed class PaymentResult {
    data class Success(
        val checkoutRequestId: String,
        val receiptNumber: String,
        val amount: Double,
        val transactionDate: String?
    ) : PaymentResult()
    
    data class Failed(
        val message: String,
        val checkoutRequestId: String?
    ) : PaymentResult()
    
    data class Cancelled(
        val checkoutRequestId: String
    ) : PaymentResult()
    
    data class Timeout(
        val checkoutRequestId: String
    ) : PaymentResult()
}
