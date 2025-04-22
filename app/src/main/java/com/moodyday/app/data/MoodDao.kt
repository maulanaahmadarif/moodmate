package com.moodyday.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(moodEntry: MoodEntry)

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Delete
    suspend fun deleteMood(moodEntry: MoodEntry)

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getMoodById(id: Int): MoodEntry?
}