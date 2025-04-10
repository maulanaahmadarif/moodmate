package com.moodyday.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moodyday.app.data.MoodEntry
import com.moodyday.app.ui.components.MoodButton
import com.moodyday.app.utils.MoodUtils
import kotlinx.coroutines.launch

@Composable
private fun getSupportMessage(mood: String): Pair<String, String> {
    return when (mood) {
        "😊" -> Pair(
            "Wonderful day!",
            "Your happiness brightens the world! Keep spreading joy and remember this feeling."
        )
        "😔" -> Pair(
            "It's okay to feel down",
            "Remember that every feeling is temporary. Consider talking to someone you trust or doing something you enjoy."
        )
        "😡" -> Pair(
            "Take a moment",
            "Try some deep breathing exercises or go for a walk. It's okay to step back and process your emotions."
        )
        "😴" -> Pair(
            "Rest is important",
            "Listen to your body. Maybe take a short break or ensure you get enough sleep tonight."
        )
        "😐" -> Pair(
            "Neutral days are okay",
            "Sometimes a steady day is just what we need. Use this time to focus on small goals or self-care."
        )
        "😎" -> Pair(
            "Feeling confident!",
            "Great to see you feeling strong! Use this energy to tackle challenges or help others."
        )
        else -> Pair(
            "Reflection time",
            "Every emotion teaches us something about ourselves. Take a moment to reflect on what you need right now."
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackerScreen(
    moodEntries: List<MoodEntry>,
    onSave: (String, String) -> Unit,
    onUpdate: (Int, String, String) -> Unit,
    navigateToHistory: () -> Unit
) {
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    val moods = listOf("😊", "😔", "😡", "😴", "😐", "😎")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val todayEntry = remember(moodEntries) { MoodUtils.getTodayMood(moodEntries) }

    // Initialize edit state with current values
    LaunchedEffect(todayEntry) {
        if (todayEntry != null && !isEditing) {
            selectedMood = null
            note = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { 
            if (todayEntry == null || isEditing) {
                TopAppBar(
                    title = { 
                        Text(
                            when {
                                todayEntry != null && !isEditing -> "Today's Mood"
                                isEditing -> "Edit your mood"
                                else -> "Track your mood"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        if (isEditing && todayEntry != null) {
                            IconButton(
                                onClick = {
                                    isEditing = false
                                    selectedMood = null
                                    note = ""
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (todayEntry != null && !isEditing) {
                    // Show today's mood entry
                    Text(
                        text = "Today's Mood",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = todayEntry.mood,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (todayEntry.note.isNotEmpty()) {
                        Text(
                            text = todayEntry.note,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Support message card
                    val (title, message) = getSupportMessage(todayEntry.mood)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            isEditing = true
                            selectedMood = todayEntry.mood
                            note = todayEntry.note
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Edit Mood",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = navigateToHistory,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = "View History",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    // Show mood selection UI
                    Text(
                        text = if (isEditing) "Update your mood" else "How are you feeling today?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(moods) { index, mood ->
                            MoodButton(
                                mood = mood,
                                isSelected = selectedMood == mood,
                                onClick = { selectedMood = mood }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { 
                            Text(
                                "Add a note (optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedMood?.let {
                                val wasEditing = isEditing
                                if (isEditing && todayEntry != null) {
                                    onUpdate(todayEntry.id, it, note)
                                } else {
                                    onSave(it, note)
                                }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (wasEditing) "Mood updated!" else "Mood saved!"
                                    )
                                }
                                selectedMood = null
                                note = ""
                                isEditing = false
                            }
                        },
                        enabled = selectedMood != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = if (isEditing) "Update Mood" else "Save Mood",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    if (todayEntry == null) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = navigateToHistory,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "View Mood History",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    )
}