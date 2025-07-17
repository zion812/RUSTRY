package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.BreedingRepository
import com.rio.rustry.data.model.BreedingRecord
import com.rio.rustry.domain.model.Result

class CreateBreedingRecordUseCase(
    private val breedingRepository: BreedingRepository
) {
    suspend operator fun invoke(breedingRecord: BreedingRecord): Result<String> {
        return try {
            breedingRepository.createBreedingRecord(breedingRecord)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}