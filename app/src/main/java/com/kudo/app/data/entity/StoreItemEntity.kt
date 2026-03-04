package com.kudo.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Store Item Entity - 商店奖励
 * @param id 唯一标识
 * @param name 奖励名称
 * @param cost 所需金币
 * @param description 描述
 * @param icon 图标（emoji 或资源 ID）
 * @param isPurchased 是否已购买
 * @param createdAt 创建时间
 */
@Entity(tableName = "store_items")
data class StoreItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    val cost: Int,
    
    val description: String = "",
    
    val icon: String = "🎁",
    
    val isPurchased: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis()
)
