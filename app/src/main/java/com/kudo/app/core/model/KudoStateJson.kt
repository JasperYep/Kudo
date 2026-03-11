package com.kudo.app.core.model

import org.json.JSONArray
import org.json.JSONObject

object KudoStateJson {

    fun decodeOrNull(raw: String?): KudoState? {
        if (raw.isNullOrBlank()) {
            return null
        }

        return try {
            val root = JSONObject(raw)
            sanitize(
                KudoState(
                    coins = root.optInt("coins", 0),
                    life = root.optInt("life", 0),
                    maxCoins = root.optInt("maxCoins", 0),
                    tasks = root.optJSONArray("tasks").toTaskList(),
                    store = root.optJSONArray("store").toStoreList(),
                    logs = root.optJSONArray("logs").toLogList(),
                    recentVals = root.optJSONArray("recentVals").toIntList(),
                    multiplier = root.optDouble("multiplier", 1.0).toFloat(),
                    listMode = root.optString("listMode", KudoState.LIST_FOCUS)
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    fun encode(state: KudoState): String {
        val root = JSONObject()
        root.put("coins", state.coins)
        root.put("life", state.life)
        root.put("maxCoins", state.maxCoins)
        root.put("multiplier", state.multiplier.toDouble())
        root.put("listMode", state.listMode)

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
                    .put("list", task.list)
                    .put("order", task.order)
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

            log.taskId?.let { json.put("taskId", it) }
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
                            put("list", item.list)
                            put("order", item.order)
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
        val sanitizedTasks = state.tasks.map { task ->
            task.copy(
                list = when (task.list) {
                    KudoState.LIST_FOCUS,
                    KudoState.LIST_INBOX -> task.list
                    else -> KudoState.LIST_FOCUS
                },
                order = if (task.order == 0L) task.id else task.order
            )
        }

        return state.copy(
            tasks = sanitizedTasks,
            listMode = when (state.listMode) {
                KudoState.LIST_FOCUS,
                KudoState.LIST_INBOX -> state.listMode
                else -> KudoState.LIST_FOCUS
            }
        )
    }

    private fun JSONArray?.toIntList(): List<Int> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                add(optInt(index, 0))
            }
        }
    }

    private fun JSONArray?.toTaskList(): List<KudoTask> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val id = json.optLong("id", System.currentTimeMillis() + index)
                add(
                    KudoTask(
                        id = id,
                        title = json.optString("title", ""),
                        valAmount = json.optInt("val", 0),
                        type = json.optInt("type", KudoState.TYPE_TASK),
                        count = json.optInt("count", 0),
                        last = json.optLong("last", 0L),
                        list = json.optString("list", KudoState.LIST_FOCUS),
                        order = json.optLong("order", id)
                    )
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
                        type = json.optString("type", ""),
                        taskId = if (json.has("taskId")) json.optLong("taskId") else null,
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
                                list = it.optString("list", KudoState.LIST_FOCUS),
                                order = it.optLong("order", id)
                            )
                        }
                    )
                )
            }
        }
    }
}
