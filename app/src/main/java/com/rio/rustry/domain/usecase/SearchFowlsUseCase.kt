package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.FowlRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * Use case for searching fowls with debouncing and caching
 * 
 * Implements proper search patterns with performance optimizations
 */
@OptIn(FlowPreview::class)
class SearchFowlsUseCase(
    private val fowlRepository: FowlRepository
) {
    
    companion object {
        private const val DEBOUNCE_DELAY = 300L
        private const val MIN_QUERY_LENGTH = 2
    }
    
    /**
     * Search fowls with debouncing to prevent excessive API calls
     */
    fun invoke(queryFlow: Flow<String>): Flow<Result<List<Fowl>>> {
        return queryFlow
            .debounce(DEBOUNCE_DELAY)
            .distinctUntilChanged()
            .filter { query -> 
                query.trim().length >= MIN_QUERY_LENGTH || query.trim().isEmpty()
            }
            .flatMapLatest { query ->
                if (query.trim().isEmpty()) {
                    flowOf(Result.Success(emptyList()))
                } else {
                    fowlRepository.searchFowls(query.trim())
                        .onStart { emit(Result.Loading) }
                        .catch { exception ->
                            emit(Result.Error(exception))
                        }
                }
            }
    }
    
    /**
     * Search fowls immediately without debouncing (for manual triggers)
     */
    fun searchImmediate(query: String): Flow<Result<List<Fowl>>> {
        return if (query.trim().length < MIN_QUERY_LENGTH && query.trim().isNotEmpty()) {
            flowOf(Result.Success(emptyList()))
        } else {
            fowlRepository.searchFowls(query.trim())
                .onStart { emit(Result.Loading) }
                .catch { exception ->
                    emit(Result.Error(exception))
                }
        }
    }
}