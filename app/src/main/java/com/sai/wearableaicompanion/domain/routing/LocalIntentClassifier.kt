package com.sai.wearableaicompanion.domain.routing

/**
 * Shared, exact-match classifier used by both [DefaultIntentRouter] and
 * [LocalIntentHandler] so the two never disagree about what counts as "local".
 * Exact matching (rather than substring matching) is deliberate: it is what keeps
 * a query like "Explain time series forecasting" from being misrouted just because
 * it contains the word "time".
 */
internal enum class LocalIntent {
    TIME,
    DATE,
    BATTERY,
    HELP,
}

internal object LocalIntentClassifier {

    private val timePhrases = setOf(
        "what time is it",
        "what time is it now",
        "what's the time",
        "what's the time now",
        "current time",
        "tell me the time",
        "tell me the current time",
    )

    private val datePhrases = setOf(
        "what date is it",
        "what date is it today",
        "what's today's date",
        "today's date",
        "what day is it",
        "what day is it today",
    )

    private val batteryPhrases = setOf(
        "battery level",
        "battery percentage",
        "what's my battery level",
        "what is my battery level",
        "what's my battery percentage",
        "what is my battery percentage",
        "how much battery do i have",
        "how much battery is left",
    )

    private val helpPhrases = setOf(
        "help",
        "what can you do",
        "how can you help me",
    )

    fun classify(rawInput: String): LocalIntent? {
        return when (normalize(rawInput)) {
            in timePhrases -> LocalIntent.TIME
            in datePhrases -> LocalIntent.DATE
            in batteryPhrases -> LocalIntent.BATTERY
            in helpPhrases -> LocalIntent.HELP
            else -> null
        }
    }

    private fun normalize(rawInput: String): String {
        return rawInput
            .trim()
            .lowercase()
            .replace('’', '\'')
            .replace(Regex("[.!?]+$"), "")
            .replace(Regex("\\s+"), " ")
    }
}
