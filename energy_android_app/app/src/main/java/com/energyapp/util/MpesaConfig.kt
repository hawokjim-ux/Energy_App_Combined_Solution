package com.energyapp.util

import java.text.SimpleDateFormat
import java.util.*

object MpesaConfig {

    const val SUPABASE_URL = "https://pxcdaivlvltmdifxietb.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB4Y2RhaXZsdmx0bWRpZnhpZXRiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDI3NDIsImV4cCI6MjA4MTMxODc0Mn0.s6nv24s6M83gAcW_nSKCBfcXcqJ_7owwqdObPDT7Ky0"

    // *** CRITICAL CHANGE: RENDER BASE URL ADDED ***
    const val RENDER_BASE_URL = "https://online-link.onrender.com/"

    // *** ENDPOINT UPDATES: POINTING TO RENDER PHP FILES ***
    const val STK_PUSH_ENDPOINT = "${RENDER_BASE_URL}stkpush.php"
    const val CHECK_STATUS_ENDPOINT = "${RENDER_BASE_URL}check_status.php"
    const val CALLBACK_ENDPOINT = "${RENDER_BASE_URL}callback.php" // Still used by M-Pesa, but kept for reference

    const val STK_PUSH_TIMEOUT = 30
    const val POLLING_TIMEOUT = 120
    const val POLLING_INTERVAL = 5000
    const val MAX_POLLING_ATTEMPTS = 24

    fun isValidKenyanPhone(phone: String): Boolean {
        val cleaned = phone.replace("+", "").replace(" ", "").replace("-", "").trim()
        return when {
            cleaned.matches(Regex("^07\\d{8}$")) -> true
            cleaned.matches(Regex("^254\\d{9}$")) -> true
            else -> false
        }
    }

    fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("+", "").replace(" ", "").replace("-", "").trim()
        return when {
            cleaned.startsWith("254") -> cleaned
            cleaned.startsWith("0") -> "254${cleaned.substring(1)}"
            cleaned.startsWith("7") -> "254$cleaned"
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