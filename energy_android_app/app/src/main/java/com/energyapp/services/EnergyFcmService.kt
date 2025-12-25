package com.energyapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging Service for Real-Time Payment Notifications
 * 
 * Features:
 * - Instant M-Pesa payment status notifications
 * - Multi-station support (station-specific notifications)
 * - Rich notifications with payment details
 * - Token management for device registration
 * 
 * Notification Types:
 * - payment_success: Payment completed successfully
 * - payment_failed: Payment failed
 * - payment_cancelled: Payment cancelled by user
 * - shift_reminder: Shift change reminders
 * - station_alert: Station-wide alerts
 */
class EnergyFcmService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "EnergyFcmService"
        
        // Notification Channels
        const val CHANNEL_PAYMENTS = "mpesa_payments"
        const val CHANNEL_SHIFTS = "shift_notifications"
        const val CHANNEL_ALERTS = "station_alerts"
        
        // Shared preference key for FCM token
        const val PREF_FCM_TOKEN = "fcm_token"
        
        // Current FCM token (accessible from app)
        var currentToken: String? = null
            private set
        
        // Listener for token refresh
        var onTokenRefreshListener: ((String) -> Unit)? = null
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 New FCM Token received: ${token.take(20)}...")
        
        currentToken = token
        
        // Save token to shared preferences
        saveTokenToPrefs(token)
        
        // Notify listener (so app can send to server)
        onTokenRefreshListener?.invoke(token)
        
        // Send token to backend
        sendRegistrationToServer(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "📬 FCM Message received from: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "📦 Message data: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "📢 Notification: ${notification.title} - ${notification.body}")
            showNotification(
                title = notification.title ?: "Energy App",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"] ?: "general"
        
        when (messageType) {
            "payment_status" -> handlePaymentNotification(data)
            "shift_reminder" -> handleShiftReminder(data)
            "station_alert" -> handleStationAlert(data)
            else -> {
                // Generic notification
                showNotification(
                    title = data["title"] ?: "Energy App",
                    body = data["body"] ?: "",
                    data = data
                )
            }
        }
    }
    
    private fun handlePaymentNotification(data: Map<String, String>) {
        val status = data["status"] ?: "unknown"
        val amount = data["amount"] ?: "0"
        val receipt = data["receipt"] ?: ""
        val checkoutId = data["checkout_request_id"] ?: ""
        
        val (title, body, icon) = when (status) {
            "completed" -> Triple(
                "✅ Payment Successful!",
                "KES $amount received. Receipt: $receipt",
                android.R.drawable.ic_dialog_info
            )
            "cancelled" -> Triple(
                "❌ Payment Cancelled",
                "Customer cancelled the KES $amount payment",
                android.R.drawable.ic_dialog_alert
            )
            "failed" -> Triple(
                "❌ Payment Failed",
                "KES $amount payment failed: ${data["result_desc"] ?: "Unknown error"}",
                android.R.drawable.ic_dialog_alert
            )
            else -> Triple(
                "💳 Payment Update",
                "Status: $status for KES $amount",
                android.R.drawable.ic_dialog_info
            )
        }
        
        // Show rich notification
        showPaymentNotification(
            title = title,
            body = body,
            amount = amount,
            receipt = receipt,
            status = status,
            checkoutId = checkoutId
        )
        
        // Broadcast to app for UI update
        broadcastPaymentUpdate(status, checkoutId, receipt, amount)
    }
    
    private fun handleShiftReminder(data: Map<String, String>) {
        val shiftName = data["shift_name"] ?: "Shift"
        val action = data["action"] ?: "reminder"
        
        val body = when (action) {
            "start" -> "Your $shiftName is starting soon. Please prepare."
            "end" -> "Your $shiftName is ending. Please complete your tasks."
            else -> "Shift reminder: $shiftName"
        }
        
        showNotification(
            title = "⏰ Shift Reminder",
            body = body,
            channelId = CHANNEL_SHIFTS,
            data = data
        )
    }
    
    private fun handleStationAlert(data: Map<String, String>) {
        val stationName = data["station_name"] ?: "Station"
        val alertType = data["alert_type"] ?: "info"
        val message = data["message"] ?: "Station alert"
        
        val title = when (alertType) {
            "warning" -> "⚠️ $stationName Alert"
            "critical" -> "🚨 URGENT: $stationName"
            else -> "ℹ️ $stationName Update"
        }
        
        showNotification(
            title = title,
            body = message,
            channelId = CHANNEL_ALERTS,
            data = data
        )
    }
    
    private fun showPaymentNotification(
        title: String,
        body: String,
        amount: String,
        receipt: String,
        status: String,
        checkoutId: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", "payment")
            putExtra("checkout_id", checkoutId)
            putExtra("status", status)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_PAYMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(100, 200, 100, 200))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
        
        // Add expanded style for more details
        if (status == "completed" && receipt.isNotEmpty()) {
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Amount: KES $amount\nReceipt: $receipt\n\nTap to view transaction details.")
                    .setBigContentTitle(title)
            )
        }
        
        // Use checkoutId hash as notification ID for uniqueness
        val notificationId = checkoutId.hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "📱 Payment notification shown: $title")
    }
    
    private fun showNotification(
        title: String,
        body: String,
        channelId: String = CHANNEL_PAYMENTS,
        data: Map<String, String> = emptyMap()
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data.forEach { (key, value) -> putExtra(key, value) }
        } ?: Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            launchIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Payment notifications channel (High priority)
            val paymentChannel = NotificationChannel(
                CHANNEL_PAYMENTS,
                "M-Pesa Payments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for M-Pesa payment status updates"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 100, 200)
            }
            
            // Shift notifications channel
            val shiftChannel = NotificationChannel(
                CHANNEL_SHIFTS,
                "Shift Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for shift changes and reminders"
            }
            
            // Station alerts channel
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Station Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important station-wide alerts and updates"
            }
            
            notificationManager.createNotificationChannels(
                listOf(paymentChannel, shiftChannel, alertsChannel)
            )
        }
    }
    
    private fun broadcastPaymentUpdate(
        status: String,
        checkoutId: String,
        receipt: String,
        amount: String
    ) {
        val intent = Intent("com.energyapp.PAYMENT_UPDATE").apply {
            putExtra("status", status)
            putExtra("checkout_request_id", checkoutId)
            putExtra("receipt", receipt)
            putExtra("amount", amount)
        }
        
        sendBroadcast(intent)
        Log.d(TAG, "📡 Payment update broadcast sent: $status")
    }
    
    private fun saveTokenToPrefs(token: String) {
        getSharedPreferences("energy_app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_FCM_TOKEN, token)
            .apply()
    }
    
    private fun sendRegistrationToServer(token: String) {
        // This would be called to register the token with the backend
        // For now, we'll let the app handle this via the listener
        Log.d(TAG, "📤 Token ready to send to server")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Token will be sent via OptimizedMpesaService.setFcmToken()
                // which includes it in STK push requests
                Log.d(TAG, "Token registration will be handled by app")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register token: ${e.message}")
            }
        }
    }
}

/**
 * Helper object to manage FCM token from the app
 */
object FcmTokenManager {
    private const val TAG = "FcmTokenManager"
    
    /**
     * Get the current FCM token
     */
    fun getToken(context: Context): String? {
        // First check the cached token
        EnergyFcmService.currentToken?.let { return it }
        
        // Then check shared preferences
        return context.getSharedPreferences("energy_app_prefs", Context.MODE_PRIVATE)
            .getString(EnergyFcmService.PREF_FCM_TOKEN, null)
    }
    
    /**
     * Set listener for token refresh events
     */
    fun setOnTokenRefreshListener(listener: (String) -> Unit) {
        EnergyFcmService.onTokenRefreshListener = listener
    }
    
    /**
     * Request a new FCM token (call during app initialization)
     */
    fun refreshToken(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d(TAG, "FCM Token refreshed: ${token.take(20)}...")
                        onSuccess(token)
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.exception)
                        task.exception?.let { onFailure(it) }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception refreshing token: ${e.message}")
            onFailure(e)
        }
    }
}
