package com.sai.wearableaicompanion.presentation.home

data class HomeUiState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.CHECKING,
    val selectedQuickAction: QuickAction? = null,
    val message: String? = null,
)
