package com.energyapp.util

import java.text.SimpleDateFormat
import java.util.*

object MpesaConfig {

    const val SUPABASE_URL = "https://pxcdaivlvltmdifxietb.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB4Y2RhaXZsdmx0bWRpZnhpZXRiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDI3NDIsImV4cCI6MjA4MTMxODc0Mn0.s6nv24s6M83gAcW_nSKCBfcXcqJ_7owwqdObPDT7Ky0"

    // *** SUPABASE EDGE FUNCTIONS (NO COLD STARTS!) ***
    const val SUPABASE_FUNCTIONS_URL = "${SUPABASE_URL}/functions/v1/"
    
    // *** EDGE FUNCTION ENDPOINTS ***
    const val STK_PUSH_ENDPOINT = "${SUPABASE_FUNCTIONS_URL}stkpush"
    const val CHECK_STATUS_ENDPOINT = "${SUPABASE_FUNCTIONS_URL}check-status"
    const val CALLBACK_ENDPOINT = "${SUPABASE_FUNCTIONS_URL}callback"
    
    // Legacy Render URL (kept for reference, no longer used)
    const val RENDER_BASE_URL = "https://online-link.onrender.com/"

    const val STK_PUSH_TIMEOUT = 30
    const val POLLING_TIMEOUT = 120
    const val POLLING_INTERVAL = 3000  // Faster polling (3 seconds)
    const val MAX_POLLING_ATTEMPTS = 20

    /**
     * Validate Kenyan phone number
     * Supports all formats:
     * - 07XX XXX XXX (original Safaricom, Airtel, Telkom)
     * - 011X XXX XXX (new Safaricom: 0110-0115)
     * - 010X XXX XXX (new Airtel: 0100-0109)
     * - 254XXXXXXXXX (international format)
     */
    fun isValidKenyanPhone(phone: String): Boolean {
        val cleaned = phone.replace("+", "").replace(" ", "").replace("-", "").trim()
        return when {
            // Traditional 07xx format (Safaricom, Airtel, Telkom)
            cleaned.matches(Regex("^07\\d{8}$")) -> true
            // New Safaricom 011x format (0110-0115)
            cleaned.matches(Regex("^011[0-5]\\d{6}$")) -> true
            // New Airtel 010x format (0100-0109)
            cleaned.matches(Regex("^010[0-9]\\d{6}$")) -> true
            // International format with 2547x
            cleaned.matches(Regex("^2547\\d{8}$")) -> true
            // International format with 25410x or 25411x
            cleaned.matches(Regex("^2541[01]\\d{7}$")) -> true
            else -> false
        }
    }

    /**
     * Format phone number to M-Pesa format (254XXXXXXXXX)
     * Handles all Kenyan phone number formats
     */
    fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("+", "").replace(" ", "").replace("-", "").trim()
        return when {
            // Already in international format
            cleaned.startsWith("254") -> cleaned
            // Local format starting with 0 (07x, 010x, 011x)
            cleaned.startsWith("0") -> "254${cleaned.substring(1)}"
            // Without leading 0 (7x, 10x, 11x)
            cleaned.startsWith("7") || cleaned.startsWith("1") -> "254$cleaned"
            else -> cleaned
        }
    }

    fun getDisplayPhoneNumber(phone: String): String {
        val formatted = formatPhoneNumber(phone)
        return if (formatted.startsWith("254")) {
            "0${formatted.substring(3)}"
        } else {
            formatted
        }
    }
}

object MpesaResultCodes {
    const val SUCCESS = 0
    const val INSUFFICIENT_FUNDS = 1
    const val CANCELLED_BY_USER = 1032
    const val TRANSACTION_TIMEOUT = 1037
    const val INVALID_PHONE_NUMBER = 2001

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

    fun isSuccess(code: Int?): Boolean = code == SUCCESS
    fun isPending(code: Int?): Boolean = code == null
    fun isFailure(code: Int?): Boolean = code != null && code != SUCCESS
}

enum class TransactionStatus {
    PENDING, SUCCESS, FAILED, TIMEOUT;

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

object MpesaDateUtils {
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

object MpesaAmountUtils {
    fun formatAmount(amount: Double?): String {
        if (amount == null) return "KES 0.00"
        return try {
            val format = java.text.DecimalFormat("#,##0.00")
            "KES ${format.format(amount)}"
        } catch (e: Exception) {
            "KES $amount"
        }
    }

    fun formatPlainAmount(amount: Double?): String {
        if (amount == null) return "0.00"
        return try {
            val format = java.text.DecimalFormat("#,##0.00")
            format.format(amount)
        } catch (e: Exception) {
            "$amount"
        }
    }

    fun isValidAmount(amount: Double?): Boolean {
        return amount != null && amount > 0 && amount < 1000000
    }
}