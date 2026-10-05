package com.sai.wearableaicompanion.domain.routing

interface BatteryStatusProvider {
    fun batteryPercent(): Int?
}

object UnavailableBatteryStatusProvider : BatteryStatusProvider {
    override fun batteryPercent(): Int? = null
}
