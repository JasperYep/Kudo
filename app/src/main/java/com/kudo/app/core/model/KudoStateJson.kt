package com.kudo.app.core.model

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

object KudoStateJson {

    private const val FORMAT = "kudo"
    private const val VERSION = 1
    private const val KIND_TASK = "task"
    private const val KIND_HABIT = "habit"
    private const val KIND_STORE = "store"
    private const val STORE_ONCE = "once"
    private const val STORE_REPEATABLE = "repeatable"
    private const val SORT_AUTO_DUE = "autoDue"
    private const val SORT_MANUAL = "manual"
    private val BackupZoneId: ZoneId = ZoneId.systemDefault()
    private val BackupTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val LocalDateTimeFormats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )

    fun decodeOrNull(raw: String?): KudoState? {
        if (raw.isNullOrBlank()) return null

        return try {
            val root = JSONObject(raw)
            val settings = root.optJSONObject("settings") ?: root
            val logs = root.optJSONArray("logs").toLogList()
            sanitize(
                KudoState(
                    coins = settings.optInt("coins", root.optInt("coins", 0)),
                    tasks = root.optJSONArray("tasks").toTaskList(),
                    store = root.optJSONArray("store").toStoreList(),
                    logs = logs,
                    recentVals = logs.toRecentValues(),
                    multiplier = settings.optDouble("multiplier", root.optDouble("multiplier", 1.0)).toFloat(),
                    taskSortMode = settings.optString("taskSort", root.optString("taskSort", SORT_AUTO_DUE))
                        .toTaskSortMode(),
                    notes = root.optJSONArray("notes").toNoteList()
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    fun encode(state: KudoState): String {
        val root = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put(
                "settings",
                JSONObject()
                    .put("coins", state.coins)
                    .put("multiplier", state.multiplier.toDouble())
                    .put("taskSort", state.taskSortMode.toTaskSortName())
            )
            .put("tasks", state.tasks.toTaskJsonArray())
            .put("store", state.store.toStoreJsonArray())
            .put("logs", state.logs.sortedByDescending(KudoLogEntry::timestamp).toLogJsonArray())
            .put("notes", state.notes.toNoteJsonArray())

        return root.toString(2)
    }

    fun decode(raw: String?): KudoState {
        return decodeOrNull(raw) ?: KudoState()
    }

    fun sanitize(state: KudoState): KudoState {
        return state.copy(
            tasks = state.tasks.map { task ->
                task.copy(subtasks = if (task.type == KudoState.TYPE_HABIT) emptyList() else task.subtasks)
            },
            logs = state.logs.sortedByDescending(KudoLogEntry::timestamp),
            taskSortMode = sanitizeTaskSortMode(state.taskSortMode)
        )
    }

    private fun List<KudoTask>.toTaskJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toTaskJsonArray.forEach { task ->
                put(
                    JSONObject()
                        .put("id", task.id)
                        .put("title", task.title)
                        .put("kind", task.type.toTaskKind())
                        .put("coins", task.valAmount)
                        .apply {
                            if (task.isHabit) {
                                put("count", task.count)
                                task.last.toIsoStringOrNull()?.let { put("lastDone", it) }
                            }
                            task.dueAtEpochMillis?.let { put("due", it.toIsoString()) }
                            if (task.subtasks.isNotEmpty()) {
                                put("subtasks", task.subtasks.toSubtaskJsonArray())
                            }
                        }
                )
            }
        }
    }

    private fun JSONArray?.toTaskList(): List<KudoTask> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val id = json.optLong("id", System.currentTimeMillis() + index)
                val kind = json.optString("kind", json.optString("type", KIND_TASK))
                add(
                    KudoTask(
                        id = id,
                        title = json.optString("title", ""),
                        valAmount = json.optInt("coins", json.optInt("value", 0)),
                        type = kind.toTaskType(),
                        count = json.optInt("count", 0),
                        last = json.optStringOrNull("lastDone")
                            ?.toEpochMillisOrNull()
                            ?: json.optString("lastCompletedAt", "").toEpochMillisOrZero(),
                        order = json.optLong("order", index.toLong()),
                        dueAtEpochMillis = (json.optStringOrNull("due") ?: json.optStringOrNull("dueAt"))
                            ?.toEpochMillisOrNull(),
                        subtasks = json.optJSONArray("subtasks").toSubtaskList()
                    )
                )
            }
        }
    }

    private fun List<KudoSubtask>.toSubtaskJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toSubtaskJsonArray.forEach { subtask ->
                put(
                    JSONObject()
                        .put("id", subtask.id)
                        .put("title", subtask.title)
                        .put("coins", subtask.valAmount)
                        .apply {
                            subtask.completedAt?.let { put("doneAt", it.toIsoString()) }
                        }
                )
            }
        }
    }

    private fun JSONArray?.toSubtaskList(): List<KudoSubtask> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                add(
                    KudoSubtask(
                        id = json.optLong("id", System.currentTimeMillis() + index),
                        title = json.optString("title", ""),
                        valAmount = json.optInt("coins", json.optInt("value", 0)),
                        completedAt = (json.optStringOrNull("doneAt") ?: json.optStringOrNull("completedAt"))
                            ?.toEpochMillisOrNull()
                    )
                )
            }
        }
    }

    private fun List<KudoStoreItem>.toStoreJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toStoreJsonArray.forEach { item ->
                put(
                    JSONObject()
                        .put("id", item.id)
                        .put("title", item.title)
                        .put("coins", item.cost)
                        .put("kind", item.type.toStoreKind())
                )
            }
        }
    }

    private fun JSONArray?.toStoreList(): List<KudoStoreItem> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                add(
                    KudoStoreItem(
                        id = json.optLong("id", System.currentTimeMillis() + index),
                        title = json.optString("title", ""),
                        cost = json.optInt("coins", json.optInt("cost", 0)),
                        type = json.optString("kind", json.optString("type", STORE_ONCE)).toStoreType()
                    )
                )
            }
        }
    }

    private fun List<KudoLogEntry>.toLogJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toLogJsonArray.forEach { log ->
                put(
                    JSONObject()
                        .put("time", log.timestamp.toIsoString())
                        .put("title", log.text)
                        .put("coins", log.value)
                        .put("kind", log.type)
                        .apply {
                            log.baseValue?.let { put("baseCoins", it) }
                            log.subtaskId?.let { put("subtaskId", it) }
                            log.itemData?.let { put("undo", it.toUndoJson(log.type)) }
                        }
                )
            }
        }
    }

    private fun JSONArray?.toLogList(): List<KudoLogEntry> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val kind = json.optString("kind", json.optString("type", ""))
                val undoJson = json.optJSONObject("undo")
                add(
                    KudoLogEntry(
                        timestamp = (json.optStringOrNull("time") ?: json.optString("timestamp", ""))
                            .toEpochMillisOrZero(),
                        text = json.optString("title", json.optString("text", "")),
                        value = json.optInt("coins", json.optInt("value", 0)),
                        baseValue = if (json.has("baseCoins")) {
                            json.optInt("baseCoins", 0)
                        } else if (json.has("baseValue")) {
                            json.optInt("baseValue", 0)
                        } else {
                            null
                        },
                        type = kind,
                        taskId = if (kind == KIND_STORE) null else undoJson?.optLong("id"),
                        subtaskId = if (json.has("subtaskId")) {
                            json.optLong("subtaskId")
                        } else if (json.has("completedSubtaskId")) {
                            json.optLong("completedSubtaskId")
                        } else {
                            null
                        },
                        isHabit = undoJson?.optString("kind") == KIND_HABIT,
                        itemData = undoJson?.toLogItemData(kind)
                    )
                )
            }
        }
    }

    private fun KudoLogItemData.toUndoJson(logKind: String): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("title", title)
            .apply {
                if (logKind == KIND_STORE) {
                    cost?.let { put("coins", it) }
                    put("kind", type.toStoreKind())
                } else {
                    valAmount?.let { put("coins", it) }
                    put("kind", type.toTaskKind())
                    if (type == KudoState.TYPE_HABIT) {
                        put("count", count)
                        last.toIsoStringOrNull()?.let { put("lastDone", it) }
                    }
                    dueAtEpochMillis?.let { put("due", it.toIsoString()) }
                    if (subtasks.isNotEmpty()) {
                        put("subtasks", subtasks.toSubtaskJsonArray())
                    }
                }
            }
    }

    private fun JSONObject.toLogItemData(logKind: String): KudoLogItemData {
        val id = optLong("id", 0L)
        return if (logKind == KIND_STORE) {
            KudoLogItemData(
                id = id,
                title = optString("title", ""),
                cost = if (has("coins")) optInt("coins", 0) else null,
                type = optString("kind", STORE_ONCE).toStoreType()
            )
        } else {
            val kind = optString("kind", KIND_TASK)
            KudoLogItemData(
                id = id,
                title = optString("title", ""),
                valAmount = if (has("coins")) optInt("coins", 0) else null,
                type = kind.toTaskType(),
                count = optInt("count", 0),
                last = optString("lastDone", "").toEpochMillisOrZero(),
                order = optLong("order", id),
                dueAtEpochMillis = optStringOrNull("due")?.toEpochMillisOrNull(),
                subtasks = optJSONArray("subtasks").toSubtaskList()
            )
        }
    }

    private fun List<KudoNote>.toNoteJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toNoteJsonArray.forEach { note ->
                put(
                    JSONObject()
                        .put("id", note.id)
                        .put("title", note.title)
                        .put("content", note.content)
                        .put("updated", note.updatedAt.toIsoString())
                )
            }
        }
    }

    private fun JSONArray?.toNoteList(): List<KudoNote> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val id = json.optLong("id", System.currentTimeMillis() + index)
                add(
                    KudoNote(
                        id = id,
                        title = json.optString("title", ""),
                        content = json.optString("content", ""),
                        updatedAt = (json.optStringOrNull("updated") ?: json.optStringOrNull("updatedAt"))
                            ?.toEpochMillisOrNull()
                            ?: id
                    )
                )
            }
        }
    }

    private fun List<KudoLogEntry>.toRecentValues(): List<Int> {
        return asSequence()
            .filter { it.type == KIND_TASK && it.value > 0 }
            .sortedBy(KudoLogEntry::timestamp)
            .map { it.baseValue ?: it.value }
            .toList()
            .takeLast(5)
    }

    private fun JSONObject.optStringOrNull(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return optString(name).takeIf(String::isNotBlank)
    }

    private fun Long.toIsoString(): String {
        return Instant.ofEpochMilli(this)
            .atZone(BackupZoneId)
            .format(BackupTimeFormatter)
    }

    private fun Long.toIsoStringOrNull(): String? {
        return if (this == 0L) null else toIsoString()
    }

    private fun String.toEpochMillisOrNull(): Long? {
        val value = trim()
        if (value.isBlank()) return null

        runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()?.let { return it }
        runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()?.let { return it }
        LocalDateTimeFormats.forEach { formatter ->
            runCatching {
                LocalDateTime.parse(value, formatter)
                    .atZone(BackupZoneId)
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()?.let { return it }
        }
        return runCatching {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(BackupZoneId)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    private fun String.toEpochMillisOrZero(): Long {
        return toEpochMillisOrNull() ?: 0L
    }

    private fun Int.toTaskKind(): String {
        return when (this) {
            KudoState.TYPE_HABIT -> KIND_HABIT
            else -> KIND_TASK
        }
    }

    private fun String.toTaskType(): Int {
        return when (this) {
            KIND_HABIT -> KudoState.TYPE_HABIT
            else -> KudoState.TYPE_TASK
        }
    }

    private fun Int.toStoreKind(): String {
        return when (this) {
            KudoState.STORE_INFINITE -> STORE_REPEATABLE
            else -> STORE_ONCE
        }
    }

    private fun String.toStoreType(): Int {
        return when (this) {
            STORE_REPEATABLE -> KudoState.STORE_INFINITE
            else -> KudoState.STORE_ONCE
        }
    }

    private fun Int.toTaskSortName(): String {
        return when (this) {
            KudoState.TASK_SORT_MANUAL -> SORT_MANUAL
            else -> SORT_AUTO_DUE
        }
    }

    private fun String.toTaskSortMode(): Int {
        return when (this) {
            SORT_MANUAL -> KudoState.TASK_SORT_MANUAL
            else -> KudoState.TASK_SORT_AUTO_DUE
        }
    }

    private fun sanitizeTaskSortMode(mode: Int): Int {
        return when (mode) {
            KudoState.TASK_SORT_AUTO_DUE,
            KudoState.TASK_SORT_MANUAL -> mode
            else -> KudoState.TASK_SORT_AUTO_DUE
        }
    }
}
