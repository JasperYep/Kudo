package com.kudo.app.domain.model

data class Log(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val value: Int,
    val taskId: Long? = null,
    val isHabit: Boolean = false,
    val beforeCoins: Int = 0,
    val afterCoins: Int = 0
)
