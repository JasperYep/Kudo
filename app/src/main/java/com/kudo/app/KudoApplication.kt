package com.kudo.app

import android.app.Application
import com.kudo.app.core.repository.KudoStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class KudoApplication : Application() {
    
    val applicationScope = CoroutineScope(SupervisorJob())

    val kudoStateRepository by lazy { KudoStateRepository(this, applicationScope) }
}
