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
    @GET("users_new")
    suspend fun getUsers(
        @Query("select") select: String = "*,user_roles(role_name)"
    ): List<UserWithRole>

    @GET("users_new")
    suspend fun getUserByUsername(
        @Query("username") username: String,
        @Query("select") select: String = "*,user_roles(role_name)"
    ): List<UserWithRole>

    @POST("users_new")
    suspend fun createUser(@Body user: Map<String, @JvmSuppressWildcards Any>): List<UserWithRole>

    @PATCH("users_new")
    suspend fun updateUser(
        @Query("user_id") userId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any>
    ): List<UserWithRole>

    @DELETE("users_new")
    suspend fun deleteUser(
        @Query("user_id") userId: String
    )

    @GET("pumps")
    suspend fun getPumps(
        @Query("is_active") isActive: String = "eq.true"
    ): List<PumpResponse>

    @GET("pumps")
    suspend fun getAllPumps(): List<PumpResponse>

    @POST("pumps")
    suspend fun createPump(@Body pump: Map<String, @JvmSuppressWildcards Any>): List<PumpResponse>

    @PATCH("pumps")
    suspend fun updatePump(
        @Query("pump_id") pumpId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any>
    ): List<PumpResponse>

    @DELETE("pumps")
    suspend fun deletePump(
        @Query("pump_id") pumpId: String
    )

    // ==================== Fuel Types ====================
    @GET("fuel_types")
    suspend fun getFuelTypes(
        @Query("is_active") isActive: String = "eq.true"
    ): List<FuelTypeResponse>

    @GET("fuel_types")
    suspend fun getFuelTypeById(
        @Query("fuel_type_id") fuelTypeId: String
    ): List<FuelTypeResponse>

    @POST("shifts")
    suspend fun createShift(@Body shift: Map<String, @JvmSuppressWildcards Any>): List<ShiftResponse>

    @PATCH("shifts")
    suspend fun updateShift(
        @Query("shift_id") shiftId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any>
    ): List<ShiftResponse>

    @DELETE("shifts")
    suspend fun deleteShift(
        @Query("shift_id") shiftId: String
    )

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

    // ==================== Stations ====================
    @GET("stations")
    suspend fun getStations(
        @Query("is_active") isActive: String = "eq.true"
    ): List<StationResponse>

    @GET("stations")
    suspend fun getStationById(
        @Query("station_id") stationId: String
    ): List<StationResponse>

    // ==================== Attendant Station Assignments ====================
    @GET("attendant_stations")
    suspend fun getAttendantStations(
        @Query("attendant_id") attendantId: String,
        @Query("is_active") isActive: String = "eq.true"
    ): List<AttendantStationResponse>

    @GET("attendant_stations")
    suspend fun getAttendantPrimaryStation(
        @Query("attendant_id") attendantId: String,
        @Query("is_primary_station") isPrimary: String = "eq.true",
        @Query("is_active") isActive: String = "eq.true"
    ): List<AttendantStationResponse>

    // FIXED: Changed from sales_records to sales
    @GET("sales")
    suspend fun getAllSales(
        @Query("order") order: String = "created_at.desc"
    ): List<SaleResponse>

    @GET("sales")
    suspend fun getSalesByAttendant(
        @Query("attendant_id") attendantId: String,
        @Query("order") order: String = "created_at.desc"
    ): List<SaleResponse>

    @POST("sales")
    suspend fun createSale(@Body saleRecord: Map<String, @JvmSuppressWildcards Any>): List<SaleResponse>

    // FIXED: Changed from sales_records to sales
    @PATCH("sales")
    suspend fun updateSaleStatus(
        @Query("sale_id") saleId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any>
    ): List<SaleResponse>

    // ==================== License Management ====================
    @GET("app_licenses")
    suspend fun getAllLicenses(
        @Query("order") order: String = "created_at.desc"
    ): List<LicenseDbResponse>

    @GET("app_licenses")
    suspend fun getLicenseByKey(
        @Query("license_key") licenseKey: String
    ): List<LicenseDbResponse>

    @GET("app_licenses")
    suspend fun getLicenseByDeviceId(
        @Query("activation_device_id") deviceId: String
    ): List<LicenseDbResponse>

    @GET("app_licenses")
    suspend fun getLicenseByPhone(
        @Query("client_phone") clientPhone: String
    ): List<LicenseDbResponse>

    @POST("app_licenses")
    suspend fun createLicense(@Body license: Map<String, @JvmSuppressWildcards Any?>): List<LicenseDbResponse>

    @PATCH("app_licenses")
    suspend fun updateLicense(
        @Query("license_key") licenseKey: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any?>
    ): List<LicenseDbResponse>

    @PATCH("app_licenses")
    suspend fun revokeLicense(
        @Query("license_id") licenseId: String,
        @Body updateData: Map<String, @JvmSuppressWildcards Any?>
    ): List<LicenseDbResponse>
}

// ==================== Data Models ====================
data class UserWithRole(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    val username: String,
    @SerializedName("mobile_no") val mobileNo: String?,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("password_hash") val passwordHash: String? = null,
    @SerializedName("user_roles") val userRoles: RoleData? = null
)

data class CreateUserRequest(
    @SerializedName("full_name") val fullName: String,
    val username: String,
    @SerializedName("mobile_no") val mobileNo: String? = null,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("password_hash") val passwordHash: String
)

fun CreateUserRequest.toInsertMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
        "full_name" to fullName,
        "username" to username,
        "role_id" to roleId,
        "is_active" to isActive,
        "password_hash" to passwordHash
    )
    mobileNo?.takeIf { it.isNotBlank() }?.let { map["mobile_no"] = it }
    return map
}

data class RoleData(
    @SerializedName("role_name") val roleName: String
)

// License Database Response Model
data class LicenseDbResponse(
    @SerializedName("license_id") val licenseId: Int,
    @SerializedName("license_key") val licenseKey: String,
    @SerializedName("license_type") val licenseType: String,
    @SerializedName("client_name") val clientName: String?,
    @SerializedName("client_phone") val clientPhone: String?,
    @SerializedName("duration_days") val durationDays: Int,
    @SerializedName("max_devices") val maxDevices: Int,
    @SerializedName("is_activated") val isActivated: Boolean,
    @SerializedName("activation_device_id") val activationDeviceId: String?,
    @SerializedName("activation_date") val activationDate: String?,
    @SerializedName("expiration_date") val expirationDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("activation_count") val activationCount: Int,
    @SerializedName("is_revoked") val isRevoked: Boolean,
    @SerializedName("revoked_reason") val revokedReason: String?,
    @SerializedName("device_manufacturer") val deviceManufacturer: String?,
    @SerializedName("device_model") val deviceModel: String?
)

// ==================== Main Service ====================
@Singleton
class SupabaseApiService @Inject constructor(
    private val mpesaBackendService: MpesaBackendService
) {
    private val TAG = "SupabaseApiService"
    private val api: SupabaseApi

    companion object {
        private const val SUPABASE_URL = "https://pxcdaivlvltmdifxietb.supabase.co/rest/v1/"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB4Y2RhaXZsdmx0bWRpZnhpZXRiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDI3NDIsImV4cCI6MjA4MTMxODc0Mn0.s6nv24s6M83gAcW_nSKCBfcXcqJ_7owwqdObPDT7Ky0"
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

    suspend fun getAllPumps(): Result<List<PumpResponse>> = withContext(Dispatchers.IO) {
        try {
            val pumps = api.getAllPumps()
            Result.success(pumps)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching all pumps: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createPump(name: String, type: String): Result<PumpResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚀 Creating pump: $name")
            val pumpData = mapOf<String, Any>(
                "pump_name" to name,
                "pump_type" to type,
                "is_active" to true
            )
            val result = api.createPump(pumpData)
            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ Pump created successfully")
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to create pump"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating pump: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updatePump(pumpId: Int, name: String, type: String, isActive: Boolean): Result<PumpResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Updating pump: $pumpId")
            val updateData = mapOf<String, Any>(
                "pump_name" to name,
                "pump_type" to type,
                "is_active" to isActive
            )
            val result = api.updatePump("eq.$pumpId", updateData)
            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ Pump updated successfully")
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to update pump"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating pump: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deletePump(pumpId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Deleting pump: $pumpId")
            api.deletePump("eq.$pumpId")
            Log.d(TAG, "✅ Pump deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting pump: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== Fuel Types ====================
    suspend fun getFuelTypes(): Result<List<FuelTypeResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "⛽ Fetching fuel types...")
            val fuelTypes = api.getFuelTypes()
            Log.d(TAG, "✅ Retrieved ${fuelTypes.size} fuel types")
            Result.success(fuelTypes)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching fuel types: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getFuelTypeById(fuelTypeId: Int): Result<FuelTypeResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "⛽ Fetching fuel type: $fuelTypeId")
            val fuelTypes = api.getFuelTypeById("eq.$fuelTypeId")
            if (fuelTypes.isNotEmpty()) {
                Result.success(fuelTypes.first())
            } else {
                Result.failure(Exception("Fuel type not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching fuel type: ${e.message}")
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

    suspend fun createShiftDefinition(name: String, startTime: String, endTime: String): Result<ShiftResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚀 Creating shift: $name")
            val shiftData = mapOf<String, Any>(
                "shift_name" to name,
                "start_time" to startTime,
                "end_time" to endTime
            )
            val result = api.createShift(shiftData)
            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ Shift created successfully")
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to create shift"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating shift: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateShiftDefinition(shiftId: Int, name: String, startTime: String, endTime: String): Result<ShiftResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Updating shift: $shiftId")
            val updateData = mapOf<String, Any>(
                "shift_name" to name,
                "start_time" to startTime,
                "end_time" to endTime
            )
            val result = api.updateShift("eq.$shiftId", updateData)
            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ Shift updated successfully")
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to update shift"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating shift: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteShiftDefinition(shiftId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Deleting shift: $shiftId")
            api.deleteShift("eq.$shiftId")
            Log.d(TAG, "✅ Shift deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting shift: ${e.message}")
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

    // ==================== Stations ====================
    suspend fun getStations(): Result<List<StationResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏪 Fetching stations...")
            val stations = api.getStations()
            Log.d(TAG, "✅ Retrieved ${stations.size} stations")
            Result.success(stations)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching stations: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getStationById(stationId: Int): Result<StationResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏪 Fetching station: $stationId")
            val stations = api.getStationById("eq.$stationId")
            if (stations.isNotEmpty()) {
                Result.success(stations.first())
            } else {
                Result.failure(Exception("Station not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching station: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== Attendant Station Assignments ====================
    /**
     * Get all stations assigned to an attendant
     */
    suspend fun getAttendantStations(attendantId: Int): Result<List<AttendantStationResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔗 Fetching station assignments for attendant: $attendantId")
            val assignments = api.getAttendantStations("eq.$attendantId")
            Log.d(TAG, "✅ Retrieved ${assignments.size} station assignments")
            Result.success(assignments)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching attendant stations: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get the primary station assignment for an attendant
     */
    suspend fun getAttendantPrimaryStation(attendantId: Int): Result<AttendantStationResponse?> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔗 Fetching primary station for attendant: $attendantId")
            val assignments = api.getAttendantPrimaryStation("eq.$attendantId")
            if (assignments.isNotEmpty()) {
                Log.d(TAG, "✅ Found primary station: ${assignments.first().stationId}")
                Result.success(assignments.first())
            } else {
                // Try to get any active assignment if no primary
                val allAssignments = api.getAttendantStations("eq.$attendantId")
                if (allAssignments.isNotEmpty()) {
                    Log.d(TAG, "✅ Found station assignment (not primary): ${allAssignments.first().stationId}")
                    Result.success(allAssignments.first())
                } else {
                    Log.d(TAG, "⚠️ No station assignment found for attendant")
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching primary station: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get full user station assignment details (with station info)
     */
    suspend fun getUserStationAssignment(userId: Int): Result<UserStationAssignment?> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔗 Fetching full station assignment for user: $userId")
            
            // Get the user's station assignment
            val assignmentResult = getAttendantPrimaryStation(userId)
            val assignment = assignmentResult.getOrNull() ?: return@withContext Result.success(null)
            
            // Get the station details
            val stationResult = getStationById(assignment.stationId)
            val station = stationResult.getOrNull() ?: return@withContext Result.success(null)
            
            // Build the full assignment object
            val userAssignment = UserStationAssignment(
                userId = userId,
                stationId = station.stationId,
                stationName = station.stationName,
                stationCode = station.stationCode,
                stationRole = assignment.stationRole ?: "attendant",
                canProcessSales = assignment.canProcessSales,
                canManageShift = assignment.canManageShift,
                isPrimaryStation = assignment.isPrimaryStation
            )
            
            Log.d(TAG, "✅ User assignment: ${station.stationName} (${station.stationCode})")
            Result.success(userAssignment)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching user station assignment: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== Sales Methods ====================
    suspend fun createSale(request: CreateSaleRequest): Result<SaleResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📝 Creating sale record: ${request.saleIdNo}")
            
            // Build sale record with ALL fields matching web app exactly
            val saleRecord = mutableMapOf<String, Any>(
                "sale_id_no" to request.saleIdNo,
                "pump_shift_id" to request.pumpShiftId,
                "pump_id" to request.pumpId,
                "attendant_id" to request.attendantId,
                "amount" to request.amount,
                "customer_mobile_no" to request.customerMobileNo,
                "transaction_status" to request.transactionStatus,
                // Station and fuel details
                "station_id" to request.stationId,
                // Detailed sale fields (matching web app)
                "liters_sold" to request.litersSold,
                "price_per_liter" to request.pricePerLiter,
                "total_amount" to request.totalAmount,
                "payment_method" to request.paymentMethod
            )
            
            // Add optional fields if not null/blank
            request.checkoutRequestId?.takeIf { it.isNotBlank() }?.let { 
                saleRecord["checkout_request_id"] = it 
            }
            request.fuelTypeId?.let { 
                saleRecord["fuel_type_id"] = it 
            }
            request.saleTime?.takeIf { it.isNotBlank() }?.let { 
                saleRecord["sale_time"] = it 
            }
            request.mpesaTransactionId?.takeIf { it.isNotBlank() }?.let { 
                saleRecord["mpesa_transaction_id"] = it 
            }
            request.mpesaReceiptNumber?.takeIf { it.isNotBlank() }?.let { 
                saleRecord["mpesa_receipt_number"] = it 
            }
            
            Log.d(TAG, "📤 Sale record fields: ${saleRecord.keys.joinToString()}")
            
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
            mpesaReceipt?.let { updateMap["mpesa_receipt_number"] = it }
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

            // FIXED: Clean and format phone number properly
            val rawPhone = request.mobileNo ?: return@withContext Result.failure(Exception("Mobile number is required"))
            val cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")

            val formattedPhone = when {
                cleanPhone.startsWith("254") && cleanPhone.length == 12 -> cleanPhone
                cleanPhone.startsWith("0") && cleanPhone.length == 10 -> "254${cleanPhone.substring(1)}"
                cleanPhone.startsWith("+254") -> cleanPhone.substring(1)
                cleanPhone.length == 9 -> "254$cleanPhone"
                else -> {
                    Log.e(TAG, "❌ Invalid phone number format: $rawPhone")
                    return@withContext Result.failure(Exception("Invalid phone number format. Use 254XXXXXXXXX or 07XXXXXXXX"))
                }
            }

            if (formattedPhone.length != 12) {
                Log.e(TAG, "❌ Phone number must be 12 digits: $formattedPhone")
                return@withContext Result.failure(Exception("Phone number must be 12 digits (254XXXXXXXXX)"))
            }

            Log.d(TAG, "📱 Formatted phone: $formattedPhone, Amount: ${request.amount}")

            // Generate unique sale ID
            val saleIdNo = "SALE_${System.currentTimeMillis()}"

            // Create pending sale record
            val saleRecord = mapOf<String, Any>(
                "sale_id_no" to saleIdNo,
                "pump_shift_id" to (request.pumpShiftId ?: 0),
                "pump_id" to (request.pumpId ?: 0),
                "attendant_id" to (request.attendantId ?: 0),
                "amount" to request.amount,
                "customer_mobile_no" to formattedPhone,
                "transaction_status" to "PENDING"
            )

            val sales = api.createSale(saleRecord)
            if (sales.isEmpty()) {
                return@withContext Result.failure(Exception("Failed to create sale record"))
            }

            val sale = sales.first()
            Log.d(TAG, "✅ Pending sale record created with ID: ${sale.saleId}")

            // FIXED: Use proper amount (must be integer for M-Pesa)
            val stkPushRequest = MpesaStkRequest(
                amount = request.amount.toInt().toDouble(), // Ensure no decimals
                phone = formattedPhone,
                account = saleIdNo,
                description = "Fuel Payment - ${request.pumpId?.let { "Pump $it" } ?: "Energy App"}",
                pumpId = request.pumpId?.toString() ?: "0",
                shiftId = request.pumpShiftId?.toString() ?: "0",
                userId = request.attendantId?.toString() ?: "0"
            )

            Log.d(TAG, "📤 Sending STK Push request to backend...")
            val stkResponse = mpesaBackendService.initiateStkPush(stkPushRequest)

            if (!stkResponse.success) {
                Log.e(TAG, "❌ STK Push failed: ${stkResponse.message}")
                updateSaleStatusInternal(sale.saleId, "FAILED")
                return@withContext Result.failure(Exception(stkResponse.message))
            }

            Log.d(TAG, "✅ STK Push sent to customer's phone")
            Log.d(TAG, "🎫 Checkout Request ID: ${stkResponse.checkoutRequestID}")

            // Update sale with checkout request ID
            val checkoutRequestID = stkResponse.checkoutRequestID
            if (!checkoutRequestID.isNullOrEmpty()) {
                try {
                    val updateMap = mapOf<String, Any>("checkout_request_id" to checkoutRequestID)
                    api.updateSaleStatus("eq.${sale.saleId}", updateMap)
                    Log.d(TAG, "✅ Sale updated with checkout request ID")
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ Failed to update checkout request ID: ${e.message}")
                }

                // Start polling for transaction status
                pollTransactionStatus(checkoutRequestID, sale.saleId, 24)
            }

            Result.success(
                MpesaSTKPushResponse(
                    success = true,
                    message = "Payment request sent. Please check your phone for M-Pesa prompt.",
                    transactionStatus = "PENDING",
                    resultDescription = "Payment request sent to ${formattedPhone}",
                    saleId = sale.saleId,
                    mpesaReceiptNumber = null,
                    checkoutRequestID = checkoutRequestID
                )
            )
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
            Log.d(TAG, "🔄 Starting transaction polling for sale $saleId")
            var attempts = 0

            while (attempts < maxAttempts) {
                delay(5000) // Wait 5 seconds between polls
                attempts++

                Log.d(TAG, "🔍 Polling transaction status (attempt $attempts/$maxAttempts)...")

                try {
                    val statusResponse = mpesaBackendService.checkTransactionStatus(checkoutRequestID)

                    Log.d(TAG, "📊 Status: ResultCode=${statusResponse.resultCode}, Desc=${statusResponse.resultDesc}")

                    when (statusResponse.resultCode) {
                        0 -> {
                            Log.d(TAG, "✅ Transaction successful!")
                            val receipt = statusResponse.mpesaReceiptNumber ?: "MP${System.currentTimeMillis()}"
                            updateSaleStatusInternal(
                                saleId = saleId,
                                status = "SUCCESS",
                                mpesaCode = receipt
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
                        null -> {
                            // Still pending, continue polling
                            Log.d(TAG, "⏳ Transaction still pending...")
                        }
                        else -> {
                            // Some other error occurred
                            Log.d(TAG, "❌ Transaction failed with code ${statusResponse.resultCode}: ${statusResponse.resultDesc}")
                            updateSaleStatusInternal(saleId, "FAILED")
                            return@withContext
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ Error in poll attempt $attempts: ${e.message}")
                    // Continue polling even if one attempt fails
                }
            }

            // Max attempts reached, still pending
            Log.d(TAG, "⏰ Polling timeout after $maxAttempts attempts - transaction still pending")
            updateSaleStatusInternal(saleId, "TIMEOUT")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in polling loop: ${e.message}")
            updateSaleStatusInternal(saleId, "TIMEOUT")
        }
    }

    private suspend fun updateSaleStatusInternal(
        saleId: Int,
        status: String,
        mpesaCode: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Updating sale $saleId to status: $status")
            val updateMap = mutableMapOf<String, Any>("transaction_status" to status)
            mpesaCode?.let {
                updateMap["mpesa_receipt_number"] = it
                Log.d(TAG, "💳 M-Pesa Receipt: $it")
            }

            api.updateSaleStatus("eq.$saleId", updateMap)
            Log.d(TAG, "✅ Sale $saleId updated to $status")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to update sale status: ${e.message}")
            e.printStackTrace()
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
            Log.e(TAG, "❌ Full error: ${e.stackTraceToString()}")
            e.printStackTrace()
            
            // Get the actual error message for debugging
            val rawMessage = e.message ?: "Unknown error"
            Log.e(TAG, "❌ Raw error message: $rawMessage")
            
            // Try to get the response body for more details
            val errorMessage = when {
                rawMessage.contains("23503", ignoreCase = true) -> "Invalid role selected - role does not exist"
                rawMessage.contains("role_id", ignoreCase = true) -> "Invalid role selected - role does not exist"
                rawMessage.contains("foreign key", ignoreCase = true) -> "Invalid role selected - role does not exist"
                rawMessage.contains("duplicate", ignoreCase = true) -> "Username already exists"
                rawMessage.contains("unique", ignoreCase = true) -> "Username already exists"
                rawMessage.contains("23505", ignoreCase = true) -> "Username already exists"  // Unique violation code
                rawMessage.contains("400") -> "Invalid user data: $rawMessage"
                rawMessage.contains("401") -> "Unauthorized - check API key"
                rawMessage.contains("403") -> "Forbidden - RLS policy may be blocking insert"
                rawMessage.contains("409") -> "Conflict error - check role selection or username"
                rawMessage.contains("500") -> "Server error"
                else -> "Failed to create user: $rawMessage"
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

    // ==================== License Management ====================
    
    /**
     * Create a new license in the database
     */
    suspend fun createLicenseInDb(
        licenseKey: String,
        licenseType: String,
        clientName: String?,
        clientPhone: String?,
        durationDays: Int,
        maxDevices: Int
    ): Result<LicenseDbResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔑 Creating license in database: $licenseKey")
            val licenseData = mapOf<String, Any?>(
                "license_key" to licenseKey,
                "license_type" to licenseType,
                "client_name" to clientName,
                "client_phone" to clientPhone,
                "duration_days" to durationDays,
                "max_devices" to maxDevices,
                "is_activated" to false,
                "activation_count" to 0,
                "is_revoked" to false
            )
            val result = api.createLicense(licenseData)
            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ License created in database")
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to create license in database"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating license: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Check if license exists and get its details from database
     */
    suspend fun checkLicenseInDb(licenseKey: String): Result<LicenseDbResponse?> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Checking license in database: $licenseKey")
            val results = api.getLicenseByKey("eq.$licenseKey")
            if (results.isNotEmpty()) {
                Log.d(TAG, "✅ License found in database")
                Result.success(results.first())
            } else {
                Log.d(TAG, "⚠️ License not found in database")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking license: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Activate license on a device
     */
    suspend fun activateLicenseInDb(
        licenseKey: String,
        deviceId: String,
        deviceManufacturer: String,
        deviceModel: String
    ): Result<LicenseDbResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔓 Activating license: $licenseKey for device: $deviceId")
            
            // Calculate expiration date
            val checkResult = checkLicenseInDb(licenseKey)
            if (checkResult.isFailure || checkResult.getOrNull() == null) {
                return@withContext Result.failure(Exception("License not found in database"))
            }
            
            val license = checkResult.getOrNull()!!
            
            // Check if already activated on different device
            if (license.isActivated && license.activationDeviceId != deviceId) {
                return@withContext Result.failure(Exception(
                    "License already activated on another device.\nContact: +254720316175"
                ))
            }
            
            // Check if revoked
            if (license.isRevoked) {
                return@withContext Result.failure(Exception(
                    "This license has been revoked.\nContact: +254720316175"
                ))
            }
            
            // Calculate expiration
            val now = java.time.Instant.now()
            val expirationInstant = now.plusSeconds(license.durationDays.toLong() * 24 * 60 * 60)
            val activationDate = now.toString()
            val expirationDate = expirationInstant.toString()
            
            val updateData = mapOf<String, Any?>(
                "is_activated" to true,
                "activation_device_id" to deviceId,
                "activation_date" to activationDate,
                "expiration_date" to expirationDate,
                "activation_count" to (license.activationCount + 1),
                "device_manufacturer" to deviceManufacturer,
                "device_model" to deviceModel,
                "last_check_date" to activationDate
            )
            
            val result = api.updateLicense("eq.$licenseKey", updateData)
            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ License activated successfully")
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to activate license"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error activating license: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get all licenses from database
     */
    suspend fun getAllLicenses(): Result<List<LicenseDbResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📋 Fetching all licenses from database")
            val licenses = api.getAllLicenses()
            Log.d(TAG, "✅ Retrieved ${licenses.size} licenses")
            Result.success(licenses)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching licenses: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Check if device has valid license
     */
    suspend fun checkDeviceLicense(deviceId: String): Result<LicenseDbResponse?> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Checking device license: $deviceId")
            val results = api.getLicenseByDeviceId("eq.$deviceId")
            val validLicense = results.firstOrNull { license ->
                license.isActivated && !license.isRevoked && 
                license.expirationDate?.let { expDate ->
                    try {
                        val expInstant = java.time.Instant.parse(expDate)
                        expInstant.isAfter(java.time.Instant.now())
                    } catch (e: Exception) { false }
                } ?: false
            }
            Result.success(validLicense)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking device license: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Check if phone number already has a license (for duplicate prevention)
     */
    suspend fun checkPhoneDuplicate(phoneNumber: String): Result<LicenseDbResponse?> = withContext(Dispatchers.IO) {
        try {
            if (phoneNumber.isBlank()) {
                return@withContext Result.success(null) // No phone = no duplicate
            }
            
            // Normalize phone number - remove spaces, handle both formats
            val cleanPhone = phoneNumber.trim().replace(" ", "")
            
            Log.d(TAG, "🔍 Checking for duplicate phone: $cleanPhone")
            
            // Try to find exact match first
            var results = api.getLicenseByPhone("eq.$cleanPhone")
            
            // If not found and starts with 0, try with 254 prefix
            if (results.isEmpty() && cleanPhone.startsWith("0")) {
                val with254 = "254${cleanPhone.substring(1)}"
                results = api.getLicenseByPhone("eq.$with254")
            }
            
            // If not found and starts with 254, try with 0 prefix
            if (results.isEmpty() && cleanPhone.startsWith("254")) {
                val with0 = "0${cleanPhone.substring(3)}"
                results = api.getLicenseByPhone("eq.$with0")
            }
            
            // If not found and starts with +254, try without +
            if (results.isEmpty() && cleanPhone.startsWith("+254")) {
                val without = cleanPhone.substring(1)
                results = api.getLicenseByPhone("eq.$without")
            }
            
            val existingLicense = results.firstOrNull()
            if (existingLicense != null) {
                Log.d(TAG, "⚠️ Duplicate phone found - License: ${existingLicense.licenseKey}")
            } else {
                Log.d(TAG, "✅ No duplicate phone found")
            }
            
            Result.success(existingLicense)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking phone duplicate: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Revoke a license
     */
    suspend fun revokeLicenseInDb(licenseId: Int, reason: String): Result<LicenseDbResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚫 Revoking license: $licenseId")
            val updateData = mapOf<String, Any?>(
                "is_revoked" to true,
                "revoked_at" to java.time.Instant.now().toString(),
                "revoked_reason" to reason
            )
            val result = api.revokeLicense("eq.$licenseId", updateData)
            if (result.isNotEmpty()) {
                Log.d(TAG, "✅ License revoked successfully")
                Result.success(result.first())
            } else {
                Result.failure(Exception("Failed to revoke license"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error revoking license: ${e.message}")
            Result.failure(e)
        }
    }
}