package com.energyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_records",
    foreignKeys = [
        ForeignKey(
            entity = PumpShiftEntity::class,
            parentColumns = ["pump_shift_id"],
            childColumns = ["pump_shift_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PumpEntity::class,
            parentColumns = ["pump_id"],
            childColumns = ["pump_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["attendant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sale_id_no"], unique = true),
        Index(value = ["pump_shift_id"]),
        Index(value = ["pump_id"]),
        Index(value = ["attendant_id"])
    ]
)
data class SalesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sale_id")
    val saleId: Int = 0,
    
    @ColumnInfo(name = "sale_id_no")
    val saleIdNo: String,
    
    @ColumnInfo(name = "pump_shift_id")
    val pumpShiftId: Int,
    
    @ColumnInfo(name = "pump_id")
    val pumpId: Int,
    
    @ColumnInfo(name = "attendant_id")
    val attendantId: Int,
    
    @ColumnInfo(name = "sale_time")
    val saleTime: Long, // Unix timestamp in milliseconds
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "customer_mobile_no")
    val customerMobileNo: String?,
    
    @ColumnInfo(name = "mpesa_transaction_code")
    val mpesaTransactionCode: String?,
    
    @ColumnInfo(name = "transaction_status")
    val transactionStatus: String // SUCCESS, PENDING, FAILED
)

@Entity(
    tableName = "mpesa_transactions",
    foreignKeys = [
        ForeignKey(
            entity = SalesEntity::class,
            parentColumns = ["sale_id"],
            childColumns = ["sale_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sale_id"])]
)
data class MpesaTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    val transactionId: Int = 0,
    
    @ColumnInfo(name = "sale_id")
    val saleId: Int? = null,
    
    @ColumnInfo(name = "mobile_no")
    val mobileNo: String,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "request_time")
    val requestTime: Long,
    
    @ColumnInfo(name = "checkout_request_id")
    val checkoutRequestId: String,
    
    @ColumnInfo(name = "merchant_request_id")
    val merchantRequestId: String,
    
    @ColumnInfo(name = "response_code")
    val responseCode: String,
    
    @ColumnInfo(name = "response_description")
    val responseDescription: String?,
    
    @ColumnInfo(name = "result_code")
    val resultCode: String?,
    
    @ColumnInfo(name = "result_description")
    val resultDescription: String?,
    
    @ColumnInfo(name = "mpesa_receipt_number")
    val mpesaReceiptNumber: String?
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "setting_key")
    val settingKey: String,
    
    @ColumnInfo(name = "setting_value")
    val settingValue: String?
)
