package com.sai.wearableaicompanion.data

sealed interface CloudAiResult {
    data class Success(
        val response: String,
        val requestId: String,
        val latencyMs: Double,
    ) : CloudAiResult

    data class Failure(val message: String) : CloudAiResult
}

fun interface CloudAiRepository {
    suspend fun sendMessage(message: String): CloudAiResult
}
