package com.energyapp.ui.screens.attendant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.energyapp.data.remote.MpesaBackendService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Polling status states
 */
sealed class PollingState {
    data object Idle : PollingState()
    data object Polling : PollingState()
    data class Success(val receipt: String, val amount: Double) : PollingState()
    data class Failed(val errorMessage: String) : PollingState()
    data class Timeout(val message: String) : PollingState()
}

/**
 * ViewModel for polling M-Pesa transaction status
 * 
 * Usage:
 * - User initiates payment and gets CheckoutRequestID
 * - Pass CheckoutRequestID to this poller
 * - It will poll every 5 seconds until:
 *   - Transaction succeeds (show receipt)
 *   - Transaction fails (show error)
 *   - 2 minutes pass (show timeout)
 */
@HiltViewModel
class TransactionPollerViewModel @Inject constructor(
    private val mpesaBackendService: MpesaBackendService
) : ViewModel() {

    private val _pollingState = MutableStateFlow<PollingState>(PollingState.Idle)
    val pollingState: StateFlow<PollingState> = _pollingState.asStateFlow()

    private var pollingJob: kotlinx.coroutines.Job? = null
    private var pollCount = 0
    private val MAX_POLLS = 24 // 24 * 5 seconds = 2 minutes

    /**
     * Start polling for transaction status
     * 
     * @param checkoutRequestId The CheckoutRequestID from STK Push response
     */
    fun startPolling(checkoutRequestId: String) {
        if (pollingJob?.isActive == true) {
            return // Already polling
        }

        _pollingState.value = PollingState.Polling
        pollCount = 0

        pollingJob = viewModelScope.launch {
            while (pollCount < MAX_POLLS) {
                try {
                    // Wait 5 seconds before first poll
                    delay(5000)
                    pollCount++

                    // Check transaction status
                    val response = mpesaBackendService.checkTransactionStatus(checkoutRequestId)

                    if (response.success && response.resultCode == 0) {
                        // ✅ Transaction successful!
                        val receipt = response.mpesaReceiptNumber ?: "N/A"
                        val amount = response.amount ?: 0.0
                        
                        _pollingState.value = PollingState.Success(
                            receipt = receipt,
                            amount = amount
                        )
                        return@launch
                    } else if (response.success && response.resultCode != null && response.resultCode != 0) {
                        // ❌ Transaction failed
                        val errorMsg = response.resultDesc ?: "Transaction failed"
                        _pollingState.value = PollingState.Failed(errorMessage = errorMsg)
                        return@launch
                    }
                    // If resultCode is null, transaction is still pending - continue polling

                } catch (e: Exception) {
                    // Network error, but keep polling
                    // Only fail after MAX_POLLS attempts
                    if (pollCount >= MAX_POLLS) {
                        _pollingState.value = PollingState.Failed(
                            errorMessage = "Network error: ${e.message}"
                        )
                        return@launch
                    }
                }
            }

            // Max polls reached - timeout
            _pollingState.value = PollingState.Timeout(
                message = "Payment confirmation timed out. Please check M-Pesa or try again."
            )
        }
    }

    /**
     * Stop polling and reset state
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        pollCount = 0
        _pollingState.value = PollingState.Idle
    }

    /**
     * Reset to idle state
     */
    fun reset() {
        stopPolling()
    }
}

// ==================== USAGE IN COMPOSABLE ====================

// In your PaymentSuccessDialog or similar composable:
/*

val pollerViewModel: TransactionPollerViewModel = hiltViewModel()
val pollingState by pollerViewModel.pollingState.collectAsState()

LaunchedEffect(checkoutRequestId) {
    if (!checkoutRequestId.isNullOrEmpty()) {
        pollerViewModel.startPolling(checkoutRequestId)
    }
}

when (pollingState) {
    is PollingState.Polling -> {
        // Show loading dialog
        LoadingDialog(message = "Waiting for payment confirmation...\nPlease check your phone")
    }
    is PollingState.Success -> {
        val state = pollingState as PollingState.Success
        // Show success dialog with receipt
        SuccessDialog(
            receipt = state.receipt,
            amount = state.amount,
            onDismiss = {
                pollerViewModel.reset()
                // Navigate back to sales screen
            }
        )
    }
    is PollingState.Failed -> {
        val state = pollingState as PollingState.Failed
        // Show error dialog
        ErrorDialog(
            message = state.errorMessage,
            onDismiss = {
                pollerViewModel.reset()
            }
        )
    }
    is PollingState.Timeout -> {
        val state = pollingState as PollingState.Timeout
        // Show timeout dialog
        TimeoutDialog(
            message = state.message,
            onRetry = {
                pollerViewModel.startPolling(checkoutRequestId)
            },
            onDismiss = {
                pollerViewModel.reset()
            }
        )
    }
    PollingState.Idle -> {
        // Nothing to show
    }
}
*/