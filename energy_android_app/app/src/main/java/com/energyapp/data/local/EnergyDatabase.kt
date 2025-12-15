package com.energyapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.energyapp.data.local.dao.*
import com.energyapp.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        UserRoleEntity::class,
        UserEntity::class,
        PumpEntity::class,
        ShiftEntity::class,
        PumpShiftEntity::class,
        SalesEntity::class,
        MpesaTransactionEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EnergyDatabase : RoomDatabase() {
    abstract fun userRoleDao(): UserRoleDao
    abstract fun userDao(): UserDao
    abstract fun pumpDao(): PumpDao
    abstract fun shiftDao(): ShiftDao
    abstract fun pumpShiftDao(): PumpShiftDao
    abstract fun salesDao(): SalesDao
    abstract fun mpesaTransactionDao(): MpesaTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: EnergyDatabase? = null

        fun getDatabase(context: Context): EnergyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EnergyDatabase::class.java,
                    "energy_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        private suspend fun populateDatabase(database: EnergyDatabase) {
            val userRoleDao = database.userRoleDao()
            val userDao = database.userDao()
            val pumpDao = database.pumpDao()
            val shiftDao = database.shiftDao()

            // Insert default roles
            val adminRoleId = userRoleDao.insertRole(
                UserRoleEntity(roleName = "Admin")
            ).toInt()
            val attendantRoleId = userRoleDao.insertRole(
                UserRoleEntity(roleName = "Pump Attendant")
            ).toInt()

            // Insert default users with hashed passwords
            userDao.insertUser(
                UserEntity(
                    fullName = "System Administrator",
                    username = "admin",
                    passwordHash = hashPassword("admin123"),
                    mobileNo = "0700123456",
                    roleId = adminRoleId,
                    isActive = true
                )
            )

            userDao.insertUser(
                UserEntity(
                    fullName = "John Doe",
                    username = "attendant1",
                    passwordHash = hashPassword("pass123"),
                    mobileNo = "0711223344",
                    roleId = attendantRoleId,
                    isActive = true
                )
            )

            // Insert default pumps
            pumpDao.insertPumps(
                listOf(
                    PumpEntity(pumpNo = "P1", pumpName = "Pump One", isActive = true),
                    PumpEntity(pumpNo = "P2", pumpName = "Pump Two", isActive = true),
                    PumpEntity(pumpNo = "P3", pumpName = "Pump Three", isActive = true)
                )
            )

            // Insert default shifts
            shiftDao.insertShifts(
                listOf(
                    ShiftEntity(shiftName = "Day Shift"),
                    ShiftEntity(shiftName = "Night Shift")
                )
            )
        }

        private fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
