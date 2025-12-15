package com.energyapp.data.local.dao

import androidx.room.*
import com.energyapp.data.local.entity.PumpEntity
import com.energyapp.data.local.entity.PumpShiftEntity
import com.energyapp.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PumpDao {
    @Query("SELECT * FROM pumps WHERE is_active = 1")
    fun getAllActivePumps(): Flow<List<PumpEntity>>
    
    @Query("SELECT * FROM pumps")
    fun getAllPumps(): Flow<List<PumpEntity>>
    
    @Query("SELECT * FROM pumps WHERE pump_id = :pumpId")
    suspend fun getPumpById(pumpId: Int): PumpEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPump(pump: PumpEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPumps(pumps: List<PumpEntity>)
    
    @Update
    suspend fun updatePump(pump: PumpEntity)
    
    @Delete
    suspend fun deletePump(pump: PumpEntity)
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts")
    fun getAllShifts(): Flow<List<ShiftEntity>>
    
    @Query("SELECT * FROM shifts WHERE shift_id = :shiftId")
    suspend fun getShiftById(shiftId: Int): ShiftEntity?
    
    @Query("SELECT * FROM shifts WHERE shift_name = :shiftName")
    suspend fun getShiftByName(shiftName: String): ShiftEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShifts(shifts: List<ShiftEntity>)
}

@Dao
interface PumpShiftDao {
    @Query("SELECT * FROM pump_shifts ORDER BY opening_time DESC")
    fun getAllPumpShifts(): Flow<List<PumpShiftEntity>>
    
    @Query("SELECT * FROM pump_shifts WHERE pump_shift_id = :pumpShiftId")
    suspend fun getPumpShiftById(pumpShiftId: Int): PumpShiftEntity?
    
    @Query("SELECT * FROM pump_shifts WHERE pump_id = :pumpId AND is_closed = 0 ORDER BY opening_time DESC LIMIT 1")
    suspend fun getCurrentOpenShiftForPump(pumpId: Int): PumpShiftEntity?
    
    @Query("SELECT * FROM pump_shifts WHERE pump_id = :pumpId ORDER BY opening_time DESC")
    fun getShiftsForPump(pumpId: Int): Flow<List<PumpShiftEntity>>
    
    @Query("SELECT * FROM pump_shifts WHERE is_closed = 0")
    fun getAllOpenShifts(): Flow<List<PumpShiftEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPumpShift(pumpShift: PumpShiftEntity): Long
    
    @Update
    suspend fun updatePumpShift(pumpShift: PumpShiftEntity)
    
    @Query("UPDATE pump_shifts SET closing_attendant_id = :closingAttendantId, closing_time = :closingTime, closing_meter_reading = :closingMeterReading, is_closed = 1 WHERE pump_shift_id = :pumpShiftId")
    suspend fun closeShift(
        pumpShiftId: Int,
        closingAttendantId: Int,
        closingTime: Long,
        closingMeterReading: Double
    )
}
