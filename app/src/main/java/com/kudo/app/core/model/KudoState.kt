package com.kudo.app.core.model

import androidx.compose.runtime.Immutable
import kotlin.math.sqrt

@Immutable
data class KudoState(
    val coins: Int = 0,
    val life: Int = 0,
    val maxCoins: Int = 0,
    val tasks: List<KudoTask> = emptyList(),
    val store: List<KudoStoreItem> = emptyList(),
    val logs: List<KudoLogEntry> = emptyList(),
    val recentVals: List<Int> = emptyList(),
    val multiplier: Float = 1.0f,
    val listMode: String = LIST_FOCUS
) {
    val level: Int
        get() = sqrt(life / 100.0).toInt() + 1

    val finalMultiplier: Float
        get() = multiplier * (1 + (level - 1) * 0.01f)

    companion object {
        const val LIST_FOCUS = "focus"
        const val LIST_INBOX = "inbox"

        const val TYPE_TASK = 0
        const val TYPE_HABIT = 1

        const val STORE_ONCE = 0
        const val STORE_INFINITE = 1
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
    val list: String = KudoState.LIST_FOCUS,
    val order: Long = id
) {
    val isHabit: Boolean
        get() = type == KudoState.TYPE_HABIT
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
    val type: String,
    val taskId: Long? = null,
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
    val list: String = KudoState.LIST_FOCUS,
    val order: Long = id
) {
    fun toTask(): KudoTask = KudoTask(
        id = id,
        title = title,
        valAmount = valAmount ?: 0,
        type = type,
        count = count,
        last = last,
        list = list,
        order = order
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
            list = task.list,
            order = task.order
        )

        fun fromStoreItem(item: KudoStoreItem): KudoLogItemData = KudoLogItemData(
            id = item.id,
            title = item.title,
            cost = item.cost,
            type = item.type
        )
    }
}
