package com.moodyday.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moodyday.app.ui.AnalyticsScreen
import com.moodyday.app.ui.MoodHistoryScreen
import com.moodyday.app.ui.MoodTrackerScreen
import com.moodyday.app.ui.theme.MoodydayTheme
import com.moodyday.app.viewmodel.MoodViewModel
import com.moodyday.app.viewmodel.MoodViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: MoodViewModel by viewModels {
        MoodViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoodydayTheme {
                val navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = "history") {
                        composable(route = "tracker") {
                            MoodTrackerScreen(
                                onSave = { mood: String, note: String ->
                                    viewModel.saveMood(mood, note)
                                    navController.navigate("history")
                                },
                                navigateBack = {
                                    navController.popBackStack()
                                },
                            )
                        }

                        composable(route = "history") {
                            val moods by viewModel.allMoods.collectAsState(initial = emptyList())
                            MoodHistoryScreen(
                                moods = moods,
                                onNavigateToEntry = { navController.navigate("tracker") },
                                onDelete = { viewModel.deleteMood(it) },
                                onUndo = { mood -> viewModel.saveMood(mood.mood, mood.note) },
                                onNavigateToAnalytics = { navController.navigate("analytics") }
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
