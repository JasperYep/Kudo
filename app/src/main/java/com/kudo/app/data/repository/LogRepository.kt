package com.kudo.app.data.repository

import com.kudo.app.data.dao.LogDao
import com.kudo.app.data.entity.LogEntity
import kotlinx.coroutines.flow.Flow

class LogRepository(private val logDao: LogDao) {
    
    fun getRecentLogs(limit: Int = 100): Flow<List<LogEntity>> {
        return logDao.getRecentLogs(limit)
    }
    
    suspend fun getAllLogs(): List<LogEntity> {
        return logDao.getAllLogs()
    }
    
    suspend fun insert(log: LogEntity): Long {
        return logDao.insert(log)
    }
    
    suspend fun deleteById(id: Long) {
        logDao.deleteById(id)
    }
    
    suspend fun deleteAll() {
        logDao.deleteAll()
    }
    
    suspend fun getTotalIncome(): Int {
        return logDao.getTotalIncome() ?: 0
    }
    
    suspend fun getTotalExpense(): Int {
        return logDao.getTotalExpense() ?: 0
    }
    
    suspend fun getIncomeExpenseRatio(): Float {
        val income = getTotalIncome()
        val expense = getTotalExpense()
        return if (expense == 0) {
            if (income > 0) Float.MAX_VALUE else 0f
        } else {
            income.toFloat() / expense.toFloat()
        }
    }
}
