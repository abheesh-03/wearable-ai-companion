package com.sai.wearableaicompanion.domain.routing

class DefaultIntentRouter : IntentRouter {
    override fun route(input: String): IntentRoute {
        return if (LocalIntentClassifier.classify(input) != null) {
            IntentRoute.LOCAL
        } else {
            IntentRoute.CLOUD
        }
    }
}
