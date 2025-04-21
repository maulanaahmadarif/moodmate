package com.moodyday.app.notification

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object NotificationTester {
    fun triggerTestNotification(context: Context) {
        // Create a one-time work request for immediate execution
        val workRequest = OneTimeWorkRequestBuilder<MoodReminderWorker>()
            .build()
            
        // Enqueue the work request
        WorkManager.getInstance(context)
            .enqueue(workRequest)
    }
} 