package com.sai.wearableaicompanion.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.sai.wearableaicompanion.presentation.ask.AskAiScreen
import com.sai.wearableaicompanion.presentation.ask.AskAiViewModel
import com.sai.wearableaicompanion.presentation.home.HomeScreen
import com.sai.wearableaicompanion.presentation.home.HomeViewModel
import com.sai.wearableaicompanion.presentation.home.QuickAction

@Composable
fun WearableApp() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
    ) {
        composable(AppDestination.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

            // Fires once per Home composition: on first launch, and again each
            // time Home is recomposed after returning from Ask AI. No polling.
            LaunchedEffect(Unit) {
                homeViewModel.refreshConnectivity()
            }

            HomeScreen(
                uiState = uiState,
                onQuickAction = { action ->
                    homeViewModel.onQuickAction(action)
                    navController.navigate(AppDestination.AskAi.routeFor(action))
                },
                onAskAi = { navController.navigate(AppDestination.AskAi.routeFor()) },
            )
        }
        composable(
            route = AppDestination.AskAi.route,
            arguments = listOf(
                navArgument(AppDestination.AskAi.QUICK_ACTION_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val askAiViewModel: AskAiViewModel = viewModel()
            val uiState by askAiViewModel.uiState.collectAsStateWithLifecycle()

            // Keyed on the backstack entry's stable id, not Unit: this runs
            // exactly once per navigation into this screen, regardless of how
            // many times uiState-driven recomposition happens afterwards — so
            // a quick action can never be auto-sent twice.
            LaunchedEffect(backStackEntry.id) {
                val quickActionArg = backStackEntry.arguments
                    ?.getString(AppDestination.AskAi.QUICK_ACTION_ARG)
                val action = QuickAction.fromArgOrNull(quickActionArg)
                if (action != null) {
                    askAiViewModel.onInputChange(action.promptText)
                    if (action.autoSend) {
                        askAiViewModel.onSend()
                    }
                }
            }

            AskAiScreen(
                uiState = uiState,
                onInputChange = askAiViewModel::onInputChange,
                onSend = askAiViewModel::onSend,
            )
        }
    }
}
