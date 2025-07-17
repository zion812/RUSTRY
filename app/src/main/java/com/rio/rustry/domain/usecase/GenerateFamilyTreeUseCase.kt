package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.BreedingRepository
import com.rio.rustry.domain.model.FamilyTree
import com.rio.rustry.domain.model.Result

class GenerateFamilyTreeUseCase(
    private val breedingRepository: BreedingRepository
) {
    suspend operator fun invoke(fowlId: String): Result<FamilyTree> {
        return try {
            breedingRepository.generateFamilyTree(fowlId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}