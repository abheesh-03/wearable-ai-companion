package com.sai.wearableaicompanion.presentation.ask

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sai.wearableaicompanion.data.device.AndroidBatteryStatusProvider
import com.sai.wearableaicompanion.domain.routing.LocalIntentHandler

class AskAiViewModelFactory(
    context: Context,
) : ViewModelProvider.Factory {

    private val applicationContext = context.applicationContext

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (modelClass.isAssignableFrom(AskAiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AskAiViewModel(
                localIntentHandler = LocalIntentHandler(
                    batteryStatusProvider =
                        AndroidBatteryStatusProvider(applicationContext),
                ),
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}",
        )
    }
}
