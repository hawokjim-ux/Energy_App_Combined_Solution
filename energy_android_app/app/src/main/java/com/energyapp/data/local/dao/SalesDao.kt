package com.energyapp.data.local.dao

import androidx.room.*
import com.energyapp.data.local.entity.MpesaTransactionEntity
import com.energyapp.data.local.entity.SalesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {
    @Query("SELECT * FROM sales_records ORDER BY sale_time DESC")
    fun getAllSales(): Flow<List<SalesEntity>>
    
    @Query("SELECT * FROM sales_records WHERE sale_id = :saleId")
    suspend fun getSaleById(saleId: Int): SalesEntity?
    
    @Query("SELECT * FROM sales_records WHERE sale_id_no = :saleIdNo")
    suspend fun getSaleBySaleIdNo(saleIdNo: String): SalesEntity?
    
    @Query("SELECT * FROM sales_records WHERE pump_id = :pumpId ORDER BY sale_time DESC")
    fun getSalesByPump(pumpId: Int): Flow<List<SalesEntity>>
    
    @Query("SELECT * FROM sales_records WHERE attendant_id = :attendantId ORDER BY sale_time DESC")
    fun getSalesByAttendant(attendantId: Int): Flow<List<SalesEntity>>
    
    @Query("SELECT * FROM sales_records WHERE pump_shift_id = :pumpShiftId ORDER BY sale_time DESC")
    fun getSalesByPumpShift(pumpShiftId: Int): Flow<List<SalesEntity>>
    
    @Query("SELECT * FROM sales_records WHERE customer_mobile_no LIKE '%' || :mobileNo || '%' ORDER BY sale_time DESC")
    fun searchSalesByMobile(mobileNo: String): Flow<List<SalesEntity>>
    
    @Query("""
        SELECT * FROM sales_records 
        WHERE (:pumpId IS NULL OR pump_id = :pumpId)
        AND (:attendantId IS NULL OR attendant_id = :attendantId)
        AND (:pumpShiftId IS NULL OR pump_shift_id = :pumpShiftId)
        AND (:mobileNo IS NULL OR customer_mobile_no LIKE '%' || :mobileNo || '%')
        ORDER BY sale_time DESC
    """)
    fun getSalesWithFilters(
        pumpId: Int?,
        attendantId: Int?,
        pumpShiftId: Int?,
        mobileNo: String?
    ): Flow<List<SalesEntity>>
    
    @Query("SELECT SUM(amount) FROM sales_records WHERE transaction_status = 'SUCCESS'")
    suspend fun getTotalSuccessfulSales(): Double?
    
    @Query("SELECT SUM(amount) FROM sales_records WHERE transaction_status = 'SUCCESS' AND pump_id = :pumpId")
    suspend fun getTotalSalesByPump(pumpId: Int): Double?
    
    @Query("SELECT SUM(amount) FROM sales_records WHERE transaction_status = 'SUCCESS' AND attendant_id = :attendantId")
    suspend fun getTotalSalesByAttendant(attendantId: Int): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SalesEntity): Long
    
    @Update
    suspend fun updateSale(sale: SalesEntity)
    
    @Delete
    suspend fun deleteSale(sale: SalesEntity)
}

@Dao
interface MpesaTransactionDao {
    @Query("SELECT * FROM mpesa_transactions ORDER BY request_time DESC")
    fun getAllTransactions(): Flow<List<MpesaTransactionEntity>>
    
    @Query("SELECT * FROM mpesa_transactions WHERE transaction_id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): MpesaTransactionEntity?
    
    @Query("SELECT * FROM mpesa_transactions WHERE sale_id = :saleId")
    suspend fun getTransactionBySaleId(saleId: Int): MpesaTransactionEntity?
    
    @Query("SELECT * FROM mpesa_transactions WHERE checkout_request_id = :checkoutRequestId")
    suspend fun getTransactionByCheckoutRequestId(checkoutRequestId: String): MpesaTransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MpesaTransactionEntity): Long
    
    @Update
    suspend fun updateTransaction(transaction: MpesaTransactionEntity)
}
