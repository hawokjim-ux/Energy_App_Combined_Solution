package com.energyapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// Common API Response
data class ApiResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: Any? = null
)

// Login
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("user")
    val user: UserDto
)

data class UserDto(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("mobile_no")
    val mobileNo: String?,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("is_active")
    val isActive: Boolean
)

// Pumps and Shifts
data class PumpDto(
    @SerializedName("pump_id")
    val pumpId: Int,
    
    @SerializedName("pump_no")
    val pumpNo: String,
    
    @SerializedName("pump_name")
    val pumpName: String,
    
    @SerializedName("is_shift_open")
    val isShiftOpen: Boolean,
    
    @SerializedName("current_shift_id")
    val currentShiftId: Int?
)

data class ShiftDto(
    @SerializedName("shift_id")
    val shiftId: Int,
    
    @SerializedName("shift_name")
    val shiftName: String
)

data class OpenShiftRequest(
    @SerializedName("pump_id")
    val pumpId: Int,
    
    @SerializedName("shift_id")
    val shiftId: Int,
    
    @SerializedName("attendant_id")
    val attendantId: Int,
    
    @SerializedName("opening_meter_reading")
    val openingMeterReading: Double
)

data class CloseShiftRequest(
    @SerializedName("pump_shift_id")
    val pumpShiftId: Int,
    
    @SerializedName("closing_attendant_id")
    val closingAttendantId: Int,
    
    @SerializedName("closing_meter_reading")
    val closingMeterReading: Double
)

// M-Pesa
data class MpesaStkPushRequest(
    @SerializedName("mobile_no")
    val mobileNo: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("sale_id_no")
    val saleIdNo: String,
    
    @SerializedName("pump_shift_id")
    val pumpShiftId: Int,
    
    @SerializedName("attendant_id")
    val attendantId: Int
)

data class MpesaStkPushResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("transaction_status")
    val transactionStatus: String,
    
    @SerializedName("result_description")
    val resultDescription: String,
    
    @SerializedName("sale_id")
    val saleId: Int,
    
    @SerializedName("mpesa_receipt_number")
    val mpesaReceiptNumber: String?
)

// Sales Records
data class SalesRecordDto(
    @SerializedName("sale_id")
    val saleId: Int,
    
    @SerializedName("sale_id_no")
    val saleIdNo: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("sale_time")
    val saleTime: String,
    
    @SerializedName("customer_mobile_no")
    val customerMobileNo: String?,
    
    @SerializedName("mpesa_transaction_code")
    val mpesaTransactionCode: String?,
    
    @SerializedName("transaction_status")
    val transactionStatus: String,
    
    @SerializedName("pump_no")
    val pumpNo: String,
    
    @SerializedName("pump_name")
    val pumpName: String,
    
    @SerializedName("shift_name")
    val shiftName: String,
    
    @SerializedName("attendant_name")
    val attendantName: String
)

// User Management
data class CreateUserRequest(
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("mobile_no")
    val mobileNo: String?,
    
    @SerializedName("role")
    val role: String
)

// Filters
data class FiltersResponse(
    @SerializedName("attendants")
    val attendants: List<AttendantFilter>,
    
    @SerializedName("pumps")
    val pumps: List<PumpFilter>,
    
    @SerializedName("shifts")
    val shifts: List<ShiftFilter>
)

data class AttendantFilter(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String
)

data class PumpFilter(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("no")
    val no: String
)

data class ShiftFilter(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String
)
