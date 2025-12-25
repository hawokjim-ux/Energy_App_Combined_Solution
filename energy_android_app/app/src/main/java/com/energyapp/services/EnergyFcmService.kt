package com.energyapp.services

import android.content.Context
import android.util.Log

/**
 * FCM Token Manager - Placeholder
 * 
 * This is a placeholder until Firebase is configured.
 * When you add Firebase to your project:
 * 1. Add firebase-messaging dependency
 * 2. Replace this file with the full EnergyFcmService.kt
 * 
 * For now, this provides mock FCM token functionality
 * that works with the OptimizedMpesaService.
 */
object FcmTokenManager {
    private const val TAG = "FcmTokenManager"
    private const val PREF_FCM_TOKEN = "fcm_token"
    
    // Mock token for testing (will be replaced with real FCM when Firebase is added)
    private var mockToken: String? = null
    
    /**
     * Get the current FCM token
     * Returns a mock token until Firebase is configured
     */
    fun getToken(context: Context): String? {
        // First check if we have a saved token
        val savedToken = context.getSharedPreferences("energy_app_prefs", Context.MODE_PRIVATE)
            .getString(PREF_FCM_TOKEN, null)
        
        if (savedToken != null) {
            return savedToken
        }
        
        // Generate a mock token for testing
        if (mockToken == null) {
            mockToken = "mock_fcm_token_${System.currentTimeMillis()}"
            Log.d(TAG, "Generated mock FCM token: ${mockToken?.take(20)}...")
            
            // Save it
            context.getSharedPreferences("energy_app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_FCM_TOKEN, mockToken)
                .apply()
        }
        
        return mockToken
    }
    
    /**
     * Set listener for token refresh events
     * (Placeholder - will be implemented when Firebase is added)
     */
    fun setOnTokenRefreshListener(listener: (String) -> Unit) {
        // When Firebase is added, this will register for token refresh events
        Log.d(TAG, "Token refresh listener registered (placeholder)")
    }
    
    /**
     * Request a new FCM token
     * (Placeholder - will be implemented when Firebase is added)
     */
    fun refreshToken(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            // Generate a mock token
            val token = "mock_fcm_token_${System.currentTimeMillis()}"
            mockToken = token
            Log.d(TAG, "Mock FCM Token generated: ${token.take(20)}...")
            onSuccess(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate mock token: ${e.message}")
            onFailure(e)
        }
    }
}

/**
 * INSTRUCTIONS TO ENABLE REAL FCM:
 * 
 * 1. Add Firebase to your project:
 *    - Go to Firebase Console (https://console.firebase.google.com)
 *    - Create a project or use existing
 *    - Add Android app with package: com.energyapp
 *    - Download google-services.json and place in app/ folder
 * 
 * 2. Add dependencies to app/build.gradle.kts:
 *    
 *    // Add to plugins block:
 *    id("com.google.gms.google-services")
 *    
 *    // Add to dependencies:
 *    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
 *    implementation("com.google.firebase:firebase-messaging-ktx")
 * 
 * 3. Add to project-level build.gradle.kts:
 *    
 *    classpath("com.google.gms:google-services:4.4.0")
 * 
 * 4. Replace this file with the full EnergyFcmService.kt from:
 *    D:\Energy_App_Combined_Solution\energy_android_app\app\src\main\java\com\energyapp\services\EnergyFcmService_Full.kt
 * 
 * 5. Register the service in AndroidManifest.xml:
 *    
 *    <service
 *        android:name=".services.EnergyFcmService"
 *        android:exported="false">
 *        <intent-filter>
 *            <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *        </intent-filter>
 *    </service>
 */
