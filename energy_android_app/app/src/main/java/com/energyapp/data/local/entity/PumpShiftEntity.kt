package com.energyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pump_shifts",
    foreignKeys = [
        ForeignKey(
            entity = PumpEntity::class,
            parentColumns = ["pump_id"],
            childColumns = ["pump_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShiftEntity::class,
            parentColumns = ["shift_id"],
            childColumns = ["shift_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["opening_attendant_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["closing_attendant_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["pump_id"]),
        Index(value = ["shift_id"]),
        Index(value = ["opening_attendant_id"]),
        Index(value = ["closing_attendant_id"])
    ]
)
data class PumpShiftEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pump_shift_id")
    val pumpShiftId: Int = 0,
    
    @ColumnInfo(name = "pump_id")
    val pumpId: Int,
    
    @ColumnInfo(name = "shift_id")
    val shiftId: Int,
    
    @ColumnInfo(name = "opening_attendant_id")
    val openingAttendantId: Int,
    
    @ColumnInfo(name = "opening_time")
    val openingTime: Long, // Unix timestamp in milliseconds
    
    @ColumnInfo(name = "opening_meter_reading")
    val openingMeterReading: Double,
    
    @ColumnInfo(name = "closing_attendant_id")
    val closingAttendantId: Int? = null,
    
    @ColumnInfo(name = "closing_time")
    val closingTime: Long? = null,
    
    @ColumnInfo(name = "closing_meter_reading")
    val closingMeterReading: Double? = null,
    
    @ColumnInfo(name = "is_closed")
    val isClosed: Boolean = false
)
