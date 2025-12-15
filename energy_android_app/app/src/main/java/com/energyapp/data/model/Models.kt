package com.energyapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// ===== USER MODELS =====
@Serializable
data class User(
    @SerialName("user_id")
    val userId: Int,

    @SerialName("full_name")
    val fullName: String,

    val username: String,

    @SerialName("mobile_no")
    val mobileNo: String? = null,

    @SerialName("role_id")
    val roleId: Int,

    @SerialName("role_name")
    val roleName: String,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("user_roles")
    val userRoles: String? = null
)

// ===== PUMP MODELS =====
@Serializable
data class Pump(
    @SerialName("pump_id")
    val pumpId: Int,

    @SerialName("pump_no")
    val pumpNo: String,

    @SerialName("pump_name")
    val pumpName: String,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("is_shift_open")
    val isShiftOpen: Boolean = false,

    @SerialName("current_shift_id")
    val currentShiftId: Int? = null
)

// ===== SHIFT MODELS =====
@Serializable
data class Shift(
    @SerialName("shift_id")
    val shiftId: Int,

    @SerialName("shift_name")
    val shiftName: String
)

// ===== PUMP SHIFT MODELS =====
@Serializable
data class PumpShift(
    @SerialName("pump_shift_id")
    val pumpShiftId: Int,

    @SerialName("pump_id")
    val pumpId: Int,

    @SerialName("pump_no")
    val pumpNo: String,

    @SerialName("pump_name")
    val pumpName: String,

    @SerialName("shift_id")
    val shiftId: Int,

    @SerialName("shift_name")
    val shiftName: String,

    @SerialName("opening_attendant_id")
    val openingAttendantId: Int,

    @SerialName("opening_attendant_name")
    val openingAttendantName: String,

    @SerialName("opening_time")
    val openingTime: Long,

    @SerialName("opening_meter_reading")
    val openingMeterReading: Double,

    @SerialName("closing_attendant_id")
    val closingAttendantId: Int? = null,

    @SerialName("closing_attendant_name")
    val closingAttendantName: String? = null,

    @SerialName("closing_time")
    val closingTime: Long? = null,

    @SerialName("closing_meter_reading")
    val closingMeterReading: Double? = null,

    @SerialName("is_closed")
    val isClosed: Boolean = false
)

// ===== SALES MODELS =====
@Serializable
data class SalesRecord(
    @SerialName("sale_id")
    val saleId: Int,

    @SerialName("sale_id_no")
    val saleIdNo: String,

    @SerialName("pump_shift_id")
    val pumpShiftId: Int,

    @SerialName("pump_id")
    val pumpId: Int,

    @SerialName("pump_no")
    val pumpNo: String,

    @SerialName("pump_name")
    val pumpName: String,

    @SerialName("attendant_id")
    val attendantId: Int,

    @SerialName("attendant_name")
    val attendantName: String,

    @SerialName("shift_name")
    val shiftName: String,

    @SerialName("sale_time")
    val saleTime: Long,

    val amount: Double,

    @SerialName("customer_mobile_no")
    val customerMobileNo: String? = null,

    @SerialName("mpesa_transaction_code")
    val mpesaTransactionCode: String? = null,

    @SerialName("transaction_status")
    val transactionStatus: String = "PENDING"
)

// ===== CREATE SALE REQUEST MODEL =====
@Serializable
data class CreateSaleRequest(
    @SerialName("sale_id_no")
    val saleIdNo: String,

    @SerialName("pump_shift_id")
    val pumpShiftId: Int,

    @SerialName("pump_id")
    val pumpId: Int,

    @SerialName("attendant_id")
    val attendantId: Int,

    val amount: Double,

    @SerialName("customer_mobile_no")
    val customerMobileNo: String,

    @SerialName("transaction_status")
    val transactionStatus: String = "PENDING",

    @SerialName("sale_time")
    val saleTime: Long = System.currentTimeMillis(),

    @SerialName("checkout_request_id")
    val checkoutRequestId: String? = null  // ← This field needs the column in DB
)
// ===== MPESA TRANSACTION MODELS =====
@Serializable
data class MpesaTransaction(
    @SerialName("transaction_id")
    val transactionId: Int,

    @SerialName("sale_id")
    val saleId: Int? = null,

    @SerialName("mobile_no")
    val mobileNo: String,

    val amount: Double,

    @SerialName("request_time")
    val requestTime: Long,

    @SerialName("checkout_request_id")
    val checkoutRequestId: String,

    @SerialName("merchant_request_id")
    val merchantRequestId: String,

    @SerialName("response_code")
    val responseCode: String,

    @SerialName("response_description")
    val responseDescription: String? = null,

    @SerialName("result_code")
    val resultCode: String? = null,

    @SerialName("result_description")
    val resultDescription: String? = null,

    @SerialName("mpesa_receipt_number")
    val mpesaReceiptNumber: String? = null
)

// ===== MPESA STK PUSH MODELS =====
@Serializable
data class MpesaStkRequest(
    val amount: Double,
    val phone: String,
    val account: String,
    val description: String
)

@Serializable
data class MpesaStkResponse(
    val success: Boolean,
    val message: String,
    @SerialName("MerchantRequestID")
    val merchantRequestID: String? = null,
    @SerialName("CheckoutRequestID")
    val checkoutRequestID: String? = null
)

@Serializable
data class MpesaStatusResponse(
    val success: Boolean,
    val message: String,
    val resultCode: String? = null,
    val resultDesc: String? = null,
    val mpesaReceiptNumber: String? = null
)

// ===== FILTER & SUMMARY MODELS =====
data class SalesFilter(
    val pumpId: Int? = null,
    val attendantId: Int? = null,
    val shiftId: Int? = null,
    val mobileNo: String? = null
)

data class SalesSummary(
    val totalSales: Double,
    val successfulTransactions: Int,
    val failedTransactions: Int,
    val totalTransactions: Int
)

// ===== SEALED CLASSES & ENUMS =====
sealed class TransactionStatus {
    object Success : TransactionStatus()
    data class Failed(val reason: String) : TransactionStatus()
    object Pending : TransactionStatus()
}

enum class MpesaResultCode(val code: String, val description: String) {
    SUCCESS("0", "Transaction successful"),
    INSUFFICIENT_FUNDS("1001", "Insufficient funds in M-Pesa account"),
    CANCELLED("1032", "Transaction cancelled by customer"),
    OTHER_ERROR("1000", "An error occurred during transaction")
}