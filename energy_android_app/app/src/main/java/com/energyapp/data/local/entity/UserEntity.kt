package com.energyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_roles")
data class UserRoleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "role_id")
    val roleId: Int = 0,
    
    @ColumnInfo(name = "role_name")
    val roleName: String
)

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = UserRoleEntity::class,
            parentColumns = ["role_id"],
            childColumns = ["role_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["username"], unique = true), Index(value = ["role_id"])]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Int = 0,
    
    @ColumnInfo(name = "full_name")
    val fullName: String,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    
    @ColumnInfo(name = "mobile_no")
    val mobileNo: String?,
    
    @ColumnInfo(name = "role_id")
    val roleId: Int,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
