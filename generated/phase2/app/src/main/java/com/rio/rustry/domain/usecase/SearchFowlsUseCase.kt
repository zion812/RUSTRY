// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/SearchFowlsUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.marketplace.MarketplaceRepository
import com.rio.rustry.marketplace.SearchParams
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchFowlsUseCase @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) {
    suspend operator fun invoke(params: SearchParams): Flow<Result<List<Fowl>>> {
        return marketplaceRepository.searchFowls(params)
    }
}