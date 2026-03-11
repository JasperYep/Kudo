package com.kudo.app.core.model

import kotlin.math.floor

object KudoReducer {

    data class MoveTaskResult(
        val state: KudoState,
        val requiresValue: Boolean = false
    )

    fun addTask(
        state: KudoState,
        title: String,
        value: Int,
        type: Int,
        list: String,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val item = KudoTask(
            id = now,
            title = title,
            valAmount = value,
            type = type,
            count = 0,
            last = 0L,
            list = if (type == KudoState.TYPE_HABIT) KudoState.LIST_FOCUS else list,
            order = now
        )
        return KudoStateJson.sanitize(
            state.copy(tasks = listOf(item) + state.tasks)
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

    fun moveTask(
        state: KudoState,
        id: Long,
        assignedValueForInboxToFocus: Int? = null
    ): MoveTaskResult {
        val task = state.tasks.firstOrNull { it.id == id } ?: return MoveTaskResult(state)

        if (task.list == KudoState.LIST_INBOX) {
            if (task.valAmount == 0 && assignedValueForInboxToFocus == null) {
                return MoveTaskResult(state = state, requiresValue = true)
            }

            val updated = state.tasks.map { current ->
                if (current.id != id) {
                    current
                } else {
                    current.copy(
                        valAmount = assignedValueForInboxToFocus ?: current.valAmount,
                        list = KudoState.LIST_FOCUS
                    )
                }
            }
            return MoveTaskResult(state.copy(tasks = updated))
        }

        val updated = state.tasks.map { current ->
            if (current.id == id) current.copy(list = KudoState.LIST_INBOX) else current
        }
        return MoveTaskResult(state.copy(tasks = updated))
    }

    fun completeTask(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        val task = state.tasks.firstOrNull { it.id == id } ?: return state
        val reward = floor(task.valAmount * state.finalMultiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, task.valAmount)
        val log = KudoLogEntry(
            timestamp = now,
            text = task.title,
            value = reward,
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
        val reward = floor(habit.valAmount * state.finalMultiplier).toInt()
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

        val nextState = state.copy(
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
        return nextState
    }

    fun undoLog(state: KudoState, index: Int): KudoState {
        val log = state.logs.getOrNull(index) ?: return state
        var nextState = state.copy(coins = state.coins - log.value)

        if (log.value > 0 && log.type == "task") {
            val base = log.itemData?.valAmount ?: 0
            nextState = nextState.copy(
                life = nextState.life - base,
                maxCoins = nextState.maxCoins - base
            )
        }

        log.itemData?.let { itemData ->
            when (log.type) {
                "task" -> {
                    val exists = nextState.tasks.firstOrNull { it.id == itemData.id }
                    nextState = if (exists == null && itemData.type == KudoState.TYPE_TASK) {
                        nextState.copy(tasks = nextState.tasks + itemData.toTask())
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

    fun updateTask(state: KudoState, id: Long, title: String, value: Int): KudoState {
        return state.copy(
            tasks = state.tasks.map { task ->
                if (task.id == id) {
                    task.copy(
                        title = title.ifBlank { task.title },
                        valAmount = value
                    )
                } else {
                    task
                }
            }
        )
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

    fun moveTaskItem(state: KudoState, fromId: Long, toId: Long): KudoState {
        val fromIndex = state.tasks.indexOfFirst { it.id == fromId }
        val toIndex = state.tasks.indexOfFirst { it.id == toId }
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) {
            return state
        }

        val mutable = state.tasks.toMutableList()
        val item = mutable.removeAt(fromIndex)
        val insertIndex = if (fromIndex < toIndex) toIndex else toIndex
        mutable.add(insertIndex, item)
        return state.copy(
            tasks = mutable.mapIndexed { index, task ->
                task.copy(order = index.toLong())
            }
        )
    }

    fun moveStoreItem(state: KudoState, fromId: Long, toId: Long): KudoState {
        val fromIndex = state.store.indexOfFirst { it.id == fromId }
        val toIndex = state.store.indexOfFirst { it.id == toId }
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) {
            return state
        }

        val mutable = state.store.toMutableList()
        val item = mutable.removeAt(fromIndex)
        val insertIndex = if (fromIndex < toIndex) toIndex else toIndex
        mutable.add(insertIndex, item)
        return state.copy(store = mutable)
    }

    fun reorderHabits(state: KudoState, orderedIds: List<Long>): KudoState {
        return reorderTaskSubset(
            state = state,
            orderedIds = orderedIds
        ) { task ->
            task.type == KudoState.TYPE_HABIT
        }
    }

    fun reorderTasks(state: KudoState, listMode: String, orderedIds: List<Long>): KudoState {
        return reorderTaskSubset(
            state = state,
            orderedIds = orderedIds
        ) { task ->
            task.type == KudoState.TYPE_TASK && task.list == listMode
        }
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
        val targetTasks = state.tasks.filter(predicate)
        if (targetTasks.isEmpty()) return state

        val taskMap = state.tasks.associateBy { it.id }
        val orderedIdSet = orderedIds.toSet()
        val reorderedTargets = orderedIds.mapNotNull(taskMap::get).filter(predicate)
        val remainingTargets = targetTasks.filterNot { it.id in orderedIdSet }
        val replacementIterator = (reorderedTargets + remainingTargets).iterator()

        return state.copy(
            tasks = state.tasks.map { task ->
                if (predicate(task) && replacementIterator.hasNext()) {
                    replacementIterator.next()
                } else {
                    task
                }
            }.mapIndexed { index, task ->
                task.copy(order = index.toLong())
            }
        )
    }

    private fun processGrowth(state: KudoState, value: Int): KudoState {
        val updatedLife = state.life + value
        val updatedMaxCoins = maxOf(state.maxCoins, state.coins + value)
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
            life = updatedLife,
            maxCoins = updatedMaxCoins,
            multiplier = updatedMultiplier,
            recentVals = updatedRecentVals
        )
    }
}
