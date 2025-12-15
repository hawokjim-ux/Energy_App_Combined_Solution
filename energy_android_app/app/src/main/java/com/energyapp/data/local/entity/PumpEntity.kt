package com.energyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pumps",
    indices = [Index(value = ["pump_no"], unique = true)]
)
data class PumpEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pump_id")
    val pumpId: Int = 0,
    
    @ColumnInfo(name = "pump_no")
    val pumpNo: String,
    
    @ColumnInfo(name = "pump_name")
    val pumpName: String,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

@Entity(
    tableName = "shifts",
    indices = [Index(value = ["shift_name"], unique = true)]
)
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "shift_id")
    val shiftId: Int = 0,
    
    @ColumnInfo(name = "shift_name")
    val shiftName: String
)
