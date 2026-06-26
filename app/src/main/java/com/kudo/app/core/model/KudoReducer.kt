package com.kudo.app.core.model

import kotlin.math.floor

object KudoReducer {

    fun addTask(
        state: KudoState,
        title: String,
        value: Int,
        type: Int,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val nextOrder = if (
            type == KudoState.TYPE_TASK &&
            state.taskSortMode == KudoState.TASK_SORT_MANUAL
        ) {
            state.tasks
                .asSequence()
                .filter { it.type == KudoState.TYPE_TASK }
                .maxOfOrNull(KudoTask::order)
                ?.plus(1L)
                ?: 0L
        } else {
            now
        }
        val item = KudoTask(
            id = now,
            title = title,
            valAmount = value,
            type = type,
            count = 0,
            last = 0L,
            order = nextOrder
        )
        return KudoStateJson.sanitize(
            state.copy(tasks = state.tasks + listOf(item))
        )
    }

    fun addImportedTasks(
        state: KudoState,
        drafts: List<KudoTaskImportDraft>,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        if (drafts.isEmpty()) return state

        val firstOrder = if (state.taskSortMode == KudoState.TASK_SORT_MANUAL) {
            state.tasks
                .asSequence()
                .filter { it.type == KudoState.TYPE_TASK }
                .maxOfOrNull(KudoTask::order)
                ?.plus(1L)
                ?: 0L
        } else {
            now
        }
        val imported = drafts.mapIndexed { index, draft ->
            KudoTask(
                id = now + index,
                title = draft.title,
                valAmount = draft.value,
                type = KudoState.TYPE_TASK,
                count = 0,
                last = 0L,
                order = firstOrder + index
            )
        }
        return KudoStateJson.sanitize(
            state.copy(tasks = state.tasks + imported)
        )
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

    fun completeTask(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        val task = state.tasks.firstOrNull { it.id == id } ?: return state
        val baseValue = task.remainingValue
        val reward = floor(baseValue * state.multiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, baseValue)
        val log = KudoLogEntry(
            timestamp = now,
            text = task.title,
            value = reward,
            baseValue = baseValue,
            type = "task",
            taskId = task.id,
            itemData = KudoLogItemData.fromTask(task)
        )
        val remainingTasks = if (task.type == KudoState.TYPE_TASK) {
            grown.tasks.filterNot { it.id == id }
        } else {
            grown.tasks
        }

        return grown.copy(
            tasks = remainingTasks,
            logs = listOf(log) + grown.logs
        )
    }

    fun completeHabit(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        val habit = state.tasks.firstOrNull { it.id == id } ?: return state
        val reward = floor(habit.valAmount * state.multiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, habit.valAmount)
        val updatedTasks = grown.tasks.map { current ->
            if (current.id == id) {
                current.copy(
                    last = now,
                    count = current.count + 1
                )
            } else {
                current
            }
        }
        val snapshot = updatedTasks.firstOrNull { it.id == id } ?: habit
        val log = KudoLogEntry(
            timestamp = now,
            text = habit.title,
            value = reward,
            baseValue = habit.valAmount,
            type = "task",
            taskId = habit.id,
            isHabit = true,
            itemData = KudoLogItemData.fromTask(snapshot)
        )
        return grown.copy(
            tasks = updatedTasks,
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
        var nextState = state.copy(coins = state.coins - log.value)

        log.itemData?.let { itemData ->
            when (log.type) {
                "task" -> {
                    val exists = nextState.tasks.firstOrNull { it.id == itemData.id }
                    val snapshot = itemData.toTask()
                    nextState = if (exists == null && itemData.type == KudoState.TYPE_TASK) {
                        nextState.copy(tasks = nextState.tasks + snapshot)
                    } else if (exists != null && log.isHabit) {
                        nextState.copy(
                            tasks = nextState.tasks.map { task ->
                                if (task.id == itemData.id) {
                                    task.copy(count = (task.count - 1).coerceAtLeast(0))
                                } else {
                                    task
                                }
                            }
                        )
                    } else {
                        nextState
                    }
                }

                "store" -> {
                    val exists = nextState.store.firstOrNull { it.id == itemData.id }
                    if (exists == null && itemData.type == KudoState.STORE_ONCE) {
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

    fun reorderHabits(state: KudoState, orderedIds: List<Long>): KudoState {
        return reorderTaskSubset(
            state = state,
            orderedIds = orderedIds
        ) { task ->
            task.type == KudoState.TYPE_HABIT
        }
    }

    fun reorderTasks(state: KudoState, orderedIds: List<Long>): KudoState {
        return reorderTaskSubset(
            state = state,
            orderedIds = orderedIds
        ) { task ->
            task.type == KudoState.TYPE_TASK
        }
    }

    fun resetTaskOrder(state: KudoState): KudoState {
        val orderedIds = state.tasks
            .asSequence()
            .filter { it.type == KudoState.TYPE_TASK }
            .sortedWith(
                compareBy<KudoTask>(
                    { it.dueAtEpochMillis == null },
                    { it.dueAtEpochMillis ?: Long.MAX_VALUE },
                    { it.id }
                )
            )
            .map(KudoTask::id)
            .toList()
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

    private fun reorderTaskSubset(
        state: KudoState,
        orderedIds: List<Long>,
        predicate: (KudoTask) -> Boolean
    ): KudoState {
        val allTasks = state.tasks
        val targetIndices = allTasks.mapIndexedNotNull { index, task -> if (predicate(task)) index else null }
        if (targetIndices.isEmpty()) return state

        val taskMap = allTasks.associateBy { it.id }
        // Reorder the matched tasks according to orderedIds
        val reorderedTargets = orderedIds.mapNotNull { taskMap[it] }.filter(predicate)
        // Tasks that matched the predicate but weren't in orderedIds (fallback)
        val remainingTargets = allTasks.filter(predicate).filterNot { it.id in orderedIds }
        val newTargets = reorderedTargets + remainingTargets

        val resultTasks = allTasks.toMutableList()
        targetIndices.forEachIndexed { i, originalIndex ->
            resultTasks[originalIndex] = newTargets[i]
        }

        return state.copy(
            tasks = resultTasks.mapIndexed { index, task ->
                task.copy(order = index.toLong())
            }
        )
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
