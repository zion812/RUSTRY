// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/GetFowlDetailUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.marketplace.FowlDetail
import com.rio.rustry.marketplace.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFowlDetailUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    suspend operator fun invoke(fowlId: String): Flow<Result<FowlDetail>> {
        return marketplaceRepository.getFowlDetail(fowlId)
    }
}