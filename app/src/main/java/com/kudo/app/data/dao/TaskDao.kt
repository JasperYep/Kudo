package com.kudo.app.data.dao

import androidx.room.*
import com.kudo.app.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks WHERE type = :type AND list = :list ORDER BY `order` ASC")
    fun getTasksByTypeAndList(type: Int, list: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?
    
    @Query("SELECT * FROM tasks ORDER BY `order` ASC")
    suspend fun getAllTasks(): List<TaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long
    
    @Update
    suspend fun update(task: TaskEntity)
    
    @Delete
    suspend fun delete(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE tasks SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Long)
    
    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompleted(id: Long, completed: Boolean)
    
    @Query("SELECT MAX(`order`) + 1 FROM tasks")
    suspend fun getNextOrder(): Long?
}
