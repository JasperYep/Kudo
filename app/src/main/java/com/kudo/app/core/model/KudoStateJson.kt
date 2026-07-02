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
            sanitize(state)
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
        return state.copy(
            taskSortMode = sanitizeTaskSortMode(state.taskSortMode)
        )
    }

    private fun sanitizeTaskSortMode(mode: Int): Int {
        return when (mode) {
            KudoState.TASK_SORT_AUTO_DUE,
            KudoState.TASK_SORT_MANUAL -> mode
            else -> KudoState.TASK_SORT_AUTO_DUE
        }
    }
}
