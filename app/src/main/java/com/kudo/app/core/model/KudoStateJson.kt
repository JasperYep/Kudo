package com.kudo.app.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
            val normalized = normalizeRaw(raw) ?: return null
            sanitize(json.decodeFromJsonElement(KudoState.serializer(), normalized))
        } catch (_: Exception) {
            null
        }
    }

    fun encode(state: KudoState): String {
        return json.encodeToString(sanitize(state))
    }

    fun decode(raw: String?): KudoState {
        return decodeOrNull(raw) ?: KudoState()
    }

    fun sanitize(state: KudoState): KudoState {
        return state.copy(
            schemaVersion = KudoState.SCHEMA_VERSION,
            logs = state.logs.map { log ->
                if (log.undoable) log else log.copy(taskId = null, itemData = null)
            },
            taskSortMode = sanitizeTaskSortMode(state.taskSortMode)
        )
    }

    private fun normalizeRaw(raw: String): JsonObject? {
        val source = json.parseToJsonElement(raw).jsonObject
        val schemaVersion = source["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1
        val normalizedTasks = source["tasks"]?.jsonArray
            ?.mapNotNull { task -> normalizeTask(task, schemaVersion) }
            ?: emptyList()
        val normalizedLogs = source["logs"]?.jsonArray
            ?.mapNotNull(::normalizeLog)
            ?: emptyList()

        return JsonObject(
            source.toMutableMap().apply {
                put("schemaVersion", JsonPrimitive(KudoState.SCHEMA_VERSION))
                put("tasks", JsonArray(normalizedTasks))
                put("logs", JsonArray(normalizedLogs))
            }
        )
    }

    private fun normalizeTask(task: JsonElement, schemaVersion: Int): JsonObject? {
        val taskObject = task as? JsonObject ?: return null
        val legacyType = taskObject["type"]?.jsonPrimitive?.intOrNull
        val isTask = if (schemaVersion >= KudoState.SCHEMA_VERSION) {
            legacyType == null || legacyType == 0
        } else {
            legacyType == 0
        }
        if (!isTask) return null

        return JsonObject(
            taskObject.toMutableMap().apply {
                remove("type")
                remove("count")
                remove("last")
            }
        )
    }

    private fun normalizeLog(log: JsonElement): JsonObject? {
        val logObject = log as? JsonObject ?: return null
        val logType = logObject["type"]?.jsonPrimitive?.contentOrNull
        val itemData = logObject["itemData"] as? JsonObject
        val isLegacyHabitLog = logType == "task" && (
            logObject["isHabit"]?.jsonPrimitive?.booleanOrNull == true ||
                itemData?.get("type")?.jsonPrimitive?.intOrNull == 1
            )

        return if (isLegacyHabitLog) {
            JsonObject(
                logObject.toMutableMap().apply {
                    remove("taskId")
                    remove("isHabit")
                    remove("itemData")
                    put("undoable", JsonPrimitive(false))
                }
            )
        } else {
            JsonObject(
                logObject.toMutableMap().apply {
                    remove("isHabit")
                    itemData?.let { data ->
                        put("itemData", normalizeLogItemData(data, logType))
                    }
                }
            )
        }
    }

    private fun normalizeLogItemData(itemData: JsonObject, logType: String?): JsonObject {
        return JsonObject(
            itemData.toMutableMap().apply {
                val legacyType = remove("type")
                if (logType == "store" && !containsKey("storeType") && legacyType != null) {
                    put("storeType", legacyType)
                }
                remove("count")
                remove("last")
            }
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
