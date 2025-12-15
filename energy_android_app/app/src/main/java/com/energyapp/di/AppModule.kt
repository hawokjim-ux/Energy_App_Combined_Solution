package com.energyapp.di

import android.content.Context
import com.energyapp.data.remote.MpesaBackendService
import com.energyapp.data.remote.SupabaseApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Main Hilt Module for application-wide dependency injection
 * Provides singleton instances of all services
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ==================== API SERVICES ====================

    /**
     * Provides M-Pesa Backend Service for Railway PHP backend integration
     * Handles: STK Push, Transaction status checking
     */
    @Singleton
    @Provides
    fun provideMpesaBackendService(): MpesaBackendService {
        return MpesaBackendService()
    }

    /**
     * Provides Supabase API Service for database operations
     * Handles: Users, Pumps, Shifts, Sales, M-Pesa transactions
     */
    @Singleton
    @Provides
    fun provideSupabaseApiService(
        mpesaBackendService: MpesaBackendService
    ): SupabaseApiService {
        return SupabaseApiService(mpesaBackendService)
    }

    // ==================== CONTEXT ====================

    /**
     * Provides application context for preference storage and resources
     */
    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context {
        return context
    }
}