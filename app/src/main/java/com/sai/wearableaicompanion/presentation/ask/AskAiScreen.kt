package com.sai.wearableaicompanion.presentation.ask

import android.app.Activity
import android.app.RemoteInput
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.sai.wearableaicompanion.domain.routing.IntentRoute

private const val ASK_AI_INPUT_KEY = "ask_ai_input"

/**
 * Wear Compose (Material3 1.5.6) has no on-screen text field, by design: a full
 * QWERTY keyboard doesn't fit a round watch face. The Wear-native pattern is to
 * launch the system's remote-input picker (keyboard / voice / handwriting) via
 * [RemoteInputIntentHelper] and receive the typed text back as an activity result.
 */
@Composable
fun AskAiScreen(
    uiState: AskAiUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val data = result.data ?: return@rememberLauncherForActivityResult
        RemoteInput.getResultsFromIntent(data)
            ?.getCharSequence(ASK_AI_INPUT_KEY)
            ?.let { onInputChange(it.toString()) }
    }

    AskAiScreenContent(
        uiState = uiState,
        onEditRequested = {
            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            RemoteInputIntentHelper.putRemoteInputsExtra(
                intent,
                listOf(
                    RemoteInput.Builder(ASK_AI_INPUT_KEY)
                        .setLabel("Ask anything...")
                        .wearableExtender {
                            setInputActionType(EditorInfo.IME_ACTION_DONE)
                        }
                        .build(),
                ),
            )
            launcher.launch(intent)
        },
        onSend = onSend,
    )
}

@Composable
internal fun AskAiScreenContent(
    uiState: AskAiUiState,
    onEditRequested: () -> Unit,
    onSend: () -> Unit,
) {
    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(
            scrollState = listState,
            edgeButton = {
                Button(
                    onClick = onSend,
                    enabled = !uiState.isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(text = if (uiState.isSubmitting) "Sending..." else "Send")
                }
            },
        ) { contentPadding ->
            TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                item {
                    ListHeader(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(text = "Ask AI")
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onEditRequested,
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(
                            text = uiState.input.ifBlank { "Ask anything..." },
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (uiState.input.isNotEmpty()) {
                    item {
                        Text(
                            text = "${uiState.input.length} characters",
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (uiState.error != null) {
                    item {
                        Text(
                            text = uiState.error,
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (uiState.isSubmitting) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sending...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                val route = uiState.route
                val response = uiState.response
                if (!uiState.isSubmitting && route != null && response != null) {
                    item {
                        Text(
                            text = when (route) {
                                IntentRoute.LOCAL -> "On-device"
                                IntentRoute.CLOUD -> "Cloud AI"
                            },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    item {
                        Text(
                            text = response,
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (route == IntentRoute.CLOUD && uiState.latencyMs != null) {
                        item {
                            Text(
                                text = "${uiState.latencyMs.toInt()} ms",
                                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
