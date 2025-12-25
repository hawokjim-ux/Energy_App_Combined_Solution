package com.energyapp.data.remote.models

import com.google.gson.annotations.SerializedName

// ==================== M-Pesa Models ====================

data class MpesaSTKPushRequest(
    @SerializedName("mobile_no")
    val mobileNo: String?,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("sale_id_no")
    val saleIdNo: String? = null,
    @SerializedName("pump_shift_id")
    val pumpShiftId: Int? = null,
    @SerializedName("attendant_id")
    val attendantId: Int? = null,
    @SerializedName("pump_id")
    val pumpId: Int? = null
)

data class MpesaSTKPushResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("transaction_status")
    val transactionStatus: String,
    @SerializedName("result_description")
    val resultDescription: String,
    @SerializedName("sale_id")
    val saleId: Int,
    @SerializedName("mpesa_receipt_number")
    val mpesaReceiptNumber: String? = null,
    @SerializedName("checkout_request_id")
    val checkoutRequestID: String? = null
)

data class MpesaQueryRequest(
    @SerializedName("checkout_request_id")
    val checkoutRequestID: String
)

data class MpesaQueryResponse(
    @SerializedName("result_code")
    val resultCode: String?,
    @SerializedName("result_desc")
    val resultDesc: String?,
    @SerializedName("checkout_request_id")
    val checkoutRequestID: String?,
    @SerializedName("merchant_request_id")
    val merchantRequestID: String?,
    @SerializedName("customer_message")
    val customerMessage: String?
)

data class MpesaTransactionResponse(
    @SerializedName("checkout_request_id")
    val checkoutRequestID: String?,
    @SerializedName("merchant_request_id")
    val merchantRequestID: String?,
    @SerializedName("response_description")
    val responseDescription: String?,
    @SerializedName("customer_message")
    val customerMessage: String?
)

// ==================== User Models ====================

data class UserResponse(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("mobile_no")
    val mobileNo: String?,
    @SerializedName("role_id")
    val roleId: Int,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("role_name")
    val roleName: String
)

// ==================== Pump Models ====================

data class PumpResponse(
    @SerializedName("pump_id")
    val pumpId: Int,
    @SerializedName("pump_name")
    val pumpName: String,
    @SerializedName("pump_type")
    val pumpType: String? = null,  // Nullable - may not exist in DB
    @SerializedName("is_active")
    val isActive: Boolean = true
)

// ==================== Shift Models ====================

data class ShiftResponse(
    @SerializedName("shift_id")
    val shiftId: Int,
    @SerializedName("shift_name")
    val shiftName: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String
)

data class OpenShiftRequest(
    @SerializedName("pump_id")
    val pumpId: Int,
    @SerializedName("shift_id")
    val shiftId: Int,
    @SerializedName("attendant_id")
    val attendantId: Int,
    @SerializedName("opening_reading")
    val openingReading: Double,
    @SerializedName("opening_time")
    val openingTime: String
)

data class CloseShiftRequest(
    @SerializedName("closing_reading")
    val closingReading: Double,
    @SerializedName("closing_time")
    val closingTime: String,
    @SerializedName("amount_received")
    val amountReceived: Double,
    @SerializedName("is_closed")
    val isClosed: Boolean = true
)

data class PumpShiftResponse(
    @SerializedName("pump_shift_id")
    val pumpShiftId: Int,
    @SerializedName("pump_id")
    val pumpId: Int,
    @SerializedName("shift_id")
    val shiftId: Int,
    @SerializedName("attendant_id")
    val attendantId: Int,
    @SerializedName("opening_reading")
    val openingReading: Double,
    @SerializedName("opening_time")
    val openingTime: String,
    @SerializedName("closing_reading")
    val closingReading: Double?,
    @SerializedName("closing_time")
    val closingTime: String?,
    @SerializedName("is_closed")
    val isClosed: Boolean
)

// ==================== Sales Models ====================

data class SaleResponse(
    @SerializedName("sale_id")
    val saleId: Int,
    @SerializedName("sale_id_no")
    val saleIdNo: String,
    @SerializedName("pump_shift_id")
    val pumpShiftId: Int,
    @SerializedName("pump_id")
    val pumpId: Int,
    @SerializedName("attendant_id")
    val attendantId: Int,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("customer_mobile_no")
    val customerMobileNo: String? = null,
    @SerializedName("transaction_status")
    val transactionStatus: String = "PENDING",
    @SerializedName("mpesa_receipt_number")
    val mpesaReceiptNumber: String? = null,
    @SerializedName("created_at")  // Changed from sale_time to match database
    val saleTime: String = "",
    @SerializedName("checkout_request_id")
    val checkoutRequestId: String? = null
)

data class CreateSaleRequest(
    @SerializedName("sale_id_no")
    val saleIdNo: String,
    @SerializedName("pump_shift_id")
    val pumpShiftId: Int,
    @SerializedName("pump_id")
    val pumpId: Int,
    @SerializedName("attendant_id")
    val attendantId: Int,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("customer_mobile_no")
    val customerMobileNo: String,
    @SerializedName("transaction_status")
    val transactionStatus: String = "PENDING",
    @SerializedName("checkout_request_id")
    val checkoutRequestId: String? = null
)

// ==================== Attendant Models ====================

data class AttendantSummary(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("mobile_no")
    val mobileNo: String?,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("today_sales")
    val todaySales: Double,
    @SerializedName("transaction_count")
    val transactionCount: Int
)