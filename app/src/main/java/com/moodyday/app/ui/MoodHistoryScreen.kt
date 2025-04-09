package com.moodyday.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moodyday.app.data.MoodEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodHistoryScreen(
    moods: List<MoodEntry>,
    onNavigateToEntry: () -> Unit,
    onDelete: (MoodEntry) -> Unit,
    onUndo: (MoodEntry) -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var moodToDelete by remember { mutableStateOf<MoodEntry?>(null) }
    var recentlyDeletedMood by remember { mutableStateOf<MoodEntry?>(null) }

    // Group moods by month
    val groupedMoods = moods.groupBy { entry ->
        val calendar = Calendar.getInstance().apply { 
            timeInMillis = entry.timestamp 
        }
        calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH)
    }.toSortedMap(compareByDescending { it })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track your Mood") },
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "View Analytics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEntry,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Mood")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (moods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No mood entries yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        groupedMoods.forEach { (monthKey, monthMoods) ->
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, monthKey / 100)
                                set(Calendar.MONTH, monthKey % 100)
                            }
                            val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
                            
                            item {
                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(monthMoods) { mood ->
                                MoodHistoryItem(
                                    entry = mood,
                                    onDeleteRequest = { moodToDelete = it }
                                )
                            }
                        }
                    }
                }
            }

            if (moodToDelete != null) {
                AlertDialog(
                    onDismissRequest = { moodToDelete = null },
                    title = { 
                        Text(
                            "Delete Entry",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = { 
                        Text(
                            "Are you sure you want to delete this mood entry?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDelete(moodToDelete!!)
                                recentlyDeletedMood = moodToDelete

                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Mood entry deleted",
                                        actionLabel = "Undo"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        recentlyDeletedMood?.let { onUndo(it) }
                                        recentlyDeletedMood = null
                                    }
                                }

                                moodToDelete = null
                            }
                        ) {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { moodToDelete = null }) {
                            Text(
                                "Cancel",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun MoodHistoryItem(entry: MoodEntry, onDeleteRequest: (MoodEntry) -> Unit) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(entry.timestamp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Mood : ${entry.mood}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.note.isNotBlank()) {
                        Text(
                            text = "Note : ${entry.note}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                IconButton(onClick = { onDeleteRequest(entry) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete mood entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}