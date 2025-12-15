package com.energyapp.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatTimestamp(timestamp: Long, format: String = Constants.DATE_FORMAT_FULL): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return formatTimestamp(timestamp, Constants.DATE_FORMAT_SHORT)
    }

    fun formatTime(timestamp: Long): String {
        return formatTimestamp(timestamp, Constants.TIME_FORMAT)
    }

    fun formatDateTime(timestamp: Long): String {
        return formatTimestamp(timestamp, Constants.DATE_FORMAT_FULL)
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> formatDate(timestamp)
        }
    }
}
