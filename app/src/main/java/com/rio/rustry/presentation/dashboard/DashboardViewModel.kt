package com.rio.rustry.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.domain.model.UserType
import com.rio.rustry.domain.model.Result
import com.rio.rustry.data.repository.LeaderboardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color

class DashboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // GDPR consent management
    private val _hasGDPRConsent = MutableStateFlow(false)
    val hasGDPRConsent: StateFlow<Boolean> = _hasGDPRConsent.asStateFlow()

    fun loadDashboardData(userType: UserType) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Load different data based on user type
                val dashboardData = when (userType) {
                    UserType.FARMER -> loadFarmerDashboard()
                    UserType.BUYER -> loadBuyerDashboard()
                    UserType.ENTHUSIAST -> loadEnthusiastDashboard()
                    UserType.VETERINARIAN -> loadVeterinarianDashboard()
                }
                
                _uiState.value = _uiState.value.copy(
                    dashboardData = dashboardData,
                    quickStats = generateQuickStats(userType),
                    growthAnalytics = generateGrowthAnalytics(userType),
                    performanceMetrics = generatePerformanceMetrics(userType),
                    recentActivities = generateRecentActivities(userType),
                    aiInsights = generateAIInsights(userType),
                    marketTrends = if (userType == UserType.FARMER || userType == UserType.BUYER) 
                        generateMarketTrends() else null,
                    breedingProgram = if (userType == UserType.FARMER) 
                        generateBreedingProgram() else null
                )
                
            } catch (e: Exception) {
                _error.value = "Failed to load dashboard: ${e.message}"
                _isOffline.value = true
                // Load cached data if available
                loadCachedData(userType)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData(userType: UserType) {
        loadDashboardData(userType)
    }

    private suspend fun loadFarmerDashboard(): DashboardData {
        // Simulate network delay
        delay(1000)
        
        return DashboardData(
            userType = UserType.FARMER,
            totalRevenue = 125000.0,
            totalBirds = 450,
            activeListings = 12,
            completedTransactions = 28
        )
    }

    private suspend fun loadBuyerDashboard(): DashboardData {
        delay(800)
        
        return DashboardData(
            userType = UserType.BUYER,
            totalRevenue = 85000.0,
            totalBirds = 0,
            activeListings = 0,
            completedTransactions = 15
        )
    }

    private suspend fun loadEnthusiastDashboard(): DashboardData {
        delay(600)
        
        return DashboardData(
            userType = UserType.ENTHUSIAST,
            totalRevenue = 0.0,
            totalBirds = 25,
            activeListings = 2,
            completedTransactions = 5
        )
    }

    private suspend fun loadVeterinarianDashboard(): DashboardData {
        delay(700)
        
        return DashboardData(
            userType = UserType.VETERINARIAN,
            totalRevenue = 45000.0,
            totalBirds = 0,
            activeListings = 0,
            completedTransactions = 42
        )
    }

    private fun generateQuickStats(userType: UserType): List<QuickStat> {
        return when (userType) {
            UserType.FARMER -> listOf(
                QuickStat(
                    label = "Total Birds",
                    value = "450",
                    icon = Icons.Default.Pets,
                    color = Color.Blue,
                    navigationKey = "birds"
                ),
                QuickStat(
                    label = "Monthly Revenue",
                    value = "₹1.25L",
                    icon = Icons.Default.AttachMoney,
                    color = Color.Green,
                    navigationKey = "revenue"
                ),
                QuickStat(
                    label = "Active Listings",
                    value = "12",
                    icon = Icons.Default.Store,
                    color = Color.Orange,
                    navigationKey = "listings"
                ),
                QuickStat(
                    label = "Health Score",
                    value = "92%",
                    icon = Icons.Default.Favorite,
                    color = Color.Red,
                    navigationKey = "health"
                )
            )
            UserType.BUYER -> listOf(
                QuickStat(
                    label = "Purchases",
                    value = "15",
                    icon = Icons.Default.ShoppingCart,
                    color = Color.Blue,
                    navigationKey = "purchases"
                ),
                QuickStat(
                    label = "Spent This Month",
                    value = "₹85K",
                    icon = Icons.Default.AttachMoney,
                    color = Color.Green,
                    navigationKey = "spending"
                ),
                QuickStat(
                    label = "Saved Searches",
                    value = "8",
                    icon = Icons.Default.Search,
                    color = Color.Purple,
                    navigationKey = "searches"
                ),
                QuickStat(
                    label = "Wishlist",
                    value = "23",
                    icon = Icons.Default.FavoriteBorder,
                    color = Color.Red,
                    navigationKey = "wishlist"
                )
            )
            UserType.ENTHUSIAST -> listOf(
                QuickStat(
                    label = "My Birds",
                    value = "25",
                    icon = Icons.Default.Pets,
                    color = Color.Blue,
                    navigationKey = "birds"
                ),
                QuickStat(
                    label = "Posts Shared",
                    value = "18",
                    icon = Icons.Default.Share,
                    color = Color.Green,
                    navigationKey = "posts"
                ),
                QuickStat(
                    label = "Community Rank",
                    value = "#42",
                    icon = Icons.Default.EmojiEvents,
                    color = Color.Orange,
                    navigationKey = "rank"
                ),
                QuickStat(
                    label = "Knowledge Score",
                    value = "78%",
                    icon = Icons.Default.School,
                    color = Color.Purple,
                    navigationKey = "knowledge"
                )
            )
            UserType.VETERINARIAN -> listOf(
                QuickStat(
                    label = "Consultations",
                    value = "42",
                    icon = Icons.Default.MedicalServices,
                    color = Color.Blue,
                    navigationKey = "consultations"
                ),
                QuickStat(
                    label = "Monthly Earnings",
                    value = "₹45K",
                    icon = Icons.Default.AttachMoney,
                    color = Color.Green,
                    navigationKey = "earnings"
                ),
                QuickStat(
                    label = "Success Rate",
                    value = "96%",
                    icon = Icons.Default.CheckCircle,
                    color = Color.Green,
                    navigationKey = "success"
                ),
                QuickStat(
                    label = "Rating",
                    value = "4.8★",
                    icon = Icons.Default.Star,
                    color = Color.Orange,
                    navigationKey = "rating"
                )
            )
        }
    }

    private fun generateGrowthAnalytics(userType: UserType): GrowthAnalytics? {
        return if (userType == UserType.FARMER || userType == UserType.ENTHUSIAST) {
            GrowthAnalytics(
                avgWeightGain = 125.5,
                weightTrend = TrendDirection.UP,
                mortalityRate = 2.1,
                mortalityTrend = TrendDirection.DOWN,
                feedEfficiency = 92.5,
                feedTrend = TrendDirection.UP
            )
        } else null
    }

    private fun generatePerformanceMetrics(userType: UserType): List<PerformanceMetric> {
        return when (userType) {
            UserType.FARMER -> listOf(
                PerformanceMetric("Feed Efficiency", "92.5%", Color.Green),
                PerformanceMetric("Growth Rate", "+12%", Color.Blue),
                PerformanceMetric("Health Score", "94/100", Color.Green),
                PerformanceMetric("Profit Margin", "28%", Color.Green)
            )
            UserType.BUYER -> listOf(
                PerformanceMetric("Avg Price", "₹850", Color.Blue),
                PerformanceMetric("Quality Score", "4.6/5", Color.Green),
                PerformanceMetric("Delivery Time", "2.3 days", Color.Orange),
                PerformanceMetric("Satisfaction", "96%", Color.Green)
            )
            UserType.ENTHUSIAST -> listOf(
                PerformanceMetric("Engagement", "85%", Color.Blue),
                PerformanceMetric("Knowledge", "78%", Color.Purple),
                PerformanceMetric("Community", "4.2/5", Color.Green),
                PerformanceMetric("Activity", "Daily", Color.Orange)
            )
            UserType.VETERINARIAN -> listOf(
                PerformanceMetric("Success Rate", "96%", Color.Green),
                PerformanceMetric("Response Time", "2.1 hrs", Color.Blue),
                PerformanceMetric("Client Rating", "4.8/5", Color.Green),
                PerformanceMetric("Availability", "90%", Color.Orange)
            )
        }
    }

    private fun generateRecentActivities(userType: UserType): List<RecentActivity> {
        return when (userType) {
            UserType.FARMER -> listOf(
                RecentActivity(
                    id = "activity_1",
                    title = "Health checkup completed",
                    description = "Routine health inspection for Coop A",
                    timeAgo = "2 hours ago",
                    icon = Icons.Default.HealthAndSafety,
                    color = Color.Green
                ),
                RecentActivity(
                    id = "activity_2",
                    title = "New listing created",
                    description = "Posted 15 Desi chickens for sale",
                    timeAgo = "5 hours ago",
                    icon = Icons.Default.Add,
                    color = Color.Blue
                ),
                RecentActivity(
                    id = "activity_3",
                    title = "Feed order delivered",
                    description = "Premium feed stock replenished",
                    timeAgo = "1 day ago",
                    icon = Icons.Default.LocalShipping,
                    color = Color.Orange
                )
            )
            UserType.BUYER -> listOf(
                RecentActivity(
                    id = "activity_1",
                    title = "Purchase completed",
                    description = "Bought 10 Rhode Island Red hens",
                    timeAgo = "3 hours ago",
                    icon = Icons.Default.ShoppingCart,
                    color = Color.Green
                ),
                RecentActivity(
                    id = "activity_2",
                    title = "Payment processed",
                    description = "₹8,500 payment successful",
                    timeAgo = "3 hours ago",
                    icon = Icons.Default.Payment,
                    color = Color.Blue
                ),
                RecentActivity(
                    id = "activity_3",
                    title = "Delivery scheduled",
                    description = "Birds will arrive tomorrow",
                    timeAgo = "4 hours ago",
                    icon = Icons.Default.Schedule,
                    color = Color.Orange
                )
            )
            UserType.ENTHUSIAST -> listOf(
                RecentActivity(
                    id = "activity_1",
                    title = "Photo shared",
                    description = "Posted new pictures of your flock",
                    timeAgo = "1 hour ago",
                    icon = Icons.Default.PhotoCamera,
                    color = Color.Purple
                ),
                RecentActivity(
                    id = "activity_2",
                    title = "Question answered",
                    description = "Helped with breeding advice",
                    timeAgo = "4 hours ago",
                    icon = Icons.Default.QuestionAnswer,
                    color = Color.Blue
                ),
                RecentActivity(
                    id = "activity_3",
                    title = "Achievement unlocked",
                    description = "Earned 'Community Helper' badge",
                    timeAgo = "2 days ago",
                    icon = Icons.Default.EmojiEvents,
                    color = Color.Orange
                )
            )
            UserType.VETERINARIAN -> listOf(
                RecentActivity(
                    id = "activity_1",
                    title = "Consultation completed",
                    description = "Treated respiratory infection",
                    timeAgo = "30 minutes ago",
                    icon = Icons.Default.MedicalServices,
                    color = Color.Green
                ),
                RecentActivity(
                    id = "activity_2",
                    title = "Emergency call",
                    description = "Responded to urgent health issue",
                    timeAgo = "2 hours ago",
                    icon = Icons.Default.Emergency,
                    color = Color.Red
                ),
                RecentActivity(
                    id = "activity_3",
                    title = "Report submitted",
                    description = "Health assessment for Farm XYZ",
                    timeAgo = "5 hours ago",
                    icon = Icons.Default.Assignment,
                    color = Color.Blue
                )
            )
        }
    }

    private fun generateAIInsights(userType: UserType): List<AIInsight> {
        return when (userType) {
            UserType.FARMER -> listOf(
                AIInsight(
                    title = "Optimal Selling Time",
                    description = "Market analysis suggests selling your mature birds in the next 2 weeks for maximum profit.",
                    priority = InsightPriority.HIGH
                ),
                AIInsight(
                    title = "Feed Optimization",
                    description = "Consider switching to high-protein feed for better growth rates in your current flock.",
                    priority = InsightPriority.MEDIUM
                )
            )
            UserType.BUYER -> listOf(
                AIInsight(
                    title = "Price Alert",
                    description = "Desi chicken prices are expected to drop by 8% next week. Consider waiting for better deals.",
                    priority = InsightPriority.HIGH
                ),
                AIInsight(
                    title = "Quality Recommendation",
                    description = "Farmer Rajesh Kumar has consistently high-quality birds matching your preferences.",
                    priority = InsightPriority.MEDIUM
                )
            )
            UserType.ENTHUSIAST -> listOf(
                AIInsight(
                    title = "Breeding Opportunity",
                    description = "Your rooster and hens show excellent genetic compatibility for breeding.",
                    priority = InsightPriority.MEDIUM
                ),
                AIInsight(
                    title = "Community Engagement",
                    description = "Share your recent success story to help other enthusiasts and earn community points.",
                    priority = InsightPriority.LOW
                )
            )
            UserType.VETERINARIAN -> listOf(
                AIInsight(
                    title = "Disease Pattern Alert",
                    description = "Increased respiratory issues reported in your area. Prepare preventive measures.",
                    priority = InsightPriority.HIGH
                ),
                AIInsight(
                    title = "Consultation Demand",
                    description = "High demand for your services this week. Consider extending availability hours.",
                    priority = InsightPriority.MEDIUM
                )
            )
        }
    }

    private fun generateMarketTrends(): MarketTrends {
        return MarketTrends(
            averagePrice = 950.0,
            priceTrend = TrendDirection.UP,
            demandLevel = "High",
            demandTrend = TrendDirection.UP,
            summary = "Strong demand for Desi breeds with prices trending upward due to festival season approaching."
        )
    }

    private fun generateBreedingProgram(): BreedingProgram {
        return BreedingProgram(
            activePairs = 8,
            expectedHatchDate = "March 15, 2024",
            recommendation = "Maintain current incubation temperature. Expected hatch rate: 85%"
        )
    }

    private fun loadCachedData(userType: UserType) {
        // Load cached data when offline
        viewModelScope.launch {
            try {
                // Simulate loading cached data
                _uiState.value = _uiState.value.copy(
                    quickStats = generateQuickStats(userType),
                    recentActivities = generateRecentActivities(userType).take(2), // Limited cached data
                    performanceMetrics = generatePerformanceMetrics(userType).take(2)
                )
            } catch (e: Exception) {
                _error.value = "Failed to load cached data: ${e.message}"
            }
        }
    }

    fun hasGDPRConsent(): Boolean {
        // In real implementation, this would check SharedPreferences or database
        return _hasGDPRConsent.value
    }

    fun acceptGDPRConsent() {
        _hasGDPRConsent.value = true
        // In real implementation, save to SharedPreferences or database
    }

    fun clearError() {
        _error.value = null
    }
}

data class DashboardUiState(
    val dashboardData: DashboardData? = null,
    val quickStats: List<QuickStat> = emptyList(),
    val growthAnalytics: GrowthAnalytics? = null,
    val performanceMetrics: List<PerformanceMetric> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val aiInsights: List<AIInsight> = emptyList(),
    val marketTrends: MarketTrends? = null,
    val breedingProgram: BreedingProgram? = null
)

data class DashboardData(
    val userType: UserType,
    val totalRevenue: Double,
    val totalBirds: Int,
    val activeListings: Int,
    val completedTransactions: Int
)