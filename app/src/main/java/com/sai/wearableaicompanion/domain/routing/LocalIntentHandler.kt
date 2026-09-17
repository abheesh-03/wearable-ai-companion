package com.sai.wearableaicompanion.domain.routing

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Answers requests that [DefaultIntentRouter] classified as [IntentRoute.LOCAL].
 * [clock] is injected (defaulting to the real system clock in production) so time
 * and date responses are deterministic and testable without touching the device
 * clock.
 */
class LocalIntentHandler @JvmOverloads constructor(
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    fun handle(input: String): String {
        return when (LocalIntentClassifier.classify(input)) {
            LocalIntent.TIME -> respondWithTime()
            LocalIntent.DATE -> respondWithDate()
            LocalIntent.HELP -> HELP_MESSAGE
            null -> UNRECOGNIZED_MESSAGE
        }
    }

    private fun respondWithTime(): String {
        val time = LocalTime.now(clock)
        return "It's ${time.format(TIME_FORMATTER)}"
    }

    private fun respondWithDate(): String {
        val date = LocalDate.now(clock)
        return "Today is ${date.format(DATE_FORMATTER)}"
    }

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)

        const val HELP_MESSAGE =
            "I can answer simple device questions locally or use cloud AI for deeper requests."
        const val UNRECOGNIZED_MESSAGE = "I couldn't process that on-device."
    }
}
