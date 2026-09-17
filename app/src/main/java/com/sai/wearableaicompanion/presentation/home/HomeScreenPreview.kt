package com.sai.wearableaicompanion.presentation.home

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.sai.wearableaicompanion.presentation.theme.WearableAICompanionTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun HomeScreenPreview() {
    WearableAICompanionTheme {
        HomeScreen(uiState = HomeUiState(connectionStatus = ConnectionStatus.CONNECTED))
    }
}

@WearPreviewDevices
@Composable
fun HomeScreenCheckingPreview() {
    WearableAICompanionTheme {
        HomeScreen(uiState = HomeUiState(connectionStatus = ConnectionStatus.CHECKING))
    }
}

@WearPreviewDevices
@Composable
fun HomeScreenOfflinePreview() {
    WearableAICompanionTheme {
        HomeScreen(uiState = HomeUiState(connectionStatus = ConnectionStatus.OFFLINE))
    }
}

@WearPreviewDevices
@Composable
fun HomeScreenSelectedActionPreview() {
    WearableAICompanionTheme {
        HomeScreen(
            uiState = HomeUiState(
                connectionStatus = ConnectionStatus.CONNECTED,
                selectedQuickAction = QuickAction.TIME,
                message = "${QuickAction.TIME.label} selected",
            )
        )
    }
}
