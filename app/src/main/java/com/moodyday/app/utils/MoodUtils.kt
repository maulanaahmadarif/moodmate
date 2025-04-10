package com.moodyday.app.utils

import com.moodyday.app.data.MoodEntry
import java.util.Calendar

object MoodUtils {
    fun getTodayTimeRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    fun hasTodayMood(moods: List<MoodEntry>): Boolean {
        val (startOfDay, endOfDay) = getTodayTimeRange()
        return moods.any { entry ->
            entry.timestamp in startOfDay..endOfDay
        }
    }

    fun getTodayMood(moods: List<MoodEntry>): MoodEntry? {
        val (startOfDay, endOfDay) = getTodayTimeRange()
        return moods.find { entry ->
            entry.timestamp in startOfDay..endOfDay
        }
    }
} 