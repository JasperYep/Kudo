package com.kudo.app.domain.model

data class UserStats(
    val coins: Int = 0,
    val life: Int = 0,
    val multiplier: Float = 1.0f,
    val maxCoins: Int = 0,
    val recentValues: List<Int> = emptyList()
) {
    val level: Int
        get() = Math.sqrt(life / 100.0).toInt() + 1
    
    val xpProgress: Float
        get() {
            val currentLevelStart = Math.pow((level - 1).toDouble(), 2.0) * 100
            val nextLevelStart = Math.pow(level.toDouble(), 2.0) * 100
            val progress = life - currentLevelStart
            val total = nextLevelStart - currentLevelStart
            return (progress / total).coerceIn(0f, 1f)
        }
    
    val finalMultiplier: Float
        get() {
            val levelBonus = 1 + (level - 1) * 0.01f
            return multiplier * levelBonus
        }
}
