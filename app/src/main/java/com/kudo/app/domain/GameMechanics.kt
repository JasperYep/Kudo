package com.kudo.app.domain

import com.kudo.app.domain.model.UserStats

class GameMechanics {
    
    /**
     * 计算最终倍数
     * 基础倍数 × 等级加成
     */
    fun calculateFinalMultiplier(stats: UserStats): Float {
        return stats.finalMultiplier
    }
    
    /**
     * 完成任务后更新倍数
     * 超过平均值 → +0.01
     * 低于平均值 → -0.01
     */
    fun updateMultiplier(currentMultiplier: Float, recentValues: List<Int>, newValue: Int): Float {
        if (recentValues.isEmpty()) {
            return currentMultiplier
        }
        
        val avg = recentValues.average()
        return when {
            newValue >= avg -> (currentMultiplier + 0.01f).coerceAtMost(1.20f)
            else -> (currentMultiplier - 0.01f).coerceAtLeast(1.00f)
        }
    }
    
    /**
     * 计算任务奖励
     */
    fun calculateReward(taskValue: Int, multiplier: Float): Int {
        return (taskValue * multiplier).toInt()
    }
    
    /**
     * 计算等级
     */
    fun calculateLevel(life: Int): Int {
        return Math.sqrt(life / 100.0).toInt() + 1
    }
    
    /**
     * 计算经验进度 (0-1)
     */
    fun calculateXpProgress(life: Int): Float {
        val level = calculateLevel(life)
        val currentLevelStart = kotlin.math.pow((level - 1).toDouble(), 2.0) * 100
        val nextLevelStart = kotlin.math.pow(level.toDouble(), 2.0) * 100
        val progress = (life - currentLevelStart).toFloat()
        val total = (nextLevelStart - currentLevelStart).toFloat()
        return (progress / total).coerceIn(0f, 1f)
    }
    
    /**
     * 计算收支比
     */
    fun calculateIncomeExpenseRatio(income: Int, expense: Int): String {
        return when {
            expense == 0 -> if (income > 0) "∞" else "0.00"
            else -> (income.toFloat() / expense.toFloat()).toString().format(2)
        }
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}
