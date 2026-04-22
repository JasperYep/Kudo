package com.kudo.app.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class KudoState(
    val coins: Int = 0,
    val tasks: List<KudoTask> = emptyList(),
    val store: List<KudoStoreItem> = emptyList(),
    val logs: List<KudoLogEntry> = emptyList(),
    val recentVals: List<Int> = emptyList(),
    val multiplier: Float = 1.0f,
    val taskSortMode: Int = TASK_SORT_AUTO_DUE
) {
    companion object {
        const val TYPE_TASK = 0
        const val TYPE_HABIT = 1

        const val STORE_ONCE = 0
        const val STORE_INFINITE = 1

        const val TASK_SORT_AUTO_DUE = 0
        const val TASK_SORT_MANUAL = 1
    }
}

@Immutable
data class KudoTask(
    val id: Long,
    val title: String,
    val valAmount: Int,
    val type: Int,
    val count: Int = 0,
    val last: Long = 0L,
    val order: Long = id,
    val dueAtEpochMillis: Long? = null,
    val subtasks: List<KudoSubtask> = emptyList()
) {
    val isHabit: Boolean
        get() = type == KudoState.TYPE_HABIT

    val hasSubtasks: Boolean
        get() = subtasks.isNotEmpty()

    val completedSubtaskCount: Int
        get() = subtasks.count(KudoSubtask::isCompleted)

    val remainingValue: Int
        get() = if (subtasks.isEmpty()) {
            valAmount
        } else {
            subtasks.filterNot(KudoSubtask::isCompleted).sumOf(KudoSubtask::valAmount)
        }

    val isSubtaskStructureLocked: Boolean
        get() = subtasks.any(KudoSubtask::isCompleted)
}

@Immutable
data class KudoStoreItem(
    val id: Long,
    val title: String,
    val cost: Int,
    val type: Int = KudoState.STORE_ONCE
)

@Immutable
data class KudoLogEntry(
    val timestamp: Long,
    val text: String,
    val value: Int,
    val baseValue: Int? = null,
    val type: String,
    val taskId: Long? = null,
    val subtaskId: Long? = null,
    val isHabit: Boolean = false,
    val itemData: KudoLogItemData? = null
)

@Immutable
data class KudoLogItemData(
    val id: Long,
    val title: String,
    val valAmount: Int? = null,
    val cost: Int? = null,
    val type: Int = 0,
    val count: Int = 0,
    val last: Long = 0L,
    val order: Long = id,
    val dueAtEpochMillis: Long? = null,
    val subtasks: List<KudoSubtask> = emptyList()
) {
    fun toTask(): KudoTask = KudoTask(
        id = id,
        title = title,
        valAmount = valAmount ?: 0,
        type = type,
        count = count,
        last = last,
        order = order,
        dueAtEpochMillis = dueAtEpochMillis,
        subtasks = subtasks
    )

    fun toStoreItem(): KudoStoreItem = KudoStoreItem(
        id = id,
        title = title,
        cost = cost ?: 0,
        type = type
    )

    companion object {
        fun fromTask(task: KudoTask): KudoLogItemData = KudoLogItemData(
            id = task.id,
            title = task.title,
            valAmount = task.valAmount,
            type = task.type,
            count = task.count,
            last = task.last,
            order = task.order,
            dueAtEpochMillis = task.dueAtEpochMillis,
            subtasks = task.subtasks
        )

        fun fromStoreItem(item: KudoStoreItem): KudoLogItemData = KudoLogItemData(
            id = item.id,
            title = item.title,
            cost = item.cost,
            type = item.type
        )
    }
}

@Immutable
data class KudoSubtask(
    val id: Long,
    val title: String,
    val valAmount: Int = 0,
    val completedAt: Long? = null
) {
    val isCompleted: Boolean
        get() = completedAt != null
}

@Immutable
data class KudoSubtaskDraft(
    val title: String
)
