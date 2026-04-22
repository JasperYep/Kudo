package com.kudo.app

import android.app.Application
import com.kudo.app.core.platform.KudoReminderScheduler
import com.kudo.app.core.repository.KudoStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KudoApplication : Application() {
    
    val applicationScope = CoroutineScope(SupervisorJob())

    val kudoStateRepository by lazy { KudoStateRepository(this, applicationScope) }

    override fun onCreate() {
        super.onCreate()

        val reminderScheduler = KudoReminderScheduler(this)
        reminderScheduler.createNotificationChannel()
        applicationScope.launch {
            kudoStateRepository.state.collectLatest { state ->
                reminderScheduler.sync(state)
            }
        }
    }
}
