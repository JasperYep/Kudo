package com.kudo.app.data.repository

import com.kudo.app.data.dao.TaskDao
import com.kudo.app.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    
    val allTasks: Flow<List<TaskEntity>> = taskDao.getTasksByTypeAndList(0, "focus")
    
    fun getTasksByTypeAndList(type: Int, list: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksByTypeAndList(type, list)
    }
    
    suspend fun getTaskById(id: Long): TaskEntity? {
        return taskDao.getTaskById(id)
    }
    
    suspend fun getAllTasks(): List<TaskEntity> {
        return taskDao.getAllTasks()
    }
    
    suspend fun insert(task: TaskEntity): Long {
        val order = taskDao.getNextOrder() ?: System.currentTimeMillis()
        return taskDao.insert(task.copy(order = order))
    }
    
    suspend fun update(task: TaskEntity) {
        taskDao.update(task)
    }
    
    suspend fun delete(task: TaskEntity) {
        taskDao.delete(task)
    }
    
    suspend fun deleteById(id: Long) {
        taskDao.deleteById(id)
    }
    
    suspend fun updateOrder(id: Long, order: Long) {
        taskDao.updateOrder(id, order)
    }
    
    suspend fun updateCompleted(id: Long, completed: Boolean) {
        taskDao.updateCompleted(id, completed)
    }
    
    suspend fun reorderTasks(tasks: List<TaskEntity>) {
        tasks.forEachIndexed { index, task ->
            taskDao.updateOrder(task.id, index.toLong())
        }
    }
}
