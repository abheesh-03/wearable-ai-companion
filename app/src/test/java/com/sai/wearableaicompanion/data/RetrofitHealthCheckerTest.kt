package com.sai.wearableaicompanion.data

import com.sai.wearableaicompanion.data.remote.ApiService
import com.sai.wearableaicompanion.data.remote.model.ChatRequest
import com.sai.wearableaicompanion.data.remote.model.ChatResponse
import com.sai.wearableaicompanion.data.remote.model.HealthResponse
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrofitHealthCheckerTest {

    @Test
    fun `ok status maps to reachable`() = runBlocking {
        val checker = RetrofitHealthChecker(FakeApiService { HealthResponse(status = "ok") })

        assertTrue(checker.isBackendReachable())
    }

    @Test
    fun `non-ok status maps to unreachable`() = runBlocking {
        val checker = RetrofitHealthChecker(FakeApiService { HealthResponse(status = "degraded") })

        assertFalse(checker.isBackendReachable())
    }

    @Test
    fun `connection failure maps to unreachable without throwing`() = runBlocking {
        val checker = RetrofitHealthChecker(
            FakeApiService { throw IOException("connection refused") },
        )

        assertFalse(checker.isBackendReachable())
    }

    private class FakeApiService(private val block: () -> HealthResponse) : ApiService {
        override suspend fun health(): HealthResponse = block()

        override suspend fun sendChat(request: ChatRequest): ChatResponse {
            error("not used in this test")
        }
    }
}
