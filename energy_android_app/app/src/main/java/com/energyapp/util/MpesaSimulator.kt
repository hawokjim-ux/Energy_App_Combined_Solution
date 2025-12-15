package com.energyapp.util

import com.energyapp.data.model.MpesaResultCode
import kotlin.random.Random

object MpesaSimulator {
    /**
     * Simulates M-Pesa STK Push transaction
     * Returns random outcomes: SUCCESS, INSUFFICIENT_FUNDS, CANCELLED, or OTHER_ERROR
     */
    fun simulateStkPush(mobileNo: String, amount: Double): MpesaResultCode {
        // Randomly select an outcome
        val outcomes = MpesaResultCode.values()
        val randomIndex = Random.nextInt(outcomes.size)
        return outcomes[randomIndex]
    }

    /**
     * Simulates M-Pesa STK Push with weighted probabilities
     * 70% success, 10% insufficient funds, 10% cancelled, 10% other error
     */
    fun simulateStkPushWeighted(mobileNo: String, amount: Double): MpesaResultCode {
        val random = Random.nextInt(100)
        return when {
            random < 70 -> MpesaResultCode.SUCCESS
            random < 80 -> MpesaResultCode.INSUFFICIENT_FUNDS
            random < 90 -> MpesaResultCode.CANCELLED
            else -> MpesaResultCode.OTHER_ERROR
        }
    }

    /**
     * Validates Kenyan mobile number format
     * Accepts formats: 0712345678, 254712345678, +254712345678
     */
    fun isValidKenyanMobile(mobileNo: String): Boolean {
        val cleanNumber = mobileNo.replace("+", "").replace(" ", "")
        return when {
            cleanNumber.matches(Regex("^07\\d{8}$")) -> true // 0712345678
            cleanNumber.matches(Regex("^2547\\d{8}$")) -> true // 254712345678
            else -> false
        }
    }

    /**
     * Formats mobile number to M-Pesa format (2547XXXXXXXX)
     */
    fun formatMobileNumber(mobileNo: String): String {
        val cleanNumber = mobileNo.replace("+", "").replace(" ", "")
        return when {
            cleanNumber.startsWith("07") -> "254${cleanNumber.substring(1)}"
            cleanNumber.startsWith("2547") -> cleanNumber
            else -> cleanNumber
        }
    }
}
