package com.rio.rustry.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rustry.R
import com.rio.rustry.domain.model.UserType
import com.rio.rustry.presentation.components.OfflineIndicator
import com.rio.rustry.presentation.components.GDPRConsentDialog
import com.rio.rustry.presentation.components.LoadingIndicator
import com.rio.rustry.presentation.components.ErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighLevelDashboard(
    userType: UserType,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showGDPRDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData(userType)
        if (!viewModel.hasGDPRConsent()) {
            showGDPRDialog = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with offline indicator
        DashboardHeader(
            userType = userType,
            isOffline = isOffline,
            onRefresh = { viewModel.refreshData(userType) }
        )

        when {
            isLoading && uiState.dashboardData == null -> {
                LoadingIndicator(
                    message = stringResource(R.string.loading_dashboard),
                    modifier = Modifier.fillMaxSize()
                )
            }
            error != null -> {
                ErrorMessage(
                    message = error,
                    onRetry = { viewModel.loadDashboardData(userType) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Stats Cards
                    item {
                        QuickStatsSection(
                            stats = uiState.quickStats,
                            userType = userType,
                            onStatClick = onNavigateToDetail
                        )
                    }

                    // Growth Analytics
                    item {
                        GrowthAnalyticsSection(
                            analytics = uiState.growthAnalytics,
                            onViewDetails = { onNavigateToDetail("growth_analytics") }
                        )
                    }

                    // Performance Metrics
                    item {
                        PerformanceMetricsSection(
                            metrics = uiState.performanceMetrics,
                            userType = userType,
                            onViewDetails = { onNavigateToDetail("performance") }
                        )
                    }

                    // Recent Activities
                    item {
                        RecentActivitiesSection(
                            activities = uiState.recentActivities,
                            onActivityClick = onNavigateToDetail
                        )
                    }

                    // AI Insights
                    item {
                        AIInsightsSection(
                            insights = uiState.aiInsights,
                            onViewAllInsights = { onNavigateToDetail("ai_insights") }
                        )
                    }

                    // Market Trends (for Farmers and Buyers)
                    if (userType == UserType.FARMER || userType == UserType.BUYER) {
                        item {
                            MarketTrendsSection(
                                trends = uiState.marketTrends,
                                onViewMarket = { onNavigateToDetail("market") }
                            )
                        }
                    }

                    // Breeding Program (for Farmers)
                    if (userType == UserType.FARMER) {
                        item {
                            BreedingProgramSection(
                                program = uiState.breedingProgram,
                                onViewProgram = { onNavigateToDetail("breeding") }
                            )
                        }
                    }
                }
            }
        }
    }

    // GDPR Consent Dialog
    if (showGDPRDialog) {
        GDPRConsentDialog(
            onAccept = {
                viewModel.acceptGDPRConsent()
                showGDPRDialog = false
            },
            onDecline = {
                showGDPRDialog = false
                // Handle decline - maybe show limited functionality
            }
        )
    }
}

@Composable
private fun DashboardHeader(
    userType: UserType,
    isOffline: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_welcome),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            when (userType) {
                                UserType.FARMER -> R.string.farmer_dashboard_subtitle
                                UserType.BUYER -> R.string.buyer_dashboard_subtitle
                                UserType.ENTHUSIAST -> R.string.enthusiast_dashboard_subtitle
                                UserType.VETERINARIAN -> R.string.vet_dashboard_subtitle
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh)
                    )
                }
            }

            if (isOffline) {
                Spacer(modifier = Modifier.height(8.dp))
                OfflineIndicator()
            }
        }
    }
}

@Composable
private fun QuickStatsSection(
    stats: List<QuickStat>,
    userType: UserType,
    onStatClick: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.quick_stats),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(stats) { stat ->
            QuickStatCard(
                stat = stat,
                onClick = { onStatClick(stat.navigationKey) }
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    stat: QuickStat,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = stat.color,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun GrowthAnalyticsSection(
    analytics: GrowthAnalytics?,
    onViewDetails: () -> Unit
) {
    analytics?.let {
        SectionCard(
            title = stringResource(R.string.growth_analytics),
            onViewMore = onViewDetails
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Growth chart placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.growth_chart_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GrowthMetric(
                        label = stringResource(R.string.avg_weight_gain),
                        value = "${analytics.avgWeightGain}g/week",
                        trend = analytics.weightTrend
                    )
                    GrowthMetric(
                        label = stringResource(R.string.mortality_rate),
                        value = "${analytics.mortalityRate}%",
                        trend = analytics.mortalityTrend
                    )
                    GrowthMetric(
                        label = stringResource(R.string.feed_efficiency),
                        value = "${analytics.feedEfficiency}%",
                        trend = analytics.feedTrend
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthMetric(
    label: String,
    value: String,
    trend: TrendDirection
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Icon(
            imageVector = when (trend) {
                TrendDirection.UP -> Icons.Default.TrendingUp
                TrendDirection.DOWN -> Icons.Default.TrendingDown
                TrendDirection.STABLE -> Icons.Default.TrendingFlat
            },
            contentDescription = null,
            tint = when (trend) {
                TrendDirection.UP -> Color.Green
                TrendDirection.DOWN -> Color.Red
                TrendDirection.STABLE -> Color.Gray
            },
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun PerformanceMetricsSection(
    metrics: List<PerformanceMetric>,
    userType: UserType,
    onViewDetails: () -> Unit
) {
    SectionCard(
        title = stringResource(R.string.performance_metrics),
        onViewMore = onViewDetails
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(metrics) { metric ->
                PerformanceMetricCard(metric = metric)
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(
    metric: PerformanceMetric
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = metric.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = metric.color
            )
        }
    }
}

@Composable
private fun RecentActivitiesSection(
    activities: List<RecentActivity>,
    onActivityClick: (String) -> Unit
) {
    SectionCard(
        title = stringResource(R.string.recent_activities),
        onViewMore = { onActivityClick("all_activities") }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activities.take(3).forEach { activity ->
                ActivityItem(
                    activity = activity,
                    onClick = { onActivityClick(activity.id) }
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(
    activity: RecentActivity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = activity.color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = activity.timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AIInsightsSection(
    insights: List<AIInsight>,
    onViewAllInsights: () -> Unit
) {
    SectionCard(
        title = stringResource(R.string.ai_insights),
        onViewMore = onViewAllInsights
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            insights.take(2).forEach { insight ->
                AIInsightCard(insight = insight)
            }
        }
    }
}

@Composable
private fun AIInsightCard(
    insight: AIInsight
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = insight.priority.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = insight.priority.color,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MarketTrendsSection(
    trends: MarketTrends?,
    onViewMarket: () -> Unit
) {
    trends?.let {
        SectionCard(
            title = stringResource(R.string.market_trends),
            onViewMore = onViewMarket
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MarketTrendItem(
                        label = stringResource(R.string.avg_price),
                        value = "₹${trends.averagePrice}",
                        trend = trends.priceTrend
                    )
                    MarketTrendItem(
                        label = stringResource(R.string.demand),
                        value = trends.demandLevel,
                        trend = trends.demandTrend
                    )
                }
                
                Text(
                    text = trends.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MarketTrendItem(
    label: String,
    value: String,
    trend: TrendDirection
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            imageVector = when (trend) {
                TrendDirection.UP -> Icons.Default.TrendingUp
                TrendDirection.DOWN -> Icons.Default.TrendingDown
                TrendDirection.STABLE -> Icons.Default.TrendingFlat
            },
            contentDescription = null,
            tint = when (trend) {
                TrendDirection.UP -> Color.Green
                TrendDirection.DOWN -> Color.Red
                TrendDirection.STABLE -> Color.Gray
            },
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun BreedingProgramSection(
    program: BreedingProgram?,
    onViewProgram: () -> Unit
) {
    program?.let {
        SectionCard(
            title = stringResource(R.string.breeding_program),
            onViewMore = onViewProgram
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.active_pairs),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${program.activePairs}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.expected_hatch),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = program.expectedHatchDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = program.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    onViewMore: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onViewMore) {
                    Text(text = stringResource(R.string.view_more))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// Data classes for dashboard components
data class QuickStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val navigationKey: String
)

data class GrowthAnalytics(
    val avgWeightGain: Double,
    val weightTrend: TrendDirection,
    val mortalityRate: Double,
    val mortalityTrend: TrendDirection,
    val feedEfficiency: Double,
    val feedTrend: TrendDirection
)

data class PerformanceMetric(
    val name: String,
    val value: String,
    val color: Color
)

data class RecentActivity(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val icon: ImageVector,
    val color: Color
)

data class AIInsight(
    val title: String,
    val description: String,
    val priority: InsightPriority
)

data class MarketTrends(
    val averagePrice: Double,
    val priceTrend: TrendDirection,
    val demandLevel: String,
    val demandTrend: TrendDirection,
    val summary: String
)

data class BreedingProgram(
    val activePairs: Int,
    val expectedHatchDate: String,
    val recommendation: String
)

enum class TrendDirection {
    UP, DOWN, STABLE
}

enum class InsightPriority(val color: Color) {
    HIGH(Color.Red),
    MEDIUM(Color.Orange),
    LOW(Color.Blue)
}