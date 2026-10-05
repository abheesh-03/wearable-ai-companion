package com.sai.wearableaicompanion.data.device

import android.content.Context
import android.os.BatteryManager
import com.sai.wearableaicompanion.domain.routing.BatteryStatusProvider

class AndroidBatteryStatusProvider(
    context: Context,
) : BatteryStatusProvider {

    private val batteryManager =
        context.applicationContext.getSystemService(BatteryManager::class.java)

    override fun batteryPercent(): Int? {
        val percentage = batteryManager
            ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            ?: return null

        return percentage.takeIf { it in 0..100 }
    }
}
