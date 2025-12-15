package com.energyapp.data.repository

import com.energyapp.data.local.dao.PumpDao
import com.energyapp.data.local.dao.PumpShiftDao
import com.energyapp.data.local.dao.ShiftDao
import com.energyapp.data.local.dao.UserDao
import com.energyapp.data.local.entity.PumpShiftEntity
import com.energyapp.data.model.Pump
import com.energyapp.data.model.PumpShift
import com.energyapp.data.model.Shift
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShiftRepository(
    private val pumpDao: PumpDao,
    private val shiftDao: ShiftDao,
    private val pumpShiftDao: PumpShiftDao,
    private val userDao: UserDao
) {
    fun getAllPumps(): Flow<List<Pump>> {
        return pumpDao.getAllActivePumps().map { pumps ->
            pumps.map { pump ->
                val currentShift = pumpShiftDao.getCurrentOpenShiftForPump(pump.pumpId)
                Pump(
                    pumpId = pump.pumpId,
                    pumpNo = pump.pumpNo,
                    pumpName = pump.pumpName,
                    isActive = pump.isActive,
                    isShiftOpen = currentShift != null,
                    currentShiftId = currentShift?.pumpShiftId
                )
            }
        }
    }

    fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts().map { entities ->
            entities.map { entity ->
                Shift(
                    shiftId = entity.shiftId,
                    shiftName = entity.shiftName
                )
            }
        }
    }

    fun getAllPumpShifts(): Flow<List<PumpShift>> {
        return pumpShiftDao.getAllPumpShifts().map { entities ->
            entities.mapNotNull { entity ->
                convertToPumpShift(entity)
            }
        }
    }

    fun getOpenShifts(): Flow<List<PumpShift>> {
        return pumpShiftDao.getAllOpenShifts().map { entities ->
            entities.mapNotNull { entity ->
                convertToPumpShift(entity)
            }
        }
    }

    suspend fun openShift(
        pumpId: Int,
        shiftId: Int,
        attendantId: Int,
        openingMeterReading: Double
    ): Result<Int> {
        return try {
            // Check if shift is already open for this pump
            val existingShift = pumpShiftDao.getCurrentOpenShiftForPump(pumpId)
            if (existingShift != null) {
                return Result.failure(Exception("Shift is already open for this pump"))
            }

            val pumpShift = PumpShiftEntity(
                pumpId = pumpId,
                shiftId = shiftId,
                openingAttendantId = attendantId,
                openingTime = System.currentTimeMillis(),
                openingMeterReading = openingMeterReading,
                isClosed = false
            )

            val pumpShiftId = pumpShiftDao.insertPumpShift(pumpShift).toInt()
            Result.success(pumpShiftId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeShift(
        pumpShiftId: Int,
        closingAttendantId: Int,
        closingMeterReading: Double
    ): Result<Unit> {
        return try {
            val shift = pumpShiftDao.getPumpShiftById(pumpShiftId)
                ?: return Result.failure(Exception("Shift not found"))

            if (shift.isClosed) {
                return Result.failure(Exception("Shift is already closed"))
            }

            pumpShiftDao.closeShift(
                pumpShiftId = pumpShiftId,
                closingAttendantId = closingAttendantId,
                closingTime = System.currentTimeMillis(),
                closingMeterReading = closingMeterReading
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentShiftForPump(pumpId: Int): PumpShift? {
        val entity = pumpShiftDao.getCurrentOpenShiftForPump(pumpId) ?: return null
        return convertToPumpShift(entity)
    }

    private suspend fun convertToPumpShift(entity: PumpShiftEntity): PumpShift? {
        val pump = pumpDao.getPumpById(entity.pumpId) ?: return null
        val shift = shiftDao.getShiftById(entity.shiftId) ?: return null
        val openingAttendant = userDao.getUserById(entity.openingAttendantId) ?: return null
        val closingAttendant = entity.closingAttendantId?.let { userDao.getUserById(it) }

        return PumpShift(
            pumpShiftId = entity.pumpShiftId,
            pumpId = entity.pumpId,
            pumpNo = pump.pumpNo,
            pumpName = pump.pumpName,
            shiftId = entity.shiftId,
            shiftName = shift.shiftName,
            openingAttendantId = entity.openingAttendantId,
            openingAttendantName = openingAttendant.fullName,
            openingTime = entity.openingTime,
            openingMeterReading = entity.openingMeterReading,
            closingAttendantId = entity.closingAttendantId,
            closingAttendantName = closingAttendant?.fullName,
            closingTime = entity.closingTime,
            closingMeterReading = entity.closingMeterReading,
            isClosed = entity.isClosed
        )
    }
}
