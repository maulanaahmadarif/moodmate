package com.moodyday.app.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moodyday.app.data.MoodDatabase
import com.moodyday.app.data.MoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MoodViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MoodDatabase.getDatabase(application).moodDao()

    val allMoods: Flow<List<MoodEntry>> = dao.getAllMoods()

    fun saveMood(mood: String, note: String, supportTitle: String? = null, supportMessage: String? = null) {
        viewModelScope.launch {
            dao.insertMood(
                MoodEntry(
                    mood = mood,
                    note = note,
                    supportTitle = supportTitle,
                    supportMessage = supportMessage
                )
            )
        }
    }

    fun updateMood(id: Int, mood: String, note: String, supportTitle: String? = null, supportMessage: String? = null) {
        viewModelScope.launch {
            val updatedMood = MoodEntry(
                id = id,
                mood = mood,
                note = note,
                supportTitle = supportTitle,
                supportMessage = supportMessage
            )
            dao.insertMood(updatedMood)
        }
    }

    fun deleteMood(moodEntry: MoodEntry) {
        viewModelScope.launch {
            dao.deleteMood(moodEntry)
        }
    }

    suspend fun getMoodById(id: Int): MoodEntry? {
        return dao.getMoodById(id)
    }
}