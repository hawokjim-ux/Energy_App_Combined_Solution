package com.energyapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.energyapp.data.remote.LicenseDbResponse
import com.energyapp.data.remote.SupabaseApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * License Types with durations and device limits
 * Format: ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX
 */
enum class LicenseType(
    val code: String,
    val durationDays: Int,
    val maxDevices: Int,
    val displayName: String,
    val icon: String,
    val colorHex: String
) {
    TRIAL_2_DAYS("T02D", 2, 1, "Trial (2 Days)", "⏰", "#F97316"),
    TRIAL_10_DAYS("T10D", 10, 1, "Trial (10 Days)", "⏰", "#F97316"),
    TRIAL_20_DAYS("T20D", 20, 1, "Trial (20 Days)", "⏰", "#F97316"),
    FULL_2_DEVICES("F02D", 360, 2, "Full (2 Devices)", "💎", "#22C55E"),
    FULL_3_DEVICES("F03D", 360, 3, "Full (3 Devices)", "💎", "#22C55E"),
    FULL_4_DEVICES("F04D", 360, 4, "Full (4 Devices)", "💎", "#22C55E"),
    FULL_5_DEVICES("F05D", 360, 5, "Full (5 Devices)", "💎", "#22C55E"),
    ENTERPRISE("ENTP", 9999, 999, "Enterprise Unlimited", "👑", "#7C3AED");

    companion object {
        fun fromCode(code: String): LicenseType? {
            return values().find { it.code == code }
        }
    }
}

data class LicenseInfo(
    val licenseKey: String = "",
    val licenseType: LicenseType? = null,
    val deviceId: String = "",
    val activationDate: Long = 0,
    val expirationDate: Long = 0,
    val isActivated: Boolean = false,
    val activationCount: Int = 0,
    val maxActivations: Int = 1,
    val clientName: String? = null,
    val clientPhone: String? = null
)

data class LicenseValidationResult(
    val isValid: Boolean,
    val message: String,
    val licenseType: LicenseType? = null,
    val daysRemaining: Int = 0,
    val errorCode: String? = null
)

/**
 * Contact Information for Licensing
 */
object LicenseContact {
    const val COMPANY_NAME = "Hawkinsoft Solutions"
    const val DEVELOPER_NAME = "Jimhawkins Korir"
    const val PHONE = "+254720316175"
    const val EMAIL = "support@hawkinsoft.com"
    const val WEBSITE = "www.hawkinsoft.com"
    
    const val SUPPORT_MESSAGE = "Contact for full licensing or demo"
    
    fun getFullContactInfo(): String {
        return "$DEVELOPER_NAME @ $COMPANY_NAME\n$PHONE\n$SUPPORT_MESSAGE"
    }
}

/**
 * Database-Driven License Manager
 * Uses Supabase for stringent license validation and tracking
 */
@Singleton
class LicenseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val supabaseApiService: SupabaseApiService
) {
    private val TAG = "LicenseManager"
    
    companion object {
        // License format: ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX (uppercase)
        const val LICENSE_PREFIX = "ALPHA-ENERGY-46E4"
        private const val LICENSE_PATTERN = "^ALPHA-ENERGY-46E4-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
        
        // Secret keys for validation
        private const val LICENSE_SALT = "HAWKINSOFT_ENERGY_APP_2024_JIMHAWKINS_KORIR"
        private const val VALIDATION_SECRET = "ALPHA_ENERGY_46E4_MASTER_SECURE_KEY_V2"
        
        // Checksum modifiers
        private val CHECKSUM_FACTORS = intArrayOf(7, 11, 13, 17, 19, 23, 29, 31, 37, 41)
    }

    /**
     * Get unique device identifier
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        
        val deviceInfo = buildString {
            append(androidId)
            append(Build.MANUFACTURER)
            append(Build.MODEL)
            append(Build.DEVICE)
            append(Build.BOARD)
            append(Build.HARDWARE)
        }
        
        return hashString(deviceInfo).take(16).uppercase()
    }

    fun getDeviceInfo(): String = "${Build.MANUFACTURER} ${Build.MODEL}"
    fun getDeviceManufacturer(): String = Build.MANUFACTURER
    fun getDeviceModel(): String = Build.MODEL

    /**
     * Generate a valid license key and save to database
     */
    suspend fun generateLicenseKey(
        licenseType: LicenseType,
        clientName: String? = null,
        clientPhone: String? = null
    ): String {
        val random = UUID.randomUUID().toString().replace("-", "").uppercase()
        
        val part1 = licenseType.code
        val part2 = random.take(4)
        val dataToCheck = part1 + part2 + LICENSE_SALT
        val part3 = calculateChecksum(dataToCheck)
        
        // IMPORTANT: Save license key in UPPERCASE to match query format during validation
        val licenseKey = "$LICENSE_PREFIX-$part1-$part2-$part3".uppercase()
        
        // Save to database
        try {
            val result = supabaseApiService.createLicenseInDb(
                licenseKey = licenseKey,
                licenseType = licenseType.code,
                clientName = clientName,
                clientPhone = clientPhone,
                durationDays = licenseType.durationDays,
                maxDevices = licenseType.maxDevices
            )
            if (result.isSuccess) {
                Log.d(TAG, "✅ License saved to database: $licenseKey")
            } else {
                Log.e(TAG, "❌ Failed to save license to database")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving license: ${e.message}")
        }
        
        return licenseKey
    }

    /**
     * Validate license key format
     */
    fun validateLicenseFormat(licenseKey: String): Boolean {
        val cleanKey = licenseKey.trim().uppercase()
        return cleanKey.matches(Regex(LICENSE_PATTERN, RegexOption.IGNORE_CASE))
    }

    /**
     * Full license validation with DATABASE check (stringent!)
     */
    suspend fun validateLicense(licenseKey: String, deviceId: String): LicenseValidationResult {
        val cleanKey = licenseKey.trim().uppercase()
        
        Log.d(TAG, "🔍 Validating license: $cleanKey")
        
        // Step 1: Check prefix
        if (!cleanKey.startsWith(LICENSE_PREFIX.uppercase())) {
            return LicenseValidationResult(
                isValid = false,
                message = "Invalid license. Must start with ALPHA-ENERGY-46E4",
                errorCode = "PREFIX_ERROR"
            )
        }

        // Step 2: Format validation
        if (!validateLicenseFormat(cleanKey)) {
            return LicenseValidationResult(
                isValid = false,
                message = "Invalid license format.\nFormat: ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX",
                errorCode = "FORMAT_ERROR"
            )
        }

        // Step 3: Extract parts for checksum validation
        val prefixToRemove = "${LICENSE_PREFIX.uppercase()}-"
        val withoutPrefix = cleanKey.removePrefix(prefixToRemove)
        val parts = withoutPrefix.split("-")
        
        if (parts.size != 3) {
            return LicenseValidationResult(
                isValid = false,
                message = "License structure is invalid.\nExpected format: ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX",
                errorCode = "STRUCTURE_ERROR"
            )
        }

        // Step 4: Verify checksum
        val dataToCheck = parts[0] + parts[1] + LICENSE_SALT
        val expectedChecksum = calculateChecksum(dataToCheck)
        
        if (parts[2] != expectedChecksum) {
            return LicenseValidationResult(
                isValid = false,
                message = "License verification failed.\nKey may be invalid or tampered.\nContact: ${LicenseContact.PHONE}",
                errorCode = "CHECKSUM_ERROR"
            )
        }

        // Step 5: DATABASE VALIDATION (Stringent!)
        try {
            val dbResult = supabaseApiService.checkLicenseInDb(cleanKey)
            
            if (dbResult.isFailure) {
                return LicenseValidationResult(
                    isValid = false,
                    message = "Cannot verify license.\nCheck internet connection.",
                    errorCode = "NETWORK_ERROR"
                )
            }
            
            val dbLicense = dbResult.getOrNull()
            
            if (dbLicense == null) {
                return LicenseValidationResult(
                    isValid = false,
                    message = "License not found in system.\nContact: ${LicenseContact.PHONE}",
                    errorCode = "NOT_FOUND"
                )
            }
            
            // Check if revoked
            if (dbLicense.isRevoked) {
                return LicenseValidationResult(
                    isValid = false,
                    message = "This license has been revoked.\nReason: ${dbLicense.revokedReason ?: "Unknown"}\nContact: ${LicenseContact.PHONE}",
                    errorCode = "REVOKED"
                )
            }
            
            // Check if already activated on different device
            if (dbLicense.isActivated && dbLicense.activationDeviceId != null && dbLicense.activationDeviceId != deviceId) {
                return LicenseValidationResult(
                    isValid = false,
                    message = "License already activated on another device.\n(${dbLicense.deviceManufacturer} ${dbLicense.deviceModel})\nContact: ${LicenseContact.PHONE}",
                    errorCode = "ALREADY_ACTIVATED"
                )
            }
            
            val licenseType = LicenseType.fromCode(dbLicense.licenseType)
            
            return LicenseValidationResult(
                isValid = true,
                message = "License validated successfully!",
                licenseType = licenseType,
                daysRemaining = dbLicense.durationDays
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Database validation error: ${e.message}")
            return LicenseValidationResult(
                isValid = false,
                message = "License verification failed.\nContact: ${LicenseContact.PHONE}",
                errorCode = "DB_ERROR"
            )
        }
    }

    /**
     * Activate license on this device (DATABASE-DRIVEN)
     */
    suspend fun activateLicense(licenseKey: String): LicenseValidationResult {
        val deviceId = getDeviceId()
        val cleanKey = licenseKey.trim().uppercase()
        
        Log.d(TAG, "🔓 Activating license: $cleanKey")
        
        // Validate first
        val validation = validateLicense(cleanKey, deviceId)
        if (!validation.isValid) {
            return validation
        }
        
        // Activate in database
        try {
            val activateResult = supabaseApiService.activateLicenseInDb(
                licenseKey = cleanKey,
                deviceId = deviceId,
                deviceManufacturer = getDeviceManufacturer(),
                deviceModel = getDeviceModel()
            )
            
            if (activateResult.isFailure) {
                return LicenseValidationResult(
                    isValid = false,
                    message = activateResult.exceptionOrNull()?.message ?: "Activation failed",
                    errorCode = "ACTIVATION_FAILED"
                )
            }
            
            val dbLicense = activateResult.getOrNull()!!
            val licenseType = LicenseType.fromCode(dbLicense.licenseType)!!
            
            // Calculate expiration from database response
            val expirationMs = try {
                dbLicense.expirationDate?.let { Instant.parse(it).toEpochMilli() } ?: 0L
            } catch (e: Exception) {
                System.currentTimeMillis() + (licenseType.durationDays.toLong() * 24 * 60 * 60 * 1000)
            }
            
            val activationMs = try {
                dbLicense.activationDate?.let { Instant.parse(it).toEpochMilli() } ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
            
            // Save to local preferences for offline access
            preferencesManager.saveLicense(
                licenseKey = cleanKey,
                licenseTypeCode = licenseType.code,
                deviceId = deviceId,
                activationDate = activationMs,
                expirationDate = expirationMs,
                maxDevices = licenseType.maxDevices
            )
            
            Log.d(TAG, "✅ License activated successfully in database")
            
            return LicenseValidationResult(
                isValid = true,
                message = "${licenseType.icon} License activated!\nValid for ${licenseType.durationDays} days\n${licenseType.displayName}",
                licenseType = licenseType,
                daysRemaining = licenseType.durationDays
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Activation error: ${e.message}")
            return LicenseValidationResult(
                isValid = false,
                message = "Activation failed: ${e.message}",
                errorCode = "ACTIVATION_ERROR"
            )
        }
    }

    /**
     * Check if current license is valid (local + database)
     */
    suspend fun isLicenseValid(): Boolean {
        val deviceId = getDeviceId()
        
        // First check local cache
        val localInfo = getLicenseInfo()
        if (!localInfo.isActivated || localInfo.licenseKey.isEmpty()) {
            Log.d(TAG, "❌ No local license found")
            return false
        }
        
        // Check expiration locally
        val currentTime = System.currentTimeMillis()
        if (currentTime >= localInfo.expirationDate) {
            Log.d(TAG, "❌ Local license expired")
            return false
        }
        
        // Check device match
        if (localInfo.deviceId != deviceId) {
            Log.d(TAG, "❌ Device ID mismatch")
            return false
        }
        
        // Verify with database (stringent check)
        try {
            val dbResult = supabaseApiService.checkDeviceLicense(deviceId)
            if (dbResult.isSuccess) {
                val dbLicense = dbResult.getOrNull()
                if (dbLicense == null) {
                    Log.d(TAG, "⚠️ No valid license in database for this device")
                    // Clear local license if not in database
                    preferencesManager.clearLicense()
                    return false
                }
                
                if (dbLicense.isRevoked) {
                    Log.d(TAG, "❌ License has been revoked")
                    preferencesManager.clearLicense()
                    return false
                }
                
                Log.d(TAG, "✅ License verified with database")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Database check failed, using local: ${e.message}")
        }
        
        // Fallback to local check if database unavailable
        return true
    }

    /**
     * Get current license info from local storage
     */
    suspend fun getLicenseInfo(): LicenseInfo {
        val licenseKey = preferencesManager.licenseKey.first() ?: ""
        val typeCode = preferencesManager.licenseTypeCode.first() ?: ""
        val deviceId = preferencesManager.licenseDeviceId.first() ?: ""
        val activationDate = preferencesManager.licenseActivationDate.first() ?: 0L
        val expirationDate = preferencesManager.licenseExpirationDate.first() ?: 0L
        val maxDevices = preferencesManager.licenseMaxDevices.first() ?: 1

        val licenseType = LicenseType.fromCode(typeCode)

        return LicenseInfo(
            licenseKey = licenseKey,
            licenseType = licenseType,
            deviceId = deviceId,
            activationDate = activationDate,
            expirationDate = expirationDate,
            isActivated = licenseKey.isNotEmpty() && expirationDate > 0,
            maxActivations = maxDevices
        )
    }

    /**
     * Get days remaining on license
     */
    suspend fun getDaysRemaining(): Int {
        val licenseInfo = getLicenseInfo()
        if (!licenseInfo.isActivated) return 0

        val currentTime = System.currentTimeMillis()
        val remaining = licenseInfo.expirationDate - currentTime
        
        return maxOf(0, (remaining / (24 * 60 * 60 * 1000)).toInt())
    }

    /**
     * Get all licenses from database
     */
    suspend fun getAllLicensesFromDb(): List<LicenseDbResponse> {
        return try {
            val result = supabaseApiService.getAllLicenses()
            result.getOrNull() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching licenses: ${e.message}")
            emptyList()
        }
    }

    /**
     * Revoke license
     */
    suspend fun revokeLicense(licenseId: Int, reason: String): Boolean {
        return try {
            val result = supabaseApiService.revokeLicenseInDb(licenseId, reason)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error revoking license: ${e.message}")
            false
        }
    }

    /**
     * Clear local license
     */
    suspend fun clearLocalLicense() {
        preferencesManager.clearLicense()
    }

    /**
     * Check if phone number already has a license (for duplicate prevention)
     * Returns the existing license if found, null otherwise
     */
    suspend fun checkPhoneDuplicate(phoneNumber: String): LicenseDbResponse? {
        return try {
            val result = supabaseApiService.checkPhoneDuplicate(phoneNumber)
            result.getOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking phone duplicate: ${e.message}")
            null
        }
    }

    /**
     * Calculate checksum for license validation
     */
    private fun calculateChecksum(data: String): String {
        var checksum = 0L
        
        data.forEachIndexed { index, char ->
            val factor = CHECKSUM_FACTORS[index % CHECKSUM_FACTORS.size]
            checksum += char.code.toLong() * factor * (index + 1)
        }

        val saltHash = hashString(VALIDATION_SECRET)
        checksum += saltHash.take(10).fold(0L) { acc, c -> acc + c.code * 3 }

        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val result = StringBuilder()
        var temp = kotlin.math.abs(checksum)
        repeat(4) {
            result.append(chars[(temp % chars.length).toInt()])
            temp /= chars.length
        }

        return result.toString()
    }

    private fun hashString(input: String): String {
        val combined = input + VALIDATION_SECRET
        val bytes = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        return bytes.joinToString("") { "%02X".format(it) }
    }
}
