package com.kudo.app.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KudoState(
    @SerialName("schemaVersion") val schemaVersion: Int = SCHEMA_VERSION,
    @SerialName("coins") val coins: Int = 0,
    @SerialName("tasks") val tasks: List<KudoTask> = emptyList(),
    @SerialName("store") val store: List<KudoStoreItem> = emptyList(),
    @SerialName("logs") val logs: List<KudoLogEntry> = emptyList(),
    @SerialName("recentVals") val recentVals: List<Int> = emptyList(),
    @SerialName("multiplier") val multiplier: Float = 1.0f,
    @SerialName("taskSortMode") val taskSortMode: Int = TASK_SORT_AUTO_DUE
) {
    companion object {
        const val SCHEMA_VERSION = 2

        const val STORE_ONCE = 0
        const val STORE_INFINITE = 1

        const val TASK_SORT_AUTO_DUE = 0
        const val TASK_SORT_MANUAL = 1
    }
}

@Serializable
data class KudoTask(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("val") val valAmount: Int,
    @SerialName("order") val order: Long = id,
    @SerialName("dueAt") val dueAtEpochMillis: Long? = null,
    @SerialName("isTimerRunning") val isTimerRunning: Boolean = false,
    @SerialName("accumulatedTimeMillis") val accumulatedTimeMillis: Long = 0L,
    @SerialName("lastTimerStart") val lastTimerStart: Long = 0L
)

@Serializable
data class KudoStoreItem(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("cost") val cost: Int,
    @SerialName("type") val type: Int = KudoState.STORE_ONCE
)

@Serializable
data class KudoLogEntry(
    @SerialName("t") val timestamp: Long,
    @SerialName("txt") val text: String,
    @SerialName("v") val value: Int,
    @SerialName("base") val baseValue: Int? = null,
    @SerialName("type") val type: String,
    @SerialName("taskId") val taskId: Long? = null,
    @SerialName("undoable") val undoable: Boolean = true,
    @SerialName("itemData") val itemData: KudoLogItemData? = null
)

@Serializable
data class KudoLogItemData(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("val") val valAmount: Int? = null,
    @SerialName("cost") val cost: Int? = null,
    @SerialName("storeType") val storeType: Int? = null,
    @SerialName("order") val order: Long = id,
    @SerialName("dueAt") val dueAtEpochMillis: Long? = null,
    @SerialName("isTimerRunning") val isTimerRunning: Boolean = false,
    @SerialName("accumulatedTimeMillis") val accumulatedTimeMillis: Long = 0L,
    @SerialName("lastTimerStart") val lastTimerStart: Long = 0L
) {
    fun toTask(): KudoTask = KudoTask(
        id = id,
        title = title,
        valAmount = valAmount ?: 0,
        order = order,
        dueAtEpochMillis = dueAtEpochMillis,
        isTimerRunning = isTimerRunning,
        accumulatedTimeMillis = accumulatedTimeMillis,
        lastTimerStart = lastTimerStart
    )

    fun toStoreItem(): KudoStoreItem = KudoStoreItem(
        id = id,
        title = title,
        cost = cost ?: 0,
        type = storeType ?: KudoState.STORE_ONCE
    )

    companion object {
        fun fromTask(task: KudoTask): KudoLogItemData = KudoLogItemData(
            id = task.id,
            title = task.title,
            valAmount = task.valAmount,
            order = task.order,
            dueAtEpochMillis = task.dueAtEpochMillis,
            isTimerRunning = task.isTimerRunning,
            accumulatedTimeMillis = task.accumulatedTimeMillis,
            lastTimerStart = task.lastTimerStart
        )

        fun fromStoreItem(item: KudoStoreItem): KudoLogItemData = KudoLogItemData(
            id = item.id,
            title = item.title,
            cost = item.cost,
            storeType = item.type
        )
    }
}
