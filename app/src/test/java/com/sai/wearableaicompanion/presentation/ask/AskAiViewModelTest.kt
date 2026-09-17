package com.sai.wearableaicompanion.presentation.ask

import com.sai.wearableaicompanion.data.CloudAiRepository
import com.sai.wearableaicompanion.data.CloudAiResult
import com.sai.wearableaicompanion.domain.routing.IntentRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AskAiViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty with no error and not submitting`() {
        val viewModel = AskAiViewModel(cloudAiRepository = NeverCalledCloudAiRepository())

        val state = viewModel.uiState.value

        assertEquals("", state.input)
        assertFalse(state.isSubmitting)
        assertNull(state.error)
        assertNull(state.route)
        assertNull(state.response)
        assertNull(state.requestId)
        assertNull(state.latencyMs)
    }

    @Test
    fun `updating input reflects in state`() {
        val viewModel = AskAiViewModel(cloudAiRepository = NeverCalledCloudAiRepository())

        viewModel.onInputChange("Summarize my day")

        val state = viewModel.uiState.value
        assertEquals("Summarize my day", state.input)
    }

    @Test
    fun `sending blank input is rejected locally with an error`() {
        val viewModel = AskAiViewModel(cloudAiRepository = NeverCalledCloudAiRepository())

        viewModel.onSend()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Enter a question first", state.error)
        assertNull(state.route)
    }

    @Test
    fun `sending whitespace only input is rejected locally with an error`() {
        val viewModel = AskAiViewModel(cloudAiRepository = NeverCalledCloudAiRepository())

        viewModel.onInputChange("   ")
        viewModel.onSend()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Enter a question first", state.error)
    }

    @Test
    fun `LOCAL request produces the LOCAL route with a real local response and never calls the cloud repository`() {
        val repository = NeverCalledCloudAiRepository()
        val viewModel = AskAiViewModel(cloudAiRepository = repository)

        viewModel.onInputChange("help")
        viewModel.onSend()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNull(state.error)
        assertEquals(IntentRoute.LOCAL, state.route)
        assertEquals(
            "I can answer simple device questions locally or use cloud AI for deeper requests.",
            state.response,
        )
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `CLOUD success populates response, requestId and latency, and clears loading`() {
        val repository = FakeCloudAiRepository(
            CloudAiResult.Success(
                response = "Cloud backend received: Summarize my day",
                requestId = "req-abc",
                latencyMs = 87.0,
            ),
        )
        val viewModel = AskAiViewModel(cloudAiRepository = repository)

        viewModel.onInputChange("Summarize my day")
        viewModel.onSend()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNull(state.error)
        assertEquals(IntentRoute.CLOUD, state.route)
        assertEquals("Cloud backend received: Summarize my day", state.response)
        assertEquals("req-abc", state.requestId)
        assertEquals(87.0, state.latencyMs)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `CLOUD failure exposes a friendly error, clears response, and clears loading`() {
        val repository = FakeCloudAiRepository(
            CloudAiResult.Failure("Unable to reach cloud service."),
        )
        val viewModel = AskAiViewModel(cloudAiRepository = repository)

        viewModel.onInputChange("Summarize my day")
        viewModel.onSend()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Unable to reach cloud service.", state.error)
        assertEquals(IntentRoute.CLOUD, state.route)
        assertNull(state.response)
    }

    @Test
    fun `duplicate send while a cloud request is already in flight does not call the repository again`() {
        val repository = SuspendingCloudAiRepository()
        val viewModel = AskAiViewModel(cloudAiRepository = repository)
        viewModel.onInputChange("Summarize my day")

        viewModel.onSend()
        assertTrue(viewModel.uiState.value.isSubmitting)
        assertEquals(1, repository.callCount)

        viewModel.onSend()

        assertEquals(1, repository.callCount)

        repository.complete(CloudAiResult.Success("done", "req-1", 5.0))
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `error clears once the input changes again`() {
        val viewModel = AskAiViewModel(cloudAiRepository = NeverCalledCloudAiRepository())

        viewModel.onSend()
        assertEquals("Enter a question first", viewModel.uiState.value.error)

        viewModel.onInputChange("Quick note")

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertEquals("Quick note", state.input)
    }

    @Test
    fun `stale route and response clear once the input changes after a send`() {
        val viewModel = AskAiViewModel(cloudAiRepository = NeverCalledCloudAiRepository())

        viewModel.onInputChange("help")
        viewModel.onSend()
        assertEquals(IntentRoute.LOCAL, viewModel.uiState.value.route)

        viewModel.onInputChange("What's the weather like?")

        val state = viewModel.uiState.value
        assertNull(state.route)
        assertNull(state.response)
    }

    @Test
    fun `input longer than 160 characters is clamped`() {
        val viewModel = AskAiViewModel(cloudAiRepository = NeverCalledCloudAiRepository())

        viewModel.onInputChange("a".repeat(200))

        val state = viewModel.uiState.value
        assertEquals(160, state.input.length)
    }

    /** Fails the test if the CLOUD repository is ever invoked — used by LOCAL/validation tests. */
    private class NeverCalledCloudAiRepository : CloudAiRepository {
        var callCount = 0
            private set

        override suspend fun sendMessage(message: String): CloudAiResult {
            callCount++
            error("CloudAiRepository should not be called for this request")
        }
    }

    private class FakeCloudAiRepository(private val result: CloudAiResult) : CloudAiRepository {
        var callCount = 0
            private set

        override suspend fun sendMessage(message: String): CloudAiResult {
            callCount++
            return result
        }
    }

    /** Suspends forever until [complete] is called, letting tests observe the in-flight state. */
    private class SuspendingCloudAiRepository : CloudAiRepository {
        var callCount = 0
            private set
        private val completion = CompletableDeferred<CloudAiResult>()

        override suspend fun sendMessage(message: String): CloudAiResult {
            callCount++
            return completion.await()
        }

        fun complete(result: CloudAiResult) {
            completion.complete(result)
        }
    }
}
