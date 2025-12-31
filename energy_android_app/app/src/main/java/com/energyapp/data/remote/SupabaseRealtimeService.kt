package com.energyapp.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SUPABASE REALTIME SERVICE
 * 
 * Provides real-time updates for M-Pesa transaction status using WebSockets.
 * This eliminates the need for polling and provides INSTANT payment notifications.
 * 
 * How it works:
 * 1. Connect to Supabase Realtime WebSocket
 * 2. Subscribe to mpesa_transactions table changes
 * 3. Receive instant updates when transaction status changes
 * 4. Emit status updates through Kotlin Flow
 * 
 * @version 1.0.0
 */

// Data class for M-Pesa transaction status update
data class MpesaTransactionUpdate(
    val checkoutRequestId: String,
    val status: String,
    val resultCode: Int?,
    val resultDesc: String?,
    val mpesaReceiptNumber: String?,
    val amount: Double?,
    val completedAt: String?
)

// Sealed class for Realtime connection states
sealed class RealtimeConnectionState {
    object Connecting : RealtimeConnectionState()
    object Connected : RealtimeConnectionState()
    object Disconnected : RealtimeConnectionState()
    data class Error(val message: String) : RealtimeConnectionState()
}

// Sealed class for transaction status events
sealed class TransactionStatusEvent {
    object Pending : TransactionStatusEvent()
    data class Completed(val receiptNumber: String, val amount: Double) : TransactionStatusEvent()
    data class Cancelled(val reason: String) : TransactionStatusEvent()
    data class Failed(val reason: String, val resultCode: Int?) : TransactionStatusEvent()
    data class Timeout(val message: String) : TransactionStatusEvent()
    data class Error(val message: String) : TransactionStatusEvent()
}

@Singleton
class SupabaseRealtimeService @Inject constructor() {
    
    private val TAG = "SupabaseRealtime"
    
    companion object {
        private const val SUPABASE_URL = "pxcdaivlvltmdifxietb.supabase.co"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB4Y2RhaXZsdmx0bWRpZnhpZXRiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDI3NDIsImV4cCI6MjA4MTMxODc0Mn0.s6nv24s6M83gAcW_nSKCBfcXcqJ_7owwqdObPDT7Ky0"
        
        // Realtime WebSocket URL
        private const val REALTIME_URL = "wss://$SUPABASE_URL/realtime/v1/websocket?apikey=$SUPABASE_ANON_KEY&vsn=1.0.0"
        
        // Heartbeat interval (30 seconds)
        private const val HEARTBEAT_INTERVAL_MS = 30000L
        
        // Maximum wait time for transaction confirmation (90 seconds)
        private const val MAX_WAIT_TIME_MS = 90000L
    }
    
    private val gson = Gson()
    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .addInterceptor(HttpLoggingInterceptor { message ->
            Log.d(TAG, "WS: $message")
        }.apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()
    
    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var timeoutJob: Job? = null
    
    private val _connectionState = MutableStateFlow<RealtimeConnectionState>(RealtimeConnectionState.Disconnected)
    val connectionState: StateFlow<RealtimeConnectionState> = _connectionState.asStateFlow()
    
    private val _transactionUpdates = MutableSharedFlow<MpesaTransactionUpdate>(replay = 1)
    val transactionUpdates: SharedFlow<MpesaTransactionUpdate> = _transactionUpdates.asSharedFlow()
    
    private var currentCheckoutRequestId: String? = null
    private var messageRef = 0
    
    /**
     * Subscribe to M-Pesa transaction status updates for a specific checkout request ID.
     * Returns a Flow that emits transaction status events.
     * 
     * @param checkoutRequestId The checkout request ID to monitor
     * @param scope The CoroutineScope to use for the subscription
     * @param onStatusUpdate Callback for status updates
     */
    fun subscribeToTransactionStatus(
        checkoutRequestId: String,
        scope: CoroutineScope,
        onStatusUpdate: (TransactionStatusEvent) -> Unit
    ): Job {
        currentCheckoutRequestId = checkoutRequestId
        
        return scope.launch {
            Log.d(TAG, "⚡ Subscribing to real-time updates for: $checkoutRequestId")
            
            // Start timeout countdown
            timeoutJob?.cancel()
            timeoutJob = launch {
                delay(MAX_WAIT_TIME_MS)
                Log.w(TAG, "⏰ Transaction timeout after ${MAX_WAIT_TIME_MS / 1000}s")
                onStatusUpdate(TransactionStatusEvent.Timeout("Payment confirmation timeout. Check M-Pesa messages."))
                disconnect()
            }
            
            // Connect and subscribe
            connect()
            
            // Wait for connection
            connectionState.first { it is RealtimeConnectionState.Connected }
            
            // Subscribe to the channel
            subscribeToChannel(checkoutRequestId)
            
            // Collect updates
            transactionUpdates
                .filter { it.checkoutRequestId == checkoutRequestId }
                .collect { update ->
                    Log.d(TAG, "📥 Received update: status=${update.status}")
                    
                    val event = when (update.status.lowercase()) {
                        "completed" -> {
                            timeoutJob?.cancel()
                            TransactionStatusEvent.Completed(
                                receiptNumber = update.mpesaReceiptNumber ?: "",
                                amount = update.amount ?: 0.0
                            )
                        }
                        "cancelled" -> {
                            timeoutJob?.cancel()
                            TransactionStatusEvent.Cancelled(update.resultDesc ?: "Cancelled by user")
                        }
                        "failed" -> {
                            timeoutJob?.cancel()
                            TransactionStatusEvent.Failed(
                                reason = update.resultDesc ?: "Payment failed",
                                resultCode = update.resultCode
                            )
                        }
                        else -> TransactionStatusEvent.Pending
                    }
                    
                    onStatusUpdate(event)
                    
                    // Disconnect after final status
                    if (event !is TransactionStatusEvent.Pending) {
                        disconnect()
                    }
                }
        }
    }
    
    /**
     * Connect to Supabase Realtime WebSocket
     */
    private fun connect() {
        if (webSocket != null) {
            Log.d(TAG, "Already connected, reusing connection")
            return
        }
        
        _connectionState.value = RealtimeConnectionState.Connecting
        Log.d(TAG, "🔌 Connecting to Supabase Realtime...")
        
        val request = Request.Builder()
            .url(REALTIME_URL)
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "✅ WebSocket connected!")
                _connectionState.value = RealtimeConnectionState.Connected
                startHeartbeat()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                webSocket.close(1000, null)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                _connectionState.value = RealtimeConnectionState.Disconnected
                cleanup()
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ WebSocket error: ${t.message}")
                _connectionState.value = RealtimeConnectionState.Error(t.message ?: "Connection failed")
                cleanup()
            }
        })
    }
    
    /**
     * Subscribe to mpesa_transactions channel with postgres_changes
     */
    private fun subscribeToChannel(checkoutRequestId: String) {
        val channelTopic = "realtime:public:mpesa_transactions"
        
        // Join channel message
        val joinMessage = JsonObject().apply {
            addProperty("topic", channelTopic)
            addProperty("event", "phx_join")
            add("payload", JsonObject().apply {
                add("config", JsonObject().apply {
                    add("postgres_changes", gson.toJsonTree(listOf(
                        mapOf(
                            "event" to "UPDATE",
                            "schema" to "public",
                            "table" to "mpesa_transactions",
                            "filter" to "checkout_request_id=eq.$checkoutRequestId"
                        )
                    )))
                })
            })
            addProperty("ref", (++messageRef).toString())
        }
        
        val messageString = gson.toJson(joinMessage)
        Log.d(TAG, "📤 Subscribing to channel: $channelTopic")
        Log.d(TAG, "📤 Filter: checkout_request_id=$checkoutRequestId")
        
        webSocket?.send(messageString)
    }
    
    /**
     * Handle incoming WebSocket messages
     */
    private fun handleMessage(text: String) {
        try {
            val message = gson.fromJson(text, JsonObject::class.java)
            val event = message.get("event")?.asString
            val topic = message.get("topic")?.asString
            
            Log.d(TAG, "📨 Received: event=$event, topic=$topic")
            
            when (event) {
                "phx_reply" -> {
                    val payload = message.getAsJsonObject("payload")
                    val status = payload?.get("status")?.asString
                    Log.d(TAG, "Channel reply: $status")
                    
                    if (status == "ok") {
                        Log.d(TAG, "✅ Successfully subscribed to channel!")
                    }
                }
                
                "postgres_changes" -> {
                    val payload = message.getAsJsonObject("payload")
                    Log.d(TAG, "📊 Database change detected!")
                    handleDatabaseChange(payload)
                }
                
                "system" -> {
                    Log.d(TAG, "System message received")
                }
                
                "phx_error" -> {
                    val payload = message.getAsJsonObject("payload")
                    Log.e(TAG, "❌ Channel error: $payload")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}")
        }
    }
    
    /**
     * Handle database change notifications
     */
    private fun handleDatabaseChange(payload: JsonObject?) {
        try {
            val data = payload?.getAsJsonObject("data")
            val record = data?.getAsJsonObject("record")
            
            if (record != null) {
                val checkoutRequestId = record.get("checkout_request_id")?.asString ?: return
                val status = record.get("status")?.asString ?: "pending"
                
                Log.d(TAG, "🔄 Transaction update: $checkoutRequestId -> $status")
                
                // Only process if this is the transaction we're watching
                if (checkoutRequestId == currentCheckoutRequestId) {
                    val update = MpesaTransactionUpdate(
                        checkoutRequestId = checkoutRequestId,
                        status = status,
                        resultCode = record.get("result_code")?.asInt,
                        resultDesc = record.get("result_desc")?.asString,
                        mpesaReceiptNumber = record.get("mpesa_receipt_number")?.asString 
                            ?: record.get("mpesa_receipt")?.asString,
                        amount = record.get("amount")?.asDouble,
                        completedAt = record.get("completed_at")?.asString
                    )
                    
                    // Emit update through flow
                    CoroutineScope(Dispatchers.Main).launch {
                        _transactionUpdates.emit(update)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling database change: ${e.message}")
        }
    }
    
    /**
     * Start heartbeat to keep connection alive
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                
                val heartbeat = JsonObject().apply {
                    addProperty("topic", "phoenix")
                    addProperty("event", "heartbeat")
                    add("payload", JsonObject())
                    addProperty("ref", (++messageRef).toString())
                }
                
                val sent = webSocket?.send(gson.toJson(heartbeat)) ?: false
                if (sent) {
                    Log.d(TAG, "💓 Heartbeat sent")
                } else {
                    Log.w(TAG, "⚠️ Heartbeat failed")
                }
            }
        }
    }
    
    /**
     * Disconnect from WebSocket
     */
    fun disconnect() {
        Log.d(TAG, "🔌 Disconnecting...")
        timeoutJob?.cancel()
        heartbeatJob?.cancel()
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        currentCheckoutRequestId = null
        _connectionState.value = RealtimeConnectionState.Disconnected
    }
    
    /**
     * Cleanup resources
     */
    private fun cleanup() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        webSocket = null
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return _connectionState.value is RealtimeConnectionState.Connected
    }
}
