// generated/phase3/app/src/main/java/com/rio/rustry/dashboard/BreedingAnalyticsScreen.kt

package com.rio.rustry.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingAnalyticsScreen(
    viewModel: BreedingAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.THIRTY_DAYS) }

    LaunchedEffect(selectedPeriod) {
        viewModel.loadAnalytics(selectedPeriod)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Period Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(AnalyticsPeriod.values()) { period ->
                FilterChip(
                    onClick = { selectedPeriod = period },
                    label = { Text(period.displayName) },
                    selected = selectedPeriod == period,
                    leadingIcon = if (selectedPeriod == period) {
                        { Icon(Icons.Default.DateRange, contentDescription = null) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is BreedingAnalyticsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BreedingAnalyticsUiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // KPI Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCard(
                                title = "Hatch Rate",
                                value = "${uiState.analytics.hatchRate}%",
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Mortality",
                                value = "${uiState.analytics.mortalityRate}%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        KpiCard(
                            title = "Avg Weight Gain",
                            value = "${uiState.analytics.avgWeightGain}g",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Charts
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Breeding Trends",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                AndroidView(
                                    factory = { context ->
                                        LineChart(context).apply {
                                            description.isEnabled = false
                                            setTouchEnabled(true)
                                            isDragEnabled = true
                                            setScaleEnabled(true)
                                            setPinchZoom(true)
                                        }
                                    },
                                    update = { chart ->
                                        val entries = uiState.analytics.trendData.mapIndexed { index, value ->
                                            Entry(index.toFloat(), value.toFloat())
                                        }
                                        
                                        val dataSet = LineDataSet(entries, "Hatch Rate").apply {
                                            color = Color.Blue.hashCode()
                                            setCircleColor(Color.Blue.hashCode())
                                            lineWidth = 2f
                                            circleRadius = 4f
                                            setDrawCircleHole(false)
                                            valueTextSize = 10f
                                        }
                                        
                                        chart.data = LineData(dataSet)
                                        chart.invalidate()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
            is BreedingAnalyticsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class AnalyticsPeriod(val displayName: String, val days: Int) {
    SEVEN_DAYS("7 Days", 7),
    THIRTY_DAYS("30 Days", 30),
    NINETY_DAYS("90 Days", 90),
    CUSTOM("Custom", 0)
}