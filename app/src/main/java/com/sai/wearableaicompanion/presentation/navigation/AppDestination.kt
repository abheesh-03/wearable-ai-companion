package com.sai.wearableaicompanion.presentation.navigation

import com.sai.wearableaicompanion.presentation.home.QuickAction

sealed interface AppDestination {
    val route: String

    data object Home : AppDestination {
        override val route: String = "home"
    }

    data object AskAi : AppDestination {
        const val QUICK_ACTION_ARG = "quickAction"
        override val route: String = "ask_ai?$QUICK_ACTION_ARG={$QUICK_ACTION_ARG}"

        fun routeFor(action: QuickAction? = null): String =
            if (action == null) "ask_ai" else "ask_ai?$QUICK_ACTION_ARG=${action.name}"
    }
}
