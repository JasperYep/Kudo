package com.kudo.app.domain.model

data class Task(
    val id: Long = 0,
    val title: String,
    val value: Int,
    val type: TaskType = TaskType.TASK,
    val list: TaskList = TaskList.FOCUS,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val habitChargeTime: Long = 1500,
    val habitCount: Int = 0,
    val lastCompletedTime: Long = 0,
    val order: Long = System.currentTimeMillis()
)

enum class TaskType {
    TASK,
    HABIT
}

enum class TaskList {
    FOCUS,
    INBOX
}
