package com.sai.wearableaicompanion.domain.routing

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultIntentRouterTest {

    private val router = DefaultIntentRouter()

    @Test
    fun `what time is it routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What time is it?"))
    }

    @Test
    fun `whats the time routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What's the time"))
    }

    @Test
    fun `current time routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("current time"))
    }

    @Test
    fun `what date is it routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What date is it?"))
    }

    @Test
    fun `whats todays date routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What's today's date?"))
    }

    @Test
    fun `what day is it routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What day is it?"))
    }

    @Test
    fun `help routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("help"))
    }

    @Test
    fun `what can you do routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What can you do?"))
    }

    @Test
    fun `what time is it now routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What time is it now?"))
    }

    @Test
    fun `tell me the current time routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("Tell me the current time"))
    }

    @Test
    fun `what date is it today routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("What date is it today?"))
    }

    @Test
    fun `how can you help me routes locally`() {
        assertEquals(IntentRoute.LOCAL, router.route("How can you help me?"))
    }

    @Test
    fun `normal ai question routes to cloud`() {
        assertEquals(IntentRoute.CLOUD, router.route("Write me a haiku about the ocean"))
    }

    @Test
    fun `semantic question containing the word time still routes to cloud`() {
        assertEquals(IntentRoute.CLOUD, router.route("Explain time series forecasting"))
    }

    @Test
    fun `what is time complexity routes to cloud`() {
        assertEquals(IntentRoute.CLOUD, router.route("What is time complexity?"))
    }

    @Test
    fun `question containing the word date still routes to cloud`() {
        assertEquals(IntentRoute.CLOUD, router.route("What's a good first date idea?"))
    }

    @Test
    fun `phrase similar to a local trigger but not exact routes to cloud`() {
        assertEquals(IntentRoute.CLOUD, router.route("current time zone in Tokyo"))
    }

    @Test
    fun `current time zone in tokyo routes to cloud`() {
        assertEquals(IntentRoute.CLOUD, router.route("Current time zone in Tokyo"))
    }

    @Test
    fun `casing and surrounding whitespace are normalized before matching`() {
        assertEquals(IntentRoute.LOCAL, router.route("   WHAT   TIME IS IT   "))
    }

    @Test
    fun `blank input routes to cloud`() {
        assertEquals(IntentRoute.CLOUD, router.route(""))
    }
}
