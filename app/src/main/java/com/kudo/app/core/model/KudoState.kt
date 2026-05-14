package com.kudo.app.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class KudoState(
    val coins: Int = 0,
    val tasks: List<KudoTask> = emptyList(),
    val store: List<KudoStoreItem> = emptyList(),
    val logs: List<KudoLogEntry> = emptyList(),
    val recentCoins: List<Int> = emptyList(),
    val multiplier: Float = 1.0f,
    val taskSortMode: KudoTaskSortMode = KudoTaskSortMode.AutoDue,
    val notes: List<KudoNote> = emptyList()
)

enum class KudoTaskKind { Task, Habit }

enum class KudoStoreKind { Once, Repeatable }

enum class KudoTaskSortMode { AutoDue, Manual }

enum class KudoLogKind { Task, Store }

@Immutable
data class KudoTask(
    val id: Long,
    val title: String,
    val coins: Int,
    val kind: KudoTaskKind,
    val count: Int = 0,
    val last: Long = 0L,
    val order: Long = id,
    val dueAtEpochMillis: Long? = null,
    val subtasks: List<KudoSubtask> = emptyList()
) {
    val isHabit: Boolean
        get() = kind == KudoTaskKind.Habit

    val hasSubtasks: Boolean
        get() = subtasks.isNotEmpty()

    val completedSubtaskCount: Int
        get() = subtasks.count(KudoSubtask::isCompleted)

    val remainingCoins: Int
        get() = if (subtasks.isEmpty()) {
            coins
        } else {
            subtasks.filterNot(KudoSubtask::isCompleted).sumOf(KudoSubtask::coins)
        }

    val isSubtaskStructureLocked: Boolean
        get() = subtasks.any(KudoSubtask::isCompleted)
}

@Immutable
data class KudoStoreItem(
    val id: Long,
    val title: String,
    val cost: Int,
    val kind: KudoStoreKind = KudoStoreKind.Once
)

@Immutable
data class KudoLogEntry(
    val timestamp: Long,
    val text: String,
    val coins: Int,
    val baseCoins: Int? = null,
    val kind: KudoLogKind,
    val taskId: Long? = null,
    val subtaskId: Long? = null,
    val isHabit: Boolean = false,
    val subject: KudoLogSubject? = null
)

@Immutable
sealed interface KudoLogSubject {
    val id: Long
    val title: String

    @Immutable
    data class Task(
        override val id: Long,
        override val title: String,
        val coins: Int,
        val kind: KudoTaskKind,
        val count: Int = 0,
        val last: Long = 0L,
        val order: Long = id,
        val dueAtEpochMillis: Long? = null,
        val subtasks: List<KudoSubtask> = emptyList()
    ) : KudoLogSubject {
        fun toTask(): KudoTask = KudoTask(
            id = id,
            title = title,
            coins = coins,
            kind = kind,
            count = count,
            last = last,
            order = order,
            dueAtEpochMillis = dueAtEpochMillis,
            subtasks = subtasks
        )

        companion object {
            fun fromTask(task: KudoTask): Task = Task(
                id = task.id,
                title = task.title,
                coins = task.coins,
                kind = task.kind,
                count = task.count,
                last = task.last,
                order = task.order,
                dueAtEpochMillis = task.dueAtEpochMillis,
                subtasks = task.subtasks
            )
        }
    }

    @Immutable
    data class Store(
        override val id: Long,
        override val title: String,
        val cost: Int,
        val kind: KudoStoreKind
    ) : KudoLogSubject {
        fun toStoreItem(): KudoStoreItem = KudoStoreItem(
            id = id,
            title = title,
            cost = cost,
            kind = kind
        )

        companion object {
            fun fromStoreItem(item: KudoStoreItem): Store = Store(
                id = item.id,
                title = item.title,
                cost = item.cost,
                kind = item.kind
            )
        }
    }
}

@Immutable
data class KudoSubtask(
    val id: Long,
    val title: String,
    val coins: Int = 0,
    val completedAt: Long? = null
) {
    val isCompleted: Boolean
        get() = completedAt != null
}

@Immutable
data class KudoSubtaskDraft(
    val title: String
)

@Immutable
data class KudoNote(
    val id: Long,
    val title: String = "",
    val content: String = "",
    val updatedAt: Long
) {
    val displayTitle: String
        get() = title.trim()
            .ifBlank {
                content.lineSequence()
                    .firstOrNull { it.isNotBlank() }
                    ?.trim()
                    .orEmpty()
            }
            .ifBlank { "New Note" }
}
