package com.moodyday.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moodyday.app.notification.MoodReminderScheduler
import com.moodyday.app.ui.AnalyticsScreen
import com.moodyday.app.ui.MoodHistoryScreen
import com.moodyday.app.ui.MoodTrackerScreen
import com.moodyday.app.ui.theme.MoodydayTheme
import com.moodyday.app.utils.MoodUtils
import com.moodyday.app.utils.PermissionUtils
import com.moodyday.app.viewmodel.MoodViewModel
import com.moodyday.app.viewmodel.MoodViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: MoodViewModel by viewModels {
        MoodViewModelFactory(application)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, schedule the reminder
            MoodReminderScheduler.scheduleDailyReminder(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request notification permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionUtils.hasNotificationPermission(this)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Permission already granted, schedule the reminder
                MoodReminderScheduler.scheduleDailyReminder(this)
            }
        } else {
            // No runtime permission needed for Android 12 and below
            MoodReminderScheduler.scheduleDailyReminder(this)
        }
        
        enableEdgeToEdge()
        setContent {
            MoodydayTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val moods by viewModel.allMoods.collectAsState(initial = null)
                    
                    if (moods == null) {
                        // Show loading indicator while data is being fetched
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        val navController = rememberNavController()
                        
                        NavHost(
                            navController = navController,
                            startDestination = "tracker"
                        ) {
                            composable(route = "tracker") {
                                MoodTrackerScreen(
                                    moodEntries = moods ?: emptyList(),
                                    onSave = { mood: String, note: String ->
                                        viewModel.saveMood(mood, note)
                                    },
                                    onUpdate = { id: Int, mood: String, note: String ->
                                        viewModel.updateMood(id, mood, note)
                                    },
                                    navigateToHistory = { navController.navigate("history") }
                                )
                            }

                            composable(route = "history") {
                                MoodHistoryScreen(
                                    moods = moods ?: emptyList(),
                                    onNavigateToEntry = { navController.navigate("tracker") },
                                    onDelete = { viewModel.deleteMood(it) },
                                    onUndo = { mood -> viewModel.saveMood(mood.mood, mood.note) },
                                    onNavigateToAnalytics = { navController.navigate("analytics") },
                                    navigateBack = { navController.popBackStack() }
                                )
                            }

                            composable("analytics") {
                                AnalyticsScreen(
                                    viewModel = viewModel,
                                    navigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
