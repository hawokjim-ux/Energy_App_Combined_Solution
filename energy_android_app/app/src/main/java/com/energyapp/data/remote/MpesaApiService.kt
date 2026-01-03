package com.energyapp.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * DEPRECATED: This service is no longer used
 *
 * We now use MpesaBackendService which calls the Railway backend
 * The Railway backend (stkpush.php) handles all Safaricom M-Pesa communication
 *
 * This keeps M-Pesa credentials secure on the server instead of in the Android APK
 */

@Singleton
class MpesaApiService {
    private val TAG = "MpesaApiService"

    init {
        Log.d(TAG, "⚠️ DEPRECATED: MpesaApiService is no longer used")
        Log.d(TAG, "Use MpesaBackendService instead (calls Railway backend)")
    }

    /**
     * Validate Kenyan phone number
     * Supports all formats:
     * - 07XX XXX XXX (original Safaricom, Airtel, Telkom)
     * - 011X XXX XXX (new Safaricom: 0110-0115)
     * - 010X XXX XXX (new Airtel: 0100-0109)
     * - 254XXXXXXXXX (international format)
     */
    fun isValidKenyanPhone(phone: String): Boolean {
        val formatted = formatPhoneNumber(phone)
        // Matches: 2547xxxxxxxx, 25410xxxxxxx, 25411xxxxxxx
        return formatted.matches(Regex("^254(7\\d{8}|1[01]\\d{7})$"))
    }

    /**
     * Format phone number to M-Pesa format (254XXXXXXXXX)
     * Handles all Kenyan phone number formats
     */
    private fun formatPhoneNumber(phone: String): String {
        var cleaned = phone.replace("+", "")
            .replace(" ", "")
            .replace("-", "")
            .trim()

        return when {
            cleaned.startsWith("254") -> cleaned
            cleaned.startsWith("0") -> "254${cleaned.substring(1)}"
            cleaned.startsWith("7") || cleaned.startsWith("1") -> "254$cleaned"
            else -> cleaned
        }
    }
}