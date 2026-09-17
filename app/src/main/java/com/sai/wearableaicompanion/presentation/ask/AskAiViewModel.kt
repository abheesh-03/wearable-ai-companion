package com.sai.wearableaicompanion.presentation.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sai.wearableaicompanion.data.CloudAiRepository
import com.sai.wearableaicompanion.data.CloudAiResult
import com.sai.wearableaicompanion.data.RetrofitCloudAiRepository
import com.sai.wearableaicompanion.domain.routing.DefaultIntentRouter
import com.sai.wearableaicompanion.domain.routing.IntentRoute
import com.sai.wearableaicompanion.domain.routing.IntentRouter
import com.sai.wearableaicompanion.domain.routing.LocalIntentHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AskAiViewModel @JvmOverloads constructor(
    private val intentRouter: IntentRouter = DefaultIntentRouter(),
    private val localIntentHandler: LocalIntentHandler = LocalIntentHandler(),
    private val cloudAiRepository: CloudAiRepository = RetrofitCloudAiRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskAiUiState())
    val uiState: StateFlow<AskAiUiState> = _uiState.asStateFlow()

    fun onInputChange(input: String) {
        _uiState.update {
            it.copy(
                input = input.take(MAX_INPUT_LENGTH),
                error = null,
                route = null,
                response = null,
                requestId = null,
                latencyMs = null,
            )
        }
    }

    fun onSend() {
        val currentState = _uiState.value
        if (currentState.isSubmitting) {
            return
        }

        val input = currentState.input
        if (input.isBlank()) {
            _uiState.update {
                it.copy(error = "Enter a question first")
            }
            return
        }

        when (val route = intentRouter.route(input)) {
            IntentRoute.LOCAL -> {
                val response = localIntentHandler.handle(input)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = null,
                        route = route,
                        response = response,
                        requestId = null,
                        latencyMs = null,
                    )
                }
            }
            IntentRoute.CLOUD -> sendToCloud(input)
        }
    }

    private fun sendToCloud(input: String) {
        _uiState.update {
            it.copy(
                isSubmitting = true,
                error = null,
                route = IntentRoute.CLOUD,
                response = null,
                requestId = null,
                latencyMs = null,
            )
        }

        viewModelScope.launch {
            when (val result = cloudAiRepository.sendMessage(input)) {
                is CloudAiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = null,
                            route = IntentRoute.CLOUD,
                            response = result.response,
                            requestId = result.requestId,
                            latencyMs = result.latencyMs,
                        )
                    }
                }
                is CloudAiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.message,
                            route = IntentRoute.CLOUD,
                            response = null,
                            requestId = null,
                            latencyMs = null,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val MAX_INPUT_LENGTH = 160
    }
}
