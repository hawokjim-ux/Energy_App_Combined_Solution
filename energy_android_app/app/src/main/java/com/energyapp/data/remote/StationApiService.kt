package com.energyapp.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.energyapp.util.MpesaConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable

/**
 * Station API Service for Multi-Station Support
 * 
 * Endpoints:
 * - GET /stations.php - List all stations
 * - GET /stations.php?id=1 - Get single station
 * - GET /stations.php?user_id=xxx - Get user's accessible stations
 * - POST /stations.php - Create station
 * - PUT /stations.php?id=1 - Update station
 */

// ==================== DATA MODELS ====================

@Serializable
data class StationDto(
    @SerializedName("station_id")
    val stationId: Int,
    
    @SerializedName("station_code")
    val stationCode: String,
    
    @SerializedName("station_name")
    val stationName: String,
    
    @SerializedName("station_type")
    val stationType: String? = null,
    
    @SerializedName("physical_address")
    val physicalAddress: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("county")
    val county: String? = null,
    
    @SerializedName("region")
    val region: String? = null,
    
    @SerializedName("gps_latitude")
    val gpsLatitude: Double? = null,
    
    @SerializedName("gps_longitude")
    val gpsLongitude: Double? = null,
    
    @SerializedName("mpesa_till_number")
    val mpesaTillNumber: String? = null,
    
    @SerializedName("station_phone")
    val stationPhone: String? = null,
    
    @SerializedName("station_email")
    val stationEmail: String? = null,
    
    @SerializedName("manager_name")
    val managerName: String? = null,
    
    @SerializedName("manager_phone")
    val managerPhone: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("is_online")
    val isOnline: Boolean = true,
    
    @SerializedName("pump_count")
    val pumpCount: Int? = null,
    
    @SerializedName("user_count")
    val userCount: Int? = null,
    
    @SerializedName("active_shifts")
    val activeShifts: Int? = null,
    
    @SerializedName("today_sales")
    val todaySales: Double? = null,
    
    @SerializedName("today_transactions")
    val todayTransactions: Int? = null,
    
    @SerializedName("mpesa_sales")
    val mpesaSales: Double? = null,
    
    // User-specific fields
    @SerializedName("station_role")
    val stationRole: String? = null,
    
    @SerializedName("is_primary_station")
    val isPrimaryStation: Boolean? = null,
    
    @SerializedName("can_view_reports")
    val canViewReports: Boolean? = null
)

@Serializable
data class StationsListResponseDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<StationDto>,
    
    @SerializedName("count")
    val count: Int? = null,
    
    @SerializedName("summary")
    val summary: StationsSummaryDto? = null
)

@Serializable
data class StationsSummaryDto(
    @SerializedName("total_stations")
    val totalStations: Int,
    
    @SerializedName("total_today_sales")
    val totalTodaySales: Double,
    
    @SerializedName("total_today_transactions")
    val totalTodayTransactions: Int
)

@Serializable
data class StationResponseDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: StationDto? = null,
    
    @SerializedName("station_id")
    val stationId: Int? = null
)

@Serializable
data class CreateStationRequestDto(
    @SerializedName("station_code")
    val stationCode: String,
    
    @SerializedName("station_name")
    val stationName: String,
    
    @SerializedName("station_type")
    val stationType: String = "petrol_station",
    
    @SerializedName("physical_address")
    val physicalAddress: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("county")
    val county: String? = null,
    
    @SerializedName("region")
    val region: String? = null,
    
    @SerializedName("mpesa_till_number")
    val mpesaTillNumber: String? = null,
    
    @SerializedName("station_phone")
    val stationPhone: String? = null,
    
    @SerializedName("manager_name")
    val managerName: String? = null,
    
    @SerializedName("created_by")
    val createdBy: String? = null
)

// ==================== RETROFIT INTERFACE ====================

interface IStationApi {
    
    @GET("stations.php")
    suspend fun getAllStations(): StationsListResponseDto
    
    @GET("stations.php")
    suspend fun getStation(@Query("id") stationId: Int): StationResponseDto
    
    @GET("stations.php")
    suspend fun getUserStations(@Query("user_id") userId: String): StationsListResponseDto
    
    @POST("stations.php")
    suspend fun createStation(@Body request: CreateStationRequestDto): StationResponseDto
    
    @PUT("stations.php")
    suspend fun updateStation(
        @Query("id") stationId: Int,
        @Body updates: Map<String, Any>
    ): StationResponseDto
    
    @DELETE("stations.php")
    suspend fun deleteStation(@Query("id") stationId: Int): StationResponseDto
}

// ==================== SERVICE ====================

@Singleton
class StationApiService @Inject constructor() {
    
    private val TAG = "StationApiService"
    private val api: IStationApi
    
    init {
        Log.d(TAG, "Initializing StationApiService...")
        
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Add Supabase auth headers
        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", MpesaConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${MpesaConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        
        val gson = GsonBuilder()
            .setLenient()
            .create()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(MpesaConfig.SUPABASE_URL + "/rest/v1/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        api = retrofit.create(IStationApi::class.java)
        
        Log.d(TAG, "✅ StationApiService initialized")
    }
    
    suspend fun getAllStations(): Result<StationsListResponseDto> {
        return try {
            Log.d(TAG, "Fetching all stations...")
            val response = api.getAllStations()
            
            if (response.success) {
                Log.d(TAG, "✅ Loaded ${response.data.size} stations")
                Result.success(response)
            } else {
                Log.e(TAG, "❌ Failed: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getStation(stationId: Int): Result<StationDto> {
        return try {
            val response = api.getStation(stationId)
            
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting station: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getUserStations(userId: String): Result<StationsListResponseDto> {
        return try {
            Log.d(TAG, "Fetching stations for user: $userId")
            val response = api.getUserStations(userId)
            
            if (response.success) {
                Log.d(TAG, "✅ User has access to ${response.data.size} stations")
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user stations: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun createStation(request: CreateStationRequestDto): Result<Int> {
        return try {
            Log.d(TAG, "Creating station: ${request.stationName}")
            val response = api.createStation(request)
            
            if (response.success && response.stationId != null) {
                Log.d(TAG, "✅ Station created with ID: ${response.stationId}")
                Result.success(response.stationId)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating station: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun updateStation(stationId: Int, updates: Map<String, Any>): Result<Boolean> {
        return try {
            Log.d(TAG, "Updating station: $stationId")
            val response = api.updateStation(stationId, updates)
            
            if (response.success) {
                Log.d(TAG, "✅ Station updated")
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating station: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun deleteStation(stationId: Int): Result<Boolean> {
        return try {
            Log.d(TAG, "Deleting station: $stationId")
            val response = api.deleteStation(stationId)
            
            if (response.success) {
                Log.d(TAG, "✅ Station deleted")
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting station: ${e.message}")
            Result.failure(e)
        }
    }
}
