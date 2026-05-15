package com.kudo.app.core.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kudo.app.MainActivity
import com.kudo.app.R
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.model.KudoTask
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class KudoReminderScheduler(
    private val context: Context
) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
    private val reminderPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Deadline reminders for Kudo tasks"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun sync(state: KudoState, now: Long = System.currentTimeMillis()) {
        previousReminderIds().forEach(::cancelReminder)

        val futureTasks = state.tasks.filter { task ->
            task.dueAtEpochMillis != null && task.dueAtEpochMillis > now
        }

        futureTasks.forEach(::scheduleReminder)
        reminderPrefs.edit()
            .putStringSet(PREF_KEY_IDS, futureTasks.map { it.id.toPreferenceValue() }.toSet())
            .apply()
    }

    fun showReminder(taskId: Long, title: String, dueAtEpochMillis: Long) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = PendingIntent.getActivity(
            appContext,
            taskId.toRequestCode(),
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "Due ${formatReminderTime(dueAtEpochMillis)}"
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(appContext).notify(taskId.toRequestCode(), notification)
        cancelStoredReminderId(taskId)
    }

    private fun scheduleReminder(task: KudoTask) {
        val dueAt = task.dueAtEpochMillis ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }
        val intent = Intent(appContext, KudoReminderReceiver::class.java).apply {
            action = ACTION_REMIND_TASK
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_DUE_AT, dueAt)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            task.id.toRequestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showIntent = PendingIntent.getActivity(
            appContext,
            task.id.toRequestCode(),
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(dueAt, showIntent),
            pendingIntent
        )
    }

    private fun cancelReminder(taskId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            taskId.toRequestCode(),
            Intent(appContext, KudoReminderReceiver::class.java).apply {
                action = ACTION_REMIND_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun cancelStoredReminderId(taskId: Long) {
        val updatedIds = previousReminderIds()
            .filterNot { it == taskId }
            .map { it.toPreferenceValue() }
            .toSet()
        reminderPrefs.edit().putStringSet(PREF_KEY_IDS, updatedIds).apply()
    }

    private fun previousReminderIds(): Set<Long> {
        return reminderPrefs.getStringSet(PREF_KEY_IDS, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()
    }

    private fun formatReminderTime(dueAtEpochMillis: Long): String {
        return Instant.ofEpochMilli(dueAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(ReminderTimeFormatter)
    }

    private fun Long.toRequestCode(): Int {
        return (this xor (this ushr 32)).toInt()
    }

    private fun Long.toPreferenceValue(): String = toString()

    companion object {
        const val ACTION_REMIND_TASK = "com.kudo.app.REMIND_TASK"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_DUE_AT = "due_at"

        private const val CHANNEL_ID = "kudo_deadlines"
        private const val PREFS_NAME = "kudo_reminders"
        private const val PREF_KEY_IDS = "scheduled_task_ids"

        private val ReminderTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE, MMM d · H:mm", Locale.getDefault())
    }
}
