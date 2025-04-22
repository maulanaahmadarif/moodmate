package com.moodyday.app.data

import java.util.Calendar
import java.util.concurrent.TimeUnit

object DummyData {
    fun getDummyMoods(): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val currentTimeMillis = calendar.timeInMillis
        
        return listOf(
            // April 2024
            MoodEntry(
                id = 1,
                mood = "😊",
                note = "Started a new project at work, feeling optimistic!",
                supportTitle = "Embracing New Beginnings",
                supportMessage = "Your enthusiasm for this new project is wonderful! This positive energy will help you tackle any challenges that come your way.",
                timestamp = currentTimeMillis
            ),
            MoodEntry(
                id = 2,
                mood = "😎",
                note = "Completed my first milestone, everything's going great!",
                supportTitle = "Celebrating Progress",
                supportMessage = "What a fantastic achievement! Each milestone you reach is a testament to your dedication and hard work.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(2)
            ),
            MoodEntry(
                id = 3,
                mood = "😐",
                note = "Regular day at work",
                supportTitle = "Finding Balance",
                supportMessage = "Even ordinary days have their purpose. Take this moment to appreciate the stability and routine in your life.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(4)
            ),
            
            // March 2024
            MoodEntry(
                id = 4,
                mood = "😊",
                note = "Spring is here! Beautiful weather today",
                supportTitle = "Seasonal Joy",
                supportMessage = "Nature has a wonderful way of lifting our spirits. Embrace this fresh start and the positive energy it brings.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(15)
            ),
            MoodEntry(
                id = 5,
                mood = "😔",
                note = "Missing my family today",
                supportTitle = "Honoring Connections",
                supportMessage = "It's natural to miss those we love. Consider reaching out to them today - even a small connection can warm the heart.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(20)
            ),
            MoodEntry(
                id = 6,
                mood = "😎",
                note = "Got a promotion at work!",
                supportTitle = "Well-Deserved Success",
                supportMessage = "Congratulations on this achievement! Your hard work and dedication have paid off in a meaningful way.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(25)
            ),
            MoodEntry(
                id = 7,
                mood = "😡",
                note = "Traffic was terrible today",
                supportTitle = "Finding Peace",
                supportMessage = "Frustrating situations test our patience. Remember that this moment is temporary, and tomorrow brings a fresh start.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(30)
            ),
            
            // February 2024
            MoodEntry(
                id = 8,
                mood = "😊",
                note = "Valentine's day dinner was amazing",
                supportTitle = "Cherishing Moments",
                supportMessage = "Special moments like these add color to our lives. Hold onto this feeling of joy and connection.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(45)
            ),
            MoodEntry(
                id = 9,
                mood = "😴",
                note = "Working overtime this week",
                supportTitle = "Rest and Recovery",
                supportMessage = "Remember to take care of yourself during busy times. Your well-being matters just as much as your work.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(50)
            ),
            MoodEntry(
                id = 10,
                mood = "😐",
                note = "Winter is dragging on",
                supportTitle = "Embracing Change",
                supportMessage = "Every season has its purpose. Use this time to focus on indoor activities that bring you joy and comfort.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(55)
            ),
            
            // January 2024
            MoodEntry(
                id = 11,
                mood = "😊",
                note = "New Year, new beginnings!",
                supportTitle = "Fresh Start",
                supportMessage = "Your optimism for the new year is inspiring! Carry this energy forward as you pursue your goals.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(75)
            ),
            MoodEntry(
                id = 12,
                mood = "😎",
                note = "Started my fitness journey",
                supportTitle = "Healthy Choices",
                supportMessage = "Taking steps toward better health is a gift to yourself. Be proud of your commitment to self-improvement.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(80)
            ),
            MoodEntry(
                id = 13,
                mood = "😔",
                note = "Post-holiday blues",
                supportTitle = "Gentle Transition",
                supportMessage = "It's normal to feel this way after the holidays. Take time to create small moments of joy in your daily routine.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(85)
            ),
            
            // December 2023
            MoodEntry(
                id = 14,
                mood = "😊",
                note = "Christmas with family!",
                supportTitle = "Family Bonds",
                supportMessage = "These precious moments with family create lasting memories. Cherish the warmth and love you shared today.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(100)
            ),
            MoodEntry(
                id = 15,
                mood = "😎",
                note = "End of year bonus received",
                supportTitle = "Well-Earned Rewards",
                supportMessage = "Your hard work throughout the year has been recognized. Take a moment to appreciate your achievements.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(105)
            ),
            
            // November 2023
            MoodEntry(
                id = 16,
                mood = "😊",
                note = "Thanksgiving dinner was perfect",
                supportTitle = "Gratitude and Joy",
                supportMessage = "Moments of gratitude fill our hearts with contentment. What a beautiful way to celebrate with loved ones.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(130)
            ),
            MoodEntry(
                id = 17,
                mood = "😴",
                note = "Black Friday shopping exhaustion",
                supportTitle = "Rest and Recharge",
                supportMessage = "Listen to your body when it asks for rest. Take time to recover and rejuvenate.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(135)
            ),
            
            // October 2023
            MoodEntry(
                id = 18,
                mood = "😊",
                note = "Halloween party was fun!",
                supportTitle = "Playful Spirit",
                supportMessage = "It's wonderful to let loose and have fun! These moments of joy and laughter are precious.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(160)
            ),
            MoodEntry(
                id = 19,
                mood = "😎",
                note = "Fall colors are beautiful",
                supportTitle = "Natural Beauty",
                supportMessage = "Taking time to appreciate nature's beauty can be so uplifting. Let this scenery inspire your day.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(165)
            ),
            
            // September 2023
            MoodEntry(
                id = 20,
                mood = "😊",
                note = "Labor Day weekend at the beach",
                supportTitle = "Weekend Escape",
                supportMessage = "Time spent relaxing and recharging is never wasted. Carry this peaceful beach energy with you.",
                timestamp = currentTimeMillis - TimeUnit.DAYS.toMillis(190)
            )
        )
    }
} 