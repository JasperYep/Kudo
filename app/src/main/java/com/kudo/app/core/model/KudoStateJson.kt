package com.kudo.app.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object KudoStateJson {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun decodeOrNull(raw: String?): KudoState? {
        if (raw.isNullOrBlank()) {
            return null
        }

        return try {
            val state = json.decodeFromString<KudoState>(raw)
            val mergedTasks = mergeLegacyLists(state.tasks)
            sanitize(state.copy(tasks = mergedTasks))
        } catch (_: Exception) {
            null
        }
    }

    fun encode(state: KudoState): String {
        return json.encodeToString(state)
    }

    fun decode(raw: String?): KudoState {
        return decodeOrNull(raw) ?: KudoState()
    }

    fun sanitize(state: KudoState): KudoState {
        // Reset all running timers before save/load - prevents stale timer issues
        val resetTasks = state.tasks.map { task ->
            task.copy(
                isTimerRunning = false,
                lastTimerStart = 0L
            )
        }
        return state.copy(
            tasks = resetTasks,
            taskSortMode = sanitizeTaskSortMode(state.taskSortMode)
        )
    }

    // Legacy list merging removed - list field no longer exists
    private fun mergeLegacyLists(tasks: List<KudoTask>): List<KudoTask> {
        return tasks
    }

    private fun sanitizeTaskSortMode(mode: Int): Int {
        return when (mode) {
            KudoState.TASK_SORT_AUTO_DUE,
            KudoState.TASK_SORT_MANUAL -> mode
            else -> KudoState.TASK_SORT_AUTO_DUE
        }
    }
}
