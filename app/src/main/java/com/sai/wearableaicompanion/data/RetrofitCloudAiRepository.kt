package com.sai.wearableaicompanion.data

import com.google.gson.JsonParseException
import com.sai.wearableaicompanion.data.remote.ApiService
import com.sai.wearableaicompanion.data.remote.RetrofitClient
import com.sai.wearableaicompanion.data.remote.model.ChatRequest
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/**
 * Maps Retrofit/OkHttp/Gson failures to concise, user-facing messages so raw
 * exception details (stack traces, URLs, class names) never reach the UI layer.
 */
class RetrofitCloudAiRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
) : CloudAiRepository {

    override suspend fun sendMessage(message: String): CloudAiResult {
        return try {
            val response = apiService.sendChat(ChatRequest(message = message))
            if (response.response.isBlank() || response.requestId.isBlank()) {
                CloudAiResult.Failure(INVALID_RESPONSE_MESSAGE)
            } else {
                CloudAiResult.Success(
                    response = response.response,
                    requestId = response.requestId,
                    latencyMs = response.latencyMs,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            CloudAiResult.Failure(TIMEOUT_MESSAGE)
        } catch (e: IOException) {
            CloudAiResult.Failure(CONNECTION_MESSAGE)
        } catch (e: HttpException) {
            if (e.code() in 500..599) {
                CloudAiResult.Failure(SERVER_UNAVAILABLE_MESSAGE)
            } else {
                CloudAiResult.Failure(UNKNOWN_MESSAGE)
            }
        } catch (e: JsonParseException) {
            CloudAiResult.Failure(INVALID_RESPONSE_MESSAGE)
        } catch (e: Exception) {
            CloudAiResult.Failure(UNKNOWN_MESSAGE)
        }
    }

    private companion object {
        const val CONNECTION_MESSAGE = "Unable to reach cloud service."
        const val TIMEOUT_MESSAGE = "Cloud request timed out."
        const val SERVER_UNAVAILABLE_MESSAGE = "Cloud service is temporarily unavailable."
        const val INVALID_RESPONSE_MESSAGE = "Invalid cloud response."
        const val UNKNOWN_MESSAGE = "Something went wrong."
    }
}
