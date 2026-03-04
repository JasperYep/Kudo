package com.kudo.app.domain.model

data class StoreItem(
    val id: Long = 0,
    val name: String,
    val cost: Int,
    val description: String = "",
    val icon: String = "🎁",
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
