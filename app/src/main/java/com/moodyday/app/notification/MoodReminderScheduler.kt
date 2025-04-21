package com.moodyday.app.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MoodReminderScheduler {
    private const val WORK_NAME = "mood_reminder_work"
    private const val REMINDER_HOUR = 20
    private const val REMINDER_MINUTE = 0

    fun scheduleDailyReminder(context: Context) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Set reminder time
        calendar.set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
        calendar.set(Calendar.MINUTE, REMINDER_MINUTE)
        calendar.set(Calendar.SECOND, 0)

        // If current time is past the scheduled time, schedule for next day
        if (currentHour > REMINDER_HOUR || 
            (currentHour == REMINDER_HOUR && currentMinute >= REMINDER_MINUTE)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Calculate delay in minutes
        val nowMillis = System.currentTimeMillis()
        val initialDelayMillis = calendar.timeInMillis - nowMillis
        val initialDelayMinutes = TimeUnit.MILLISECONDS.toMinutes(initialDelayMillis)

        // Set up work constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Create periodic work request that repeats daily
        val reminderWork = PeriodicWorkRequestBuilder<MoodReminderWorker>(
            24, TimeUnit.HOURS // Repeat every 24 hours
        )
            .setConstraints(constraints)
            .setInitialDelay(
                initialDelayMinutes,
                TimeUnit.MINUTES
            )
            .build()

        // Enqueue the work request
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Update existing if any
            reminderWork
        )
    }

    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
} 