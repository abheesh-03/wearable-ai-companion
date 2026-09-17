package com.sai.wearableaicompanion.data

fun interface HealthChecker {
    suspend fun isBackendReachable(): Boolean
}
