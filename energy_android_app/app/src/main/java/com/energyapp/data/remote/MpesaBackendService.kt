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

// ==================== REQUEST/RESPONSE MODELS ====================

@Serializable
data class MpesaStkRequest(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("account")
    val account: String,
    @SerializedName("description")
    val description: String
)

@Serializable
data class MpesaStkResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("MerchantRequestID")
    val merchantRequestID: String?,
    @SerializedName("CheckoutRequestID")
    val checkoutRequestID: String?
)

@Serializable
data class MpesaStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("resultCode")
    val resultCode: Int?,
    @SerializedName("resultDesc")
    val resultDesc: String?,
    @SerializedName("checkoutRequestID")
    val checkoutRequestID: String?,
    @SerializedName("merchantRequestID")
    val merchantRequestID: String?,
    @SerializedName("amount")
    val amount: Double?,
    @SerializedName("mpesaReceiptNumber")
    val mpesaReceiptNumber: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
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
    val status: String // SUCCESS, FAILED, PENDING
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

// ==================== RETROFIT INTERFACE ====================

interface IMpesaBackendApi {

    /**
     * Initiate M-Pesa STK Push
     * POST /stkpush.php
     */
    @POST("stkpush.php")
    suspend fun initiateStkPush(@Body request: MpesaStkRequest): MpesaStkResponse

    /**
     * Check transaction status
     * GET /check_status.php?checkout_request_id={id}
     */
    @GET("check_status.php")
    suspend fun checkStatus(
        @Query("checkout_request_id") checkoutRequestId: String
    ): MpesaStatusResponse

    /**
     * Get all transactions with pagination and filtering
     * GET /transactions_dashboard.php?status=all&limit=50&offset=0
     */
    @GET("transactions_dashboard.php")
    suspend fun getTransactions(
        @Query("status") status: String = "all",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TransactionListResponse

    /**
     * Get only successful transactions
     */
    @GET("transactions_dashboard.php")
    suspend fun getSuccessfulTransactions(
        @Query("status") status: String = "success",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TransactionListResponse

    /**
     * Get only pending transactions
     */
    @GET("transactions_dashboard.php")
    suspend fun getPendingTransactions(
        @Query("status") status: String = "pending",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TransactionListResponse

    /**
     * Get only failed transactions
     */
    @GET("transactions_dashboard.php")
    suspend fun getFailedTransactions(
        @Query("status") status: String = "failed",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TransactionListResponse
}

// ==================== M-PESA BACKEND SERVICE ====================

/**
 * Service for communicating with Railway PHP backend for M-Pesa integration
 *
 * The Railway backend handles:
 * - Communication with live Safaricom M-Pesa API
 * - Secure storage of M-Pesa credentials
 * - STK Push initiation
 * - Transaction status querying
 * - Callback handling
 * - Transaction history and dashboard
 *
 * Android app only needs to call the Railway endpoints!
 */
@Singleton
class MpesaBackendService @Inject constructor() {

    private val TAG = "MpesaBackendService"
    private val api: IMpesaBackendApi

    init {
        Log.d(TAG, "🔧 Initializing MpesaBackendService (Railway Supabase)...")

        // Create logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create OkHttp client with logging and timeouts
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Create Gson converter with lenient parsing
        val gson = GsonBuilder()
            .setLenient()
            .create()

        // Create Retrofit instance using Railway base URL from MpesaConfig
        val retrofit = Retrofit.Builder()
            .baseUrl(MpesaConfig.BACKEND_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        // Create API service
        api = retrofit.create(IMpesaBackendApi::class.java)

        Log.d(TAG, "✅ MpesaBackendService initialized")
        Log.d(TAG, "🌐 Backend URL: ${MpesaConfig.BACKEND_BASE_URL}")
    }

    /**
     * Initiate STK Push via Railway backend
     * Calls stkpush.php with payment details
     *
     * @param request MpesaStkRequest with amount, phone, account, description
     * @return MpesaStkResponse with CheckoutRequestID if successful
     */
    suspend fun initiateStkPush(request: MpesaStkRequest): MpesaStkResponse {
        return try {
            Log.d(TAG, "🚀 Initiating STK Push via Railway...")
            Log.d(TAG, "📱 Phone: ${request.phone}, Amount: ${request.amount}")

            val response = api.initiateStkPush(request)

            Log.d(TAG, "📤 STK Response: success=${response.success}, message=${response.message}")

            if (response.success) {
                Log.d(TAG, "✅ STK Push initiated successfully")
                Log.d(TAG, "🎫 CheckoutRequestID: ${response.checkoutRequestID}")
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
                merchantRequestID = null,
                checkoutRequestID = null
            )
        }
    }

    /**
     * Check transaction status via Railway backend
     * Calls check_status.php to get current transaction state
     *
     * @param checkoutRequestId The CheckoutRequestID from STK Push response
     * @return MpesaStatusResponse with result code and receipt
     */
    suspend fun checkTransactionStatus(checkoutRequestId: String): MpesaStatusResponse {
        return try {
            Log.d(TAG, "🔍 Checking transaction status...")
            Log.d(TAG, "🎫 CheckoutRequestID: $checkoutRequestId")

            val response = api.checkStatus(checkoutRequestId)

            Log.d(TAG, "✅ Status response: success=${response.success}, resultCode=${response.resultCode}")

            if (response.resultCode == 0) {
                Log.d(TAG, "💰 Payment successful! Receipt: ${response.mpesaReceiptNumber}")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Status check error: ${e.message}")
            e.printStackTrace()
            MpesaStatusResponse(
                success = false,
                message = "Error: ${e.message ?: "Unknown error"}",
                resultCode = null,
                resultDesc = null,
                checkoutRequestID = checkoutRequestId,
                merchantRequestID = null,
                amount = null,
                mpesaReceiptNumber = null,
                phoneNumber = null,
                transactionDate = null
            )
        }
    }

    /**
     * Get all transactions from dashboard
     *
     * @param status Filter by status: "all", "success", "pending", "failed"
     * @param limit Number of records per page (default 50)
     * @param offset Starting position for pagination (default 0)
     * @return TransactionListResponse with list of transactions and pagination info
     */
    suspend fun getTransactions(
        status: String = "all",
        limit: Int = 50,
        offset: Int = 0
    ): TransactionListResponse {
        return try {
            Log.d(TAG, "📊 Fetching transactions (status=$status, limit=$limit, offset=$offset)...")

            val response = api.getTransactions(status, limit, offset)

            if (response.success) {
                Log.d(TAG, "✅ Fetched ${response.data.size} transactions")
            } else {
                Log.e(TAG, "❌ Failed to fetch transactions")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "❌ Transaction fetch error: ${e.message}")
            e.printStackTrace()
            TransactionListResponse(
                success = false,
                data = emptyList(),
                pagination = PaginationInfo(0, limit, offset, false)
            )
        }
    }

    /**
     * Get only successful transactions
     */
    suspend fun getSuccessfulTransactions(limit: Int = 50, offset: Int = 0): TransactionListResponse {
        return getTransactions("success", limit, offset)
    }

    /**
     * Get only pending transactions
     */
    suspend fun getPendingTransactions(limit: Int = 50, offset: Int = 0): TransactionListResponse {
        return getTransactions("pending", limit, offset)
    }

    /**
     * Get only failed transactions
     */
    suspend fun getFailedTransactions(limit: Int = 50, offset: Int = 0): TransactionListResponse {
        return getTransactions("failed", limit, offset)
    }
}