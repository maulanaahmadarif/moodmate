package com.moodyday.app.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.moodyday.app.data.Message
import com.moodyday.app.data.MoodEntry
import com.moodyday.app.data.OpenAIRequest
import com.moodyday.app.network.OpenAIService
import com.moodyday.app.ui.components.MoodButton
import com.moodyday.app.utils.MoodUtils
import kotlinx.coroutines.launch
import com.moodyday.app.BuildConfig
import com.moodyday.app.notification.NotificationTester
import androidx.compose.ui.platform.LocalContext
import com.moodyday.app.viewmodel.MoodViewModel

private suspend fun getSupportMessage(mood: String, moodViewModel: MoodViewModel, existingId: Int? = null, note: String = ""): Pair<String, String> {
    // Try to get cached message if we're updating
    if (existingId != null) {
        val cachedEntry = moodViewModel.getMoodById(existingId)
        if (cachedEntry?.supportTitle != null && cachedEntry.supportMessage != null) {
            return Pair(cachedEntry.supportTitle, cachedEntry.supportMessage)
        }
    }

    val contextPrompt = if (note.isNotEmpty()) {
        "They mentioned: \"$note\""
    } else {
        ""
    }

    val prompt = """
        As an empathetic AI assistant, provide a supportive response for someone who is feeling $mood.
        ${if (contextPrompt.isNotEmpty()) "$contextPrompt\n" else ""}
        Return the response in this exact format:
        Title: [A short, encouraging title that relates to their mood${if (note.isNotEmpty()) " and situation" else ""}]
        Message: [A supportive, empathetic message of 2-3 sentences that acknowledges their feelings${if (note.isNotEmpty()) " and specifically addresses their situation" else ""}]
        
        Keep the message personal, warm, and actionable if appropriate.
    """.trimIndent()

    try {
        val request = OpenAIRequest(
            messages = listOf(
                Message(
                    role = "system",
                    content = "You are an empathetic AI assistant providing supportive responses to people's moods and situations. Focus on being genuine, specific, and encouraging."
                ),
                Message(
                    role = "user",
                    content = prompt
                )
            )
        )

        val response = OpenAIService.api.getChatCompletion(request)
        val content = response.choices.firstOrNull()?.message?.content ?: return Pair(
            "Reflection time",
            "Every emotion teaches us something about ourselves. Take a moment to reflect on what you need right now."
        )

        // Parse the response
        val lines = content.lines()
        val title = lines.firstOrNull { it.startsWith("Title:") }?.substringAfter("Title:")?.trim() ?: "Reflection time"
        val message = lines.firstOrNull { it.startsWith("Message:") }?.substringAfter("Message:")?.trim()
            ?: "Every emotion teaches us something about ourselves. Take a moment to reflect on what you need right now."

        return Pair(title, message)
    } catch (e: Exception) {
        when (e) {
            is retrofit2.HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("OpenAI", "Network Error - Code: ${e.code()}, Response: $errorBody", e)
            }
            else -> {
                Log.e("OpenAI", "Error fetching response: ${e.message}", e)
            }
        }
        return Pair(
            "Reflection time",
            "Every emotion teaches us something about ourselves. Take a moment to reflect on what you need right now."
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackerScreen(
    moodViewModel: MoodViewModel,
    moodEntries: List<MoodEntry>,
    onSave: (String, String, String?, String?) -> Unit,
    onUpdate: (Int, String, String, String?, String?) -> Unit,
    navigateToHistory: () -> Unit
) {
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var supportMessage by remember { mutableStateOf<Pair<String, String>?>(null) }
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

    LaunchedEffect(todayEntry?.mood) {
        if (todayEntry != null) {
            supportMessage = getSupportMessage(todayEntry.mood, moodViewModel, todayEntry.id, todayEntry.note)
        }
    }

    // Add a function to check if there are changes
    fun hasChanges(): Boolean {
        return if (isEditing && todayEntry != null) {
            selectedMood != todayEntry.mood || note != todayEntry.note
        } else {
            selectedMood != null // For new entries, just check if mood is selected
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                                supportMessage?.let { (title, message) ->
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
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                cursorColor = MaterialTheme.colorScheme.primary,
                                disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedMood?.let {
                                    val wasEditing = isEditing
                                    coroutineScope.launch {
                                        isLoading = true
                                        try {
                                            // Always get a new support message when updating
                                            val (title, message) = getSupportMessage(it, moodViewModel, null, note)
                                            if (isEditing && todayEntry != null) {
                                                onUpdate(todayEntry.id, it, note, title, message)
                                            } else {
                                                onSave(it, note, title, message)
                                            }
                                            snackbarHostState.showSnackbar(
                                                if (wasEditing) "Mood updated!" else "Mood saved!"
                                            )
                                            selectedMood = null
                                            note = ""
                                            isEditing = false
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading && hasChanges(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isEditing) "Update Mood" else "Save Mood",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
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

                    if (BuildConfig.DEBUG) {
                        Spacer(modifier = Modifier.height(24.dp))
                        val context = LocalContext.current
                        TextButton(
                            onClick = { NotificationTester.triggerTestNotification(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Test Notification",
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