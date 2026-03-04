package com.kudo.app.data.dao

import androidx.room.*
import com.kudo.app.data.entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<LogEntity>>
    
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<LogEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: LogEntity): Long
    
    @Query("DELETE FROM logs WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM logs")
    suspend fun deleteAll()
    
    @Query("SELECT SUM(value) FROM logs WHERE value > 0")
    suspend fun getTotalIncome(): Int?
    
    @Query("SELECT SUM(ABS(value)) FROM logs WHERE value < 0")
    suspend fun getTotalExpense(): Int?
}
