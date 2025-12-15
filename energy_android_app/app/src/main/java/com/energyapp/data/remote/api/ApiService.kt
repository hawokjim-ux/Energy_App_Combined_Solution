package com.energyapp.data.remote.api

import com.energyapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service interface for future backend integration
 * Currently, the app uses local Room database
 * To enable backend integration, implement RetrofitClient and use this service
 */
interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/pumps")
    suspend fun getPumps(): Response<List<PumpDto>>

    @GET("api/shifts")
    suspend fun getShifts(): Response<List<ShiftDto>>

    @POST("api/shift/open")
    suspend fun openShift(@Body request: OpenShiftRequest): Response<ApiResponse>

    @POST("api/shift/close")
    suspend fun closeShift(@Body request: CloseShiftRequest): Response<ApiResponse>

    @POST("api/mpesa/stk_push")
    suspend fun initiateStkPush(@Body request: MpesaStkPushRequest): Response<MpesaStkPushResponse>

    @GET("api/reports/sales")
    suspend fun getSalesRecords(
        @Query("pump_id") pumpId: Int? = null,
        @Query("attendant_id") attendantId: Int? = null,
        @Query("mobile_no") mobileNo: String? = null,
        @Query("shift_id") shiftId: Int? = null
    ): Response<List<SalesRecordDto>>

    @GET("api/admin/users")
    suspend fun getUsers(): Response<List<UserDto>>

    @POST("api/admin/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<ApiResponse>

    @GET("api/filters")
    suspend fun getFilters(): Response<FiltersResponse>

    @GET("api/status")
    suspend fun getStatus(): Response<ApiResponse>
}