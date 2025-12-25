package com.energyapp.data.remote.api

import com.energyapp.util.MpesaConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // *** CRITICAL CHANGE: RENDER PHP BASE URL ***
    // The base URL must be the root of your Render service.
    // The endpoints (stkpush.php, check_status.php) will be appended to this.
    private const val BASE_URL = MpesaConfig.RENDER_BASE_URL // "https://online-link.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // *** CRITICAL CHANGE: REMOVE AUTH INTERCEPTOR ***
    // The PHP backend is a direct REST service and does not require Supabase auth headers.
    // Keeping this simple header to ensure JSON content type.
    private val standardHeaderInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        // REMOVED: .addInterceptor(authInterceptor)
        .addInterceptor(standardHeaderInterceptor) // Using the simpler header interceptor
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Assuming ApiService is the interface for ALL API endpoints, including Mpesa
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}