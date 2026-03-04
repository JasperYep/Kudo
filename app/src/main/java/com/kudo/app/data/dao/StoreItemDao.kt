package com.kudo.app.data.dao

import androidx.room.*
import com.kudo.app.data.entity.StoreItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreItemDao {
    
    @Query("SELECT * FROM store_items ORDER BY createdAt ASC")
    fun getAllItems(): Flow<List<StoreItemEntity>>
    
    @Query("SELECT * FROM store_items WHERE id = :id")
    suspend fun getItemById(id: Long): StoreItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StoreItemEntity): Long
    
    @Update
    suspend fun update(item: StoreItemEntity)
    
    @Delete
    suspend fun delete(item: StoreItemEntity)
    
    @Query("UPDATE store_items SET isPurchased = :purchased WHERE id = :id")
    suspend fun updatePurchased(id: Long, purchased: Boolean)
}
