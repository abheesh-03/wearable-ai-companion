package com.sai.wearableaicompanion.presentation.ask

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.sai.wearableaicompanion.domain.routing.IntentRoute
import com.sai.wearableaicompanion.presentation.theme.WearableAICompanionTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AskAiScreenEmptyPreview() {
    WearableAICompanionTheme {
        AskAiScreenContent(
            uiState = AskAiUiState(),
            onEditRequested = {},
            onSend = {},
        )
    }
}

@WearPreviewDevices
@Composable
fun AskAiScreenWithInputPreview() {
    WearableAICompanionTheme {
        AskAiScreenContent(
            uiState = AskAiUiState(input = "Summarize my last three messages"),
            onEditRequested = {},
            onSend = {},
        )
    }
}

@WearPreviewDevices
@Composable
fun AskAiScreenErrorPreview() {
    WearableAICompanionTheme {
        AskAiScreenContent(
            uiState = AskAiUiState(error = "Enter a question first"),
            onEditRequested = {},
            onSend = {},
        )
    }
}

@WearPreviewDevices
@Composable
fun AskAiScreenLocalResultPreview() {
    WearableAICompanionTheme {
        AskAiScreenContent(
            uiState = AskAiUiState(
                input = "What time is it?",
                route = IntentRoute.LOCAL,
                response = "It's 3:45 PM",
            ),
            onEditRequested = {},
            onSend = {},
        )
    }
}

@WearPreviewDevices
@Composable
fun AskAiScreenCloudResultPreview() {
    WearableAICompanionTheme {
        AskAiScreenContent(
            uiState = AskAiUiState(
                input = "Summarize my last three messages",
                route = IntentRoute.CLOUD,
                response = "Cloud backend received: Summarize my last three messages",
                requestId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                latencyMs = 182.0,
            ),
            onEditRequested = {},
            onSend = {},
        )
    }
}

@WearPreviewDevices
@Composable
fun AskAiScreenCloudLoadingPreview() {
    WearableAICompanionTheme {
        AskAiScreenContent(
            uiState = AskAiUiState(
                input = "Summarize my last three messages",
                route = IntentRoute.CLOUD,
                isSubmitting = true,
            ),
            onEditRequested = {},
            onSend = {},
        )
    }
}

@WearPreviewDevices
@Composable
fun AskAiScreenCloudFailurePreview() {
    WearableAICompanionTheme {
        AskAiScreenContent(
            uiState = AskAiUiState(
                input = "Summarize my last three messages",
                route = IntentRoute.CLOUD,
                error = "Unable to reach cloud service.",
            ),
            onEditRequested = {},
            onSend = {},
        )
    }
}
