package com.kudo.app.data.repository

import com.kudo.app.data.dao.UserStatsDao
import com.kudo.app.data.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray

class UserStatsRepository(private val userStatsDao: UserStatsDao) {
    
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStats()
    
    suspend fun getUserStats(): UserStatsEntity? {
        return userStatsDao.getUserStatsOnce()
    }
    
    suspend fun initializeStats() {
        if (getUserStats() == null) {
            userStatsDao.insert(UserStatsEntity())
        }
    }
    
    suspend fun updateStats(
        coins: Int,
        life: Int,
        multiplier: Float,
        maxCoins: Int,
        recentValues: List<Int>
    ) {
        userStatsDao.updateStats(
            coins = coins,
            life = life,
            multiplier = multiplier,
            maxCoins = maxCoins,
            recentValues = JSONArray(recentValues).toString(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    suspend fun addCoins(amount: Int) {
        val current = getUserStats() ?: return
        val newCoins = current.coins + amount
        val newMax = maxOf(current.maxCoins, newCoins)
        
        userStatsDao.updateStats(
            coins = newCoins,
            life = current.life,
            multiplier = current.multiplier,
            maxCoins = newMax,
            recentValues = current.recentValues,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    suspend fun spendCoins(amount: Int): Boolean {
        val current = getUserStats() ?: return false
        if (current.coins < amount) return false
        
        userStatsDao.updateStats(
            coins = current.coins - amount,
            life = current.life,
            multiplier = current.multiplier,
            maxCoins = current.maxCoins,
            recentValues = current.recentValues,
            updatedAt = System.currentTimeMillis()
        )
        return true
    }
    
    suspend fun addLife(amount: Int) {
        val current = getUserStats() ?: return
        userStatsDao.updateStats(
            coins = current.coins,
            life = current.life + amount,
            multiplier = current.multiplier,
            maxCoins = current.maxCoins,
            recentValues = current.recentValues,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    suspend fun updateMultiplier(newMultiplier: Float) {
        val current = getUserStats() ?: return
        userStatsDao.updateStats(
            coins = current.coins,
            life = current.life,
            multiplier = newMultiplier,
            maxCoins = current.maxCoins,
            recentValues = current.recentValues,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    suspend fun addRecentValue(value: Int) {
        val current = getUserStats() ?: return
        val values = try {
            JSONArray(current.recentValues).let { json ->
                List(json.length()) { json.getInt(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
        
        val newValues = (values + value).takeLast(5)
        
        userStatsDao.updateStats(
            coins = current.coins,
            life = current.life,
            multiplier = current.multiplier,
            maxCoins = current.maxCoins,
            recentValues = JSONArray(newValues).toString(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    suspend fun getRecentValues(): List<Int> {
        return try {
            val current = getUserStats() ?: return emptyList()
            JSONArray(current.recentValues).let { json ->
                List(json.length()) { json.getInt(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
