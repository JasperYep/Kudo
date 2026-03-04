package com.kudo.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Log Entity - 收支记录
 * @param id 唯一标识
 * @param timestamp 时间戳
 * @param description 描述
 * @param value 金额（正数=收入，负数=支出）
 * @param taskId 关联任务 ID（可选）
 * @param isHabit 是否是习惯完成
 * @param beforeCoins 操作前金币
 * @param afterCoins 操作后金币
 */
@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    val description: String,
    
    val value: Int,
    
    val taskId: Long? = null,
    
    val isHabit: Boolean = false,
    
    val beforeCoins: Int = 0,
    
    val afterCoins: Int = 0
)
