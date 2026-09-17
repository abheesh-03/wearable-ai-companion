package com.sai.wearableaicompanion.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sai.wearableaicompanion.data.HealthChecker
import com.sai.wearableaicompanion.data.RetrofitHealthChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel @JvmOverloads constructor(
    private val healthChecker: HealthChecker = RetrofitHealthChecker(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onQuickAction(action: QuickAction) {
        _uiState.update {
            it.copy(
                selectedQuickAction = action,
                message = "${action.label} selected",
            )
        }
    }

    /** One-shot GET /health check — call when Home becomes active, not on a timer. */
    fun refreshConnectivity() {
        _uiState.update { it.copy(connectionStatus = ConnectionStatus.CHECKING) }
        viewModelScope.launch {
            val reachable = healthChecker.isBackendReachable()
            _uiState.update {
                it.copy(
                    connectionStatus = if (reachable) ConnectionStatus.CONNECTED else ConnectionStatus.OFFLINE,
                )
            }
        }
    }
}
