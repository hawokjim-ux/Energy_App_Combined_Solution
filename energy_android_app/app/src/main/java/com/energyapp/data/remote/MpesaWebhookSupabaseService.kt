package com.energyapp.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton

// ==================== RETROFIT INTERFACE FOR MPESA WEBHOOK PROJECT ====================

interface MpesaWebhookApi {

    @POST("mpesa_transactions")
    suspend fun saveMpesaTransaction(
        @Body transaction: Map<String, @JvmSuppressWildcards Any>
    ): List<MpesaTransactionRecord>

    @GET("mpesa_transactions")
    suspend fun getAllMpesaTransactions(
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 100
    ): List<MpesaTransactionRecord>

    // ✅ FIXED: Changed query parameter from "id" to "checkout_request_id"
    @PATCH("mpesa_transactions")
    suspend fun updateMpesaTransaction(
        @Query("checkout_request_id") checkoutRequestId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any>
    ): List<MpesaTransactionRecord>
}

// ==================== DATA MODELS ====================

data class MpesaTransactionRecord(
    @SerializedName("id")
    val id: Int,
    @SerializedName("merchant_request_id")
    val merchantRequestId: String?,
    @SerializedName("checkout_request_id")
    val checkoutRequestId: String,
    @SerializedName("result_code")
    val resultCode: Int?,
    @SerializedName("result_desc")
    val resultDesc: String?,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("mpesa_receipt_number")
    val mpesaReceiptNumber: String?,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("transaction_date")
    val transactionDate: String?,
    @SerializedName("transaction_status")
    val transactionStatus: String, // SUCCESS, FAILED, PENDING
    @SerializedName("created_at")
    val createdAt: String
)

// ==================== MPESA WEBHOOK SUPABASE SERVICE ====================

/**
 * Service to save M-Pesa transaction data to the mpesa-webhook Supabase project
 *
 * This is SEPARATE from the Energy App project
 * Use this to store all M-Pesa webhook responses and transaction confirmations
 */
@Singleton
class MpesaWebhookSupabaseService @Inject constructor() {

    private val TAG = "MpesaWebhookSupabase"
    private val api: MpesaWebhookApi

    companion object {
        // ✅ MPESA-WEBHOOK PROJECT CREDENTIALS (VERIFIED)
        private const val MPESA_WEBHOOK_URL = "https://zaaaeoxfjhwsqeowbdrx.supabase.co/rest/v1/"
        private const val MPESA_WEBHOOK_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InphYWFlb3hmamh3c3Flb3diZHJ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU2MTQ5MDMsImV4cCI6MjA4MTE5MDkwM30.iGrWzFOM2x7zfJzsf3SMlxV-ucIyH1EgfNMizHp35Hc"
    }

    init {
        Log.d(TAG, "🔧 Initializing MpesaWebhookSupabaseService...")

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("apikey", MPESA_WEBHOOK_ANON_KEY)
                .header("Authorization", "Bearer $MPESA_WEBHOOK_ANON_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .build()
            chain.proceed(newRequest)
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(MPESA_WEBHOOK_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(MpesaWebhookApi::class.java)
        Log.d(TAG, "✅ MpesaWebhookSupabaseService initialized")
    }

    /**
     * Save M-Pesa STK Push response to mpesa_transactions table
     * Called AFTER STK Push is initiated
     */
    suspend fun saveStkPushResponse(
        checkoutRequestId: String,
        merchantRequestId: String?,
        amount: Double,
        phoneNumber: String,
        accountReference: String
    ): Result<MpesaTransactionRecord> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "💾 Saving STK Push response...")

            val transaction = mapOf<String, Any>(
                "checkout_request_id" to checkoutRequestId,
                "merchant_request_id" to (merchantRequestId ?: ""),
                "amount" to amount,
                "phone_number" to phoneNumber,
                "transaction_status" to "PENDING",
                "result_code" to -1  // Pending status code
            )

            val result = api.saveMpesaTransaction(transaction)

            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ STK Push response saved! Record ID: ${result.first().id}")
                Result.success(result.first())
            } else {
                Log.e(TAG, "❌ Failed to save STK Push response")
                Result.failure(Exception("Empty response from database"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving STK Push response: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Update M-Pesa transaction with webhook callback response
     * Called when Safaricom sends webhook callback with payment result
     */
    suspend fun updateTransactionWithWebhookResponse(
        checkoutRequestId: String,
        resultCode: Int,
        resultDesc: String,
        mpesaReceiptNumber: String?,
        transactionDate: String?
    ): Result<MpesaTransactionRecord> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Updating transaction with webhook response...")
            Log.d(TAG, "Result Code: $resultCode, Result: $resultDesc")

            val status = when (resultCode) {
                0 -> "SUCCESS"
                1032 -> "CANCELLED"
                1 -> "INSUFFICIENT_FUNDS"
                1037 -> "TIMEOUT"
                else -> "FAILED"
            }

            val updateData = mutableMapOf<String, Any>(
                "result_code" to resultCode,
                "result_desc" to resultDesc,
                "transaction_status" to status
            )

            mpesaReceiptNumber?.let {
                updateData["mpesa_receipt_number"] = it
            }

            transactionDate?.let {
                updateData["transaction_date"] = it
            }

            // ✅ FIXED: Include "eq." in the query parameter
            // Must be: ?checkout_request_id=eq.{checkoutRequestId}
            val result = api.updateMpesaTransaction("eq.$checkoutRequestId", updateData)

            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ Transaction updated! Status: $status")
                Result.success(result.first())
            } else {
                Log.e(TAG, "❌ Failed to update transaction")
                Result.failure(Exception("Empty response from database"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating transaction: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get all M-Pesa transactions
     */
    suspend fun getAllTransactions(): Result<List<MpesaTransactionRecord>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📊 Fetching all M-Pesa transactions...")
            val transactions = api.getAllMpesaTransactions()
            Log.d(TAG, "✅ Fetched ${transactions.size} transactions")
            Result.success(transactions)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching transactions: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get transaction by checkoutRequestId
     */
    suspend fun getTransactionByCheckoutId(
        checkoutRequestId: String
    ): Result<MpesaTransactionRecord?> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching transaction: $checkoutRequestId")
            val transactions = api.getAllMpesaTransactions()
            val transaction = transactions.find { it.checkoutRequestId == checkoutRequestId }
            Result.success(transaction)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching transaction: ${e.message}")
            Result.failure(e)
        }
    }
}