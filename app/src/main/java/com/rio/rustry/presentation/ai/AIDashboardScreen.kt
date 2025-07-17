package com.rio.rustry.presentation.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rio.rustry.ai.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDashboardScreen(
    viewModel: AIViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "AI Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Actions
            item {
                QuickActionsSection(
                    onImageAnalysis = { viewModel.startImageAnalysis() },
                    onMarketAnalysis = { viewModel.startMarketAnalysis() },
                    onHealthCheck = { viewModel.startHealthCheck() },
                    onBreedingOptimization = { viewModel.startBreedingOptimization() }
                )
            }
            
            // Smart Alerts
            item {
                SmartAlertsSection(
                    alerts = uiState.smartAlerts,
                    onAlertAction = { alert, action -> 
                        viewModel.handleAlertAction(alert, action)
                    }
                )
            }
            
            // Personalized Insights
            item {
                PersonalizedInsightsSection(
                    insights = uiState.personalizedInsights
                )
            }
            
            // Performance Metrics
            item {
                PerformanceMetricsSection(
                    metrics = uiState.performanceMetrics
                )
            }
            
            // AI Recommendations
            item {
                AIRecommendationsSection(
                    recommendations = uiState.recommendations
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onImageAnalysis: () -> Unit,
    onMarketAnalysis: () -> Unit,
    onHealthCheck: () -> Unit,
    onBreedingOptimization: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    listOf(
                        AIAction("Image Analysis", Icons.Default.CameraAlt, onImageAnalysis),
                        AIAction("Market Trends", Icons.Default.TrendingUp, onMarketAnalysis),
                        AIAction("Health Check", Icons.Default.LocalHospital, onHealthCheck),
                        AIAction("Breeding AI", Icons.Default.Psychology, onBreedingOptimization)
                    )
                ) { action ->
                    AIActionCard(action)
                }
            }
        }
    }
}

@Composable
fun AIActionCard(action: AIAction) {
    Card(
        onClick = action.onClick,
        modifier = Modifier.size(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SmartAlertsSection(
    alerts: List<SmartAlert>,
    onAlertAction: (SmartAlert, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (alerts.isNotEmpty()) {
                    Badge {
                        Text(text = alerts.size.toString())
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (alerts.isEmpty()) {
                Text(
                    text = "No alerts at the moment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                alerts.take(3).forEach { alert ->
                    SmartAlertCard(
                        alert = alert,
                        onAction = { action -> onAlertAction(alert, action) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (alerts.size > 3) {
                    TextButton(
                        onClick = { /* Navigate to full alerts screen */ }
                    ) {
                        Text("View all ${alerts.size} alerts")
                    }
                }
            }
        }
    }
}

@Composable
fun SmartAlertCard(
    alert: SmartAlert,
    onAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.priority) {
                "High" -> MaterialTheme.colorScheme.errorContainer
                "Medium" -> MaterialTheme.colorScheme.warningContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Icon(
                    imageVector = when (alert.type) {
                        "Health" -> Icons.Default.LocalHospital
                        "Market" -> Icons.Default.TrendingUp
                        "Weather" -> Icons.Default.Cloud
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = alert.type,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (alert.actionRequired && alert.suggestedActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alert.suggestedActions.take(2)) { action ->
                        AssistChip(
                            onClick = { onAction(action) },
                            label = { Text(action, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalizedInsightsSection(
    insights: PersonalizedInsights?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            insights?.let { data ->
                data.insights.take(3).forEach { insight ->
                    InsightCard(insight)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (data.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recommendations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    data.recommendations.take(2).forEach { recommendation ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } ?: run {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun InsightCard(insight: Insight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (insight.category) {
                    "Health" -> Icons.Default.LocalHospital
                    "Market" -> Icons.Default.TrendingUp
                    "Performance" -> Icons.Default.Analytics
                    else -> Icons.Default.Info
                },
                contentDescription = insight.category,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (insight.actionable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Action Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceMetricsSection(
    metrics: List<PerformanceMetric>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Performance Metrics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (metrics.isEmpty()) {
                Text(
                    text = "Loading metrics...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                metrics.take(4).forEach { metric ->
                    MetricCard(metric)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MetricCard(metric: PerformanceMetric) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metric.metric,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Benchmark: ${metric.benchmark}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when (metric.trend) {
                    "Improving" -> Color(0xFF4CAF50)
                    "Declining" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (metric.trend) {
                        "Improving" -> Icons.Default.TrendingUp
                        "Declining" -> Icons.Default.TrendingDown
                        else -> Icons.Default.TrendingFlat
                    },
                    contentDescription = metric.trend,
                    modifier = Modifier.size(16.dp),
                    tint = when (metric.trend) {
                        "Improving" -> Color(0xFF4CAF50)
                        "Declining" -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = metric.trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AIRecommendationsSection(
    recommendations: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI Recommendations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (recommendations.isEmpty()) {
                Text(
                    text = "No recommendations available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recommendations.take(3).forEachIndexed { index, recommendation ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Card(
                            modifier = Modifier.size(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Data classes
data class AIAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// Extension property for warning container color
val ColorScheme.warningContainer: Color
    @Composable get() = Color(0xFFFFF3CD)