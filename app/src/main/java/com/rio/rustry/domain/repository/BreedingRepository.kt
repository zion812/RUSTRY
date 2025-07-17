package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.BreedingRecord
import com.rio.rustry.breeding.VaccinationEvent
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.model.FamilyTree
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for breeding operations
 */
interface BreedingRepository {
    
    /**
     * Get all breeding records for a user
     */
    fun getBreedingRecords(userId: String): Flow<Result<List<BreedingRecord>>>
    
    /**
     * Get breeding record by ID
     */
    suspend fun getBreedingRecordById(recordId: String): Result<BreedingRecord?>
    
    /**
     * Create a new breeding record
     */
    suspend fun createBreedingRecord(record: BreedingRecord): Result<String>
    
    /**
     * Update breeding record
     */
    suspend fun updateBreedingRecord(record: BreedingRecord): Result<Unit>
    
    /**
     * Delete breeding record
     */
    suspend fun deleteBreedingRecord(recordId: String): Result<Unit>
    
    /**
     * Get breeding analytics
     */
    suspend fun getBreedingAnalytics(userId: String): Result<BreedingAnalytics>
    
    /**
     * Get family tree data
     */
    suspend fun getFamilyTree(fowlId: String): Result<FamilyTreeData>
    
    /**
     * Generate family tree
     */
    suspend fun generateFamilyTree(fowlId: String): Result<FamilyTree>
    
    /**
     * Get breeding recommendations
     */
    suspend fun getBreedingRecommendations(fowlId: String): Result<List<BreedingRecommendation>>

    suspend fun getVaccinationEvents(fowlId: String): List<VaccinationEvent>
    suspend fun addVaccinationEvent(fowlId: String, event: VaccinationEvent)
    suspend fun updateVaccinationEvent(event: VaccinationEvent)
    suspend fun deleteVaccinationEvent(eventId: String)
    suspend fun markVaccinationComplete(eventId: String)
}

/**
 * Breeding analytics data class
 */
data class BreedingAnalytics(
    val totalBreedings: Int = 0,
    val successfulBreedings: Int = 0,
    val averageOffspringCount: Double = 0.0,
    val breedingSuccessRate: Double = 0.0,
    val popularBreeds: Map<String, Int> = emptyMap(),
    val seasonalTrends: Map<String, Int> = emptyMap()
)

/**
 * Family tree data class
 */
data class FamilyTreeData(
    val fowlId: String,
    val parents: List<String> = emptyList(),
    val offspring: List<String> = emptyList(),
    val siblings: List<String> = emptyList(),
    val generations: Int = 0
)

/**
 * Breeding recommendation data class
 */
data class BreedingRecommendation(
    val partnerId: String,
    val partnerBreed: String,
    val compatibilityScore: Double,
    val expectedTraits: List<String>,
    val reasoning: String
)