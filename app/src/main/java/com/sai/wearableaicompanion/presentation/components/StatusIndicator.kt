package com.sai.wearableaicompanion.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.sai.wearableaicompanion.presentation.home.ConnectionStatus
import com.sai.wearableaicompanion.presentation.theme.StatusConnectedGreen

@Composable
fun StatusIndicator(
    connectionStatus: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        StatusConnectedGreen
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = when (connectionStatus) {
                ConnectionStatus.CHECKING -> "Checking..."
                ConnectionStatus.CONNECTED -> "Connected"
                ConnectionStatus.OFFLINE -> "Offline"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
