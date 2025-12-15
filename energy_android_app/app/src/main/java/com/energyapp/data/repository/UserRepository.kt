package com.energyapp.data.repository

import com.energyapp.data.local.dao.UserDao
import com.energyapp.data.local.dao.UserRoleDao
import com.energyapp.data.local.entity.UserEntity
import com.energyapp.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

class UserRepository(
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao
) {
    fun getAllActiveUsers(): Flow<List<User>> {
        return userDao.getAllActiveUsers().map { entities ->
            entities.map { entity ->
                val role = userRoleDao.getRoleById(entity.roleId)
                User(
                    userId = entity.userId,
                    fullName = entity.fullName,
                    username = entity.username,
                    mobileNo = entity.mobileNo,
                    roleId = entity.roleId,
                    roleName = role?.roleName ?: "",
                    isActive = entity.isActive
                )
            }
        }
    }

    suspend fun getUserById(userId: Int): User? {
        val entity = userDao.getUserById(userId) ?: return null
        val role = userRoleDao.getRoleById(entity.roleId)
        return User(
            userId = entity.userId,
            fullName = entity.fullName,
            username = entity.username,
            mobileNo = entity.mobileNo,
            roleId = entity.roleId,
            roleName = role?.roleName ?: "",
            isActive = entity.isActive
        )
    }

    suspend fun authenticateUser(username: String, password: String): User? {
        val entity = userDao.getActiveUserByUsername(username) ?: return null
        val hashedPassword = hashPassword(password)
        
        if (entity.passwordHash != hashedPassword) {
            return null
        }
        
        val role = userRoleDao.getRoleById(entity.roleId)
        return User(
            userId = entity.userId,
            fullName = entity.fullName,
            username = entity.username,
            mobileNo = entity.mobileNo,
            roleId = entity.roleId,
            roleName = role?.roleName ?: "",
            isActive = entity.isActive
        )
    }

    suspend fun createUser(
        fullName: String,
        username: String,
        password: String,
        mobileNo: String?,
        roleName: String
    ): Result<Long> {
        return try {
            val role = userRoleDao.getRoleByName(roleName)
                ?: return Result.failure(Exception("Role not found"))
            
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                return Result.failure(Exception("Username already exists"))
            }
            
            val user = UserEntity(
                fullName = fullName,
                username = username,
                passwordHash = hashPassword(password),
                mobileNo = mobileNo,
                roleId = role.roleId,
                isActive = true
            )
            
            val userId = userDao.insertUser(user)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(
        userId: Int,
        fullName: String,
        mobileNo: String?,
        isActive: Boolean
    ): Result<Unit> {
        return try {
            val existingUser = userDao.getUserById(userId)
                ?: return Result.failure(Exception("User not found"))
            
            val updatedUser = existingUser.copy(
                fullName = fullName,
                mobileNo = mobileNo,
                isActive = isActive
            )
            
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserPassword(userId: Int, newPassword: String): Result<Unit> {
        return try {
            val existingUser = userDao.getUserById(userId)
                ?: return Result.failure(Exception("User not found"))
            
            val updatedUser = existingUser.copy(
                passwordHash = hashPassword(newPassword)
            )
            
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: Int): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
                ?: return Result.failure(Exception("User not found"))
            
            userDao.deleteUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
