package com.rio.rustry.data.repository

import com.rio.rustry.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor() {

    // Mock data for leaderboards
    private val mockFarmers = listOf(
        LeaderboardEntry(
            id = "farmer_1",
            name = "Rajesh Kumar",
            location = "Hyderabad, Telangana",
            score = 2850,
            rank = 1,
            category = LeaderboardCategory.OVERALL,
            avatar = null,
            achievements = listOf("Top Producer", "Quality Champion", "Innovation Leader"),
            stats = FarmerStats(
                totalBirds = 500,
                avgWeight = 2.5,
                mortalityRate = 2.1,
                feedEfficiency = 92.5,
                monthlyRevenue = 125000.0
            )
        ),
        LeaderboardEntry(
            id = "farmer_2",
            name = "Priya Sharma",
            location = "Bangalore, Karnataka",
            score = 2720,
            rank = 2,
            category = LeaderboardCategory.OVERALL,
            avatar = null,
            achievements = listOf("Sustainable Farming", "Health Expert"),
            stats = FarmerStats(
                totalBirds = 350,
                avgWeight = 2.8,
                mortalityRate = 1.8,
                feedEfficiency = 94.2,
                monthlyRevenue = 98000.0
            )
        ),
        LeaderboardEntry(
            id = "farmer_3",
            name = "Mohammed Ali",
            location = "Chennai, Tamil Nadu",
            score = 2650,
            rank = 3,
            category = LeaderboardCategory.OVERALL,
            avatar = null,
            achievements = listOf("Growth Master", "Community Leader"),
            stats = FarmerStats(
                totalBirds = 420,
                avgWeight = 2.6,
                mortalityRate = 2.3,
                feedEfficiency = 91.8,
                monthlyRevenue = 110000.0
            )
        ),
        LeaderboardEntry(
            id = "farmer_4",
            name = "Sunita Devi",
            location = "Pune, Maharashtra",
            score = 2580,
            rank = 4,
            category = LeaderboardCategory.OVERALL,
            avatar = null,
            achievements = listOf("Efficiency Expert"),
            stats = FarmerStats(
                totalBirds = 280,
                avgWeight = 2.4,
                mortalityRate = 1.9,
                feedEfficiency = 93.1,
                monthlyRevenue = 85000.0
            )
        ),
        LeaderboardEntry(
            id = "farmer_5",
            name = "Arjun Reddy",
            location = "Visakhapatnam, Andhra Pradesh",
            score = 2520,
            rank = 5,
            category = LeaderboardCategory.OVERALL,
            avatar = null,
            achievements = listOf("Tech Adopter", "Quality Producer"),
            stats = FarmerStats(
                totalBirds = 380,
                avgWeight = 2.7,
                mortalityRate = 2.0,
                feedEfficiency = 90.5,
                monthlyRevenue = 95000.0
            )
        )
    )

    private val mockEvents = listOf(
        LeaderboardEvent(
            id = "event_1",
            title = "Best Growth Rate Challenge",
            description = "Compete for the highest average weight gain per bird this month",
            startDate = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
            endDate = System.currentTimeMillis() + (23 * 24 * 60 * 60 * 1000), // 23 days from now
            category = LeaderboardCategory.GROWTH_RATE,
            prize = "₹50,000 + Premium Feed Package",
            participants = 156,
            status = EventStatus.ACTIVE,
            rules = listOf(
                "Minimum 100 birds to participate",
                "Daily weight recordings required",
                "Verified by AI image analysis",
                "Must maintain health standards"
            )
        ),
        LeaderboardEvent(
            id = "event_2",
            title = "Sustainability Champion",
            description = "Showcase your eco-friendly farming practices and efficiency",
            startDate = System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000), // 5 days from now
            endDate = System.currentTimeMillis() + (35 * 24 * 60 * 60 * 1000), // 35 days from now
            category = LeaderboardCategory.SUSTAINABILITY,
            prize = "₹75,000 + Solar Panel Installation",
            participants = 89,
            status = EventStatus.UPCOMING,
            rules = listOf(
                "Document sustainable practices",
                "Reduce feed waste by 15%",
                "Use renewable energy sources",
                "Community impact assessment"
            )
        ),
        LeaderboardEvent(
            id = "event_3",
            title = "Health Excellence Award",
            description = "Maintain the lowest mortality rate while maximizing productivity",
            startDate = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000), // 15 days ago
            endDate = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000), // 1 day ago
            category = LeaderboardCategory.HEALTH_MANAGEMENT,
            prize = "₹40,000 + Veterinary Consultation Package",
            participants = 203,
            status = EventStatus.COMPLETED,
            rules = listOf(
                "Minimum 6 months of health records",
                "Regular veterinary checkups",
                "Vaccination compliance",
                "Disease prevention measures"
            )
        )
    )

    fun getLeaderboard(
        category: LeaderboardCategory = LeaderboardCategory.OVERALL,
        timeframe: LeaderboardTimeframe = LeaderboardTimeframe.MONTHLY,
        region: String? = null
    ): Flow<Result<List<LeaderboardEntry>>> = flow {
        emit(Result.Loading)
        
        try {
            // Simulate network delay
            delay(1000)
            
            // Filter and sort mock data based on parameters
            var filteredEntries = mockFarmers.filter { entry ->
                when (category) {
                    LeaderboardCategory.OVERALL -> true
                    else -> entry.category == category
                }
            }
            
            // Filter by region if specified
            region?.let { regionFilter ->
                filteredEntries = filteredEntries.filter { 
                    it.location.contains(regionFilter, ignoreCase = true) 
                }
            }
            
            // Sort by score and update ranks
            val sortedEntries = filteredEntries.sortedByDescending { it.score }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
            
            emit(Result.Success(sortedEntries))
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to load leaderboard: ${e.message}"))
        }
    }

    fun getUserRank(
        userId: String,
        category: LeaderboardCategory = LeaderboardCategory.OVERALL
    ): Flow<Result<UserRankInfo>> = flow {
        emit(Result.Loading)
        
        try {
            delay(500)
            
            // Mock user rank calculation
            val userRank = UserRankInfo(
                currentRank = 15,
                totalParticipants = 1247,
                score = 2180,
                percentile = 88.5,
                rankChange = RankChange.UP,
                rankChangeValue = 3,
                nextRankScore = 2250,
                scoreToNextRank = 70
            )
            
            emit(Result.Success(userRank))
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to get user rank: ${e.message}"))
        }
    }

    fun getActiveEvents(): Flow<Result<List<LeaderboardEvent>>> = flow {
        emit(Result.Loading)
        
        try {
            delay(800)
            
            val activeEvents = mockEvents.filter { 
                it.status == EventStatus.ACTIVE || it.status == EventStatus.UPCOMING 
            }
            
            emit(Result.Success(activeEvents))
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to load events: ${e.message}"))
        }
    }

    fun getEventDetails(eventId: String): Flow<Result<LeaderboardEvent>> = flow {
        emit(Result.Loading)
        
        try {
            delay(500)
            
            val event = mockEvents.find { it.id == eventId }
            if (event != null) {
                emit(Result.Success(event))
            } else {
                emit(Result.Error("Event not found"))
            }
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to load event details: ${e.message}"))
        }
    }

    fun joinEvent(eventId: String, userId: String): Flow<Result<Boolean>> = flow {
        emit(Result.Loading)
        
        try {
            delay(1000)
            
            // Mock event joining logic
            val event = mockEvents.find { it.id == eventId }
            if (event != null && event.status == EventStatus.ACTIVE || event.status == EventStatus.UPCOMING) {
                // In real implementation, this would update the backend
                emit(Result.Success(true))
            } else {
                emit(Result.Error("Cannot join this event"))
            }
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to join event: ${e.message}"))
        }
    }

    fun getEventLeaderboard(eventId: String): Flow<Result<List<LeaderboardEntry>>> = flow {
        emit(Result.Loading)
        
        try {
            delay(800)
            
            // Mock event-specific leaderboard
            val eventLeaderboard = mockFarmers.take(10).mapIndexed { index, farmer ->
                farmer.copy(
                    rank = index + 1,
                    score = (2500 - (index * 50)) + (Math.random() * 100).toInt()
                )
            }.sortedByDescending { it.score }
            
            emit(Result.Success(eventLeaderboard))
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to load event leaderboard: ${e.message}"))
        }
    }

    fun getAchievements(userId: String): Flow<Result<List<Achievement>>> = flow {
        emit(Result.Loading)
        
        try {
            delay(600)
            
            val achievements = listOf(
                Achievement(
                    id = "ach_1",
                    title = "First Steps",
                    description = "Complete your first month of farming",
                    icon = "🐣",
                    isUnlocked = true,
                    unlockedDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
                    category = AchievementCategory.MILESTONE,
                    points = 100
                ),
                Achievement(
                    id = "ach_2",
                    title = "Growth Master",
                    description = "Achieve 95% feed efficiency for 3 consecutive months",
                    icon = "📈",
                    isUnlocked = true,
                    unlockedDate = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000),
                    category = AchievementCategory.PERFORMANCE,
                    points = 250
                ),
                Achievement(
                    id = "ach_3",
                    title = "Community Helper",
                    description = "Help 10 fellow farmers with advice and support",
                    icon = "🤝",
                    isUnlocked = false,
                    unlockedDate = null,
                    category = AchievementCategory.SOCIAL,
                    points = 200,
                    progress = 7,
                    maxProgress = 10
                ),
                Achievement(
                    id = "ach_4",
                    title = "Tech Innovator",
                    description = "Use AI features for 30 consecutive days",
                    icon = "🤖",
                    isUnlocked = false,
                    unlockedDate = null,
                    category = AchievementCategory.TECHNOLOGY,
                    points = 300,
                    progress = 18,
                    maxProgress = 30
                )
            )
            
            emit(Result.Success(achievements))
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to load achievements: ${e.message}"))
        }
    }

    fun getRegionalStats(region: String): Flow<Result<RegionalStats>> = flow {
        emit(Result.Loading)
        
        try {
            delay(700)
            
            val stats = RegionalStats(
                region = region,
                totalFarmers = 1247,
                avgScore = 2156.5,
                topPerformer = "Rajesh Kumar",
                avgBirdsPerFarm = 342,
                avgMonthlyRevenue = 95000.0,
                growthRate = 12.5,
                sustainabilityScore = 78.2
            )
            
            emit(Result.Success(stats))
            
        } catch (e: Exception) {
            emit(Result.Error("Failed to load regional stats: ${e.message}"))
        }
    }
}

// Data classes for leaderboard functionality
data class LeaderboardEntry(
    val id: String,
    val name: String,
    val location: String,
    val score: Int,
    val rank: Int,
    val category: LeaderboardCategory,
    val avatar: String?,
    val achievements: List<String>,
    val stats: FarmerStats
)

data class FarmerStats(
    val totalBirds: Int,
    val avgWeight: Double,
    val mortalityRate: Double,
    val feedEfficiency: Double,
    val monthlyRevenue: Double
)

data class UserRankInfo(
    val currentRank: Int,
    val totalParticipants: Int,
    val score: Int,
    val percentile: Double,
    val rankChange: RankChange,
    val rankChangeValue: Int,
    val nextRankScore: Int,
    val scoreToNextRank: Int
)

data class LeaderboardEvent(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val category: LeaderboardCategory,
    val prize: String,
    val participants: Int,
    val status: EventStatus,
    val rules: List<String>
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val unlockedDate: Long?,
    val category: AchievementCategory,
    val points: Int,
    val progress: Int = 0,
    val maxProgress: Int = 0
)

data class RegionalStats(
    val region: String,
    val totalFarmers: Int,
    val avgScore: Double,
    val topPerformer: String,
    val avgBirdsPerFarm: Int,
    val avgMonthlyRevenue: Double,
    val growthRate: Double,
    val sustainabilityScore: Double
)

enum class LeaderboardCategory {
    OVERALL,
    GROWTH_RATE,
    FEED_EFFICIENCY,
    HEALTH_MANAGEMENT,
    SUSTAINABILITY,
    REVENUE,
    INNOVATION
}

enum class LeaderboardTimeframe {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY,
    ALL_TIME
}

enum class EventStatus {
    UPCOMING,
    ACTIVE,
    COMPLETED,
    CANCELLED
}

enum class RankChange {
    UP,
    DOWN,
    SAME
}

enum class AchievementCategory {
    MILESTONE,
    PERFORMANCE,
    SOCIAL,
    TECHNOLOGY,
    SUSTAINABILITY,
    INNOVATION
}