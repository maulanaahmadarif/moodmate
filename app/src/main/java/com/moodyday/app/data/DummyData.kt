package com.moodyday.app.data

import java.util.Calendar
import java.util.concurrent.TimeUnit

object DummyData {
    fun getDummyMoods(): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val currentTimeMillis = calendar.timeInMillis
        
        return listOf(
            // April 2024
            MoodEntry(1, "😊", "Started a new project at work, feeling optimistic!", currentTimeMillis),
            MoodEntry(2, "😎", "Completed my first milestone, everything's going great!", currentTimeMillis - TimeUnit.DAYS.toMillis(2)),
            MoodEntry(3, "😐", "Regular day at work", currentTimeMillis - TimeUnit.DAYS.toMillis(4)),
            
            // March 2024
            MoodEntry(4, "😊", "Spring is here! Beautiful weather today", currentTimeMillis - TimeUnit.DAYS.toMillis(15)),
            MoodEntry(5, "😔", "Missing my family today", currentTimeMillis - TimeUnit.DAYS.toMillis(20)),
            MoodEntry(6, "😎", "Got a promotion at work!", currentTimeMillis - TimeUnit.DAYS.toMillis(25)),
            MoodEntry(7, "😡", "Traffic was terrible today", currentTimeMillis - TimeUnit.DAYS.toMillis(30)),
            
            // February 2024
            MoodEntry(8, "😊", "Valentine's day dinner was amazing", currentTimeMillis - TimeUnit.DAYS.toMillis(45)),
            MoodEntry(9, "😴", "Working overtime this week", currentTimeMillis - TimeUnit.DAYS.toMillis(50)),
            MoodEntry(10, "😐", "Winter is dragging on", currentTimeMillis - TimeUnit.DAYS.toMillis(55)),
            
            // January 2024
            MoodEntry(11, "😊", "New Year, new beginnings!", currentTimeMillis - TimeUnit.DAYS.toMillis(75)),
            MoodEntry(12, "😎", "Started my fitness journey", currentTimeMillis - TimeUnit.DAYS.toMillis(80)),
            MoodEntry(13, "😔", "Post-holiday blues", currentTimeMillis - TimeUnit.DAYS.toMillis(85)),
            
            // December 2023
            MoodEntry(14, "😊", "Christmas with family!", currentTimeMillis - TimeUnit.DAYS.toMillis(100)),
            MoodEntry(15, "😎", "End of year bonus received", currentTimeMillis - TimeUnit.DAYS.toMillis(105)),
            
            // November 2023
            MoodEntry(16, "😊", "Thanksgiving dinner was perfect", currentTimeMillis - TimeUnit.DAYS.toMillis(130)),
            MoodEntry(17, "😴", "Black Friday shopping exhaustion", currentTimeMillis - TimeUnit.DAYS.toMillis(135)),
            
            // October 2023
            MoodEntry(18, "😊", "Halloween party was fun!", currentTimeMillis - TimeUnit.DAYS.toMillis(160)),
            MoodEntry(19, "😎", "Fall colors are beautiful", currentTimeMillis - TimeUnit.DAYS.toMillis(165)),
            
            // September 2023
            MoodEntry(20, "😊", "Labor Day weekend at the beach", currentTimeMillis - TimeUnit.DAYS.toMillis(190))
        )
    }
} 