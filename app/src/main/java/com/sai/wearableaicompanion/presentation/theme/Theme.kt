package com.sai.wearableaicompanion.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun WearableAICompanionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = AccentPrimary,
            primaryContainer = AccentPrimaryContainer,
            onPrimary = OnAccentPrimary,
        ),
        content = content
    )
}