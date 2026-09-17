package com.sai.wearableaicompanion.data

import com.sai.wearableaicompanion.data.remote.ApiService
import com.sai.wearableaicompanion.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException

/**
 * One-shot GET /health check. Never throws — any failure (offline, timeout,
 * unexpected response) simply resolves to `false` so callers never need to
 * handle raw network exceptions.
 */
class RetrofitHealthChecker(
    private val apiService: ApiService = RetrofitClient.apiService,
) : HealthChecker {

    override suspend fun isBackendReachable(): Boolean {
        return try {
            apiService.health().status == "ok"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }
}
