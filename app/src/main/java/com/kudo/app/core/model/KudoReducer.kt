package com.kudo.app.core.model

import kotlin.math.floor

object KudoReducer {

    fun addTask(
        state: KudoState,
        title: String,
        value: Int,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val nextOrder = if (state.taskSortMode == KudoState.TASK_SORT_MANUAL) {
            state.tasks.maxOfOrNull(KudoTask::order)?.plus(1L) ?: 0L
        } else {
            now
        }
        val item = KudoTask(
            id = now,
            title = title,
            valAmount = value,
            order = nextOrder
        )
        return state.copy(tasks = state.tasks + item)
    }

    fun addImportedTasks(
        state: KudoState,
        drafts: List<KudoTaskImportDraft>,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        if (drafts.isEmpty()) return state

        val firstOrder = if (state.taskSortMode == KudoState.TASK_SORT_MANUAL) {
            state.tasks.maxOfOrNull(KudoTask::order)?.plus(1L) ?: 0L
        } else {
            now
        }
        val imported = drafts.mapIndexed { index, draft ->
            KudoTask(
                id = now + index,
                title = draft.title,
                valAmount = draft.value,
                order = firstOrder + index
            )
        }
        return state.copy(tasks = state.tasks + imported)
    }

    fun addStoreItem(
        state: KudoState,
        title: String,
        cost: Int,
        type: Int,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val item = KudoStoreItem(
            id = now,
            title = title,
            cost = cost,
            type = type
        )
        return state.copy(store = listOf(item) + state.store)
    }

    fun toggleTimer(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        if (state.tasks.none { it.id == id }) return state

        return state.copy(
            tasks = state.tasks.map { current ->
                when {
                    current.id == id && current.isTimerRunning -> pauseTimer(current, now)
                    current.id == id -> current.copy(
                        isTimerRunning = true,
                        lastTimerStart = now
                    )
                    current.isTimerRunning -> pauseTimer(current, now)
                    else -> current
                }
            }
        )
    }

    private fun pauseTimer(task: KudoTask, now: Long): KudoTask {
        val elapsed = (now - task.lastTimerStart).coerceAtLeast(0L)
        return task.copy(
            isTimerRunning = false,
            accumulatedTimeMillis = task.accumulatedTimeMillis + elapsed,
            lastTimerStart = 0L
        )
    }

    fun completeTask(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        val task = state.tasks.firstOrNull { it.id == id } ?: return state
        val runningElapsed = if (task.isTimerRunning) {
            (now - task.lastTimerStart).coerceAtLeast(0L)
        } else {
            0L
        }
        val totalMillis = (task.accumulatedTimeMillis + runningElapsed).coerceAtLeast(0L)
        val baseValue = if (task.valAmount > 0) {
            task.valAmount
        } else {
            (totalMillis / 60_000L).toInt()
        }

        val reward = floor(baseValue * state.multiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, baseValue)
        val completedSnapshot = task.copy(
            isTimerRunning = false,
            accumulatedTimeMillis = totalMillis,
            lastTimerStart = 0L
        )
        val log = KudoLogEntry(
            timestamp = now,
            text = task.title,
            value = reward,
            baseValue = baseValue,
            type = "task",
            taskId = task.id,
            itemData = KudoLogItemData.fromTask(completedSnapshot)
        )

        return grown.copy(
            tasks = grown.tasks.filterNot { it.id == id },
            logs = listOf(log) + grown.logs
        )
    }

    fun buyItem(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        val item = state.store.firstOrNull { it.id == id } ?: return state
        if (state.coins < item.cost) return state

        return state.copy(
            coins = state.coins - item.cost,
            store = if (item.type == KudoState.STORE_ONCE) {
                state.store.filterNot { it.id == id }
            } else {
                state.store
            },
            logs = listOf(
                KudoLogEntry(
                    timestamp = now,
                    text = item.title,
                    value = -item.cost,
                    type = "store",
                    itemData = KudoLogItemData.fromStoreItem(item)
                )
            ) + state.logs
        )
    }

    fun undoLog(state: KudoState, index: Int): KudoState {
        val log = state.logs.getOrNull(index) ?: return state
        if (!log.undoable) return state

        var nextState = state.copy(coins = state.coins - log.value)
        log.itemData?.let { itemData ->
            when (log.type) {
                "task" -> {
                    if (nextState.tasks.none { it.id == itemData.id }) {
                        nextState = nextState.copy(tasks = nextState.tasks + itemData.toTask())
                    }
                }

                "store" -> {
                    if (
                        nextState.store.none { it.id == itemData.id } &&
                        itemData.storeType == KudoState.STORE_ONCE
                    ) {
                        nextState = nextState.copy(store = nextState.store + itemData.toStoreItem())
                    }
                }
            }
        }

        return nextState.copy(logs = nextState.logs.filterIndexed { logIndex, _ -> logIndex != index })
    }

    fun updateTask(
        state: KudoState,
        id: Long,
        title: String,
        value: Int,
        dueAtEpochMillis: Long?
    ): KudoState {
        return state.copy(
            tasks = state.tasks.map { task ->
                if (task.id == id) {
                    task.copy(
                        title = title.ifBlank { task.title },
                        valAmount = value,
                        dueAtEpochMillis = dueAtEpochMillis
                    )
                } else {
                    task
                }
            }
        )
    }

    fun setTaskSortMode(state: KudoState, sortMode: Int): KudoState {
        val sanitized = when (sortMode) {
            KudoState.TASK_SORT_MANUAL -> KudoState.TASK_SORT_MANUAL
            else -> KudoState.TASK_SORT_AUTO_DUE
        }
        return state.copy(taskSortMode = sanitized)
    }

    fun updateStoreItem(state: KudoState, id: Long, title: String, cost: Int): KudoState {
        return state.copy(
            store = state.store.map { item ->
                if (item.id == id) {
                    item.copy(
                        title = title.ifBlank { item.title },
                        cost = cost
                    )
                } else {
                    item
                }
            }
        )
    }

    fun deleteTask(state: KudoState, id: Long): KudoState {
        return state.copy(tasks = state.tasks.filterNot { it.id == id })
    }

    fun deleteStoreItem(state: KudoState, id: Long): KudoState {
        return state.copy(store = state.store.filterNot { it.id == id })
    }

    fun reorderTasks(state: KudoState, orderedIds: List<Long>): KudoState {
        val taskMap = state.tasks.associateBy { it.id }
        val reordered = orderedIds.mapNotNull(taskMap::get)
        val leftovers = state.tasks.filterNot { it.id in orderedIds.toSet() }
        return state.copy(
            tasks = (reordered + leftovers).mapIndexed { index, task -> task.copy(order = index.toLong()) }
        )
    }

    fun resetTaskOrder(state: KudoState): KudoState {
        val orderedIds = state.tasks
            .sortedWith(
                compareBy<KudoTask>(
                    { it.dueAtEpochMillis == null },
                    { it.dueAtEpochMillis ?: Long.MAX_VALUE },
                    { it.id }
                )
            )
            .map(KudoTask::id)
        if (orderedIds.isEmpty()) return state

        return setTaskSortMode(
            state = reorderTasks(state, orderedIds),
            sortMode = KudoState.TASK_SORT_MANUAL
        )
    }

    fun reorderStore(state: KudoState, orderedIds: List<Long>): KudoState {
        val itemMap = state.store.associateBy { it.id }
        val reordered = orderedIds.mapNotNull(itemMap::get)
        val leftovers = state.store.filterNot { it.id in orderedIds.toSet() }
        return state.copy(store = reordered + leftovers)
    }

    private fun processGrowth(state: KudoState, value: Int): KudoState {
        val average = if (state.recentVals.isNotEmpty()) {
            state.recentVals.average()
        } else {
            value.toDouble()
        }
        val updatedMultiplier = if (value >= average) {
            (state.multiplier + 0.01f).coerceAtMost(1.20f)
        } else {
            (state.multiplier - 0.01f).coerceAtLeast(1.00f)
        }
        val updatedRecentVals = (state.recentVals + value).takeLast(5)

        return state.copy(
            multiplier = updatedMultiplier,
            recentVals = updatedRecentVals
        )
    }
}
