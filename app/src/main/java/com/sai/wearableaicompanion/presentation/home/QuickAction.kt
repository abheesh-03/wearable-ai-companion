package com.sai.wearableaicompanion.presentation.home

/**
 * [promptText] is sent verbatim to [com.sai.wearableaicompanion.presentation.ask.AskAiViewModel]
 * on Ask AI's first composition for this action — Time/Date route through the
 * existing LOCAL intent path, so they work with the backend offline. [autoSend]
 * controls whether that happens immediately or the user finishes typing first.
 */
enum class QuickAction(
    val label: String,
    val promptText: String,
    val autoSend: Boolean,
) {
    TIME(label = "Time", promptText = "What time is it now", autoSend = true),
    DATE(label = "Date", promptText = "What date is it today", autoSend = true),
    EXPLAIN(label = "Explain", promptText = "Explain ", autoSend = false),
    ;

    companion object {
        fun fromArgOrNull(name: String?): QuickAction? = entries.find { it.name == name }
    }
}
