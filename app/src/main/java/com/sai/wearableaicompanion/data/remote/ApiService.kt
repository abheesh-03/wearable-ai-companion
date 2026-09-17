package com.sai.wearableaicompanion.data.remote

import com.sai.wearableaicompanion.data.remote.model.ChatRequest
import com.sai.wearableaicompanion.data.remote.model.ChatResponse
import com.sai.wearableaicompanion.data.remote.model.HealthResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("chat")
    suspend fun sendChat(@Body request: ChatRequest): ChatResponse
}
