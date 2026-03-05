package com.kudo.app

import android.app.Application
import com.kudo.app.data.KudoDatabase
import com.kudo.app.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class KudoApplication : Application() {
    
    val applicationScope = CoroutineScope(SupervisorJob())
    
    val database by lazy { KudoDatabase.getDatabase(this) }
    
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val storeRepository by lazy { StoreRepository(database.storeItemDao()) }
    val logRepository by lazy { LogRepository(database.logDao()) }
    val userStatsRepository by lazy { UserStatsRepository(database.userStatsDao()) }
}
