package com.kudo.app.data.dao

import androidx.room.*
import com.kudo.app.data.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>
    
    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsOnce(): UserStatsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: UserStatsEntity)
    
    @Update
    suspend fun update(stats: UserStatsEntity)
    
    @Query("UPDATE user_stats SET coins = :coins, life = :life, multiplier = :multiplier, maxCoins = :maxCoins, recentValues = :recentValues, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateStats(
        coins: Int,
        life: Int,
        multiplier: Float,
        maxCoins: Int,
        recentValues: String,
        updatedAt: Long
    )
}
