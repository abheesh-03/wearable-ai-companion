package com.sai.wearableaicompanion.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dev/local-emulator-only networking setup. 10.0.2.2 is the special alias the
 * Android emulator uses to reach the host machine's loopback interface, where
 * the local FastAPI backend runs (see backend/app/main.py). This is not a
 * production endpoint.
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
