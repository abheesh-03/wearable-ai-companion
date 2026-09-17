package com.sai.wearableaicompanion.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sai.wearableaicompanion.presentation.navigation.WearableApp
import com.sai.wearableaicompanion.presentation.theme.WearableAICompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearableAICompanionTheme {
                WearableApp()
            }
        }
    }
}
