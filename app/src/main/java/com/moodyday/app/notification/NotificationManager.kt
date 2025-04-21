package com.moodyday.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Constraints
import androidx.work.NetworkType
import java.util.concurrent.TimeUnit
import java.util.Calendar

object NotificationManager {
    private const val REMINDER_WORK_NAME = "mood_reminder_work"
    private const val DEFAULT_HOUR = 20 // 8 PM
    private const val DEFAULT_MINUTE = 0

    fun scheduleDailyReminder(
        context: Context,
        hour: Int = DEFAULT_HOUR,
        minute: Int = DEFAULT_MINUTE
    ) {
        // Calculate initial delay until the next occurrence of the specified time
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Calculate delay until next reminder
        var initialDelay = if (currentHour > hour || (currentHour == hour && currentMinute >= minute)) {
            // If target time has passed today, schedule for tomorrow
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            tomorrow.timeInMillis - calendar.timeInMillis
        } else {
            // Schedule for today
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            today.timeInMillis - calendar.timeInMillis
        }

        // Convert to minutes (WorkManager needs whole minutes)
        initialDelay = TimeUnit.MILLISECONDS.toMinutes(initialDelay)

        // Create work constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Create a periodic work request that runs once a day
        val reminderWorkRequest = PeriodicWorkRequestBuilder<MoodReminderWorker>(
            24, TimeUnit.HOURS // Repeat interval
        ).setInitialDelay(initialDelay, TimeUnit.MINUTES)
         .setConstraints(constraints)
         .build()

        // Schedule the work request
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Update existing if any
            reminderWorkRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }
} 