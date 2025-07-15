// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/DeleteFowlListingUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.listing.ListingRepository
import javax.inject.Inject

class DeleteFowlListingUseCase @Inject constructor(
    private val listingRepository: ListingRepository
) {
    suspend operator fun invoke(fowlId: String): Result<Unit> {
        return listingRepository.deleteListing(fowlId)
    }
}