package com.sai.wearableaicompanion.domain.routing

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalIntentHandlerTest {

    // Thursday, September 17, 2026, 15:45 UTC.
    private val fixedClock = Clock.fixed(
        Instant.parse("2026-09-17T15:45:00Z"),
        ZoneOffset.UTC,
    )
    private val handler = LocalIntentHandler(fixedClock)

    @Test
    fun `time request returns a deterministic formatted time from the injected clock`() {
        val response = handler.handle("What time is it?")

        assertEquals("It's 3:45 PM", response)
    }

    @Test
    fun `date request returns a deterministic formatted date from the injected clock`() {
        val response = handler.handle("What day is it?")

        assertEquals("Today is Thursday, September 17", response)
    }

    @Test
    fun `help request returns the capability summary`() {
        val response = handler.handle("help")

        assertEquals(
            "I can answer simple device questions locally or use cloud AI for deeper requests.",
            response,
        )
    }

    @Test
    fun `unrecognized input does not crash and returns a fallback message`() {
        val response = handler.handle("Explain time series forecasting")

        assertEquals("I couldn't process that on-device.", response)
    }
}
