package com.sai.wearableaicompanion.presentation.home

import com.sai.wearableaicompanion.domain.routing.DefaultIntentRouter
import com.sai.wearableaicompanion.domain.routing.IntentRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickActionTest {

    @Test
    fun `Time maps to the exact local time prompt and auto-sends`() {
        assertEquals("What time is it now", QuickAction.TIME.promptText)
        assertTrue(QuickAction.TIME.autoSend)
    }

    @Test
    fun `Date maps to the exact local date prompt and auto-sends`() {
        assertEquals("What date is it today", QuickAction.DATE.promptText)
        assertTrue(QuickAction.DATE.autoSend)
    }

    @Test
    fun `Explain maps to a prefill and does not auto-send`() {
        assertEquals("Explain ", QuickAction.EXPLAIN.promptText)
        assertFalse(QuickAction.EXPLAIN.autoSend)
    }

    @Test
    fun `Time and Date prompts route locally through the real intent router`() {
        val router = DefaultIntentRouter()

        assertEquals(IntentRoute.LOCAL, router.route(QuickAction.TIME.promptText))
        assertEquals(IntentRoute.LOCAL, router.route(QuickAction.DATE.promptText))
    }

    @Test
    fun `fromArgOrNull round-trips each action by name deterministically`() {
        for (action in QuickAction.entries) {
            assertEquals(action, QuickAction.fromArgOrNull(action.name))
        }
    }

    @Test
    fun `fromArgOrNull returns null for an unknown or missing argument`() {
        assertNull(QuickAction.fromArgOrNull(null))
        assertNull(QuickAction.fromArgOrNull("NOT_A_REAL_ACTION"))
    }
}
