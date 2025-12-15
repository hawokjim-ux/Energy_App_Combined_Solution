package com.energyapp.data.remote

import android.util.Log
import com.energyapp.data.remote.models.*
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.mindrot.jbcrypt.BCrypt
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton

// ==================== Retrofit Interface ====================
interface SupabaseApi {
    @GET("users")
    suspend fun getUsers(
        @Query("select") select: String = "*,user_roles(role_name)"
    ): List<UserWithRole>

    @GET("users")
    suspend fun getUserByUsername(
        @Query("username") username: String,
        @Query("select") select: String = "*,user_roles(role_name)"
    ): List<UserWithRole>

    @POST("users")
    suspend fun createUser(@Body user: Map<String, @JvmSuppressWildcards Any>): List<UserWithRole>

    @PATCH("users")
    suspend fun updateUser(
        @Query("user_id") userId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any>
    ): List<UserWithRole>

    @DELETE("users")
    suspend fun deleteUser(
        @Query("user_id") userId: String
    )

    @GET("pumps")
    suspend fun getPumps(
        @Query("is_active") isActive: String = "eq.true"
    ): List<PumpResponse>

    @GET("shifts")
    suspend fun getShifts(): List<ShiftResponse>

    @GET("pump_shifts")
    suspend fun getOpenShifts(
        @Query("is_closed") isClosed: String = "eq.false"
    ): List<PumpShiftResponse>

    @POST("pump_shifts")
    suspend fun openShift(@Body request: OpenShiftRequest): List<PumpShiftResponse>

    @PATCH("pump_shifts")
    suspend fun closeShift(
        @Query("pump_shift_id") pumpShiftId: String,
        @Body request: CloseShiftRequest
    ): List<PumpShiftResponse>

    @GET("sales_records")
    suspend fun getAllSales(
        @Query("order") order: String = "sale_time.desc"
    ): List<SaleResponse>

    @GET("sales_records")
    suspend fun getSalesByAttendant(
        @Query("attendant_id") attendantId: String,
        @Query("order") order: String = "sale_time.desc"
    ): List<SaleResponse>

    @POST("sales_records")
    suspend fun createSale(@Body saleRecord: Map<String, @JvmSuppressWildcards Any>): List<SaleResponse>

    @PATCH("sales_records")
    suspend fun updateSaleStatus(
        @Query("sale_id") saleId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any>
    ): List<SaleResponse>
}

// ==================== Data Models ====================
data class UserWithRole(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("full_name")
    val fullName: String,
    val username: String,
    @SerializedName("mobile_no")
    val mobileNo: String?,
    @SerializedName("role_id")
    val roleId: Int,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("password_hash")
    val passwordHash: String? = null,
    @SerializedName("user_roles")
    val userRoles: RoleData? = null
)

data class CreateUserRequest(
    @SerializedName("full_name")
    val fullName: String,
    val username: String,
    @SerializedName("mobile_no")
    val mobileNo: String? = null,
    @SerializedName("role_id")
    val roleId: Int,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("password_hash")
    val passwordHash: String
)

fun CreateUserRequest.toInsertMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
        "full_name" to fullName,
        "username" to username,
        "role_id" to roleId,
        "is_active" to isActive,
        "password_hash" to passwordHash
    )

    mobileNo?.takeIf { it.isNotBlank() }?.let {
        map["mobile_no"] = it
    }

    return map
}

data class RoleData(
    @SerializedName("role_name")
    val roleName: String
)

// ==================== Main Service ====================
@Singleton
class SupabaseApiService @Inject constructor(
    private val mpesaBackendService: MpesaBackendService
) {

    private val TAG = "SupabaseApiService"
    private val api: SupabaseApi

    companion object {
        private const val SUPABASE_URL = "https://acqfnlizrkpfmogyxhtu.supabase.co/rest/v1/"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFjcWZubGl6cmtwZm1vZ3l4aHR1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0NjAxNTcsImV4cCI6MjA4MTAzNjE1N30.jOP8Hesw8ybi4ooRVgf8JiYyKsDtHTzDFuCfHS3PH6Y"
    }

    init {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
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
            .baseUrl(SUPABASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(SupabaseApi::class.java)
    }

    // ==================== Authentication ====================
    suspend fun login(username: String, password: String): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔐 Attempting login with username: $username")
            val trimmedUsername = username.trim()
            val users = api.getUserByUsername("eq.$trimmedUsername")
            Log.d(TAG, "🔍 Query returned ${users.size} user(s)")

            val user = users.firstOrNull { it.isActive }
            if (user == null) {
                Log.e(TAG, "❌ No active user found with username: $trimmedUsername")
                return@withContext Result.failure(Exception("Invalid username or password"))
            }

            Log.d(TAG, "🔑 Validating password for user: $trimmedUsername")
            if (!validatePassword(password, user.passwordHash)) {
                Log.e(TAG, "❌ Password validation failed for user: $trimmedUsername")
                return@withContext Result.failure(Exception("Invalid username or password"))
            }

            Log.d(TAG, "✅ Login successful for user: ${user.username}")
            val roleName = user.userRoles?.roleName ?: "Unknown"
            val userResponse = UserResponse(
                userId = user.userId,
                fullName = user.fullName,
                username = user.username,
                mobileNo = user.mobileNo,
                roleId = user.roleId,
                isActive = user.isActive,
                roleName = roleName
            )
            Result.success(userResponse)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Login failed - ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    private fun validatePassword(plainPassword: String, hashedPassword: String?): Boolean {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            Log.e(TAG, "❌ No password hash found in database")
            return false
        }
        return try {
            BCrypt.checkpw(plainPassword, hashedPassword)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Password validation error: ${e.message}")
            false
        }
    }

    // ==================== Pumps ====================
    suspend fun getPumps(): Result<List<PumpResponse>> = withContext(Dispatchers.IO) {
        try {
            val pumps = api.getPumps()
            Result.success(pumps)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching pumps: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== Shifts ====================
    suspend fun getShifts(): Result<List<ShiftResponse>> = withContext(Dispatchers.IO) {
        try {
            val shifts = api.getShifts()
            Result.success(shifts)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching shifts: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getOpenShifts(): Result<List<PumpShiftResponse>> = withContext(Dispatchers.IO) {
        try {
            val openShifts = api.getOpenShifts()
            Result.success(openShifts)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching open shifts: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun openShift(request: OpenShiftRequest): Result<PumpShiftResponse> = withContext(Dispatchers.IO) {
        try {
            val shifts = api.openShift(request)
            if (shifts.isNotEmpty()) {
                Result.success(shifts.first())
            } else {
                Result.failure(Exception("Failed to open shift"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error opening shift: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun closeShift(pumpShiftId: Int, request: CloseShiftRequest): Result<PumpShiftResponse> = withContext(Dispatchers.IO) {
        try {
            val result = api.closeShift("eq.$pumpShiftId", request)
            if (result.isNotEmpty()) {
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to close shift"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error closing shift: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== Sales Methods ====================
    suspend fun createSale(request: CreateSaleRequest): Result<SaleResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📝 Creating sale record: ${request.saleIdNo}")

            val saleRecord = mapOf<String, Any>(
                "sale_id_no" to request.saleIdNo,
                "pump_shift_id" to request.pumpShiftId,
                "pump_id" to request.pumpId,
                "attendant_id" to request.attendantId,
                "amount" to request.amount,
                "customer_mobile_no" to request.customerMobileNo,
                "transaction_status" to request.transactionStatus,
                "checkout_request_id" to (request.checkoutRequestId ?: "")
            )

            val sales = api.createSale(saleRecord)

            if (sales.isNotEmpty()) {
                Log.d(TAG, "✅ Sale record created with ID: ${sales.first().saleId}")
                Result.success(sales.first())
            } else {
                Log.e(TAG, "❌ Failed to create sale record")
                Result.failure(Exception("Failed to create sale record"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating sale: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateSaleTransactionStatus(
        saleId: Int,
        status: String,
        mpesaReceipt: String? = null
    ): Result<SaleResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Updating sale $saleId status to: $status")

            val updateMap = mutableMapOf<String, Any>(
                "transaction_status" to status
            )

            mpesaReceipt?.let {
                updateMap["mpesa_receipt_number"] = it
            }

            val result = api.updateSaleStatus("eq.$saleId", updateMap)

            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ Sale status updated successfully")
                Result.success(result.first())
            } else {
                Log.e(TAG, "❌ Failed to update sale status")
                Result.failure(Exception("Failed to update sale status"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating sale status: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAllSalesRecords(): Result<List<SaleResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📊 Fetching all sales records")
            val sales = api.getAllSales()
            Log.d(TAG, "✅ Retrieved ${sales.size} sales records")
            Result.success(sales)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching all sales: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSalesRecordsByAttendant(attendantId: Int): Result<List<SaleResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👤 Fetching sales for attendant: $attendantId")
            val sales = api.getSalesByAttendant("eq.$attendantId")
            Log.d(TAG, "✅ Retrieved ${sales.size} sales for attendant")
            Result.success(sales)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching attendant sales: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getDailySalesTotal(attendantId: Int? = null): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val sales = if (attendantId != null) {
                api.getSalesByAttendant("eq.$attendantId")
            } else {
                api.getAllSales()
            }

            val total = sales
                .filter { it.transactionStatus == "SUCCESS" }
                .sumOf { it.amount }

            Log.d(TAG, "💰 Daily total: KES $total")
            Result.success(total)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calculating daily total: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSalesByAttendant(attendantId: Int): Result<List<SaleResponse>> = withContext(Dispatchers.IO) {
        try {
            val sales = api.getSalesByAttendant("eq.$attendantId")
            Result.success(sales)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching sales: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAllSales(): Result<List<SaleResponse>> = withContext(Dispatchers.IO) {
        try {
            val sales = api.getAllSales()
            Result.success(sales)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching all sales: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== M-Pesa Integration ====================
    suspend fun initiateMpesaSTKPush(request: MpesaSTKPushRequest): Result<MpesaSTKPushResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚀 Initiating M-Pesa STK Push...")
            Log.d(TAG, "📱 Mobile: ${request.mobileNo}, Amount: ${request.amount}")

            val mobileNumber = request.mobileNo ?: return@withContext Result.failure(Exception("Mobile number is required"))

            val saleIdNo = "SALE_${System.currentTimeMillis()}"
            val saleRecord = mapOf<String, Any>(
                "sale_id_no" to saleIdNo,
                "pump_shift_id" to (request.pumpShiftId ?: 0),
                "pump_id" to (request.pumpId ?: 0),
                "attendant_id" to (request.attendantId ?: 0),
                "amount" to request.amount,
                "customer_mobile_no" to mobileNumber,
                "transaction_status" to "PENDING"
            )

            val sales = api.createSale(saleRecord)
            if (sales.isEmpty()) {
                return@withContext Result.failure(Exception("Failed to create sale record"))
            }
            val sale = sales.first()
            Log.d(TAG, "✅ Pending sale record created with ID: ${sale.saleId}")

            val stkPushRequest = MpesaStkRequest(
                amount = request.amount,
                phone = mobileNumber,
                account = saleIdNo,
                description = "Fuel Payment"
            )

            val stkResponse = mpesaBackendService.initiateStkPush(stkPushRequest)

            if (!stkResponse.success) {
                updateSaleStatusInternal(sale.saleId, "FAILED")
                return@withContext Result.failure(Exception(stkResponse.message))
            }

            Log.d(TAG, "✅ STK Push sent to customer's phone")

            val checkoutRequestID = stkResponse.checkoutRequestID
            if (!checkoutRequestID.isNullOrEmpty()) {
                pollTransactionStatus(checkoutRequestID, sale.saleId, 30)
            }

            Result.success(MpesaSTKPushResponse(
                success = true,
                message = "Payment request sent. Please enter M-Pesa PIN on your phone.",
                transactionStatus = "PENDING",
                resultDescription = "Payment request sent",
                saleId = sale.saleId,
                mpesaReceiptNumber = null,
                checkoutRequestID = checkoutRequestID
            ))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initiating M-Pesa payment: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Payment failed: ${e.message}"))
        }
    }

    private suspend fun pollTransactionStatus(
        checkoutRequestID: String,
        saleId: Int,
        maxAttempts: Int
    ) = withContext(Dispatchers.IO) {
        try {
            var attempts = 0
            while (attempts < maxAttempts) {
                delay(2000)
                attempts++

                Log.d(TAG, "🔍 Polling transaction status (attempt $attempts/$maxAttempts)...")

                val statusResponse = mpesaBackendService.checkTransactionStatus(checkoutRequestID)

                when (statusResponse.resultCode) {
                    0 -> {
                        Log.d(TAG, "✅ Transaction successful!")
                        updateSaleStatusInternal(
                            saleId = saleId,
                            status = "SUCCESS",
                            mpesaCode = statusResponse.mpesaReceiptNumber ?: "MP${System.currentTimeMillis()}"
                        )
                        return@withContext
                    }
                    1032 -> {
                        Log.d(TAG, "❌ Transaction cancelled by user")
                        updateSaleStatusInternal(saleId, "CANCELLED")
                        return@withContext
                    }
                    1 -> {
                        Log.d(TAG, "❌ Insufficient funds")
                        updateSaleStatusInternal(saleId, "FAILED")
                        return@withContext
                    }
                    1037 -> {
                        Log.d(TAG, "⏰ Transaction timeout")
                        updateSaleStatusInternal(saleId, "TIMEOUT")
                        return@withContext
                    }
                    else -> {
                        if (statusResponse.resultCode != null) {
                            Log.d(TAG, "❌ Transaction failed: ${statusResponse.resultDesc}")
                            updateSaleStatusInternal(saleId, "FAILED")
                            return@withContext
                        }
                    }
                }
            }

            Log.d(TAG, "⏰ Polling timeout - transaction still pending")
            updateSaleStatusInternal(saleId, "TIMEOUT")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error polling transaction status: ${e.message}")
        }
    }

    private suspend fun updateSaleStatusInternal(
        saleId: Int,
        status: String,
        mpesaCode: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val updateMap = mutableMapOf<String, Any>("transaction_status" to status)
            mpesaCode?.let { updateMap["mpesa_transaction_code"] = it }

            api.updateSaleStatus("eq.$saleId", updateMap)
            Log.d(TAG, "✅ Sale $saleId updated to $status")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to update sale status: ${e.message}")
        }
    }

    suspend fun checkTransactionStatus(checkoutRequestID: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = mpesaBackendService.checkTransactionStatus(checkoutRequestID)
            val status = when (response.resultCode) {
                0 -> "SUCCESS"
                1032 -> "CANCELLED"
                1 -> "INSUFFICIENT_FUNDS"
                1037 -> "TIMEOUT"
                null -> "PENDING"
                else -> "FAILED"
            }
            Result.success(status)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking transaction status: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== Attendants ====================
    suspend fun getAttendants(): Result<List<AttendantSummary>> = withContext(Dispatchers.IO) {
        try {
            val users = api.getUsers()
            val attendants = users.filter { it.userRoles?.roleName == "Pump Attendant" }
            val attendantSummaries = attendants.map { attendant ->
                val salesResult = getSalesByAttendant(attendant.userId)
                val sales = if (salesResult.isSuccess) salesResult.getOrNull() ?: emptyList() else emptyList()
                AttendantSummary(
                    userId = attendant.userId,
                    fullName = attendant.fullName,
                    mobileNo = attendant.mobileNo,
                    isActive = attendant.isActive,
                    todaySales = sales.filter { it.transactionStatus == "SUCCESS" }.sumOf { it.amount },
                    transactionCount = sales.size
                )
            }
            Result.success(attendantSummaries)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching attendants: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== Users Management ====================
    suspend fun getAllUsers(): Result<List<UserResponse>> = withContext(Dispatchers.IO) {
        try {
            val users = api.getUsers()
            val userResponses = users.map { user ->
                val roleName = user.userRoles?.roleName ?: "Unknown"
                UserResponse(
                    userId = user.userId,
                    fullName = user.fullName,
                    username = user.username,
                    mobileNo = user.mobileNo,
                    roleId = user.roleId,
                    isActive = user.isActive,
                    roleName = roleName
                )
            }
            Result.success(userResponses)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching all users: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createUser(
        fullName: String,
        username: String,
        password: String,
        mobileNo: String?,
        roleId: Int
    ): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚀 Creating user - $username")

            if (fullName.isBlank()) {
                return@withContext Result.failure(Exception("Full name cannot be empty"))
            }
            if (username.isBlank()) {
                return@withContext Result.failure(Exception("Username cannot be empty"))
            }
            if (password.length < 6) {
                return@withContext Result.failure(Exception("Password must be at least 6 characters"))
            }

            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12))
            Log.d(TAG, "🔐 Password hashed successfully")

            val createUserRequest = CreateUserRequest(
                fullName = fullName.trim(),
                username = username.trim(),
                mobileNo = mobileNo?.trim()?.takeIf { it.isNotBlank() },
                roleId = roleId,
                isActive = true,
                passwordHash = passwordHash
            )

            val createdUsers = api.createUser(createUserRequest.toInsertMap())

            if (createdUsers.isEmpty()) {
                return@withContext Result.failure(Exception("Failed to create user"))
            }

            val createdUser = createdUsers.first()
            Log.d(TAG, "✅ User created successfully - ${createdUser.username}")

            val roleName = createdUser.userRoles?.roleName ?: "Unknown"
            val userResponse = UserResponse(
                userId = createdUser.userId,
                fullName = createdUser.fullName,
                username = createdUser.username,
                mobileNo = createdUser.mobileNo,
                roleId = createdUser.roleId,
                isActive = createdUser.isActive,
                roleName = roleName
            )
            Result.success(userResponse)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create user - ${e.message}")
            e.printStackTrace()

            val errorMessage = when {
                e.message?.contains("duplicate") == true || e.message?.contains("409") == true ->
                    "Username already exists"
                e.message?.contains("400") == true -> "Invalid user data"
                else -> "Failed to create user: ${e.message}"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun updateUser(
        userId: Int,
        fullName: String? = null,
        mobileNo: String? = null,
        isActive: Boolean? = null
    ): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val updateMap = mutableMapOf<String, Any>()
            fullName?.let { updateMap["full_name"] = it }
            mobileNo?.let { updateMap["mobile_no"] = it }
            isActive?.let { updateMap["is_active"] = it }

            if (updateMap.isEmpty()) {
                return@withContext Result.failure(Exception("No fields to update"))
            }

            val result = api.updateUser("eq.$userId", updateMap)
            if (result.isNotEmpty()) {
                val updatedUser = result.first()
                val roleName = updatedUser.userRoles?.roleName ?: "Unknown"
                val userResponse = UserResponse(
                    userId = updatedUser.userId,
                    fullName = updatedUser.fullName,
                    username = updatedUser.username,
                    mobileNo = updatedUser.mobileNo,
                    roleId = updatedUser.roleId,
                    isActive = updatedUser.isActive,
                    roleName = roleName
                )
                Result.success(userResponse)
            } else {
                Result.failure(Exception("Failed to update user"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.deleteUser("eq.$userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting user: ${e.message}")
            Result.failure(e)
        }
    }
}