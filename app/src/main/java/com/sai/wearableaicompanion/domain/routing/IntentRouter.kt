package com.sai.wearableaicompanion.domain.routing

fun interface IntentRouter {
    fun route(input: String): IntentRoute
}
