package com.moodyday.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moodyday.app.data.MoodEntry
import com.moodyday.app.ui.components.AnimatedMoodCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MoodHistoryScreen(
    moods: List<MoodEntry>,
    onNavigateToEntry: () -> Unit,
    onDelete: (MoodEntry) -> Unit,
    onUndo: (MoodEntry) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    navigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var moodToDelete by remember { mutableStateOf<MoodEntry?>(null) }
    var recentlyDeletedMood by remember { mutableStateOf<MoodEntry?>(null) }

    // Check if there's a mood entry for today
    val hasTodayMood = remember(moods) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        moods.any { mood ->
            val moodDate = Calendar.getInstance().apply {
                timeInMillis = mood.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            moodDate == today
        }
    }

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
                title = { 
                    Text(
                        "Mood History",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
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
            if (!hasTodayMood) {
                FloatingActionButton(
                    onClick = onNavigateToEntry,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Mood")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (moods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            "No mood entries yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start tracking your moods by clicking the + button",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedMoods.forEach { (monthKey, monthMoods) ->
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, monthKey / 100)
                            set(Calendar.MONTH, monthKey % 100)
                        }
                        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            .format(calendar.time)
                        
                        stickyHeader(key = monthKey) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .zIndex(1f)
                            ) {
                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                        
                        itemsIndexed(
                            items = monthMoods,
                            key = { _, mood -> mood.id }
                        ) { index, mood ->
                            AnimatedMoodCard(
                                entry = mood,
                                onDeleteRequest = { moodToDelete = it },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = if (index == monthMoods.lastIndex) 16.dp else 0.dp)
                                    .animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 200,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
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
                            val deletedMood = moodToDelete!!.copy() // Make a copy to preserve all fields
                            onDelete(deletedMood)
                            recentlyDeletedMood = deletedMood

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
}