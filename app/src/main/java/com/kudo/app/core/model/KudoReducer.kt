package com.kudo.app.core.model

import kotlin.math.floor

object KudoReducer {

    fun addTask(
        state: KudoState,
        title: String,
        coins: Int,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val nextOrder = if (state.taskSortMode == KudoTaskSortMode.Manual) {
            state.tasks.maxOfOrNull(KudoTask::order)?.plus(1L) ?: 0L
        } else {
            now
        }
        val item = KudoTask(
            id = now,
            title = title,
            coins = coins,
            order = nextOrder
        )
        return KudoStateJson.sanitize(
            state.copy(tasks = state.tasks + listOf(item))
        )
    }

    fun addHabit(
        state: KudoState,
        title: String,
        coins: Int,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        val item = KudoHabit(
            id = now,
            title = title,
            coins = coins,
            order = now
        )
        return state.copy(habits = state.habits + listOf(item))
    }

    fun addImportedTasks(
        state: KudoState,
        drafts: List<KudoTaskImportDraft>,
        now: Long = System.currentTimeMillis()
    ): KudoState {
        if (drafts.isEmpty()) return state

        val firstOrder = if (state.taskSortMode == KudoTaskSortMode.Manual) {
            state.tasks.maxOfOrNull(KudoTask::order)?.plus(1L) ?: 0L
        } else {
            now
        }
        val imported = drafts.mapIndexed { index, draft ->
            KudoTask(
                id = now + index,
                title = draft.title,
                coins = draft.value,
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

        return grown.copy(
            tasks = grown.tasks.filterNot { it.id == id },
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
        val updatedTasks = if (updatedTask.subtasks.all(KudoSubtask::isCompleted)) {
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
        val habit = state.habits.firstOrNull { it.id == id } ?: return state
        val reward = floor(habit.coins * state.multiplier).toInt()
        val rewarded = state.copy(coins = state.coins + reward)
        val grown = processGrowth(rewarded, habit.coins)
        val updatedHabits = grown.habits.map { current ->
            if (current.id == id) {
                current.copy(
                    last = now,
                    count = current.count + 1
                )
            } else {
                current
            }
        }
        val snapshot = updatedHabits.firstOrNull { it.id == id } ?: habit
        val log = KudoLogEntry(
            timestamp = now,
            text = habit.title,
            coins = reward,
            baseCoins = habit.coins,
            kind = KudoLogKind.Habit,
            taskId = habit.id,
            subject = KudoLogSubject.Habit.fromHabit(snapshot)
        )
        return grown.copy(
            habits = updatedHabits,
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
                nextState = if (log.subtaskId != null) {
                    nextState.copy(tasks = restoreTaskSnapshot(nextState.tasks, snapshot))
                } else if (exists == null) {
                    nextState.copy(tasks = nextState.tasks + snapshot)
                } else {
                    nextState
                }
            }

            is KudoLogSubject.Habit -> {
                nextState = nextState.copy(
                    habits = nextState.habits.map { habit ->
                        if (habit.id == subject.id) {
                            habit.copy(count = (habit.count - 1).coerceAtLeast(0))
                        } else {
                            habit
                        }
                    }
                )
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
                    val resolvedSubtasks = if (subtaskDrafts == null) {
                        task.subtasks
                    } else if (!isLocked) {
                        createSubtasks(
                            drafts = subtaskDrafts,
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

    fun updateHabit(
        state: KudoState,
        id: Long,
        title: String,
        coins: Int
    ): KudoState {
        return state.copy(
            habits = state.habits.map { habit ->
                if (habit.id == id) {
                    habit.copy(
                        title = title.ifBlank { habit.title },
                        coins = coins
                    )
                } else {
                    habit
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

    fun deleteHabit(state: KudoState, id: Long): KudoState {
        return state.copy(habits = state.habits.filterNot { it.id == id })
    }

    fun deleteStoreItem(state: KudoState, id: Long): KudoState {
        return state.copy(store = state.store.filterNot { it.id == id })
    }

    fun reorderHabits(state: KudoState, orderedIds: List<Long>): KudoState {
        return state.copy(habits = reorderById(state.habits, orderedIds) { it.id })
    }

    fun reorderTasks(state: KudoState, orderedIds: List<Long>): KudoState {
        val reordered = reorderById(state.tasks, orderedIds) { it.id }
        return state.copy(
            tasks = reordered.mapIndexed { index, task -> task.copy(order = index.toLong()) }
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
            sortMode = KudoTaskSortMode.Manual
        )
    }

    fun reorderStore(state: KudoState, orderedIds: List<Long>): KudoState {
        return state.copy(store = reorderById(state.store, orderedIds) { it.id })
    }

    private fun <T> reorderById(items: List<T>, orderedIds: List<Long>, idOf: (T) -> Long): List<T> {
        if (items.isEmpty()) return items
        val itemMap = items.associateBy(idOf)
        val orderedSet = orderedIds.toSet()
        val reordered = orderedIds.mapNotNull(itemMap::get)
        val leftovers = items.filterNot { idOf(it) in orderedSet }
        return reordered + leftovers
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
