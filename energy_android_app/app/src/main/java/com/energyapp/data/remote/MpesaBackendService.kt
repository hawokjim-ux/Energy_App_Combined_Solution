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

// ==================== REQUEST/RESPONSE MODELS (No changes needed) ====================

@Serializable
data class MpesaStkRequest(
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
    @SerializedName("fuel_type")
    val fuelType: String? = null,
    @SerializedName("quantity")
    val quantity: Double? = null
)

@Serializable
data class MpesaStkResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("sale_id")
    val saleId: String?,
    @SerializedName("checkout_request_id")
    val checkoutRequestID: String?,
    @SerializedName("merchant_request_id")
    val merchantRequestID: String?
)

@Serializable
data class MpesaStatusResponse(
    @SerializedName("success")
    val success: Boolean,
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
    val transactionDate: String?
)

@Serializable
data class Transaction(
    @SerializedName("id")
    val id: Int,
    @SerializedName("merchantRequestID")
    val merchantRequestID: String,
    @SerializedName("checkoutRequestID")
    val checkoutRequestID: String,
    @SerializedName("resultCode")
    val resultCode: Int,
    @SerializedName("resultDesc")
    val resultDesc: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("mpesaReceiptNumber")
    val mpesaReceiptNumber: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("transactionDate")
    val transactionDate: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("status")
    val status: String
)

@Serializable
data class PaginationInfo(
    @SerializedName("total")
    val total: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("hasMore")
    val hasMore: Boolean
)

@Serializable
data class TransactionListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<Transaction>,
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

// ==================== RETROFIT INTERFACE (Updated for Render PHP) ====================

interface IMpesaBackendApi { // Renamed from ISupabaseMpesaApi for clarity

    /**
     * Initiate M-Pesa STK Push
     * POST /stkpush.php
     * Removed Supabase API key/Authorization headers as PHP is a direct REST service.
     */
    @POST("stkpush.php")
    suspend fun initiateStkPush(
        @Body request: MpesaStkRequest
    ): MpesaStkResponse

    /**
     * Check transaction status
     * GET /check_status.php?checkout_request_id={id}
     * Removed Supabase API key/Authorization headers.
     */
    @GET("check_status.php")
    suspend fun checkStatus(
        @Query("checkout_request_id") checkoutRequestId: String
    ): MpesaStatusResponse
}

// ==================== M-PESA BACKEND SERVICE (Updated Base URL) ====================

/**
 * Service for communicating with the Render PHP Backend for M-Pesa integration
 */
@Singleton
class MpesaBackendService @Inject constructor() {

    private val TAG = "MpesaBackendService"
    private val api: IMpesaBackendApi // Changed interface name

    init {
        Log.d(TAG, "🔧 Initializing MpesaBackendService (Render PHP Backend)...")

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            // *** CRITICAL CHANGE: Using the Render Base URL ***
            .baseUrl(MpesaConfig.RENDER_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(IMpesaBackendApi::class.java)

        Log.d(TAG, "✅ MpesaBackendService initialized")
        Log.d(TAG, "🌐 Render URL: ${MpesaConfig.RENDER_BASE_URL}")
        Log.d(TAG, "🔐 Using PRODUCTION M-Pesa API")
    }

    /**
     * Initiate STK Push via Render PHP Backend
     */
    suspend fun initiateStkPush(request: MpesaStkRequest): MpesaStkResponse {
        return try {
            Log.d(TAG, "🚀 Initiating STK Push via Render PHP Backend...")
            Log.d(TAG, "📱 Phone: ${request.phone}, Amount: ${request.amount}")

            // Removed Supabase Headers
            val response = api.initiateStkPush(request)

            Log.d(TAG, "📤 STK Response: success=${response.success}, message=${response.message}")

            if (response.success) {
                Log.d(TAG, "✅ STK Push initiated successfully")
                Log.d(TAG, "🎫 CheckoutRequestID: ${response.checkoutRequestID}")
                // saleId might be null if PHP doesn't return it on initial push
            } else {
                Log.e(TAG, "❌ STK Push failed: ${response.message}")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ STK Push error: ${e.message}")
            e.printStackTrace()
            MpesaStkResponse(
                success = false,
                message = "Error: ${e.message ?: "Unknown error"}",
                saleId = null,
                checkoutRequestID = null,
                merchantRequestID = null
            )
        }
    }

    /**
     * Check transaction status via Render PHP Backend
     */
    suspend fun checkTransactionStatus(checkoutRequestId: String): MpesaStatusResponse {
        return try {
            Log.d(TAG, "🔍 Checking transaction status...")
            Log.d(TAG, "🎫 CheckoutRequestID: $checkoutRequestId")

            // Removed Supabase Headers
            val response = api.checkStatus(checkoutRequestId)

            Log.d(TAG, "✅ Status response: success=${response.success}, resultCode=${response.resultCode}")

            if (response.resultCode == 0) {
                Log.d(TAG, "💰 Payment successful! Receipt: ${response.mpesaReceiptNumber}")
            } else if (response.resultCode == null) {
                Log.d(TAG, "⏳ Payment still pending...")
            } else {
                Log.d(TAG, "❌ Payment failed with code: ${response.resultCode}")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Status check error: ${e.message}")
            e.printStackTrace()
            MpesaStatusResponse(
                success = false,
                resultCode = null,
                resultDesc = "Error: ${e.message ?: "Unknown error"}",
                checkoutRequestID = checkoutRequestId,
                amount = null,
                mpesaReceiptNumber = null,
                transactionDate = null
            )
        }
    }

    // --- Transaction List Functions (Unchanged, rely on future implementation) ---

    @Suppress("UNUSED_PARAMETER")
    suspend fun getTransactions(
        status: String = "all",
        limit: Int = 50,
        offset: Int = 0
    ): TransactionListResponse {
        Log.w(TAG, "⚠️ getTransactions not yet implemented in backend")
        return TransactionListResponse(
            success = false,
            data = emptyList(),
            pagination = PaginationInfo(0, limit, offset, false)
        )
    }

    suspend fun getSuccessfulTransactions(limit: Int = 50, offset: Int = 0): TransactionListResponse {
        return getTransactions("success", limit, offset)
    }

    suspend fun getPendingTransactions(limit: Int = 50, offset: Int = 0): TransactionListResponse {
        return getTransactions("pending", limit, offset)
    }

    suspend fun getFailedTransactions(limit: Int = 50, offset: Int = 0): TransactionListResponse {
        return getTransactions("failed", limit, offset)
    }
}