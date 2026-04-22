package com.kudo.app.core.model

import org.json.JSONArray
import org.json.JSONObject

object KudoStateJson {

    private const val LEGACY_LIST_FOCUS = "focus"

    fun decodeOrNull(raw: String?): KudoState? {
        if (raw.isNullOrBlank()) {
            return null
        }

        return try {
            val root = JSONObject(raw)
            val rawTasks = root.optJSONArray("tasks").toTaskList()
            val mergedTasks = mergeLegacyLists(rawTasks)
            sanitize(
                KudoState(
                    coins = root.optInt("coins", 0),
                    tasks = mergedTasks,
                    store = root.optJSONArray("store").toStoreList(),
                    logs = root.optJSONArray("logs").toLogList(),
                    recentVals = root.optJSONArray("recentVals").toIntList(),
                    multiplier = root.optDouble("multiplier", 1.0).toFloat(),
                    taskSortMode = root.optInt("taskSortMode", KudoState.TASK_SORT_AUTO_DUE)
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    fun encode(state: KudoState): String {
        val root = JSONObject()
        root.put("coins", state.coins)
        root.put("multiplier", state.multiplier.toDouble())
        root.put("taskSortMode", state.taskSortMode)

        val recentVals = JSONArray()
        state.recentVals.forEach(recentVals::put)
        root.put("recentVals", recentVals)

        val tasks = JSONArray()
        state.tasks.forEach { task ->
            tasks.put(
                JSONObject()
                    .put("id", task.id)
                    .put("title", task.title)
                    .put("val", task.valAmount)
                    .put("type", task.type)
                    .put("count", task.count)
                    .put("last", task.last)
                    .put("order", task.order)
                    .apply {
                        task.dueAtEpochMillis?.let { put("dueAt", it) }
                        if (task.subtasks.isNotEmpty()) {
                            put("subs", task.subtasks.toJsonArray())
                        }
                    }
            )
        }
        root.put("tasks", tasks)

        val store = JSONArray()
        state.store.forEach { item ->
            store.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("cost", item.cost)
                    .put("type", item.type)
            )
        }
        root.put("store", store)

        val logs = JSONArray()
        state.logs.forEach { log ->
            val json = JSONObject()
                .put("t", log.timestamp)
                .put("txt", log.text)
                .put("v", log.value)
                .put("type", log.type)

            log.baseValue?.let { json.put("base", it) }
            log.taskId?.let { json.put("taskId", it) }
            log.subtaskId?.let { json.put("subtaskId", it) }
            if (log.isHabit) json.put("isHabit", true)
            log.itemData?.let { item ->
                json.put(
                    "itemData",
                    JSONObject()
                        .put("id", item.id)
                        .put("title", item.title)
                        .apply {
                            item.valAmount?.let { put("val", it) }
                            item.cost?.let { put("cost", it) }
                            put("type", item.type)
                            put("count", item.count)
                            put("last", item.last)
                            put("order", item.order)
                            item.dueAtEpochMillis?.let { put("dueAt", it) }
                            if (item.subtasks.isNotEmpty()) {
                                put("subs", item.subtasks.toJsonArray())
                            }
                        }
                )
            }
            logs.put(json)
        }
        root.put("logs", logs)

        return root.toString()
    }

    fun decode(raw: String?): KudoState {
        return decodeOrNull(raw) ?: KudoState()
    }

    fun sanitize(state: KudoState): KudoState {
        return state.copy(
            tasks = state.tasks.map { task ->
                task.copy(subtasks = if (task.type == KudoState.TYPE_HABIT) emptyList() else task.subtasks)
            },
            taskSortMode = sanitizeTaskSortMode(state.taskSortMode)
        )
    }

    // Merges legacy inbox/focus lists: focus tasks first (preserving their order), then inbox.
    private fun mergeLegacyLists(tasks: List<Pair<KudoTask, String?>>): List<KudoTask> {
        val focusTasks = tasks.filter { (_, list) -> list == LEGACY_LIST_FOCUS || list == null }
            .map { (task, _) -> task }
        val inboxTasks = tasks.filter { (_, list) -> list != LEGACY_LIST_FOCUS && list != null }
            .map { (task, _) -> task }
        return focusTasks + inboxTasks
    }

    private fun JSONArray?.toIntList(): List<Int> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                add(optInt(index, 0))
            }
        }
    }

    private fun JSONArray?.toTaskList(): List<Pair<KudoTask, String?>> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val id = json.optLong("id", System.currentTimeMillis() + index)
                val list = if (json.has("list")) json.optString("list") else null
                add(
                    KudoTask(
                        id = id,
                        title = json.optString("title", ""),
                        valAmount = json.optInt("val", 0),
                        type = json.optInt("type", KudoState.TYPE_TASK),
                        count = json.optInt("count", 0),
                        last = json.optLong("last", 0L),
                        order = json.optLong("order", id),
                        dueAtEpochMillis = if (json.has("dueAt")) json.optLong("dueAt") else null,
                        subtasks = json.optJSONArray("subs").toSubtaskList()
                    ) to list
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
                        cost = json.optInt("cost", 0),
                        type = json.optInt("type", KudoState.STORE_ONCE)
                    )
                )
            }
        }
    }

    private fun JSONArray?.toLogList(): List<KudoLogEntry> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val itemJson = json.optJSONObject("itemData")
                add(
                    KudoLogEntry(
                        timestamp = json.optLong("t", 0L),
                        text = json.optString("txt", ""),
                        value = json.optInt("v", 0),
                        baseValue = if (json.has("base")) json.optInt("base", 0) else null,
                        type = json.optString("type", ""),
                        taskId = if (json.has("taskId")) json.optLong("taskId") else null,
                        subtaskId = if (json.has("subtaskId")) json.optLong("subtaskId") else null,
                        isHabit = json.optBoolean("isHabit", false),
                        itemData = itemJson?.let {
                            val id = it.optLong("id", 0L)
                            KudoLogItemData(
                                id = id,
                                title = it.optString("title", ""),
                                valAmount = if (it.has("val")) it.optInt("val", 0) else null,
                                cost = if (it.has("cost")) it.optInt("cost", 0) else null,
                                type = it.optInt("type", 0),
                                count = it.optInt("count", 0),
                                last = it.optLong("last", 0L),
                                order = it.optLong("order", id),
                                dueAtEpochMillis = if (it.has("dueAt")) it.optLong("dueAt") else null,
                                subtasks = it.optJSONArray("subs").toSubtaskList()
                            )
                        }
                    )
                )
            }
        }
    }

    private fun JSONArray?.toSubtaskList(): List<KudoSubtask> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val id = json.optLong("id", System.currentTimeMillis() + index)
                add(
                    KudoSubtask(
                        id = id,
                        title = json.optString("title", ""),
                        valAmount = json.optInt("val", 0),
                        completedAt = if (json.has("completedAt")) json.optLong("completedAt") else null
                    )
                )
            }
        }
    }

    private fun List<KudoSubtask>.toJsonArray(): JSONArray {
        return JSONArray().apply {
            this@toJsonArray.forEach { subtask ->
                put(
                    JSONObject()
                        .put("id", subtask.id)
                        .put("title", subtask.title)
                        .put("val", subtask.valAmount)
                        .apply {
                            subtask.completedAt?.let { put("completedAt", it) }
                        }
                )
            }
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
