package com.kudo.app.domain.model

import kotlin.math.pow
import kotlin.math.sqrt

data class UserStats(
    val coins: Int = 0,
    val life: Int = 0,
    val multiplier: Float = 1.0f,
    val maxCoins: Int = 0,
    val recentValues: List<Int> = emptyList()
) {
    val level: Int
        get() = sqrt(life / 100.0).toInt() + 1
    
    val xpProgress: Float
        get() {
            val currentLevelStart = ((level - 1).toDouble().pow(2.0) * 100).toFloat()
            val nextLevelStart = (level.toDouble().pow(2.0) * 100).toFloat()
            val progress = (life - currentLevelStart.toInt()).toFloat()
            val total = (nextLevelStart.toInt() - currentLevelStart.toInt()).toFloat()
            return (progress / total).coerceIn(0f, 1f)
        }
    
    val finalMultiplier: Float
        get() {
            val levelBonus = 1 + (level - 1) * 0.01f
            return multiplier * levelBonus
        }
}
