package com.energyapp.util

import android.util.Base64
import java.text.SimpleDateFormat
import java.util.*

/**
 * M-Pesa Configuration and Utilities
 *
 * Backend: Render (https://mpesa-webhook-ndom.onrender.com)
 * Database: Supabase PostgreSQL
 * Payment Provider: Safaricom M-Pesa
 */
object MpesaConfig {

    // ==================== RENDER BACKEND ENDPOINTS ====================
    // All M-Pesa logic is handled by Render PHP backend
    // Render connects to Supabase PostgreSQL database

    const val BACKEND_BASE_URL = "https://mpesa-webhook-production.up.railway.app/"

    // STK Push - Initiate M-Pesa payment
    const val STK_PUSH_ENDPOINT = "${BACKEND_BASE_URL}stkpush.php"

    // Check Status - Get transaction result
    const val CHECK_STATUS_ENDPOINT = "${BACKEND_BASE_URL}check_status.php"

    // Dashboard - Get all transactions
    const val TRANSACTIONS_DASHBOARD_ENDPOINT = "${BACKEND_BASE_URL}transactions_dashboard.php"

    // ==================== M-PESA TIMEOUT & RETRY SETTINGS ====================

    /**
     * How long to wait for STK Push response (seconds)
     */
    const val STK_PUSH_TIMEOUT = 30

    /**
     * How long to poll for payment result (seconds)
     */
    const val POLLING_TIMEOUT = 120 // 2 minutes

    /**
     * How often to poll for payment status (milliseconds)
     */
    const val POLLING_INTERVAL = 5000 // 5 seconds

    /**
     * Maximum number of polling attempts
     */
    const val MAX_POLLING_ATTEMPTS = 24 // 24 * 5 seconds = 2 minutes

    // ==================== PHONE NUMBER VALIDATION ====================

    /**
     * Validate if phone number is valid Kenyan format
     *
     * Accepts:
     * - 07XXXXXXXX (10 digits starting with 07)
     * - 2547XXXXXXXX (12 digits starting with 254)
     */
    fun isValidKenyanPhone(phone: String): Boolean {
        val cleaned = phone.replace("+", "")
            .replace(" ", "")
            .replace("-", "")
            .trim()

        return when {
            cleaned.matches(Regex("^07\\d{8}$")) -> true      // 07XXXXXXXX
            cleaned.matches(Regex("^2547\\d{8}$")) -> true    // 2547XXXXXXXX
            else -> false
        }
    }

    /**
     * Format phone number to M-Pesa format (2547XXXXXXXX)
     *
     * Converts:
     * - 07XXXXXXXX → 2547XXXXXXXX
     * - 2547XXXXXXXX → 2547XXXXXXXX (unchanged)
     * - 7XXXXXXXX → 2547XXXXXXXX
     */
    fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("+", "")
            .replace(" ", "")
            .replace("-", "")
            .trim()

        return when {
            cleaned.startsWith("254") -> cleaned           // Already in M-Pesa format
            cleaned.startsWith("0") -> "254${cleaned.substring(1)}"  // Remove leading 0
            cleaned.startsWith("7") -> "254$cleaned"       // Add country code
            else -> cleaned                                 // Return as-is (fallback)
        }
    }

    /**
     * Get display format phone number (07XXXXXXXX)
     */
    fun getDisplayPhoneNumber(phone: String): String {
        val formatted = formatPhoneNumber(phone)
        return if (formatted.startsWith("254")) {
            "0${formatted.substring(3)}"
        } else {
            formatted
        }
    }
}

/**
 * M-Pesa Result Codes from Safaricom
 */
object MpesaResultCodes {
    const val SUCCESS = 0
    const val INSUFFICIENT_FUNDS = 1
    const val CANCELLED_BY_USER = 1032
    const val TRANSACTION_TIMEOUT = 1037
    const val INVALID_PHONE_NUMBER = 2001

    /**
     * Get user-friendly description for result code
     */
    fun getResultDescription(code: Int?): String {
        return when (code) {
            SUCCESS -> "Transaction completed successfully"
            INSUFFICIENT_FUNDS -> "Insufficient funds in account"
            CANCELLED_BY_USER -> "Transaction cancelled by user"
            TRANSACTION_TIMEOUT -> "Transaction timeout"
            INVALID_PHONE_NUMBER -> "Invalid phone number"
            null -> "Pending - awaiting payment confirmation"
            else -> "Transaction failed (Code: $code)"
        }
    }

    /**
     * Check if result code indicates success
     */
    fun isSuccess(code: Int?): Boolean = code == SUCCESS

    /**
     * Check if result code indicates pending
     */
    fun isPending(code: Int?): Boolean = code == null

    /**
     * Check if result code indicates failure
     */
    fun isFailure(code: Int?): Boolean = code != null && code != SUCCESS
}

/**
 * M-Pesa Transaction Status
 */
enum class TransactionStatus {
    PENDING,    // Waiting for payment
    SUCCESS,    // Payment completed
    FAILED,     // Payment failed
    TIMEOUT;    // Payment confirmation timeout

    companion object {
        fun fromResultCode(code: Int?): TransactionStatus {
            return when {
                code == null -> PENDING
                code == 0 -> SUCCESS
                else -> FAILED
            }
        }
    }
}

/**
 * Date/Time utilities for M-Pesa
 */
object MpesaDateUtils {

    /**
     * Format date for display (e.g., "13 Dec 2025, 16:35")
     */
    fun formatTransactionDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString

            val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            displayFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Get time elapsed since transaction
     * e.g., "5 minutes ago", "2 hours ago"
     */
    fun getTimeAgo(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString

            val now = Date()
            val diffMs = now.time - date.time
            val diffSec = diffMs / 1000
            val diffMin = diffSec / 60
            val diffHour = diffMin / 60
            val diffDay = diffHour / 24

            return when {
                diffSec < 60 -> "just now"
                diffMin < 60 -> "$diffMin minute${if (diffMin > 1) "s" else ""} ago"
                diffHour < 24 -> "$diffHour hour${if (diffHour > 1) "s" else ""} ago"
                diffDay < 7 -> "$diffDay day${if (diffDay > 1) "s" else ""} ago"
                else -> formatTransactionDate(dateString)
            }
        } catch (e: Exception) {
            dateString
        }
    }
}

/**
 * Amount formatting utilities
 */
object MpesaAmountUtils {

    /**
     * Format amount as KES currency
     * e.g., 1000.0 → "KES 1,000.00"
     */
    fun formatAmount(amount: Double?): String {
        if (amount == null) return "KES 0.00"

        return try {
            val format = java.text.DecimalFormat("#,##0.00")
            "KES ${format.format(amount)}"
        } catch (e: Exception) {
            "KES $amount"
        }
    }

    /**
     * Format amount without currency
     * e.g., 1000.0 → "1,000.00"
     */
    fun formatPlainAmount(amount: Double?): String {
        if (amount == null) return "0.00"

        return try {
            val format = java.text.DecimalFormat("#,##0.00")
            format.format(amount)
        } catch (e: Exception) {
            "$amount"
        }
    }

    /**
     * Validate amount is positive and reasonable
     */
    fun isValidAmount(amount: Double?): Boolean {
        return amount != null && amount > 0 && amount < 1000000
    }
}