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
        val resolvedList = if (type == KudoState.TYPE_HABIT) KudoState.LIST_FOCUS else list
        val nextOrder = if (
            type == KudoState.TYPE_TASK &&
            state.taskSortModeFor(resolvedList) == KudoState.TASK_SORT_MANUAL
        ) {
            state.tasks
                .asSequence()
                .filter { it.type == KudoState.TYPE_TASK && it.list == resolvedList }
                .minOfOrNull(KudoTask::order)
                ?.minus(1L)
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
            list = resolvedList,
            order = nextOrder
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

            val destinationList = KudoState.LIST_FOCUS
            val updated = state.tasks.map { current ->
                if (current.id != id) {
                    current
                } else {
                    current.copy(
                        valAmount = assignedValueForInboxToFocus ?: current.valAmount,
                        list = destinationList,
                        order = nextTaskOrderForList(state, destinationList, current.order)
                    )
                }
            }
            return MoveTaskResult(state.copy(tasks = updated))
        }

        val destinationList = KudoState.LIST_INBOX
        val updated = state.tasks.map { current ->
            if (current.id == id) {
                current.copy(
                    list = destinationList,
                    order = nextTaskOrderForList(state, destinationList, current.order)
                )
            } else {
                current
            }
        }
        return MoveTaskResult(state.copy(tasks = updated))
    }

    fun completeTask(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        val task = state.tasks.firstOrNull { it.id == id } ?: return state
        val baseValue = task.remainingValue
        val reward = floor(baseValue * state.finalMultiplier).toInt()
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

    fun completeSubtask(
        state: KudoState,
        taskId: Long,
        subtaskId: Long,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val task = state.tasks.firstOrNull { it.id == taskId } ?: return state
        val subtask = task.subtasks.firstOrNull { it.id == subtaskId && !it.isCompleted } ?: return state
        val reward = floor(subtask.valAmount * state.finalMultiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, subtask.valAmount)
        val updatedTask = task.copy(
            subtasks = task.subtasks.map { current ->
                if (current.id == subtaskId) {
                    current.copy(completedAt = now)
                } else {
                    current
                }
            }
        )
        val log = KudoLogEntry(
            timestamp = now,
            text = "${task.title}: ${subtask.title}",
            value = reward,
            baseValue = subtask.valAmount,
            type = "task",
            taskId = task.id,
            subtaskId = subtask.id,
            itemData = KudoLogItemData.fromTask(task)
        )
        val updatedTasks = if (updatedTask.remainingValue == 0) {
            grown.tasks.filterNot { it.id == taskId }
        } else {
            grown.tasks.map { current ->
                if (current.id == taskId) {
                    updatedTask
                } else {
                    current
                }
            }
        }
        return grown.copy(
            tasks = updatedTasks,
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
            val base = log.baseValue ?: log.itemData?.valAmount ?: 0
            nextState = nextState.copy(
                life = nextState.life - base,
                maxCoins = nextState.maxCoins - base
            )
        }

        log.itemData?.let { itemData ->
            when (log.type) {
                "task" -> {
                    val exists = nextState.tasks.firstOrNull { it.id == itemData.id }
                    val snapshot = itemData.toTask()
                    nextState = if (log.subtaskId != null && itemData.type == KudoState.TYPE_TASK) {
                        nextState.copy(tasks = restoreTaskSnapshot(nextState.tasks, snapshot))
                    } else if (exists == null && itemData.type == KudoState.TYPE_TASK) {
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
        dueEpochDay: Long?,
        subtaskDrafts: List<KudoSubtaskDraft>? = null,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        return state.copy(
            tasks = state.tasks.map { task ->
                if (task.id == id) {
                    val isLocked = task.isSubtaskStructureLocked
                    val resolvedSubtasks = if (task.type == KudoState.TYPE_TASK && subtaskDrafts == null) {
                        task.subtasks
                    } else if (task.type == KudoState.TYPE_TASK && !isLocked) {
                        createWeightedSubtasks(
                            drafts = subtaskDrafts.orEmpty(),
                            totalValue = value,
                            now = now
                        )
                    } else {
                        task.subtasks
                    }
                    task.copy(
                        title = title.ifBlank { task.title },
                        valAmount = if (isLocked) task.valAmount else value,
                        dueEpochDay = dueEpochDay,
                        subtasks = resolvedSubtasks
                    )
                } else {
                    task
                }
            }
        )
    }

    fun setTaskSortMode(state: KudoState, listMode: String, sortMode: Int): KudoState {
        val sanitizedSortMode = when (sortMode) {
            KudoState.TASK_SORT_MANUAL -> KudoState.TASK_SORT_MANUAL
            else -> KudoState.TASK_SORT_AUTO_DUE
        }
        return when (listMode) {
            KudoState.LIST_INBOX -> state.copy(inboxSortMode = sanitizedSortMode)
            else -> state.copy(focusSortMode = sanitizedSortMode)
        }
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

    fun resetTaskOrder(state: KudoState, listMode: String): KudoState {
        val orderedIds = state.tasks
            .asSequence()
            .filter { it.type == KudoState.TYPE_TASK && it.list == listMode }
            .sortedWith(
                compareBy<KudoTask>(
                    { it.dueEpochDay == null },
                    { it.dueEpochDay ?: Long.MAX_VALUE },
                    { it.id }
                )
            )
            .map(KudoTask::id)
            .toList()
        if (orderedIds.isEmpty()) return state

        return setTaskSortMode(
            state = reorderTasks(state, listMode, orderedIds),
            listMode = listMode,
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

    private fun nextTaskOrderForList(
        state: KudoState,
        list: String,
        fallback: Long
    ): Long {
        if (state.taskSortModeFor(list) != KudoState.TASK_SORT_MANUAL) {
            return fallback
        }
        return state.tasks
            .asSequence()
            .filter { it.type == KudoState.TYPE_TASK && it.list == list }
            .minOfOrNull(KudoTask::order)
            ?.minus(1L)
            ?: 0L
    }

    private fun restoreTaskSnapshot(tasks: List<KudoTask>, snapshot: KudoTask): List<KudoTask> {
        val exists = tasks.any { it.id == snapshot.id }
        return if (exists) {
            tasks.map { task ->
                if (task.id == snapshot.id) {
                    snapshot
                } else {
                    task
                }
            }
        } else {
            tasks + snapshot
        }
    }

    private fun createWeightedSubtasks(
        drafts: List<KudoSubtaskDraft>,
        totalValue: Int,
        now: Long
    ): List<KudoSubtask> {
        val sanitizedDrafts = drafts.mapNotNull { draft ->
            val title = draft.title.trim()
            if (title.isBlank()) {
                null
            } else {
                KudoSubtaskDraft(
                    title = title,
                    difficulty = when (draft.difficulty) {
                        KudoSubtask.DIFFICULTY_SMALL,
                        KudoSubtask.DIFFICULTY_LARGE -> draft.difficulty
                        else -> KudoSubtask.DIFFICULTY_MEDIUM
                    }
                )
            }
        }
        if (sanitizedDrafts.isEmpty()) return emptyList()

        val totalWeight = sanitizedDrafts.sumOf { difficultyWeight(it.difficulty) }.coerceAtLeast(1)
        val allocations = sanitizedDrafts.mapIndexed { index, draft ->
            val weight = difficultyWeight(draft.difficulty)
            val exactShare = totalValue.toDouble() * weight.toDouble() / totalWeight.toDouble()
            val flooredShare = floor(exactShare).toInt()
            SubtaskAllocation(
                index = index,
                weight = weight,
                flooredShare = flooredShare,
                remainder = exactShare - flooredShare.toDouble()
            )
        }
        val baseShares = allocations.map(SubtaskAllocation::flooredShare).toMutableList()
        var remaining = totalValue - baseShares.sum()
        val distributionOrder = allocations
            .sortedWith(
                compareByDescending<SubtaskAllocation> { it.remainder }
                    .thenByDescending { it.weight }
                    .thenBy { it.index }
            )
            .map(SubtaskAllocation::index)
        var distributionIndex = 0
        while (remaining > 0) {
            val targetIndex = distributionOrder[distributionIndex % distributionOrder.size]
            baseShares[targetIndex] += 1
            remaining -= 1
            distributionIndex += 1
        }

        return sanitizedDrafts.mapIndexed { subtaskIndex, draft ->
            KudoSubtask(
                id = now + subtaskIndex + 1L,
                title = draft.title,
                valAmount = baseShares[subtaskIndex],
                difficulty = draft.difficulty
            )
        }
    }

    private fun difficultyWeight(difficulty: Int): Int {
        return when (difficulty) {
            KudoSubtask.DIFFICULTY_SMALL -> 1
            KudoSubtask.DIFFICULTY_LARGE -> 3
            else -> 2
        }
    }

    private data class SubtaskAllocation(
        val index: Int,
        val weight: Int,
        val flooredShare: Int,
        val remainder: Double
    )
}
