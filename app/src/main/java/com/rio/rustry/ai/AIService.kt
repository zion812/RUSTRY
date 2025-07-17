package com.rio.rustry.ai

import android.graphics.Bitmap
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.HealthRecord
import com.rio.rustry.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * AI Service interface for all AI-powered features in Rustry
 */
interface AIService {
    
    // Image Recognition & Analysis
    suspend fun analyzePoultryImage(image: Bitmap): Result<ImageAnalysisResult>
    suspend fun detectBreed(image: Bitmap): Result<BreedDetectionResult>
    suspend fun assessHealth(image: Bitmap): Result<HealthAssessmentResult>
    suspend fun detectDiseases(image: Bitmap): Result<DiseaseDetectionResult>
    
    // Market Intelligence
    suspend fun predictPricing(fowl: Fowl): Result<PricePrediction>
    suspend fun getMarketTrends(breed: String, location: String): Result<MarketTrends>
    suspend fun recommendOptimalSellTime(fowl: Fowl): Result<SellTimeRecommendation>
    
    // Health Monitoring
    suspend fun generateHealthInsights(healthRecords: List<HealthRecord>): Result<HealthInsights>
    suspend fun predictHealthRisks(fowl: Fowl, healthHistory: List<HealthRecord>): Result<HealthRiskPrediction>
    suspend fun recommendVaccinations(fowl: Fowl): Result<VaccinationRecommendations>
    
    // Breeding Optimization
    suspend fun recommendBreedingPairs(availableFowls: List<Fowl>): Result<BreedingRecommendations>
    suspend fun predictOffspringTraits(parent1: Fowl, parent2: Fowl): Result<OffspringPrediction>
    suspend fun optimizeBreedingProgram(flockData: List<Fowl>): Result<BreedingProgram>
    
    // Feed & Nutrition
    suspend fun recommendFeedPlan(fowl: Fowl, goals: List<String>): Result<FeedRecommendations>
    suspend fun optimizeNutrition(flockData: List<Fowl>): Result<NutritionPlan>
    
    // Personalized Recommendations
    suspend fun getPersonalizedInsights(userId: String): Flow<PersonalizedInsights>
    suspend fun recommendFowlsForUser(userId: String, preferences: UserPreferences): Result<List<Fowl>>
    
    // Smart Alerts
    suspend fun generateSmartAlerts(userId: String): Result<List<SmartAlert>>
    
    // Performance Analytics
    suspend fun analyzeFarmPerformance(farmId: String): Result<FarmPerformanceAnalysis>
    suspend fun benchmarkAgainstIndustry(farmData: FarmData): Result<IndustryBenchmark>
}

// Data Models for AI Results

data class ImageAnalysisResult(
    val confidence: Float,
    val detectedObjects: List<DetectedObject>,
    val imageQuality: ImageQuality,
    val recommendations: List<String>
)

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class ImageQuality(
    val score: Float,
    val issues: List<String>,
    val suggestions: List<String>
)

data class BreedDetectionResult(
    val primaryBreed: String,
    val confidence: Float,
    val alternativeBreeds: List<BreedMatch>,
    val characteristics: List<String>
)

data class BreedMatch(
    val breed: String,
    val confidence: Float,
    val characteristics: List<String>
)

data class HealthAssessmentResult(
    val overallHealth: HealthStatus,
    val confidence: Float,
    val observations: List<HealthObservation>,
    val recommendations: List<String>,
    val urgency: UrgencyLevel
)

enum class HealthStatus {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}

enum class UrgencyLevel {
    LOW, MEDIUM, HIGH, URGENT
}

data class HealthObservation(
    val category: String,
    val observation: String,
    val severity: String,
    val confidence: Float
)

data class DiseaseDetectionResult(
    val detectedDiseases: List<DiseaseMatch>,
    val riskFactors: List<String>,
    val preventiveMeasures: List<String>,
    val treatmentRecommendations: List<String>
)

data class DiseaseMatch(
    val disease: String,
    val confidence: Float,
    val symptoms: List<String>,
    val severity: String
)

data class PricePrediction(
    val predictedPrice: Double,
    val priceRange: PriceRange,
    val confidence: Float,
    val factors: List<PriceFactor>,
    val marketConditions: String
)

data class PriceRange(
    val min: Double,
    val max: Double,
    val average: Double
)

data class PriceFactor(
    val factor: String,
    val impact: String,
    val weight: Float
)

data class MarketTrends(
    val currentTrend: String,
    val priceMovement: String,
    val demandLevel: String,
    val seasonalFactors: List<String>,
    val predictions: List<TrendPrediction>
)

data class TrendPrediction(
    val timeframe: String,
    val prediction: String,
    val confidence: Float
)

data class SellTimeRecommendation(
    val optimalTime: String,
    val reasoning: List<String>,
    val expectedPrice: Double,
    val marketFactors: List<String>
)

data class HealthInsights(
    val overallTrend: String,
    val keyMetrics: List<HealthMetric>,
    val alerts: List<String>,
    val recommendations: List<String>
)

data class HealthMetric(
    val metric: String,
    val value: String,
    val trend: String,
    val benchmark: String
)

data class HealthRiskPrediction(
    val riskLevel: String,
    val riskFactors: List<RiskFactor>,
    val preventiveMeasures: List<String>,
    val monitoringRecommendations: List<String>
)

data class RiskFactor(
    val factor: String,
    val severity: String,
    val likelihood: Float
)

data class VaccinationRecommendations(
    val upcomingVaccinations: List<VaccinationSchedule>,
    val overdueVaccinations: List<VaccinationSchedule>,
    val seasonalRecommendations: List<String>
)

data class VaccinationSchedule(
    val vaccine: String,
    val dueDate: String,
    val importance: String,
    val notes: String
)

data class BreedingRecommendations(
    val recommendedPairs: List<BreedingPair>,
    val reasoning: List<String>,
    val expectedOutcomes: List<String>
)

data class BreedingPair(
    val male: Fowl,
    val female: Fowl,
    val compatibilityScore: Float,
    val expectedTraits: List<String>
)

data class OffspringPrediction(
    val predictedTraits: List<TraitPrediction>,
    val healthPredictions: List<String>,
    val performancePredictions: List<String>
)

data class TraitPrediction(
    val trait: String,
    val probability: Float,
    val dominance: String
)

data class BreedingProgram(
    val goals: List<String>,
    val timeline: String,
    val recommendedActions: List<BreedingAction>,
    val expectedOutcomes: List<String>
)

data class BreedingAction(
    val action: String,
    val timing: String,
    val priority: String,
    val resources: List<String>
)

data class FeedRecommendations(
    val feedPlan: FeedPlan,
    val nutritionalGoals: List<String>,
    val supplements: List<Supplement>,
    val feedingSchedule: List<FeedingTime>
)

data class FeedPlan(
    val primaryFeed: String,
    val quantity: String,
    val frequency: String,
    val cost: Double
)

data class Supplement(
    val name: String,
    val purpose: String,
    val dosage: String,
    val timing: String
)

data class FeedingTime(
    val time: String,
    val feedType: String,
    val quantity: String
)

data class NutritionPlan(
    val overallStrategy: String,
    val individualPlans: List<IndividualNutritionPlan>,
    val groupRecommendations: List<String>,
    val costOptimization: List<String>
)

data class IndividualNutritionPlan(
    val fowlId: String,
    val specificNeeds: List<String>,
    val feedRecommendations: FeedRecommendations
)

data class PersonalizedInsights(
    val insights: List<Insight>,
    val recommendations: List<String>,
    val alerts: List<String>,
    val opportunities: List<String>
)

data class Insight(
    val category: String,
    val title: String,
    val description: String,
    val actionable: Boolean
)

data class UserPreferences(
    val preferredBreeds: List<String>,
    val budgetRange: PriceRange,
    val location: String,
    val experience: String,
    val goals: List<String>
)

data class SmartAlert(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val priority: String,
    val actionRequired: Boolean,
    val suggestedActions: List<String>
)

data class FarmPerformanceAnalysis(
    val overallScore: Float,
    val performanceMetrics: List<PerformanceMetric>,
    val strengths: List<String>,
    val improvementAreas: List<String>,
    val recommendations: List<String>
)

data class PerformanceMetric(
    val metric: String,
    val value: String,
    val benchmark: String,
    val trend: String
)

data class FarmData(
    val farmId: String,
    val fowls: List<Fowl>,
    val healthRecords: List<HealthRecord>,
    val salesData: List<SalesRecord>,
    val location: String,
    val farmSize: Double
)

data class SalesRecord(
    val id: String,
    val fowlId: String,
    val price: Double,
    val date: String,
    val buyer: String
)

data class IndustryBenchmark(
    val industryAverages: Map<String, String>,
    val ranking: String,
    val comparisonMetrics: List<ComparisonMetric>,
    val improvementOpportunities: List<String>
)

data class ComparisonMetric(
    val metric: String,
    val yourValue: String,
    val industryAverage: String,
    val performance: String
)