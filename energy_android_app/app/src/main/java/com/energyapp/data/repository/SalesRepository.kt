package com.energyapp.data.repository

import com.energyapp.data.local.dao.*
import com.energyapp.data.local.entity.MpesaTransactionEntity
import com.energyapp.data.local.entity.SalesEntity
import com.energyapp.data.model.MpesaResultCode
import com.energyapp.data.model.SalesFilter
import com.energyapp.data.model.SalesRecord
import com.energyapp.util.MpesaSimulator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class SalesRepository(
    private val salesDao: SalesDao,
    private val mpesaTransactionDao: MpesaTransactionDao,
    private val pumpDao: PumpDao,
    private val pumpShiftDao: PumpShiftDao,
    private val shiftDao: ShiftDao,
    private val userDao: UserDao
) {
    fun getAllSales(): Flow<List<SalesRecord>> {
        return salesDao.getAllSales().map { entities ->
            entities.mapNotNull { entity ->
                convertToSalesRecord(entity)
            }
        }
    }

    fun getSalesWithFilters(filter: SalesFilter): Flow<List<SalesRecord>> {
        return salesDao.getSalesWithFilters(
            pumpId = filter.pumpId,
            attendantId = filter.attendantId,
            pumpShiftId = null, // We'll filter by shift differently if needed
            mobileNo = filter.mobileNo
        ).map { entities ->
            entities.mapNotNull { entity ->
                convertToSalesRecord(entity)
            }
        }
    }

    suspend fun getTotalSales(): Double {
        return salesDao.getTotalSuccessfulSales() ?: 0.0
    }

    suspend fun recordSale(
        saleIdNo: String,
        pumpShiftId: Int,
        pumpId: Int,
        attendantId: Int,
        amount: Double,
        customerMobileNo: String
    ): Result<Pair<Int, String?>> {
        return try {
            // Validate shift is open
            val pumpShift = pumpShiftDao.getPumpShiftById(pumpShiftId)
                ?: return Result.failure(Exception("Invalid pump shift"))

            if (pumpShift.isClosed) {
                return Result.failure(Exception("Shift is closed"))
            }

            // Simulate M-Pesa STK Push
            val mpesaResult = MpesaSimulator.simulateStkPush(
                mobileNo = customerMobileNo,
                amount = amount
            )

            // Delay to simulate processing
            delay(2000)

            // Create M-Pesa transaction record
            val checkoutRequestId = UUID.randomUUID().toString()
            val merchantRequestId = UUID.randomUUID().toString()

            val mpesaTransaction = MpesaTransactionEntity(
                mobileNo = customerMobileNo,
                amount = amount,
                requestTime = System.currentTimeMillis(),
                checkoutRequestId = checkoutRequestId,
                merchantRequestId = merchantRequestId,
                responseCode = "0",
                responseDescription = "Success. Request accepted for processing.",
                resultCode = mpesaResult.code,
                resultDescription = mpesaResult.description,
                mpesaReceiptNumber = if (mpesaResult == MpesaResultCode.SUCCESS) {
                    "NF${Random().nextInt(900000) + 100000}"
                } else null
            )

            val transactionId = mpesaTransactionDao.insertTransaction(mpesaTransaction).toInt()

            // Create sales record
            val transactionStatus = if (mpesaResult == MpesaResultCode.SUCCESS) "SUCCESS" else "FAILED"
            val salesEntity = SalesEntity(
                saleIdNo = saleIdNo,
                pumpShiftId = pumpShiftId,
                pumpId = pumpId,
                attendantId = attendantId,
                saleTime = System.currentTimeMillis(),
                amount = amount,
                customerMobileNo = customerMobileNo,
                mpesaTransactionCode = mpesaTransaction.mpesaReceiptNumber,
                transactionStatus = transactionStatus
            )

            val saleId = salesDao.insertSale(salesEntity).toInt()

            // Update M-Pesa transaction with sale ID
            val updatedTransaction = mpesaTransaction.copy(
                transactionId = transactionId,
                saleId = saleId
            )
            mpesaTransactionDao.updateTransaction(updatedTransaction)

            if (mpesaResult == MpesaResultCode.SUCCESS) {
                Result.success(Pair(saleId, mpesaTransaction.mpesaReceiptNumber))
            } else {
                Result.failure(Exception(mpesaResult.description))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun convertToSalesRecord(entity: SalesEntity): SalesRecord? {
        val pump = pumpDao.getPumpById(entity.pumpId) ?: return null
        val attendant = userDao.getUserById(entity.attendantId) ?: return null
        val pumpShift = pumpShiftDao.getPumpShiftById(entity.pumpShiftId) ?: return null
        val shift = shiftDao.getShiftById(pumpShift.shiftId) ?: return null

        return SalesRecord(
            saleId = entity.saleId,
            saleIdNo = entity.saleIdNo,
            pumpShiftId = entity.pumpShiftId,
            pumpId = entity.pumpId,
            pumpNo = pump.pumpNo,
            pumpName = pump.pumpName,
            attendantId = entity.attendantId,
            attendantName = attendant.fullName,
            shiftName = shift.shiftName,
            saleTime = entity.saleTime,
            amount = entity.amount,
            customerMobileNo = entity.customerMobileNo,
            mpesaTransactionCode = entity.mpesaTransactionCode,
            transactionStatus = entity.transactionStatus
        )
    }
}
