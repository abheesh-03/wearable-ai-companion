package com.sai.wearableaicompanion.data

import com.google.gson.JsonSyntaxException
import com.sai.wearableaicompanion.data.remote.ApiService
import com.sai.wearableaicompanion.data.remote.model.ChatRequest
import com.sai.wearableaicompanion.data.remote.model.ChatResponse
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RetrofitCloudAiRepositoryTest {

    @Test
    fun `successful response maps to Success with response, requestId and latency`() = runBlocking {
        val repository = RetrofitCloudAiRepository(
            FakeApiService {
                ChatResponse(
                    response = "Cloud backend received: hello",
                    requestId = "req-123",
                    latencyMs = 42.5,
                )
            },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Success

        assertEquals("Cloud backend received: hello", result.response)
        assertEquals("req-123", result.requestId)
        assertEquals(42.5, result.latencyMs, 0.0)
    }

    @Test
    fun `connection failure maps to unable to reach cloud service`() = runBlocking {
        val repository = RetrofitCloudAiRepository(
            FakeApiService { throw IOException("connection refused") },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Failure

        assertEquals("Unable to reach cloud service.", result.message)
    }

    @Test
    fun `timeout maps to cloud request timed out`() = runBlocking {
        val repository = RetrofitCloudAiRepository(
            FakeApiService { throw SocketTimeoutException("timeout") },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Failure

        assertEquals("Cloud request timed out.", result.message)
    }

    @Test
    fun `http 5xx maps to cloud service temporarily unavailable`() = runBlocking {
        val errorBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val repository = RetrofitCloudAiRepository(
            FakeApiService { throw HttpException(Response.error<ChatResponse>(500, errorBody)) },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Failure

        assertEquals("Cloud service is temporarily unavailable.", result.message)
    }

    @Test
    fun `http 4xx maps to a generic unknown error`() = runBlocking {
        val errorBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val repository = RetrofitCloudAiRepository(
            FakeApiService { throw HttpException(Response.error<ChatResponse>(422, errorBody)) },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Failure

        assertEquals("Something went wrong.", result.message)
    }

    @Test
    fun `malformed json maps to invalid cloud response`() = runBlocking {
        val repository = RetrofitCloudAiRepository(
            FakeApiService { throw JsonSyntaxException("bad json") },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Failure

        assertEquals("Invalid cloud response.", result.message)
    }

    @Test
    fun `blank fields in an otherwise successful response map to invalid cloud response`() = runBlocking {
        val repository = RetrofitCloudAiRepository(
            FakeApiService { ChatResponse(response = "", requestId = "req-123", latencyMs = 10.0) },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Failure

        assertEquals("Invalid cloud response.", result.message)
    }

    @Test
    fun `unknown exception maps to a generic error`() = runBlocking {
        val repository = RetrofitCloudAiRepository(
            FakeApiService { throw IllegalStateException("boom") },
        )

        val result = repository.sendMessage("hello") as CloudAiResult.Failure

        assertEquals("Something went wrong.", result.message)
    }

    @Test
    fun `cancellation is propagated rather than swallowed as a failure`() = runBlocking {
        val repository = RetrofitCloudAiRepository(
            FakeApiService { throw kotlinx.coroutines.CancellationException("cancelled") },
        )

        var threw = false
        try {
            repository.sendMessage("hello")
        } catch (e: kotlinx.coroutines.CancellationException) {
            threw = true
        }

        assertTrue(threw)
    }

    private class FakeApiService(private val block: () -> ChatResponse) : ApiService {
        override suspend fun sendChat(request: ChatRequest): ChatResponse = block()

        override suspend fun health(): com.sai.wearableaicompanion.data.remote.model.HealthResponse {
            error("not used in this test")
        }
    }
}
