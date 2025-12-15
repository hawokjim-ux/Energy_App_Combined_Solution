package com.energyapp.data.local.dao

import androidx.room.*
import com.energyapp.data.local.entity.UserEntity
import com.energyapp.data.local.entity.UserRoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRoleDao {
    @Query("SELECT * FROM user_roles")
    fun getAllRoles(): Flow<List<UserRoleEntity>>
    
    @Query("SELECT * FROM user_roles WHERE role_id = :roleId")
    suspend fun getRoleById(roleId: Int): UserRoleEntity?
    
    @Query("SELECT * FROM user_roles WHERE role_name = :roleName")
    suspend fun getRoleByName(roleName: String): UserRoleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: UserRoleEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<UserRoleEntity>)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE is_active = 1")
    fun getAllActiveUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE username = :username AND is_active = 1")
    suspend fun getActiveUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE role_id = :roleId AND is_active = 1")
    fun getUsersByRole(roleId: Int): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("UPDATE users SET is_active = :isActive WHERE user_id = :userId")
    suspend fun updateUserActiveStatus(userId: Int, isActive: Boolean)
}
