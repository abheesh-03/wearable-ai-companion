package com.sai.wearableaicompanion.presentation.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is CHECKING with no selection and no message`() {
        val viewModel = HomeViewModel(healthChecker = { true })

        val state = viewModel.uiState.value

        assertEquals(ConnectionStatus.CHECKING, state.connectionStatus)
        assertNull(state.selectedQuickAction)
        assertNull(state.message)
    }

    @Test
    fun `refreshConnectivity with a reachable backend results in CONNECTED`() {
        val viewModel = HomeViewModel(healthChecker = { true })

        viewModel.refreshConnectivity()

        assertEquals(ConnectionStatus.CONNECTED, viewModel.uiState.value.connectionStatus)
    }

    @Test
    fun `refreshConnectivity with an unreachable backend results in OFFLINE`() {
        val viewModel = HomeViewModel(healthChecker = { false })

        viewModel.refreshConnectivity()

        assertEquals(ConnectionStatus.OFFLINE, viewModel.uiState.value.connectionStatus)
    }

    @Test
    fun `refreshConnectivity can update an already-resolved status`() {
        var reachable = true
        val viewModel = HomeViewModel(healthChecker = { reachable })

        viewModel.refreshConnectivity()
        assertEquals(ConnectionStatus.CONNECTED, viewModel.uiState.value.connectionStatus)

        reachable = false
        viewModel.refreshConnectivity()

        assertEquals(ConnectionStatus.OFFLINE, viewModel.uiState.value.connectionStatus)
    }

    @Test
    fun `selecting Time updates selectedQuickAction and message`() {
        val viewModel = HomeViewModel(healthChecker = { true })

        viewModel.onQuickAction(QuickAction.TIME)

        val state = viewModel.uiState.value
        assertEquals(QuickAction.TIME, state.selectedQuickAction)
        assertEquals("Time selected", state.message)
    }

    @Test
    fun `selecting Date updates selectedQuickAction and message`() {
        val viewModel = HomeViewModel(healthChecker = { true })

        viewModel.onQuickAction(QuickAction.DATE)

        val state = viewModel.uiState.value
        assertEquals(QuickAction.DATE, state.selectedQuickAction)
        assertEquals("Date selected", state.message)
    }

    @Test
    fun `selecting Explain updates selectedQuickAction and message`() {
        val viewModel = HomeViewModel(healthChecker = { true })

        viewModel.onQuickAction(QuickAction.EXPLAIN)

        val state = viewModel.uiState.value
        assertEquals(QuickAction.EXPLAIN, state.selectedQuickAction)
        assertEquals("Explain selected", state.message)
    }

    @Test
    fun `selecting a new quick action after another does not reset connection status`() {
        val viewModel = HomeViewModel(healthChecker = { true })
        viewModel.refreshConnectivity()

        viewModel.onQuickAction(QuickAction.TIME)
        viewModel.onQuickAction(QuickAction.DATE)

        val state = viewModel.uiState.value
        assertEquals(ConnectionStatus.CONNECTED, state.connectionStatus)
        assertEquals(QuickAction.DATE, state.selectedQuickAction)
    }
}
