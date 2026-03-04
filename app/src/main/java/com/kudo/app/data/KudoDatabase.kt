package com.kudo.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kudo.app.data.dao.*
import com.kudo.app.data.entity.*

@Database(
    entities = [
        TaskEntity::class,
        StoreItemEntity::class,
        LogEntity::class,
        UserStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KudoDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun storeItemDao(): StoreItemDao
    abstract fun logDao(): LogDao
    abstract fun userStatsDao(): UserStatsDao
    
    companion object {
        @Volatile
        private var INSTANCE: KudoDatabase? = null
        
        fun getDatabase(context: Context): KudoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KudoDatabase::class.java,
                    "kudo_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
