package com.energyapp.util

object Constants {
    // User Roles
    const val ROLE_ADMIN = "Admin"
    const val ROLE_PUMP_ATTENDANT = "Pump Attendant"

    // Transaction Status
    const val TRANSACTION_SUCCESS = "SUCCESS"
    const val TRANSACTION_FAILED = "FAILED"
    const val TRANSACTION_PENDING = "PENDING"

    // Preferences Keys
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_FULL_NAME = "full_name"
    const val PREF_ROLE_NAME = "role_name"
    const val PREF_IS_LOGGED_IN = "is_logged_in"

    // Date Formats
    const val DATE_FORMAT_FULL = "dd MMM yyyy, hh:mm a"
    const val DATE_FORMAT_SHORT = "dd/MM/yyyy"
    const val TIME_FORMAT = "hh:mm a"

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val KENYAN_MOBILE_REGEX = "^(07|\\+?2547)\\d{8}$"

    // M-Pesa
    const val MPESA_TILL_NUMBER = "174379"
    const val MPESA_SIMULATION_DELAY_MS = 2000L
}
