package com.kudo.app.core.model

import kotlin.math.floor

object KudoReducer {

    fun addTask(
        state: KudoState,
        title: String,
        coins: Int,
        kind: KudoTaskKind,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val nextOrder = if (
            kind == KudoTaskKind.Task &&
            state.taskSortMode == KudoTaskSortMode.Manual
        ) {
            state.tasks
                .asSequence()
                .filter { it.kind == KudoTaskKind.Task }
                .maxOfOrNull(KudoTask::order)
                ?.plus(1L)
                ?: 0L
        } else {
            now
        }
        val item = KudoTask(
            id = now,
            title = title,
            coins = coins,
            kind = kind,
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

        val firstOrder = if (state.taskSortMode == KudoTaskSortMode.Manual) {
            state.tasks
                .asSequence()
                .filter { it.kind == KudoTaskKind.Task }
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
                coins = draft.value,
                kind = KudoTaskKind.Task,
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
        kind: KudoStoreKind,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val item = KudoStoreItem(
            id = now,
            title = title,
            cost = cost,
            kind = kind
        )
        return state.copy(store = listOf(item) + state.store)
    }

    fun completeTask(state: KudoState, id: Long, now: Long = System.currentTimeMillis()): KudoState {
        val task = state.tasks.firstOrNull { it.id == id } ?: return state
        val baseCoins = task.remainingCoins
        val reward = floor(baseCoins * state.multiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, baseCoins)
        val log = KudoLogEntry(
            timestamp = now,
            text = task.title,
            coins = reward,
            baseCoins = baseCoins,
            kind = KudoLogKind.Task,
            taskId = task.id,
            subject = KudoLogSubject.Task.fromTask(task)
        )
        val remainingTasks = if (task.kind == KudoTaskKind.Task) {
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
        val reward = floor(subtask.coins * state.multiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, subtask.coins)
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
            coins = reward,
            baseCoins = subtask.coins,
            kind = KudoLogKind.Task,
            taskId = task.id,
            subtaskId = subtask.id,
            subject = KudoLogSubject.Task.fromTask(task)
        )
        val updatedTasks = if (updatedTask.remainingCoins == 0) {
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
        val reward = floor(habit.coins * state.multiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, habit.coins)
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
            coins = reward,
            baseCoins = habit.coins,
            kind = KudoLogKind.Task,
            taskId = habit.id,
            isHabit = true,
            subject = KudoLogSubject.Task.fromTask(snapshot)
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
            store = if (item.kind == KudoStoreKind.Once) {
                state.store.filterNot { it.id == id }
            } else {
                state.store
            },
            logs = listOf(
                KudoLogEntry(
                    timestamp = now,
                    text = item.title,
                    coins = -item.cost,
                    kind = KudoLogKind.Store,
                    subject = KudoLogSubject.Store.fromStoreItem(item)
                )
            ) + state.logs
        )
    }

    fun undoLog(state: KudoState, index: Int): KudoState {
        val log = state.logs.getOrNull(index) ?: return state
        var nextState = state.copy(coins = state.coins - log.coins)

        when (val subject = log.subject) {
            is KudoLogSubject.Task -> {
                val exists = nextState.tasks.firstOrNull { it.id == subject.id }
                val snapshot = subject.toTask()
                nextState = if (log.subtaskId != null && subject.kind == KudoTaskKind.Task) {
                    nextState.copy(tasks = restoreTaskSnapshot(nextState.tasks, snapshot))
                } else if (exists == null && subject.kind == KudoTaskKind.Task) {
                    nextState.copy(tasks = nextState.tasks + snapshot)
                } else if (exists != null && log.isHabit) {
                    nextState.copy(
                        tasks = nextState.tasks.map { task ->
                            if (task.id == subject.id) {
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

            is KudoLogSubject.Store -> {
                val exists = nextState.store.firstOrNull { it.id == subject.id }
                if (exists == null && subject.kind == KudoStoreKind.Once) {
                    nextState = nextState.copy(store = nextState.store + subject.toStoreItem())
                }
            }

            null -> Unit
        }

        return nextState.copy(logs = nextState.logs.filterIndexed { logIndex, _ -> logIndex != index })
    }

    fun updateTask(
        state: KudoState,
        id: Long,
        title: String,
        coins: Int,
        dueAtEpochMillis: Long?,
        subtaskDrafts: List<KudoSubtaskDraft>? = null,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        return state.copy(
            tasks = state.tasks.map { task ->
                if (task.id == id) {
                    val isLocked = task.isSubtaskStructureLocked
                    val resolvedSubtasks = if (task.kind == KudoTaskKind.Task && subtaskDrafts == null) {
                        task.subtasks
                    } else if (task.kind == KudoTaskKind.Task && !isLocked) {
                        createSubtasks(
                            drafts = subtaskDrafts.orEmpty(),
                            totalCoins = coins,
                            now = now
                        )
                    } else {
                        task.subtasks
                    }
                    task.copy(
                        title = title.ifBlank { task.title },
                        coins = if (isLocked) task.coins else coins,
                        dueAtEpochMillis = dueAtEpochMillis,
                        subtasks = resolvedSubtasks
                    )
                } else {
                    task
                }
            }
        )
    }

    fun setTaskSortMode(state: KudoState, sortMode: KudoTaskSortMode): KudoState {
        return state.copy(taskSortMode = sortMode)
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
            task.kind == KudoTaskKind.Habit
        }
    }

    fun reorderTasks(state: KudoState, orderedIds: List<Long>): KudoState {
        return reorderTaskSubset(
            state = state,
            orderedIds = orderedIds
        ) { task ->
            task.kind == KudoTaskKind.Task
        }
    }

    fun resetTaskOrder(state: KudoState): KudoState {
        val orderedIds = state.tasks
            .asSequence()
            .filter { it.kind == KudoTaskKind.Task }
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
            sortMode = KudoTaskSortMode.Manual
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

    private fun processGrowth(state: KudoState, coins: Int): KudoState {
        val average = if (state.recentCoins.isNotEmpty()) {
            state.recentCoins.average()
        } else {
            coins.toDouble()
        }
        val updatedMultiplier = if (coins >= average) {
            (state.multiplier + 0.01f).coerceAtMost(1.20f)
        } else {
            (state.multiplier - 0.01f).coerceAtLeast(1.00f)
        }
        val updatedRecentCoins = (state.recentCoins + coins).takeLast(5)

        return state.copy(
            multiplier = updatedMultiplier,
            recentCoins = updatedRecentCoins
        )
    }

    private fun restoreTaskSnapshot(tasks: List<KudoTask>, snapshot: KudoTask): List<KudoTask> {
        val exists = tasks.any { it.id == snapshot.id }
        return if (exists) {
            tasks.map { task ->
                if (task.id == snapshot.id) snapshot else task
            }
        } else {
            tasks + snapshot
        }
    }

    fun addNote(
        state: KudoState,
        now: Long = System.currentTimeMillis()
    ): Pair<KudoState, Long> {
        val note = KudoNote(
            id = now,
            updatedAt = now
        )
        return state.copy(notes = listOf(note) + state.notes) to note.id
    }

    fun updateNoteTitle(
        state: KudoState,
        id: Long,
        title: String,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val note = state.notes.firstOrNull { it.id == id } ?: return state
        if (note.title == title) return state
        val updated = note.copy(title = title, updatedAt = now)
        return state.copy(notes = listOf(updated) + state.notes.filterNot { it.id == id })
    }

    fun updateNoteContent(
        state: KudoState,
        id: Long,
        content: String,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val note = state.notes.firstOrNull { it.id == id } ?: return state
        if (note.content == content) return state
        val updated = note.copy(content = content, updatedAt = now)
        return state.copy(notes = listOf(updated) + state.notes.filterNot { it.id == id })
    }

    fun deleteNote(state: KudoState, id: Long): KudoState {
        if (state.notes.none { it.id == id }) return state
        return state.copy(notes = state.notes.filterNot { it.id == id })
    }

    private fun createSubtasks(
        drafts: List<KudoSubtaskDraft>,
        totalCoins: Int,
        now: Long
    ): List<KudoSubtask> {
        val titles = drafts.mapNotNull { draft ->
            draft.title.trim().ifBlank { null }
        }
        if (titles.isEmpty()) return emptyList()

        val count = titles.size
        val base = totalCoins / count
        val remainder = totalCoins % count

        return titles.mapIndexed { index, title ->
            KudoSubtask(
                id = now + index + 1L,
                title = title,
                coins = base + if (index < remainder) 1 else 0
            )
        }
    }
}
