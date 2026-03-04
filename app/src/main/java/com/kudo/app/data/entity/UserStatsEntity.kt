package com.kudo.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User Stats Entity - 用户统计
 * @param id 固定为 1（单例）
 * @param coins 当前金币
 * @param life 经验值
 * @param multiplier 倍数加成
 * @param maxCoins 历史最高金币
 * @param recentValues 最近任务值（用于计算平均）
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    
    val coins: Int = 0,
    
    val life: Int = 0,
    
    val multiplier: Float = 1.0f,
    
    val maxCoins: Int = 0,
    
    val recentValues: String = "", // JSON 数组存储最近 5 个任务值
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val updatedAt: Long = System.currentTimeMillis()
)
