package com.sai.wearableaicompanion.presentation.ask

import com.sai.wearableaicompanion.domain.routing.IntentRoute

data class AskAiUiState(
    val input: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val route: IntentRoute? = null,
    val response: String? = null,
    val requestId: String? = null,
    val latencyMs: Double? = null,
)
