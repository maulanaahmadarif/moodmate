package com.moodyday.app.ui

import android.util.Log
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.moodyday.app.data.MoodEntry
import com.moodyday.app.viewmodel.MoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MoodViewModel,
    navigateBack: () -> Unit,
) {
    val moodEntries by viewModel.getMoodList().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Analytics") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            if (moodEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("No mood data yet.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    MoodLineChart(moodEntries)
                }
            }
        }
    )
}

@Composable
fun MoodLineChart(moodEntries: List<MoodEntry>) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val isDarkTheme = MaterialTheme.colorScheme.surface.toArgb() == Color(0xFF1E1E2E).toArgb()
    
    // Extract all theme colors before AndroidView
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f).toArgb()
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.2f else 0.1f).toArgb()

    AndroidView(factory = {
        LineChart(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
            )
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = onSurfaceColor
            
            // Configure X Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textSize = 10f
                textColor = onSurfaceColor
                labelRotationAngle = -45f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        if (index >= 0 && index < moodEntries.size) {
                            return dateFormat.format(Date(moodEntries[index].timestamp))
                        }
                        return ""
                    }
                }
            }

            // Configure Y Axis
            axisLeft.apply {
                setDrawGridLines(true)
                textSize = 10f
                textColor = onSurfaceColor
                gridColor = gridLineColor
                axisMinimum = 0f
                axisMaximum = 7f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            1 -> "😔"
                            2 -> "😡"
                            3 -> "😴"
                            4 -> "😐"
                            5 -> "😎"
                            6 -> "😊"
                            else -> ""
                        }
                    }
                }
            }
            axisRight.isEnabled = false
        }
    }, update = { chart ->
        val entries = moodEntries.mapIndexed { index, entry ->
            Entry(index.toFloat(), moodToScore(entry.mood).toFloat())
        }

        val dataSet = LineDataSet(entries, "Mood Over Time").apply {
            color = primaryColor
            valueTextColor = onSurfaceColor
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(primaryColor)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            this.fillColor = fillColor
        }

        val lineData = LineData(dataSet as ILineDataSet)
        chart.data = lineData

        // Ensure proper spacing and fitting
        chart.setVisibleXRangeMaximum(7f) // Show 7 days at a time
        chart.moveViewToX(moodEntries.size - 1f) // Move to the latest entry
        chart.invalidate()
    })
}

fun moodToScore(mood: String): Int {
    return when (mood.lowercase(Locale.getDefault())) {
        "😊" -> 6
        "😔" -> 1
        "😴" -> 3
        "😡" -> 2
        "😐" -> 4
        "😎" -> 5
        else -> 0
    }
}