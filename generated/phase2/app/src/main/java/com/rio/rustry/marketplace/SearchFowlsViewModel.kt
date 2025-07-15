// generated/phase2/app/src/main/java/com/rio/rustry/marketplace/SearchFowlsViewModel.kt

package com.rio.rustry.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.usecase.SearchFowlsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchFowlsViewModel @Inject constructor(
    private val searchFowlsUseCase: SearchFowlsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Load initial results
        search()
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun updateSort(sortOption: SortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
        search()
    }

    fun updateFilters(filters: SearchFilters) {
        _uiState.update { it.copy(filters = filters) }
        search()
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val searchParams = SearchParams(
                    query = _uiState.value.query,
                    filters = _uiState.value.filters,
                    sortOption = _uiState.value.sortOption
                )
                
                searchFowlsUseCase(searchParams).collect { result ->
                    result.fold(
                        onSuccess = { fowls ->
                            _uiState.update { 
                                it.copy(
                                    fowls = fowls,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Unknown error occurred"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun retry() {
        search()
    }
}

data class SearchUiState(
    val query: String = "",
    val fowls: List<Fowl> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val sortOption: SortOption = SortOption.NEWEST,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SearchParams(
    val query: String,
    val filters: SearchFilters,
    val sortOption: SortOption
)