package com.sai.wearableaicompanion.presentation.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.sai.wearableaicompanion.presentation.components.PrimaryActionButton
import com.sai.wearableaicompanion.presentation.components.QuickActionChip
import com.sai.wearableaicompanion.presentation.components.StatusIndicator

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onQuickAction: (QuickAction) -> Unit = {},
    onAskAi: () -> Unit = {},
) {
    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(
            scrollState = listState,
            edgeButton = {
                PrimaryActionButton(
                    text = "Ask AI",
                    onClick = onAskAi,
                )
            },
        ) { contentPadding ->
            TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                item {
                    ListHeader(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(text = "AI Companion")
                    }
                }
                item {
                    StatusIndicator(
                        connectionStatus = uiState.connectionStatus,
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    )
                }
                if (uiState.message != null) {
                    item {
                        Text(
                            text = uiState.message,
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                item {
                    QuickActionChip(
                        text = QuickAction.TIME.label,
                        selected = uiState.selectedQuickAction == QuickAction.TIME,
                        onClick = { onQuickAction(QuickAction.TIME) },
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    )
                }
                item {
                    QuickActionChip(
                        text = QuickAction.DATE.label,
                        selected = uiState.selectedQuickAction == QuickAction.DATE,
                        onClick = { onQuickAction(QuickAction.DATE) },
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    )
                }
                item {
                    QuickActionChip(
                        text = QuickAction.EXPLAIN.label,
                        selected = uiState.selectedQuickAction == QuickAction.EXPLAIN,
                        onClick = { onQuickAction(QuickAction.EXPLAIN) },
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    )
                }
            }
        }
    }
}
