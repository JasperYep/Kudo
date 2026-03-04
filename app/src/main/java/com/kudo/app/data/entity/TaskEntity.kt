package com.kudo.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Task Entity - 支持 Tasks 和 Habits
 * @param id 唯一标识
 * @param title 任务标题
 * @param value 任务价值（金币）
 * @param type 类型：0=Task, 1=Habit
 * @param list 列表：focus/inbox
 * @param isCompleted 是否完成
 * @param createdAt 创建时间
 * @param habitChargeTime 习惯充能时间（毫秒）
 * @param habitCount 习惯完成次数
 * @param lastCompletedTime 最后完成时间
 * @param order 排序顺序
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    
    val value: Int,
    
    val type: Int = 0, // 0=Task, 1=Habit
    
    val list: String = "focus", // focus/inbox
    
    val isCompleted: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val habitChargeTime: Long = 1500, // 1.5 秒充能
    
    val habitCount: Int = 0,
    
    val lastCompletedTime: Long = 0,
    
    val order: Long = System.currentTimeMillis()
)
