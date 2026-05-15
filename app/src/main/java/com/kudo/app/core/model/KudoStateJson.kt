package com.kudo.app.core.model

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            val (tasks, legacyHabits) = root.optJSONArray("tasks").toTaskOrLegacyHabitList()
            val habits = root.optJSONArray("habits").toHabitList() + legacyHabits
            sanitize(
                KudoState(
                    coins = settings.optInt("coins", root.optInt("coins", 0)),
                    tasks = tasks,
                    habits = habits,
                    store = root.optJSONArray("store").toStoreList(),
                    logs = logs,
                    recentCoins = logs.toRecentCoins(),
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
                    .put("taskSort", state.taskSortMode.toJsonName())
            )
            .put("tasks", state.tasks.toTaskJsonArray())
            .put("habits", state.habits.toHabitJsonArray())
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
            logs = state.logs.sortedByDescending(KudoLogEntry::timestamp)
        )
    }

    private fun List<KudoTask>.toTaskJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toTaskJsonArray.forEach { task ->
                put(
                    JSONObject()
                        .put("id", task.id)
                        .put("title", task.title)
                        .put("coins", task.coins)
                        .apply {
                            task.dueAtEpochMillis?.let { put("due", it.toIsoString()) }
                            if (task.subtasks.isNotEmpty()) {
                                put("subtasks", task.subtasks.toSubtaskJsonArray())
                            }
                        }
                )
            }
        }
    }

    private fun List<KudoHabit>.toHabitJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toHabitJsonArray.forEach { habit ->
                put(
                    JSONObject()
                        .put("id", habit.id)
                        .put("title", habit.title)
                        .put("coins", habit.coins)
                        .put("count", habit.count)
                        .apply {
                            habit.last.toIsoStringOrNull()?.let { put("lastDone", it) }
                        }
                )
            }
        }
    }

    /**
     * Reads the `tasks` array. The legacy format placed both tasks and habits in
     * a single array discriminated by `kind`, so the second item in the result is
     * any habit entries lifted out for callers to merge with the `habits` array.
     */
    private fun JSONArray?.toTaskOrLegacyHabitList(): Pair<List<KudoTask>, List<KudoHabit>> {
        if (this == null) return emptyList<KudoTask>() to emptyList()
        val tasks = mutableListOf<KudoTask>()
        val habits = mutableListOf<KudoHabit>()
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            val kindName = json.optString("kind", json.optString("type", KIND_TASK))
            if (kindName == KIND_HABIT) {
                habits += json.toHabit(index)
            } else {
                tasks += json.toTask(index)
            }
        }
        return tasks to habits
    }

    private fun JSONArray?.toHabitList(): List<KudoHabit> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                add(json.toHabit(index))
            }
        }
    }

    private fun JSONObject.toTask(index: Int): KudoTask {
        val id = optLong("id", KudoIds.next())
        return KudoTask(
            id = id,
            title = optString("title", ""),
            coins = optInt("coins", optInt("value", 0)),
            order = optLong("order", index.toLong()),
            dueAtEpochMillis = (optStringOrNull("due") ?: optStringOrNull("dueAt"))
                ?.toEpochMillisOrNull(),
            subtasks = optJSONArray("subtasks").toSubtaskList()
        )
    }

    private fun JSONObject.toHabit(index: Int): KudoHabit {
        val id = optLong("id", KudoIds.next())
        return KudoHabit(
            id = id,
            title = optString("title", ""),
            coins = optInt("coins", optInt("value", 0)),
            count = optInt("count", 0),
            last = optStringOrNull("lastDone")
                ?.toEpochMillisOrNull()
                ?: optString("lastCompletedAt", "").toEpochMillisOrZero(),
            order = optLong("order", index.toLong())
        )
    }

    private fun List<KudoSubtask>.toSubtaskJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toSubtaskJsonArray.forEach { subtask ->
                put(
                    JSONObject()
                        .put("id", subtask.id)
                        .put("title", subtask.title)
                        .put("coins", subtask.coins)
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
                        id = json.optLong("id", KudoIds.next()),
                        title = json.optString("title", ""),
                        coins = json.optInt("coins", json.optInt("value", 0)),
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
                        .put("kind", item.kind.toJsonName())
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
                        id = json.optLong("id", KudoIds.next()),
                        title = json.optString("title", ""),
                        cost = json.optInt("coins", json.optInt("cost", 0)),
                        kind = json.optString("kind", json.optString("type", STORE_ONCE)).toStoreKind()
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
                        .put("coins", log.coins)
                        .put("kind", log.kind.toJsonName())
                        .apply {
                            log.baseCoins?.let { put("baseCoins", it) }
                            log.subtaskId?.let { put("subtaskId", it) }
                            log.subject?.let { put("undo", it.toUndoJson()) }
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
                val kindName = json.optString("kind", json.optString("type", ""))
                val undoJson = json.optJSONObject("undo")
                val legacyIsHabit = json.optBoolean("isHabit", false) ||
                    undoJson?.optString("kind") == KIND_HABIT
                val logKind = kindName.toLogKind(legacyIsHabit)
                val subject = undoJson?.toLogSubject(logKind)
                add(
                    KudoLogEntry(
                        timestamp = (json.optStringOrNull("time") ?: json.optString("timestamp", ""))
                            .toEpochMillisOrZero(),
                        text = json.optString("title", json.optString("text", "")),
                        coins = json.optInt("coins", json.optInt("value", 0)),
                        baseCoins = if (json.has("baseCoins")) {
                            json.optInt("baseCoins", 0)
                        } else if (json.has("baseValue")) {
                            json.optInt("baseValue", 0)
                        } else {
                            null
                        },
                        kind = logKind,
                        taskId = if (logKind == KudoLogKind.Store) null else undoJson?.optLong("id"),
                        subtaskId = if (json.has("subtaskId")) {
                            json.optLong("subtaskId")
                        } else if (json.has("completedSubtaskId")) {
                            json.optLong("completedSubtaskId")
                        } else {
                            null
                        },
                        subject = subject
                    )
                )
            }
        }
    }

    private fun KudoLogSubject.toUndoJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .apply {
                when (val subject = this@toUndoJson) {
                    is KudoLogSubject.Task -> {
                        put("title", subject.title)
                        put("coins", subject.coins)
                        put("kind", KIND_TASK)
                        subject.dueAtEpochMillis?.let { put("due", it.toIsoString()) }
                        if (subject.subtasks.isNotEmpty()) {
                            put("subtasks", subject.subtasks.toSubtaskJsonArray())
                        }
                    }

                    is KudoLogSubject.Habit -> {
                        put("title", subject.title)
                        put("coins", subject.coins)
                        put("kind", KIND_HABIT)
                        put("count", subject.count)
                        subject.last.toIsoStringOrNull()?.let { put("lastDone", it) }
                    }

                    is KudoLogSubject.Store -> {
                        put("title", subject.title)
                        put("coins", subject.cost)
                        put("kind", subject.kind.toJsonName())
                    }
                }
            }
    }

    private fun JSONObject.toLogSubject(logKind: KudoLogKind): KudoLogSubject {
        val id = optLong("id", 0L)
        return when (logKind) {
            KudoLogKind.Store -> KudoLogSubject.Store(
                id = id,
                title = optString("title", ""),
                cost = optInt("coins", 0),
                kind = optString("kind", STORE_ONCE).toStoreKind()
            )

            KudoLogKind.Habit -> KudoLogSubject.Habit(
                id = id,
                title = optString("title", ""),
                coins = optInt("coins", 0),
                count = optInt("count", 0),
                last = optString("lastDone", "").toEpochMillisOrZero(),
                order = optLong("order", id)
            )

            KudoLogKind.Task -> KudoLogSubject.Task(
                id = id,
                title = optString("title", ""),
                coins = optInt("coins", 0),
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
                val id = json.optLong("id", KudoIds.next())
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

    private fun List<KudoLogEntry>.toRecentCoins(): List<Int> {
        return asSequence()
            .filter { (it.kind == KudoLogKind.Task || it.kind == KudoLogKind.Habit) && it.coins > 0 }
            .sortedBy(KudoLogEntry::timestamp)
            .map { it.baseCoins ?: it.coins }
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

    private fun KudoStoreKind.toJsonName(): String = when (this) {
        KudoStoreKind.Repeatable -> STORE_REPEATABLE
        KudoStoreKind.Once -> STORE_ONCE
    }

    private fun String.toStoreKind(): KudoStoreKind = when (this) {
        STORE_REPEATABLE -> KudoStoreKind.Repeatable
        else -> KudoStoreKind.Once
    }

    private fun KudoLogKind.toJsonName(): String = when (this) {
        KudoLogKind.Task -> KIND_TASK
        KudoLogKind.Habit -> KIND_HABIT
        KudoLogKind.Store -> KIND_STORE
    }

    private fun String.toLogKind(legacyIsHabit: Boolean = false): KudoLogKind = when {
        this == KIND_STORE -> KudoLogKind.Store
        this == KIND_HABIT -> KudoLogKind.Habit
        legacyIsHabit -> KudoLogKind.Habit
        else -> KudoLogKind.Task
    }

    private fun KudoTaskSortMode.toJsonName(): String = when (this) {
        KudoTaskSortMode.Manual -> SORT_MANUAL
        KudoTaskSortMode.AutoDue -> SORT_AUTO_DUE
    }

    private fun String.toTaskSortMode(): KudoTaskSortMode = when (this) {
        SORT_MANUAL -> KudoTaskSortMode.Manual
        else -> KudoTaskSortMode.AutoDue
    }
}
