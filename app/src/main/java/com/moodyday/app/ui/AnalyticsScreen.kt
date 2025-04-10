package com.moodyday.app.ui

import android.widget.LinearLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.moodyday.app.data.MoodEntry
import com.moodyday.app.viewmodel.MoodViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp

@Composable
fun MoodInsightCard(moodEntries: List<MoodEntry>) {
    if (moodEntries.isEmpty()) return

    val latestMood = moodEntries.maxByOrNull { it.timestamp }?.mood ?: return
    val recentMoods = moodEntries.takeLast(7)
    val mostFrequentMood = recentMoods
        .groupBy { it.mood }
        .maxByOrNull { it.value.size }
        ?.key

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Insights",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Recent Mood Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Latest Mood
            Text(
                text = "Current Mood: $latestMood",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (mostFrequentMood != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Most frequent mood this week: $mostFrequentMood",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mood Streak
            if (recentMoods.size >= 2) {
                Spacer(modifier = Modifier.height(8.dp))
                val moodTrend = analyzeMoodTrend(recentMoods)
                Text(
                    text = moodTrend,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun analyzeMoodTrend(recentMoods: List<MoodEntry>): String {
    val scores = recentMoods.map { moodToScore(it.mood) }
    val increasing = scores.zipWithNext().all { (a, b) -> b >= a }
    val decreasing = scores.zipWithNext().all { (a, b) -> b <= a }
    
    return when {
        increasing -> "Your mood has been improving 📈"
        decreasing -> "Your mood has been declining 📉"
        else -> "Your mood has been varying 📊"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    months: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedMonth,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                months.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(month) },
                        onClick = {
                            onMonthSelected(month)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MoodViewModel,
    navigateBack: () -> Unit,
) {
    val moods by viewModel.allMoods.collectAsState(initial = emptyList())
    
    // Group moods by month and year
    val groupedMoods = remember(moods) {
        moods.groupBy { mood ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = mood.timestamp
            }
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        }.toSortedMap(reverseOrder())
    }
    
    var selectedMonth by remember(groupedMoods.keys) { mutableStateOf(groupedMoods.keys.firstOrNull() ?: "") }
    
    val selectedMoods = remember(selectedMonth, groupedMoods) {
        groupedMoods[selectedMonth] ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mood Analytics",
                        style = MaterialTheme.typography.titleLarge
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
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
                            "No mood data yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start tracking your moods to see analytics",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                MonthSelector(
                    months = groupedMoods.keys.toList(),
                    selectedMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val analyticsAvailable = runCatching {
                    selectedMoods.isNotEmpty()
                }.getOrDefault(false)

                if (analyticsAvailable) {
                    MoodBarChart(selectedMoods)
                    Spacer(modifier = Modifier.height(16.dp))
                    MoodInsightCard(selectedMoods)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No mood data for selected month",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodBarChart(moodEntries: List<MoodEntry>) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary.toArgb()
    val onSurfaceColor = colorScheme.onSurface.toArgb()
    val gridLineColor = colorScheme.onSurface.copy(alpha = 0.1f).toArgb()

    // Sort entries by date
    val sortedEntries = remember(moodEntries) {
        moodEntries.sortedBy { it.timestamp }
    }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    800
                )
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setDrawValueAboveBar(true)
                
                // Remove legend
                legend.isEnabled = false
                
                // Extra padding
                setExtraOffsets(16f, 16f, 16f, 16f)
                
                // Configure X Axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textSize = 12f
                    textColor = onSurfaceColor
                    labelRotationAngle = -45f
                    setAvoidFirstLastClipping(true)
                    granularity = 1f
                }

                // Configure Y Axis
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = gridLineColor
                    textSize = 14f
                    textColor = onSurfaceColor
                    setDrawAxisLine(false)
                    axisMinimum = 0f
                    axisMaximum = 7f
                    granularity = 1f
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

                // Disable highlighting
                setHighlightPerTapEnabled(false)
                setHighlightPerDragEnabled(false)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        update = { chart ->
            // Update X-axis formatter with current entries
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    if (index >= 0 && index < sortedEntries.size) {
                        return dateFormat.format(Date(sortedEntries[index].timestamp))
                    }
                    return ""
                }
            }

            val entries = sortedEntries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), moodToScore(entry.mood).toFloat())
            }

            val dataSet = BarDataSet(entries, "").apply {
                color = primaryColor
                setDrawValues(false)
                highLightAlpha = 0
            }

            val barData = BarData(dataSet).apply {
                barWidth = 0.7f
            }
            
            // Clear existing data before setting new data
            chart.clear()
            chart.data = barData

            // Configure visible range based on data size
            val visibleBars = minOf(sortedEntries.size.toFloat(), 7f)
            chart.setVisibleXRangeMaximum(visibleBars)
            
            // Only move view if there are entries
            if (sortedEntries.isNotEmpty()) {
                chart.moveViewToX(sortedEntries.size - 1f)
            }
            
            // Notify chart of data change
            chart.notifyDataSetChanged()
            
            // Animate and invalidate
            chart.animateY(1000)
            chart.invalidate()
        }
    )
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