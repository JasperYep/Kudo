package com.kudo.app.core.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kudo.app.KudoApplication
import kotlinx.coroutines.launch

class KudoReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as KudoApplication
        val scheduler = KudoReminderScheduler(context)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            AlarmManagerActions.ACTION_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                val pendingResult = goAsync()
                app.applicationScope.launch {
                    try {
                        scheduler.createNotificationChannel()
                        scheduler.sync(app.kudoStateRepository.getState())
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            KudoReminderScheduler.ACTION_REMIND_TASK -> {
                val taskId = intent.getLongExtra(KudoReminderScheduler.EXTRA_TASK_ID, -1L)
                val title = intent.getStringExtra(KudoReminderScheduler.EXTRA_TASK_TITLE).orEmpty()
                val dueAt = intent.getLongExtra(KudoReminderScheduler.EXTRA_DUE_AT, -1L)
                if (taskId != -1L && title.isNotBlank() && dueAt > 0L) {
                    scheduler.createNotificationChannel()
                    scheduler.showReminder(
                        taskId = taskId,
                        title = title,
                        dueAtEpochMillis = dueAt
                    )
                }
            }
        }
    }

    private object AlarmManagerActions {
        val ACTION_EXACT_ALARM_PERMISSION_STATE_CHANGED: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
            } else {
                "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
            }
    }
}
