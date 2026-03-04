package com.kudo.app.data.repository

import com.kudo.app.data.dao.StoreItemDao
import com.kudo.app.data.entity.StoreItemEntity
import kotlinx.coroutines.flow.Flow

class StoreRepository(private val storeItemDao: StoreItemDao) {
    
    val allItems: Flow<List<StoreItemEntity>> = storeItemDao.getAllItems()
    
    suspend fun getItemById(id: Long): StoreItemEntity? {
        return storeItemDao.getItemById(id)
    }
    
    suspend fun insert(item: StoreItemEntity): Long {
        return storeItemDao.insert(item)
    }
    
    suspend fun update(item: StoreItemEntity) {
        storeItemDao.update(item)
    }
    
    suspend fun delete(item: StoreItemEntity) {
        storeItemDao.delete(item)
    }
    
    suspend fun purchaseItem(id: Long) {
        storeItemDao.updatePurchased(id, true)
    }
    
    suspend fun resetPurchase(id: Long) {
        storeItemDao.updatePurchased(id, false)
    }
}
